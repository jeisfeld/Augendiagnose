package de.eisfeldj.augendiagnosefx.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Utility class for handling dates.
 */
public final class VersioningUtil {
	/**
	 * The current version of the application.
	 */
	public static final VersionInfo CURRENT_VERSION = new VersionInfo(1, "0.1");

	/**
	 * The URL where to get the info about latest version.
	 */
	private static final String CURRENT_VERSION_URL = "http://augendiagnose.jeisfeld.de/bin/currentVersion.txt";

	/**
	 * Hide default constructor.
	 */
	private VersioningUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Find out if application is running on 64bit java.
	 *
	 * @return true if 64bit
	 */
	private static boolean is64Bit() {
		return System.getProperty("sun.arch.data.model").equals("64");
	}

	/**
	 * Retrieve the download URL for a specific version of the application.
	 *
	 * @param version
	 *            The version number.
	 * @return The download URL.
	 */
	public static String getDownloadUrl(final String version) {
		StringBuffer result = new StringBuffer("http://augendiagnose.jeisfeld.de/bin/Augendiagnose-");
		if (is64Bit()) {
			result.append("x64");
		}
		else {
			result.append("x86");
		}
		result.append("-").append(version).append(".exe");
		return result.toString();
	}

	/**
	 * Retrieve information about the latest available application version (from Internet).
	 *
	 * @return The latest version info.
	 */
	@SuppressFBWarnings(value = "NP_DEREFERENCE_OF_READLINE_VALUE",
			justification = "Generically catching all exceptions")
	public static VersionInfo getLatestVersionInfo() {
		try {
			URLConnection connection = new URL(CURRENT_VERSION_URL).openConnection();
			InputStream inputStream = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String versionNumberString = reader.readLine();
			String versionString = reader.readLine();
			reader.close();
			inputStream.close();

			int versionNumber = Integer.parseInt(versionNumberString);
			return new VersionInfo(versionNumber, versionString);
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Check if there is a newer version of the application.
	 */
	public static void checkForNewerVersion() {
		VersionInfo latestVersion = getLatestVersionInfo();

		boolean requiresNewVersion = latestVersion.getVersionNumber() > CURRENT_VERSION.getVersionNumber();

		if (requiresNewVersion) {
			// TODO: inform the user and let him download
		}
	}

	/**
	 * A class holding information about the version of the application.
	 */
	public static final class VersionInfo {
		/**
		 * The version number (counting upwards from 1).
		 */
		private int versionNumber;

		public int getVersionNumber() {
			return versionNumber;
		}

		/**
		 * The version String (such as 1.2.3).
		 */
		private String versionString;

		public String getVersionString() {
			return versionString;
		}

		/**
		 * Constructor for a VersionInfo object.
		 *
		 * @param versionNumber
		 *            The version number.
		 * @param versionString
		 *            The version String.
		 */
		private VersionInfo(final int versionNumber, final String versionString) {
			this.versionNumber = versionNumber;
			this.versionString = versionString;
		}

		@Override
		public String toString() {
			return versionString + " (" + versionNumber + ")";
		}
	}

}
