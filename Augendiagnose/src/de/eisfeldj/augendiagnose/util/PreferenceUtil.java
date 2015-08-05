package de.eisfeldj.augendiagnose.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;

/**
 * Utility class for handling the shared preferences.
 */
public final class PreferenceUtil {
	/**
	 * The list of preferences used for switching on and off hints.
	 */
	private static final Integer[] HINT_PREFERENCES = { R.string.key_tip_admarvel,
			R.string.key_tip_clarity,
			R.string.key_tip_displaydetails,
			R.string.key_tip_displaynames,
			R.string.key_tip_displaypictures,
			R.string.key_tip_editcomment,
			R.string.key_tip_firstuse,
			R.string.key_tip_organizephotos,
			R.string.key_tip_overlay,
			R.string.key_tip_saveview };

	/**
	 * Hide default constructor.
	 */
	private PreferenceUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Retrieve the default shared preferences of the application.
	 *
	 * @return the default shared preferences.
	 */
	public static SharedPreferences getSharedPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(Application.getAppContext());
	}

	/**
	 * Retrieve a String shared preference.
	 *
	 * @param preferenceId
	 *            the id of the shared preference.
	 * @return the corresponding preference value.
	 */
	public static String getSharedPreferenceString(final int preferenceId) {
		return getSharedPreferences().getString(Application.getAppContext().getString(preferenceId), "");
	}

	/**
	 * Retrieve a String shared preference, setting a default value if the preference is not set.
	 *
	 * @param preferenceId
	 *            the id of the shared preference.
	 * @param defaultId
	 *            the String key of the default value.
	 * @return the corresponding preference value.
	 */
	public static String getSharedPreferenceString(final int preferenceId, final int defaultId) {
		String result = getSharedPreferences().getString(Application.getAppContext().getString(preferenceId), null);
		if (result == null) {
			result = Application.getAppContext().getString(defaultId);
			setSharedPreferenceString(preferenceId, result);
		}
		return result;
	}

	/**
	 * Set a String shared preference.
	 *
	 * @param preferenceId
	 *            the id of the shared preference.
	 * @param s
	 *            the target value of the preference.
	 */
	public static void setSharedPreferenceString(final int preferenceId, final String s) {
		Editor editor = getSharedPreferences().edit();
		editor.putString(Application.getAppContext().getString(preferenceId), s);
		editor.commit();
	}

	/**
	 * Retrieve an Uri shared preference.
	 *
	 * @param preferenceId
	 *            the id of the shared preference.
	 * @return the corresponding preference value.
	 */
	public static Uri getSharedPreferenceUri(final int preferenceId) {
		String uriString = getSharedPreferences().getString(Application.getAppContext().getString(preferenceId), null);

		if (uriString == null) {
			return null;
		}
		else {
			return Uri.parse(uriString);
		}
	}

	/**
	 * Get the stored tree URIs.
	 *
	 * @return The tree URIs.
	 */
	public static Uri[] getTreeUris() {
		List<Uri> uris = new ArrayList<Uri>();

		Uri uri1 = getSharedPreferenceUri(R.string.key_internal_uri_extsdcard_photos);
		if (uri1 != null) {
			uris.add(uri1);
		}

		Uri uri2 = getSharedPreferenceUri(R.string.key_internal_uri_extsdcard_input);
		if (uri2 != null) {
			uris.add(uri2);
		}
		return uris.toArray(new Uri[0]);
	}

	/**
	 * Set a shared preference for an Uri.
	 *
	 * @param preferenceId
	 *            the id of the shared preference.
	 * @param uri
	 *            the target value of the preference.
	 */
	public static void setSharedPreferenceUri(final int preferenceId, final Uri uri) {
		Editor editor = getSharedPreferences().edit();
		if (uri == null) {
			editor.putString(Application.getAppContext().getString(preferenceId), null);
		}
		else {
			editor.putString(Application.getAppContext().getString(preferenceId), uri.toString());
		}
		editor.commit();
	}

	/**
	 * Retrieve a boolean shared preference.
	 *
	 * @param preferenceId
	 *            the id of the shared preference.
	 * @return the corresponding preference value.
	 */
	public static boolean getSharedPreferenceBoolean(final int preferenceId) {
		return getSharedPreferences().getBoolean(Application.getAppContext().getString(preferenceId), false);
	}

	/**
	 * Set a Boolean shared preference.
	 *
	 * @param preferenceId
	 *            the id of the shared preference.
	 * @param b
	 *            the target value of the preference.
	 */
	public static void setSharedPreferenceBoolean(final int preferenceId, final boolean b) {
		Editor editor = getSharedPreferences().edit();
		editor.putBoolean(Application.getAppContext().getString(preferenceId), b);
		editor.commit();
	}

	/**
	 * Retrieve an integer shared preference.
	 *
	 * @param preferenceId
	 *            the id of the shared preference.
	 * @param defaultValue
	 *            the default value of the shared preference.
	 * @return the corresponding preference value.
	 */
	public static int getSharedPreferenceInt(final int preferenceId, final int defaultValue) {
		return getSharedPreferences().getInt(Application.getAppContext().getString(preferenceId), defaultValue);
	}

	/**
	 * Set an integer shared preference.
	 *
	 * @param preferenceId
	 *            the id of the shared preference.
	 * @param i
	 *            the target value of the preference.
	 */
	public static void setSharedPreferenceInt(final int preferenceId, final int i) {
		Editor editor = getSharedPreferences().edit();
		editor.putInt(Application.getAppContext().getString(preferenceId), i);
		editor.commit();
	}

	/**
	 * Increment a counter shared preference, and return the new value.
	 *
	 * @param preferenceId
	 *            the id of the shared preference.
	 * @return the new value.
	 */
	public static int incrementCounter(final int preferenceId) {
		int newValue = getSharedPreferenceInt(preferenceId, 0) + 1;
		setSharedPreferenceInt(preferenceId, newValue);
		return newValue;
	}

	/**
	 * Retrieve a long shared preference.
	 *
	 * @param preferenceId
	 *            the id of the shared preference.
	 * @param defaultValue
	 *            the default value of the shared preference.
	 * @return the corresponding preference value.
	 */
	public static long getSharedPreferenceLong(final int preferenceId, final long defaultValue) {
		return getSharedPreferences().getLong(Application.getAppContext().getString(preferenceId), defaultValue);
	}

	/**
	 * Set a long shared preference.
	 *
	 * @param preferenceId
	 *            the id of the shared preference.
	 * @param i
	 *            the target value of the preference.
	 */
	public static void setSharedPreferenceLong(final int preferenceId, final long i) {
		Editor editor = getSharedPreferences().edit();
		editor.putLong(Application.getAppContext().getString(preferenceId), i);
		editor.commit();
	}

	/**
	 * Set all hint preferences to the given value.
	 *
	 * @param value
	 *            The value.
	 */
	public static void setAllHints(final boolean value) {
		for (int preferenceId : Arrays.asList(HINT_PREFERENCES)) {
			setSharedPreferenceBoolean(preferenceId, value);
		}
	}

	/**
	 * Set the default setting for handling of full resolution, if no value is set.
	 */
	public static void setDefaultResolutionSettings() {
		// Max bitmap size
		String maxBitmapSize = getSharedPreferenceString(R.string.key_max_bitmap_size);

		if (maxBitmapSize == null || maxBitmapSize.length() == 0) {
			// Only if 512 MB are accessible, then full resolution image should be stored.
			int memoryClass = SystemUtil.getLargeMemoryClass();
			if (memoryClass >= 256) { // MAGIC_NUMBER
				maxBitmapSize = "2048";
			}
			else {
				maxBitmapSize = "1024";
			}

			setSharedPreferenceString(R.string.key_max_bitmap_size, maxBitmapSize);
		}

		// Full resolution setting
		String fullResolutionSetting = getSharedPreferenceString(R.string.key_full_resolution);

		if (fullResolutionSetting == null || fullResolutionSetting.length() == 0) {
			// Only if 512 MB are accessible, then full resolution image should be stored.
			int memoryClass = SystemUtil.getLargeMemoryClass();
			if (memoryClass >= 512) { // MAGIC_NUMBER
				fullResolutionSetting = "2";
			}
			else if (memoryClass >= 256) { // MAGIC_NUMBER
				fullResolutionSetting = "1";
			}
			else {
				fullResolutionSetting = "0";
			}

			setSharedPreferenceString(R.string.key_full_resolution, fullResolutionSetting);
		}
	}

}
