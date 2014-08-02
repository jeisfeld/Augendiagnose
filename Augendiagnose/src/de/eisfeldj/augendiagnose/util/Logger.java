package de.eisfeldj.augendiagnose.util;

import de.eisfeldj.augendiagnose.Application;
import android.util.Log;

/**
 * Utility class for debugging.
 */
public final class Logger {

	/**
	 * Hide default constructor.
	 */
	private Logger() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Make a log entry.
	 *
	 * @param output
	 *            the content of th log entry.
	 */
	public static void log(final String output) {
		if (output == null) {
			Log.i(Application.TAG, "null");
		}
		else {
			Log.i(Application.TAG, output);
		}
	}
}
