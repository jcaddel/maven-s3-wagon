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

import javax.swing.tree.DefaultMutableTreeNode;

import org.junit.Test;
import org.kuali.maven.wagon.util.BucketSizeComparator;
import org.kuali.maven.wagon.util.BucketSummary;
import org.kuali.maven.wagon.util.S3Utils;
import org.kuali.maven.wagon.util.SimpleFormatter;
import org.kuali.maven.wagon.util.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;

public class S3UtilsTest {
	private static final String ACCESSKEY = "AKIAJFD5IM7IPVVUEBNA";
	private static final String SECRETYKEY = System.getProperty("secret.key");

	private static final Logger log = LoggerFactory.getLogger(S3UtilsTest.class);
	S3Utils utils = S3Utils.getInstance();

	protected AWSCredentials getCredentials() {
		log.debug("access key: " + ACCESSKEY);
		return new BasicAWSCredentials(ACCESSKEY, SECRETYKEY);
	}

	protected AmazonS3Client getClient() {
		AWSCredentials credentials = getCredentials();
		return new AmazonS3Client(credentials);
	}

	@Test
	public void testGetStructure() {
		try {
			Size[] values = Size.values();
			for (Size value : values) {
				log.info(value.getSizeLabel() + " " + value.getValue());
			}
			long now = System.currentTimeMillis();
			long bytes = Long.MAX_VALUE;
			SimpleFormatter sf = new SimpleFormatter();
			log.info(sf.getSize(bytes));
			log.info(sf.getRate(now, bytes));
			log.info(sf.getTime(now));
			log.info(sf.getTime(bytes));
			String delimiter = "/";
			String bucket = "maven.kuali.org";
			AmazonS3Client client = getClient();

			KualiMavenBucketBaseCase baseCase1 = new KualiMavenBucketBaseCase();
			baseCase1.setDelimiter(delimiter);
			baseCase1.setToken("latest");

			JavaxServletOnlyBaseCase baseCase2 = new JavaxServletOnlyBaseCase();
			baseCase2.setDelimiter(delimiter);
			baseCase2.setToken("latest");

			ExternalOnlyBaseCase baseCase3 = new ExternalOnlyBaseCase();
			baseCase3.setDelimiter(delimiter);
			baseCase3.setToken("latest");

			long start1 = System.currentTimeMillis();
			List<String> prefixes = new ArrayList<String>();
			utils.buildPrefixList(client, bucket, prefixes, null, delimiter, baseCase2);
			long elapsed1 = System.currentTimeMillis() - start1;
			DefaultMutableTreeNode node = utils.buildTree(prefixes, delimiter);
			log.info("Total Prefixes: " + prefixes.size());
			log.info("Total Time: " + sf.getTime(elapsed1));
			List<DefaultMutableTreeNode> leaves = utils.getLeaves(node);
			log.info("Total Leaves: " + leaves.size());
			long start2 = System.currentTimeMillis();
			utils.summarize(client, bucket, node);
			long elapsed2 = System.currentTimeMillis() - start2;
			BucketSummary summary = (BucketSummary) node.getUserObject();
			log.info("Total Time: " + sf.getTime(elapsed2));
			log.info("Count: " + summary.getCount());
			log.info("Size: " + sf.getSize(summary.getSize()));

			log.info("S3 Bucket Summary\n" + utils.toString(node, Size.MB, new BucketSizeComparator()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
