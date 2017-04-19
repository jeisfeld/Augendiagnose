package de.eisfeldj.augendiagnose.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import de.eisfeldj.augendiagnose.R;
import de.jeisfeld.augendiagnoselib.activities.CameraActivity;
import de.jeisfeld.augendiagnoselib.activities.ListFoldersForDisplayActivity;
import de.jeisfeld.augendiagnoselib.activities.OrganizeNewPhotosActivity;
import de.jeisfeld.augendiagnoselib.activities.OrganizeNewPhotosActivity.NextAction;
import de.jeisfeld.augendiagnoselib.activities.StandardActivity;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.SystemUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.ImageUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.MediaStoreUtil;

/**
 * Main activity of the application.
 */
public class MainActivity extends StandardActivity {

	@Override
	protected final void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (isCreationFailed()) {
			return;
		}

		if (VERSION.SDK_INT >= VERSION_CODES.N && isInMultiWindowMode() && !SystemUtil.isTablet()) {
			setContentView(R.layout.activity_main_splitscreen);
		}
		else {
			setContentView(R.layout.activity_main);
		}

		PreferenceUtil.setSharedPreferenceBoolean(R.string.key_internal_organized_new_photo, false);

		// Track if AdMarvel has been clicked in the current session.
		boolean isAdMarvelClicked = false;
		if (savedInstanceState != null) {
			isAdMarvelClicked = savedInstanceState.getBoolean("isAdMarvelClicked");
		}
		PreferenceUtil.setSharedPreferenceBoolean(R.string.key_admarvel_iscurrentlyclicked, isAdMarvelClicked);

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
				OrganizeNewPhotosActivity.startActivity(this, fileNames.toArray(new String[fileNames.size()]), rightEyeLast, NextAction.NEXT_IMAGES);
			}
		}

		if (!SystemUtil.isAppInstalled(getString(R.string.package_eyefi)) && !SystemUtil.isAppInstalled(getString(R.string.package_mobi))) {
			Button buttonEyeFi = (Button) findViewById(R.id.mainButtonOpenEyeFiApp);
			buttonEyeFi.setVisibility(View.GONE);
		}

		if (!SystemUtil.hasCamera()) {
			Button buttonTakePhotos = (Button) findViewById(R.id.mainButtonTakePictures);
			buttonTakePhotos.setVisibility(View.GONE);
		}

		if (savedInstanceState == null) {
			PreferenceUtil.incrementCounter(R.string.key_statistics_countmain);
		}

		requestBannerAdIfEligible();
	}

	@Override
	protected final int getHelpResource() {
		return R.string.html_overview;
	}

	@Override
	protected final void onSaveInstanceState(@NonNull final Bundle outState) {
		super.onSaveInstanceState(outState);
		boolean isAdMarvelClicked = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_admarvel_iscurrentlyclicked);
		outState.putBoolean("isAdMarvelClicked", isAdMarvelClicked);
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
}
