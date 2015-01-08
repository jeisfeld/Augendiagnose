package de.eisfeldj.augendiagnosefx.util;

import java.util.ResourceBundle;

import org.apache.commons.lang3.StringEscapeUtils;

import javafx.scene.image.Image;

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
		return StringEscapeUtils.unescapeJava(STRINGS_BUNDLE.getString(key));
	}

	/**
	 * Get an Image.
	 *
	 * @param name
	 *            The name of the image file.
	 * @return The image object.
	 */
	public static Image getImage(final String name) {
		return new Image(ClassLoader.getSystemResourceAsStream("img/" + name));
	}

}
