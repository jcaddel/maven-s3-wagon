package org.kuali.maven.wagon.auth;

import com.amazonaws.auth.AWSCredentials;

public final class ImmutableAwsCredentials implements AWSCredentials {

	public ImmutableAwsCredentials(AWSCredentials credentials) {
		this(credentials.getAWSAccessKeyId(), credentials.getAWSSecretKey());
	}

	public ImmutableAwsCredentials(String accessKey, String secretKey) {
		Assert.noBlanks(accessKey, secretKey);
		this.accessKey = accessKey;
		this.secretKey = secretKey;
	}

	private final String accessKey;
	private final String secretKey;

	public String getAWSAccessKeyId() {
		return accessKey;
	}

	public String getAWSSecretKey() {
		return secretKey;
	}

}
