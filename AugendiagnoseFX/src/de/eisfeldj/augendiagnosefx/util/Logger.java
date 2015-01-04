package de.eisfeldj.augendiagnosefx.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Utility class for debugging.
 */
public final class Logger {

	/**
	 * The logger to be used.
	 */
	private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);

	static {
		System.setProperty("java.util.logging.SimpleFormatter.format",
				"%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %5$s%6$s%n");
	}

	/**
	 * Hide default constructor.
	 */
	private Logger() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Make an info entry.
	 *
	 * @param output
	 *            the content of the log entry.
	 */
	public static void info(final String output) {
		LOGGER.info(output);
	}

	/**
	 * Make a warning entry.
	 *
	 * @param output
	 *            the content of the log entry.
	 */
	public static void warning(final String output) {
		LOGGER.warning(output);
	}

	/**
	 * Make an error entry.
	 *
	 * @param output
	 *            the content of the log entry.
	 */
	public static void error(final String output) {
		LOGGER.severe(output);
	}

	/**
	 * Make an error entry.
	 *
	 * @param output
	 *            The log entry
	 * @param e
	 *            The exception
	 */
	public static void error(final String output, final Throwable e) {
		error(output + "\n" + ExceptionUtils.getStackTrace(e));
	}

}
