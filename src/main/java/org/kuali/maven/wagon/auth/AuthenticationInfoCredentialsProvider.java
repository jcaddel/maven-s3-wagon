package org.kuali.maven.wagon.auth;

import org.apache.maven.wagon.authentication.AuthenticationInfo;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.google.common.base.Optional;

public final class AuthenticationInfoCredentialsProvider implements AWSCredentialsProvider {

	public AuthenticationInfoCredentialsProvider(Optional<AuthenticationInfo> auth) {
		Assert.notNull(auth);
		this.auth = auth;
	}

	private final Optional<AuthenticationInfo> auth;

	public AWSCredentials getCredentials() {
		if (!auth.isPresent()) {
			throw new IllegalStateException(getAuthenticationErrorMessage());
		}
		String accessKey = auth.get().getUserName();
		String secretKey = auth.get().getPassword();
		Assert.noBlanksWithMsg(getAuthenticationErrorMessage(), accessKey, secretKey);
		return new ImmutableAwsCredentials(accessKey, secretKey);
	}

	public void refresh() {
		// no-op
	}

	protected String getAuthenticationErrorMessage() {
		StringBuffer sb = new StringBuffer();
		sb.append("The S3 wagon needs AWS Access Key set as the username and AWS Secret Key set as the password. eg:\n");
		sb.append("<server>\n");
		sb.append("  <id>my.server</id>\n");
		sb.append("  <username>[AWS Access Key ID]</username>\n");
		sb.append("  <password>[AWS Secret Access Key]</password>\n");
		sb.append("</server>\n");
		return sb.toString();
	}

}
