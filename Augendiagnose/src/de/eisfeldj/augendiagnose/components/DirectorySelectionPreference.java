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
import de.eisfeldj.augendiagnose.components.DirectoryChooserDialogFragment.ChosenDirectoryListener;
import de.eisfeldj.augendiagnose.util.FileUtil;

public class DirectorySelectionPreference extends ListPreference {

	private static final String CUSTOM_FOLDER = "__custom__";
	private static final String EXTERNAL_STORAGE_PREFIX = "__ext_storage__";
	private static final String CAMERA_FOLDER_PREFIX = "__folder_camera__";

	private int selectedIndex = -1;
	private String selectedCustomDir = null;
	private int customIndex = -1;

	/**
	 * The constructor replaces placeholders for external storage and camera folder.
	 * 
	 * @param context
	 * @param attrs
	 */
	public DirectorySelectionPreference(Context context, AttributeSet attrs) {
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

	public DirectorySelectionPreference(Context context) {
		this(context, null);
	}

	/**
	 * Create the dialog and prepare the creation of the directory selection dialog.
	 */
	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		super.onPrepareDialogBuilder(builder);

		final CharSequence[] entries = getEntries();

		int clickedDialogEntryIndex = findIndexOfValue(getValue());

		if (clickedDialogEntryIndex < 0) {
			clickedDialogEntryIndex = customIndex;
		}

		builder.setSingleChoiceItems(entries, clickedDialogEntryIndex, new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int which) {
				selectedCustomDir = null;

				if (getEntryValues()[which].toString().equals(CUSTOM_FOLDER)) {
					// determine custom folder via dialog
					ChosenDirectoryListener listener = new ChosenDirectoryListener() {
						private static final long serialVersionUID = -220546291074442095L;
						@Override
						public void onChosenDir(String chosenDir) {
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
	 * Fill the value after closing the dialog. (Mostly a clone of super method.)
	 */
	@Override
	protected void onDialogClosed(boolean positiveResult) {
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
