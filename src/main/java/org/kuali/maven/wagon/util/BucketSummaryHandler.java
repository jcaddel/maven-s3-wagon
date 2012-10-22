package org.kuali.maven.wagon.util;

import org.kuali.common.threads.ElementHandler;
import org.kuali.common.threads.ListIteratorContext;

import com.amazonaws.services.s3.AmazonS3Client;

public class BucketSummaryHandler implements ElementHandler<S3PrefixContext> {

	public void handleElement(ListIteratorContext<S3PrefixContext> context, int index, S3PrefixContext element) {
		AmazonS3Client client = element.getClient();
		String bucketName = element.getBucketName();
		BucketSummary summary = element.getSummary();
		S3Utils.getInstance().summarize(client, bucketName, summary);
	}

}
