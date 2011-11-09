package org.kuali.maven.wagon;

import java.util.List;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;

/**
 * Thread implementation for uploading a list of files to S3
 */
public class PutThread implements Runnable {

	PutThreadContext context;

	public PutThread() {
		this(null);
	}

	public PutThread(PutThreadContext context) {
		super();
		this.context = context;
	}

	public void run() {
		int offset = context.getOffset();
		int length = context.getLength();
		List<PutContext> list = context.getContexts();
		RequestFactory factory = context.getFactory();
		AmazonS3Client client = context.getClient();
		for (int i = offset; i < offset + length; i++) {
			if (i >= list.size()) {
				break;
			}
			if (context.getHandler().isStopThreads()) {
				break;
			}
			PutContext pc = list.get(i);
			PutObjectRequest request = factory.getPutObjectRequest(pc);
			pc.fireStart();
			client.putObject(request);
			pc.fireComplete();
		}
	}

	public PutThreadContext getContext() {
		return context;
	}

	public void setContext(PutThreadContext context) {
		this.context = context;
	}
}
