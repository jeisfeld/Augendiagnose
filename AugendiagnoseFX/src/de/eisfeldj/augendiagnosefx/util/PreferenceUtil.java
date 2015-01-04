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
	 * A map of default values for preferences.
	 */
	private static final Map<String, Object> DEFAULT_MAP = new HashMap<String, Object>();

	/**
	 * The user's preferences.
	 */
	private static Preferences prefs = Preferences.userNodeForPackage(Application.class);

	static {
		DEFAULT_MAP.put(KEY_STORE_OPTION, 2);
	}

	/**
	 * Private constructor to disable instantiation.
	 */
	private PreferenceUtil() {
		// do nothing.
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
