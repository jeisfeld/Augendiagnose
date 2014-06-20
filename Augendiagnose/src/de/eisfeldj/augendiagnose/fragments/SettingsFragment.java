package de.eisfeldj.augendiagnose.fragments;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.activities.SettingsActivity;

/**
 * Fragment for displaying the settings.
 */
public class SettingsFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.pref_general);
		bindPreferenceSummaryToValue(R.string.key_folder_input);
		bindPreferenceSummaryToValue(R.string.key_folder_photos);
		bindPreferenceSummaryToValue(R.string.key_max_bitmap_size);
		bindPreferenceSummaryToValue(R.string.key_store_option);
	}

	/**
	 * Binds a preference's summary to its value. More specifically, when the preference's value is changed, its summary
	 * (line of text below the preference title) is updated to reflect the value. The summary is also immediately
	 * updated upon calling this method. The exact display format is dependent on the type of preference.
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

				// For maxBitmapSize, check format and inform PinchImageView
				if (preference.getKey().equals(preference.getContext().getString(R.string.key_max_bitmap_size))) {
					int maxBitmapSize = SettingsActivity.pushMaxBitmapSize(stringValue);
					preference.setSummary(Integer.toString(maxBitmapSize));
				}
			}
			return true;
		}
	};
}