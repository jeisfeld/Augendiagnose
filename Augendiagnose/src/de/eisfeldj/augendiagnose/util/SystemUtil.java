package de.eisfeldj.augendiagnose.util;

/**
 * Utility class to get system information
 */
public class SystemUtil {
	/**
	 * Verify if the app is run on BlueStacks
	 */
	public static boolean isBlueStacks() {
		return System.getProperty("os.version").contains("android-bst");
	}
}
