package de.eisfeldj.augendiagnosefx.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

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

		File logDir = null;
		File logFile = new File("");
		try {
			logDir = new File(new File(System.getProperty("java.io.tmpdir")), "Augendiagnose");
			logFile = new File(logDir, "Augendiagnose.log");
			logDir.mkdirs();
			FileHandler fileHandler = new FileHandler(logFile.getAbsolutePath(), 1000000, 5, true); // MAGIC_NUMBER
			fileHandler.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(fileHandler);
		}
		catch (SecurityException | IOException e) {
			DialogUtil.displayError(ResourceConstants.MESSAGE_DIALOG_COULD_NOT_OPEN_LOG_FILE,
					logFile.getAbsolutePath(), ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		}
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
