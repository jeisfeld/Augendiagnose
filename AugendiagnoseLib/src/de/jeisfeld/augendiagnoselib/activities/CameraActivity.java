package de.jeisfeld.augendiagnoselib.activities;

import static de.jeisfeld.augendiagnoselib.activities.CameraActivity.Action.CANCEL_AND_VIEW_IMAGES;
import static de.jeisfeld.augendiagnoselib.activities.CameraActivity.Action.CHECK_PHOTO;
import static de.jeisfeld.augendiagnoselib.activities.CameraActivity.Action.FINISH_CAMERA;
import static de.jeisfeld.augendiagnoselib.activities.CameraActivity.Action.RE_TAKE_PHOTO;
import static de.jeisfeld.augendiagnoselib.activities.CameraActivity.Action.TAKE_PHOTO;
import static de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto.RightLeft.LEFT;
import static de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto.RightLeft.RIGHT;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.activities.OrganizeNewPhotosActivity.NextAction;
import de.jeisfeld.augendiagnoselib.util.Camera1Handler;
import de.jeisfeld.augendiagnoselib.util.Camera2Handler;
import de.jeisfeld.augendiagnoselib.util.CameraHandler;
import de.jeisfeld.augendiagnoselib.util.OrientationManager;
import de.jeisfeld.augendiagnoselib.util.OrientationManager.OrientationListener;
import de.jeisfeld.augendiagnoselib.util.OrientationManager.ScreenOrientation;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto.RightLeft;
import de.jeisfeld.augendiagnoselib.util.imagefile.FileUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.ImageUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.JpegMetadata;
import de.jeisfeld.augendiagnoselib.util.imagefile.JpegSynchronizationUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.MediaStoreUtil;

/**
 * An activity to take pictures with the camera.
 */
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
	 * The size of the circle overlay bitmap.
	 */
	private static final int CIRCLE_BITMAP_SIZE = 1024;
	/**
	 * The array of possible radii of overlay circles.
	 */
	private static final int[] CIRCLE_RADII = Application.getAppContext().getResources().getIntArray(R.array.camera_overlay_radii);
	/**
	 * The default circle size.
	 */
	private static final int DEFAULT_CIRCLE_TYPE = 2;
	/**
	 * The used flashlight modes.
	 */
	private static final FlashMode[] FLASHLIGHT_MODES = { FlashMode.OFF, FlashMode.ON, FlashMode.TORCH };

	/**
	 * The current rightLeft in the activity.
	 */
	private Action mCurrentAction;

	/**
	 * The current flashlight mode.
	 */
	private FlashMode mCurrentFlashlightMode;

	/**
	 * The current eye.
	 */
	private RightLeft mCurrentRightLeft;

	/**
	 * The side of the last recorded eye.
	 */
	private RightLeft mLastRightLeft;

	/**
	 * The temp file holding the right eye.
	 */
	private File mRightEyeFile = null;

	/**
	 * The temp file holding the next photo for the right eye.
	 */
	private File mNewRightEyeFile = null;

	/**
	 * The temp file holding the left eye.
	 */
	private File mLeftEyeFile = null;

	/**
	 * The temp file holding the next photo for the left eye.
	 */
	private File mNewLeftEyeFile = null;

	/**
	 * The folder where to store the photos.
	 */
	private File mPhotoFolder = null;

	/**
	 * The right eye file coming as input to the activity.
	 */
	private File mInputRightFile = null;

	/**
	 * The left eye file coming as input to the activity.
	 */
	private File mInputLeftFile = null;

	/**
	 * An orientation manager used to track the orientation of the image.
	 */
	private OrientationManager mOrientationManager = null;

	/**
	 * The current screen orientation.
	 */
	private ScreenOrientation mCurrentScreenOrientation;

	/**
	 * The handler operating the camera.
	 */
	private CameraHandler mCameraHandler;

	/**
	 * Static helper method to start the activity for taking two photos to the input folder.
	 *
	 * @param activity
	 *            The activity from which the activity is started.
	 */
	public static final void startActivity(final Activity activity) {
		Intent intent = new Intent(activity, CameraActivity.class);
		activity.startActivity(intent);
	}

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

		setContentView(R.layout.activity_camera);

		boolean useCamera2 = false;

		// TODO: add proper logic to distinguish between camera interfaces
		SurfaceView camera1View = (SurfaceView) findViewById(R.id.camera1_preview);
		TextureView camera2View = (TextureView) findViewById(R.id.camera2_preview);
		if (useCamera2) {
			mCameraHandler = new Camera2Handler(this, (FrameLayout) findViewById(R.id.camera_preview_frame), camera2View, mOnPictureTakenHandler);
			camera1View.setVisibility(View.GONE);
			camera2View.setVisibility(View.VISIBLE);
		}
		else {
			mCameraHandler = new Camera1Handler(this, (FrameLayout) findViewById(R.id.camera_preview_frame), camera1View, mOnPictureTakenHandler);
			camera1View.setVisibility(View.VISIBLE);
			camera2View.setVisibility(View.GONE);
		}

		configureMainButtons();
		configureConfigButtons();

		String photoFolderName = getIntent().getStringExtra(STRING_EXTRA_PHOTOFOLDER);
		if (photoFolderName != null) {
			mPhotoFolder = new File(photoFolderName);
		}

		String inputRightFileName = getIntent().getStringExtra(STRING_EXTRA_PHOTO_RIGHT);
		String inputLeftFileName = getIntent().getStringExtra(STRING_EXTRA_PHOTO_LEFT);

		// Handle the different scenarios based on input and based on existing temp files.
		if (inputLeftFileName != null && inputRightFileName != null) {
			mInputLeftFile = new File(inputLeftFileName);
			mInputRightFile = new File(inputRightFileName);

			// Triggered by OrganizeNewPhotosActivity to update photos.
			mPhotoFolder = mInputRightFile.getParentFile();
			if (FileUtil.getTempCameraFolder().equals(mPhotoFolder)) {
				mPhotoFolder = null;
			}
			mLeftEyeFile = mInputLeftFile;
			mRightEyeFile = mInputRightFile;
			setThumbImage(mLeftEyeFile.getAbsolutePath(), LEFT);
			setThumbImage(mRightEyeFile.getAbsolutePath(), RIGHT);

			setAction(RE_TAKE_PHOTO, null);
		}
		else {
			boolean leftEyeFirst = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_eye_sequence_choice);
			File[] existingFiles = getTempCameraFiles();

			if (existingFiles == null || existingFiles.length == 0 || mPhotoFolder != null) {
				// This is the standard scenario.
				setAction(Action.TAKE_PHOTO, leftEyeFirst ? LEFT : RIGHT);
			}
			else if (existingFiles.length == 1) {
				// one file already there. Assume that this is already taken and we only have to take the other one
				if (leftEyeFirst) {
					setThumbImage(mLeftEyeFile.getAbsolutePath(), LEFT);
					setAction(Action.TAKE_PHOTO, RIGHT);
				}
				else {
					setThumbImage(mRightEyeFile.getAbsolutePath(), RIGHT);
					setAction(Action.TAKE_PHOTO, LEFT);
				}
			}
			else {
				// both files are already there - switch to Organize.
				OrganizeNewPhotosActivity.startActivity(this, FileUtil.getTempCameraFolder().getAbsolutePath(),
						mLastRightLeft == RIGHT, NextAction.VIEW_IMAGES);
				finish();
				return;
			}
		}

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		int overlayCircleSize = PreferenceUtil.getSharedPreferenceInt(R.string.key_internal_camera_circle_type, DEFAULT_CIRCLE_TYPE);
		drawOverlayCircle(overlayCircleSize);

		mOrientationManager = new OrientationManager(this, SensorManager.SENSOR_DELAY_NORMAL, new OrientationListener() {
			@Override
			public void onOrientationChange(final ScreenOrientation screenOrientation) {
				mCurrentScreenOrientation = screenOrientation;
			}
		});
		mOrientationManager.enable();
	}

	@Override
	public final void onDestroy() {
		cleanupTempFolder();
		mCameraHandler.stopPreview();
		if (mOrientationManager != null) {
			mOrientationManager.disable();
		}
		super.onDestroy();
	}

	@Override
	protected final int getHelpResource() {
		return 0;
	}

	/**
	 * Configure the main buttons of this activity.
	 */
	private void configureMainButtons() {
		// Add a listener to the capture button
		final Button captureButton = (Button) findViewById(R.id.buttonCameraTrigger);
		captureButton.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(final View v) {
						// get an image from the camera
						captureButton.setEnabled(false);
						mCameraHandler.takePicture();
					}
				});

		// Add listeners to the accept/decline button
		Button acceptButton = (Button) findViewById(R.id.buttonCameraAccept);
		acceptButton.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(final View v) {
						// Analyze next required step
						if (mCurrentRightLeft == RIGHT) {
							if (mRightEyeFile != null && mRightEyeFile.exists()) {
								mRightEyeFile.delete();
							}
							mRightEyeFile = mNewRightEyeFile;
							mNewRightEyeFile = null;
							mLastRightLeft = RIGHT;

							if (mLeftEyeFile == null) {
								setAction(TAKE_PHOTO, LEFT);
							}
							else {
								setAction(FINISH_CAMERA, null);
							}
						}
						else {
							if (mLeftEyeFile != null && mLeftEyeFile.exists()) {
								mLeftEyeFile.delete();
							}
							mLeftEyeFile = mNewLeftEyeFile;
							mNewLeftEyeFile = null;
							mLastRightLeft = LEFT;

							if (mRightEyeFile == null) {
								setAction(TAKE_PHOTO, RIGHT);
							}
							else {
								setAction(FINISH_CAMERA, null);
							}
						}
					}
				});

		Button declineButton = (Button) findViewById(R.id.buttonCameraDecline);
		declineButton.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(final View v) {
						if (mInputRightFile != null && mInputLeftFile != null && mCurrentAction != CHECK_PHOTO) {
							// in case of re-take, this button serves to cancel the whole re-take.
							setAction(FINISH_CAMERA, null);
						}
						else {
							if (mCurrentRightLeft == RIGHT) {
								if (mNewRightEyeFile != null && mNewRightEyeFile.exists()) {
									mNewRightEyeFile.delete();
								}
								mNewRightEyeFile = null;
								if (mRightEyeFile != null && mRightEyeFile.exists()) {
									setThumbImage(mRightEyeFile.getAbsolutePath(), RIGHT);
								}
								else {
									setThumbImage(null, RIGHT);
								}
							}
							else {
								if (mNewLeftEyeFile != null && mNewLeftEyeFile.exists()) {
									mNewLeftEyeFile.delete();
								}
								mNewLeftEyeFile = null;
							}
							if (mLeftEyeFile != null && mLeftEyeFile.exists()) {
								setThumbImage(mLeftEyeFile.getAbsolutePath(), LEFT);
							}
							else {
								setThumbImage(null, LEFT);
							}

							setAction(TAKE_PHOTO, mCurrentRightLeft);
						}
					}
				});

		// Add a listener to the view image button
		Button viewImagesButton = (Button) findViewById(R.id.buttonCameraViewImages);
		viewImagesButton.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(final View v) {
						setAction(CANCEL_AND_VIEW_IMAGES, null);
					}
				});

	}

	/**
	 * Configure configuration buttons in this activity.
	 */
	private void configureConfigButtons() {

		Button overlayCircleButton = (Button) findViewById(R.id.buttonCameraOverlayCircle);
		overlayCircleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				int currentCircleType = PreferenceUtil.getSharedPreferenceInt(R.string.key_internal_camera_circle_type, DEFAULT_CIRCLE_TYPE);
				int nextCircleType = (currentCircleType + 1) % CIRCLE_RADII.length;
				PreferenceUtil.setSharedPreferenceInt(R.string.key_internal_camera_circle_type, nextCircleType);
				drawOverlayCircle(nextCircleType);
			}
		});

		Button flashlightButton = (Button) findViewById(R.id.buttonCameraFlashlight);
		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
			String storedFlashlightString = PreferenceUtil.getSharedPreferenceString(R.string.key_internal_camera_flashlight_mode);
			FlashMode storedFlashlightMode;
			try {
				storedFlashlightMode = FlashMode.valueOf(storedFlashlightString);
			}
			catch (Exception e) {
				storedFlashlightMode = FlashMode.OFF;
				PreferenceUtil.setSharedPreferenceString(R.string.key_internal_camera_flashlight_mode, storedFlashlightMode.toString());
			}

			setFlashlightMode(storedFlashlightMode);

			flashlightButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					// Determine next flashlight mode
					int flashlightModeIndex = 0;
					for (int i = 0; i < FLASHLIGHT_MODES.length; i++) {
						if (FLASHLIGHT_MODES[i].equals(mCurrentFlashlightMode)) {
							flashlightModeIndex = i;
						}
					}
					flashlightModeIndex = (flashlightModeIndex + 1) % FLASHLIGHT_MODES.length;
					FlashMode newFlashlightMode = FLASHLIGHT_MODES[flashlightModeIndex];
					PreferenceUtil.setSharedPreferenceString(R.string.key_internal_camera_flashlight_mode, newFlashlightMode.toString());

					setFlashlightMode(newFlashlightMode);
				}
			});
		}
		else {
			setFlashlightMode(null);
			flashlightButton.setVisibility(View.GONE);
		}

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

		// Hide application specific buttons
		TypedArray hiddenButtons = getResources().obtainTypedArray(R.array.hidden_camera_buttons);
		for (int i = 0; i < hiddenButtons.length(); i++) {
			int id = hiddenButtons.getResourceId(i, 0);
			if (id != 0) {
				View view = findViewById(id);
				if (view != null) {
					view.setVisibility(View.GONE);
					view.setEnabled(false);
				}
			}
		}
		hiddenButtons.recycle();
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
		mCurrentAction = action;
		mCurrentRightLeft = rightLeft;

		LinearLayout cameraThumbRight = (LinearLayout) findViewById(R.id.camera_thumb_layout_right);
		LinearLayout cameraThumbLeft = (LinearLayout) findViewById(R.id.camera_thumb_layout_left);
		Button buttonCapture = (Button) findViewById(R.id.buttonCameraTrigger);
		Button buttonAccept = (Button) findViewById(R.id.buttonCameraAccept);
		Button buttonDecline = (Button) findViewById(R.id.buttonCameraDecline);
		Button buttonViewImages = (Button) findViewById(R.id.buttonCameraViewImages);
		Button buttonOverlayCircle = (Button) findViewById(R.id.buttonCameraOverlayCircle);
		ImageView imageViewReview = (ImageView) findViewById(R.id.camera_review);

		switch (action) {
		case TAKE_PHOTO:
			updateFlashlight();
			mCameraHandler.startPreview();
			buttonCapture.setVisibility(View.VISIBLE);
			buttonCapture.setEnabled(true);
			buttonAccept.setVisibility(View.GONE);
			buttonDecline.setVisibility(mInputLeftFile != null && mInputRightFile != null ? View.VISIBLE : View.GONE);
			if (buttonViewImages.isEnabled() && buttonDecline.getVisibility() == View.GONE) {
				buttonViewImages.setVisibility(View.VISIBLE);
			}
			buttonOverlayCircle.setEnabled(true);
			imageViewReview.setVisibility(View.GONE);
			cameraThumbLeft.setEnabled(true);
			cameraThumbRight.setEnabled(true);

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
			buttonViewImages.setVisibility(View.INVISIBLE);
			buttonOverlayCircle.setEnabled(false);
			imageViewReview.setVisibility(View.VISIBLE);
			cameraThumbLeft.setEnabled(false);
			cameraThumbRight.setEnabled(false);
			updateFlashlight();
			break;
		case RE_TAKE_PHOTO:
			buttonCapture.setVisibility(View.GONE);
			buttonAccept.setVisibility(View.GONE);
			buttonDecline.setVisibility(View.VISIBLE);
			if (buttonViewImages.isEnabled()) {
				buttonViewImages.setVisibility(View.VISIBLE);
			}
			buttonOverlayCircle.setEnabled(true);
			imageViewReview.setVisibility(View.GONE);
			cameraThumbLeft.setEnabled(true);
			cameraThumbRight.setEnabled(true);
			cameraThumbLeft.setBackgroundResource(R.drawable.camera_thumb_background);
			cameraThumbRight.setBackgroundResource(R.drawable.camera_thumb_background);
			updateFlashlight();
			break;
		case FINISH_CAMERA:
			mCameraHandler.stopPreview();
			cleanupTempFolder();

			// move files to their target position
			if (mInputLeftFile != null && mInputRightFile != null) {
				if (!mInputLeftFile.equals(mLeftEyeFile)) {
					FileUtil.moveFile(mLeftEyeFile, mInputLeftFile);
					// prevent cleanup
					mLeftEyeFile = mInputLeftFile;
					MediaStoreUtil.deleteThumbnail(mInputLeftFile.getAbsolutePath());
					MediaStoreUtil.addPictureToMediaStore(mInputLeftFile.getAbsolutePath());
				}
				if (!mInputRightFile.equals(mRightEyeFile)) {
					FileUtil.moveFile(mRightEyeFile, mInputRightFile);
					// prevent cleanup
					mRightEyeFile = mInputRightFile;
					MediaStoreUtil.deleteThumbnail(mInputRightFile.getAbsolutePath());
					MediaStoreUtil.addPictureToMediaStore(mInputRightFile.getAbsolutePath());
				}
			}
			else if (mPhotoFolder != null && mPhotoFolder.isDirectory()) {
				FileUtil.moveFile(mLeftEyeFile, new File(mPhotoFolder, mLeftEyeFile.getName()));
				FileUtil.moveFile(mRightEyeFile, new File(mPhotoFolder, mRightEyeFile.getName()));
			}

			File organizeFolder = mPhotoFolder == null ? FileUtil.getTempCameraFolder() : mPhotoFolder;
			OrganizeNewPhotosActivity.startActivity(this, organizeFolder.getAbsolutePath(),
					mLastRightLeft == RIGHT, NextAction.VIEW_IMAGES);
			finish();
			return;
		case CANCEL_AND_VIEW_IMAGES:
			mCameraHandler.stopPreview();
			cleanupTempFolder();

			ListFoldersForDisplayActivity.startActivity(this);
			finish();
			break;
		default:
			break;
		}
	}

	/**
	 * Update the flashlight mode.
	 *
	 * @param flashlightMode
	 *            The new flashlight mode.
	 */
	private void setFlashlightMode(final FlashMode flashlightMode) {
		mCurrentFlashlightMode = flashlightMode;
		mCameraHandler.setFlashlightMode(flashlightMode);
		if (flashlightMode != null) {
			updateFlashlight();
		}
	}

	/**
	 * Remove unused files from the temp folder.
	 */
	private void cleanupTempFolder() {
		File[] tempFiles = FileUtil.getTempCameraFiles();
		for (File file : tempFiles) {
			if (!file.equals(mRightEyeFile) & !file.equals(mLeftEyeFile)) {
				file.delete();
			}
		}
	}

	/**
	 * Get all temp camera files and link them to the app.
	 *
	 * @return The list of existing temp files.
	 */
	private File[] getTempCameraFiles() {
		File[] existingFiles = FileUtil.getTempCameraFiles();
		boolean leftEyeFirst = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_eye_sequence_choice);

		if (existingFiles.length == 1) {
			if (leftEyeFirst) {
				mLeftEyeFile = existingFiles[0];
			}
			else {
				mRightEyeFile = existingFiles[0];
			}
		}
		else if (existingFiles.length >= 2) {
			mRightEyeFile = leftEyeFirst ? existingFiles[1] : existingFiles[0];
			mLeftEyeFile = leftEyeFirst ? existingFiles[0] : existingFiles[1];
		}

		return existingFiles;
	}

	/**
	 * Set the thumb image with a byte array.
	 *
	 * @param data
	 *            The data representing the bitmap.
	 */
	private void setThumbImage(final byte[] data) {
		ImageView imageView = (ImageView) findViewById(mCurrentRightLeft == RIGHT ? R.id.camera_thumb_image_right : R.id.camera_thumb_image_left);

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
	 * Show the captured image for preview as fixed image.
	 *
	 * @param data
	 *            The data representing the image.
	 */
	private void setReviewImage(final byte[] data) {
		ImageView imageView = (ImageView) findViewById(R.id.camera_review);

		Bitmap bitmap = ImageUtil.getImageBitmap(data, findViewById(R.id.camera_preview_frame).getWidth());

		imageView.setImageBitmap(bitmap);
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
	 * Draw the overlay circle.
	 *
	 * @param circleType
	 *            the flag holding the circle type.
	 */
	private void drawOverlayCircle(final int circleType) {
		Bitmap overlayBitmap = Bitmap.createBitmap(CIRCLE_BITMAP_SIZE, CIRCLE_BITMAP_SIZE, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(overlayBitmap);

		int circleRadius = CIRCLE_RADII[circleType];
		if (circleRadius > 0) {
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			int overlayColor = PreferenceUtil.getSharedPreferenceInt(R.string.key_overlay_color, Color.RED);
			paint.setColor(overlayColor);
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(5); // MAGIC_NUMBER

			canvas.drawCircle(CIRCLE_BITMAP_SIZE / 2, CIRCLE_BITMAP_SIZE / 2, circleRadius, paint);
		}

		ImageView overlayView = (ImageView) findViewById(R.id.camera_overlay);
		overlayView.setImageBitmap(overlayBitmap);

	}

	/**
	 * Update the flashlight button and set the flashlight mode.
	 */
	private void updateFlashlight() {
		Button flashlightButton = (Button) findViewById(R.id.buttonCameraFlashlight);
		if (FlashMode.OFF.equals(mCurrentFlashlightMode)) {
			flashlightButton.setBackgroundResource(R.drawable.circlebutton_noflash);
		}
		else if (FlashMode.ON.equals(mCurrentFlashlightMode)) {
			flashlightButton.setBackgroundResource(R.drawable.circlebutton_flash);
		}
		else if (FlashMode.TORCH.equals(mCurrentFlashlightMode)) {
			flashlightButton.setBackgroundResource(R.drawable.circlebutton_torch);
		}
		if (mCurrentFlashlightMode != null) {
			if (mCurrentAction != Action.TAKE_PHOTO && mCurrentFlashlightMode == FlashMode.TORCH) {
				mCameraHandler.setFlashlightMode(FlashMode.OFF);
			}
			else {
				mCameraHandler.setFlashlightMode(mCurrentFlashlightMode);
			}
		}
	}

	/**
	 * Get the exif orientation to be applied.
	 *
	 * @return The orientation angle.
	 */
	private short getExifAngle() {
		if (mCurrentScreenOrientation == null) {
			return ExifInterface.ORIENTATION_NORMAL;
		}
		switch (mCurrentScreenOrientation) {
		case LANDSCAPE:
			return ExifInterface.ORIENTATION_NORMAL;
		case PORTRAIT:
			return ExifInterface.ORIENTATION_ROTATE_90;
		case REVERSED_LANDSCAPE:
			return ExifInterface.ORIENTATION_ROTATE_180;
		case REVERSED_PORTRAIT:
			return ExifInterface.ORIENTATION_ROTATE_270;
		default:
			return ExifInterface.ORIENTATION_NORMAL;
		}
	}

	/**
	 * The callback called when pictures are taken.
	 */
	private OnPictureTakenHandler mOnPictureTakenHandler = new OnPictureTakenHandler() {
		@Override
		public void onTakingPicture() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					animateFlash();
				}
			});
		}

		@Override
		public void onPictureTaken(final byte[] data) {
			short exifAngle = getExifAngle();

			File imageFile = FileUtil.getTempJpegFile();

			JpegMetadata metadata = null;

			if (mCurrentRightLeft == RIGHT) {
				mNewRightEyeFile = imageFile;
				if (mInputRightFile != null) {
					// Keep metadata from input
					metadata = JpegSynchronizationUtil.getJpegMetadata(mInputRightFile.getAbsolutePath());
				}
			}
			else {
				mNewLeftEyeFile = imageFile;
				if (mInputLeftFile != null) {
					// Keep metadata from input
					metadata = JpegSynchronizationUtil.getJpegMetadata(mInputLeftFile.getAbsolutePath());
				}
			}

			if (metadata == null) {
				metadata = new JpegMetadata();
				metadata.mRightLeft = mCurrentRightLeft;
				metadata.mComment = "";
				metadata.mOrganizeDate = new Date();
				metadata.mOrientation = exifAngle;
			}

			int overlayCircleRadius =
					CIRCLE_RADII[PreferenceUtil.getSharedPreferenceInt(R.string.key_internal_camera_circle_type, DEFAULT_CIRCLE_TYPE)];
			if (overlayCircleRadius > 0) {
				metadata.mXCenter = 0.5f; // MAGIC_NUMBER
				metadata.mYCenter = 0.5f; // MAGIC_NUMBER
				metadata.mOverlayScaleFactor = ((float) overlayCircleRadius) / CIRCLE_BITMAP_SIZE * getDefaultOverlayScaleFactor();
			}

			// save photo
			new SavePhotoTask(data, mCurrentRightLeft, metadata).execute(imageFile);

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setThumbImage(data);
					setReviewImage(data);
					setAction(CHECK_PHOTO, mCurrentRightLeft);
				}
			});
		}

	};

	/**
	 * Get the default scale factor of the overlay (dependent on the surface).
	 *
	 * @return The default scale factor of the overlay.
	 */

	private float getDefaultOverlayScaleFactor() {
		View surfaceView = findViewById(R.id.camera_preview_frame);

		int height = surfaceView.getHeight();
		int width = surfaceView.getWidth();

		// Factor 8/3 due to 75% size of base overlay circle.
		// Math/min factor due to strange implementation in OverlayPinchImageView.
		return ((float) Math.min(width, height)) / Math.max(width, height) * 8 / 3; // MAGIC_NUMBER
	}

	/**
	 * The task responsible for saving the picture.
	 */
	private final class SavePhotoTask extends AsyncTask<File, String, File> {
		/**
		 * The data to be saved.
		 */
		private byte[] mImageData;

		/**
		 * The side of the eye to be saved.
		 */
		private RightLeft mRightLeft;

		/**
		 * The metadata to be stored.
		 */
		private JpegMetadata mMetadata;

		/**
		 * Constructor, passing the data to be saved.
		 *
		 * @param data
		 *            The data to be saved.
		 * @param rightLeft
		 *            The side of the eye to be saved.
		 * @param metadata
		 *            Metadata to be stored in the photo.
		 */
		private SavePhotoTask(final byte[] data, final RightLeft rightLeft, final JpegMetadata metadata) {
			this.mImageData = data;
			this.mRightLeft = rightLeft;
			this.mMetadata = metadata;
		}

		@Override
		protected File doInBackground(final File... imageFiles) {
			File imageFile = imageFiles[0];

			try {
				FileOutputStream fos = new FileOutputStream(imageFile.getAbsolutePath());

				fos.write(mImageData);
				fos.close();

				if (mMetadata != null) {
					JpegSynchronizationUtil.storeJpegMetadata(imageFile.getAbsolutePath(), mMetadata);
				}
			}
			catch (java.io.IOException e) {
				Log.e(Application.TAG, "Exception when saving photo", e);
			}

			return imageFile;
		}

		@Override
		protected void onPostExecute(final File imageFile) {
			Log.d(Application.TAG, "Finished saving image " + imageFile.getName() + " - " + mRightLeft);
		}
	}

	/**
	 * Handler called after the picture is taken.
	 */
	public interface OnPictureTakenHandler {
		/**
		 * Callback called just when the picture is taken.
		 */
		void onTakingPicture();

		/**
		 * Callback called after the picture is taken.
		 *
		 * @param data
		 *            The image data.
		 */
		void onPictureTaken(byte[] data);
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
		CANCEL_AND_VIEW_IMAGES,
		/**
		 * Make an optional re-take of a photo.
		 */
		RE_TAKE_PHOTO
	}

	/**
	 * Enumeration for modes of the camera flash.
	 */
	public static enum FlashMode {
		/**
		 * The flash is off.
		 */
		OFF,
		/**
		 * The flash is used when taking picture.
		 */
		ON,
		/**
		 * The flash is permanently on.
		 */
		TORCH
	}

}
