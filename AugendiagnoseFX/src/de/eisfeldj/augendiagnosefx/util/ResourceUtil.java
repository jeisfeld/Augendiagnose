package de.eisfeldj.augendiagnosefx.util;

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringEscapeUtils;

import javafx.scene.image.Image;

/**
 * Utility class to get localized resources.
 */
public final class ResourceUtil {
	/**
	 * The path of the resource bundle.
	 */
	private static final String BUNDLE_NAME = "bundles/Strings";

	/**
	 * The resource bundle used for retrieving localized strings.
	 */
	public static final ResourceBundle STRINGS_BUNDLE;

	static {
		switch (PreferenceUtil.getPreferenceInt(PreferenceUtil.KEY_LANGUAGE)) {
		case 1:
			// English bundle is the unlocalized one.
			STRINGS_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, new Locale(""));
			break;
		case 2:
			STRINGS_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, new Locale("de"));
			break;
		case 3: // MAGIC_NUMBER
			STRINGS_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, new Locale("es"));
			break;
		default:
			STRINGS_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
		}
	}

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
