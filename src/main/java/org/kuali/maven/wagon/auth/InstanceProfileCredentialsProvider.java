package org.kuali.maven.wagon.auth;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.internal.EC2MetadataClient;
import com.amazonaws.util.DateUtils;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

/**
 * Credentials provider implementation that loads credentials from the Amazon EC2 Instance Metadata Service.
 */
public class InstanceProfileCredentialsProvider implements AWSCredentialsProvider {

	protected volatile AWSCredentials credentials;
	protected volatile Date credentialsExpiration;

	public AWSCredentials getCredentials() {
		if (needsToLoadCredentials())
			loadCredentials();
		if (expired()) {
			throw new AmazonClientException("The credentials received from the Amazon EC2 metadata service have expired");
		}

		return credentials;
	}

	public void refresh() {
		credentials = null;
	}

	protected boolean needsToLoadCredentials() {
		if (credentials == null)
			return true;

		if (credentialsExpiration != null) {
			int thresholdInMilliseconds = 1000 * 60 * 5;
			boolean withinExpirationThreshold = credentialsExpiration.getTime() - System.currentTimeMillis() < thresholdInMilliseconds;
			if (withinExpirationThreshold)
				return true;
		}

		return false;
	}

	private boolean expired() {
		if (credentialsExpiration != null) {
			if (credentialsExpiration.getTime() < System.currentTimeMillis()) {
				return true;
			}
		}

		return false;
	}

	private synchronized void loadCredentials() {

		if (needsToLoadCredentials()) {
			try {
				String credentialsResponse = new EC2MetadataClient().getDefaultCredentials();
				JSONObject jsonObject = new JSONObject(credentialsResponse);

				String accessKey = jsonObject.getString("AccessKeyId");
				String secretKey = jsonObject.getString("SecretAccessKey");
				System.out.println("accessKey: " + accessKey + " secretKey:" + secretKey);
				if (jsonObject.has("Token")) {
					credentials = new BasicSessionCredentials(accessKey, secretKey, jsonObject.getString("Token"));
				} else {
					credentials = new BasicAWSCredentials(accessKey, secretKey);
				}

				if (jsonObject.has("Expiration")) {
					/*
					 * TODO: The expiration string comes in a different format than what we deal with in other parts of the SDK, so we have to convert it to the ISO8601 syntax we
					 * expect.
					 */
					String expiration = jsonObject.getString("Expiration");
					expiration = expiration.replaceAll("\\+0000$", "Z");

					credentialsExpiration = new DateUtils().parseIso8601Date(expiration);
				}
			} catch (IOException e) {
				throw new AmazonClientException("Unable to load credentials from Amazon EC2 metadata service", e);
			} catch (JSONException e) {
				throw new AmazonClientException("Unable to parse credentials from Amazon EC2 metadata service", e);
			} catch (ParseException e) {
				throw new AmazonClientException("Unable to parse credentials expiration date from Amazon EC2 metadata service", e);
			}
		}

	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
