package de.eisfeldj.augendiagnose.util;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class KeyboardUtil {

	/**
	 * Utility method to hide the soft keyboard
	 * 
	 * @param context
	 * @param editText
	 */
	public static void hideKeyboard(Context context, EditText editText) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

}
