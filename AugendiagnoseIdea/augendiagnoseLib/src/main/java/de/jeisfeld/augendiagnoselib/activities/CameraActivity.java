package de.jeisfeld.augendiagnoselib.activities;

import android.Manifest;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.activities.OrganizeNewPhotosActivity.NextAction;
import de.jeisfeld.augendiagnoselib.components.OverlayPinchImageView;
import de.jeisfeld.augendiagnoselib.components.PinchImageView;
import de.jeisfeld.augendiagnoselib.util.Camera1Handler;
import de.jeisfeld.augendiagnoselib.util.Camera2Handler;
import de.jeisfeld.augendiagnoselib.util.CameraHandler;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.OrientationManager;
import de.jeisfeld.augendiagnoselib.util.OrientationManager.OrientationListener;
import de.jeisfeld.augendiagnoselib.util.OrientationManager.ScreenOrientation;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.SystemUtil;
import de.jeisfeld.augendiagnoselib.util.TrackingUtil;
import de.jeisfeld.augendiagnoselib.util.TrackingUtil.Category;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto.RightLeft;
import de.jeisfeld.augendiagnoselib.util.imagefile.FileUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.ImageUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.JpegMetadata;
import de.jeisfeld.augendiagnoselib.util.imagefile.JpegSynchronizationUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.MediaStoreUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.PupilAndIrisDetector;

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
	 * Static helper method to start the activity for taking two photos to the input folder.
	 *
	 * @param activity The activity from which the activity is started.
	 */
	public static void startActivity(@NonNull final Activity activity) {
		Intent intent = new Intent(activity, CameraActivity.class);
		activity.startActivity(intent);
	}

	/**
	 * Static helper method to start the activity for taking two photos to the input folder.
	 *
	 * @param activity    The activity from which the activity is started.
	 * @param photoFolder The folder where to store the photos.
	 */
	public static void startActivity(@NonNull final Activity activity, final String photoFolder) {
		Intent intent = new Intent(activity, CameraActivity.class);
		intent.putExtra(STRING_EXTRA_PHOTOFOLDER, photoFolder);
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

		int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

		if (permission == PackageManager.PERMISSION_GRANTED) {
			setupActivity();
		}
		// BaseActivity will request for permission. If permission is granted, then onRequestPermissionsResult will setup the activity.
	}

	/**
	 * Do the basic setup of the activity. This is basically the key part of the onCreate method.
	 */
	private void setupActivity() {
		setContentView(R.layout.activity_camera);

		setCameraHandler();

		configureMainButtons();
		configureThumbButtons();
		configureZoomCircleButton();
		configureFlashlightButton();
		// Focus button is configured after callback from CameraHandler.

		int screenAppearance =
				PreferenceUtil.getSharedPreferenceIntString(R.string.key_camera_screen_position, R.string.pref_default_camera_screen_position);
		if (screenAppearance > 0) {
			FrameLayout cameraOverallFrame = (FrameLayout) findViewById(R.id.camera_overall_frame);
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

		int overlayCircleSize = PreferenceUtil.getSharedPreferenceInt(R.string.key_internal_camera_circle_size, DEFAULT_CIRCLE_RADIUS);
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
	}

	@Override
	public final void onPause() {
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
		final Button captureButton = (Button) findViewById(R.id.buttonCameraTrigger);
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
		Button acceptButton = (Button) findViewById(R.id.buttonCameraAccept);
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

		// Add a listener to the view image button
		Button viewImagesButton = (Button) findViewById(R.id.buttonCameraViewImages);
		viewImagesButton.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(final View v) {
						setAction(CANCEL_AND_VIEW_IMAGES, null);
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
	 * Configure configuration buttons in this activity.
	 */
	private void configureThumbButtons() {

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
	 * Configure the button for setting the overlay circle size.
	 */
	private void configureZoomCircleButton() {
		Button overlayCircleButton = (Button) findViewById(R.id.buttonCameraZoomOverlayCircle);
		overlayCircleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				boolean isVisible = !PreferenceUtil.getSharedPreferenceBoolean(R.string.key_internal_camera_zoom_circle_seekbar_visibility);
				PreferenceUtil.setSharedPreferenceBoolean(R.string.key_internal_camera_zoom_circle_seekbar_visibility, isVisible);
				findViewById(R.id.seekbarCameraOverlayCircle).setVisibility(isVisible ? View.VISIBLE : View.GONE);
				if (mIsZoomAvailable) {
					findViewById(R.id.seekbarCameraZoom).setVisibility(isVisible ? View.VISIBLE : View.GONE);
				}
			}
		});
		boolean isVisible = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_internal_camera_zoom_circle_seekbar_visibility);
		findViewById(R.id.seekbarCameraOverlayCircle).setVisibility(isVisible ? View.VISIBLE : View.GONE);
		if (mIsZoomAvailable) {
			findViewById(R.id.seekbarCameraZoom).setVisibility(isVisible ? View.VISIBLE : View.GONE);
		}

		SeekBar overlayCircleSeekbar = (SeekBar) findViewById(R.id.seekbarCameraOverlayCircle);
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

		SeekBar zoomSeekbar = (SeekBar) findViewById(R.id.seekbarCameraZoom);
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
	 */
	private void configureFlashlightButton() {
		Button flashlightButton = (Button) findViewById(R.id.buttonCameraFlashlight);
		if (SystemUtil.hasFlashlight()) {
			determineAvailableFlashlightModes();
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
			setFlashlightMode(null);
			flashlightButton.setVisibility(View.GONE);
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

		Button focusButton = (Button) findViewById(R.id.buttonCameraFocus);

		if (mFocusModes.size() < 2) {
			focusButton.setVisibility(View.GONE);
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

		SeekBar focusSeekbar = (SeekBar) findViewById(R.id.seekbarCameraFocus);
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
				if (isCamera2()) {
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
	 * Change to the given action.
	 *
	 * @param action    The new action.
	 * @param rightLeft the next eye side.
	 */
	private void setAction(@NonNull final Action action, final RightLeft rightLeft) {
		mCurrentAction = action;
		mCurrentRightLeft = rightLeft;

		LinearLayout cameraThumbRight = (LinearLayout) findViewById(R.id.camera_thumb_layout_right);
		LinearLayout cameraThumbLeft = (LinearLayout) findViewById(R.id.camera_thumb_layout_left);
		Button buttonCapture = (Button) findViewById(R.id.buttonCameraTrigger);
		Button buttonAccept = (Button) findViewById(R.id.buttonCameraAccept);
		Button buttonDecline = (Button) findViewById(R.id.buttonCameraDecline);
		Button buttonViewImages = (Button) findViewById(R.id.buttonCameraViewImages);
		ImageView imageViewReview = (ImageView) findViewById(R.id.camera_review);
		FrameLayout cameraPreviewFrame = (FrameLayout) findViewById(R.id.camera_preview_frame);
		LinearLayout cameraSettingsLayout = (LinearLayout) findViewById(R.id.cameraSettingsLayout);

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
			cameraSettingsLayout.setVisibility(View.VISIBLE);
			imageViewReview.setVisibility(View.GONE);
			cameraPreviewFrame.setVisibility(View.VISIBLE);
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
			cameraSettingsLayout.setVisibility(View.INVISIBLE);
			imageViewReview.setVisibility(View.VISIBLE);
			cameraPreviewFrame.setVisibility(View.GONE);
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
			cameraSettingsLayout.setVisibility(View.VISIBLE);
			imageViewReview.setVisibility(View.GONE);
			cameraPreviewFrame.setVisibility(View.VISIBLE);
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
			else if (mPhotoFolder != null && mPhotoFolder.isDirectory() && mLeftEyeFile != null && mRightEyeFile != null) {
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

		SeekBar seekbarCameraFocus = (SeekBar) findViewById(R.id.seekbarCameraFocus);
		seekbarCameraFocus.setVisibility(mCurrentFocusMode == FocusMode.MANUAL ? View.VISIBLE : View.GONE);

		mCameraHandler.setFocusMode(mCurrentFocusMode);

		Button buttonCameraFocus = (Button) findViewById(R.id.buttonCameraFocus);

		if (mCurrentFocusMode == null) {
			buttonCameraFocus.setVisibility(View.GONE);
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
	 * @param data The data representing the bitmap.
	 */
	private void setThumbImage(@NonNull final byte[] data) {
		ImageView imageView = (ImageView) findViewById(mCurrentRightLeft == RIGHT ? R.id.camera_thumb_image_right : R.id.camera_thumb_image_left);

		Bitmap bitmap = ImageUtil.getImageBitmap(data, getResources().getDimensionPixelSize(R.dimen.camera_thumb_size));

		imageView.setImageBitmap(bitmap);
	}

	/**
	 * Set the thumb image from a file.
	 *
	 * @param file      The file to be put in the thumb.
	 * @param rightLeft The side of the eye
	 */
	private void setThumbImage(@Nullable final String file, final RightLeft rightLeft) {
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
	 * @param data The data representing the image.
	 */
	private void setReviewImage(@NonNull final byte[] data) {
		PinchImageView imageView = (PinchImageView) findViewById(R.id.camera_review);

		Bitmap bitmap = ImageUtil.getImageBitmap(data, findViewById(R.id.camera_preview_frame).getWidth());

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
				flashView.setVisibility(View.GONE);
			}
		});

		AnimationSet animation = new AnimationSet(false);
		animation.addAnimation(fadeOut);

		flashView.setVisibility(View.VISIBLE);
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
		public void updateAvailableModes(final List<FocusMode> focusModes) {
			mFocusModes = focusModes;
			configureFocusButton();
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
	 */
	private void determineAvailableFlashlightModes() {
		boolean enableFlashlight = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_enable_flash);

		if (enableFlashlight) {
			mFlashlightModes = Arrays.asList(FlashMode.OFF, FlashMode.ON, FlashMode.TORCH);
		}
		else {
			mFlashlightModes = Arrays.asList(FlashMode.OFF, FlashMode.TORCH);
		}
	}

	/**
	 * Set the camera handler.
	 */
	private void setCameraHandler() {
		SurfaceView camera1View = (SurfaceView) findViewById(R.id.camera1_preview);
		TextureView camera2View = (TextureView) findViewById(R.id.camera2_preview);
		if (isCamera2()) {
			mCameraHandler = new Camera2Handler(this, (FrameLayout) findViewById(R.id.camera_preview_frame), camera2View, mOnPictureTakenHandler);
			camera1View.setVisibility(View.GONE);
			camera2View.setVisibility(View.VISIBLE);
		}
		else {
			mCameraHandler = new Camera1Handler((FrameLayout) findViewById(R.id.camera_preview_frame), camera1View, mOnPictureTakenHandler);
			camera1View.setVisibility(View.VISIBLE);
			camera2View.setVisibility(View.GONE);
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

	/**
	 * The task responsible for saving the picture.
	 */
	private final class SavePhotoTask extends AsyncTask<File, String, File> {
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
		 * Give information which focus modes and flash modes are supported by the camera.
		 *
		 * @param focusModes The supported focus modes.
		 */
		void updateAvailableModes(List<FocusMode> focusModes);

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
		TORCH
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
		MANUAL;

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
