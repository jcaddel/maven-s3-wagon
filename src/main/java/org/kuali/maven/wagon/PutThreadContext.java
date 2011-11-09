package org.kuali.maven.wagon;

import java.util.List;

import com.amazonaws.services.s3.AmazonS3Client;

public class PutThreadContext {
	ThreadHandler handler;
	List<PutContext> contexts;
	AmazonS3Client client;
	RequestFactory factory;
	int length;
	int offset;

	public ThreadHandler getHandler() {
		return handler;
	}

	public void setHandler(ThreadHandler handler) {
		this.handler = handler;
	}

	public List<PutContext> getContexts() {
		return contexts;
	}

	public void setContexts(List<PutContext> contexts) {
		this.contexts = contexts;
	}

	public AmazonS3Client getClient() {
		return client;
	}

	public void setClient(AmazonS3Client client) {
		this.client = client;
	}

	public RequestFactory getFactory() {
		return factory;
	}

	public void setFactory(RequestFactory factory) {
		this.factory = factory;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
}
