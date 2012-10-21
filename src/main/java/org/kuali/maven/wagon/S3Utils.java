/**
 * Copyright 2010-2012 The Kuali Foundation
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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

/**
 * Utility methods related to Amazon S3
 */
public class S3Utils {
	private static final int KILOBYTE = 1024;
	private static final int MEGABYTE = 1024 * KILOBYTE;
	private static final int MULTI_PART_UPLOAD_THRESHOLD = 100 * MEGABYTE;
	private static final Logger log = LoggerFactory.getLogger(S3Utils.class);
	private static final int MAX_OBJECTS_PER_LISTING = 1001;

	/**
	 * Upload a single file to Amazon S3. If the file is larger than 100MB a multi-part upload is used. This splits the file into multiple
	 * smaller chunks with each chunk being uploaded in a different thread. Once all the threads have completed the file is reassembled on
	 * Amazon's side as a single file again.
	 */
	public static final void upload(File file, PutObjectRequest request, AmazonS3Client client, TransferManager manager) {
		// Store the file on S3
		if (file.length() < MULTI_PART_UPLOAD_THRESHOLD) {
			// Use normal upload for small files
			client.putObject(request);
		} else {
			log.debug("Blocking multi-part upload: " + file.getAbsolutePath());
			// Use multi-part upload for large files
			blockingMultiPartUpload(request, manager);
		}
	}

	/**
	 * Use this method to reliably upload large files and wait until they are fully uploaded before continuing. Behind the scenes this is
	 * accomplished by splitting the file up into manageable chunks and using separate threads to upload each chunk. Amazon recommends using
	 * a multi-part upload on files larger than 100MB. When this method returns all of the upload threads that handle portions of the file
	 * have completed. The file has also been reassembled on Amazon S3 and is ready for use.
	 */
	public static final void blockingMultiPartUpload(PutObjectRequest request, TransferManager manager) {
		// Use multi-part upload for large files
		Upload upload = manager.upload(request);
		try {
			// Block and wait for the upload to finish
			upload.waitForCompletion();
		} catch (Exception e) {
			throw new AmazonS3Exception("Unexpected error uploading file", e);
		}
	}

	public static final void getBucketStats(String accessKey, String secretKey, String bucketName) {
		Assert.notNull(accessKey, "accessKey cannot be null");
		Assert.notNull(secretKey, "secretKey cannot be null");
		Assert.notNull(bucketName, "bucketName cannot be null");
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		AmazonS3Client client = new AmazonS3Client(credentials);
		getBucketStats(client, bucketName);
	}

	public static final void getBucketStats(AmazonS3Client client, String bucketName) {
		if (!client.doesBucketExist(bucketName)) {
			throw new IllegalArgumentException("Bucket '" + bucketName + "' does not exist");
		}

		BucketSummary bucketSummary = new BucketSummary();
		ListObjectsRequest request = new ListObjectsRequest(bucketName, null, null, null, MAX_OBJECTS_PER_LISTING);
		ObjectListing current = client.listObjects(request);
		List<S3ObjectSummary> summaries = current.getObjectSummaries();
		updateBucketSummary(bucketSummary, summaries);
		while (current.isTruncated()) {
			current = client.listNextBatchOfObjects(current);
			updateBucketSummary(bucketSummary, current.getObjectSummaries());
			summaries.addAll(current.getObjectSummaries());
		}
	}

	protected static final void updateBucketSummary(BucketSummary summary, List<S3ObjectSummary> summaries) {
		SimpleFormatter sf = new SimpleFormatter();
		long totalObjectCount = summary.getObjectCount() + summaries.size();
		summary.setObjectCount(totalObjectCount);

		for (S3ObjectSummary element : summaries) {
			long totalSize = summary.getObjectSize() + element.getSize();
			summary.setObjectSize(totalSize);
		}
		log.info("Object count: " + summary.getObjectCount() + " Total Size: " + sf.getSize(summary.getObjectSize()));
	}
}
