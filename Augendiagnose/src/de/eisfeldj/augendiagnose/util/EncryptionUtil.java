package de.eisfeldj.augendiagnose.util;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

import de.eisfeldj.augendiagnose.Application;
import android.util.Base64;
import android.util.Log;

public class EncryptionUtil {

	private static Cipher cipherEncrypt;
	private static MessageDigest messageDigest;
	private static final String DUMMY_HASH = "dummy";
	private static final int HASH_LENGTH = 8;
	
	private static final String SPECIAL_KEY="Schnurpsi";

	static {
		try {
			String ALGORITHM = "DES";
			cipherEncrypt = Cipher.getInstance(ALGORITHM);
			String keyString = "bvQ+f3MqUu8=";
			Key symKey = new SecretKeySpec(Base64.decode(keyString, Base64.DEFAULT), ALGORITHM);
			cipherEncrypt.init(Cipher.ENCRYPT_MODE, symKey);

			messageDigest = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			Log.e(Application.TAG, "Failed to initialize EncryptionUtil");
		}
	}

	/**
	 * Utility method to test generation and validation of a user key
	 * @param name
	 */
	public static void test(String name) {
		String key = createUserKey(name);
		Logger.log("Key: " + key + ". Verified: " + validateUserKey(key));
	}

	/**
	 * Validate a user key
	 * @param key
	 * @return
	 */
	public static boolean validateUserKey(String key) {
		if(key==null || key.length()==0) {
			return false;
		}
		if(key.equals(SPECIAL_KEY)) {
			return true;
		}
		
		int index = key.lastIndexOf('-');
		if(index>0) {
			String name = key.substring(0, index);
			String hash = key.substring(index+1);
			return createCryptoHash(name).equals(hash);
		}
		else {
			return false;
		}
	}
	
	/**
	 * Generate a user key, which is a concatenation of user name and hash.
	 * @param input
	 * @return
	 */
	public static String createUserKey(String input) {
		return input + "-" + createCryptoHash(input);
	}

	/**
	 * Create a cryptographic hash from a username
	 * @param input
	 * @return
	 */
	private static String createCryptoHash(String input) {
		try {
			return convertBase64(createHash(encrypt(input))).substring(0, HASH_LENGTH);
		} catch (Exception e) {
			return DUMMY_HASH;
		}
	}

	/**
	 * Create a hash value from an input
	 * @param input
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	private static byte[] createHash(byte[] input) throws NoSuchAlgorithmException {
		return messageDigest.digest(input);
	}

	/**
	 * Do base 64 encoding of a message
	 * @param bytes
	 * @return
	 */
	private static String convertBase64(byte[] bytes) {
		return Base64.encodeToString(bytes, Base64.DEFAULT);
	}

	/**
	 * Encrypt a String using DES
	 * @param input
	 * @return
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */
	private static byte[] encrypt(String input) throws BadPaddingException, IllegalBlockSizeException {
		return cipherEncrypt.doFinal(input.getBytes());
	}

}
