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

import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.maven.wagon.authentication.AuthenticationInfo;

import com.amazonaws.services.s3.model.EncryptionMaterials;

import com.amazonaws.services.s3.internal.crypto.JceEncryptionConstants;
import com.amazonaws.services.s3.internal.crypto.EncryptionUtils;

/**
 * AWS S3 client side encryption support.
 * 
 * See <a href=
 * "http://docs.aws.amazon.com/AmazonS3/latest/dev/encrypt-client-side-symmetric-master-key.html">
 * encrypt-client-side-symmetric-master-key</a>
 * 
 * Master key derivation can use any AES compatible SecretKeyFactory algorithms
 * with any AES compatible key sizes.
 * 
 * See <a href=
 * "https://docs.oracle.com/javase/8/docs/technotes/guides/security/SunProviders.html#SunJCEProvider">
 * SunJCEProvider</a>
 * 
 */
public class AwsEncryption {

	// Master key scheme properties.

	/** Key scheme version. */
	static final String KEY_VERS = "vers";
	/** Key generation algorithm. */
	static final String KEY_ALGO = "algo";
	/** Key generation iteration count. */
	static final String KEY_ITER = "iter";
	/** Desired master key size, bits. */
	static final String KEY_SIZE = "size";
	/** Key generation mix-in salt hex value. */
	static final String KEY_SALT = "salt";

	// Supported master key scheme versions.

	/** PBE key derivation scheme. */
	static final String VERSION_0 = "0";

	// Default master key scheme parameters.

	static final String DEFAULT_VERS = VERSION_0;
	static final String DEFAULT_ALGO = "PBKDF2WithHmacSHA1"; // Java 7.
	static final String DEFAULT_ITER = "5000";
	static final String DEFAULT_SIZE = "128"; // Weak export policy.
	static final String DEFAULT_SALT = "A4 0B C8 34 D6 95 F3 13";

	/**
	 * {@link EncryptionUtils} uses only AES with master key.
	 */
	static final String CIPHER_ALGO = JceEncryptionConstants.SYMMETRIC_KEY_ALGORITHM;

	// Property separators.

	static final String REX_WS = "\\s*";
	static final String TERM_SEP = ";";
	static final String TERM_REX = REX_WS + TERM_SEP + REX_WS;
	static final String ENTRY_SEP = "=";
	static final String ENTRY_REX = REX_WS + ENTRY_SEP + REX_WS;

	/**
	 * Extract key/value properties from single text line:
	 * 
	 * "key1=value1; key2 = value2 ; "
	 */
	public static Map<String, String> extractProperties(String textLine) {
		Map<String, String> termMap = new HashMap<String, String>();
		if (textLine == null)
			return termMap;
		String[] termList = textLine.trim().split(TERM_REX);
		for (String term : termList) {
			if (term.contains(ENTRY_SEP)) {
				String[] entry = term.trim().split(ENTRY_REX);
				String key = entry[0];
				String value = entry[1];
				termMap.put(key, value);
			}
		}
		return termMap;
	}

	/**
	 * Amazon S3 client side encryption materials provider.
	 * 
	 * S3 encryption materials derivation context is stored in the optional
	 * privateKey+passphrase pair in the ~/.m2/settings.xml.
	 * 
	 * Parameter "privateKey" describes master key derivation scheme.
	 * 
	 * Parameter "passphrase" provides actual master key source text password.
	 * 
	 * Both "privateKey" and "passphrase" must be present to activate encryption
	 * configuration for the wagon.
	 * 
	 * User can use privateKey="default" stanza to rely on the default key
	 * derivation properties provided by the wagon.
	 * 
	 * User can verify if encryption is active by examining stored artifact
	 * meta-data in AWS web console: "x-amz-meta-x-amz-key", ... encryption
	 * properties must be present after successful deployment.
	 * 
	 * Both encrypted and non-encrypted objects can be stored in the same S3
	 * bucket / repository.
	 * 
	 * Example default encryption parameters entry in settings.xml:
	 * 
	 * <pre>
	 * {@literal
	 * <server>
	 *    <id>my.server</id>
	 *    <username>[AWS Access Key ID]</username>
	 *    <password>[AWS Secret Access Key]</password>
	 *    <privateKey>default</privateKey>
	 *    <passphrase>really-secret</passphrase>
	 * </server>
	 * }
	 * </pre>
	 * 
	 * Example custom encryption parameters entry in settings.xml:
	 * 
	 * <pre>
	 * {@literal
	 * <server>
	 *    <id>my.server</id>
	 *    <username>[AWS Access Key ID]</username>
	 *    <password>[AWS Secret Access Key]</password>
	 *    <privateKey>vers=0;algo=PBKDF2WithHmacSHA1;iter=5000;size=128;salt=A40BC834D695F313</privateKey>
	 *    <passphrase>really-secret</passphrase>
	 * </server>
	 * }
	 * </pre>
	 */
	public static EncryptionMaterials getMaterials(AuthenticationInfo auth) throws GeneralSecurityException {

		if (auth == null) {
			return null;
		}

		if (auth.getPrivateKey() == null) {
			return null;
		}

		if (auth.getPassphrase() == null) {
			return null;
		}

		Map<String, String> termMap = extractProperties(auth.getPrivateKey());

		String vers = termMap.get(KEY_VERS);
		if (vers == null) {
			vers = DEFAULT_VERS;
		}

		String algo = termMap.get(KEY_ALGO);
		if (algo == null) {
			algo = DEFAULT_ALGO;
		}

		String iter = termMap.get(KEY_ITER);
		if (iter == null) {
			iter = DEFAULT_ITER;
		}

		String size = termMap.get(KEY_SIZE);
		if (size == null) {
			size = DEFAULT_SIZE;
		}

		String salt = termMap.get(KEY_SALT);
		if (salt == null) {
			salt = DEFAULT_SALT;
		}

		if (vers.equals(VERSION_0)) {
			char[] keyPass = auth.getPassphrase().toCharArray();
			byte[] keySalt = hexStringToByteArray(salt);
			int keySize = Integer.parseInt(size);
			int keyIter = Integer.parseInt(iter);
			SecretKey secretKey = generateSecretKey(algo, keyPass, keySalt, keyIter, keySize);
			return new EncryptionMaterials(secretKey);
		}

		throw new IllegalArgumentException("Unsupported crypto key params: " + auth.getPrivateKey());

	}

	/**
	 * Parse hex string to byte array, ignore white space.
	 */
	public static byte[] hexStringToByteArray(String text) {
		if (text == null) {
			return new byte[0];
		}
		text = text.replaceAll(REX_WS, "");
		int size = text.length();
		byte[] data = new byte[size / 2];
		for (int i = 0; i < size; i += 2) {
			data[i / 2] = (byte) ((Character.digit(text.charAt(i), 16) << 4) + Character.digit(text.charAt(i + 1), 16));
		}
		return data;
	}

	/**
	 * Generate secret key for PBE/AES.
	 */
	public static SecretKey generateSecretKey(String keyAlgo, char[] keyPass, byte[] keySalt, int keyIter, int keySize)
			throws GeneralSecurityException {
		SecretKeyFactory factory = SecretKeyFactory.getInstance(keyAlgo);
		KeySpec keySpec = new PBEKeySpec(keyPass, keySalt, keyIter, keySize);
		SecretKey secretKey = factory.generateSecret(keySpec);
		return new SecretKeySpec(secretKey.getEncoded(), CIPHER_ALGO);
	}

}
