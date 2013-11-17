package org.kuali.maven.wagon.auth;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.wagon.authentication.AuthenticationInfo;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.google.common.base.Optional;

/**
 * This chain searches for AWS credentials in system properties -> environment variables -> ~/.m2/settings.xml -> Amazon's EC2 Instance Metadata Service
 */
public final class MavenAwsCredentialsProviderChain extends AWSCredentialsProviderChain {

	public MavenAwsCredentialsProviderChain(Optional<AuthenticationInfo> auth) {
		super(getProviders(auth));
	}

	private static AWSCredentialsProvider[] getProviders(Optional<AuthenticationInfo> auth) {
		List<AWSCredentialsProvider> providers = new ArrayList<AWSCredentialsProvider>();

		// System properties always win
		providers.add(new SystemPropertiesCredentialsProvider());

		// Then fall through to environment variables
		providers.add(new EnvironmentVariableCredentialsProvider());

		// Then fall through to settings.xml
		providers.add(new AuthenticationInfoCredentialsProvider(auth));

		// Then fall through to Amazon's EC2 Instance Metadata Service
		// http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/java-dg-roles.html
		// This allows you setup an IAM role, attach that role to an EC2 Instance at launch time,
		// and thus automatically provide the wagon with the credentials it needs
		providers.add(new InstanceProfileCredentialsProvider());

		return providers.toArray(new AWSCredentialsProvider[providers.size()]);
	}

}
