package de.jeisfeld.augendiagnoselib.util;

import android.support.annotation.Nullable;
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
	public static void log(@Nullable final String output) {
		if (output == null) {
			Log.i(Application.TAG, "null");
		}
		else {
			Log.i(Application.TAG, output);
		}
	}

	/**
	 * Make a log entry, including the end of the stack trace.
	 *
	 * @param output The base log entry.
	 * @param size   The number of stack trace elements.
	 */
	public static void logStack(final String output, final int size) {
		log(output);
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (int i = 3; i < Math.min(stackTrace.length, 3 + size); i++) { // MAGIC_NUMBER
			Log.i(Application.TAG, "[" + (i - 2) + "] " + stackTrace[i].toString());
		}
	}
}
