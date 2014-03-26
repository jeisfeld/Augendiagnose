package de.eisfeldj.augendiagnose.util;

import java.lang.reflect.Field;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.util.Log;
import de.eisfeldj.augendiagnose.Application;

@SuppressWarnings("unchecked")
public class EncryptionUtil {

	private static Cipher cipherEncrypt;
	private static MessageDigest messageDigest;
	private static final String DUMMY_HASH = "dummy";
	private static final int HASH_LENGTH = 8;

	private static List<String> SPECIAL_KEYS = new ArrayList<String>();
	private static String KEY = "";
	private static boolean initialized = false;

	static {
		boolean foundPrivateConstants = false;

		try {
			// Looking for a class PrivateConstants with fields SPECIAL_KEYS and KEY_STRING - not in repository
			Class<?> privateConstants = Class.forName("de.eisfeldj.augendiagnose.util.PrivateConstants");
			Field specialKeys = privateConstants.getDeclaredField("SPECIAL_KEYS");
			SPECIAL_KEYS = (List<String>) specialKeys.get(null);
			Field key = privateConstants.getDeclaredField("KEY_STRING");
			KEY = (String) key.get(null);
			foundPrivateConstants = true;
		}
		catch (Exception e) {
			Log.e(Application.TAG, "Did not find PrivateConstants", e);
		}

		if (foundPrivateConstants) {
			try {
				String ALGORITHM = "DES";
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
	}

	/**
	 * Utility method to test generation and validation of a user key
	 * 
	 * @param name
	 */
	public static void test(String name) {
		String key = createUserKey(name);
		Log.i(Application.TAG, "Key: " + key + ". Verified: " + validateUserKey(key));
	}

	/**
	 * Validate a user key
	 * 
	 * @param key
	 * @return
	 */
	public static boolean validateUserKey(String key) {
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
	 * @return
	 */
	public static String createUserKey(String input) {
		return input + "-" + createCryptoHash(input);
	}

	/**
	 * Create a cryptographic hash from a username
	 * 
	 * @param input
	 * @return
	 */
	private static String createCryptoHash(String input) {
		try {
			return convertBase64(createHash(encrypt(input))).substring(0, HASH_LENGTH);
		}
		catch (Exception e) {
			return DUMMY_HASH;
		}
	}

	/**
	 * Create a hash value from an input
	 * 
	 * @param input
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	private static byte[] createHash(byte[] input) throws NoSuchAlgorithmException {
		return messageDigest.digest(input);
	}

	/**
	 * Do base 64 encoding of a message
	 * 
	 * @param bytes
	 * @return
	 */
	private static String convertBase64(byte[] bytes) {
		return Base64.encodeToString(bytes, Base64.DEFAULT);
	}

	/**
	 * Encrypt a String using DES
	 * 
	 * @param input
	 * @return
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */
	private static byte[] encrypt(String input) throws BadPaddingException, IllegalBlockSizeException {
		return cipherEncrypt.doFinal(input.getBytes());
	}

}
