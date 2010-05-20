/*
 * Copyright 2004-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.aws.maven;

import org.apache.commons.io.IOUtils;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the Maven Wagon interface that allows you to access the Amazon S3 service. URLs that reference
 * the S3 service should be in the form of <code>s3://bucket.name</code>. As an example
 * <code>s3://maven.kuali.org</code> puts files into the <code>maven.kuali.org</code> bucket on the S3 service.
 * <p/>
 * This implementation uses the <code>username</code> and <code>password</code> portions of the server authentication
 * metadata for credentials.
 * 
 * <code>
 * 
 * pom.xml 
 * <snapshotRepository> 
 *   <id>kuali.snapshot</id> 
 *   <name>Kuali Snapshot Repository</name>
 *   <url>s3://maven.kuali.org/snapshot</url> 
 *  </snapshotRepository>
 * 
 * settings.xml 
 * <server> 
 *   <id>ks.aws</id> 
 *   <username>[AWS Access Key ID]</username> 
 *   <password>[AWS Secret Access Key]</password> 
 * </server>
 * 
 * </code>
 * 
 * 1) Use password instead of passphrase for AWS Secret Access Key (Maven 3.0 is ignoring passphrase)<br>
 * 2) Fixed a bug in getBaseDir() if it was passed a one character string<br>
 * 3) Optimized buildDestinationPath to skip directory creation if the directory exists already
 * 
 * @author Ben Hale
 * @author Jeff Caddel
 * 
 */
public class SimpleStorageServiceWagon extends AbstractWagon {

	private S3Service service;

	private S3Bucket bucket;

	private String basedir;

	/**
	 * After we have created a directory, we cache it here. No need to create the same directory again. This speeds
	 * things up by ~20% for typical Maven usage. Also makes the timestamps more meaningful when looking to see when the
	 * directory was created.
	 */
	Map<String, S3Object> directoryCache = new HashMap<String, S3Object>();

	public SimpleStorageServiceWagon() {
		super(false);
	}

	protected void connectToRepository(Repository source, AuthenticationInfo authenticationInfo, ProxyInfo proxyInfo) throws AuthenticationException {
		try {
			service = new RestS3Service(getCredentials(authenticationInfo));
		} catch (S3ServiceException e) {
			throw new AuthenticationException("Cannot authenticate with current credentials", e);
		}
		bucket = new S3Bucket(source.getHost());
		basedir = getBaseDir(source);
	}

	protected boolean doesRemoteResourceExist(String resourceName) {
		try {
			service.getObjectDetails(bucket, basedir + resourceName);
		} catch (S3ServiceException e) {
			return false;
		}
		return true;
	}

	protected void disconnectFromRepository() {
		// Nothing to do for S3
	}

	/**
	 * Pull an object out of an S3 bucket and write it to a file
	 */
	protected void getResource(String resourceName, File destination, TransferProgress progress) throws ResourceDoesNotExistException, S3ServiceException, IOException {
		S3Object object;
		try {
			String key = basedir + resourceName;
			object = service.getObject(bucket, key);
		} catch (S3ServiceException e) {
			throw new ResourceDoesNotExistException("Resource " + resourceName + " does not exist in the repository", e);
		}

		InputStream in = null;
		OutputStream out = null;
		try {
			in = object.getDataInputStream();
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
	protected boolean isRemoteResourceNewer(String resourceName, long timestamp) throws S3ServiceException {
		S3Object object = service.getObjectDetails(bucket, basedir + resourceName);
		return object.getLastModifiedDate().compareTo(new Date(timestamp)) < 0;
	}

	/**
	 * List all of the objects in a given directory
	 */
	protected List<String> listDirectory(String directory) throws Exception {
		S3Object[] objects = service.listObjects(bucket, basedir + directory, "");
		List<String> fileNames = new ArrayList<String>(objects.length);
		for (S3Object object : objects) {
			fileNames.add(object.getKey());
		}
		return fileNames;
	}

	/**
	 * Store a resource into S3
	 */
	protected void putResource(File source, String destination, TransferProgress progress) throws S3ServiceException, IOException {
		// Create the directory hierarchy
		buildDestinationPath(getDestinationPath(destination));
		// Generate our key for this file
		String key = basedir + destination;
		// Create an S3 object and make it available to be read by the public
		S3Object object = new S3Object(key);
		object.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);
		object.setDataInputFile(source);
		object.setContentLength(source.length());

		InputStream in = null;
		try {
			service.putObject(bucket, object);

			in = new FileInputStream(source);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = in.read(buffer)) != -1) {
				progress.notify(buffer, length);
			}
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * Check to see if this key already exists. If it exists in our directoryCache or it exists in S3 return true. If it
	 * exists on S3 but is not in our cache, add it to our cache.
	 */
	protected boolean isExistingDirectory(String key) throws S3ServiceException {
		S3Object object = directoryCache.get(key);
		if (object != null) {
			return true;
		}
		try {
			object = service.getObjectDetails(bucket, key);
			directoryCache.put(key, object);
			return true;
		} catch (S3ServiceException e) {
			if (e.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
				return false;
			} else {
				throw e;
			}
		}
	}

	/**
	 * Create a directory if it does not exist already
	 */
	protected void createDirectoryIfNeeded(String key) throws S3ServiceException {
		if (isExistingDirectory(key)) {
			return;
		}
		// Create a directory and add it to our cache
		S3Object object = new S3Object(key);
		object.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);
		object.setContentLength(0);
		service.putObject(bucket, object);
		directoryCache.put(key, object);
	}

	/**
	 * We are about to place an object into S3. Make sure the directory structure the object is going to be placed into
	 * exists before we do so.
	 */
	protected void buildDestinationPath(String destination) throws S3ServiceException {
		String key = basedir + destination + "/";
		createDirectoryIfNeeded(key);
		int index = destination.lastIndexOf('/');
		if (index != -1) {
			buildDestinationPath(destination.substring(0, index));
		}
	}

	protected String getDestinationPath(String destination) {
		return destination.substring(0, destination.lastIndexOf('/'));
	}

	/**
	 * Convert "/" -> ""<br>
	 * Convert "/snapshot/" -> "snapshot/"<br>
	 * Convert "/snapshot" -> "snapshot/"<br>
	 */
	protected String getBaseDir(Repository source) {
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
	 * Create AWSCredentionals from the information in settings.xml
	 */
	protected AWSCredentials getCredentials(AuthenticationInfo authenticationInfo) throws AuthenticationException {
		if (authenticationInfo == null) {
			return null;
		}
		String accessKey = authenticationInfo.getUserName();
		String secretKey = authenticationInfo.getPassword();
		if (accessKey == null || secretKey == null) {
			throw new AuthenticationException("S3 requires a username and password to be set");
		}
		return new AWSCredentials(accessKey, secretKey);
	}
}
