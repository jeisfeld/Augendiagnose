package de.eisfeldj.augendiagnosefx.util;

import java.util.ResourceBundle;

/**
 * Utility class to get localized resources.
 */
public final class ResourceUtil {
	/**
	 * The resource bundle used for retrieving localized strings.
	 */
	public static final ResourceBundle STRINGS_BUNDLE = ResourceBundle.getBundle("bundles/Strings");

	/**
	 * Private constructor to disable instantiation.
	 */
	private ResourceUtil() {
		// do nothing
	}

	/**
	 * Get a String from the default resource bundle.
	 *
	 * @param key
	 *            The key for the String.
	 * @return The value.
	 */
	public static String getString(final String key) {
		return STRINGS_BUNDLE.getString(key);
	}

}
