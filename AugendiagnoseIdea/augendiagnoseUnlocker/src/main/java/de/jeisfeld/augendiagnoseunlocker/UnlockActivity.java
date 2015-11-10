package de.jeisfeld.augendiagnoseunlocker;

import java.security.MessageDigest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Main activity of the application.
 */
public class UnlockActivity extends Activity {
	/**
	 * The resource key for the authorization with the unlocker app.
	 */
	private static final String STRING_EXTRA_REQUEST_KEY = "de.jeisfeld.augendiagnoseunlocker.REQUEST_KEY";
	/**
	 * The resource key for the name of the first selected file.
	 */
	private static final String STRING_RESULT_RESPONSE_KEY = "de.jeisfeld.augendiagnoseunlocker.RESPONSE_KEY";

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent starterIntent = getIntent();
		String requestKey = starterIntent.getStringExtra(STRING_EXTRA_REQUEST_KEY);

		Bundle resultData = new Bundle();
		String hashValue = createHash(requestKey + getString(R.string.private_unlock_key));
		resultData.putString(STRING_RESULT_RESPONSE_KEY, hashValue);

		Intent responseIntent = new Intent();
		responseIntent.putExtras(resultData);
		setResult(RESULT_OK, responseIntent);
		finish();
	}

	/**
	 * Create a hash value of a String.
	 *
	 * @param input The input string.
	 * @return The hash value.
	 */
	private static String createHash(final String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			md.reset();
			byte[] buffer = input.getBytes("UTF-8");
			md.update(buffer);
			byte[] digest = md.digest();

			String hexStr = "";
			for (int i = 0; i < digest.length; i++) {
				hexStr += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1); // MAGIC_NUMBER
			}
			return hexStr;
		}
		catch (Exception e) {
			return null;
		}
	}

}
