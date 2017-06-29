package de.jeisfeld.augendiagnoselib.util;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.components.OverlayPinchImageView;
import de.jeisfeld.augendiagnoselib.fragments.DisplayImageFragment;
import de.jeisfeld.augendiagnoselib.util.TrackingUtil.Category;

/**
 * Utility class for handling the shared preferences.
 */
public final class PreferenceUtil {
	/**
	 * The list of preferences used for switching on and off hints.
	 */
	private static final Integer[] HINT_PREFERENCES = {R.string.key_tip_admarvel,
			R.string.key_tip_clarity,
			R.string.key_tip_displaydetails,
			R.string.key_tip_displaynames,
			R.string.key_tip_displaypictures,
			R.string.key_tip_editcomment,
			R.string.key_tip_firstuse,
			R.string.key_tip_jpeg,
			R.string.key_tip_organizephotos,
			R.string.key_tip_overlay_buttons,
			R.string.key_tip_overlay_guided,
			R.string.key_tip_saveview,
			R.string.key_tip_external_flash,
			R.string.key_tip_external_flash_pref
	};

	/**
	 * Hide default constructor.
	 */
	private PreferenceUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Retrieve the default shared preferences of the application.
	 *
	 * @return the default shared preferences.
	 */
	private static SharedPreferences getSharedPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(Application.getAppContext());
	}

	/**
	 * Retrieve a String shared preference.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @return the corresponding preference value.
	 */
	@Nullable
	public static String getSharedPreferenceString(final int preferenceId) {
		return getSharedPreferences().getString(Application.getAppContext().getString(preferenceId), null);
	}

	/**
	 * Retrieve a String shared preference, setting a default value if the preference is not set.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @param defaultId    the String key of the default value.
	 * @return the corresponding preference value.
	 */
	@Nullable
	private static String getSharedPreferenceString(final int preferenceId, final int defaultId) {
		String result = getSharedPreferenceString(preferenceId);
		if (result == null) {
			result = Application.getAppContext().getString(defaultId);
			setSharedPreferenceString(preferenceId, result);
		}
		return result;
	}

	/**
	 * Set a String shared preference.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @param s            the target value of the preference.
	 */
	public static void setSharedPreferenceString(final int preferenceId, final String s) {
		Editor editor = getSharedPreferences().edit();
		editor.putString(Application.getAppContext().getString(preferenceId), s);
		editor.apply();
	}

	/**
	 * Retrieve an Uri shared preference.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @return the corresponding preference value.
	 */
	public static Uri getSharedPreferenceUri(final int preferenceId) {
		String uriString = getSharedPreferences().getString(Application.getAppContext().getString(preferenceId), null);

		if (uriString == null) {
			return null;
		}
		else {
			return Uri.parse(uriString);
		}
	}

	/**
	 * Get the stored tree URIs.
	 *
	 * @return The tree URIs.
	 */
	public static Uri[] getTreeUris() {
		List<Uri> uris = new ArrayList<>();

		Uri uri1 = getSharedPreferenceUri(R.string.key_internal_uri_extsdcard_photos);
		if (uri1 != null) {
			uris.add(uri1);
		}

		Uri uri2 = getSharedPreferenceUri(R.string.key_internal_uri_extsdcard_input);
		if (uri2 != null) {
			uris.add(uri2);
		}
		return uris.toArray(new Uri[uris.size()]);
	}

	/**
	 * Set a shared preference for an Uri.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @param uri          the target value of the preference.
	 */
	public static void setSharedPreferenceUri(final int preferenceId, @Nullable final Uri uri) {
		Editor editor = getSharedPreferences().edit();
		if (uri == null) {
			editor.putString(Application.getAppContext().getString(preferenceId), null);
		}
		else {
			editor.putString(Application.getAppContext().getString(preferenceId), uri.toString());
		}
		editor.apply();
	}

	/**
	 * Retrieve a boolean shared preference.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @return the corresponding preference value.
	 */
	public static boolean getSharedPreferenceBoolean(final int preferenceId) {
		return getSharedPreferences().getBoolean(Application.getAppContext().getString(preferenceId), false);
	}

	/**
	 * Set a String shared preference from a boolean.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @param b            the target value of the preference.
	 */
	public static void setSharedPreferenceBoolean(final int preferenceId, final boolean b) {
		Editor editor = getSharedPreferences().edit();
		editor.putBoolean(Application.getAppContext().getString(preferenceId), b);
		editor.apply();
	}

	/**
	 * Retrieve an integer shared preference.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @param defaultValue the default value of the shared preference.
	 * @return the corresponding preference value.
	 */
	public static int getSharedPreferenceInt(final int preferenceId, final int defaultValue) {
		return getSharedPreferences().getInt(Application.getAppContext().getString(preferenceId), defaultValue);
	}

	/**
	 * Set an integer shared preference.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @param i            the target value of the preference.
	 */
	public static void setSharedPreferenceInt(final int preferenceId, final int i) {
		Editor editor = getSharedPreferences().edit();
		editor.putInt(Application.getAppContext().getString(preferenceId), i);
		editor.apply();
	}

	/**
	 * Increment a counter shared preference, and return the new value.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @return the new value.
	 */
	public static int incrementCounter(final int preferenceId) {
		int newValue = getSharedPreferenceInt(preferenceId, 0) + 1;
		setSharedPreferenceInt(preferenceId, newValue);
		return newValue;
	}

	/**
	 * Retrieve an integer from a shared preference string.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @param defaultId    the String key of the default value. If not existing, value 0 is returned.
	 * @return the corresponding preference value.
	 */
	public static int getSharedPreferenceIntString(final int preferenceId, @Nullable final Integer defaultId) {
		String resultString;

		if (defaultId == null) {
			resultString = getSharedPreferenceString(preferenceId);
		}
		else {
			resultString = getSharedPreferenceString(preferenceId, defaultId);
		}
		if (resultString == null || resultString.length() == 0) {
			return 0;
		}
		try {
			return Integer.parseInt(resultString);
		}
		catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * Set a string shared preference from an integer.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @param i            the target value of the preference.
	 */
	public static void setSharedPreferenceIntString(final int preferenceId, final int i) {
		setSharedPreferenceString(preferenceId, Integer.toString(i));
	}

	/**
	 * Retrieve a long shared preference.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @param defaultValue the default value of the shared preference.
	 * @return the corresponding preference value.
	 */
	public static long getSharedPreferenceLong(final int preferenceId, final long defaultValue) {
		return getSharedPreferences().getLong(Application.getAppContext().getString(preferenceId), defaultValue);
	}

	/**
	 * Set a long shared preference.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @param i            the target value of the preference.
	 */
	public static void setSharedPreferenceLong(final int preferenceId, final long i) {
		Editor editor = getSharedPreferences().edit();
		editor.putLong(Application.getAppContext().getString(preferenceId), i);
		editor.apply();
	}

	/**
	 * Remove a shared preference.
	 *
	 * @param preferenceId the id of the shared preference.
	 */
	public static void removeSharedPreference(final int preferenceId) {
		Editor editor = getSharedPreferences().edit();
		editor.remove(Application.getAppContext().getString(preferenceId));
		editor.apply();
	}

	/**
	 * Get an indexed preference key that allows to store a shared preference with index.
	 *
	 * @param preferenceId The base preference id
	 * @param index        The index
	 * @return The indexed preference key.
	 */
	@NonNull
	public static String getIndexedPreferenceKey(final int preferenceId, final int index) {
		return Application.getAppContext().getString(preferenceId) + "[" + index + "]";
	}

	/**
	 * Get the index from an indexed preference key.
	 *
	 * @param key The preference key.
	 * @return The index.
	 */
	public static Integer getIndexFromPreferenceKey(@NonNull final String key) {
		String[] parts = key.split("\\[|\\]");
		if (parts.length < 2) {
			return null;
		}
		try {
			return Integer.parseInt(parts[1]);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Retrieve an indexed String shared preference.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @param index        The index
	 * @return the corresponding preference value.
	 */
	@Nullable
	private static String getIndexedSharedPreferenceString(final int preferenceId, final int index) {
		return getSharedPreferences().getString(getIndexedPreferenceKey(preferenceId, index), null);
	}

	/**
	 * Set an indexed String shared preference.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @param index        The index
	 * @param s            the target value of the preference.
	 */
	public static void setIndexedSharedPreferenceString(final int preferenceId, final int index, final String s) {
		Editor editor = getSharedPreferences().edit();
		editor.putString(getIndexedPreferenceKey(preferenceId, index), s);
		editor.apply();
	}

	/**
	 * Retrieve an indexed int shared preference.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @param index        The index
	 * @param defaultValue the default value of the shared preference.
	 * @return the corresponding preference value.
	 */
	public static int getIndexedSharedPreferenceInt(final int preferenceId, final int index, final int defaultValue) {
		return getSharedPreferences().getInt(getIndexedPreferenceKey(preferenceId, index), defaultValue);
	}

	/**
	 * Set an indexed int shared preference.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @param index        The index
	 * @param i            the target value of the preference.
	 */
	public static void setIndexedSharedPreferenceInt(final int preferenceId, final int index, final int i) {
		Editor editor = getSharedPreferences().edit();
		editor.putInt(getIndexedPreferenceKey(preferenceId, index), i);
		editor.apply();
	}

	/**
	 * Retrieve an indexed String shared preference as integer.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @param index        The index
	 * @param defaultValue the default value of the shared preference.
	 * @return the corresponding preference value.
	 */
	public static int getIndexedSharedPreferenceIntString(final int preferenceId, final int index, final int defaultValue) {
		String resultString = getIndexedSharedPreferenceString(preferenceId, index);

		if (resultString == null || resultString.length() == 0) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(resultString);
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * Set an indexed String shared preference from an integer.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @param index        The index
	 * @param i            the target value of the preference.
	 */
	public static void setIndexedSharedPreferenceIntString(final int preferenceId, final int index, final int i) {
		setIndexedSharedPreferenceString(preferenceId, index, Integer.toString(i));
	}

	/**
	 * Remove an indexed shared preference.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @param index        The index
	 */
	public static void removeIndexedSharedPreference(final int preferenceId, final int index) {
		Editor editor = getSharedPreferences().edit();
		editor.remove(getIndexedPreferenceKey(preferenceId, index));
		editor.apply();
	}

	/**
	 * Set all hint preferences to the given value.
	 *
	 * @param value The value.
	 */
	public static void setAllHints(final boolean value) {
		for (int preferenceId : Arrays.asList(HINT_PREFERENCES)) {
			setSharedPreferenceBoolean(preferenceId, value);
		}
	}

	/**
	 * Set the default setting for handling of full resolution, if no value is set.
	 */
	public static void setDefaultResolutionSettings() {
		// Max bitmap size
		String maxBitmapSize = getSharedPreferenceString(R.string.key_max_bitmap_size);

		if (maxBitmapSize == null || maxBitmapSize.length() == 0) {
			// Only if 512 MB are accessible, then full resolution image should be stored.
			int memoryClass = SystemUtil.getLargeMemoryClass();
			if (memoryClass >= 256) { // MAGIC_NUMBER
				maxBitmapSize = "2048";
			}
			else {
				maxBitmapSize = "1024";
			}

			setSharedPreferenceString(R.string.key_max_bitmap_size, maxBitmapSize);
		}

		// Full resolution setting
		String fullResolutionSetting = getSharedPreferenceString(R.string.key_full_resolution);

		if (fullResolutionSetting == null || fullResolutionSetting.length() == 0) {
			// Only if 512 MB are accessible, then full resolution image should be stored.
			int memoryClass = SystemUtil.getLargeMemoryClass();
			if (memoryClass >= 512) { // MAGIC_NUMBER
				fullResolutionSetting = "2";
			}
			else if (memoryClass >= 256) { // MAGIC_NUMBER
				fullResolutionSetting = "1";
			}
			else {
				fullResolutionSetting = "0";
			}

			setSharedPreferenceString(R.string.key_full_resolution, fullResolutionSetting);
		}

		// Automatic iris detection setting
		if (!PreferenceUtil.getSharedPreferenceBoolean(R.string.key_internal_iris_detection_is_set)) {
			int memoryClass = SystemUtil.getLargeMemoryClass();
			if (memoryClass >= 256) { // MAGIC_NUMBER
				PreferenceUtil.setSharedPreferenceBoolean(R.string.key_automatic_iris_detection, true);
			}
			else {
				PreferenceUtil.setSharedPreferenceBoolean(R.string.key_automatic_iris_detection, false);
			}
			PreferenceUtil.setSharedPreferenceBoolean(R.string.key_internal_iris_detection_is_set, true);
		}
	}

	/**
	 * Set the default camera settings.
	 */
	public static void setDefaultCameraSettings() {
		String cameraApiVersion = getSharedPreferenceString(R.string.key_camera_api_version);

		if (cameraApiVersion == null || cameraApiVersion.length() == 0) {
			cameraApiVersion = SystemUtil.isAndroid5() ? "2" : "1";

			setSharedPreferenceString(R.string.key_camera_api_version, cameraApiVersion);
		}
	}

	/**
	 * Set the default overlay button settings.
	 */
	public static void setDefaultOverlayButtonSettings() {
		// only set values if never set before.
		int dummyNumber = -2; // MAGIC_NUMBER
		int overlayNumber = getIndexedSharedPreferenceIntString(R.string.key_indexed_overlaytype, 0, dummyNumber);

		if (overlayNumber == dummyNumber) {
			int numberOfOverlays =
					Math.min(Math.max((int) Math.floor(SystemUtil.getPhysicalDisplaySize() * 2.5 - 3), 2), // MAGIC_NUMBER
							OverlayPinchImageView.OVERLAY_COUNT);

			for (int i = 0; i < DisplayImageFragment.OVERLAY_BUTTON_COUNT; i++) {
				// limit default number of buttons based on screen size.
				if (i < numberOfOverlays) {
					// By default, each overlay button is mapped to the overlay of the same index.
					setIndexedSharedPreferenceIntString(R.string.key_indexed_overlaytype, i, i);
				}
				else {
					setIndexedSharedPreferenceIntString(R.string.key_indexed_overlaytype, i, -1);
				}
			}
		}
		else {
			// fill missing entries if overlays/buttons have been added.
			for (int i = 0; i < DisplayImageFragment.OVERLAY_BUTTON_COUNT; i++) {
				int value = getIndexedSharedPreferenceIntString(R.string.key_indexed_overlaytype, i, dummyNumber);
				if (value == dummyNumber) {
					setIndexedSharedPreferenceIntString(R.string.key_indexed_overlaytype, i, -1);
				}
			}
		}
	}

	/**
	 * Send a statistics values to Google Analytics.
	 *
	 * @param label        The label.
	 * @param preferenceId The preference id.
	 */
	private static void sendStatistics(final String label, final int preferenceId) {
		TrackingUtil.sendEvent(Category.COUNTER_STATISTICS, "Statistics", label, (long) getSharedPreferenceInt(preferenceId, 0));

	}

	/**
	 * Send all statistics values to Google Analytics.
	 */
	public static void sendStatistics() {
		sendStatistics("App starts", R.string.key_statistics_countstarts);
		sendStatistics("Edit comment", R.string.key_statistics_countcomment);
		sendStatistics("Display images", R.string.key_statistics_countdisplay);
		sendStatistics("Display help", R.string.key_statistics_counthelp);
		sendStatistics("Iris detection failed", R.string.key_statistics_countirisdetectionfailed);
		sendStatistics("Iris detection success", R.string.key_statistics_countirisdetectionsuccess);
		sendStatistics("List Names", R.string.key_statistics_countlistnames);
		sendStatistics("List Pictures", R.string.key_statistics_countlistpictures);
		sendStatistics("Lock iris position", R.string.key_statistics_countlock);
		sendStatistics("Organize end", R.string.key_statistics_countorganizeend);
		sendStatistics("Organize start", R.string.key_statistics_countorganizestart);
		sendStatistics("Save image", R.string.key_statistics_countsave);
		sendStatistics("Open settings", R.string.key_statistics_countsettings);
	}
}
