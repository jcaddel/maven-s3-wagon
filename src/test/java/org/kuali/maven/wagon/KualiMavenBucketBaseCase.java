package org.kuali.maven.wagon;

import org.apache.commons.lang.StringUtils;

public class KualiMavenBucketBaseCase implements BaseCase {

	String delimiter;
	String token;

	public boolean isBaseCase(String prefix) {
		boolean baseCase1 = endsWithVersionNumber(prefix, delimiter);
		boolean baseCase2 = endsWithToken(prefix, delimiter, token);
		return baseCase1 || baseCase2;
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
