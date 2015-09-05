package de.jeisfeld.augendiagnoselib.activities;

import static de.jeisfeld.augendiagnoselib.activities.CameraActivity.Action.CHECK_PHOTO;
import static de.jeisfeld.augendiagnoselib.activities.CameraActivity.Action.FINISH_CAMERA;
import static de.jeisfeld.augendiagnoselib.activities.CameraActivity.Action.RE_TAKE_PHOTO;
import static de.jeisfeld.augendiagnoselib.activities.CameraActivity.Action.TAKE_PHOTO;
import static de.jeisfeld.augendiagnoselib.activities.CameraActivity.Action.VIEW_IMAGES;
import static de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto.RightLeft.LEFT;
import static de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto.RightLeft.RIGHT;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.activities.OrganizeNewPhotosActivity.NextAction;
import de.jeisfeld.augendiagnoselib.util.CameraUtil;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto.RightLeft;
import de.jeisfeld.augendiagnoselib.util.imagefile.FileUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.ImageUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.MediaStoreUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * An activity to take pictures with the camera.
 */
@SuppressWarnings("deprecation")
public class CameraActivity extends BaseActivity {
	/**
	 * The resource key for the folder where to store the photos.
	 */
	private static final String STRING_EXTRA_PHOTOFOLDER = "de.jeisfeld.augendiagnoselib.PHOTOFOLDER";
	/**
	 * The resource key for the file to be re-taken.
	 */
	private static final String STRING_EXTRA_PHOTO_RIGHT = "de.jeisfeld.augendiagnoselib.PHOTO_RIGHT";
	/**
	 * The resource key for the file to be re-taken.
	 */
	private static final String STRING_EXTRA_PHOTO_LEFT = "de.jeisfeld.augendiagnoselib.PHOTO_LEFT";

	/**
	 * The camera used by the activity.
	 */
	private Camera camera;

	/**
	 * The preview.
	 */
	private SurfaceView preview = null;

	/**
	 * The surface of the preview.
	 */
	private SurfaceHolder previewHolder = null;

	/**
	 * A flag indicating if the preview is active.
	 */
	private boolean inPreview = false;

	/**
	 * A flag indicating if the camera is configured.
	 */
	private boolean cameraConfigured = false;

	/**
	 * The current rightLeft in the activity.
	 */
	private Action currentAction;

	/**
	 * The current eye.
	 */
	private RightLeft currentRightLeft;

	/**
	 * The side of the last recorded eye.
	 */
	private RightLeft lastRightLeft;

	/**
	 * The temp file holding the right eye.
	 */
	private File rightEyeFile = null;

	/**
	 * The temp file holding the next photo for the right eye.
	 */
	private File newRightEyeFile = null;

	/**
	 * The temp file holding the left eye.
	 */
	private File leftEyeFile = null;

	/**
	 * The temp file holding the next photo for the left eye.
	 */
	private File newLeftEyeFile = null;

	/**
	 * The folder where to store the photos.
	 */
	private File photoFolder = null;

	/**
	 * The right eye file coming as input to the activity.
	 */
	private File inputRightFile = null;

	/**
	 * The left eye file coming as input to the activity.
	 */
	private File inputLeftFile = null;

	/**
	 * Static helper method to start the activity for taking two photos to the input folder.
	 *
	 * @param activity
	 *            The activity from which the activity is started.
	 * @param photoFolder
	 *            The folder where to store the photos.
	 */
	public static final void startActivity(final Activity activity, final String photoFolder) {
		Intent intent = new Intent(activity, CameraActivity.class);
		intent.putExtra(STRING_EXTRA_PHOTOFOLDER, photoFolder);
		activity.startActivity(intent);
	}

	/**
	 * Static helper method to start the activity for re-checking two images.
	 *
	 * @param activity
	 *            The activity from which the activity is started.
	 * @param photoRight
	 *            The path of the right eye image
	 * @param photoLeft
	 *            The path of the left eye image
	 */
	public static final void startActivity(final Activity activity, final String photoRight, final String photoLeft) {
		Intent intent = new Intent(activity, CameraActivity.class);
		intent.putExtra(STRING_EXTRA_PHOTO_RIGHT, photoRight);
		intent.putExtra(STRING_EXTRA_PHOTO_LEFT, photoLeft);
		activity.startActivity(intent);
	}

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (isCreationFailed()) {
			return;
		}

		// Workaround because there is no clear detection of landscape vs reverse landscape
		if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
			// Ensure that requested Orientation has once been set, triggering re-creation of activity.
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
			else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
			return;
		}

		if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			return;
		}

		setContentView(R.layout.activity_camera);

		String photoFolderName = getIntent().getStringExtra(STRING_EXTRA_PHOTOFOLDER);
		if (photoFolderName != null) {
			photoFolder = new File(photoFolderName);
		}
		String inputRightFileName = getIntent().getStringExtra(STRING_EXTRA_PHOTO_RIGHT);
		String inputLeftFileName = getIntent().getStringExtra(STRING_EXTRA_PHOTO_LEFT);

		boolean rightEyeLast = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_eye_sequence_choice);

		// Handle the different scenarios based on input and based on existing temp files.
		File[] existingFiles = FileUtil.getTempCameraFiles();
		if (inputLeftFileName != null && inputRightFileName != null) {
			inputLeftFile = new File(inputLeftFileName);
			inputRightFile = new File(inputRightFileName);

			// Triggered by OrganizeNewPhotosActivity to update photos.
			photoFolder = inputRightFile.getParentFile();
			if (FileUtil.getTempCameraDir().equals(photoFolder)) {
				photoFolder = null;
			}
			leftEyeFile = inputLeftFile;
			rightEyeFile = inputRightFile;
			setThumbImage(leftEyeFile.getAbsolutePath(), LEFT);
			setThumbImage(rightEyeFile.getAbsolutePath(), RIGHT);
			setAction(RE_TAKE_PHOTO, null);
		}
		else if (existingFiles == null || existingFiles.length == 0 || photoFolder != null) {
			// This is the standard scenario.
			setAction(Action.TAKE_PHOTO, rightEyeLast ? LEFT : RIGHT);
		}
		else if (existingFiles.length == 1) {
			// one file already there. Assume that this is already taken and we only have to take the other one
			if (rightEyeLast) {
				leftEyeFile = existingFiles[0];
				setThumbImage(leftEyeFile.getAbsolutePath(), LEFT);
				setAction(Action.TAKE_PHOTO, RIGHT);
			}
			else {
				rightEyeFile = existingFiles[0];
				setThumbImage(rightEyeFile.getAbsolutePath(), RIGHT);
				setAction(Action.TAKE_PHOTO, LEFT);
			}
		}
		else {
			// prevent cleanup in onDestroy
			rightEyeFile = rightEyeLast ? existingFiles[1] : existingFiles[0];
			leftEyeFile = rightEyeLast ? existingFiles[0] : existingFiles[1];

			// both files are already there - switch to Organize.
			OrganizeNewPhotosActivity.startActivity(this, FileUtil.getTempCameraDir().getAbsolutePath(),
					lastRightLeft == RIGHT, NextAction.VIEW_IMAGES);
			finish();
			return;
		}

		preview = (SurfaceView) findViewById(R.id.camera_preview);
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		camera = CameraUtil.getCameraInstance();

		if (camera == null) {
			// The activity depends on the camera.
			DialogUtil.displayError(this, R.string.message_dialog_failed_to_open_camera, true);
			return;
		}

		configureButtons();
	}

	@Override
	public final void onDestroy() {
		cleanupTempFolder();
		super.onDestroy();
	}

	@Override
	protected final int getHelpResource() {
		return 0;
	}

	/**
	 * Configure the buttons of this activity.
	 */
	private void configureButtons() {
		// Add a listener to the capture button
		Button captureButton = (Button) findViewById(R.id.button_capture);
		captureButton.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(final View v) {
						// get an image from the camera
						camera.takePicture(null, null, photoCallback);
						animateFlash();
					}
				});

		// Add a listener to the view image button
		Button viewImageButton = (Button) findViewById(R.id.button_view_images);
		viewImageButton.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(final View v) {
						setAction(VIEW_IMAGES, null);
					}
				});

		// Add listeners to the accept/decline button
		Button acceptButton = (Button) findViewById(R.id.button_accept);
		acceptButton.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(final View v) {
						// Analyze next required step
						if (currentRightLeft == RIGHT) {
							if (rightEyeFile != null && rightEyeFile.exists()) {
								rightEyeFile.delete();
							}
							rightEyeFile = newRightEyeFile;
							newRightEyeFile = null;
							lastRightLeft = RIGHT;

							if (leftEyeFile == null) {
								setAction(TAKE_PHOTO, LEFT);
							}
							else {
								setAction(FINISH_CAMERA, null);
							}
						}
						else {
							if (leftEyeFile != null && leftEyeFile.exists()) {
								leftEyeFile.delete();
							}
							leftEyeFile = newLeftEyeFile;
							newLeftEyeFile = null;
							lastRightLeft = LEFT;

							if (rightEyeFile == null) {
								setAction(TAKE_PHOTO, RIGHT);
							}
							else {
								setAction(FINISH_CAMERA, null);
							}
						}
					}
				});

		Button declineButton = (Button) findViewById(R.id.button_decline);
		declineButton.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(final View v) {
						if (inputRightFile != null && inputLeftFile != null && currentAction != CHECK_PHOTO) {
							// in case of re-take, this button serves to cancel the whole re-take.
							setAction(FINISH_CAMERA, null);
						}
						else {
							if (currentRightLeft == RIGHT) {
								if (newRightEyeFile != null && newRightEyeFile.exists()) {
									newRightEyeFile.delete();
								}
								newRightEyeFile = null;
								if (rightEyeFile != null && rightEyeFile.exists()) {
									setThumbImage(rightEyeFile.getAbsolutePath(), RIGHT);
								}
								else {
									setThumbImage(null, RIGHT);
								}
							}
							else {
								if (newLeftEyeFile != null && newLeftEyeFile.exists()) {
									newLeftEyeFile.delete();
								}
								newLeftEyeFile = null;
							}
							if (leftEyeFile != null && leftEyeFile.exists()) {
								setThumbImage(leftEyeFile.getAbsolutePath(), LEFT);
							}
							else {
								setThumbImage(null, LEFT);
							}

							setAction(TAKE_PHOTO, currentRightLeft);
						}
					}
				});

		LinearLayout cameraThumbRight = (LinearLayout) findViewById(R.id.camera_thumb_layout_right);
		cameraThumbRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				setAction(TAKE_PHOTO, RIGHT);
			}
		});

		LinearLayout cameraThumbLeft = (LinearLayout) findViewById(R.id.camera_thumb_layout_left);
		cameraThumbLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				setAction(TAKE_PHOTO, LEFT);
			}
		});
	}

	/**
	 * Change to the given action.
	 *
	 * @param action
	 *            The new action.
	 * @param rightLeft
	 *            the next eye side.
	 */
	private void setAction(final Action action, final RightLeft rightLeft) {
		LinearLayout cameraThumbRight = (LinearLayout) findViewById(R.id.camera_thumb_layout_right);
		LinearLayout cameraThumbLeft = (LinearLayout) findViewById(R.id.camera_thumb_layout_left);
		Button buttonCapture = (Button) findViewById(R.id.button_capture);
		Button buttonAccept = (Button) findViewById(R.id.button_accept);
		Button buttonDecline = (Button) findViewById(R.id.button_decline);

		switch (action) {
		case TAKE_PHOTO:
			startPreview();
			buttonCapture.setVisibility(View.VISIBLE);
			buttonAccept.setVisibility(View.GONE);

			buttonDecline.setVisibility(inputLeftFile != null && inputRightFile != null ? View.VISIBLE : View.GONE);

			if (rightLeft == RIGHT) {
				cameraThumbRight.setBackgroundResource(R.drawable.camera_thumb_background_highlighted);
				cameraThumbLeft.setBackgroundResource(R.drawable.camera_thumb_background);
			}
			else {
				cameraThumbRight.setBackgroundResource(R.drawable.camera_thumb_background);
				cameraThumbLeft.setBackgroundResource(R.drawable.camera_thumb_background_highlighted);
			}
			break;
		case CHECK_PHOTO:
			buttonCapture.setVisibility(View.GONE);
			buttonAccept.setVisibility(View.VISIBLE);
			buttonDecline.setVisibility(View.VISIBLE);
			break;
		case RE_TAKE_PHOTO:
			buttonCapture.setVisibility(View.GONE);
			buttonAccept.setVisibility(View.GONE);
			buttonDecline.setVisibility(View.VISIBLE);
			cameraThumbLeft.setBackgroundResource(R.drawable.camera_thumb_background);
			cameraThumbRight.setBackgroundResource(R.drawable.camera_thumb_background);
			break;
		case FINISH_CAMERA:
			stopPreview();
			cleanupTempFolder();

			// move files to their target position
			if (inputLeftFile != null && inputRightFile != null) {
				if (!inputLeftFile.equals(leftEyeFile)) {
					FileUtil.moveFile(leftEyeFile, inputLeftFile);
					MediaStoreUtil.deleteThumbnail(inputLeftFile.getAbsolutePath());
					MediaStoreUtil.addPictureToMediaStore(inputLeftFile.getAbsolutePath());
				}
				if (!inputRightFile.equals(rightEyeFile)) {
					FileUtil.moveFile(rightEyeFile, inputRightFile);
					MediaStoreUtil.deleteThumbnail(inputRightFile.getAbsolutePath());
					MediaStoreUtil.addPictureToMediaStore(inputRightFile.getAbsolutePath());
				}
			}
			else if (photoFolder != null && photoFolder.isDirectory()) {
				FileUtil.moveFile(leftEyeFile, new File(photoFolder, leftEyeFile.getName()));
				FileUtil.moveFile(rightEyeFile, new File(photoFolder, rightEyeFile.getName()));
			}

			File organizeFolder = photoFolder == null ? FileUtil.getTempCameraDir() : photoFolder;
			OrganizeNewPhotosActivity.startActivity(this, organizeFolder.getAbsolutePath(),
					lastRightLeft == RIGHT, NextAction.VIEW_IMAGES);
			finish();
			return;
		case VIEW_IMAGES:
			stopPreview();
			cleanupTempFolder();

			ListFoldersForDisplayActivity.startActivity(this);
			finish();
			break;
		default:
			break;
		}

		currentAction = action;
		currentRightLeft = rightLeft;
	}

	/**
	 * Remove unused files from the temp folder.
	 */
	private void cleanupTempFolder() {
		File[] tempFiles = FileUtil.getTempCameraFiles();
		for (File file : tempFiles) {
			if (!file.equals(rightEyeFile) & !file.equals(leftEyeFile)) {
				file.delete();
			}
		}
	}

	/**
	 * Set the thumb image with a byte array.
	 *
	 * @param data
	 *            The data representing the bitmap.
	 */
	private void setThumbImage(final byte[] data) {
		ImageView imageView = (ImageView) findViewById(currentRightLeft == RIGHT ? R.id.camera_thumb_image_right : R.id.camera_thumb_image_left);

		Bitmap bitmap = ImageUtil.getImageBitmap(data, getResources().getDimensionPixelSize(R.dimen.camera_thumb_size));

		imageView.setImageBitmap(bitmap);
	}

	/**
	 * Set the thumb image from a file.
	 *
	 * @param file
	 *            The file to be put in the thumb.
	 * @param rightLeft
	 *            The side of the eye
	 */
	private void setThumbImage(final String file, final RightLeft rightLeft) {
		ImageView imageView = (ImageView) findViewById(rightLeft == RIGHT ? R.id.camera_thumb_image_right : R.id.camera_thumb_image_left);

		if (file != null) {
			Bitmap bitmap = ImageUtil.getImageBitmap(file, getResources().getDimensionPixelSize(R.dimen.camera_thumb_size));
			imageView.setImageBitmap(bitmap);
		}
		else {
			imageView.setImageResource(rightLeft == RIGHT ? R.drawable.icon_eye_right : R.drawable.icon_eye_left);
		}
	}

	/**
	 * Show a flashlight in the preview.
	 */
	private void animateFlash() {
		final View flashView = findViewById(R.id.camera_flash);

		Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new DecelerateInterpolator());
		fadeOut.setDuration(500); // MAGIC_NUMBER
		fadeOut.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(final Animation animation) {
				flashView.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(final Animation animation) {
				// do nothing
			}

			@Override
			public void onAnimationEnd(final Animation animation) {
				flashView.setVisibility(View.GONE);
			}
		});

		AnimationSet animation = new AnimationSet(false);
		animation.addAnimation(fadeOut);

		flashView.startAnimation(animation);
	}

	/**
	 * Initialize the camera.
	 */
	private void initPreview() {
		if (camera != null && previewHolder.getSurface() != null) {
			try {
				camera.setPreviewDisplay(previewHolder);
			}
			catch (Throwable t) {
				Log.e(Application.TAG,
						"Exception in setPreviewDisplay()", t);
				Toast.makeText(CameraActivity.this, t.getMessage(),
						Toast.LENGTH_LONG).show();
			}

			if (!cameraConfigured) {
				Camera.Parameters parameters = camera.getParameters();
				Camera.Size pictureSize = CameraUtil.getBiggestPictureSize(parameters);
				if (pictureSize == null) {
					return;
				}
				Camera.Size previewSsize = CameraUtil.getBestPreviewSize(((float) pictureSize.width) / pictureSize.height, parameters);
				if (previewSsize == null) {
					return;
				}

				parameters.setPreviewSize(previewSsize.width, previewSsize.height);
				parameters.setPictureSize(pictureSize.width, pictureSize.height);
				parameters.setPictureFormat(ImageFormat.JPEG);
				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
				camera.setParameters(parameters);

				// Resize frame to match aspect ratio
				float aspectRatio = ((float) pictureSize.width) / pictureSize.height;
				FrameLayout previewFrame = (FrameLayout) findViewById(R.id.camera_preview_frame);
				LayoutParams layoutParams = previewFrame.getLayoutParams();
				if (previewFrame.getWidth() > aspectRatio * previewFrame.getHeight()) {
					layoutParams.width = Math.round(previewFrame.getHeight() * aspectRatio);
					layoutParams.height = previewFrame.getHeight();
				}
				else {
					layoutParams.width = previewFrame.getWidth();
					layoutParams.height = Math.round(previewFrame.getWidth() / aspectRatio);
				}
				previewFrame.setLayoutParams(layoutParams);

				cameraConfigured = true;
			}
		}
	}

	/**
	 * Start the camera preview.
	 */
	private void startPreview() {
		if (cameraConfigured && camera != null && !inPreview) {
			camera.startPreview();
			inPreview = true;
		}
	}

	/**
	 * Stop the camera preview.
	 */
	private void stopPreview() {
		if (camera != null) {
			if (inPreview) {
				camera.stopPreview();
			}

			camera.release();
			camera = null;
			inPreview = false;
		}
	}

	/**
	 * The callback client for the preview.
	 */
	private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		@Override
		public void surfaceCreated(final SurfaceHolder holder) {
			initPreview();
			if (currentAction == TAKE_PHOTO) {
				startPreview();
			}
		}

		@Override
		public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
		}

		@Override
		public void surfaceDestroyed(final SurfaceHolder holder) {
			stopPreview();
		}
	};

	/**
	 * The callback called when pictures are taken.
	 */
	private PictureCallback photoCallback = new PictureCallback() {
		@Override
		@SuppressFBWarnings(value = "VA_PRIMITIVE_ARRAY_PASSED_TO_OBJECT_VARARG", justification = "Intentionally sending byte array")
		public void onPictureTaken(final byte[] data, final Camera photoCamera) {
			inPreview = false;
			setThumbImage(data);
			File imageFile = FileUtil.getTempJpegFile();

			if (currentRightLeft == RIGHT) {
				newRightEyeFile = imageFile;
			}
			else {
				newLeftEyeFile = imageFile;
			}

			// save photo
			new SavePhotoTask(data, currentRightLeft).execute(imageFile);

			// go to next step
			setAction(CHECK_PHOTO, currentRightLeft);
		}
	};

	/**
	 * The task responsible for saving the picture.
	 */
	private final class SavePhotoTask extends AsyncTask<File, String, File> {
		/**
		 * The data to be saved.
		 */
		private byte[] data;

		/**
		 * The side of the eye to be saved.
		 */
		private RightLeft rightLeft;

		/**
		 * Constructor, passing the data to be saved.
		 *
		 * @param data
		 *            The data to be saved.
		 * @param rightLeft
		 *            The side of the eye to be saved.
		 */
		private SavePhotoTask(final byte[] data, final RightLeft rightLeft) {
			this.data = data;
			this.rightLeft = rightLeft;
		}

		@Override
		protected File doInBackground(final File... imageFiles) {
			File imageFile = imageFiles[0];

			try {
				FileOutputStream fos = new FileOutputStream(imageFile.getPath());

				fos.write(data);
				fos.close();
			}
			catch (java.io.IOException e) {
				Log.e(Application.TAG, "Exception when saving photo", e);
			}

			return imageFile;
		}

		@Override
		protected void onPostExecute(final File imageFile) {
			Log.d(Application.TAG, "Finished saving image " + imageFile.getName() + " - " + rightLeft);
		}
	}

	/**
	 * Enumeration for holding the current rightLeft that the activity is doing.
	 */
	enum Action {
		/**
		 * Capture a photo.
		 */
		TAKE_PHOTO,
		/**
		 * Check a photo.
		 */
		CHECK_PHOTO,
		/**
		 * Finish the camera activity.
		 */
		FINISH_CAMERA,
		/**
		 * Cancel and go to the view images activity.
		 */
		VIEW_IMAGES,
		/**
		 * Make an optional re-take of a photo.
		 */
		RE_TAKE_PHOTO
	}

}
