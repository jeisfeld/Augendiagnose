package de.eisfeldj.augendiagnosefx.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import de.eisfeldj.augendiagnosefx.Application;
import de.eisfeldj.augendiagnosefx.util.DialogUtil.ConfirmDialogListener;

import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_LAST_KNOWN_VERSION;

/**
 * Utility class for handling the download of new application version.
 */
public final class VersioningUtil {
	/**
	 * The current version of the application.
	 */
	public static final VersionInfo CURRENT_VERSION = new VersionInfo(17, "0.1.16");

	/**
	 * Base URL for downloading the application.
	 */
	private static final String DOWNLOAD_BASE_URL = "https://augendiagnose-app.de/bin/";

	/**
	 * The URL where to get the info about the latest version.
	 */
	private static final String CURRENT_VERSION_URL = DOWNLOAD_BASE_URL + "currentVersion.txt";

	/**
	 * Hide default constructor.
	 */
	private VersioningUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Retrieve information about the latest available application version (from Internet).
	 *
	 * @return The latest version info.
	 */
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
			Logger.error("Error while determining latest application version.", e);
			return null;
		}
	}

	/**
	 * Download a version update.
	 *
	 * @param version
	 *            The version to be downloaded.
	 */
	public static void downloadUpdate(final VersionInfo version) {
		Application.openWebPage("https://augendiagnose-app.de/downloads/");
	}

	/**
	 * Check if there is a newer version of the application.
	 *
	 * @param fromMenu
	 *            give information if started from menu. If started from menu, than failure to find new version will be
	 *            reported, and previous cancellation is ignored.
	 */
	public static void checkForNewerVersion(final boolean fromMenu) {
		VersionInfo latestVersion = getLatestVersionInfo();

		if (latestVersion == null) {
			return;
		}

		// Prompt only if the same version has not yet been refused.
		boolean requiresNewVersion =
				latestVersion.getVersionNumber() > CURRENT_VERSION.getVersionNumber();


		// Otherwise, differentiate between check on startup and check via menu.
		if (fromMenu) {
			PreferenceUtil.setPreference(PreferenceUtil.KEY_LAST_KNOWN_VERSION,
					VersioningUtil.CURRENT_VERSION.getVersionNumber());
		}
		else {
			requiresNewVersion = requiresNewVersion
					&& latestVersion.getVersionNumber() > PreferenceUtil.getPreferenceInt(KEY_LAST_KNOWN_VERSION);
		}

		if (requiresNewVersion) {
			ConfirmDialogListener listener = new ConfirmDialogListener() {
				@Override
				public void onDialogPositiveClick() {
					downloadUpdate(latestVersion);
				}

				@Override
				public void onDialogNegativeClick() {
					PreferenceUtil.setPreference(KEY_LAST_KNOWN_VERSION, latestVersion.mVersionNumber);
				}
			};

			DialogUtil.displayConfirmationMessage(listener, ResourceConstants.BUTTON_DOWNLOAD,
					ResourceConstants.MESSAGE_CONFIRM_NEW_VERSION, latestVersion.getVersionString());
		}
		else {
			if (fromMenu) {
				DialogUtil.displayInfo(ResourceConstants.MESSAGE_INFO_NO_NEW_VERSION);
			}
		}
	}

	/**
	 * A class holding information about the version of the application.
	 */
	public static final class VersionInfo {
		/**
		 * The version number (counting upwards from 1).
		 */
		private int mVersionNumber;

		/**
		 * Get the version number.
		 *
		 * @return The version number.
		 */
		public int getVersionNumber() {
			return mVersionNumber;
		}

		/**
		 * The version String (such as 1.2.3).
		 */
		private String mVersionString;

		/**
		 * Get the version string.
		 *
		 * @return The version string.
		 */
		public String getVersionString() {
			return mVersionString;
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
			this.mVersionNumber = versionNumber;
			this.mVersionString = versionString;
		}

		@Override
		public String toString() {
			return mVersionString + " (" + mVersionNumber + ")";
		}

	}

}
