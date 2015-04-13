package de.eisfeldj.augendiagnose.fragments;

import java.io.File;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.activities.SettingsActivity;
import de.eisfeldj.augendiagnose.util.DialogUtil;
import de.eisfeldj.augendiagnose.util.DialogUtil.DisplayMessageDialogFragment.MessageDialogListener;
import de.eisfeldj.augendiagnose.util.FileUtil;
import de.eisfeldj.augendiagnose.util.JpegSynchronizationUtil;
import de.eisfeldj.augendiagnose.util.PreferenceUtil;
import de.eisfeldj.augendiagnose.util.VersionUtil;

/**
 * Fragment for displaying the settings.
 */
public class SettingsFragment extends PreferenceFragment {
	/**
	 * The requestCode with which the storage access framework is triggered.
	 */
	public static final int REQUEST_CODE_STORAGE_ACCESS = 3;

	/**
	 * Field holding the value of the language preference, in order to detect a real change.
	 */
	private String languageString;
	/**
	 * Field holding the value of the input folder preference, in order to detect a real change.
	 */
	private String folderInput;
	/**
	 * Field holding the value of the photo folder preference, in order to detect a real change.
	 */
	private String folderPhotos;

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.pref_general);

		// Ensure that default values are set.
		languageString = PreferenceUtil.getSharedPreferenceString(R.string.key_language);
		folderInput = PreferenceUtil.getSharedPreferenceString(R.string.key_folder_input);
		folderPhotos = PreferenceUtil.getSharedPreferenceString(R.string.key_folder_photos);

		bindPreferenceSummaryToValue(R.string.key_folder_input);
		bindPreferenceSummaryToValue(R.string.key_folder_photos);
		bindPreferenceSummaryToValue(R.string.key_max_bitmap_size);
		bindPreferenceSummaryToValue(R.string.key_store_option);
		bindPreferenceSummaryToValue(R.string.key_full_resolution);
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

					// For folder choices, if not writable on Android 5, then trigger Storage Access Framework.
					if (preference.getKey().equals(preference.getContext().getString(R.string.key_folder_photos))) {
						if (!folderPhotos.equals(value)) {
							checkFolder(stringValue);
							folderPhotos = stringValue;
						}
					}
					if (preference.getKey().equals(preference.getContext().getString(R.string.key_folder_input))) {
						if (!folderInput.equals(value)) {
							checkFolder(stringValue);
							folderInput = stringValue;
						}
					}

					// Apply change of language
					if (preference.getKey().equals(preference.getContext().getString(R.string.key_language))) {
						if (!languageString.equals(value)) {
							Application.setLanguage();
							PreferenceUtil.setSharedPreferenceString(R.string.key_language, (String) value);

							// Workaround to get rid of all kinds of cashing
							if (!JpegSynchronizationUtil.isSaving()) {
								System.exit(0);
							}

						}
					}

					return true;
				}

				/**
				 * In Android 5, check the folder for writeability. If not, retrieve Uri vor extsdcard via Storage
				 * Access Framework.
				 *
				 * @param folderName
				 *            The folder to be checked.
				 */
				private void checkFolder(final String folderName) {
					if (VersionUtil.isAndroid5() && FileUtil.isOnExtSdCard(folderName)) {
						// Check writeability
						File targetFolder = new File(folderName);
						File dummyFile = new File(targetFolder, "Augendiagnose-Dummy-File");
						if (
						// Case 1: Folder does not exist, and cannot be written
						!targetFolder.exists() && !FileUtil.isWritable(targetFolder)
								&& !FileUtil.isSafWritable(targetFolder)
								||
								// Case 2: Folder exists, and subfiles cannot be written
								targetFolder.exists() && !FileUtil.isWritable(dummyFile)
								&& !FileUtil.isSafWritable(dummyFile)
						) {
							// Ensure via listener that storage access framework is called only after information
							// message.
							MessageDialogListener listener = new MessageDialogListener() {
								/**
								 * Default serial version id.
								 */
								private static final long serialVersionUID = 1L;

								@Override
								public void onDialogClick(final DialogFragment dialog) {
									triggerStorageAccessFramework();
								}
							};

							DialogUtil.displayInfo(getActivity(), listener, R.string.message_dialog_select_extsdcard,
									FileUtil.getExtSdCardFolder(folderName));
						}
					}
				}

				/**
				 * Trigger the storage access framework to access the base folder of the ext sd card.
				 *
				 * @param requestCode
				 */
				@TargetApi(Build.VERSION_CODES.LOLLIPOP)
				private void triggerStorageAccessFramework() {
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
					startActivityForResult(intent, REQUEST_CODE_STORAGE_ACCESS);
				}
			};

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public final void onActivityResult(final int requestCode, final int resultCode, final Intent resultData) {
		if (requestCode == SettingsFragment.REQUEST_CODE_STORAGE_ACCESS && resultCode == Activity.RESULT_OK) {
			// Get Uri from Storage Access Framework.
			Uri treeUri = resultData.getData();

			// Persist access permissions.
			final int takeFlags = resultData.getFlags()
					& (Intent.FLAG_GRANT_READ_URI_PERMISSION
					| Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
			getActivity().getContentResolver().takePersistableUriPermission(treeUri, takeFlags);

			// Persist URI.
			PreferenceUtil.setSharedPreferenceUri(R.string.key_internal_uri_extsdcard, treeUri);
		}
	}

}