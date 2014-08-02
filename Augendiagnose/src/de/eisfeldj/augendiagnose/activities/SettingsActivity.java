package de.eisfeldj.augendiagnose.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.components.PinchImageView;
import de.eisfeldj.augendiagnose.fragments.SettingsFragment;
import de.eisfeldj.augendiagnose.util.FileUtil;

/**
 * Activity to display the settings page
 */
public class SettingsActivity extends Activity {
	private static String EXTERNAL_STORAGE_PREFIX = "__ext_storage__";
	private static int[] PATH_RESOURCES = { R.string.key_folder_input, R.string.key_folder_photos };

	public static void startActivity(Context context) {
		Intent intent = new Intent(context, SettingsActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Display the listFoldersFragment as the main content.
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
	}

	/**
	 * Inflate options menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_only_help, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Handle menu actions
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_help:
			DisplayHtmlActivity.startActivity(this, R.string.html_settings);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Set the default shared preferences (after first installation) Regarding paths, choose external folder as base
	 * folder. For Bluestacks, chose bluestacks shared folder as base folder.
	 */
	@SuppressLint("SdCardPath")
	public static void setDefaultSharedPreferences(Context context) {
		PreferenceManager.setDefaultValues(Application.getAppContext(), R.xml.pref_general, false);

		if (Application.getSharedPreferenceString(R.string.key_folder_input).equals(
				context.getString(R.string.pref_dummy_value))) {
			// On first startup, make default setting dependent on status of Eye-Fi.
			if (Application.isEyeFiInstalled()) {
				// If Eye-Fi is available, use Eye-Fi folder, which is the first selection
				Application.setSharedPreferenceString(R.string.key_folder_input,
						context.getString(R.string.pref_default_folder_input));
			}
			else {
				// Otherwise, use Camera foldder.
				Application.setSharedPreferenceString(R.string.key_folder_input, FileUtil.getDefaultCameraFolder());
			}
		}

		for (int id : PATH_RESOURCES) {
			String path = Application.getSharedPreferenceString(id);
			if (path.startsWith(EXTERNAL_STORAGE_PREFIX)) {
				path = Environment.getExternalStorageDirectory().getAbsolutePath()
						+ path.substring(EXTERNAL_STORAGE_PREFIX.length());
				Application.setSharedPreferenceString(id, path);
			}
		}

		// Delta setting for storeOption - required after upgrade to version 0.3
		String storeOption = Application.getSharedPreferenceString(R.string.key_store_option);
		if (storeOption == null || storeOption.length() == 0) {
			Application.setSharedPreferenceString(R.string.key_store_option,
					Application.getAppContext().getString(R.string.pref_default_store_options));
		}

		// Inform PinchImageView about maxBitmapSize
		pushMaxBitmapSize(Application.getSharedPreferenceString(R.string.key_max_bitmap_size));
	}

	/**
	 * Validate the maxBitmapSize. If not numeric, replace with default. In any case, inform PinchImageView about it.
	 *
	 * @param value
	 *            the String value to be set
	 * @return the maxBitmapSize
	 */
	public static int pushMaxBitmapSize(String value) {
		int maxBitmapSize;
		try {
			maxBitmapSize = Integer.parseInt(value);
			if (maxBitmapSize < 512) {
				maxBitmapSize = 512;
			}
			if (maxBitmapSize > 4096) {
				maxBitmapSize = 4096;
			}
		}
		catch (NumberFormatException e) {
			// Override preference with default value
			String defaultMaxBitmapSize = Application.getAppContext().getString(R.string.pref_default_max_bitmap_size);
			Application.setSharedPreferenceString(R.string.key_max_bitmap_size, defaultMaxBitmapSize);
			maxBitmapSize = Integer.parseInt(defaultMaxBitmapSize);
		}
		PinchImageView.setMaxBitmapSize(maxBitmapSize);
		return maxBitmapSize;
	}

}
