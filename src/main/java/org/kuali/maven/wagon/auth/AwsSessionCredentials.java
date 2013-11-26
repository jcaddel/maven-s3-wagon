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

import com.amazonaws.auth.AWSSessionCredentials;

/**
 * Implementation of <code>AWSSessionCredentials</code> that is immutable.
 */
public final class AwsSessionCredentials implements AWSSessionCredentials {

	public AwsSessionCredentials(AWSSessionCredentials credentials) {
		this(credentials.getAWSAccessKeyId(), credentials.getAWSSecretKey(), credentials.getSessionToken());
	}

	public AwsSessionCredentials(String accessKey, String secretKey, String sessionToken) {
		Assert.noBlanks(accessKey, secretKey, sessionToken);
		this.accessKey = accessKey;
		this.secretKey = secretKey;
		this.sessionToken = sessionToken;
	}

	private final String accessKey;
	private final String secretKey;
	private final String sessionToken;

	public String getAWSAccessKeyId() {
		return accessKey;
	}

	public String getAWSSecretKey() {
		return secretKey;
	}

	public String getSessionToken() {
		return sessionToken;
	}

}
