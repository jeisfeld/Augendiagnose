package de.eisfeldj.augendiagnosefx.util;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import de.eisfeldj.augendiagnosefx.Application;

/**
 * Utility class to get and store preferences.
 */
public final class PreferenceUtil {
	/**
	 * Preference key for the store option.
	 */
	public static final String KEY_STORE_OPTION = "key_store_option";

	/**
	 * Preference key for the Window width.
	 */
	public static final String KEY_WINDOW_SIZE_X = "key_window_size_x";

	/**
	 * Preference key for the Window height.
	 */
	public static final String KEY_WINDOW_SIZE_Y = "key_window_size_y";

	/**
	 * Preference key for the Window maximized property.
	 */
	public static final String KEY_WINDOW_MAXIMIZED = "key_window_maximized";

	/**
	 * Preference key for the flag indicating if the comment pane should be shown.
	 */
	public static final String KEY_SHOW_COMMENT_PANE = "key_show_comment_pane";

	/**
	 * Preference key for the Window maximized property.
	 */
	public static final String KEY_LAST_NAME = "key_last_name";

	/**
	 * Preference key for the eye photos folder.
	 */
	public static final String KEY_FOLDER_PHOTOS = "key_folder_photos";

	/**
	 * A map of default values for preferences.
	 */
	private static final Map<String, Object> DEFAULT_MAP = new HashMap<String, Object>();

	/**
	 * The user's preferences.
	 */
	private static Preferences prefs = Preferences.userNodeForPackage(Application.class);

	static {
		DEFAULT_MAP.put(KEY_STORE_OPTION, 2);
		DEFAULT_MAP.put(KEY_WINDOW_SIZE_X, 1024.0); // MAGIC_NUMBER
		DEFAULT_MAP.put(KEY_WINDOW_SIZE_Y, 720.0); // MAGIC_NUMBER
		DEFAULT_MAP.put(KEY_WINDOW_MAXIMIZED, false);
		DEFAULT_MAP.put(KEY_SHOW_COMMENT_PANE, true);
		DEFAULT_MAP.put(KEY_LAST_NAME, null);
		DEFAULT_MAP.put(KEY_FOLDER_PHOTOS, "D:\\");
	}

	/**
	 * Private constructor to disable instantiation.
	 */
	private PreferenceUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Set a String shared preference.
	 *
	 * @param key
	 *            The key of the preference.
	 * @param value
	 *            The value of the preference.
	 */
	public static void setPreference(final String key, final String value) {
		prefs.put(key, value);
	}

	/**
	 * Retrieve a String shared preference.
	 *
	 * @param key
	 *            the key of the preference.
	 *
	 * @return the corresponding preference value.
	 */
	public static String getPreferenceString(final String key) {
		return prefs.get(key, (String) DEFAULT_MAP.get(key));
	}

	/**
	 * Set an integer shared preference.
	 *
	 * @param key
	 *            The key of the preference.
	 * @param value
	 *            The value of the preference.
	 */
	public static void setPreference(final String key, final int value) {
		prefs.putInt(key, value);
	}

	/**
	 * Retrieve an integer shared preference.
	 *
	 * @param key
	 *            the key of the preference.
	 *
	 * @return the corresponding preference value.
	 */
	public static int getPreferenceInt(final String key) {
		return prefs.getInt(key, (Integer) DEFAULT_MAP.get(key));
	}

	/**
	 * Set an double shared preference.
	 *
	 * @param key
	 *            The key of the preference.
	 * @param value
	 *            The value of the preference.
	 */
	public static void setPreference(final String key, final double value) {
		prefs.putDouble(key, value);
	}

	/**
	 * Retrieve an double shared preference.
	 *
	 * @param key
	 *            the key of the preference.
	 *
	 * @return the corresponding preference value.
	 */
	public static double getPreferenceDouble(final String key) {
		return prefs.getDouble(key, (Double) DEFAULT_MAP.get(key));
	}

	/**
	 * Set a boolean shared preference.
	 *
	 * @param key
	 *            The key of the preference.
	 * @param value
	 *            The value of the preference.
	 */
	public static void setPreference(final String key, final boolean value) {
		prefs.putBoolean(key, value);
	}

	/**
	 * Retrieve a boolean shared preference.
	 *
	 * @param key
	 *            the key of the preference.
	 *
	 * @return the corresponding preference value.
	 */
	public static boolean getPreferenceBoolean(final String key) {
		return prefs.getBoolean(key, (Boolean) DEFAULT_MAP.get(key));
	}

}
