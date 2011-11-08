package org.kuali.maven.wagon;

import java.io.File;

public class PutRequest {
	public PutRequest() {
		this(null, null);
	}

	public PutRequest(File sourceDirectory, String destinationDirectory) {
		super();
		this.sourceDirectory = sourceDirectory;
		this.destinationDirectory = destinationDirectory;
	}

	File sourceDirectory;
	String destinationDirectory;

	public File getSourceDirectory() {
		return sourceDirectory;
	}

	public void setSourceDirectory(File sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	public String getDestinationDirectory() {
		return destinationDirectory;
	}

	public void setDestinationDirectory(String destinationDirectory) {
		this.destinationDirectory = destinationDirectory;
	}

}
