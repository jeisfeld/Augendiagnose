package de.jeisfeld.augendiagnoselib.util;

import android.util.Log;

import de.jeisfeld.augendiagnoselib.Application;

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
	 * @param output the content of th log entry.
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
