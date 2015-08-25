package de.jeisfeld.augendiagnoselib.util;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Utility class for keyboard handling.
 */
public final class KeyboardUtil {

	/**
	 * Hide default constructor.
	 */
	private KeyboardUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Utility method to hide the soft keyboard.
	 *
	 * @param context the context.
	 * @param editText the EditText triggering the soft keyboard.
	 */
	public static void hideKeyboard(final Context context, final EditText editText) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

}
