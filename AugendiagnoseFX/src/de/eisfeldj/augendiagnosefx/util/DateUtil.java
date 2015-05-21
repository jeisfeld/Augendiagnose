package de.eisfeldj.augendiagnosefx.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for handling dates.
 */
public final class DateUtil {

	/**
	 * Hide default constructor.
	 */
	private DateUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Transfer a date String into a date object, using a given date format.
	 *
	 * @param date
	 *            the date string
	 * @param format
	 *            the date format
	 * @return the date object
	 * @throws ParseException
	 */
	public static Date parse(final String date, final String format) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
		return dateFormat.parse(date);
	}

	/**
	 * Format a date object into a date String using a given date format.
	 *
	 * @param date
	 *            the date object
	 * @param format
	 *            the date format
	 * @return the formatted date
	 */
	public static String format(final Date date, final String format) {
		DateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
		return dateFormat.format(date);
	}

	/**
	 * Format a date object into a date String using the default date format.
	 *
	 * @param date
	 *            the date object
	 * @return the formatted date
	 */
	public static String format(final Date date) {
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
		return dateFormat.format(date);
	}

}
