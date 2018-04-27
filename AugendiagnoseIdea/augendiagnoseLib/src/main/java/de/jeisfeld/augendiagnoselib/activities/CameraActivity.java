package de.jeisfeld.augendiagnoselib.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.activities.OrganizeNewPhotosActivity.NextAction;
import de.jeisfeld.augendiagnoselib.components.HeadsetPlugReceiver;
import de.jeisfeld.augendiagnoselib.components.OverlayPinchImageView;
import de.jeisfeld.augendiagnoselib.components.PinchImageView;
import de.jeisfeld.augendiagnoselib.util.AudioUtil;
import de.jeisfeld.augendiagnoselib.util.Camera1Handler;
import de.jeisfeld.augendiagnoselib.util.Camera2Handler;
import de.jeisfeld.augendiagnoselib.util.CameraHandler;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.DialogUtil.ConfirmDialogFragment.ConfirmDialogListener;
import de.jeisfeld.augendiagnoselib.util.OrientationManager;
import de.jeisfeld.augendiagnoselib.util.OrientationManager.OrientationListener;
import de.jeisfeld.augendiagnoselib.util.OrientationManager.ScreenOrientation;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.SystemUtil;
import de.jeisfeld.augendiagnoselib.util.TrackingUtil;
import de.jeisfeld.augendiagnoselib.util.TrackingUtil.Category;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto.RightLeft;
import de.jeisfeld.augendiagnoselib.util.imagefile.FileUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.ImageUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.JpegMetadata;
import de.jeisfeld.augendiagnoselib.util.imagefile.JpegSynchronizationUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.MediaStoreUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.PupilAndIrisDetector;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static de.jeisfeld.augendiagnoselib.activities.CameraActivity.Action.CANCEL_AND_VIEW_IMAGES;
import static de.jeisfeld.augendiagnoselib.activities.CameraActivity.Action.CHECK_PHOTO;
import static de.jeisfeld.augendiagnoselib.activities.CameraActivity.Action.FINISH_CAMERA;
import static de.jeisfeld.augendiagnoselib.activities.CameraActivity.Action.RE_TAKE_PHOTO;
import static de.jeisfeld.augendiagnoselib.activities.CameraActivity.Action.TAKE_PHOTO;
import static de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto.RightLeft.LEFT;
import static de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto.RightLeft.RIGHT;

/**
 * An activity to take pictures with the camera.
 */
public class CameraActivity extends StandardActivity {
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
	 * The request code for calling external camera app.
	 */
	private static final int REQUEST_CODE_CAMERA_APP = 6;

	/**
	 * The size of the circle overlay bitmap.
	 */
	private static final int CIRCLE_BITMAP_SIZE = OverlayPinchImageView.OVERLAY_SIZE;
	/**
	 * The maximum circle size.
	 */
	private static final int MAX_CIRCLE_RADIUS = 512;
	/**
	 * The minimum circle size.
	 */
	private static final int MIN_CIRCLE_RADIUS = 128;
	/**
	 * The default circle size.
	 */
	private static final int DEFAULT_CIRCLE_RADIUS = 384;

	/**
	 * Activity String used for tracking.
	 */
	private static final String CAMERA = "Camera";

	/**
	 * The available focus modes.
	 */
	private static List<FocusMode> mFocusModes;
	/**
	 * The used flashlight modes.
	 */
	private static List<FlashMode> mFlashlightModes;
	/**
	 * A flag indicating if zoom is available.
	 */
	private static boolean mIsZoomAvailable = false;

	/**
	 * The current rightLeft in the activity.
	 */
	private Action mCurrentAction;

	/**
	 * The current flashlight mode.
	 */
	private FlashMode mCurrentFlashlightMode;

	/**
	 * The current focus mode.
	 */
	private FocusMode mCurrentFocusMode;

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
	@Nullable
	private File mRightEyeFile = null;

	/**
	 * The temp file holding the next photo for the right eye.
	 */
	@Nullable
	private File mNewRightEyeFile = null;

	/**
	 * The temp file holding the left eye.
	 */
	@Nullable
	private File mLeftEyeFile = null;

	/**
	 * The temp file holding the next photo for the left eye.
	 */
	@Nullable
	private File mNewLeftEyeFile = null;

	/**
	 * The temp file holding the photo taken with external camera.
	 */
	@Nullable
	private File mNewExternalFile = null;

	/**
	 * The folder where to store the photos.
	 */
	@Nullable
	private File mPhotoFolder = null;

	/**
	 * The right eye file coming as input to the activity.
	 */
	@Nullable
	private File mInputRightFile = null;

	/**
	 * The left eye file coming as input to the activity.
	 */
	@Nullable
	private File mInputLeftFile = null;

	/**
	 * An orientation manager used to track the orientation of the image.
	 */
	@Nullable
	private OrientationManager mOrientationManager = null;

	/**
	 * The current screen orientation.
	 */
	private ScreenOrientation mCurrentScreenOrientation;

	/**
	 * The handler operating the camera.
	 */
	@Nullable
	private CameraHandler mCameraHandler;

	/**
	 * Timestamp for measuring the tracking duration.
	 */
	private long mTrackingTimestamp = 0;

	/**
	 * The receiver handling headset plugin.
	 */
	private HeadsetPlugReceiver mHeadsetPlugReceiver = null;

	/**
	 * Static helper method to start the activity for taking two photos to the input folder.
	 *
	 * @param activity    The activity from which the activity is started.
	 * @param photoFolder The folder where to store the photos.
	 */
	public static void startActivity(@NonNull final Activity activity, final String photoFolder) {
		Intent intent = new Intent(activity, CameraActivity.class);
		if (photoFolder != null) {
			intent.putExtra(STRING_EXTRA_PHOTOFOLDER, photoFolder);
		}
		activity.startActivity(intent);
	}

	/**
	 * Static helper method to start the activity for re-checking two images.
	 *
	 * @param activity   The activity from which the activity is started.
	 * @param photoRight The path of the right eye image
	 * @param photoLeft  The path of the left eye image
	 */
	public static void startActivity(@NonNull final Activity activity, final String photoRight, final String photoLeft) {
		Intent intent = new Intent(activity, CameraActivity.class);
		if (photoRight != null) {
			intent.putExtra(STRING_EXTRA_PHOTO_RIGHT, photoRight);
		}
		if (photoLeft != null) {
			intent.putExtra(STRING_EXTRA_PHOTO_LEFT, photoLeft);
		}
		activity.startActivity(intent);
	}

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (isCreationFailed()) {
			return;
		}

		int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

		if (permission == PackageManager.PERMISSION_GRANTED) {
			setupActivity();
		}
		// StandardActivity will request for permission. If permission is granted, then onRequestPermissionsResult will setup the activity.
	}

	/**
	 * Do the basic setup of the activity. This is basically the key part of the onCreate method.
	 */
	private void setupActivity() {
		setContentView(R.layout.activity_camera);

		setCameraHandler();

		configureMainButtons();
		configureExternalCameraButton();
		configureThumbButtons();
		configureZoomCircleButton();
		configureFlashlightButton(null);
		// Focus button is configured after callback from CameraHandler.

		final int screenAppearance =
				PreferenceUtil.getSharedPreferenceIntString(R.string.key_camera_screen_position, R.string.pref_default_camera_screen_position);
		if (screenAppearance > 0) {
			FrameLayout cameraOverallFrame = findViewById(R.id.camera_overall_frame);
			int offset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 8, getResources().getDisplayMetrics()); // MAGIC_NUMBER
			if (screenAppearance == 1) {
				// value 1: left offset
				cameraOverallFrame.setPadding(offset, 0, 0, 0);
			}
			else {
				// value 2: right offset
				cameraOverallFrame.setPadding(0, 0, offset, 0);
			}
		}

		String photoFolderName = getIntent().getStringExtra(STRING_EXTRA_PHOTOFOLDER);
		if (photoFolderName != null) {
			mPhotoFolder = new File(photoFolderName);
		}

		String inputRightFileName = getIntent().getStringExtra(STRING_EXTRA_PHOTO_RIGHT);
		String inputLeftFileName = getIntent().getStringExtra(STRING_EXTRA_PHOTO_LEFT);

		// Handle the different scenarios based on input and based on existing temp files.
		if (inputLeftFileName != null || inputRightFileName != null) {
			if (inputRightFileName != null) {
				mInputRightFile = new File(inputRightFileName);
			}
			if (inputLeftFileName != null) {
				mInputLeftFile = new File(inputLeftFileName);
			}

			// Triggered by OrganizeNewPhotosActivity to update photos.
			mPhotoFolder = mInputRightFile == null ? mInputLeftFile.getParentFile() : mInputRightFile.getParentFile();
			if (FileUtil.getTempCameraFolder().equals(mPhotoFolder)) {
				mPhotoFolder = null;
			}
			mLeftEyeFile = mInputLeftFile;
			if (mLeftEyeFile != null) {
				setThumbImage(mLeftEyeFile.getAbsolutePath(), LEFT);
			}
			mRightEyeFile = mInputRightFile;
			if (mRightEyeFile != null) {
				setThumbImage(mRightEyeFile.getAbsolutePath(), RIGHT);
			}

			if (mLeftEyeFile != null && mRightEyeFile != null) {
				setAction(RE_TAKE_PHOTO, null);
			}
			else if (mLeftEyeFile == null) {
				setAction(TAKE_PHOTO, LEFT);
			}
			else {
				setAction(TAKE_PHOTO, RIGHT);
			}
		}
		else {
			File[] existingFiles = getTempCameraFiles();

			if (existingFiles == null || existingFiles.length == 0 || mPhotoFolder != null) {
				// This is the standard scenario.
				boolean leftEyeFirst = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_eye_sequence_choice);
				setAction(TAKE_PHOTO, leftEyeFirst ? LEFT : RIGHT);
			}
			else if (existingFiles.length == 1) {
				// one file already there. Assume that this is already taken and we only have to take the other one
				if (mLeftEyeFile != null) {
					setThumbImage(mLeftEyeFile.getAbsolutePath(), LEFT);
					setAction(TAKE_PHOTO, RIGHT);
				}
				else {
					setThumbImage(mRightEyeFile.getAbsolutePath(), RIGHT);
					setAction(TAKE_PHOTO, LEFT);
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

		int overlayCircleSize = PreferenceUtil.getSharedPreferenceInt(R.string.key_internal_camera_circle_size, DEFAULT_CIRCLE_RADIUS);
		drawOverlayCircle(overlayCircleSize);

		mOrientationManager = new OrientationManager(this, SensorManager.SENSOR_DELAY_NORMAL, new OrientationListener() {
			@Override
			public void onOrientationChange(final ScreenOrientation screenOrientation) {
				switch (screenOrientation) {
				case LANDSCAPE:
				case REVERSED_LANDSCAPE:
					if (screenOrientation != mCurrentScreenOrientation) {
						mCurrentScreenOrientation = screenOrientation;
						realignViewElements(screenOrientation == ScreenOrientation.REVERSED_LANDSCAPE);
					}
					break;
				default:
					// do nothing
				}
			}
		});
		mOrientationManager.enable();
	}

	@Override
	public final void onDestroy() {
		cleanupTempFolder();
		// mCameraHandler.stopPreview();
		if (mOrientationManager != null) {
			mOrientationManager.disable();
		}
		super.onDestroy();
	}

	@Override
	public final void onResume() {
		super.onResume();
		if (mCameraHandler != null) {
			mCameraHandler.startPreview();
		}
		mTrackingTimestamp = System.currentTimeMillis();

		registerHeadsetReceiver();
	}

	@Override
	public final void onPause() {
		if (mHeadsetPlugReceiver != null) {
			mHeadsetPlugReceiver.unregister(this);
			mHeadsetPlugReceiver = null;
		}

		if (mCameraHandler != null) {
			mCameraHandler.stopPreview();
		}
		super.onPause();
		TrackingUtil.sendTiming(Category.TIME_USAGE, CAMERA, null, System.currentTimeMillis() - mTrackingTimestamp);
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
		final Button captureButton = findViewById(R.id.buttonCameraTrigger);
		captureButton.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(final View v) {
						// get an image from the camera
						captureButton.setEnabled(false);
						mCameraHandler.takePicture();
						TrackingUtil.sendEvent(Category.EVENT_USER, CAMERA, "Capture");
					}
				});

		// Add listeners to the accept/decline button
		Button acceptButton = findViewById(R.id.buttonCameraAccept);
		acceptButton.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(final View v) {
						// Analyze next required step
						if (mCurrentRightLeft == RIGHT) {
							if (mRightEyeFile != null && mRightEyeFile.exists()) {
								// noinspection ResultOfMethodCallIgnored
								mRightEyeFile.delete();
							}
							mRightEyeFile = mNewRightEyeFile;
							mNewRightEyeFile = null;
							mLastRightLeft = RIGHT;

							if (mLeftEyeFile == null) {
								setAction(TAKE_PHOTO, LEFT);
								PupilAndIrisDetector.determineAndStoreIrisPosition(mRightEyeFile.getAbsolutePath());
							}
							else {
								setAction(FINISH_CAMERA, null);
							}
						}
						else {
							if (mLeftEyeFile != null && mLeftEyeFile.exists()) {
								// noinspection ResultOfMethodCallIgnored
								mLeftEyeFile.delete();
							}
							mLeftEyeFile = mNewLeftEyeFile;
							mNewLeftEyeFile = null;
							mLastRightLeft = LEFT;

							if (mRightEyeFile == null) {
								setAction(TAKE_PHOTO, RIGHT);
								PupilAndIrisDetector.determineAndStoreIrisPosition(mLeftEyeFile.getAbsolutePath());
							}
							else {
								setAction(FINISH_CAMERA, null);
							}
						}
						TrackingUtil.sendEvent(Category.EVENT_USER, CAMERA, "Accept");
					}
				});

		Button declineButton = findViewById(R.id.buttonCameraDecline);
		declineButton.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(final View v) {
						if (mCurrentAction == CHECK_PHOTO) {
							if (mCurrentRightLeft == RIGHT) {
								if (mNewRightEyeFile != null && mNewRightEyeFile.exists()) {
									// noinspection ResultOfMethodCallIgnored
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
									// noinspection ResultOfMethodCallIgnored
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
						TrackingUtil.sendEvent(Category.EVENT_USER, CAMERA, "Decline");
					}
				});

		Button returnButton = findViewById(R.id.buttonCameraReturn);
		returnButton.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(final View v) {
						setAction(FINISH_CAMERA, null);
					}
				});


		Button viewImagesButton = findViewById(R.id.buttonCameraViewImages);
		if (getResources().getBoolean(R.bool.hide_button_view_images)) {
			// Hide application specific button "view images" if applicable
			viewImagesButton.setVisibility(GONE);
			viewImagesButton.setEnabled(false);
		}
		else {
			// Add a listener to the view image button
			viewImagesButton.setOnClickListener(
					new OnClickListener() {
						@Override
						public void onClick(final View v) {
							setAction(CANCEL_AND_VIEW_IMAGES, null);
						}
					});
		}
	}

	/**
	 * Configure the button for calling external camera app.
	 */
	private void configureExternalCameraButton() {
		Button externalCameraButton = findViewById(R.id.buttonCameraExternal);
		if (PreferenceUtil.getSharedPreferenceBoolean(R.string.key_enable_external_camera)) {
			externalCameraButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					mNewExternalFile = FileUtil.getTempJpegFile();

					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
						takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mNewExternalFile));
					}
					else {
						Uri photoUri = FileProvider.getUriForFile(getApplicationContext(),
								getApplicationContext().getPackageName() + ".fileprovider", mNewExternalFile);
						takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
					}
					takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
					if (takePictureIntent.resolveActivity(getApplicationContext().getPackageManager()) == null) {
						PreferenceUtil.setSharedPreferenceBoolean(R.string.key_enable_external_camera, false);
						findViewById(R.id.buttonCameraExternal).setVisibility(GONE);
					}
					else {
						startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA_APP);
					}
				}
			});
		}
	}

	/**
	 * Configure configuration buttons in this activity.
	 */
	private void configureThumbButtons() {

		LinearLayout cameraThumbRight = findViewById(R.id.camera_thumb_layout_right);
		cameraThumbRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				setAction(TAKE_PHOTO, RIGHT);
			}
		});

		LinearLayout cameraThumbLeft = findViewById(R.id.camera_thumb_layout_left);
		cameraThumbLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				setAction(TAKE_PHOTO, LEFT);
			}
		});

	}

	/**
	 * Configure the button for setting the overlay circle size.
	 */
	private void configureZoomCircleButton() {
		Button overlayCircleButton = findViewById(R.id.buttonCameraZoomOverlayCircle);
		overlayCircleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				boolean isVisible = !PreferenceUtil.getSharedPreferenceBoolean(R.string.key_internal_camera_zoom_circle_seekbar_visibility);
				PreferenceUtil.setSharedPreferenceBoolean(R.string.key_internal_camera_zoom_circle_seekbar_visibility, isVisible);
				findViewById(R.id.seekbarCameraOverlayCircle).setVisibility(isVisible ? VISIBLE : INVISIBLE);
				if (mIsZoomAvailable) {
					findViewById(R.id.seekbarCameraZoom).setVisibility(isVisible ? VISIBLE : INVISIBLE);
				}
			}
		});
		boolean isVisible = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_internal_camera_zoom_circle_seekbar_visibility);
		findViewById(R.id.seekbarCameraOverlayCircle).setVisibility(isVisible ? VISIBLE : INVISIBLE);
		if (mIsZoomAvailable) {
			findViewById(R.id.seekbarCameraZoom).setVisibility(isVisible ? VISIBLE : INVISIBLE);
		}

		SeekBar overlayCircleSeekbar = findViewById(R.id.seekbarCameraOverlayCircle);
		overlayCircleSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(final SeekBar seekBar) {
				// do nothing.
			}

			@Override
			public void onStartTrackingTouch(final SeekBar seekBar) {
				// do nothing.
			}

			@Override
			public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
				int size = (int) ((float) progress / seekBar.getMax() * MAX_CIRCLE_RADIUS);
				if (size < MIN_CIRCLE_RADIUS) {
					size = 0;
				}
				if (fromUser) {
					PreferenceUtil.setSharedPreferenceInt(R.string.key_internal_camera_circle_size, size);
				}
				drawOverlayCircle(size);
			}
		});
		int overlayCircleSize = PreferenceUtil.getSharedPreferenceInt(R.string.key_internal_camera_circle_size, DEFAULT_CIRCLE_RADIUS);
		overlayCircleSeekbar.setProgress((int) ((float) overlayCircleSize / MAX_CIRCLE_RADIUS * overlayCircleSeekbar.getMax()));

		SeekBar zoomSeekbar = findViewById(R.id.seekbarCameraZoom);
		zoomSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(final SeekBar seekBar) {
				// do nothing.
			}

			@Override
			public void onStartTrackingTouch(final SeekBar seekBar) {
				// do nothing.
			}

			@Override
			public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
				PreferenceUtil.setSharedPreferenceInt(R.string.key_internal_camera_zoom_seekbar_progress, progress);
				float relativeProgress = (float) progress / seekBar.getMax();
				mCameraHandler.setRelativeZoom(relativeProgress);
			}
		});
		zoomSeekbar.setProgress(PreferenceUtil.getSharedPreferenceInt(R.string.key_internal_camera_zoom_seekbar_progress, 0));
	}

	/**
	 * Configure the button for setting flashlight.
	 *
	 * @param flashModes The list of available flash modes.
	 */
	private void configureFlashlightButton(final List<FlashMode> flashModes) {
		Button flashlightButton = findViewById(R.id.buttonCameraFlashlight);
		determineAvailableFlashlightModes(flashModes);

		if (mFlashlightModes.size() > 1) {
			String storedFlashlightString = PreferenceUtil.getSharedPreferenceString(R.string.key_internal_camera_flashlight_mode);
			FlashMode storedFlashlightMode;
			try {
				storedFlashlightMode = FlashMode.valueOf(storedFlashlightString);
			}
			catch (Exception e) {
				storedFlashlightMode = FlashMode.OFF;
				PreferenceUtil.setSharedPreferenceString(R.string.key_internal_camera_flashlight_mode, storedFlashlightMode.toString());
			}
			if (!mFlashlightModes.contains(storedFlashlightMode)) {
				storedFlashlightMode = FlashMode.OFF;
				PreferenceUtil.setSharedPreferenceString(R.string.key_internal_camera_flashlight_mode, storedFlashlightMode.toString());
			}

			setFlashlightMode(storedFlashlightMode);
			flashlightButton.setVisibility(VISIBLE);

			flashlightButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					if (mFlashlightModes.size() > 0) {
						int flashlightModeIndex = mFlashlightModes.indexOf(mCurrentFlashlightMode);
						flashlightModeIndex = (flashlightModeIndex + 1) % mFlashlightModes.size();
						FlashMode newFlashlightMode = mFlashlightModes.get(flashlightModeIndex);
						PreferenceUtil.setSharedPreferenceString(R.string.key_internal_camera_flashlight_mode, newFlashlightMode.toString());

						setFlashlightMode(newFlashlightMode);
					}
				}
			});
		}
		else {
			PreferenceUtil.setSharedPreferenceString(R.string.key_internal_camera_flashlight_mode, FlashMode.OFF.toString());
			setFlashlightMode(FlashMode.OFF);
			flashlightButton.setVisibility(GONE);
		}
	}

	/**
	 * Configure the button for setting focus.
	 */
	private void configureFocusButton() {
		String storedFocusModeString = PreferenceUtil.getSharedPreferenceString(R.string.key_internal_camera_focus_mode);
		FocusMode storedFocusMode;
		try {
			storedFocusMode = FocusMode.valueOf(storedFocusModeString);
		}
		catch (Exception e) {
			storedFocusMode = FocusMode.MACRO;
			PreferenceUtil.setSharedPreferenceString(R.string.key_internal_camera_focus_mode, storedFocusMode.toString());
		}
		if (!mFocusModes.contains(storedFocusMode)) {
			if (mFocusModes.contains(FocusMode.AUTO)) {
				storedFocusMode = FocusMode.AUTO;
				PreferenceUtil.setSharedPreferenceString(R.string.key_internal_camera_focus_mode, storedFocusMode.toString());
			}
			else if (mFocusModes.contains(FocusMode.FIXED)) {
				storedFocusMode = FocusMode.FIXED;
				PreferenceUtil.setSharedPreferenceString(R.string.key_internal_camera_focus_mode, storedFocusMode.toString());
			}
			else if (mFocusModes.size() > 0) {
				storedFocusMode = mFocusModes.get(0);
				PreferenceUtil.setSharedPreferenceString(R.string.key_internal_camera_focus_mode, storedFocusMode.toString());
			}
			else {
				storedFocusMode = null;
				PreferenceUtil.removeSharedPreference(R.string.key_internal_camera_focus_mode);
			}
		}

		setFocusMode(storedFocusMode);

		Button focusButton = findViewById(R.id.buttonCameraFocus);

		if (mFocusModes.size() < 2) {
			focusButton.setVisibility(GONE);
			return;
		}

		focusButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (mFocusModes.size() > 0) {
					int focusModeIndex = mFocusModes.indexOf(mCurrentFocusMode);
					focusModeIndex = (focusModeIndex + 1) % mFocusModes.size();
					FocusMode newFocusMode = mFocusModes.get(focusModeIndex);
					PreferenceUtil.setSharedPreferenceString(R.string.key_internal_camera_focus_mode, newFocusMode.toString());

					setFocusMode(newFocusMode);
				}
			}
		});

		SeekBar focusSeekbar = findViewById(R.id.seekbarCameraFocus);
		focusSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(final SeekBar seekBar) {
				// do nothing.
			}

			@Override
			public void onStartTrackingTouch(final SeekBar seekBar) {
				// do nothing.
			}

			@Override
			public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
				if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && isCamera2()) {
					Camera2Handler cameraHandler = (Camera2Handler) mCameraHandler;
					PreferenceUtil.setSharedPreferenceInt(R.string.key_internal_camera_focal_distance_seekbar_progress, progress);

					float relativeProgress = (float) progress / seekBar.getMax();

					cameraHandler.setRelativeFocalDistance(1 - relativeProgress);
				}
			}
		});
		focusSeekbar.setProgress(PreferenceUtil.getSharedPreferenceInt(R.string.key_internal_camera_focal_distance_seekbar_progress, 0));
	}

	/**
	 * Register the receiver waiting for headset plugged.
	 */
	private void registerHeadsetReceiver() {
		boolean enableExternalFlash = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_enable_flash_ext);
		if (enableExternalFlash) {
			mHeadsetPlugReceiver = new HeadsetPlugReceiver();
			mHeadsetPlugReceiver.register(this, new HeadsetPlugReceiver.HeadsetPlugHandler() {
				@Override
				public void handleHeadsetPlug(final boolean plugged) {
					try {
						configureFlashlightButton(mFlashlightModes);
					}
					catch (Exception e) {
						TrackingUtil.sendException("hea1", e);
						return;
					}
					if (plugged && mCurrentFlashlightMode != FlashMode.EXT) {
						DialogUtil.displayConfirmationMessage(CameraActivity.this, new ConfirmDialogListener() {
							private static final long serialVersionUID = 1L;

							@Override
							public void onDialogPositiveClick(final DialogFragment dialog) {
								PreferenceUtil.setSharedPreferenceString(R.string.key_internal_camera_flashlight_mode, FlashMode.EXT.toString());
								setFlashlightMode(FlashMode.EXT);
								DialogUtil.displayTip(CameraActivity.this, R.string.message_tip_external_flash, R.string.key_tip_external_flash);
							}

							@Override
							public void onDialogNegativeClick(final DialogFragment dialog) {
								DialogUtil.displayTip(CameraActivity.this,
										R.string.message_tip_external_flash_pref, R.string.key_tip_external_flash_pref);
							}
						}, R.string.button_external_flash, R.string.message_dialog_confirm_external_flash);
					}
				}
			});
		}
	}

	/**
	 * Change to the given action.
	 *
	 * @param action    The new action.
	 * @param rightLeft the next eye side.
	 */
	private void setAction(@NonNull final Action action, final RightLeft rightLeft) {
		mCurrentAction = action;
		mCurrentRightLeft = rightLeft;

		LinearLayout cameraThumbRight = findViewById(R.id.camera_thumb_layout_right);
		LinearLayout cameraThumbLeft = findViewById(R.id.camera_thumb_layout_left);
		Button buttonCapture = findViewById(R.id.buttonCameraTrigger);
		Button buttonCameraApp = findViewById(R.id.buttonCameraExternal);
		Button buttonAccept = findViewById(R.id.buttonCameraAccept);
		Button buttonDecline = findViewById(R.id.buttonCameraDecline);
		Button buttonReturn = findViewById(R.id.buttonCameraReturn);
		Button buttonViewImages = findViewById(R.id.buttonCameraViewImages);
		ImageView imageViewReview = findViewById(R.id.camera_review);
		FrameLayout cameraPreviewFrame = findViewById(R.id.camera_preview_frame);
		LinearLayout cameraSettingsLayout = findViewById(R.id.cameraSettingsLayout);

		switch (action) {
		case TAKE_PHOTO:
			updateFlashlight();
			mCameraHandler.startPreview();
			buttonCapture.setVisibility(VISIBLE);
			buttonCapture.setEnabled(true);
			buttonCameraApp.setVisibility(PreferenceUtil.getSharedPreferenceBoolean(R.string.key_enable_external_camera) ? VISIBLE : GONE);
			buttonAccept.setVisibility(GONE);
			buttonDecline.setVisibility(GONE);
			buttonReturn.setVisibility(mLeftEyeFile == null && mRightEyeFile == null ? GONE : VISIBLE);
			buttonViewImages.setVisibility(buttonViewImages.isEnabled() && buttonReturn.getVisibility() == GONE ? VISIBLE : GONE);
			cameraSettingsLayout.setVisibility(VISIBLE);
			imageViewReview.setVisibility(GONE);
			cameraPreviewFrame.setVisibility(VISIBLE);
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
			buttonCapture.setVisibility(GONE);
			buttonCameraApp.setVisibility(GONE);
			buttonAccept.setVisibility(VISIBLE);
			buttonDecline.setVisibility(VISIBLE);
			buttonReturn.setVisibility(GONE);
			buttonViewImages.setVisibility(GONE);
			cameraSettingsLayout.setVisibility(INVISIBLE);
			imageViewReview.setVisibility(VISIBLE);
			cameraPreviewFrame.setVisibility(GONE);
			cameraThumbLeft.setEnabled(false);
			cameraThumbRight.setEnabled(false);
			updateFlashlight();
			break;
		case RE_TAKE_PHOTO:
			buttonCapture.setVisibility(GONE);
			buttonCameraApp.setVisibility(GONE);
			buttonAccept.setVisibility(GONE);
			buttonDecline.setVisibility(GONE);
			buttonReturn.setVisibility(VISIBLE);
			buttonViewImages.setVisibility(GONE);
			cameraSettingsLayout.setVisibility(VISIBLE);
			imageViewReview.setVisibility(GONE);
			cameraPreviewFrame.setVisibility(VISIBLE);
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
			if (mInputLeftFile != null || mInputRightFile != null) {
				if (mLeftEyeFile != null && !mLeftEyeFile.equals(mInputLeftFile)) {
					File newLeftFile = mInputLeftFile == null ? new File(mInputRightFile.getParentFile(), mLeftEyeFile.getName()) : mInputLeftFile;
					FileUtil.moveFile(mLeftEyeFile, newLeftFile);
					// prevent cleanup
					mLeftEyeFile = newLeftFile;
					MediaStoreUtil.deleteThumbnail(newLeftFile.getAbsolutePath());
					MediaStoreUtil.addPictureToMediaStore(newLeftFile.getAbsolutePath());
				}
				if (mRightEyeFile != null && !mRightEyeFile.equals(mInputRightFile)) {
					File newRightFile = mInputRightFile == null ? new File(mInputLeftFile.getParentFile(), mRightEyeFile.getName()) : mInputRightFile;
					FileUtil.moveFile(mRightEyeFile, newRightFile);
					// prevent cleanup
					mRightEyeFile = newRightFile;
					MediaStoreUtil.deleteThumbnail(newRightFile.getAbsolutePath());
					MediaStoreUtil.addPictureToMediaStore(newRightFile.getAbsolutePath());
				}
			}
			else if (mPhotoFolder != null && mPhotoFolder.isDirectory()) {
				if (mLeftEyeFile != null) {
					FileUtil.moveFile(mLeftEyeFile, new File(mPhotoFolder, mLeftEyeFile.getName()));
				}
				if (mRightEyeFile != null) {
					FileUtil.moveFile(mRightEyeFile, new File(mPhotoFolder, mRightEyeFile.getName()));
				}
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
	 * @param flashlightMode The new flashlight mode.
	 */
	private void setFlashlightMode(final FlashMode flashlightMode) {
		mCurrentFlashlightMode = flashlightMode;
		updateFlashlight();
	}

	/**
	 * Update the focus mode.
	 *
	 * @param focusMode The new focus mode.
	 */
	private void setFocusMode(final FocusMode focusMode) {
		mCurrentFocusMode = focusMode;

		SeekBar seekbarCameraFocus = findViewById(R.id.seekbarCameraFocus);
		seekbarCameraFocus.setVisibility(mCurrentFocusMode == FocusMode.MANUAL ? VISIBLE : GONE);

		mCameraHandler.setFocusMode(mCurrentFocusMode);

		Button buttonCameraFocus = findViewById(R.id.buttonCameraFocus);

		if (mCurrentFocusMode == null) {
			buttonCameraFocus.setVisibility(GONE);
		}
		else {
			buttonCameraFocus.setText(mCurrentFocusMode.toDisplayString());
		}
	}

	/**
	 * Remove unused files from the temp folder.
	 */
	private void cleanupTempFolder() {
		File[] tempFiles = FileUtil.getTempCameraFiles();
		for (File file : tempFiles) {
			if (!file.equals(mRightEyeFile) & !file.equals(mLeftEyeFile)) {
				// noinspection ResultOfMethodCallIgnored
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
		RightLeft rightLeft = existingFiles.length == 0 ? null : new EyePhoto(existingFiles[0]).getRightLeft();
		boolean firstFileIsLeftEye = rightLeft == LEFT || rightLeft == null && leftEyeFirst;

		if (existingFiles.length == 1) {
			if (firstFileIsLeftEye) {
				mLeftEyeFile = existingFiles[0];
			}
			else {
				mRightEyeFile = existingFiles[0];
			}
		}
		else if (existingFiles.length >= 2) {
			mRightEyeFile = firstFileIsLeftEye ? existingFiles[1] : existingFiles[0];
			mLeftEyeFile = firstFileIsLeftEye ? existingFiles[0] : existingFiles[1];
		}

		return existingFiles;
	}

	/**
	 * Set the thumb image with a byte array.
	 *
	 * @param data The data representing the bitmap.
	 */
	private void setThumbImage(@NonNull final byte[] data) {
		ImageView imageView = findViewById(mCurrentRightLeft == RIGHT ? R.id.camera_thumb_image_right : R.id.camera_thumb_image_left);

		Bitmap bitmap = ImageUtil.getImageBitmap(data, getResources().getDimensionPixelSize(R.dimen.camera_thumb_size));
		if (mCurrentScreenOrientation == ScreenOrientation.REVERSED_LANDSCAPE) {
			bitmap = ImageUtil.rotateBitmap(bitmap, ExifInterface.ORIENTATION_ROTATE_180);
		}

		imageView.setImageBitmap(bitmap);
	}

	/**
	 * Set the thumb image from a file.
	 *
	 * @param file      The file to be put in the thumb.
	 * @param rightLeft The side of the eye
	 */
	private void setThumbImage(@Nullable final String file, final RightLeft rightLeft) {
		ImageView imageView = findViewById(rightLeft == RIGHT ? R.id.camera_thumb_image_right : R.id.camera_thumb_image_left);

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
	 * @param data The data representing the image.
	 */
	private void setReviewImage(@NonNull final byte[] data) {
		PinchImageView imageView = findViewById(R.id.camera_review);

		Bitmap bitmap = ImageUtil.getImageBitmap(data, findViewById(R.id.camera_preview_frame).getWidth());
		if (mCurrentScreenOrientation == ScreenOrientation.REVERSED_LANDSCAPE) {
			bitmap = ImageUtil.rotateBitmap(bitmap, ExifInterface.ORIENTATION_ROTATE_180);
		}


		imageView.setImage(bitmap);
	}

	/**
	 * Show the captured image for preview as fixed image.
	 *
	 * @param file the image file
	 */
	private void setReviewImage(@NonNull final File file) {
		PinchImageView imageView = findViewById(R.id.camera_review);

		Bitmap bitmap = ImageUtil.getImageBitmap(file.getAbsolutePath(), findViewById(R.id.camera_preview_frame).getWidth());

		imageView.setImage(bitmap);
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
				// do nothing
			}

			@Override
			public void onAnimationRepeat(final Animation animation) {
				// do nothing
			}

			@Override
			public void onAnimationEnd(final Animation animation) {
				flashView.setVisibility(GONE);
			}
		});

		AnimationSet animation = new AnimationSet(false);
		animation.addAnimation(fadeOut);

		flashView.setVisibility(VISIBLE);
		flashView.startAnimation(animation);
	}

	/**
	 * Draw the overlay circle.
	 *
	 * @param circleRadius The circle radius.
	 */
	private void drawOverlayCircle(final int circleRadius) {
		Bitmap overlayBitmap = Bitmap.createBitmap(CIRCLE_BITMAP_SIZE, CIRCLE_BITMAP_SIZE, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(overlayBitmap);

		if (circleRadius > 0) {
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			int overlayColor = PreferenceUtil.getSharedPreferenceInt(R.string.key_overlay_color, Color.RED);
			paint.setColor(overlayColor);
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(5); // MAGIC_NUMBER

			canvas.drawCircle(CIRCLE_BITMAP_SIZE / 2, CIRCLE_BITMAP_SIZE / 2, circleRadius, paint);
		}

		ImageView overlayView = findViewById(R.id.camera_overlay);
		overlayView.setImageBitmap(overlayBitmap);
	}

	/**
	 * Update the flashlight button and set the flashlight mode.
	 */
	private void updateFlashlight() {
		Button flashlightButton = findViewById(R.id.buttonCameraFlashlight);
		if (FlashMode.OFF.equals(mCurrentFlashlightMode)) {
			flashlightButton.setBackgroundResource(R.drawable.circlebutton_noflash);
		}
		else if (FlashMode.ON.equals(mCurrentFlashlightMode)) {
			flashlightButton.setBackgroundResource(R.drawable.circlebutton_flash);
		}
		else if (FlashMode.TORCH.equals(mCurrentFlashlightMode)) {
			flashlightButton.setBackgroundResource(R.drawable.circlebutton_torch);
		}
		else if (FlashMode.EXT.equals(mCurrentFlashlightMode)) {
			flashlightButton.setBackgroundResource(R.drawable.circlebutton_flash_ext);
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
		case REVERSED_LANDSCAPE:
			return ExifInterface.ORIENTATION_ROTATE_180;
		default:
			return ExifInterface.ORIENTATION_NORMAL;
		}
	}

	/**
	 * The callback called when pictures are taken.
	 */
	@Nullable
	private final CameraCallback mOnPictureTakenHandler = new CameraCallback() {
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
		public void onPictureTaken(@NonNull final byte[] data) {
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
				metadata.setRightLeft(mCurrentRightLeft);
				metadata.setComment("");
				metadata.setOrganizeDate(new Date());
			}
			metadata.setOrientation(exifAngle);

			int overlayCircleRadius = PreferenceUtil.getSharedPreferenceInt(R.string.key_internal_camera_circle_size, DEFAULT_CIRCLE_RADIUS);
			if (overlayCircleRadius > 0) {
				metadata.setXCenter(0.5f); // MAGIC_NUMBER
				metadata.setYCenter(0.5f); // MAGIC_NUMBER
				metadata.setOverlayScaleFactor(((float) overlayCircleRadius) / CIRCLE_BITMAP_SIZE * getDefaultOverlayScaleFactor());
				metadata.addFlag(JpegMetadata.FLAG_OVERLAY_SET_BY_CAMERA_ACTIVITY);
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

		@Override
		public void onCameraError(final String message, final String shortMessage, @Nullable final Throwable e) {
			String messageString = message;
			if (e == null) {
				Log.e(Application.TAG, message);
			}
			else {
				Log.e(Application.TAG, message, e);
				messageString += "\n" + e.toString();
			}

			boolean isCamera2Api = mCameraHandler instanceof Camera2Handler;
			boolean wasCamera2Successful = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_internal_camera2_successful);

			if (isCamera2Api && !wasCamera2Successful) {
				// Reconfigure to Camera 1 API
				PreferenceUtil.setSharedPreferenceIntString(R.string.key_camera_api_version, 1);
				DialogUtil.displayError(CameraActivity.this, R.string.message_dialog_failed_to_use_camera2, true, messageString);
			}
			else {
				DialogUtil.displayError(CameraActivity.this, R.string.message_dialog_failed_to_access_camera, true, messageString);
			}

			if (e != null) {
				TrackingUtil.sendException(shortMessage, e);
			}
		}

		@Override
		public void updateAvailableFocusModes(final List<FocusMode> focusModes) {
			mFocusModes = focusModes;
			configureFocusButton();
		}

		@Override
		public void updateAvailableFlashModes(final List<FlashMode> flashModes) {
			configureFlashlightButton(flashModes);
		}

		@Override
		public void updateAvailableZoom(final boolean isZoomAvailable) {
			mIsZoomAvailable = isZoomAvailable;
			configureZoomCircleButton();
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
	 * Get the list of available flashlight modes.
	 *
	 * @param flashModes The list of available flash modes.
	 */
	private void determineAvailableFlashlightModes(final List<FlashMode> flashModes) {
		boolean enableFlashlight = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_enable_flash);
		boolean enableExternalFlash = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_enable_flash_ext);
		List<FlashMode> flashlightModes = new ArrayList<>();

		flashlightModes.add(FlashMode.OFF);
		if (enableExternalFlash && AudioUtil.isHeadphonePlugged()) {
			flashlightModes.add(FlashMode.EXT);
		}

		if (flashModes == null) {
			if (SystemUtil.hasFlashlight()) {
				if (enableFlashlight) {
					flashlightModes.add(FlashMode.ON);
				}
				flashlightModes.add(FlashMode.TORCH);
			}
		}
		else {
			if (flashModes.contains(FlashMode.ON) && enableFlashlight) {
				flashlightModes.add(FlashMode.ON);
			}
			if (flashModes.contains(FlashMode.TORCH)) {
				flashlightModes.add(FlashMode.TORCH);
			}
		}

		mFlashlightModes = flashlightModes;
	}

	/**
	 * Set the camera handler.
	 */
	private void setCameraHandler() {
		SurfaceView camera1View = findViewById(R.id.camera1_preview);
		TextureView camera2View = findViewById(R.id.camera2_preview);
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && isCamera2()) {
			mCameraHandler = new Camera2Handler(this, (FrameLayout) findViewById(R.id.camera_preview_frame), camera2View, mOnPictureTakenHandler);
			camera1View.setVisibility(GONE);
			camera2View.setVisibility(VISIBLE);
		}
		else {
			mCameraHandler = new Camera1Handler((FrameLayout) findViewById(R.id.camera_preview_frame), camera1View, mOnPictureTakenHandler);
			camera1View.setVisibility(VISIBLE);
			camera2View.setVisibility(GONE);
		}
	}

	/**
	 * Realign the view elements for landscape vs. reverseLandscape
	 *
	 * @param isReverseLandscape true if for reverse landscape
	 */
	private void realignViewElements(final boolean isReverseLandscape) {
		int rotation = isReverseLandscape ? 180 : 0; // MAGIC_NUMBER

		LinearLayout cameraThumbRight = findViewById(R.id.camera_thumb_layout_right);
		cameraThumbRight.setRotation(rotation);
		FrameLayout.LayoutParams frameParams = (FrameLayout.LayoutParams) cameraThumbRight.getLayoutParams();
		frameParams.gravity = isReverseLandscape ? Gravity.RIGHT | Gravity.BOTTOM : Gravity.LEFT | Gravity.TOP;
		cameraThumbRight.setLayoutParams(frameParams);

		LinearLayout cameraThumbLeft = findViewById(R.id.camera_thumb_layout_left);
		cameraThumbLeft.setRotation(rotation);
		frameParams = (FrameLayout.LayoutParams) cameraThumbLeft.getLayoutParams();
		frameParams.gravity = isReverseLandscape ? Gravity.LEFT | Gravity.BOTTOM : Gravity.RIGHT | Gravity.TOP;
		cameraThumbLeft.setLayoutParams(frameParams);

		LinearLayout cameraButtonsRight = findViewById(R.id.camera_buttons_layout_right);
		cameraButtonsRight.setRotation(rotation);
		frameParams = (FrameLayout.LayoutParams) cameraButtonsRight.getLayoutParams();
		frameParams.gravity = isReverseLandscape ? Gravity.RIGHT | Gravity.TOP : Gravity.RIGHT | Gravity.BOTTOM;
		cameraButtonsRight.setLayoutParams(frameParams);

		LinearLayout cameraSettings = findViewById(R.id.cameraSettingsLayout);
		cameraSettings.setRotation(rotation);
		frameParams = (FrameLayout.LayoutParams) cameraSettings.getLayoutParams();
		frameParams.gravity = isReverseLandscape ? Gravity.LEFT | Gravity.TOP : Gravity.LEFT | Gravity.BOTTOM;
		cameraSettings.setLayoutParams(frameParams);

		realignLinearLayout(R.id.layoutCameraFocus, isReverseLandscape, R.id.buttonCameraFocus, R.id.seekbarCameraFocus);
		realignLinearLayout(R.id.layoutCameraZoom, isReverseLandscape, R.id.buttonCameraZoomOverlayCircle, R.id.layoutCameraZoomSeekbars);
		((LinearLayout) findViewById(R.id.cameraSettingsLayout)).setGravity(isReverseLandscape ? Gravity.RIGHT : Gravity.LEFT);

		findViewById(R.id.buttonCameraAccept).setRotation(rotation);
		findViewById(R.id.buttonCameraTrigger).setRotation(rotation);
		findViewById(R.id.camera_review).setRotation(rotation);
	}

	/**
	 * Align elements of a linear layout to either original sequence or reverted sequence.
	 *
	 * @param layoutId The id of the linear layout.
	 * @param reverse  true for reverting the order of children.
	 * @param childIds The ids of the children.
	 */
	private void realignLinearLayout(final int layoutId, final boolean reverse, final int... childIds) {
		View[] childViews = new View[childIds.length];
		for (int i = 0; i < childIds.length; i++) {
			childViews[i] = findViewById(childIds[i]);
		}
		LinearLayout layout = findViewById(layoutId);
		layout.removeAllViews();
		if (reverse) {
			for (int i = childViews.length - 1; i >= 0; i--) {
				if (childViews[i] != null) {
					layout.addView(childViews[i]);
				}
			}
		}
		else {
			for (int i = 0; i < childViews.length; i++) {
				if (childViews[i] != null) {
					layout.addView(childViews[i]);
				}
			}
		}
	}


	/**
	 * Get information if Camera2 API is used.
	 *
	 * @return true if Camera2 API is used.
	 */
	private boolean isCamera2() {
		int cameraApiVersion = PreferenceUtil.getSharedPreferenceIntString(R.string.key_camera_api_version, null);
		return cameraApiVersion == 2;
	}

	@SuppressLint("InlinedApi")
	@Override
	protected final String[] getRequiredPermissions() {
		return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
	}

	@Override
	protected final int getPermissionInfoResource() {
		return R.string.message_dialog_confirm_need_camera_permission;
	}

	@Override
	public final void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
		if (requestCode == REQUEST_CODE_PERMISSION) {
			// If request is cancelled, the result arrays are empty.
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				setupActivity();
			}
			else {
				finish();
			}
		}
	}

	@Override
	protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (requestCode == REQUEST_CODE_CAMERA_APP) {
			if (resultCode == RESULT_OK) {
				JpegMetadata metadata = new JpegMetadata();
				metadata.setRightLeft(mCurrentRightLeft);
				metadata.setComment("");
				metadata.setOrganizeDate(new Date());
				JpegSynchronizationUtil.storeJpegMetadata(mNewExternalFile.getAbsolutePath(), metadata);

				if (mCurrentRightLeft == RIGHT) {
					mNewRightEyeFile = mNewExternalFile;
				}
				else {
					mNewLeftEyeFile = mNewExternalFile;
				}

				setThumbImage(mNewExternalFile.getAbsolutePath(), mCurrentRightLeft);
				setReviewImage(mNewExternalFile);
				setAction(CHECK_PHOTO, mCurrentRightLeft);

			}
			else {
				Log.w(Application.TAG, "Did not successfully capture picture");
				finish();
			}
		}
	}

	/**
	 * The task responsible for saving the picture.
	 */
	private static final class SavePhotoTask extends AsyncTask<File, String, File> {
		/**
		 * The data to be saved.
		 */
		private final byte[] mImageData;

		/**
		 * The side of the eye to be saved.
		 */
		private final RightLeft mRightLeft;

		/**
		 * The metadata to be stored.
		 */
		private final JpegMetadata mMetadata;

		/**
		 * Constructor, passing the data to be saved.
		 *
		 * @param data      The data to be saved.
		 * @param rightLeft The side of the eye to be saved.
		 * @param metadata  Metadata to be stored in the photo.
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
		protected void onPostExecute(@NonNull final File imageFile) {
			Log.d(Application.TAG, "Finished saving image " + imageFile.getName() + " - " + mRightLeft);
		}
	}

	/**
	 * Handler called after the picture is taken.
	 */
	public interface CameraCallback {
		/**
		 * Callback called just when the picture is taken.
		 */
		void onTakingPicture();

		/**
		 * Callback called after the picture is taken.
		 *
		 * @param data The image data.
		 */
		void onPictureTaken(byte[] data);

		/**
		 * Callback called on fatal camera errors.
		 *
		 * @param message      The error message as String
		 * @param shortMessage a short form of the message (for analytics).
		 * @param e            The exception
		 */
		void onCameraError(String message, String shortMessage, Throwable e);

		/**
		 * Give information which focus modes are supported by the camera.
		 *
		 * @param focusModes The supported focus modes.
		 */
		void updateAvailableFocusModes(List<FocusMode> focusModes);

		/**
		 * Give information which flash modes are supported by the camera.
		 *
		 * @param flashModes The supported flash modes.
		 */
		void updateAvailableFlashModes(List<FlashMode> flashModes);

		/**
		 * Give information if zoom is available.
		 *
		 * @param isZoomAvailable true if zoom is available.
		 */
		void updateAvailableZoom(boolean isZoomAvailable);
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
	public enum FlashMode {
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
		TORCH,
		/**
		 * The external flash is used.
		 */
		EXT
	}

	/**
	 * Enumeration for modes of the camera focus.
	 */
	public enum FocusMode {
		/**
		 * Continuous autofocus.
		 */
		CONTINUOUS,
		/**
		 * Autofocus.
		 */
		AUTO,
		/**
		 * Macro.
		 */
		MACRO,
		/**
		 * Manual focus.
		 */
		MANUAL,
		/**
		 * Fixed focus.
		 */
		FIXED;

		/**
		 * Convert into a display string, to be used in the GUI.
		 *
		 * @return the display string.
		 */
		public final String toDisplayString() {
			switch (this) {
			case CONTINUOUS:
				return "AUTO\n∞";
			default:
				return toString();
			}
		}
	}

}
