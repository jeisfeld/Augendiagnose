package de.eisfeldj.augendiagnose.util;

import de.eisfeldj.augendiagnose.Application;
import android.util.Log;

/**
 * Utility class for debugging - git test
 */
public class Logger {
	/**
	 * Make a log entry
	 */
	public static void log(String output) {
		if(output == null) {
			Log.i(Application.TAG, "null");
		}
		else {
			Log.i(Application.TAG, output);
		}
	}
}
