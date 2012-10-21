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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;

public class S3UtilsTest {
	private static final String ACCESSKEY = "AKIAJFD5IM7IPVVUEBNA";
	private static final String SECRETYKEY = System.getProperty("secret.key");

	private static final Logger log = LoggerFactory.getLogger(S3UtilsTest.class);

	protected AWSCredentials getCredentials() {
		log.debug("access key: " + ACCESSKEY);
		return new BasicAWSCredentials(ACCESSKEY, SECRETYKEY);
	}

	protected AmazonS3Client getClient() {
		AWSCredentials credentials = getCredentials();
		return new AmazonS3Client(credentials);
	}

	protected ListObjectsRequest getListObjectsRequest(String bucketName, String prefix, String delimiter) {
		ListObjectsRequest request = new ListObjectsRequest();
		request.setBucketName(bucketName);
		request.setDelimiter(delimiter);
		request.setPrefix(prefix);
		return request;
	}

	public void buildPrefixList(AmazonS3Client client, String bucketName, List<String> prefixes, String prefix, String delimiter, BaseCase baseCase) {
		log.info(prefix);
		prefixes.add(prefix);
		ListObjectsRequest request = getListObjectsRequest(bucketName, prefix, delimiter);
		ObjectListing listing = client.listObjects(request);
		List<String> commonPrefixes = listing.getCommonPrefixes();
		for (String commonPrefix : commonPrefixes) {
			if (!baseCase.isBaseCase(commonPrefix)) {
				buildPrefixList(client, bucketName, prefixes, commonPrefix, delimiter, baseCase);
			}
		}
	}

	@Test
	public void testGetStructure() {
		try {
			SimpleFormatter sf = new SimpleFormatter();
			String delimiter = "/";
			String bucket = "maven.kuali.org";
			AmazonS3Client client = getClient();
			KualiMavenBucketBaseCase baseCase = new KualiMavenBucketBaseCase();
			baseCase.setDelimiter(delimiter);
			baseCase.setToken("latest");

			long start = System.currentTimeMillis();
			List<String> prefixes = new ArrayList<String>();
			buildPrefixList(client, bucket, prefixes, null, delimiter, baseCase);
			long elapsed = System.currentTimeMillis() - start;
			log.info("Total Prefixes: " + prefixes.size());
			log.info("Total Time: " + sf.getTime(elapsed));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
