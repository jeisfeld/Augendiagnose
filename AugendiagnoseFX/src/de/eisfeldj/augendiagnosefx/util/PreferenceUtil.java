package de.eisfeldj.augendiagnosefx.util;

/**
 * Utility class to get and store preferences.
 */
public final class PreferenceUtil {
	/**
	 * Preference key for the store option.
	 */
	public static final String KEY_STORE_OPTION = "key_store_option";

	/**
	 * Private constructor to disable instantiation.
	 */
	private PreferenceUtil() {
		// do nothing.
	}

	/**
	 * Retrieve a String shared preference.
	 *
	 * @param key
	 *            the key of the preference.
	 * @return the corresponding preference value.
	 */
	public static String getPreferenceString(final String key) {
		// TODO

		if (key.equals(KEY_STORE_OPTION)) {
			return "2";
		}
		else {
			return null;
		}
	}
}
