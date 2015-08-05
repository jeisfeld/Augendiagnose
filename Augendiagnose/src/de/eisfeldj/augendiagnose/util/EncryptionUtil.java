package de.eisfeldj.augendiagnose.util;

import java.lang.reflect.Field;
import java.security.Key;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

import android.annotation.SuppressLint;
import android.util.Base64;
import android.util.Log;
import de.eisfeldj.augendiagnose.Application;

/**
 * Utility for handling user keys.
 */
@SuppressWarnings("unchecked")
public final class EncryptionUtil {
	// JAVADOC:OFF
	// parameters for encryption
	private static Cipher cipherEncrypt;
	private static MessageDigest messageDigest;
	private static final String DUMMY_HASH = "dummy";
	private static final int HASH_LENGTH = 8;
	private static final String ALGORITHM = "DES";

	// JAVADOC:ON

	/**
	 * Hide default constructor.
	 */
	private EncryptionUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Special keys that get are set as valid.
	 */
	private static final List<String> SPECIAL_KEYS;

	/**
	 * The private key to be used for user key generation.
	 */
	private static final String KEY;

	/**
	 * Flag to store initialization status.
	 */
	private static boolean initialized = false;

	static {
		boolean foundPrivateConstants = false;

		List<String> specialKeys = new ArrayList<String>();
		String key = "";
		try {
			// Looking for a class PrivateConstants with fields SPECIAL_KEYS and KEY_STRING - not in repository
			Class<?> privateConstants = Class.forName("de.eisfeldj.augendiagnose.util.PrivateConstants");
			Field specialKeysField = privateConstants.getDeclaredField("SPECIAL_KEYS");
			specialKeys = (List<String>) specialKeysField.get(null);
			Field keyField = privateConstants.getDeclaredField("KEY_STRING");
			key = (String) keyField.get(null);
			foundPrivateConstants = true;
		}
		catch (Exception e) {
			Log.e(Application.TAG, "Did not find PrivateConstants", e);
		}
		SPECIAL_KEYS = specialKeys;
		KEY = key;

		if (foundPrivateConstants) {
			initializeCipher();
		}
	}

	/**
	 * Initialize the encryption cipher.
	 */
	@SuppressLint("TrulyRandom")
	private static void initializeCipher() {
		try {
			cipherEncrypt = Cipher.getInstance(ALGORITHM);
			Key symKey = new SecretKeySpec(Base64.decode(KEY, Base64.DEFAULT), ALGORITHM);
			cipherEncrypt.init(Cipher.ENCRYPT_MODE, symKey);

			messageDigest = MessageDigest.getInstance("MD5");
			initialized = true;
		}
		catch (Exception e) {
			Log.e(Application.TAG, "Failed to initialize EncryptionUtil", e);
		}
	}

	/**
	 * Utility method to test generation and validation of a user key.
	 *
	 * @param name
	 *            the user name for which key generation is to be tested.
	 */
	public static void test(final String name) {
		String key = createUserKey(name);
		Log.i(Application.TAG, "Key: " + key + ". Verified: " + validateUserKey(key));
	}

	/**
	 * Validate a user key.
	 *
	 * @param key
	 *            the user key to be validated.
	 * @return true if the user key is valid.
	 */
	public static boolean validateUserKey(final String key) {
		if (key == null || key.length() == 0 || !initialized) {
			return false;
		}
		if (SPECIAL_KEYS.contains(key)) {
			return true;
		}

		int index = key.lastIndexOf('-');
		if (index > 0) {
			String name = key.substring(0, index);
			String hash = key.substring(index + 1);
			return createCryptoHash(name).equals(hash);
		}
		else {
			return false;
		}
	}

	/**
	 * Generate a user key, which is a concatenation of user name and hash.
	 *
	 * @param input
	 *            the user key without hash.
	 * @return the user key including hash.
	 */
	public static String createUserKey(final String input) {
		return input + "-" + createCryptoHash(input);
	}

	/**
	 * Create a cryptographic hash from a String.
	 *
	 * @param input
	 *            the input for creating the hash. (Will be username.)
	 * @return the cryptocraphic hash.
	 */
	private static String createCryptoHash(final String input) {
		try {
			return convertBase64(createHash(encrypt(input))).substring(0, HASH_LENGTH);
		}
		catch (Exception e) {
			return DUMMY_HASH;
		}
	}

	/**
	 * Create a hash value from an input.
	 *
	 * @param input
	 *            the input for the hash creation.
	 * @return the hash.
	 */
	private static byte[] createHash(final byte[] input) {
		return messageDigest.digest(input);
	}

	/**
	 * Do base 64 encoding of a message.
	 *
	 * @param bytes
	 *            the bytes to be encoded.
	 * @return the base 64 encoded String.
	 */
	private static String convertBase64(final byte[] bytes) {
		return Base64.encodeToString(bytes, Base64.DEFAULT);
	}

	/**
	 * Encrypt a String using DES.
	 *
	 * @param input
	 *            the String to be encrypted.
	 * @return the encrypted String.
	 * @throws BadPaddingException
	 *             if this cipher is in decryption mode, and (un)padding has been requested, but the decrypted data is
	 *             not bounded by the appropriate padding bytes
	 * @throws IllegalBlockSizeException
	 *             if this cipher is a block cipher, no padding has been requested (only in encryption mode), and the
	 *             total input length of the data processed by this cipher is not a multiple of block size; or if this
	 *             encryption algorithm is unable to process the input data provided.
	 */
	private static byte[] encrypt(final String input) throws BadPaddingException, IllegalBlockSizeException {
		return cipherEncrypt.doFinal(input.getBytes());
	}

}
