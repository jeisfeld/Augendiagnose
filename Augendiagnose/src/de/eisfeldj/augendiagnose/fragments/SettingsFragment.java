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

	/**
	 * Field for temporarily storing the key which is currently processed. This is required while handling Storage
	 * Access Framework.
	 */
	private String currentKey;

	/**
	 * Field for temporarily storing the folder used for Storage Access Framework.
	 */
	private File currentFolder;

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

		// Trigger the listener immediately with the preference's current value.
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
					boolean acceptChange = true;

					// For maxBitmapSize, check format and inform PinchImageView
					if (preference.getKey().equals(preference.getContext().getString(R.string.key_max_bitmap_size))) {
						SettingsActivity.pushMaxBitmapSize(stringValue);
					}

					// For folder choices, if not writable on Android 5, then trigger Storage Access Framework.
					else if (preference.getKey().equals(preference.getContext().getString(R.string.key_folder_photos))) {
						if (!folderPhotos.equals(value)) {
							currentKey = preference.getKey();
							currentFolder = new File(stringValue);
							acceptChange = checkFolder(currentFolder);
						}
					}
					else if (preference.getKey().equals(preference.getContext().getString(R.string.key_folder_input))) {
						if (!folderInput.equals(value)) {
							currentKey = preference.getKey();
							currentFolder = new File(stringValue);
							acceptChange = checkFolder(currentFolder);
						}
					}

					// Apply change of language
					else if (preference.getKey().equals(preference.getContext().getString(R.string.key_language))) {
						if (!languageString.equals(value)) {
							Application.setLanguage();
							PreferenceUtil.setSharedPreferenceString(R.string.key_language, (String) value);

							// Workaround to get rid of all kinds of cashing
							if (!JpegSynchronizationUtil.isSaving()) {
								System.exit(0);
							}

						}
					}

					// set summary
					if (acceptChange) {
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
					}

					return acceptChange;
				}

				/**
				 * Check the folder for writeability. If not, then on Android 5 retrieve Uri for extsdcard via Storage
				 * Access Framework.
				 *
				 * @param folderName
				 *            The folder to be checked.
				 *
				 * @return true if the check was successful or if SAF has been triggered.
				 */
				private boolean checkFolder(final File folder) {
					if (VersionUtil.isAndroid5() && FileUtil.isOnExtSdCard(folder)) {
						if (!folder.exists() || !folder.isDirectory()) {
							return false;
						}

						// On Android 5, trigger storage access framework.
						if (!FileUtil.isWritableNormalOrSaf(folder)) {
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
									FileUtil.getExtSdCardFolder(folder));
						}
						return true;
					}
					else if (VersionUtil.isKitkat() && FileUtil.isOnExtSdCard(folder)) {
						// Assume that Kitkat workaround works
						return true;
					}
					else if (FileUtil.isWritable(new File(folder, "DummyFile"))) {
						return true;
					}
					else {
						DialogUtil.displayError(getActivity(), R.string.message_dialog_cannot_write_to_folder, false,
								currentFolder);

						currentKey = null;
						currentFolder = null;
						return false;
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

	/*
	 * After triggering the Storage Access Framework, ensure that folder is really writable. Set preferences
	 * accordingly.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public final void onActivityResult(final int requestCode, final int resultCode, final Intent resultData) {
		if (requestCode == SettingsFragment.REQUEST_CODE_STORAGE_ACCESS) {
			Uri oldUri = PreferenceUtil.getSharedPreferenceUri(R.string.key_internal_uri_extsdcard);

			Uri treeUri = null;
			if (resultCode == Activity.RESULT_OK) {
				// Get Uri from Storage Access Framework.
				treeUri = resultData.getData();
				// Persist URI - this is required for verification of writability.
				PreferenceUtil.setSharedPreferenceUri(R.string.key_internal_uri_extsdcard, treeUri);
			}

			// If not confirmed SAF, or if still not writable, then revert settings.
			if (resultCode != Activity.RESULT_OK || !FileUtil.isWritableNormalOrSaf(currentFolder)) {
				DialogUtil.displayError(getActivity(), R.string.message_dialog_cannot_write_to_folder_saf, false,
						currentFolder);

				// revert settings
				if (currentKey.equals(getActivity().getString(R.string.key_folder_photos))) {
					PreferenceUtil.setSharedPreferenceString(R.string.key_folder_photos, folderPhotos);
					findPreference(getString(R.string.key_folder_photos)).setSummary(folderPhotos);
				}
				if (currentKey.equals(getActivity().getString(R.string.key_folder_input))) {
					PreferenceUtil.setSharedPreferenceString(R.string.key_folder_input, folderInput);
					findPreference(getString(R.string.key_folder_input)).setSummary(folderInput);
				}
				currentKey = null;
				currentFolder = null;
				PreferenceUtil.setSharedPreferenceUri(R.string.key_internal_uri_extsdcard, oldUri);
				return;
			}

			// After confirmation, update stored value of folder.
			if (currentKey.equals(getActivity().getString(R.string.key_folder_photos))) {
				folderPhotos = currentFolder.getAbsolutePath();
				PreferenceUtil.setSharedPreferenceString(R.string.key_folder_photos, folderPhotos);
			}
			if (currentKey.equals(getActivity().getString(R.string.key_folder_input))) {
				folderInput = currentFolder.getAbsolutePath();
				PreferenceUtil.setSharedPreferenceString(R.string.key_folder_input, folderInput);
			}

			// Persist access permissions.
			final int takeFlags = resultData.getFlags()
					& (Intent.FLAG_GRANT_READ_URI_PERMISSION
					| Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
			getActivity().getContentResolver().takePersistableUriPermission(treeUri, takeFlags);

		}
	}

}
