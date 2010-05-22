package org.springframework.aws.maven;

/**
 * Maps a mime-type to a file extension
 */
public class Mapping {
	String type;
	String extension;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

}
