package de.eisfeldj.augendiagnose.activities;

import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import de.eisfeldj.augendiagnose.R;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.activities.CameraActivity;
import de.jeisfeld.augendiagnoselib.activities.ListFoldersForDisplayActivity;
import de.jeisfeld.augendiagnoselib.activities.OrganizeNewPhotosActivity;
import de.jeisfeld.augendiagnoselib.activities.OrganizeNewPhotosActivity.NextAction;
import de.jeisfeld.augendiagnoselib.activities.StandardActivity;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.DialogUtil.DisplayMessageDialogFragment.MessageDialogListener;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.SystemUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.FileUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.ImageUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.MediaStoreUtil;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * Main activity of the application.
 */
public class MainActivity extends StandardActivity {
	/**
	 * The requestCode with which the storage access framework is triggered for input folder.
	 */
	private static final int REQUEST_CODE_STORAGE_ACCESS_INPUT = 4;
	/**
	 * The expected input folder.
	 */
	private String mExpectedFolderInput = null;

	@Override
	protected final void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (isCreationFailed()) {
			finish();
		}

		if (VERSION.SDK_INT >= VERSION_CODES.N && isInMultiWindowMode() && !SystemUtil.isTablet()) {
			setContentView(R.layout.activity_main_splitscreen);
		}
		else {
			setContentView(R.layout.activity_main);
		}

		PreferenceUtil.setSharedPreferenceBoolean(R.string.key_internal_organized_new_photo, false);

		Intent intent = getIntent();
		if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction()) && intent.getType() != null) {
			// Application was started from other application by passing a list of images - open
			// OrganizeNewPhotosActivity.
			ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
			if (imageUris != null) {
				ArrayList<String> fileNames = new ArrayList<>();
				for (int i = 0; i < imageUris.size(); i++) {
					if (ImageUtil.getMimeType(imageUris.get(i)).startsWith("image/")) {
						String fileName = MediaStoreUtil.getRealPathFromUri(imageUris.get(i));
						if (fileName == null) {
							fileName = imageUris.get(i).getPath();
						}
						fileNames.add(fileName);
					}
				}
				boolean rightEyeLast = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_eye_sequence_choice);
				OrganizeNewPhotosActivity.startActivity(this, fileNames.toArray(new String[0]), rightEyeLast, NextAction.NEXT_IMAGES);
			}
		}

		if (!SystemUtil.isAppInstalled(getString(R.string.package_eyefi)) && !SystemUtil.isAppInstalled(getString(R.string.package_mobi))) {
			Button buttonEyeFi = findViewById(R.id.mainButtonOpenEyeFiApp);
			buttonEyeFi.setVisibility(View.GONE);
		}

		if (!SystemUtil.hasCamera()) {
			Button buttonTakePhotos = findViewById(R.id.mainButtonTakePictures);
			buttonTakePhotos.setVisibility(View.GONE);
		}

		if (savedInstanceState == null) {
			PreferenceUtil.incrementCounter(R.string.key_statistics_countmain);
		}
	}

	@Override
	protected final int getHelpResource() {
		return R.string.html_overview;
	}

	@Override
	protected final void checkPermissions() {
		super.checkPermissions();

		if (SystemUtil.isAtLeastVersion(VERSION_CODES.Q)
				&& PreferenceUtil.getSharedPreferenceUri(de.jeisfeld.augendiagnoselib.R.string.key_internal_uri_extsdcard_input) == null) {
			int initialVersion = PreferenceUtil.getSharedPreferenceInt(de.jeisfeld.augendiagnoselib.R.string.key_statistics_initialversion, -1);
			int dialogResource = R.string.message_dialog_select_input_folder;
			if (initialVersion < Application.getVersion()) {
				mExpectedFolderInput = PreferenceUtil.getSharedPreferenceString(de.jeisfeld.augendiagnoselib.R.string.key_folder_input);
				dialogResource = R.string.message_dialog_select_input_folder_saf;
			}
			DialogUtil.displayInfo(this, new MessageDialogListener() {
				/**
				 * The serial version UID.
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void onDialogClick(final DialogFragment dialog) {
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
					startActivityForResult(intent, REQUEST_CODE_STORAGE_ACCESS_INPUT);
				}

				@Override
				public void onDialogCancel(final DialogFragment dialog) {
					finish();
				}
			}, dialogResource, mExpectedFolderInput);
		}
	}


	/**
	 * onClick action for Button to open the Eye-Fi app.
	 *
	 * @param view the button to open the Eye-Fi app.
	 */
	public final void openEyeFiApp(final View view) {
		if (SystemUtil.isAppInstalled(getString(R.string.package_mobi))) {
			startActivity(getPackageManager().getLaunchIntentForPackage(getString(R.string.package_mobi)));
		}
		else if (SystemUtil.isAppInstalled(getString(R.string.package_eyefi))) {
			startActivity(getPackageManager().getLaunchIntentForPackage(getString(R.string.package_eyefi)));
		}
		else {
			Intent googlePlayIntent = new Intent(Intent.ACTION_VIEW);
			googlePlayIntent.setData(Uri.parse("market://details?id=" + getString(R.string.package_mobi)));
			try {
				startActivity(googlePlayIntent);
			}
			catch (Exception e) {
				DialogUtil.displayError(this, R.string.message_dialog_failed_to_open_google_play, false);
			}
		}
	}

	/**
	 * onClick action for Button to start the activity to take pictures.
	 *
	 * @param view the button to take pictures.
	 */
	public final void takePicturesActivity(final View view) {
		CameraActivity.startActivity(this, PreferenceUtil.getSharedPreferenceString(R.string.key_folder_input));
	}

	/**
	 * onClick action for Button to display eye photos.
	 *
	 * @param view the button to display the eye photos.
	 */
	public final void listFoldersForDisplayActivity(final View view) {
		ListFoldersForDisplayActivity.startActivity(this);
	}

	/**
	 * onClick action for Button to organize new eye photos.
	 *
	 * @param view the button to organize new folders.
	 */
	public final void organizeNewFoldersActivity(final View view) {
		OrganizeNewPhotosActivity.startActivity(this,
				PreferenceUtil.getSharedPreferenceString(R.string.key_folder_input),
				PreferenceUtil.getSharedPreferenceBoolean(R.string.key_eye_sequence_choice),
				NextAction.NEXT_IMAGES);
	}

	@RequiresApi(api = VERSION_CODES.Q)
	@Override
	protected final void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_STORAGE_ACCESS_INPUT && resultCode == RESULT_OK && data != null) {
			Uri treeUri = data.getData();
			PreferenceUtil.setSharedPreferenceUri(de.jeisfeld.augendiagnoselib.R.string.key_internal_uri_extsdcard_input, treeUri);
			String path = FileUtil.getFullPathFromTreeUri(treeUri);
			PreferenceUtil.setSharedPreferenceString(de.jeisfeld.augendiagnoselib.R.string.key_folder_input, path);
			// Persist access permissions.
			getContentResolver().takePersistableUriPermission(treeUri, data.getFlags()
					& (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));

			if (mExpectedFolderInput != null && !mExpectedFolderInput.equals(path)) {
				DialogUtil.displayInfo(this, new MessageDialogListener() {
					/**
					 * The serial version uid.
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public void onDialogClick(final DialogFragment dialog) {
						mExpectedFolderInput = null;
						restartActivity();
					}

					@Override
					public void onDialogCancel(final DialogFragment dialog) {
						mExpectedFolderInput = null;
						restartActivity();
					}
				}, R.string.message_dialog_changed_input_folder, mExpectedFolderInput, path);
			}
			else {
				mExpectedFolderInput = null;
				restartActivity();
			}
		}
	}
}
