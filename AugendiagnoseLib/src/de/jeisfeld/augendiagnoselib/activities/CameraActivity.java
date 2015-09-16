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
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
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
import de.jeisfeld.augendiagnoselib.util.imagefile.JpegMetadata;
import de.jeisfeld.augendiagnoselib.util.imagefile.JpegSynchronizationUtil;
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
	 * The default flashlight type.
	 */
	private static final String[] FLASHLIGHT_MODES = { Parameters.FLASH_MODE_OFF, Parameters.FLASH_MODE_ON, Parameters.FLASH_MODE_TORCH };

	/**
	 * The default overlay scale factor (required due to strange calculation in OverlayPinchImageView).
	 */
	private float defaultOverlayScaleFactor;

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
	 * The current flashlight mode.
	 */
	private String currentFlashlightMode;

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

		configureMainButtons();
		configureConfigButtons();

		String photoFolderName = getIntent().getStringExtra(STRING_EXTRA_PHOTOFOLDER);
		if (photoFolderName != null) {
			photoFolder = new File(photoFolderName);
		}
		String inputRightFileName = getIntent().getStringExtra(STRING_EXTRA_PHOTO_RIGHT);
		String inputLeftFileName = getIntent().getStringExtra(STRING_EXTRA_PHOTO_LEFT);

		// Handle the different scenarios based on input and based on existing temp files.
		if (inputLeftFileName != null && inputRightFileName != null) {
			inputLeftFile = new File(inputLeftFileName);
			inputRightFile = new File(inputRightFileName);

			// Triggered by OrganizeNewPhotosActivity to update photos.
			photoFolder = inputRightFile.getParentFile();
			if (FileUtil.getTempCameraFolder().equals(photoFolder)) {
				photoFolder = null;
			}
			leftEyeFile = inputLeftFile;
			rightEyeFile = inputRightFile;
			setThumbImage(leftEyeFile.getAbsolutePath(), LEFT);
			setThumbImage(rightEyeFile.getAbsolutePath(), RIGHT);

			setAction(RE_TAKE_PHOTO, null);
		}
		else {
			boolean leftEyeFirst = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_eye_sequence_choice);
			File[] existingFiles = getTempCameraFiles();

			if (existingFiles == null || existingFiles.length == 0 || photoFolder != null) {
				// This is the standard scenario.
				setAction(Action.TAKE_PHOTO, leftEyeFirst ? LEFT : RIGHT);
			}
			else if (existingFiles.length == 1) {
				// one file already there. Assume that this is already taken and we only have to take the other one
				if (leftEyeFirst) {
					setThumbImage(leftEyeFile.getAbsolutePath(), LEFT);
					setAction(Action.TAKE_PHOTO, RIGHT);
				}
				else {
					setThumbImage(rightEyeFile.getAbsolutePath(), RIGHT);
					setAction(Action.TAKE_PHOTO, LEFT);
				}
			}
			else {
				// both files are already there - switch to Organize.
				OrganizeNewPhotosActivity.startActivity(this, FileUtil.getTempCameraFolder().getAbsolutePath(),
						lastRightLeft == RIGHT, NextAction.VIEW_IMAGES);
				finish();
				return;
			}
		}

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		preview = (SurfaceView) findViewById(R.id.camera_preview);
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setKeepScreenOn(true);

		camera = CameraUtil.getCameraInstance();

		if (camera == null) {
			// The activity depends on the camera.
			DialogUtil.displayError(this, R.string.message_dialog_failed_to_open_camera, true);
			return;
		}

		int overlayCircleSize = PreferenceUtil.getSharedPreferenceInt(R.string.key_internal_camera_circle_type, DEFAULT_CIRCLE_TYPE);
		drawOverlayCircle(overlayCircleSize);
	}

	@Override
	public final void onDestroy() {
		cleanupTempFolder();
		stopPreview();
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
						camera.takePicture(null, null, photoCallback);
						animateFlash();
					}
				});

		// Add listeners to the accept/decline button
		Button acceptButton = (Button) findViewById(R.id.buttonCameraAccept);
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

		Button declineButton = (Button) findViewById(R.id.buttonCameraDecline);
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
			currentFlashlightMode = PreferenceUtil.getSharedPreferenceString(R.string.key_internal_camera_flashlight_mode);
			if (currentFlashlightMode == null || currentFlashlightMode.length() == 0) {
				currentFlashlightMode = FLASHLIGHT_MODES[0];
				PreferenceUtil.setSharedPreferenceString(R.string.key_internal_camera_flashlight_mode, currentFlashlightMode);
			}
			updateFlashlight();

			flashlightButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					// Determine next flashlight mode
					int flashlightModeIndex = 0;
					for (int i = 0; i < FLASHLIGHT_MODES.length; i++) {
						if (FLASHLIGHT_MODES[i].equals(currentFlashlightMode)) {
							flashlightModeIndex = i;
						}
					}
					flashlightModeIndex = (flashlightModeIndex + 1) % FLASHLIGHT_MODES.length;
					currentFlashlightMode = FLASHLIGHT_MODES[flashlightModeIndex];
					PreferenceUtil.setSharedPreferenceString(R.string.key_internal_camera_flashlight_mode, currentFlashlightMode);

					updateFlashlight();
				}
			});
		}
		else {
			currentFlashlightMode = null;
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
		currentAction = action;
		currentRightLeft = rightLeft;

		LinearLayout cameraThumbRight = (LinearLayout) findViewById(R.id.camera_thumb_layout_right);
		LinearLayout cameraThumbLeft = (LinearLayout) findViewById(R.id.camera_thumb_layout_left);
		Button buttonCapture = (Button) findViewById(R.id.buttonCameraTrigger);
		Button buttonAccept = (Button) findViewById(R.id.buttonCameraAccept);
		Button buttonDecline = (Button) findViewById(R.id.buttonCameraDecline);
		Button buttonViewImages = (Button) findViewById(R.id.buttonCameraViewImages);
		Button buttonOverlayCircle = (Button) findViewById(R.id.buttonCameraOverlayCircle);

		switch (action) {
		case TAKE_PHOTO:
			startPreview();
			buttonCapture.setVisibility(View.VISIBLE);
			buttonCapture.setEnabled(true);
			buttonAccept.setVisibility(View.GONE);
			buttonDecline.setVisibility(inputLeftFile != null && inputRightFile != null ? View.VISIBLE : View.GONE);
			if (buttonViewImages.isEnabled() && buttonDecline.getVisibility() == View.GONE) {
				buttonViewImages.setVisibility(View.VISIBLE);
			}
			buttonOverlayCircle.setEnabled(true);

			if (rightLeft == RIGHT) {
				cameraThumbRight.setBackgroundResource(R.drawable.camera_thumb_background_highlighted);
				cameraThumbLeft.setBackgroundResource(R.drawable.camera_thumb_background);
			}
			else {
				cameraThumbRight.setBackgroundResource(R.drawable.camera_thumb_background);
				cameraThumbLeft.setBackgroundResource(R.drawable.camera_thumb_background_highlighted);
			}

			updateFlashlight();
			break;
		case CHECK_PHOTO:
			buttonCapture.setVisibility(View.GONE);
			buttonAccept.setVisibility(View.VISIBLE);
			buttonDecline.setVisibility(View.VISIBLE);
			buttonViewImages.setVisibility(View.INVISIBLE);
			buttonOverlayCircle.setEnabled(false);
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
			cameraThumbLeft.setBackgroundResource(R.drawable.camera_thumb_background);
			cameraThumbRight.setBackgroundResource(R.drawable.camera_thumb_background);
			updateFlashlight();
			break;
		case FINISH_CAMERA:
			stopPreview();
			cleanupTempFolder();

			// move files to their target position
			if (inputLeftFile != null && inputRightFile != null) {
				if (!inputLeftFile.equals(leftEyeFile)) {
					FileUtil.moveFile(leftEyeFile, inputLeftFile);
					// prevent cleanup
					leftEyeFile = inputLeftFile;
					MediaStoreUtil.deleteThumbnail(inputLeftFile.getAbsolutePath());
					MediaStoreUtil.addPictureToMediaStore(inputLeftFile.getAbsolutePath());
				}
				if (!inputRightFile.equals(rightEyeFile)) {
					FileUtil.moveFile(rightEyeFile, inputRightFile);
					// prevent cleanup
					rightEyeFile = inputRightFile;
					MediaStoreUtil.deleteThumbnail(inputRightFile.getAbsolutePath());
					MediaStoreUtil.addPictureToMediaStore(inputRightFile.getAbsolutePath());
				}
			}
			else if (photoFolder != null && photoFolder.isDirectory()) {
				FileUtil.moveFile(leftEyeFile, new File(photoFolder, leftEyeFile.getName()));
				FileUtil.moveFile(rightEyeFile, new File(photoFolder, rightEyeFile.getName()));
			}

			File organizeFolder = photoFolder == null ? FileUtil.getTempCameraFolder() : photoFolder;
			OrganizeNewPhotosActivity.startActivity(this, organizeFolder.getAbsolutePath(),
					lastRightLeft == RIGHT, NextAction.VIEW_IMAGES);
			finish();
			return;
		case CANCEL_AND_VIEW_IMAGES:
			stopPreview();
			cleanupTempFolder();

			ListFoldersForDisplayActivity.startActivity(this);
			finish();
			break;
		default:
			break;
		}
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
	 * Get all temp camera files and link them to the app.
	 *
	 * @return The list of existing temp files.
	 */
	private File[] getTempCameraFiles() {
		File[] existingFiles = FileUtil.getTempCameraFiles();
		boolean leftEyeFirst = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_eye_sequence_choice);

		if (existingFiles.length == 1) {
			if (leftEyeFirst) {
				leftEyeFile = existingFiles[0];
			}
			else {
				rightEyeFile = existingFiles[0];
			}
		}
		else if (existingFiles.length >= 2) {
			rightEyeFile = leftEyeFirst ? existingFiles[1] : existingFiles[0];
			leftEyeFile = leftEyeFirst ? existingFiles[0] : existingFiles[1];
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
		if (Parameters.FLASH_MODE_OFF.equals(currentFlashlightMode)) {
			flashlightButton.setBackgroundResource(R.drawable.circlebutton_noflash);
		}
		else if (Parameters.FLASH_MODE_ON.equals(currentFlashlightMode)) {
			flashlightButton.setBackgroundResource(R.drawable.circlebutton_flash);
		}
		else if (Parameters.FLASH_MODE_TORCH.equals(currentFlashlightMode)) {
			flashlightButton.setBackgroundResource(R.drawable.circlebutton_torch);
		}
		if (currentFlashlightMode != null) {
			if (currentAction == Action.TAKE_PHOTO) {
				setFlashlightMode(currentFlashlightMode);
			}
			else {
				setFlashlightMode(FLASHLIGHT_MODES[0]);
			}
		}
	}

	/**
	 * Set the flashlight mode.
	 *
	 * @param flashlightMode
	 *            The new flashlight mode.
	 */
	private void setFlashlightMode(final String flashlightMode) {
		if (camera != null) {
			Parameters parameters = camera.getParameters();
			if (parameters.getSupportedFlashModes().contains(flashlightMode)) {
				parameters.setFlashMode(flashlightMode);
			}
			camera.setParameters(parameters);
		}
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
				Parameters parameters = camera.getParameters();
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
				updateFlashlight();

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

				// Factor 8/3 due to 75% size of base overlay circle.
				// Math/min factor due to strange implementation in OverlayPinchImageView.
				defaultOverlayScaleFactor =
						((float) Math.min(layoutParams.width, layoutParams.height))
								/ Math.max(layoutParams.width, layoutParams.height) * 8 / 3; // MAGIC_NUMBER

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
			try {
				initPreview();
				if (currentAction == TAKE_PHOTO) {
					startPreview();
				}
			}
			catch (Exception e) {
				DialogUtil.displayError(CameraActivity.this, R.string.message_dialog_failed_to_open_camera, true);
			}
		}

		@Override
		public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
			Display display = getWindowManager().getDefaultDisplay();
			int angle;
			switch (display.getRotation()) {
			case Surface.ROTATION_0: // This is display orientation
				angle = 90; // MAGIC_NUMBER
				break;
			case Surface.ROTATION_90:
				angle = 0;
				break;
			case Surface.ROTATION_180:
				angle = 270; // MAGIC_NUMBER
				break;
			case Surface.ROTATION_270:
				angle = 180; // MAGIC_NUMBER
				break;
			default:
				angle = 0;
				break;
			}
			if (camera != null) {
				camera.setDisplayOrientation(angle);
			}
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

			JpegMetadata metadata = null;

			if (currentRightLeft == RIGHT) {
				newRightEyeFile = imageFile;
				if (inputRightFile != null) {
					// Keep metadata from input
					metadata = JpegSynchronizationUtil.getJpegMetadata(inputRightFile.getAbsolutePath());
				}
			}
			else {
				newLeftEyeFile = imageFile;
				if (inputLeftFile != null) {
					// Keep metadata from input
					metadata = JpegSynchronizationUtil.getJpegMetadata(inputLeftFile.getAbsolutePath());
				}
			}

			if (metadata == null) {
				metadata = new JpegMetadata();
				metadata.rightLeft = currentRightLeft;
				metadata.comment = "";
				metadata.organizeDate = new Date();
			}

			int overlayCircleRadius =
					CIRCLE_RADII[PreferenceUtil.getSharedPreferenceInt(R.string.key_internal_camera_circle_type, DEFAULT_CIRCLE_TYPE)];
			if (overlayCircleRadius > 0) {
				metadata.xCenter = 0.5f; // MAGIC_NUMBER
				metadata.yCenter = 0.5f; // MAGIC_NUMBER
				metadata.overlayScaleFactor = ((float) overlayCircleRadius) / CIRCLE_BITMAP_SIZE * defaultOverlayScaleFactor;
			}

			// save photo
			new SavePhotoTask(data, currentRightLeft, metadata).execute(imageFile);

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
		 * The metadata to be stored.
		 */
		private JpegMetadata metadata;

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
			this.data = data;
			this.rightLeft = rightLeft;
			this.metadata = metadata;
		}

		@Override
		protected File doInBackground(final File... imageFiles) {
			File imageFile = imageFiles[0];

			try {
				FileOutputStream fos = new FileOutputStream(imageFile.getAbsolutePath());

				fos.write(data);
				fos.close();

				if (metadata != null) {
					JpegSynchronizationUtil.storeJpegMetadata(imageFile.getAbsolutePath(), metadata);
				}
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
		CANCEL_AND_VIEW_IMAGES,
		/**
		 * Make an optional re-take of a photo.
		 */
		RE_TAKE_PHOTO
	}

}
