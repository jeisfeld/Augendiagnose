package de.eisfeldj.augendiagnosefx.tools;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class to hold private constants.
 */
public final class PrivateConstants {

	/**
	 * Hide default constructor.
	 */
	private PrivateConstants() {
		throw new UnsupportedOperationException();
	}

	/**
	 * The list of special keys that give extra permissions.
	 */
	protected static final List<String> SPECIAL_KEYS = Arrays.asList(new String[] { "Schnurpsi" });

	/**
	 * The key used for generation of valid user keys.
	 */
	protected static final String KEY_STRING = "bvQ+f3MqUu8=";

}
