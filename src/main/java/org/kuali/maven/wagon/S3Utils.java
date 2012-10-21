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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger log = LoggerFactory.getLogger(S3Utils.class);
	private static final int KILOBYTE = 1024;
	private static final int MEGABYTE = 1024 * KILOBYTE;
	private static final int MULTI_PART_UPLOAD_THRESHOLD = 100 * MEGABYTE;
	private static final String ROOT_NODE_PREFIX = null;
	SimpleFormatter formatter = new SimpleFormatter();

	private static S3Utils instance;

	public static synchronized S3Utils getInstance() {
		if (instance == null) {
			instance = new S3Utils();
		}
		return instance;
	}

	private S3Utils() {
		super();
	}

	/**
	 * Upload a single file to Amazon S3. If the file is larger than 100MB a multi-part upload is used. This splits the file into multiple
	 * smaller chunks with each chunk being uploaded in a different thread. Once all the threads have completed the file is reassembled on
	 * Amazon's side as a single file again.
	 */
	public void upload(File file, PutObjectRequest request, AmazonS3Client client, TransferManager manager) {
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
	public void blockingMultiPartUpload(PutObjectRequest request, TransferManager manager) {
		// Use multi-part upload for large files
		Upload upload = manager.upload(request);
		try {
			// Block and wait for the upload to finish
			upload.waitForCompletion();
		} catch (Exception e) {
			throw new AmazonS3Exception("Unexpected error uploading file", e);
		}
	}

	public ListObjectsRequest getListObjectsRequest(String bucketName, String prefix, String delimiter, Integer maxKeys) {
		ListObjectsRequest request = new ListObjectsRequest();
		request.setBucketName(bucketName);
		request.setDelimiter(delimiter);
		request.setPrefix(prefix);
		request.setMaxKeys(maxKeys);
		return request;
	}

	public ListObjectsRequest getListObjectsRequest(String bucketName, String prefix, String delimiter) {
		return getListObjectsRequest(bucketName, prefix, delimiter, null);
	}

	public ListObjectsRequest getListObjectsRequest(String bucketName, String prefix) {
		return getListObjectsRequest(bucketName, prefix, null);
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
			BucketSummary summary = new BucketSummary(prefix);
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(summary);
			if (prefix != null) {
				String parentKey = getParentPrefix(prefix, delimiter);
				DefaultMutableTreeNode parent = map.get(parentKey);
				parent.add(node);
			}
			map.put(prefix, node);
		}
		return map.get(ROOT_NODE_PREFIX);
	}

	public String getParentPrefix(String prefix, String delimiter) {
		String[] tokens = StringUtils.split(prefix, delimiter);
		if (tokens.length == 1) {
			return ROOT_NODE_PREFIX;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tokens.length - 1; i++) {
			sb.append(tokens[i] + delimiter);
		}
		return sb.toString();
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

	public void summarize(AmazonS3Client client, String bucketName, DefaultMutableTreeNode node) {
		List<DefaultMutableTreeNode> leaves = getLeaves(node);
		for (DefaultMutableTreeNode leaf : leaves) {
			BucketSummary summary = (BucketSummary) leaf.getUserObject();
			summarize(client, bucketName, summary);
		}
		fillInSummaries(node);
	}

	public void fillInSummaries(DefaultMutableTreeNode node) {
		BucketSummary summary = (BucketSummary) node.getUserObject();
		List<DefaultMutableTreeNode> children = getChildren(node);
		for (DefaultMutableTreeNode child : children) {
			fillInSummaries(child);
			BucketSummary childSummary = (BucketSummary) child.getUserObject();
			long count = childSummary.getCount();
			long size = childSummary.getSize();
			summary.setCount(summary.getCount() + count);
			summary.setSize(summary.getSize() + size);
		}
	}

	public List<DefaultMutableTreeNode> getChildren(DefaultMutableTreeNode node) {
		Enumeration<?> e = node.children();
		List<DefaultMutableTreeNode> children = new ArrayList<DefaultMutableTreeNode>();
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) e.nextElement();
			children.add(child);
		}
		return children;
	}

	public BucketSummary summarize(AmazonS3Client client, String bucketName, BucketSummary summary) {
		ListObjectsRequest request = getListObjectsRequest(bucketName, summary.getPrefix());
		ObjectListing current = client.listObjects(request);
		summarize(summary, current.getObjectSummaries());
		while (current.isTruncated()) {
			current = client.listNextBatchOfObjects(current);
			summarize(summary, current.getObjectSummaries());
		}
		return summary;
	}

	public void summarize(BucketSummary summary, List<S3ObjectSummary> summaries) {
		for (S3ObjectSummary element : summaries) {
			summary.setSize(summary.getSize() + element.getSize());
			summary.setCount(summary.getCount() + 1);
			if (log.isDebugEnabled()) {
				log.debug(summary.getCount() + " - " + element.getKey() + " - " + formatter.getSize(element.getSize()));
			}
		}
		if (log.isDebugEnabled()) {
			String prefix = summary.getPrefix();
			long count = summary.getCount();
			long bytes = summary.getSize();
			log.debug(rpad(prefix, 40) + " Total Count: " + lpad(count + "", 3) + " Total Size: " + lpad(formatter.getSize(bytes), 9));
		}
	}

	public String toString(DefaultMutableTreeNode node) {
		Enumeration<?> e = node.breadthFirstEnumeration();
		int maxPrefixLength = "prefix".length();
		int maxCountLength = "count".length();
		int maxSizeLength = "size".length();
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode element = (DefaultMutableTreeNode) e.nextElement();
			BucketSummary summary = (BucketSummary) element.getUserObject();
			if (summary.getPrefix() != null) {
				maxPrefixLength = Math.max(maxPrefixLength, summary.getPrefix().length());
			}
			maxCountLength = Math.max(maxCountLength, (summary.getCount() + "").length());
			maxSizeLength = Math.max(maxSizeLength, formatter.getSize(summary.getSize()).length());
		}
		e = node.breadthFirstEnumeration();
		StringBuilder sb = new StringBuilder();
		sb.append(rpad("prefix", maxPrefixLength) + " " + lpad("count", maxCountLength) + " " + lpad("size", maxSizeLength) + "\n");
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode element = (DefaultMutableTreeNode) e.nextElement();
			BucketSummary summary = (BucketSummary) element.getUserObject();
			String prefix = summary.getPrefix() == null ? "/" : summary.getPrefix();
			sb.append(rpad(prefix, maxPrefixLength));
			sb.append(" ");
			sb.append(lpad(summary.getCount() + "", maxCountLength));
			sb.append(" ");
			sb.append(lpad(formatter.getSize(summary.getSize()), maxSizeLength));
			sb.append("\n");
		}
		return sb.toString();
	}

	public String lpad(String s, int size) {
		return StringUtils.leftPad(s, size, " ");
	}

	public String rpad(String s, int size) {
		return StringUtils.rightPad(s, size, " ");
	}
}
