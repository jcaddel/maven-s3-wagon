package org.kuali.maven.wagon.util;

import com.amazonaws.services.s3.AmazonS3Client;

public class S3PrefixContext {
	AmazonS3Client client;
	String bucketName;
	BucketSummary summary;

	public AmazonS3Client getClient() {
		return client;
	}

	public void setClient(AmazonS3Client client) {
		this.client = client;
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public BucketSummary getSummary() {
		return summary;
	}

	public void setSummary(BucketSummary summary) {
		this.summary = summary;
	}

}
