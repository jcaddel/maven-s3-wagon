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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.codehaus.plexus.util.StringUtils;
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
	private static final String ROOTNODE = "ROOTNODE";

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
			KualiMavenBucketBaseCase baseCase1 = new KualiMavenBucketBaseCase();
			baseCase1.setDelimiter(delimiter);
			baseCase1.setToken("latest");
			JavaxServletOnlyBaseCase baseCase2 = new JavaxServletOnlyBaseCase();
			baseCase2.setDelimiter(delimiter);
			baseCase2.setToken("latest");

			long start = System.currentTimeMillis();
			List<String> prefixes = new ArrayList<String>();
			buildPrefixList(client, bucket, prefixes, null, delimiter, baseCase2);
			long elapsed = System.currentTimeMillis() - start;
			DefaultMutableTreeNode node = buildTree(prefixes, delimiter);
			List<DefaultMutableTreeNode> leaves = getLeaves(node);
			log.info("Total Prefixes: " + prefixes.size());
			log.info("Total Time: " + sf.getTime(elapsed));
			log.info("Leaves: " + leaves.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<DefaultMutableTreeNode> getLeaves(DefaultMutableTreeNode node) {
		Enumeration<?> e = node.breadthFirstEnumeration();
		List<DefaultMutableTreeNode> leaves = new ArrayList<DefaultMutableTreeNode>();
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode element = (DefaultMutableTreeNode) e.nextElement();
			if (element.isLeaf()) {
				leaves.add(element);
			}
		}
		return leaves;

	}

	public DefaultMutableTreeNode buildTree(List<String> prefixes, String delimiter) {
		Map<String, DefaultMutableTreeNode> map = new HashMap<String, DefaultMutableTreeNode>();
		for (String prefix : prefixes) {
			if (prefix == null) {
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(ROOTNODE);
				map.put(ROOTNODE, node);
			} else {
				String[] tokens = StringUtils.split(prefix, delimiter);
				String key = tokens[tokens.length - 1];
				DefaultMutableTreeNode child = new DefaultMutableTreeNode(key);
				String parentKey = (tokens.length == 1) ? ROOTNODE : tokens[tokens.length - 2];
				DefaultMutableTreeNode parent = map.get(parentKey);
				parent.add(child);
				map.put(key, child);
			}
		}
		return map.get(ROOTNODE);
	}

}
