package de.eisfeldj.augendiagnose.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public abstract class DateUtil {

	/**
	 * Transfer a date String into a date object, using a given date format
	 * 
	 * @param date
	 * @param format
	 * @return
	 * @throws ParseException
	 */
	public static Date parse(String date, String format) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
		return dateFormat.parse(date);
	}

	/**
	 * Format a date object into a date String using a given date format
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static String format(Date date, String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
		return dateFormat.format(date);
	}

	/**
	 * Get display format of the date
	 * 
	 * @param calendar
	 * @return
	 */
	public static String getDisplayDate(Calendar calendar) {
		return format(calendar.getTime(), "d. MMMM yyyy");
	}

}
