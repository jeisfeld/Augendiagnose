package de.jeisfeld.augendiagnoselib.components;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.fragments.DirectoryChooserDialogFragment;
import de.jeisfeld.augendiagnoselib.fragments.DirectoryChooserDialogFragment.ChosenDirectoryListener;
import de.jeisfeld.augendiagnoselib.util.imagefile.FileUtil;

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
	 * Tag to be replaced by the external storage root.
	 */
	private static final String EXTERNAL_STORAGE_PREFIX = "__ext_storage__";

	/**
	 * Tag to be replaced by the application's cache directory.
	 */
	private static final String CACHE_DIR_PREFIX = "__cache_dir__";

	/**
	 * List tag to be replaced by the default camera folder.
	 */
	private static final String CAMERA_FOLDER_PREFIX = "__folder_camera__";

	/**
	 * The selected index in the list.
	 */
	private int mSelectedIndex = -1;

	/**
	 * The custom directory selected via directory browser. Value is null if no custom directory is selected.
	 */
	private String mSelectedCustomDir = null;

	/**
	 * The list index of the custom directory.
	 */
	private int mCustomIndex = -1;

	/**
	 * A listener called when the dialog is closed.
	 */
	private OnDialogClosedListener mOnDialogClosedListener = null;

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

			String mappedValue = replaceSpecialFolderTags(value);

			if (value.equals(CUSTOM_FOLDER)) {
				mCustomIndex = i;
			}
			else if (!mappedValue.equals(value)) {
				entryValues[i] = mappedValue;
			}
		}
	}

	/**
	 * Replace special folder tags in a path.
	 *
	 * @param path
	 *            The path.
	 * @return The path with special folder tags replaced.
	 */
	public static final String replaceSpecialFolderTags(final String path) {
		if (path.startsWith(EXTERNAL_STORAGE_PREFIX)) {
			return FileUtil.getSdCardPath() + path.substring(EXTERNAL_STORAGE_PREFIX.length());
		}
		else if (path.startsWith(CACHE_DIR_PREFIX)) {
			return FileUtil.getTempCameraFolder().getAbsolutePath() + path.substring(CACHE_DIR_PREFIX.length());
		}
		else if (path.startsWith(CAMERA_FOLDER_PREFIX)) {
			return FileUtil.getDefaultCameraFolder() + path.substring(CAMERA_FOLDER_PREFIX.length());
		}
		else {
			return path;
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
			clickedDialogEntryIndex = mCustomIndex;
		}

		builder.setSingleChoiceItems(entries, clickedDialogEntryIndex, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				mSelectedCustomDir = null;

				if (getEntryValues()[which].toString().equals(CUSTOM_FOLDER)) {
					// determine custom folder via dialog
					ChosenDirectoryListener listener = new ChosenDirectoryListener() {
						private static final long serialVersionUID = -220546291074442095L;

						@Override
						public void onChosenDir(final String chosenDir) {
							mSelectedIndex = which;
							mSelectedCustomDir = chosenDir;

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
					mSelectedIndex = which;
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
		if (positiveResult && mSelectedIndex >= 0 && getEntryValues() != null) {
			String value;
			if (mSelectedCustomDir != null) {
				value = mSelectedCustomDir;
			}
			else {
				value = getEntryValues()[mSelectedIndex].toString();
			}
			if (callChangeListener(value)) {
				setValue(value);
				setSummary(value);
			}
		}
		if (mOnDialogClosedListener != null) {
			mOnDialogClosedListener.onDialogClosed();
		}
	}

	/**
	 * Trigger the dialog programmatically.
	 */
	public final void showDialog() {
		showDialog(null);
	}

	/**
	 * Set a listener called when the dialog is closed.
	 *
	 * @param listener
	 *            The listener.
	 */
	public final void setOnDialogClosedListener(final OnDialogClosedListener listener) {
		mOnDialogClosedListener = listener;
	}

	/**
	 * Listener for additional actions to be done when the dialog is closed.
	 */
	public interface OnDialogClosedListener {
		/**
		 * Actions done when the dialog is closed.
		 */
		void onDialogClosed();
	}

}
