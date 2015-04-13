package de.eisfeldj.augendiagnose.util;

import android.os.Build;

/**
 * Utility class for getting information about Android version.
 */
public abstract class VersionUtil {
	/**
	 * Get information if Android version is Kitkat (4.4).
	 *
	 * @return true if Kitkat.
	 */
	public static boolean isKitkat() {
		return Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT;
	}

	/**
	 * Get information if Android version is Lollipop (5.0) or higher.
	 *
	 * @return true if Lollipop or higher.
	 */
	public static boolean isAndroid5() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
	}

}
