package de.eisfeldj.augendiagnose.fragments;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.activities.SettingsActivity;
import de.eisfeldj.augendiagnose.util.JpegSynchronizationUtil;

/**
 * Fragment for displaying the settings.
 */
public class SettingsFragment extends PreferenceFragment {
	/**
	 * Field holding the value of the language preference, in order to detect a real change.
	 */
	private String languageString;

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.pref_general);

		// Ensure that language is set
		languageString = Application.getSharedPreferenceString(R.string.key_language);
		if (languageString == null || languageString.length() == 0) {
			languageString = getString(R.string.pref_default_language);
			Application.setSharedPreferenceString(R.string.key_language, languageString);
		}

		bindPreferenceSummaryToValue(R.string.key_folder_input);
		bindPreferenceSummaryToValue(R.string.key_folder_photos);
		bindPreferenceSummaryToValue(R.string.key_max_bitmap_size);
		bindPreferenceSummaryToValue(R.string.key_store_option);
		bindPreferenceSummaryToValue(R.string.key_language);
	}

	/**
	 * Binds a preference's summary to its value. More specifically, when the preference's value is changed, its summary
	 * (line of text below the preference title) is updated to reflect the value. The summary is also immediately
	 * updated upon calling this method. The exact display format is dependent on the type of preference.
	 *
	 * @param preference
	 *            The preference to be bound.
	 */
	private void bindPreferenceSummaryToValue(final Preference preference) {
		// Set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager
				.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
	}

	/**
	 * Helper method for easier call of {@link #bindPreferenceSummaryToValue(android.preference.Preference)}.
	 *
	 * @param preferenceKey
	 *            The key of the preference.
	 */
	private void bindPreferenceSummaryToValue(final int preferenceKey) {
		bindPreferenceSummaryToValue(findPreference(getString(preferenceKey)));
	}

	/**
	 * A preference value change listener that updates the preference's summary to reflect its new value.
	 */
	private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
			new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(final Preference preference, final Object value) {
					String stringValue = value.toString();

					if (preference.getClass().equals(ListPreference.class)) {
						// For list preferences (except customized ones), look up the correct display value in
						// the preference's 'entries' list.
						ListPreference listPreference = (ListPreference) preference;
						int index = listPreference.findIndexOfValue(stringValue);

						preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
					}
					else {
						// For all other preferences, set the summary to the value's
						// simple string representation.
						preference.setSummary(stringValue);

					}

					// For maxBitmapSize, check format and inform PinchImageView
					if (preference.getKey().equals(preference.getContext().getString(R.string.key_max_bitmap_size))) {
						SettingsActivity.pushMaxBitmapSize(stringValue);
					}

					// Apply change of language
					if (preference.getKey().equals(preference.getContext().getString(R.string.key_language))) {
						if (!languageString.equals(value)) {
							Application.setLanguage();
							Application.setSharedPreferenceString(R.string.key_language, (String) value);

							// Workaround to get rid of all kinds of cashing
							if (!JpegSynchronizationUtil.isSaving()) {
								System.exit(0);
							}

						}
					}

					return true;
				}
			};
}