package org.kuali.maven.wagon.auth;

import org.apache.commons.lang.StringUtils;

import com.amazonaws.auth.AWSCredentials;

public final class ImmutableAwsCredentials implements AWSCredentials {

	public ImmutableAwsCredentials(String accessKey, String secretKey) {
		assertNoBlanks(accessKey, secretKey);
		this.accessKey = accessKey;
		this.secretKey = secretKey;
	}

	private void assertNoBlanks(String... strings) {
		if (strings == null) {
			throw new IllegalArgumentException("null not allowed");
		}
		for (String string : strings) {
			if (StringUtils.isBlank(string)) {
				throw new IllegalArgumentException("blanks not allowed");
			}
		}
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
