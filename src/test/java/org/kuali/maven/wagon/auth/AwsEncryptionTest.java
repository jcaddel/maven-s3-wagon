/**
 * Copyright 2010-2015 The Kuali Foundation
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

import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.util.Arrays;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.junit.Test;
import org.kuali.maven.wagon.S3Wagon;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.model.EncryptionMaterials;

public class AwsEncryptionTest {

	// 16 bytes minimum for AES.
	static final String CIPHER_PASS = "0123456789abcdef";

	/** Verify if key+cipher algorithm is preset/allowed by JCE policy. */
	static boolean isAlgoPresent() {
		try {
			String keyAlgo = AwsEncryption.DEFAULT_ALGO;
			char[] keyPass = CIPHER_PASS.toCharArray();
			byte[] keySalt = new byte[8];
			int keyIter = 1000;
			int keySize = 128;
			SecretKey secretKey = AwsEncryption.generateSecretKey(keyAlgo, keyPass, keySalt, keyIter, keySize);
			Cipher cipher = Cipher.getInstance(AwsEncryption.CIPHER_ALGO);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			cipher.doFinal();
			return true;
		} catch (Exception e) {
			System.err.println("Algorithm not available, skipping tests.");
			e.printStackTrace();
			return false;
		}
	}

	/** Emulate authentication stored in settings.xml */
	static AuthenticationInfo makeAuth(String username, String password, String secretkey, String passphrase) {
		AuthenticationInfo auth = new AuthenticationInfo();
		auth.setUserName(username);
		auth.setPassword(password);
		auth.setPrivateKey(secretkey);
		auth.setPassphrase(passphrase);
		return auth;
	}

	/** Wagon method access wrapper class. */
	static class WagonWrap extends S3Wagon {
		protected AmazonS3Client getAmazonS3Client(AWSCredentials credentials, EncryptionMaterials materials) {
			return super.getAmazonS3Client(credentials, materials);
		}
	}

	@Test
	public void testHexParse() throws Exception {
		{
			byte[] source = AwsEncryption.hexStringToByteArray(null);
			byte[] target = {};
			assertTrue(Arrays.equals(source, target));
		}
		{
			String text = AwsEncryption.DEFAULT_SALT.toUpperCase();
			byte[] source = AwsEncryption.hexStringToByteArray(text);
			byte[] target = { //
					(byte) 0xA4, (byte) 0x0B, (byte) 0xC8, (byte) 0x34, //
					(byte) 0xD6, (byte) 0x95, (byte) 0xF3, (byte) 0x13 //
			};
			assertTrue(Arrays.equals(source, target));
		}
		{
			String text = AwsEncryption.DEFAULT_SALT.toLowerCase();
			byte[] source = AwsEncryption.hexStringToByteArray(text);
			byte[] target = { //
					(byte) 0xA4, (byte) 0x0B, (byte) 0xC8, (byte) 0x34, //
					(byte) 0xD6, (byte) 0x95, (byte) 0xF3, (byte) 0x13 //
			};
			assertTrue(Arrays.equals(source, target));
		}
	}

	@Test
	public void testExtractProperties() throws Exception {
		{
			Map<String, String> termMap = AwsEncryption.extractProperties(null);
			assertTrue(termMap.size() == 0);
		}
		{
			Map<String, String> termMap = AwsEncryption.extractProperties("");
			assertTrue(termMap.size() == 0);
		}
		{
			Map<String, String> termMap = AwsEncryption.extractProperties("key=value");
			assertTrue(termMap.size() == 1);
			assertTrue(termMap.get("key").equals("value"));
		}
		{
			Map<String, String> termMap = AwsEncryption.extractProperties("key1=value1;key2=value2");
			assertTrue(termMap.size() == 2);
			assertTrue(termMap.get("key1").equals("value1"));
			assertTrue(termMap.get("key2").equals("value2"));
		}
		{
			Map<String, String> termMap = AwsEncryption
					.extractProperties("; key1 = \t value1 ; key2 \t = value2 ; \t ;");
			assertTrue(termMap.size() == 2);
			assertTrue(termMap.get("key1").equals("value1"));
			assertTrue(termMap.get("key2").equals("value2"));
		}
	}

	@Test
	public void testMissingCryptoAuth() throws Exception {
		assertNull(AwsEncryption.getMaterials(null));
		assertNull(AwsEncryption.getMaterials(makeAuth("username", "password", null, null)));
		assertNull(AwsEncryption.getMaterials(makeAuth("username", "password", "secretkey", null)));
		assertNull(AwsEncryption.getMaterials(makeAuth("username", "password", null, "passphrase")));
	}

	@Test
	public void testDefaultCryptoParams() throws Exception {
		assumeTrue(isAlgoPresent());

		EncryptionMaterials materials = AwsEncryption
				.getMaterials(makeAuth("username", "password", "default", CIPHER_PASS));
		SecretKey key = materials.getSymmetricKey();
		assertEquals(key.getAlgorithm(), AwsEncryption.CIPHER_ALGO);
	}

	@Test
	public void testAmazonS3ClientFactory() throws Exception {
		assumeTrue(isAlgoPresent());

		WagonWrap wagon = new WagonWrap();

		AWSCredentials credentials = new AwsCredentials("username", "password");

		{
			AuthenticationInfo auth = makeAuth("username", "password", null, null);
			EncryptionMaterials materials = AwsEncryption.getMaterials(auth);
			AmazonS3Client client = wagon.getAmazonS3Client(credentials, materials);
			assertTrue(client instanceof AmazonS3Client);
		}

		{
			AuthenticationInfo auth = makeAuth("username", "password", "default", "secret");
			EncryptionMaterials materials = AwsEncryption.getMaterials(auth);
			AmazonS3Client client = wagon.getAmazonS3Client(credentials, materials);
			assertTrue(client instanceof AmazonS3EncryptionClient);
		}

	}

}
