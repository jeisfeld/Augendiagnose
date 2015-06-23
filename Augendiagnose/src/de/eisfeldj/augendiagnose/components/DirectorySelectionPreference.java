package de.eisfeldj.augendiagnose.components;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.fragments.DirectoryChooserDialogFragment;
import de.eisfeldj.augendiagnose.fragments.DirectoryChooserDialogFragment.ChosenDirectoryListener;
import de.eisfeldj.augendiagnose.util.imagefile.FileUtil;

/**
 * A variant of ListPreference that allows to choose from a list of given folders (conigured in the menu configuration)
 * or to select a custom folder via the directory browser.
 */
public class DirectorySelectionPreference extends ListPreference {

	/**
	 * List value to represent a custom folder to be chosen.
	 */
	private static final String CUSTOM_FOLDER = "__custom__";

	/**
	 * List tag to be replaced by the external storage root.
	 */
	private static final String EXTERNAL_STORAGE_PREFIX = "__ext_storage__";

	/**
	 * List tag to be replaced by the default camera folder.
	 */
	private static final String CAMERA_FOLDER_PREFIX = "__folder_camera__";

	/**
	 * The selected index in the list.
	 */
	private int selectedIndex = -1;

	/**
	 * The custom directory selected via directory browser. Value is null if no custom directory is selected.
	 */
	private String selectedCustomDir = null;

	/**
	 * The list index of the custom directory.
	 */
	private int customIndex = -1;

	/**
	 * The constructor replaces placeholders for external storage and camera folder.
	 *
	 * @param context
	 *            The Context this is associated with.
	 * @param attrs
	 *            (from Preference) The attributes of the XML tag that is inflating the preference.
	 */
	public DirectorySelectionPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);

		CharSequence[] entryValues = getEntryValues();

		// Update special values for external storage and camera folder
		for (int i = 0; i < entryValues.length; i++) {
			String value = entryValues[i].toString();

			if (value.startsWith(EXTERNAL_STORAGE_PREFIX)) {
				value = Environment.getExternalStorageDirectory().getAbsolutePath()
						+ value.substring(EXTERNAL_STORAGE_PREFIX.length());
				entryValues[i] = value;
			}
			else if (value.startsWith(CAMERA_FOLDER_PREFIX)) {
				value = FileUtil.getDefaultCameraFolder();
				entryValues[i] = value;
			}
			else if (value.equals(CUSTOM_FOLDER)) {
				customIndex = i;
			}
		}
	}

	/**
	 * Standard constructor.
	 *
	 * @param context
	 *            The Context this is associated with.
	 */
	public DirectorySelectionPreference(final Context context) {
		this(context, null);
	}

	/**
	 * Create the dialog and prepare the creation of the directory selection dialog.
	 *
	 * @param builder
	 *            The DialogBuilder to be customized.
	 */
	@Override
	protected final void onPrepareDialogBuilder(final Builder builder) {
		super.onPrepareDialogBuilder(builder);

		final CharSequence[] entries = getEntries();

		int clickedDialogEntryIndex = findIndexOfValue(getValue());

		if (clickedDialogEntryIndex < 0) {
			clickedDialogEntryIndex = customIndex;
		}

		builder.setSingleChoiceItems(entries, clickedDialogEntryIndex, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				selectedCustomDir = null;

				if (getEntryValues()[which].toString().equals(CUSTOM_FOLDER)) {
					// determine custom folder via dialog
					ChosenDirectoryListener listener = new ChosenDirectoryListener() {
						private static final long serialVersionUID = -220546291074442095L;

						@Override
						public void onChosenDir(final String chosenDir) {
							selectedIndex = which;
							selectedCustomDir = chosenDir;

							DirectorySelectionPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
							dialog.dismiss();
						}

						@Override
						public void onCancelled() {
							DirectorySelectionPreference.this.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
							dialog.dismiss();
						}
					};

					try {
						Activity activity = (Activity) getContext();
						DirectoryChooserDialogFragment.displayDirectoryChooserDialog(activity, listener, getValue());
					}
					catch (ClassCastException e) {
						Log.e(Application.TAG, "Could not open directory chooser", e);
					}
				}
				else {
					selectedIndex = which;
					setValueIndex(which);
					DirectorySelectionPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
					dialog.dismiss();
				}
			}
		});

	}

	/**
	 * Fill the value after closing the dialog. This is mostly the same as in ListPreference, but takes special care in
	 * the case of custom folder.
	 *
	 * @param positiveResult
	 *            (from DialogPreference) positiveResult Whether the positive button was clicked (true), or the negative
	 *            button was clicked or the dialog was canceled (false).
	 */
	@Override
	protected final void onDialogClosed(final boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult && selectedIndex >= 0 && getEntryValues() != null) {
			String value;
			if (selectedCustomDir != null) {
				value = selectedCustomDir;
			}
			else {
				value = getEntryValues()[selectedIndex].toString();
			}
			if (callChangeListener(value)) {
				setValue(value);
				setSummary(value);
			}
		}
	}

}
