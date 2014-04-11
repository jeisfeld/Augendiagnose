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
import de.eisfeldj.augendiagnose.util.SystemUtil;

/**
 * Activity to display the settings page
 */
public class SettingsActivity extends Activity {
	private static String EXTERNAL_STORAGE_PREFIX = "__ext_storage__";
	private static int[] PATH_RESOURCES = { R.string.key_folder_input, R.string.key_folder_photos,
			R.string.key_folder_photos_remote };

	public static void startActivity(Context context) {
		Intent intent = new Intent(context, SettingsActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Display the fragment as the main content.
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
		}
		return super.onOptionsItemSelected(item);
	}


	/**
	 * Set the default shared preferences (after first installation) Regarding paths, choose external folder as base
	 * folder. For Bluestacks, chose bluestacks shared folder as base folder.
	 */
	@SuppressLint("SdCardPath")
	public static void setDefaultSharedPreferences() {
		PreferenceManager.setDefaultValues(Application.getAppContext(), R.xml.pref_general, false);
		if (SystemUtil.isBlueStacks()) {
			for (int id : PATH_RESOURCES) {
				String path = Application.getSharedPreferenceString(id);
				if (path.startsWith(EXTERNAL_STORAGE_PREFIX)) {
					path = "/sdcard/bstfolder/BstSharedFolder" + path.substring(EXTERNAL_STORAGE_PREFIX.length());
					Application.setSharedPreferenceString(id, path);
				}
			}
		}
		else {
			for (int id : PATH_RESOURCES) {
				String path = Application.getSharedPreferenceString(id);
				if (path.startsWith(EXTERNAL_STORAGE_PREFIX)) {
					path = Environment.getExternalStorageDirectory().getAbsolutePath()
							+ path.substring(EXTERNAL_STORAGE_PREFIX.length());
					Application.setSharedPreferenceString(id, path);
				}
			}
		}

		// Delta setting for storeOption - required after upgrade to version 0.3
		String storeOption = Application.getSharedPreferenceString(R.string.key_store_option);
		if (storeOption == null || storeOption.length() == 0) {
			Application.setSharedPreferenceString(R.string.key_store_option,
					Application.getAppContext().getString(R.string.pref_default_store_options));
		}

		// Inform PinchImageView about maxBitmapSize
		PinchImageView.setMaxBitmapSize(Integer.parseInt(Application
				.getSharedPreferenceString(R.string.key_max_bitmap_size)));
	}

}
