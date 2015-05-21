package de.eisfeldj.augendiagnosefx.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import de.eisfeldj.augendiagnosefx.util.DialogUtil.ConfirmDialogListener;
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
	 * Base URL for downloading the application.
	 */
	private static final String DOWNLOAD_BASE_URL = "http://augendiagnose.jeisfeld.de/bin/";

	/**
	 * The URL where to get the info about latest version.
	 */
	private static final String CURRENT_VERSION_URL = DOWNLOAD_BASE_URL + "currentVersion.txt";

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

		if (latestVersion == null) {
			return;
		}

		boolean requiresNewVersion = latestVersion.getVersionNumber() > CURRENT_VERSION.getVersionNumber();

		if (requiresNewVersion) {
			ConfirmDialogListener listener = new ConfirmDialogListener() {
				/**
				 * The serial version id.
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void onDialogPositiveClick() {
					// TODO download
				}

				@Override
				public void onDialogNegativeClick() {
					// do nothing
				}
			};

			DialogUtil.displayConfirmationMessage(listener, ResourceConstants.BUTTON_DOWNLOAD,
					ResourceConstants.MESSAGE_DIALOG_NEW_VERSION, latestVersion.getVersionString());
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

		/**
		 * Retrieve the download URL for this version of the application.
		 *
		 * @return The download URL.
		 */
		public String getDownloadUrl() {
			StringBuffer result = new StringBuffer(DOWNLOAD_BASE_URL);
			result.append("Augendiagnose-");
			if (is64Bit()) {
				result.append("x64");
			}
			else {
				result.append("x86");
			}
			result.append("-").append(getVersionString()).append(".exe");
			return result.toString();
		}
	}

}
