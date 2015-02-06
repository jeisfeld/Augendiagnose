package de.eisfeldj.augendiagnosefx.util;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javafx.scene.paint.Color;
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
	 * Preference key for the flag indicating if the comment pane should be shown.
	 */
	public static final String KEY_SHOW_OVERLAY_PANE = "key_show_overlay_pane";

	/**
	 * Preference key for the Window maximized property.
	 */
	public static final String KEY_LAST_NAME = "key_last_name";

	/**
	 * Preference key for the eye photos folder.
	 */
	public static final String KEY_FOLDER_PHOTOS = "key_folder_photos";

	/**
	 * Preference key for max bitmap size.
	 */
	public static final String KEY_MAX_BITMAP_SIZE = "max_bitmap_size";

	/**
	 * Preference key for the thumbnail size.
	 */
	public static final String KEY_THUMBNAIL_SIZE = "thumbnail_size";

	/**
	 * Preference key for default overlay color.
	 */
	public static final String KEY_OVERLAY_COLOR = "key_overlay_color";

	/**
	 * Preference key for the flag if sorting should be by last name.
	 */
	public static final String KEY_SORT_BY_LAST_NAME = "key_sort_by_last_name";

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
		DEFAULT_MAP.put(KEY_SHOW_OVERLAY_PANE, true);
		DEFAULT_MAP.put(KEY_LAST_NAME, null);
		DEFAULT_MAP.put(KEY_FOLDER_PHOTOS, "D:\\");
		DEFAULT_MAP.put(KEY_MAX_BITMAP_SIZE, 2048); // MAGIC_NUMBER
		DEFAULT_MAP.put(KEY_THUMBNAIL_SIZE, 1024); // MAGIC_NUMBER
		DEFAULT_MAP.put(KEY_OVERLAY_COLOR, "#FF0000FF"); // RED
		DEFAULT_MAP.put(KEY_SORT_BY_LAST_NAME, false);
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

	/**
	 * Set a Color shared preference.
	 *
	 * @param key
	 *            The key of the preference.
	 * @param color
	 *            The value of the preference.
	 */
	public static void setPreference(final String key, final Color color) {
		double maxByte = 255.999; // MAGIC_NUMBER
		String colorString = String.format("#%02X%02X%02X%02X",
				(int) (color.getRed() * maxByte),
				(int) (color.getGreen() * maxByte),
				(int) (color.getBlue() * maxByte),
				(int) (color.getOpacity() * maxByte));
		prefs.put(key, colorString);
	}

	/**
	 * Retrieve a Color shared preference.
	 *
	 * @param key
	 *            the key of the preference.
	 *
	 * @return the corresponding preference value.
	 */
	public static Color getPreferenceColor(final String key) {
		String colorString = prefs.get(key, (String) DEFAULT_MAP.get(key));
		return Color.web(colorString);
	}
}
