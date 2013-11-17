/**
 * Copyright 2010-2013 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

	public ImmutableAwsCredentials(String accessKey, String secretKey, String sessionToken) {
		this(accessKey, secretKey, Optional.of(sessionToken));
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
