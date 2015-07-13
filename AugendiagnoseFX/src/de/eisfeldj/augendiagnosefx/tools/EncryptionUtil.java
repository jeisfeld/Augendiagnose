package de.eisfeldj.augendiagnosefx.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

import java.util.Base64;

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
			Class<?> privateConstants = Class.forName("de.eisfeldj.augendiagnosefx.tools.PrivateConstants");
			Field specialKeysField = privateConstants.getDeclaredField("SPECIAL_KEYS");
			specialKeys = (List<String>) specialKeysField.get(null);
			Field keyField = privateConstants.getDeclaredField("KEY_STRING");
			key = (String) keyField.get(null);
			foundPrivateConstants = true;
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		SPECIAL_KEYS = specialKeys;
		KEY = key;

		if (foundPrivateConstants) {
			try {
				cipherEncrypt = Cipher.getInstance(ALGORITHM);
				Key symKey = new SecretKeySpec(Base64.getDecoder().decode(KEY), ALGORITHM);
				cipherEncrypt.init(Cipher.ENCRYPT_MODE, symKey);

				messageDigest = MessageDigest.getInstance("MD5");
				initialized = true;
			}
			catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
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
	 * @throws NoSuchAlgorithmException
	 */
	private static byte[] createHash(final byte[] input) throws NoSuchAlgorithmException {
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
		return Base64.getEncoder().encodeToString(bytes);
	}

	/**
	 * Encrypt a String using DES.
	 *
	 * @param input
	 *            the String to be encrypted.
	 * @return the encrypted String.
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */
	private static byte[] encrypt(final String input) throws BadPaddingException, IllegalBlockSizeException {
		return cipherEncrypt.doFinal(input.getBytes());
	}

	/**
	 * Main method - retrieves the user name and returns the user key.
	 *
	 * @param args
	 *            Command line arguments - not used.
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException {
		System.out.print("Enter name: "); // SYSTEMOUT

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input = br.readLine();
		System.out.println("User key: " + createUserKey(input)); // SYSTEMOUT

		/*
		 * Used keys: Augoustides-rox11m49
		 */
	}

}
