package org.kuali.maven.wagon.auth;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSSessionCredentials;
import com.google.common.base.Optional;

public final class ImmutableAwsCredentials implements AWSSessionCredentials {

	public ImmutableAwsCredentials(AWSSessionCredentials credentials) {
		this(credentials.getAWSAccessKeyId(), credentials.getAWSSecretKey(), Optional.fromNullable(credentials.getSessionToken()));
	}

	public ImmutableAwsCredentials(AWSCredentials credentials) {
		this(credentials.getAWSAccessKeyId(), credentials.getAWSSecretKey());
	}

	public ImmutableAwsCredentials(String accessKey, String secretKey) {
		this(accessKey, secretKey, Optional.<String> absent());
	}

	public ImmutableAwsCredentials(String accessKey, String secretKey, Optional<String> sessionToken) {
		Assert.noBlanks(accessKey, secretKey);
		Assert.noNulls(sessionToken);
		if (sessionToken.isPresent()) {
			Assert.noBlanks(sessionToken.get());
		}
		this.accessKey = accessKey;
		this.secretKey = secretKey;
		this.sessionToken = sessionToken;
	}

	private final String accessKey;
	private final String secretKey;
	private final Optional<String> sessionToken;

	public String getAWSAccessKeyId() {
		return accessKey;
	}

	public String getAWSSecretKey() {
		return secretKey;
	}

	public String getSessionToken() {
		if (sessionToken.isPresent()) {
			return sessionToken.get();
		} else {
			return null;
		}
	}

}
