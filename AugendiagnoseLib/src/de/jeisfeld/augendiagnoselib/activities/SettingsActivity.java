package de.jeisfeld.augendiagnoselib.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.components.DirectorySelectionPreference;
import de.jeisfeld.augendiagnoselib.components.PinchImageView;
import de.jeisfeld.augendiagnoselib.fragments.SettingsFragment;
import de.jeisfeld.augendiagnoselib.util.GoogleBillingHelper;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.SystemUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.FileUtil;

/**
 * Activity to display the settings page.
 */
public class SettingsActivity extends BaseActivity {
	/**
	 * The fragment tag.
	 */
	private static final String FRAGMENT_TAG = "FRAGMENT_TAG";

	/**
	 * The path resources for which external storag prefix should be replaced.
	 */
	private static final int[] PATH_RESOURCES = { R.string.key_folder_input, R.string.key_folder_photos };

	/**
	 * Utility method to start the activity.
	 *
	 * @param context
	 *            The context in which the activity is started.
	 */
	public static final void startActivity(final Context context) {
		Intent intent = new Intent(context, SettingsActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Display the SettingsFragment as the main content.

		SettingsFragment fragment = (SettingsFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
		if (fragment == null) {
			getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment(), FRAGMENT_TAG)
					.commit();
			getFragmentManager().executePendingTransactions();

			if (savedInstanceState == null) {
				PreferenceUtil.incrementCounter(R.string.key_statistics_countsettings);
			}
		}

	}

	@Override
	protected final int getHelpResource() {
		return R.string.html_settings;
	}

	/**
	 * Set the default shared preferences (after first installation) Regarding paths, choose external folder as base
	 * folder. For Bluestacks, chose bluestacks shared folder as base folder.
	 *
	 * @param context
	 *            The Context in which the preferences should be set.
	 */
	@SuppressLint("SdCardPath")
	public static final void setDefaultSharedPreferences(final Context context) {
		PreferenceManager.setDefaultValues(Application.getAppContext(), R.xml.pref_general, false);

		if (PreferenceUtil.getSharedPreferenceString(R.string.key_folder_input).equals(
				context.getString(R.string.pref_dummy_value))) {
			// On first startup, make default setting dependent on status of Eye-Fi.
			if (SystemUtil.isEyeFiInstalled()) {
				// If Eye-Fi is available, use Eye-Fi folder, which is the first selection
				PreferenceUtil.setSharedPreferenceString(R.string.key_folder_input,
						context.getString(R.string.pref_default_folder_input));
			}
			else {
				// Otherwise, use Camera foldder.
				PreferenceUtil.setSharedPreferenceString(R.string.key_folder_input, FileUtil.getDefaultCameraFolder());
			}
		}

		for (int id : PATH_RESOURCES) {
			String path = PreferenceUtil.getSharedPreferenceString(id);

			String mappedPath = DirectorySelectionPreference.replaceSpecialFolderTags(path);

			if (!path.equals(mappedPath)) {
				PreferenceUtil.setSharedPreferenceString(id, mappedPath);
			}
		}

		// Delta setting for storeOption - required after upgrade to version 0.3
		String storeOption = PreferenceUtil.getSharedPreferenceString(R.string.key_store_option);
		if (storeOption == null || storeOption.length() == 0) {
			PreferenceUtil.setSharedPreferenceString(R.string.key_store_option,
					Application.getAppContext().getString(R.string.pref_default_store_options));
		}

		// Delta setting for full resolution setting and for max bitmap size - dependent on available memory.
		PreferenceUtil.setDefaultResolutionSettings();

		// Inform PinchImageView about maxBitmapSize
		pushMaxBitmapSize(PreferenceUtil.getSharedPreferenceString(R.string.key_max_bitmap_size));
	}

	/**
	 * Validate the maxBitmapSize. If not numeric, replace with default. In any case, inform PinchImageView about it.
	 *
	 * @param value
	 *            the String value to be set
	 * @return the maxBitmapSize
	 */
	public static int pushMaxBitmapSize(final String value) {
		int maxBitmapSize = Integer.parseInt(value);
		PinchImageView.setMaxBitmapSize(maxBitmapSize);
		return maxBitmapSize;
	}

	@Override
	public final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		GoogleBillingHelper.handleActivityResult(requestCode, resultCode, data);
	}

	@Override
	public final void onDestroy() {
		super.onDestroy();
		GoogleBillingHelper.dispose();
	}
}
