package de.jeisfeld.augendiagnoselib.activities;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.DialogUtil.DisplayMessageDialogFragment.MessageDialogListener;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.FileUtil;

/**
 * Activity to take pictures of two eye photos.
 */
public class TakePicturesActivity extends BaseActivity {
	/**
	 * The resource key for the input folder.
	 */
	private static final String STRING_EXTRA_PHOTOFOLDER = "de.jeisfeld.augendiagnoselib.PHOTOFOLDER";
	/**
	 * The resource key for the flag indicating if the last picture is the right eye.
	 */
	private static final String BOOL_EXTRA_RIGHTEYELAST = "de.jeisfeld.augendiagnoselib.RIGHTEYELAST";

	/**
	 * The requestCode with which the camera intent is started.
	 */
	public static final int REQUEST_CODE_CAMERA = 1;

	/**
	 * The folder where the photos are stored.
	 */
	private File photoFolder;
	/**
	 * The flag indicating if the last picture is the right eye.
	 */
	private boolean rightEyeLast;

	/**
	 * The file where the photo is stored after capturing.
	 */
	private File tempFile;

	/**
	 * The number of intents for pictures already started.
	 */
	private int countIntentsStarted = 0;
	/**
	 * The number of pictures already captured.
	 */
	private int countCapturedPictures = 0;

	/**
	 * Static helper method to start the activity.
	 *
	 * @param context
	 *            The context in which the activity is started.
	 * @param photoFolderName
	 *            The folder where the photo is stored.
	 * @param rightEyeLast
	 *            A flag indicating if the last picture is the right eye.
	 */
	public static final void startActivity(final Context context, final String photoFolderName,
			final boolean rightEyeLast) {
		Intent intent = new Intent(context, TakePicturesActivity.class);
		intent.putExtra(STRING_EXTRA_PHOTOFOLDER, photoFolderName);
		intent.putExtra(BOOL_EXTRA_RIGHTEYELAST, rightEyeLast);

		context.startActivity(intent);
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String photoFolderString = getIntent().getStringExtra(STRING_EXTRA_PHOTOFOLDER);
		if (photoFolderString != null) {
			photoFolder = new File(photoFolderString);
		}
		else {
			photoFolder = new File(PreferenceUtil.getSharedPreferenceString(R.string.key_folder_input));
		}
		rightEyeLast =
				getIntent().getBooleanExtra(BOOL_EXTRA_RIGHTEYELAST, PreferenceUtil.getSharedPreferenceBoolean(R.string.key_eye_sequence_choice));

		if (savedInstanceState != null) {
			countCapturedPictures = savedInstanceState.getInt("countCapturedPictures");
			countIntentsStarted = savedInstanceState.getInt("countIntentsStarted");
			String tempFilePath = savedInstanceState.getString("tempFilePath");
			if (tempFilePath != null) {
				tempFile = new File(tempFilePath);
			}
		}

		// Start intent only if camera is not already running
		if (countCapturedPictures == countIntentsStarted) {
			askForTakingPicture(countCapturedPictures == 0, rightEyeLast);
		}

	}

	@Override
	protected final int getHelpResource() {
		return R.string.html_organize_photos;
	}

	/**
	 * Create a unique image file as temporary file.
	 *
	 * @return The image file.
	 * @throws IOException
	 *             thrown in case of issues with file creation.
	 */
	private static File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		String imageFileName = "Photo_" + timeStamp + "_";
		File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(imageFileName, ".jpg", storageDir);
		return image;
	}

	/**
	 * Tell the customer that he should take a picture, and then set the intent to take the picture.
	 *
	 * @param isFirst
	 *            Flag indicating if this is the first photo.
	 * @param leftEyeFirst
	 *            Flag indicating if the first photo should be the left eye.
	 */
	private void askForTakingPicture(final boolean isFirst, final boolean leftEyeFirst) {
		int messageResource =
				isFirst ? R.string.message_dialog_take_photo_first : R.string.message_dialog_take_photo_second;

		int messageParameterResource = (isFirst == leftEyeFirst) ? R.string.param_left_eye : R.string.param_right_eye;

		MessageDialogListener listener = new MessageDialogListener() {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void onDialogClick(final DialogFragment dialog) {
				dispatchTakePictureIntent();
			}

			@Override
			public void onDialogCancel(final DialogFragment dialog) {
				finish();
			}
		};

		DialogUtil.displayInfo(this, listener, messageResource, getString(messageParameterResource));
	}

	/**
	 * Start the device application to take a picture.
	 */
	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			// Create the File where the photo should go
			tempFile = null;
			try {
				tempFile = createImageFile();
			}
			catch (IOException e) {
				Log.e(Application.TAG, "Could not create image file.", e);
			}

			// Continue only if the File was successfully created
			if (tempFile != null) {
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
				startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA);
				countIntentsStarted++;
			}
		}
	}

	@Override
	protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (requestCode == REQUEST_CODE_CAMERA) {
			if (resultCode == RESULT_OK) {
				File targetFile = new File(photoFolder, tempFile.getName());
				FileUtil.moveFile(tempFile, targetFile);
				countCapturedPictures++;
				PreferenceUtil.incrementCounter(R.string.key_statistics_counttakephoto);

				if (countCapturedPictures == 1) {
					askForTakingPicture(false, rightEyeLast);
				}
				else {
					OrganizeNewPhotosActivity.startActivity(this, photoFolder.getAbsolutePath(),
							PreferenceUtil.getSharedPreferenceString(R.string.key_folder_photos),
							rightEyeLast);
					finish();
				}
			}
			else {
				Log.w(Application.TAG, "Did not successfully capture picture");
				finish();
			}
		}
	}

	@Override
	protected final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("countCapturedPictures", countCapturedPictures);
		outState.putInt("countIntentsStarted", countIntentsStarted);
		if (tempFile != null) {
			outState.putString("tempFilePath", tempFile.getAbsolutePath());
		}
	}

}
