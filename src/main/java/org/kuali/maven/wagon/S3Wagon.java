/*
 * Copyright 2004-2007 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package org.kuali.maven.wagon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.internal.Mimetypes;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * An implementation of the Maven Wagon interface that is integrated with the Amazon S3 service. URLs that reference the
 * S3 service should be in the form of <code>s3://bucket.name</code>. As an example <code>s3://maven.kuali.org</code>
 * puts files into the <code>maven.kuali.org</code> bucket on the S3 service.
 * <p/>
 * This implementation uses the <code>username</code> and <code>password</code> portions of the server authentication
 * metadata for credentials. <code>
 *
 * pom.xml
 * <snapshotRepository>
 *   <id>kuali.snapshot</id>
 *   <name>Kuali Snapshot Repository</name>
 *   <url>s3://maven.kuali.org/snapshot</url>
 * </snapshotRepository>
 *
 * settings.xml
 * <server>
 *   <id>kuali.snapshot</id>
 *   <username>[AWS Access Key ID]</username>
 *   <password>[AWS Secret Access Key]</password>
 * </server>
 *
 * </code> Kuali Updates -------------<br>
 * 1) Use username/password instead of passphrase/privatekey for AWS credentials (Maven 3.0 is ignoring passphrase)<br>
 * 2) Fixed a bug in getBaseDir() if it was passed a one character string<br>
 * 3) Removed directory creation. The concept of a "directory" inside an AWS bucket is not needed for tools like S3Fox,
 * Bucket Explorer and https://s3browse.springsource.com/browse/maven.kuali.org/snapshot to correctly display the
 * contents of the bucket
 *
 * @author Ben Hale
 * @author Jeff Caddel
 */
public class S3Wagon extends AbstractWagon implements RequestFactory {
    public static final String THREADS_KEY = "maven.wagon.threads";
    public static final int DEFAULT_THREAD_COUNT = 10;

    SimpleFormatter formatter = new SimpleFormatter();
    int threadCount = getThreadCount();

    final Logger log = LoggerFactory.getLogger(S3Listener.class);

    private AmazonS3Client client;

    private Bucket bucket;

    private String basedir;

    private final Mimetypes mimeTypes = Mimetypes.getInstance();

    public S3Wagon() {
        super(true);
        S3Listener listener = new S3Listener();
        super.addSessionListener(listener);
        super.addTransferListener(listener);
    }

    protected Bucket getOrCreateBucket(final AmazonS3Client client, final String bucketName) {
        List<Bucket> buckets = client.listBuckets();
        for (Bucket bucket : buckets) {
            if (bucket.getName().equals(bucketName)) {
                return bucket;
            }
        }
        return client.createBucket(bucketName);
    }

    @Override
    protected void connectToRepository(final Repository source, final AuthenticationInfo authenticationInfo,
            final ProxyInfo proxyInfo) throws AuthenticationException {

        AWSCredentials credentials = getCredentials(authenticationInfo);
        client = new AmazonS3Client(credentials);
        bucket = getOrCreateBucket(client, source.getHost());
        basedir = getBaseDir(source);
    }

    @Override
    protected boolean doesRemoteResourceExist(final String resourceName) {
        try {
            client.getObjectMetadata(bucket.getName(), basedir + resourceName);
        } catch (AmazonClientException e1) {
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
    protected void getResource(final String resourceName, final File destination, final TransferProgress progress)
            throws ResourceDoesNotExistException, IOException {
        // Obtain the object from S3
        S3Object object = null;
        try {
            String key = basedir + resourceName;
            object = client.getObject(bucket.getName(), key);
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
        ObjectMetadata metadata = client.getObjectMetadata(bucket.getName(), basedir + resourceName);
        return metadata.getLastModified().compareTo(new Date(timestamp)) < 0;
    }

    /**
     * List all of the objects in a given directory
     */
    @Override
    protected List<String> listDirectory(final String directory) throws Exception {
        ObjectListing objectListing = client.listObjects(bucket.getName(), basedir + directory);
        List<String> fileNames = new ArrayList<String>();
        for (S3ObjectSummary summary : objectListing.getObjectSummaries()) {
            fileNames.add(summary.getKey());
        }
        return fileNames;
    }

    /**
     * Normalize the key to our S3 object<br>
     * 1. Convert "./css/style.css" into "/css/style.css"<br>
     * 2. Convert "/foo/bar/../../css/style.css" into "/css/style.css"
     *
     * @see java.net.URI.normalize()
     */
    protected String getNormalizedKey(final File source, final String destination) {
        // Generate our bucket key for this file
        String key = basedir + destination;
        try {
            String prefix = "http://s3.amazonaws.com/" + bucket.getName() + "/";
            String urlString = prefix + key;
            URI rawURI = new URI(urlString);
            URI normalizedURI = rawURI.normalize();
            String normalized = normalizedURI.toString();
            int pos = normalized.indexOf(prefix) + prefix.length();
            String normalizedKey = normalized.substring(pos);
            return normalizedKey;
        } catch (Exception e) {
            throw new RuntimeException(e);
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
            return new FileInputStream(source);
        } else {
            return new TransferProgressFileInputStream(source, progress);
        }
    }

    /**
     * Create a PutObjectRequest based on the source file and destination passed in
     */
    protected PutObjectRequest getPutObjectRequest(File source, String destination, TransferProgress progress) {
        try {
            String key = getNormalizedKey(source, destination);
            String bucketName = bucket.getName();
            InputStream input = getInputStream(source, progress);
            ObjectMetadata metadata = getObjectMetadata(source, destination);
            PutObjectRequest request = new PutObjectRequest(bucketName, key, input, metadata);
            request.setCannedAcl(CannedAccessControlList.PublicRead);
            return request;
        } catch (FileNotFoundException e) {
            throw new AmazonServiceException("File not found", e);
        }
    }

    /**
     * On S3 there are no true "directories". An S3 bucket is essentially a Hashtable of files stored by key. The
     * integration between a traditional file system and an S3 bucket is to use the path of the file on the local file
     * system as the key to the file in the bucket. The S3 bucket does not contain a separate key for the directory
     * itself.
     */
    public final void putDirectory(File sourceDir, String destinationDir) throws TransferFailedException {
        List<PutFileContext> contexts = getPutFileContexts(sourceDir, destinationDir);
        long bytes = sum(contexts);
        ThreadHandler handler = getThreadHandler(contexts);
        log.info(getPutDirMsg(sourceDir, contexts.size(), bytes, handler));
        handler.executeThreads();
        if (handler.getException() != null) {
            throw new TransferFailedException("Unexpected error", handler.getException());
        }
    }

    protected String getPutDirMsg(File sourceDir, int fileCount, long bytes, ThreadHandler handler) {
        StringBuilder sb = new StringBuilder();
        sb.append("Uploading: '" + sourceDir.getAbsolutePath() + "'");
        sb.append(" Files: " + fileCount);
        sb.append(" Size: " + formatter.getSize(bytes));
        sb.append(" Threads: " + handler.getThreads().length);
        sb.append(" Requests Per Thread: " + handler.getRequestsPerThread());
        return sb.toString();
    }

    protected int getRequestsPerThread(int threads, int requests) {
        int requestsPerThread = requests / threads;
        while (requestsPerThread * threads < requests) {
            requestsPerThread++;
        }
        return requestsPerThread;
    }

    protected ThreadHandler getThreadHandler(List<PutFileContext> contexts) {
        int requestsPerThread = getRequestsPerThread(threadCount, contexts.size());
        ThreadHandler handler = new ThreadHandler();
        handler.setRequestsPerThread(requestsPerThread);
        ThreadGroup group = new ThreadGroup("S3 Uploaders");
        group.setDaemon(true);
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threads.length; i++) {
            int offset = i * requestsPerThread;
            int length = requestsPerThread;
            PutThreadContext context = getPutThreadContext(handler, offset, length);
            context.setContexts(contexts);
            int id = i + 1;
            context.setId(id);
            Runnable runnable = new PutThread(context);
            threads[i] = new Thread(group, runnable, "S3-" + id);
            threads[i].setUncaughtExceptionHandler(handler);
            threads[i].setDaemon(true);
        }
        handler.setGroup(group);
        handler.setThreads(threads);
        return handler;
    }

    protected PutThreadContext getPutThreadContext(ThreadHandler handler, int offset, int length) {
        PutThreadContext context = new PutThreadContext();
        context.setClient(client);
        context.setFactory(this);
        context.setHandler(handler);
        context.setOffset(offset);
        context.setLength(length);
        return context;
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
    protected void putResource(final File source, final String destination, final TransferProgress progress)
            throws IOException {

        // Create a new S3Object
        PutObjectRequest request = getPutObjectRequest(source, destination, progress);

        // Store the file on S3
        client.putObject(request);
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

    protected String getAuthenticationErrorMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append("The S3 wagon needs AWS Access Key set as the username and AWS Secret Key set as the password. eg:\n");
        sb.append("<server>\n");
        sb.append("  <id>my.server</id>\n");
        sb.append("  <username>[AWS Access Key ID]</username>\n");
        sb.append("  <password>[AWS Secret Access Key]</password>\n");
        sb.append("</server>\n");
        return sb.toString();
    }

    /**
     * Create AWSCredentionals from the information in settings.xml
     */
    protected AWSCredentials getCredentials(final AuthenticationInfo authenticationInfo) throws AuthenticationException {
        if (authenticationInfo == null) {
            throw new AuthenticationException(getAuthenticationErrorMessage());
        }
        String accessKey = authenticationInfo.getUserName();
        String secretKey = authenticationInfo.getPassword();
        if (accessKey == null || secretKey == null) {
            throw new AuthenticationException(getAuthenticationErrorMessage());
        }
        return new BasicAWSCredentials(accessKey, secretKey);
    }

    protected int getThreadCount() {
        String threadCount = System.getProperty(THREADS_KEY);
        if (StringUtils.isEmpty(threadCount)) {
            return DEFAULT_THREAD_COUNT;
        } else {
            return new Integer(threadCount);
        }
    }

}
