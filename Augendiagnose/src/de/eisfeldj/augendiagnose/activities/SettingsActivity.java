package de.eisfeldj.augendiagnose.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.components.PinchImageView;
import de.eisfeldj.augendiagnose.fragments.SettingsFragment;
import de.eisfeldj.augendiagnose.util.PreferenceUtil;
import de.eisfeldj.augendiagnose.util.SystemUtil;
import de.eisfeldj.augendiagnose.util.imagefile.FileUtil;

/**
 * Activity to display the settings page.
 */
public class SettingsActivity extends BaseActivity {
	/**
	 * Tag to be replaced by the external storage root.
	 */
	private static final String EXTERNAL_STORAGE_PREFIX = "__ext_storage__";

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

		// Display the listFoldersFragment as the main content.
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
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

		// TODO: remove duplications from DirectorySelectionPreference
		for (int id : PATH_RESOURCES) {
			String path = PreferenceUtil.getSharedPreferenceString(id);
			if (path.startsWith(EXTERNAL_STORAGE_PREFIX)) {
				path = Environment.getExternalStorageDirectory().getAbsolutePath()
						+ path.substring(EXTERNAL_STORAGE_PREFIX.length());
				PreferenceUtil.setSharedPreferenceString(id, path);
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

}
