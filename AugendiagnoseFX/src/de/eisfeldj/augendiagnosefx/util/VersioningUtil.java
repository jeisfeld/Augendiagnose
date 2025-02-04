package de.eisfeldj.augendiagnosefx.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import de.eisfeldj.augendiagnosefx.Application;
import de.eisfeldj.augendiagnosefx.util.DialogUtil.ConfirmDialogListener;
import de.eisfeldj.augendiagnosefx.util.DialogUtil.ProgressDialog;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javafx.application.Platform;

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
	 * Size of the buffer for downloading the jar file.
	 */
	private static final int DOWNLOAD_BUFFER = 4096;

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
		String url = version.getJarDownloadUrl();
		final File currentJarFile = SystemUtil.getJarPath();
		final File tempJarFile;
		final ProgressDialog dialog =
				DialogUtil
						.displayProgressDialog(ResourceConstants.MESSAGE_PROGRESS_LOADING_UPDATE, version.mVersionString);
		final URLConnection connection;
		try {
			connection = new URL(url).openConnection();
			tempJarFile = new File(SystemUtil.getTempDir(), "temp.jar");
		}
		catch (IOException e) {
			Logger.error("Could not open URL " + url, e);
			return;
		}

		Thread downloadThread = new Thread() {
			@Override
			public void run() {
				Logger.info("Downloading update to file " + tempJarFile.getAbsolutePath());
				try (InputStream input = connection.getInputStream();
						OutputStream output = new FileOutputStream(tempJarFile)) {
					long totalSize = connection.getContentLengthLong();
					long currentSize = 0;

					byte[] buffer = new byte[DOWNLOAD_BUFFER];
					int n = -1;

					while ((n = input.read(buffer)) != -1) {
						if (n > 0) {
							output.write(buffer, 0, n);
							currentSize += n;
							dialog.setProgress(1.0 * currentSize / totalSize);
						}
					}

					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							dialog.close();
							SystemUtil.updateApplication(tempJarFile.getAbsolutePath(),
									currentJarFile.getAbsolutePath());

							Application.exitAfterConfirmation();
						}
					});

				}
				catch (IOException e) {
					Logger.error("Exception while downloading from " + url, e);
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							dialog.close();
						}
					});
				}
			}
		};

		downloadThread.start();
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
		boolean updateAutomatically = PreferenceUtil.getPreferenceBoolean(PreferenceUtil.KEY_UPDATE_AUTOMATICALLY);

		if (latestVersion == null) {
			return;
		}

		// Prompt only if the same version has not yet been refused.
		boolean requiresNewVersion =
				latestVersion.getVersionNumber() > CURRENT_VERSION.getVersionNumber();

		// In case of automatic updates, just do the update if existing.
		if (requiresNewVersion && updateAutomatically) {
			downloadUpdate(latestVersion);
			return;
		}

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

		/**
		 * Retrieve the download URL for this version of the application (exe file).
		 *
		 * @return The download URL.
		 */
		public String getExeDownloadUrl() {
			StringBuffer result = new StringBuffer(DOWNLOAD_BASE_URL);
			result.append("Augendiagnose-");
			if (SystemUtil.is64Bit()) {
				result.append("x64");
			}
			else {
				result.append("x86");
			}
			result.append("-").append(getVersionString()).append(".exe");
			return result.toString();
		}

		/**
		 * Retrieve the download URL for this version of the application (jar file).
		 *
		 * @return The download URL.
		 */
		public String getJarDownloadUrl() {
			StringBuffer result = new StringBuffer(DOWNLOAD_BASE_URL);
			result.append("AugendiagnoseFX-");
			result.append(getVersionString()).append(".jar");
			return result.toString();
		}
	}

}
