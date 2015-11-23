package com.android.vending.billing;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

/**
 * Security-related methods. For a secure implementation, all of this code should be implemented on a server that
 * communicates with the application on the device. For the sake of simplicity and clarity of this example, this code is
 * included here and is executed on the device. If you must verify the purchases on the phone, you should obfuscate this
 * code to make it harder for an attacker to replace the code with stubs that treat all purchases as verified.
 */
public final class Security {

	// JAVADOC:OFF

	private static final String TAG = "IABUtil/Security";

	private static final String KEY_FACTORY_ALGORITHM = "RSA";
	private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

	private Security() throws IllegalAccessException {
		throw new IllegalAccessException();
	}

	/**
	 * Verifies that the data was signed with the given signature, and returns the verified purchase. The data is in
	 * JSON format and signed with a private key. The data also contains the PurchaseState and product ID of the
	 * purchase.
	 *
	 * @param base64PublicKey the base64-encoded public key to use for verifying.
	 * @param signedData      the signed JSON string (signed, not encrypted)
	 * @param signature       the signature for the data, signed with the private key
	 * @return true if verification was successful.
	 */
	public static boolean verifyPurchase(@NonNull final String base64PublicKey, @NonNull final String signedData, @NonNull final String signature) {
		if (TextUtils.isEmpty(signedData) || TextUtils.isEmpty(base64PublicKey)) {
			Log.e(TAG, "Purchase verification failed: missing data.");
			return false;
		}

		if (!TextUtils.isEmpty(signature)) {
			PublicKey key = Security.generatePublicKey(base64PublicKey);
			if (!Security.verify(key, signedData, signature)) {
				Log.w(TAG, "signature does not match data.");
				return false;
			}
		}
		return true;
	}

	/**
	 * Generates a PublicKey instance from a string containing the Base64-encoded public key.
	 *
	 * @param encodedPublicKey Base64-encoded public key
	 * @return the PublicKey instance.
	 */
	private static PublicKey generatePublicKey(@NonNull final String encodedPublicKey) {
		try {
			byte[] decodedKey = Base64.decode(encodedPublicKey);
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
			return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		catch (InvalidKeySpecException e) {
			Log.e(TAG, "Invalid key specification.");
			throw new IllegalArgumentException(e);
		}
		catch (Base64DecoderException e) {
			Log.e(TAG, "Base64 decoding failed.");
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Verifies that the signature from the server matches the computed signature on the data. Returns true if the data
	 * is correctly signed.
	 *
	 * @param publicKey  public key associated with the developer account
	 * @param signedData signed data from server
	 * @param signature  server signature
	 * @return true if the data and signature match
	 */
	private static boolean verify(final PublicKey publicKey, @NonNull final String signedData, @NonNull final String signature) {
		Signature sig;
		try {
			sig = Signature.getInstance(SIGNATURE_ALGORITHM);
			sig.initVerify(publicKey);
			sig.update(signedData.getBytes());
			if (!sig.verify(Base64.decode(signature))) {
				Log.e(TAG, "Signature verification failed.");
				return false;
			}
			return true;
		}
		catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "NoSuchAlgorithmException.");
		}
		catch (InvalidKeyException e) {
			Log.e(TAG, "Invalid key specification.");
		}
		catch (SignatureException e) {
			Log.e(TAG, "Signature exception.");
		}
		catch (Base64DecoderException e) {
			Log.e(TAG, "Base64 decoding failed.");
		}
		return false;
	}
}
