/**
 * Copyright 2010-2014 The Kuali Foundation
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

/**
 * Implementation of <code>AWSCredentials</code> that is immutable.
 */
public final class AwsCredentials implements AWSCredentials {

	public AwsCredentials(AWSCredentials credentials) {
		this(credentials.getAWSAccessKeyId(), credentials.getAWSSecretKey());
	}

	public AwsCredentials(String accessKey, String secretKey) {
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
