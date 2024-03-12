package de.jeisfeld.augendiagnoselib.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;

/**
 * Utility for handling user keys.
 */
public final class EncryptionUtil {
	// JAVADOC:OFF
	// parameters for encryption
	private static Cipher mCipherEncrypt;
	private static MessageDigest mMessageDigest;
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
	@NonNull
	private static final List<String> SPECIAL_KEYS;

	/**
	 * Special keys that lead to prolonged trial period.
	 */
	@NonNull
	private static final List<String> TRIAL_PROLONGATION_KEYS;

	/**
	 * The private key to be used for user key generation.
	 */
	@NonNull
	private static final String KEY;

	/**
	 * Flag to store initialization status.
	 */
	private static boolean mIsInitialized = false;

	static {
		KEY = Application.getResourceString(R.string.private_key_string);

		String[] specialKeys = Application.getAppContext().getResources().getStringArray(R.array.private_special_keys);
		SPECIAL_KEYS = Arrays.asList(specialKeys);

		String[] trialProlongationKeys = Application.getAppContext().getResources().getStringArray(R.array.private_trial_prolongation_keys);
		TRIAL_PROLONGATION_KEYS = Arrays.asList(trialProlongationKeys);

		initializeCipher();
	}

	/**
	 * Initialize the encryption cipher.
	 */
	@SuppressLint("TrulyRandom")
	private static void initializeCipher() {
		try {
			mCipherEncrypt = Cipher.getInstance(ALGORITHM);
			Key symKey = new SecretKeySpec(Base64.decode(KEY, Base64.DEFAULT), ALGORITHM);
			mCipherEncrypt.init(Cipher.ENCRYPT_MODE, symKey);

			mMessageDigest = MessageDigest.getInstance("MD5");
			mIsInitialized = true;
		}
		catch (Exception e) {
			Log.e(Application.TAG, "Failed to initialize EncryptionUtil", e);
		}
	}

	/**
	 * Utility method to test generation and validation of a user key.
	 *
	 * @param name the user name for which key generation is to be tested.
	 */
	public static void test(@NonNull final String name) {
		String key = createUserKey(name);
		Log.i(Application.TAG, "Key: " + key + ". Verification result: " + validateUserKey(key));
	}

	/**
	 * Validate a user key.
	 *
	 * @param key the user key to be validated.
	 * @return the validation result
	 */
	public static KeyValidationResult validateUserKey(@Nullable final String key) {
		if (key == null || key.isEmpty() || !mIsInitialized) {
			return KeyValidationResult.FAILED;
		}
		UserKeyStatus userKeyStatus = UserKeyStatus.storedValue();
		if (userKeyStatus == UserKeyStatus.BLOCKED || userKeyStatus == UserKeyStatus.FAILED) {
			return KeyValidationResult.FAILED;
		}
		else if (userKeyStatus == UserKeyStatus.SUCCESS) {
			return KeyValidationResult.SUCCESS;
		}

		if (SPECIAL_KEYS.contains(key)) {
			return KeyValidationResult.SUCCESS;
		}
		if (TRIAL_PROLONGATION_KEYS.contains(key)) {
			return KeyValidationResult.PROLONG_TRIAL;
		}

		return isKeyValid(key) ? KeyValidationResult.SUCCESS : KeyValidationResult.FAILED;
	}

	/**
	 * Check if user key is valid according to standard key policy.
	 *
	 * @param key The key
	 * @return True if valid
	 */
	private static boolean isKeyValid(final String key) {
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
	 * Make online check of the user key.
	 *
	 * @param activity The activity triggering the check.
	 * @param listener The listener called after status is retrieved.
	 */
	public static void checkUserKeyOnline(final Activity activity, final OnKeyStatusReceivedListener listener) {
		String key = PreferenceUtil.getSharedPreferenceString(R.string.key_user_key);
		if (key == null || key.isEmpty()) {
			PreferenceUtil.removeSharedPreference(R.string.key_user_key_status);
			if (listener != null) {
				listener.onKeyStatusReceived(UserKeyStatus.UNKNOWN, false);
			}
			return;
		}
		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(Application.getResourceString(R.string.private_http_username),
						Application.getResourceString(R.string.private_http_password).toCharArray());
			}
		});
		String urlBase = "https://augendiagnose-app.de/appauth/";

		new Thread(() -> {
			Reader in = null;
			try {
				URL url = new URL(urlBase);
				URLConnection urlConnection = url.openConnection();
				urlConnection.setDoOutput(true);
				((HttpsURLConnection) urlConnection).setRequestMethod("POST");
				urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				String postData = "androidId=" + SystemUtil.getAndroidId() + "&userKey=" + key;
				byte[] postDataBytes = postData.getBytes("UTF-8");
				urlConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
				urlConnection.getOutputStream().write(postDataBytes);
				in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
				StringBuilder result = new StringBuilder();
				for (int c; (c = in.read()) >= 0; ) {
					result.append((char) c);
				}
				JSONObject jsonObject = new JSONObject(result.toString());
				boolean success = "success".equals(jsonObject.getString("status"));
				if (success) {
					String keyStatusString = jsonObject.getString("keystatus");
					UserKeyStatus status = UserKeyStatus.valueOf(keyStatusString);
					boolean isStatusChanged = status != UserKeyStatus.storedValue();
					status.store();
					if (listener != null) {
						listener.onKeyStatusReceived(status, isStatusChanged);
					}
					if (status == UserKeyStatus.BLOCKED && isStatusChanged) {
						PreferenceUtil.setSharedPreferenceString(R.string.key_user_key, "");
						activity.runOnUiThread(() -> {
							DialogUtil.displayError(activity, R.string.message_dialog_blocked_user_key, true, key);
						});
					}
				}
			}
			catch (IOException e) {
				Log.e(Application.TAG, "Invalid URL", e);
			}
			catch (JSONException e) {
				Log.e(Application.TAG, "Invalid Response", e);
			}
			catch (IllegalArgumentException e) {
				Log.e(Application.TAG, "Invalid Status", e);
			}
			finally {
				if (in != null) {
					try {
						in.close();
					}
					catch (IOException e) {
						// ignore
					}
				}
			}
		}).start();
	}



	/**
	 * Generate a user key, which is a concatenation of user name and hash.
	 *
	 * @param input the user key without hash.
	 * @return the user key including hash.
	 */
	@NonNull
	private static String createUserKey(@NonNull final String input) {
		return input + "-" + createCryptoHash(input);
	}

	/**
	 * Create a cryptographic hash from a String.
	 *
	 * @param input the input for creating the hash. (Will be username.)
	 * @return the cryptographic hash.
	 */
	@NonNull
	private static String createCryptoHash(@NonNull final String input) {
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
	 * @param input the input for the hash creation.
	 * @return the hash.
	 */
	private static byte[] createHash(final byte[] input) {
		return mMessageDigest.digest(input);
	}

	/**
	 * Do base 64 encoding of a message.
	 *
	 * @param bytes the bytes to be encoded.
	 * @return the base 64 encoded String.
	 */
	private static String convertBase64(final byte[] bytes) {
		return Base64.encodeToString(bytes, Base64.DEFAULT);
	}

	/**
	 * Encrypt a String using DES.
	 *
	 * @param input the String to be encrypted.
	 * @return the encrypted String.
	 * @throws BadPaddingException       if this cipher is in decryption mode, and (un)padding has been requested, but the decrypted data is
	 *                                   not bounded by the appropriate padding bytes
	 * @throws IllegalBlockSizeException if this cipher is a block cipher, no padding has been requested (only in encryption mode), and the
	 *                                   total input length of the data processed by this cipher is not a multiple of block size; or if this
	 *                                   encryption algorithm is unable to process the input data provided.
	 */
	private static byte[] encrypt(@NonNull final String input) throws BadPaddingException, IllegalBlockSizeException {
		return mCipherEncrypt.doFinal(input.getBytes());
	}

	/**
	 * Create a hash value of a String.
	 *
	 * @param input The input string.
	 * @return The hash value.
	 */
	public static String createHash(@NonNull final String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			md.reset();
			byte[] buffer = input.getBytes("UTF-8");
			md.update(buffer);
			byte[] digest = md.digest();

			StringBuilder hexStr = new StringBuilder();
			for (byte b : digest) {
				hexStr.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1)); // MAGIC_NUMBER
			}
			return hexStr.toString();
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Listener called when online query for user key status is completed.
	 */
	public interface OnKeyStatusReceivedListener {
		/**
		 * Callback called if online query for user key status is done.
		 *
		 * @param userKeyStatus   The user key status.
		 * @param isStatusChanged Flag indicating if status is changed.
		 */
		void onKeyStatusReceived(final UserKeyStatus userKeyStatus, final boolean isStatusChanged);
	}

	/**
	 * Result of user key validation.
	 */
	public enum KeyValidationResult {
		/**
		 * Validation successful.
		 */
		SUCCESS,
		/**
		 * Validation failed.
		 */
		FAILED,
		/**
		 * Extension of trial period.
		 */
		PROLONG_TRIAL
	}

	/**
	 * The online verification status of user key.
	 */
	public enum UserKeyStatus {
		/**
		 * Unknown.
		 */
		UNKNOWN,
		/**
		 * Successfully validated.
		 */
		SUCCESS,
		/**
		 * Rejected in validation.
		 */
		FAILED,
		/**
		 * Blocked.
		 */
		BLOCKED;

		/**
		 * Store the status.
		 */
		public void store() {
			PreferenceUtil.setSharedPreferenceInt(R.string.key_user_key_status, ordinal());
		}

		/**
		 * Get the stored status.
		 * @return The stored status.
		 */
		public static UserKeyStatus storedValue() {
			int ordinal = PreferenceUtil.getSharedPreferenceInt(R.string.key_user_key_status, -1);
			for (UserKeyStatus status : values()) {
				if (status.ordinal() == ordinal) {
					return status;
				}
			}
			return UNKNOWN;
		}
	}
}
