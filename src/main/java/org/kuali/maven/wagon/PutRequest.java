package org.kuali.maven.wagon;

import java.io.File;

public class PutRequest {
	public PutRequest() {
		this(null, null);
	}

	public PutRequest(File sourceDirectory, String destinationDirectory) {
		super();
		this.source = sourceDirectory;
		this.destination = destinationDirectory;
	}

	File source;
	String destination;

	public File getSource() {
		return source;
	}

	public void setSource(File sourceDirectory) {
		this.source = sourceDirectory;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destinationDirectory) {
		this.destination = destinationDirectory;
	}

}
