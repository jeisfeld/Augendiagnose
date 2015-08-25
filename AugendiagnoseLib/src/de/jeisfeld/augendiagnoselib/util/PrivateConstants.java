package de.jeisfeld.augendiagnoselib.util;

import java.util.List;

/**
 * Utility interface for private constants.
 */
public interface PrivateConstants {

	/**
	 * Get the list of special keys that give extra permissions.
	 *
	 * @return The list of special keys that give extra permissions.
	 */
	List<String> getSpecialKeys();

	/**
	 * Get the key used for generation of valid user keys.
	 *
	 * @return The key used for generation of valid user keys.
	 */
	String getKeyString();

	/**
	 * Get the public license key of the app.
	 *
	 * @return The public license key of the app.
	 */
	String getLicenseKey();
}
