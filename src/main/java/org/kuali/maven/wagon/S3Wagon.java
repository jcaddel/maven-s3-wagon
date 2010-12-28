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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.internal.Mimetypes;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.S3Object;

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
public class S3Wagon extends AbstractWagon {

    private S3Service service;

    private S3Bucket bucket;

    private String basedir;

    private final Mimetypes mimeTypes = Mimetypes.getInstance();

    public S3Wagon() {
        super(true);
        S3Listener listener = new S3Listener();
        super.addSessionListener(listener);
        super.addTransferListener(listener);
    }

    @Override
    protected void connectToRepository(final Repository source, final AuthenticationInfo authenticationInfo, final ProxyInfo proxyInfo)
            throws AuthenticationException {
        try {
            AWSCredentials credentials = getCredentials(authenticationInfo);
            service = new RestS3Service(credentials);
        } catch (S3ServiceException e) {
            throw new AuthenticationException("Cannot authenticate with current credentials", e);
        }
        try {
            bucket = service.getOrCreateBucket(source.getHost());
        } catch (S3ServiceException e) {
            throw new AuthenticationException("Cannot get or create bucket: " + source.getHost(), e);
        }
        basedir = getBaseDir(source);
    }

    @Override
    protected boolean doesRemoteResourceExist(final String resourceName) {
        try {
            service.getObjectDetails(bucket, basedir + resourceName);
        } catch (S3ServiceException e) {
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
            throws ResourceDoesNotExistException, S3ServiceException, IOException {
        // Obtain the object from S3
        S3Object object = null;
        try {
            String key = basedir + resourceName;
            object = service.getObject(bucket, key);
        } catch (S3ServiceException e) {
            throw new ResourceDoesNotExistException("Resource " + resourceName + " does not exist in the repository", e);
        }

        //
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
    @Override
    protected boolean isRemoteResourceNewer(final String resourceName, final long timestamp) throws S3ServiceException {
        S3Object object = service.getObjectDetails(bucket, basedir + resourceName);
        return object.getLastModifiedDate().compareTo(new Date(timestamp)) < 0;
    }

    /**
     * List all of the objects in a given directory
     */
    @Override
    protected List<String> listDirectory(final String directory) throws Exception {
        S3Object[] objects = service.listObjects(bucket, basedir + directory, "");
        List<String> fileNames = new ArrayList<String>(objects.length);
        for (S3Object object : objects) {
            fileNames.add(object.getKey());
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

    /**
     * Create an S3Object based on the source file and destination passed in
     */
    protected S3Object createS3Object(final File source, final String destination, final TransferProgress progress)
            throws FileNotFoundException {

        // Generate our bucket key for this file
        String key = getNormalizedKey(source, destination);

        // Create an S3 object
        S3Object object = new S3Object(key);

        // Make it available to the public
        object.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);
        // object.setDataInputFile(source);
        object.setDataInputStream(new TransferProgressFileInputStream(source, progress));
        object.setContentLength(source.length());

        // Set the mime type according to the extension of the destination file
        String mimeType = mimeTypes.getMimetype(destination);
        object.setContentType(mimeType);
        return object;
    }

    /**
     * Store a resource into S3
     */
    @Override
    protected void putResource(final File source, final String destination, final TransferProgress progress) throws S3ServiceException,
            IOException {

        // Create a new S3Object
        S3Object object = createS3Object(source, destination, progress);

        // Store the file on S3
        service.putObject(bucket, object);
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
        return new AWSCredentials(accessKey, secretKey);
    }
}
