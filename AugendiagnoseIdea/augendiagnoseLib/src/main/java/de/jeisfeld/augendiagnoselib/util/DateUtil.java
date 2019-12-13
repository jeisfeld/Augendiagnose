package de.jeisfeld.augendiagnoselib.util;

import android.content.Context;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;

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
	 * @param date   the date string
	 * @param format the date format
	 * @return the date object
	 * @throws ParseException thrown if the date string cannot be parsed.
	 */
	public static Date parse(final String date, @NonNull final String format) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
		return dateFormat.parse(date);
	}

	/**
	 * Format a date object into a date String using a given date format.
	 *
	 * @param date   the date object
	 * @param format the date format
	 * @return the formatted date
	 */
	public static String format(final Date date, @NonNull final String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
		return dateFormat.format(date);
	}

	/**
	 * Format a date object into a date String using the Android system default date format.
	 *
	 * @param context the context.
	 * @param date the date object
	 * @return the formatted date
	 */
	public static String format(final Context context, final Date date) {
		DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(context);
		return dateFormat.format(date);
	}

	/**
	 * Get display format of the date.
	 *
	 * @param context the context
	 * @param calendar the date as calendar object
	 * @return the date formatted for display
	 */
	public static String getDisplayDate(final Context context, @NonNull final Calendar calendar) {
		DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(context);
		return dateFormat.format(calendar.getTime());
	}

}
