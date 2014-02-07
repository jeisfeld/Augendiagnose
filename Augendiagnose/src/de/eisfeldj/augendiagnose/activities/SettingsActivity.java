package de.eisfeldj.augendiagnose.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.components.PinchImageView;
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
		getMenuInflater().inflate(R.menu.only_help, menu);
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
	 * A preference value change listener that updates the preference's summary to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

			}
			else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);

				// Inform PinchImageView about maxBitmapSize
				if (preference.getKey().equals(preference.getContext().getString(R.string.key_max_bitmap_size))) {
					PinchImageView.setMaxBitmapSize(Integer.parseInt(preference.getSummary().toString()));
				}
			}
			return true;
		}
	};

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

		// Inform PinchImageView about maxBitmapSize
		PinchImageView.setMaxBitmapSize(Integer.parseInt(Application
				.getSharedPreferenceString(R.string.key_max_bitmap_size)));
	}

	/**
	 * Fragment for displaying the settings.
	 */
	public static class SettingsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.pref_general);
			bindPreferenceSummaryToValue(R.string.key_folder_input);
			bindPreferenceSummaryToValue(R.string.key_folder_photos);
			bindPreferenceSummaryToValue(R.string.key_max_bitmap_size);
		}

		/**
		 * Binds a preference's summary to its value. More specifically, when the preference's value is changed, its
		 * summary (line of text below the preference title) is updated to reflect the value. The summary is also
		 * immediately updated upon calling this method. The exact display format is dependent on the type of
		 * preference.
		 */
		private static void bindPreferenceSummaryToValue(Preference preference) {
			// Set the listener to watch for value changes.
			preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

			// Trigger the listener immediately with the preference's
			// current value.
			sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager
					.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
		}

		/**
		 * Helper method for easier cal of bindPreferenceSummaryToValue.
		 * 
		 * @param preferenceKey
		 */
		private void bindPreferenceSummaryToValue(int preferenceKey) {
			bindPreferenceSummaryToValue(findPreference(getString(preferenceKey)));
		}
	}
}
