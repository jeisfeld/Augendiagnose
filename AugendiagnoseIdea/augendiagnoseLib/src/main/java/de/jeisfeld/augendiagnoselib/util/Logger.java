package de.jeisfeld.augendiagnoselib.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

	/**
	 * Make a log entry, including the contents of a list.
	 *
	 * @param output The base log entry.
	 * @param list   The list to be output.
	 */
	public static void log(final String output, final List<?> list) {
		log(output);
		if (list == null) {
			log("    size: null");
			return;
		}
		log("    size: " + list.size());
		int counter = 0;
		for (Object element : list) {
			log("    [" + counter++ + "] " + element.toString());
		}
	}

	/**
	 * Make a log entry, including the contents of an Object array.
	 *
	 * @param output The base log entry.
	 * @param array  The array to be output.
	 */
	public static void log(final String output, final Object[] array) {
		log(output, Arrays.asList(array));
	}

	/**
	 * Make a log entry, including the contents of an int array.
	 *
	 * @param output The base log entry.
	 * @param array  The array to be output.
	 */
	public static void log(final String output, final int[] array) {
		List<Integer> list = new ArrayList<>();
		for (int element : array) {
			list.add(element);
		}
		log(output, list);
	}

	/**
	 * Make a log entry, including the contents of a long array.
	 *
	 * @param output The base log entry.
	 * @param array  The array to be output.
	 */
	public static void log(final String output, final long[] array) {
		List<Long> list = new ArrayList<>();
		for (long element : array) {
			list.add(element);
		}
		log(output, list);
	}

	/**
	 * Make a log entry, including the contents of a float array.
	 *
	 * @param output The base log entry.
	 * @param array  The array to be output.
	 */
	public static void log(final String output, final float[] array) {
		List<Float> list = new ArrayList<>();
		for (float element : array) {
			list.add(element);
		}
		log(output, list);
	}

	/**
	 * Make a log entry, including the contents of a double array.
	 *
	 * @param output The base log entry.
	 * @param array  The array to be output.
	 */
	public static void log(final String output, final double[] array) {
		List<Double> list = new ArrayList<>();
		for (double element : array) {
			list.add(element);
		}
		log(output, list);
	}
}
