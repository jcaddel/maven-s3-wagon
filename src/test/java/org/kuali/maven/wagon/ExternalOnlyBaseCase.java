/**
 * Copyright 2010-2012 The Kuali Foundation
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
package org.kuali.maven.wagon;

import org.apache.commons.lang.StringUtils;
import org.kuali.common.aws.s3.BaseCase;

public class ExternalOnlyBaseCase implements BaseCase {

	String delimiter;
	String token;

	public boolean isBaseCase(String prefix) {
		boolean test1 = endsWithVersionNumber(prefix, delimiter);
		boolean test2 = endsWithToken(prefix, delimiter, token);
		boolean test3 = !prefix.startsWith("external/");
		return test1 || test2 || test3;
	}

	public boolean endsWithVersionNumber(String prefix, String delimiter) {
		String[] tokens = prefix.split(delimiter);
		String lastToken = tokens[tokens.length - 1];
		String firstChar = lastToken.substring(0, 1);
		return StringUtils.isNumeric(firstChar);
	}

	public boolean endsWithToken(String prefix, String delimiter, String token) {
		return prefix.endsWith(delimiter + token + delimiter);
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
