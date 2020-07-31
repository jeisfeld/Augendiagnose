package de.jeisfeld.augendiagnoselib.activities;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.DialogUtil.DisplayMessageDialogFragment.MessageDialogListener;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.SystemUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.FileUtil;

/**
 * Activity to retrigger SAF access.
 */
public class RequestSafActivity extends StandardActivity {
	/**
	 * The requestCode with which the storage access framework is triggered for input folder.
	 */
	private static final int REQUEST_CODE_STORAGE_ACCESS_INPUT = 4;
	/**
	 * The expected input folder.
	 */
	private String mExpectedFolderInput = null;

	/**
	 * Static helper method to start the activity.
	 *
	 * @param context The context in which the activity is started.
	 */
	public static void startActivity(@NonNull final Context context) {
		if (SystemUtil.isAtLeastVersion(VERSION_CODES.Q)) {
			Intent intent = new Intent(context, RequestSafActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}

	@Override
	protected final void onCreate(@Nullable final Bundle savedInstanceState) {
		PreferenceUtil.removeSharedPreference(R.string.key_internal_uri_extsdcard_photos);
		PreferenceUtil.removeSharedPreference(R.string.key_internal_uri_extsdcard_input);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected final int getHelpResource() {
		return R.string.html_overview;
	}

	@Override
	@RequiresApi(api = VERSION_CODES.LOLLIPOP)
	protected final boolean checkSafPermissions() {
		DialogUtil.displayInfo(this, new MessageDialogListener() {
					/**
					 * The serial version UID.
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public void onDialogClick(final DialogFragment dialog) {
						setExpectedFolderPhotos(PreferenceUtil.getSharedPreferenceString(de.jeisfeld.augendiagnoselib.R.string.key_folder_photos));
						Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
						startActivityForResult(intent, REQUEST_CODE_STORAGE_ACCESS_PHOTOS);
					}

					@Override
					public void onDialogCancel(final DialogFragment dialog) {
						finish();
					}
				},
				R.string.message_dialog_select_photos_folder_saf_redo,
				PreferenceUtil.getSharedPreferenceString(de.jeisfeld.augendiagnoselib.R.string.key_folder_photos));
		return false;
	}

	@Override
	@RequiresApi(api = VERSION_CODES.Q)
	protected final void restartActivity() {
		if (Application.getAppContext().getResources().getBoolean(R.bool.flag_requires_input_folder)) {
			DialogUtil.displayInfo(this, new MessageDialogListener() {
						/**
						 * The serial version UID.
						 */
						private static final long serialVersionUID = 1L;

						@Override
						public void onDialogClick(final DialogFragment dialog) {
							mExpectedFolderInput = PreferenceUtil.getSharedPreferenceString(R.string.key_folder_input);
							Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
							startActivityForResult(intent, REQUEST_CODE_STORAGE_ACCESS_INPUT);
						}

						@Override
						public void onDialogCancel(final DialogFragment dialog) {
							finish();
						}
					},
					R.string.message_dialog_select_input_folder_saf_redo,
					PreferenceUtil.getSharedPreferenceString(R.string.key_folder_input));
		}
		else {
			finish();
		}
	}

	@Override
	protected final void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
		if (requestCode == REQUEST_CODE_STORAGE_ACCESS_INPUT) {
			if (VERSION.SDK_INT >= VERSION_CODES.Q && resultCode == RESULT_OK && data != null && data.getData() != null) {
				handleSelectedInputFolderUri(data);
			}
		}
		else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	/**
	 * Process the URI returned from SAF for input folder (similar to MainActivity).
	 *
	 * @param data The returned data.
	 */
	@RequiresApi(api = VERSION_CODES.Q)
	private void handleSelectedInputFolderUri(final Intent data) {
		Uri treeUri = data.getData();
		String path = FileUtil.getFullPathFromTreeUri(treeUri);

		if (treeUri == null || path == null) {
			restartActivity();
			return;
		}

		if (mExpectedFolderInput != null && !mExpectedFolderInput.equals(path)) {
			DialogUtil.displayInfo(this, new MessageDialogListener() {
				/**
				 * The serial version uid.
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void onDialogClick(final DialogFragment dialog) {
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
					startActivityForResult(intent, REQUEST_CODE_STORAGE_ACCESS_INPUT);
				}

				@Override
				public void onDialogCancel(final DialogFragment dialog) {
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
					startActivityForResult(intent, REQUEST_CODE_STORAGE_ACCESS_INPUT);
				}
			}, de.jeisfeld.augendiagnoselib.R.string.message_dialog_changed_input_folder, mExpectedFolderInput, path);
		}
		else {
			PreferenceUtil.setSharedPreferenceUri(de.jeisfeld.augendiagnoselib.R.string.key_internal_uri_extsdcard_input, treeUri);

			// If still not writable, then revert settings.
			if (!FileUtil.isWritableNormalOrSaf(new File(path))) {
				PreferenceUtil.removeSharedPreference(de.jeisfeld.augendiagnoselib.R.string.key_internal_uri_extsdcard_input);
				DialogUtil.displayInfo(this, new MessageDialogListener() {
					/**
					 * The serial version uid.
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public void onDialogClick(final DialogFragment dialog) {
						Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
						startActivityForResult(intent, REQUEST_CODE_STORAGE_ACCESS_INPUT);
					}

					@Override
					public void onDialogCancel(final DialogFragment dialog) {
						Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
						startActivityForResult(intent, REQUEST_CODE_STORAGE_ACCESS_INPUT);
					}
				}, de.jeisfeld.augendiagnoselib.R.string.message_dialog_cannot_write_to_folder, path);
				return;
			}

			PreferenceUtil.setSharedPreferenceString(de.jeisfeld.augendiagnoselib.R.string.key_folder_input, path);
			getContentResolver().takePersistableUriPermission(treeUri, data.getFlags()
					& (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
			mExpectedFolderInput = null;
			finish();
		}
	}
}
