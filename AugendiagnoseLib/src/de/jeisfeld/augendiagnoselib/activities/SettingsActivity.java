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

/**
 * Activity to display the settings page.
 */
public class SettingsActivity extends BaseActivity {
	/**
	 * The fragment tag.
	 */
	private static final String FRAGMENT_TAG = "FRAGMENT_TAG";

	/**
	 * The resource key for the flag indicating if only packs should be displayed.
	 */
	private static final String STRING_EXTRA_ONLY_PACKS = "de.jeisfeld.augendiagnoselib.ONLY_PACKS";

	/**
	 * The path resources for which external storage prefix should be replaced.
	 */
	private static final int[] PATH_RESOURCES = { R.string.key_folder_input, R.string.key_folder_photos };

	/**
	 * Utility method to start the activity.
	 *
	 * @param context
	 *            The context in which the activity is started.
	 * @param onlyPacks
	 *            the flag indicating if only packs should be displayed.
	 */
	public static final void startActivity(final Context context, final boolean onlyPacks) {
		Intent intent = new Intent(context, SettingsActivity.class);
		intent.putExtra(STRING_EXTRA_ONLY_PACKS, onlyPacks);
		context.startActivity(intent);
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		boolean onlyPacks = getIntent().getBooleanExtra(STRING_EXTRA_ONLY_PACKS, false);

		// Display the SettingsFragment as the main content.

		SettingsFragment fragment = (SettingsFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
		if (fragment == null) {
			fragment = new SettingsFragment();
			fragment.setParameters(onlyPacks);

			getFragmentManager().beginTransaction().replace(android.R.id.content, fragment, FRAGMENT_TAG).commit();
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
				context.getString(R.string.pref_dummy_folder_input))) {
			// On first startup, make default setting dependent on status of Eye-Fi.
			if (SystemUtil.isAppInstalled("fi.eye.android")) {
				// If Eye-Fi is available, use Eye-Fi default folder
				PreferenceUtil.setSharedPreferenceString(R.string.key_folder_input,
						context.getString(R.string.pref_default_folder_input_eyefi));
			}
			else {
				// Otherwise, use normal default folder.
				PreferenceUtil.setSharedPreferenceString(R.string.key_folder_input,
						context.getString(R.string.pref_default_folder_input));
			}
		}

		for (int id : PATH_RESOURCES) {
			String path = PreferenceUtil.getSharedPreferenceString(id);

			String mappedPath = DirectorySelectionPreference.replaceSpecialFolderTags(path);

			if (!path.equals(mappedPath)) {
				PreferenceUtil.setSharedPreferenceString(id, mappedPath);
			}
		}

		// Setting for full resolution setting and for max bitmap size - dependent on available memory.
		PreferenceUtil.setDefaultResolutionSettings();

		// Setting for camera API - dependent on OS version.
		PreferenceUtil.setDefaultCameraSettings();

		boolean showTips = Boolean.parseBoolean(context.getString(R.string.pref_default_show_tips));
		PreferenceUtil.setAllHints(!showTips);

		// Inform PinchImageView about maxBitmapSize
		PinchImageView.setMaxBitmapSize(PreferenceUtil.getSharedPreferenceIntString(R.string.key_max_bitmap_size, 0));
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
