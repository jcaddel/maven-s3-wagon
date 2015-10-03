/**
 * Copyright 2010-2015 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.maven.wagon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.repository.RepositoryPermissions;
import org.kuali.common.aws.s3.S3Utils;
import org.kuali.common.aws.s3.SimpleFormatter;
import org.kuali.common.threads.ExecutionStatistics;
import org.kuali.common.threads.ThreadHandlerContext;
import org.kuali.common.threads.ThreadInvoker;
import org.kuali.common.threads.listener.PercentCompleteListener;
import org.kuali.maven.wagon.auth.AwsCredentials;
import org.kuali.maven.wagon.auth.AwsEncryption;
import org.kuali.maven.wagon.auth.AwsSessionCredentials;
import org.kuali.maven.wagon.auth.MavenAwsCredentialsProviderChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.internal.Mimetypes;
import com.amazonaws.services.s3.internal.RepeatableFileInputStream;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CryptoConfiguration;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.google.common.base.Optional;

/**
 * <p>
 * An implementation of the Maven Wagon interface that is integrated with the Amazon S3 service.
 * </p>
 * 
 * <p>
 * URLs that reference the S3 service should be in the form of <code>s3://bucket.name</code>. As an example <code>s3://maven.kuali.org</code> puts files into the
 * <code>maven.kuali.org</code> bucket on the S3 service.
 * </p>
 * 
 * <p>
 * This implementation uses the <code>username</code> and <code>password</code> portions of the server authentication metadata for credentials.
 * </p>
 * 
 * @plexus.component role="org.apache.maven.wagon.Wagon" role-hint="http" instantiation-strategy="per-lookup"
 * 
 * @author Ben Hale
 * @author Jeff Caddel
 */
public class S3Wagon extends AbstractWagon implements RequestFactory {

	/**
	 * Set the system property <code>maven.wagon.protocol</code> to <code>http</code> to force the wagon to communicate over <code>http</code>. Default is <code>https</code>.
	 */
	public static final String PROTOCOL_KEY = "maven.wagon.protocol";
	public static final String HTTP = "http";
	public static final String HTTP_ENDPOINT_VALUE = "http://s3.amazonaws.com";
	public static final String HTTPS = "https";
	public static final String MIN_THREADS_KEY = "maven.wagon.threads.min";
	public static final String MAX_THREADS_KEY = "maven.wagon.threads.max";
	public static final String DIVISOR_KEY = "maven.wagon.threads.divisor";
	public static final int DEFAULT_MIN_THREAD_COUNT = 10;
	public static final int DEFAULT_MAX_THREAD_COUNT = 50;
	public static final int DEFAULT_DIVISOR = 50;
	public static final int DEFAULT_READ_TIMEOUT = 60 * 1000;
	public static final CannedAccessControlList DEFAULT_ACL = CannedAccessControlList.PublicRead;
	private static final File TEMP_DIR = getCanonicalFile(System.getProperty("java.io.tmpdir"));
	private static final String TEMP_DIR_PATH = TEMP_DIR.getAbsolutePath();

	ThreadInvoker invoker = new ThreadInvoker();
	SimpleFormatter formatter = new SimpleFormatter();
	int minThreads = getMinThreads();
	int maxThreads = getMaxThreads();
	int divisor = getDivisor();
	String protocol = getValue(PROTOCOL_KEY, HTTPS);
	boolean http = HTTP.equals(protocol);
	int readTimeout = DEFAULT_READ_TIMEOUT;
	CannedAccessControlList acl = DEFAULT_ACL;
	TransferManager transferManager;

	private static final Logger log = LoggerFactory.getLogger(S3Wagon.class);

	AmazonS3Client client;
	String bucketName;
	String basedir;

	private final Mimetypes mimeTypes = Mimetypes.getInstance();

	public S3Wagon() {
		super(true);
		S3Listener listener = new S3Listener();
		super.addSessionListener(listener);
		super.addTransferListener(listener);
	}

	protected void validateBucket(AmazonS3Client client, String bucketName) {
		log.debug("Looking for bucket: " + bucketName);
		if (client.doesBucketExist(bucketName)) {
			log.debug("Found bucket '" + bucketName + "' Validating permissions");
			validatePermissions(client, bucketName);
		} else {
			log.info("Creating bucket " + bucketName);
			// If we create the bucket, we "own" it and by default have the "fullcontrol" permission
			client.createBucket(bucketName);
		}
	}

	/**
	 * Establish that we have enough permissions on this bucket to do what we need to do
	 */
	protected void validatePermissions(AmazonS3Client client, String bucketName) {
		// This establishes our ability to list objects in this bucket
		ListObjectsRequest zeroObjectsRequest = new ListObjectsRequest(bucketName, null, null, null, 0);
		client.listObjects(zeroObjectsRequest);

		/**
		 * The current AWS Java SDK does not appear to have a simple method for discovering what set of permissions the currently authenticated user has on a bucket. The AWS dev's
		 * suggest that you attempt to perform an operation that would fail if you don't have the permission in question. You would then use the success/failure of that attempt to
		 * establish what your permissions are. This is definitely not ideal and they are working on it, but it is not ready yet.
		 */

		// Do something simple and quick to verify that we have write permissions on this bucket
		// One way to do this would be to create an object in this bucket, and then immediately delete it
		// That seems messy, inconvenient, and lame.

	}

	protected CannedAccessControlList getAclFromRepository(Repository repository) {
		RepositoryPermissions permissions = repository.getPermissions();
		if (permissions == null) {
			return null;
		}
		String filePermissions = permissions.getFileMode();
		if (StringUtils.isBlank(filePermissions)) {
			return null;
		}
		return CannedAccessControlList.valueOf(filePermissions.trim());
	}

	protected ClientConfiguration getClientConfiguration() {
		ClientConfiguration configuration = new ClientConfiguration();
		if (http) {
			log.info("http selected");
			configuration.setProtocol(Protocol.HTTP);
		}
		return configuration;
	}

	protected EncryptionMaterials getMaterials(AuthenticationInfo auth) {
		try {
			return AwsEncryption.getMaterials(auth);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected AmazonS3Client getAmazonS3Client(AWSCredentials credentials, EncryptionMaterials materials ) {
		ClientConfiguration configuration = getClientConfiguration();
		if(materials == null) {
			return new AmazonS3Client(credentials, configuration);
		} else {
			return new AmazonS3EncryptionClient(credentials, materials, configuration, new CryptoConfiguration());
		}
	}

	@Override
	protected void connectToRepository(Repository source, AuthenticationInfo auth, ProxyInfo proxy) {
		
		// Required login credentials.
		AWSCredentials credentials = getCredentials(auth);
		
		// Optional encryption materials.
		EncryptionMaterials materials = getMaterials(auth);
		if(materials != null) {
			log.info("Using encryption.");
		}
		
		this.client = getAmazonS3Client(credentials, materials);
		this.transferManager = new TransferManager(client);
		this.bucketName = source.getHost();
		validateBucket(client, bucketName);
		this.basedir = getBaseDir(source);

		// If they've specified <filePermissions> in settings.xml, that always wins
		CannedAccessControlList repoAcl = getAclFromRepository(source);
		if (repoAcl != null) {
			log.info("File permissions: " + repoAcl.name());
			acl = repoAcl;
		}
	}

	@Override
	protected boolean doesRemoteResourceExist(final String resourceName) {
		try {
			client.getObjectMetadata(bucketName, basedir + resourceName);
		} catch (AmazonClientException e) {
			return false;
		}
		return true;
	}

	@Override
	protected void disconnectFromRepository() {
		// Nothing to do for S3
	}

	/**
	 * Pull an object out of an S3 bucket and write it to a file
	 */
	@Override
	protected void getResource(final String resourceName, final File destination, final TransferProgress progress) throws ResourceDoesNotExistException, IOException {
		// Obtain the object from S3
		S3Object object = null;
		try {
			String key = basedir + resourceName;
			object = client.getObject(bucketName, key);
		} catch (Exception e) {
			throw new ResourceDoesNotExistException("Resource " + resourceName + " does not exist in the repository", e);
		}

		//
		InputStream in = null;
		OutputStream out = null;
		try {
			in = object.getObjectContent();
			out = new TransferProgressFileOutputStream(destination, progress);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = in.read(buffer)) != -1) {
				out.write(buffer, 0, length);
			}
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * Is the S3 object newer than the timestamp passed in?
	 */
	@Override
	protected boolean isRemoteResourceNewer(final String resourceName, final long timestamp) {
		ObjectMetadata metadata = client.getObjectMetadata(bucketName, basedir + resourceName);
		return metadata.getLastModified().compareTo(new Date(timestamp)) < 0;
	}

	/**
	 * List all of the objects in a given directory
	 */
	@Override
	protected List<String> listDirectory(String directory) throws Exception {
		// info("directory=" + directory);
		if (StringUtils.isBlank(directory)) {
			directory = "";
		}
		String delimiter = "/";
		String prefix = basedir + directory;
		if (!prefix.endsWith(delimiter)) {
			prefix += delimiter;
		}
		// info("prefix=" + prefix);
		ListObjectsRequest request = new ListObjectsRequest();
		request.setBucketName(bucketName);
		request.setPrefix(prefix);
		request.setDelimiter(delimiter);
		ObjectListing objectListing = client.listObjects(request);
		// info("truncated=" + objectListing.isTruncated());
		// info("prefix=" + prefix);
		// info("basedir=" + basedir);
		List<String> fileNames = new ArrayList<String>();
		for (S3ObjectSummary summary : objectListing.getObjectSummaries()) {
			// info("summary.getKey()=" + summary.getKey());
			String key = summary.getKey();
			String relativeKey = key.startsWith(basedir) ? key.substring(basedir.length()) : key;
			boolean add = !StringUtils.isBlank(relativeKey) && !relativeKey.equals(directory);
			if (add) {
				// info("Adding key - " + relativeKey);
				fileNames.add(relativeKey);
			}
		}
		for (String commonPrefix : objectListing.getCommonPrefixes()) {
			String value = commonPrefix.startsWith(basedir) ? commonPrefix.substring(basedir.length()) : commonPrefix;
			// info("commonPrefix=" + commonPrefix);
			// info("relativeValue=" + relativeValue);
			// info("Adding common prefix - " + value);
			fileNames.add(value);
		}
		// StringBuilder sb = new StringBuilder();
		// sb.append("\n");
		// for (String fileName : fileNames) {
		// sb.append(fileName + "\n");
		// }
		// info(sb.toString());
		return fileNames;
	}

	protected void info(String msg) {
		System.out.println("[INFO] " + msg);
	}

	/**
	 * Normalize the key to our S3 object:<br>
	 * Convert <code>./css/style.css</code> into <code>/css/style.css</code><br>
	 * Convert <code>/foo/bar/../../css/style.css<code> into <code>/css/style.css</code><br>
	 */
	protected String getCanonicalKey(String key) {
		// release/./css/style.css
		String path = basedir + key;

		// /temp/release/css/style.css
		File file = getCanonicalFile(new File(TEMP_DIR, path));
		String canonical = file.getAbsolutePath();

		// release/css/style.css
		int pos = TEMP_DIR_PATH.length() + 1;
		String suffix = canonical.substring(pos);

		// Always replace backslash with forward slash just in case we are running on Windows
		String canonicalKey = suffix.replace("\\", "/");

		// Return the canonical key
		return canonicalKey;
	}

	protected static File getCanonicalFile(String path) {
		return getCanonicalFile(new File(path));
	}

	protected static File getCanonicalFile(File file) {
		try {
			return new File(file.getCanonicalPath());
		} catch (IOException e) {
			throw new IllegalArgumentException("Unexpected IO error", e);
		}
	}

	protected ObjectMetadata getObjectMetadata(final File source, final String destination) {
		// Set the mime type according to the extension of the destination file
		String contentType = mimeTypes.getMimetype(destination);
		long contentLength = source.length();

		ObjectMetadata omd = new ObjectMetadata();
		omd.setContentLength(contentLength);
		omd.setContentType(contentType);
		return omd;
	}

	/**
	 * Create a PutObjectRequest based on the PutContext
	 */
	public PutObjectRequest getPutObjectRequest(PutFileContext context) {
		File source = context.getSource();
		String destination = context.getDestination();
		TransferProgress progress = context.getProgress();
		return getPutObjectRequest(source, destination, progress);
	}

	protected InputStream getInputStream(File source, TransferProgress progress) throws FileNotFoundException {
		if (progress == null) {
			return new RepeatableFileInputStream(source);
		} else {
			return new TransferProgressFileInputStream(source, progress);
		}
	}

	/**
	 * Create a PutObjectRequest based on the source file and destination passed in
	 */
	protected PutObjectRequest getPutObjectRequest(File source, String destination, TransferProgress progress) {
		try {
			String key = getCanonicalKey(destination);
			InputStream input = getInputStream(source, progress);
			ObjectMetadata metadata = getObjectMetadata(source, destination);
			PutObjectRequest request = new PutObjectRequest(bucketName, key, input, metadata);
			request.setCannedAcl(acl);
			return request;
		} catch (FileNotFoundException e) {
			throw new AmazonServiceException("File not found", e);
		}
	}

	/**
	 * On S3 there are no true "directories". An S3 bucket is essentially a Hashtable of files stored by key. The integration between a traditional file system and an S3 bucket is
	 * to use the path of the file on the local file system as the key to the file in the bucket. The S3 bucket does not contain a separate key for the directory itself.
	 */
	public final void putDirectory(File sourceDir, String destinationDir) throws TransferFailedException {

		// Examine the contents of the directory
		List<PutFileContext> contexts = getPutFileContexts(sourceDir, destinationDir);
		for (PutFileContext context : contexts) {
			// Progress is tracked by the thread handler when uploading files this way
			context.setProgress(null);
		}

		// Sum the total bytes in the directory
		long bytes = sum(contexts);

		// Show what we are up to
		log.info(getUploadStartMsg(contexts.size(), bytes));

		// Store some context for the thread handler
		ThreadHandlerContext<PutFileContext> thc = new ThreadHandlerContext<PutFileContext>();
		thc.setList(contexts);
		thc.setHandler(new FileHandler());
		thc.setMax(maxThreads);
		thc.setMin(minThreads);
		thc.setDivisor(divisor);
		thc.setListener(new PercentCompleteListener<PutFileContext>());

		// Invoke the threads
		ExecutionStatistics stats = invoker.invokeThreads(thc);

		// Show some stats
		long millis = stats.getExecutionTime();
		long count = stats.getIterationCount();
		log.info(getUploadCompleteMsg(millis, bytes, count));
	}

	protected String getUploadCompleteMsg(long millis, long bytes, long count) {
		String rate = formatter.getRate(millis, bytes);
		String time = formatter.getTime(millis);
		StringBuilder sb = new StringBuilder();
		sb.append("Files: " + count);
		sb.append("  Time: " + time);
		sb.append("  Rate: " + rate);
		return sb.toString();
	}

	protected String getUploadStartMsg(int fileCount, long bytes) {
		StringBuilder sb = new StringBuilder();
		sb.append("Files: " + fileCount);
		sb.append("  Bytes: " + formatter.getSize(bytes));
		return sb.toString();
	}

	protected int getRequestsPerThread(int threads, int requests) {
		int requestsPerThread = requests / threads;
		while (requestsPerThread * threads < requests) {
			requestsPerThread++;
		}
		return requestsPerThread;
	}

	protected long sum(List<PutFileContext> contexts) {
		long sum = 0;
		for (PutFileContext context : contexts) {
			File file = context.getSource();
			long length = file.length();
			sum += length;
		}
		return sum;
	}

	/**
	 * Store a resource into S3
	 */
	@Override
	protected void putResource(final File source, final String destination, final TransferProgress progress) throws IOException {

		// Create a new PutObjectRequest
		PutObjectRequest request = getPutObjectRequest(source, destination, progress);

		// Upload the file to S3, using multi-part upload for large files
		S3Utils.getInstance().upload(source, request, client, transferManager);
	}

	protected String getDestinationPath(final String destination) {
		return destination.substring(0, destination.lastIndexOf('/'));
	}

	/**
	 * Convert "/" -> ""<br>
	 * Convert "/snapshot/" -> "snapshot/"<br>
	 * Convert "/snapshot" -> "snapshot/"<br>
	 */
	protected String getBaseDir(final Repository source) {
		StringBuilder sb = new StringBuilder(source.getBasedir());
		sb.deleteCharAt(0);
		if (sb.length() == 0) {
			return "";
		}
		if (sb.charAt(sb.length() - 1) != '/') {
			sb.append('/');
		}
		return sb.toString();
	}

	/**
	 * Create AWSCredentionals from the information in system properties, environment variables, settings.xml, or EC2 instance metadata (only applicable when running the wagon on
	 * an Amazon EC2 instance)
	 */
	protected AWSCredentials getCredentials(final AuthenticationInfo authenticationInfo) {
		Optional<AuthenticationInfo> auth = Optional.fromNullable(authenticationInfo);
		AWSCredentialsProviderChain chain = new MavenAwsCredentialsProviderChain(auth);
		AWSCredentials credentials = chain.getCredentials();
		if (credentials instanceof AWSSessionCredentials) {
			return new AwsSessionCredentials((AWSSessionCredentials) credentials);
		} else {
			return new AwsCredentials(credentials);
		}
	}

	@Override
	protected PutFileContext getPutFileContext(File source, String destination) {
		PutFileContext context = super.getPutFileContext(source, destination);
		context.setFactory(this);
		context.setTransferManager(this.transferManager);
		context.setClient(this.client);
		return context;
	}

	protected int getMinThreads() {
		return getValue(MIN_THREADS_KEY, DEFAULT_MIN_THREAD_COUNT);
	}

	protected int getMaxThreads() {
		return getValue(MAX_THREADS_KEY, DEFAULT_MAX_THREAD_COUNT);
	}

	protected int getDivisor() {
		return getValue(DIVISOR_KEY, DEFAULT_DIVISOR);
	}

	protected int getValue(String key, int defaultValue) {
		String value = System.getProperty(key);
		if (StringUtils.isEmpty(value)) {
			return defaultValue;
		} else {
			return new Integer(value);
		}
	}

	protected String getValue(String key, String defaultValue) {
		String value = System.getProperty(key);
		if (StringUtils.isEmpty(value)) {
			return defaultValue;
		} else {
			return value;
		}
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

}
