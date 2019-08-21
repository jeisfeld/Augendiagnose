package de.jeisfeld.augendiagnoselib.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.activities.CameraActivity.CameraCallback;
import de.jeisfeld.augendiagnoselib.activities.CameraActivity.FlashMode;
import de.jeisfeld.augendiagnoselib.activities.CameraActivity.FocusMode;

/**
 * A handler to take pictures with the camera via the new Camera interface.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@SuppressWarnings("static-access")
public class Camera2Handler implements CameraHandler {
	/**
	 * The duration of the external flash in milliseconds.
	 */
	private static final long EXTERNAL_FLASH_DURATION = 1500;

	/**
	 * The activity using the handler.
	 */
	private final Activity mActivity;

	/**
	 * The FrameLayout holding the preview.
	 */
	private final FrameLayout mPreviewFrame;

	/**
	 * Flag indicating if the camera is in preview state.
	 */
	private boolean mIsInPreview = false;

	/**
	 * The handler called when the picture is taken.
	 */
	private final CameraCallback mCameraCallback;

	/**
	 * Constructor of the Camera1Handler.
	 *
	 * @param activity       The activity using the handler.
	 * @param previewFrame   The FrameLayout holding the preview.
	 * @param preview        The view holding the preview.
	 * @param cameraCallback The handler called when the picture is taken.
	 */
	public Camera2Handler(final Activity activity, final FrameLayout previewFrame, final TextureView preview,
						  final CameraCallback cameraCallback) {
		this.mActivity = activity;
		this.mPreviewFrame = previewFrame;
		this.mTextureView = preview;
		this.mCameraCallback = cameraCallback;
	}

	/**
	 * Tag for the {@link Log}.
	 */
	private static final String TAG = "Camera2Handler";

	/**
	 * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
	 * {@link TextureView}.
	 */
	private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

		@Override
		public void onSurfaceTextureAvailable(final SurfaceTexture texture, final int width, final int height) {
			openCamera(width, height);
		}

		@Override
		public void onSurfaceTextureSizeChanged(final SurfaceTexture texture, final int width, final int height) {
			configureTransform(width, height);
		}

		@Override
		public boolean onSurfaceTextureDestroyed(final SurfaceTexture texture) {
			closeCamera();
			return true;
		}

		@Override
		public void onSurfaceTextureUpdated(final SurfaceTexture texture) {
		}

	};

	/**
	 * ID of the current {@link CameraDevice}.
	 */
	private String mCameraId;

	/**
	 * A {@link TextureView} for camera preview.
	 */
	private final TextureView mTextureView;

	/**
	 * A {@link CameraCaptureSession } for camera preview.
	 */
	@Nullable
	private CameraCaptureSession mCaptureSession;

	/**
	 * A reference to the opened {@link CameraDevice}.
	 */
	@Nullable
	private CameraDevice mCameraDevice;

	/**
	 * A reference to the camera characteristics.
	 */
	private CameraCharacteristics mCameraCharacteristics;

	/**
	 * The {@link android.util.Size} of camera preview.
	 */
	@Nullable
	private Size mPreviewSize;

	/**
	 * The surface displaying the preview.
	 */
	private Surface mSurface;

	/**
	 * The autoexposure mode.
	 */
	private int mCurrentAutoExposureMode = CameraCharacteristics.CONTROL_AE_MODE_OFF;

	/**
	 * The flash mode.
	 */
	private int mCurrentFlashMode = CameraCharacteristics.FLASH_MODE_OFF;

	/**
	 * Flag indicating if external flash should be used.
	 */
	private boolean mUseExternalFlash = false;

	/**
	 * The timestamp when the external flash was started.
	 */
	private long mExternalFlashTimestamp = 0;

	/**
	 * The handler of the external flash signal.
	 */
	private AudioUtil.Beep mExternalFlashBeep = null;

	/**
	 * The focus mode for the preview.
	 */
	private int mCurrentFocusMode = CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_PICTURE;

	/**
	 * The Edge Mode to be used.
	 */
	private int mEdgeMode = CameraCharacteristics.EDGE_MODE_OFF;

	/**
	 * The Optical Stabilization Mode to be used.
	 */
	private int mOpticalStabilizationMode = CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_OFF;

	/**
	 * The Noise Reduction Mode to be used.
	 */
	private int mNoiseReductionMode = CameraCharacteristics.NOISE_REDUCTION_MODE_OFF;

	/**
	 * The current focal distance (in case of manual focus).
	 */
	private float mCurrentRelativeFocalDistance = 0;

	/**
	 * The minimal focal distance.
	 */
	private float mMinimalFocalDistance = 0;

	/**
	 * The minimal focal length.
	 */
	private float mMaximalFocalLength = 0;

	/**
	 * The current relative zoom.
	 */
	private float mCurrentRelativeZoom = 0;

	/**
	 * The maximal digital zoom.
	 */
	private float mMaxDigitalZoom = 1;

	/**
	 * The size of the image array.
	 */
	private Rect mArraySize;

	/**
	 * An additional thread for running tasks that shouldn't block the UI.
	 */
	@Nullable
	private HandlerThread mBackgroundThread = null;

	/**
	 * A {@link Handler} for running tasks in the background.
	 */
	@Nullable
	private Handler mBackgroundHandler;

	/**
	 * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
	 */
	@Nullable
	private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

		@Override
		public void onOpened(@NonNull final CameraDevice cameraDevice) {
			// This method is called when the camera is opened. We start camera preview here.
			mCameraOpenCloseLock.release();
			mCameraDevice = cameraDevice;
			createCameraPreviewSession();
		}

		@Override
		public void onDisconnected(@NonNull final CameraDevice cameraDevice) {
			mCameraOpenCloseLock.release();
			cameraDevice.close();
			mCameraDevice = null;
			mCaptureSession = null;
		}

		@Override
		public void onError(@NonNull final CameraDevice cameraDevice, final int error) {
			mCameraOpenCloseLock.release();
			cameraDevice.close();
			mCameraDevice = null;
			mCaptureSession = null;

			mCameraCallback.onCameraError("Error on camera: " + error, "err1", null);
		}

	};

	/**
	 * An {@link ImageReader} that handles still image capture.
	 */
	@Nullable
	private ImageReader mImageReader;

	/**
	 * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
	 * still image is ready to be saved.
	 */
	private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
		@Override
		public void onImageAvailable(@NonNull final ImageReader reader) {
			if (mUseExternalFlash && mExternalFlashBeep != null) {
				mExternalFlashBeep.stop();
			}

			Image image = reader.acquireNextImage();
			ByteBuffer buffer = image.getPlanes()[0].getBuffer();
			byte[] data = new byte[buffer.remaining()];
			buffer.get(data);
			image.close();

			mCameraCallback.onPictureTaken(data);

			// Keep track that use of Camera2 API was once successful.
			PreferenceUtil.setSharedPreferenceBoolean(R.string.key_internal_camera2_successful, true);
		}
	};

	/**
	 * {@link CaptureRequest.Builder} for the camera preview.
	 */
	private CaptureRequest.Builder mPreviewRequestBuilder;

	/**
	 * {@link CaptureRequest} generated by {@link #mPreviewRequestBuilder}.
	 */
	private CaptureRequest mPreviewRequest;

	/**
	 * The current state of camera state for taking pictures.
	 *
	 * @see #mCaptureCallback
	 */
	@NonNull
	private CameraState mState = CameraState.STATE_PREVIEW;

	/**
	 * A {@link Semaphore} to prevent the app from exiting before closing the camera.
	 */
	private final Semaphore mCameraOpenCloseLock = new Semaphore(1);

	/**
	 * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
	 */
	@Nullable
	private final CaptureCallback mCaptureCallback = new CaptureCallback() {

		private void process(@NonNull final CaptureResult result) {
			Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
			Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
			switch (mState) {
			case STATE_PREVIEW:
				// We have nothing to do when the camera preview is working normally.
				break;
			case STATE_WAITING_LOCK:
				if (afState == null || mCurrentFocusMode == CaptureRequest.CONTROL_AF_MODE_OFF) {
					captureStillPictureIfExternalFlashReady();
				}
				else if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
						|| afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
					// CONTROL_AE_STATE can be null on some devices
					if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
						captureStillPictureIfExternalFlashReady();
					}
					else {
						runPrecaptureSequence();
					}
				}
				else if (afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED
						&& aeState != null && aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
					captureStillPictureIfExternalFlashReady();
				}
				break;
			case STATE_WAITING_PRECAPTURE:
				// CONTROL_AE_STATE can be null on some devices
				if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE
						|| aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
					mState = CameraState.STATE_WAITING_NON_PRECAPTURE;
				}
				else if (aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
					captureStillPictureIfExternalFlashReady();
				}
				break;
			case STATE_WAITING_NON_PRECAPTURE:
				// CONTROL_AE_STATE can be null on some devices
				if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
					captureStillPictureIfExternalFlashReady();
				}
				break;
			default:
				break;
			}
		}

		@Override
		public void onCaptureProgressed(@NonNull final CameraCaptureSession session,
										@NonNull final CaptureRequest request,
										@NonNull final CaptureResult partialResult) {
			process(partialResult);
		}

		@Override
		public void onCaptureCompleted(@NonNull final CameraCaptureSession session,
									   @NonNull final CaptureRequest request,
									   @NonNull final TotalCaptureResult result) {
			process(result);
		}

	};

	/**
	 * Capture the image unless external flash needs more time.
	 */
	private void captureStillPictureIfExternalFlashReady() {
		if (!mUseExternalFlash || System.currentTimeMillis() > mExternalFlashTimestamp + EXTERNAL_FLASH_DURATION) {
			captureStillPicture();
		}
	}


	/**
	 * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
	 * width and height are at least as large as the respective requested values, and whose aspect
	 * ratio matches with the specified value.
	 *
	 * @param choices     The list of sizes that the camera supports for the intended output class
	 * @param width       The minimum desired width
	 * @param height      The minimum desired height
	 * @param aspectRatio The aspect ratio
	 * @return The optimal {@code Size}, or an arbitrary one if none were big enough
	 */
	@Nullable
	private static Size chooseOptimalPreviewSize(@NonNull final Size[] choices, final int width, final int height, @NonNull final Size aspectRatio) {
		// Collect the supported resolutions that are at least as big as the preview Surface
		List<Size> bigEnough = new ArrayList<>();
		Size biggest = null;
		int w = aspectRatio.getWidth();
		int h = aspectRatio.getHeight();
		for (Size option : choices) {
			if (option.getHeight() * w == option.getWidth() * h) {
				if (option.getHeight() >= height || option.getWidth() >= width) {
					bigEnough.add(option);
				}
				if (biggest == null || option.getHeight() > biggest.getHeight()) {
					biggest = option;
				}
			}
		}

		// Pick the smallest of those, assuming we found any
		if (bigEnough.size() > 0) {
			return Collections.min(bigEnough, new CompareSizesBySmallestSide());
		}
		else if (biggest != null) {
			return biggest;
		}
		else {
			Log.e(TAG, "Couldn't find any suitable preview size");
			return choices[0];
		}
	}

	@Override
	public final void startPreview() {
		if (mIsInPreview) {
			reconfigureCamera();
		}
		else {
			startBackgroundThread();

			if (mTextureView.isAvailable()) {
				openCamera(mTextureView.getWidth(), mTextureView.getHeight());
			}
			else {
				mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
			}
		}
	}

	@Override
	public final void stopPreview() {
		mIsInPreview = false;
		closeCamera();
		stopBackgroundThread();
	}

	/**
	 * Sets up member variables related to camera.
	 *
	 * @param width  The width of available size for camera preview
	 * @param height The height of available size for camera preview
	 */
	private void setUpCameraOutputs(final int width, final int height) {
		CameraManager manager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
		try {
			for (String cameraId : manager.getCameraIdList()) {
				CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraId);

				StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
				if (map == null) {
					continue;
				}

				// For still image captures, we use the largest available size.
				Size largest = Collections.max(
						Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
						new CompareSizesBySmallestSide());

				mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, /* maxImages */2);
				mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);

				// Danger, W.R.! Attempting to use too large a preview size could exceed the camera
				// bus' bandwidth limitation, resulting in gorgeous previews but the storage of
				// garbage capture data.
				mPreviewSize = chooseOptimalPreviewSize(map.getOutputSizes(SurfaceTexture.class),
						width, height, largest);

				// Resize frame to match aspect ratio
				float aspectRatio = ((float) mPreviewSize.getWidth()) / mPreviewSize.getHeight();

				LayoutParams layoutParams = mPreviewFrame.getLayoutParams();
				if (mPreviewFrame.getWidth() > aspectRatio * mPreviewFrame.getHeight()) {
					layoutParams.width = Math.round(mPreviewFrame.getHeight() * aspectRatio);
					layoutParams.height = mPreviewFrame.getHeight();
				}
				else {
					layoutParams.width = mPreviewFrame.getWidth();
					layoutParams.height = Math.round(mPreviewFrame.getWidth() / aspectRatio);
				}
				mPreviewFrame.setLayoutParams(layoutParams);

				mCameraId = cameraId;
				mCameraCharacteristics = cameraCharacteristics;

				updateAvailableFocusModes();
				updateAvailableFlashModes();
				updateAvailableOtherModes();

				boolean useFrontCamera = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_use_front_camera);
				Integer preferredFacing = useFrontCamera ? CameraCharacteristics.LENS_FACING_FRONT : CameraCharacteristics.LENS_FACING_BACK;
				Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
				if (preferredFacing.equals(facing)) {
					return;
				}
			}
		}
		catch (CameraAccessException | IllegalStateException e) {
			mCameraCallback.onCameraError("Failed to access camera", "acc1", e);
		}
		catch (NullPointerException e) {
			mCameraCallback.onCameraError("Camera2 API does not seem to work", "acc2", e);
		}
	}

	/**
	 * Opens the camera specified by {@link Camera2Handler#mCameraId}.
	 *
	 * @param width  the width of the preview
	 * @param height the height of the preview
	 */
	private void openCamera(final int width, final int height) {
		setUpCameraOutputs(width, height);
		configureTransform(width, height);
		CameraManager manager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
		try {
			if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) { // MAGIC_NUMBER
				throw new RuntimeException("Time out waiting to lock camera opening.");
			}
			manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
			mIsInPreview = true;
		}
		catch (CameraAccessException | IllegalArgumentException | SecurityException | IllegalStateException | AssertionError e) {
			mCameraCallback.onCameraError("Failed to open camera", "ope2", e);
		}
		catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
		}
	}

	/**
	 * Closes the current {@link CameraDevice}.
	 */
	private void closeCamera() {
		try {
			mCameraOpenCloseLock.acquire();
			if (null != mCaptureSession) {
				mCaptureSession.close();
				mCaptureSession = null;
			}
			if (null != mCameraDevice) {
				mCameraDevice.close();
				mCameraDevice = null;
			}
			if (null != mImageReader) {
				mImageReader.close();
				mImageReader = null;
			}
		}
		catch (InterruptedException e) {
			Log.e(TAG, "Interrupted while trying to lock camera closing.", e);
		}
		finally {
			mCameraOpenCloseLock.release();
		}
	}

	/**
	 * Creates a new {@link CameraCaptureSession} for camera preview.
	 */
	private void createCameraPreviewSession() {
		try {
			SurfaceTexture texture = mTextureView.getSurfaceTexture();
			assert texture != null;

			// We configure the size of default buffer to be the size of camera preview we want.
			texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

			// This is the output Surface we need to start preview.
			mSurface = new Surface(texture);

			// Here, we create a CameraCaptureSession for camera preview.
			mCameraDevice.createCaptureSession(Arrays.asList(mSurface, mImageReader.getSurface()),
					new CameraCaptureSession.StateCallback() {

						@Override
						public void onConfigured(@NonNull final CameraCaptureSession cameraCaptureSession) {
							// The camera is already closed
							if (mCameraDevice == null) {
								return;
							}

							// When the session is ready, we start displaying the preview.
							mCaptureSession = cameraCaptureSession;

							doPreviewConfiguration();
						}

						@Override
						public void onConfigureFailed(
								@NonNull final CameraCaptureSession cameraCaptureSession) {
							mActivity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									mCameraCallback.onCameraError("Failed to create capture session", "pre3", null);
								}
							});
						}
					}, mBackgroundHandler);
		}
		catch (CameraAccessException | IllegalStateException | AssertionError e) {
			mCameraCallback.onCameraError("Failed to create preview session", "pre4", e);
		}
	}

	/**
	 * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
	 * This method should be called after the camera preview size is determined in
	 * setUpCameraOutputs and also the size of `mTextureView` is fixed.
	 *
	 * @param viewWidth  The width of `mTextureView`
	 * @param viewHeight The height of `mTextureView`
	 */
	private void configureTransform(final int viewWidth, final int viewHeight) {
		if (null == mTextureView || null == mPreviewSize || null == mActivity) {
			return;
		}
		int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
		Matrix matrix = new Matrix();
		RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
		RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
		float centerX = viewRect.centerX();
		float centerY = viewRect.centerY();
		if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
			bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
			matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
			float scale = Math.max(
					(float) viewHeight / mPreviewSize.getHeight(),
					(float) viewWidth / mPreviewSize.getWidth());
			matrix.postScale(scale, scale, centerX, centerY);
			matrix.postRotate(90 * (rotation - 2), centerX, centerY); // MAGIC_NUMBER
		}
		else if (Surface.ROTATION_180 == rotation) {
			matrix.postRotate(180, centerX, centerY); // MAGIC_NUMBER
		}
		mTextureView.setTransform(matrix);
	}

	/**
	 * Initiate a still image capture.
	 */
	@Override
	public final void takePicture() {
		if (mUseExternalFlash && mExternalFlashBeep != null) {
			mExternalFlashTimestamp = System.currentTimeMillis();
			mExternalFlashBeep.start();
		}

		lockFocus();
	}

	/**
	 * Lock the focus as the first step for a still image capture.
	 */
	private void lockFocus() {
		try {
			// This is how to tell the camera to lock focus.
			mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
			// Tell mCaptureCallback to wait for the lock.
			mState = CameraState.STATE_WAITING_LOCK;

			mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
		}
		catch (CameraAccessException | IllegalStateException e) {
			mCameraCallback.onCameraError("Failed to lock focus", "loc1", e);
		}
	}

	/**
	 * Run the precapture sequence for capturing a still image. This method should be called when
	 * we get a response in {@link #mCaptureCallback} from {@link #lockFocus()}.
	 */
	private void runPrecaptureSequence() {
		try {
			// This is how to tell the camera to trigger.
			mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
					CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
			// Tell #mCaptureCallback to wait for the precapture sequence to be set.
			mState = CameraState.STATE_WAITING_PRECAPTURE;
			mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
		}
		catch (CameraAccessException | IllegalStateException e) {
			mCameraCallback.onCameraError("Failed to run precapture sequence", "pre5", e);
		}
	}

	/**
	 * Capture a still picture. This method should be called when we get a response in
	 * {@link #mCaptureCallback} from both {@link #lockFocus()}.
	 */
	private void captureStillPicture() {
		mState = CameraState.STATE_TAKING_PICTURE;
		try {
			if (null == mActivity || null == mCameraDevice || null == mCaptureSession) {
				return;
			}
			// This is the CaptureRequest.Builder that we use to take a picture.
			final CaptureRequest.Builder captureBuilder =
					mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
			captureBuilder.addTarget(mImageReader.getSurface());

			// Use the same AE and AF modes as the preview.
			captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, mCurrentFocusMode);
			captureBuilder.set(CaptureRequest.FLASH_MODE, mCurrentFlashMode);
			captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, mCurrentAutoExposureMode);
			captureBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, mMinimalFocalDistance * mCurrentRelativeFocalDistance);
			captureBuilder.set(CaptureRequest.SCALER_CROP_REGION, getCroppingRect(mCurrentRelativeZoom));
			if (mMaximalFocalLength > 0) {
				captureBuilder.set(CaptureRequest.LENS_FOCAL_LENGTH, mMaximalFocalLength);
			}

			captureBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, mNoiseReductionMode);
			captureBuilder.set(CaptureRequest.EDGE_MODE, mEdgeMode);
			captureBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, mOpticalStabilizationMode);

			mCaptureSession.stopRepeating();
			mCameraCallback.onTakingPicture();

			mCaptureSession.capture(captureBuilder.build(), new CaptureCallback() {
				@Override
				public void onCaptureCompleted(@NonNull final CameraCaptureSession session, @NonNull final CaptureRequest request,
											   @NonNull final TotalCaptureResult result) {
					mState = CameraState.STATE_PICTURE_TAKEN;
					unlockFocus();
				}
			}, null);
		}
		catch (CameraAccessException | IllegalStateException e) {
			mCameraCallback.onCameraError("Failed to capture picture", "cap1", e);
		}
	}

	/**
	 * Unlock the focus. This method should be called when still image capture sequence is
	 * finished.
	 */
	private void unlockFocus() {
		try {
			// Reset the auto-focus trigger
			mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
			mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
			// After this, the camera will go back to the normal state of preview.
			mState = CameraState.STATE_PREVIEW;
			mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
		}
		catch (CameraAccessException | IllegalStateException | NullPointerException e) {
			mCameraCallback.onCameraError("Failed to unlock focus", "unl1", e);
		}
	}

	/**
	 * Starts a background thread and its {@link Handler}.
	 */
	private void startBackgroundThread() {
		if (mBackgroundThread == null) {
			mBackgroundThread = new HandlerThread("CameraBackground");
			mBackgroundThread.start();
			mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
		}
	}

	/**
	 * Stops the background thread and its {@link Handler}.
	 */
	private void stopBackgroundThread() {
		if (mBackgroundThread != null) {
			mBackgroundThread.quitSafely();
			try {
				mBackgroundThread.join();
				mBackgroundThread = null;
				mBackgroundHandler = null;
			}
			catch (InterruptedException e) {
				// ignore
			}
		}
	}

	@Override
	public final void setFlashlightMode(@Nullable final FlashMode flashlightMode) {
		if (flashlightMode == null) {
			mCurrentAutoExposureMode = CaptureRequest.CONTROL_AE_MODE_OFF;
		}
		else {
			switch (flashlightMode) {
			case OFF:
			case EXT:
				if (SystemUtil.hasFlashlight()) {
					mCurrentFlashMode = CaptureRequest.FLASH_MODE_OFF;
					mCurrentAutoExposureMode = CaptureRequest.CONTROL_AE_MODE_ON;
				}
				else {
					mCurrentAutoExposureMode = CaptureRequest.CONTROL_AE_MODE_OFF;
				}
				break;
			case ON:
				mCurrentFlashMode = CaptureRequest.FLASH_MODE_SINGLE;
				mCurrentAutoExposureMode = CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH;
				break;
			case TORCH:
				mCurrentFlashMode = CaptureRequest.FLASH_MODE_TORCH;
				mCurrentAutoExposureMode = CaptureRequest.CONTROL_AE_MODE_ON;
				break;
			default:
				mCurrentFlashMode = CaptureRequest.FLASH_MODE_OFF;
				mCurrentAutoExposureMode = CaptureRequest.CONTROL_AE_MODE_ON;
				break;
			}

			mUseExternalFlash = flashlightMode == FlashMode.EXT;
			if (mUseExternalFlash && mExternalFlashBeep == null) {
				mExternalFlashBeep = new AudioUtil.Beep();
			}
		}
		reconfigureCamera();
	}

	@Override
	public final void setFocusMode(@Nullable final FocusMode focusMode) {
		if (focusMode == null) {
			mCurrentFocusMode = CaptureRequest.CONTROL_AF_MODE_OFF;
		}
		else {
			switch (focusMode) {
			case CONTINUOUS:
				mCurrentFocusMode = CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE;
				break;
			case AUTO:
				mCurrentFocusMode = CaptureRequest.CONTROL_AF_MODE_AUTO;
				break;
			case MACRO:
				mCurrentFocusMode = CaptureRequest.CONTROL_AF_MODE_MACRO;
				break;
			case MANUAL:
			case FIXED:
				mCurrentFocusMode = CaptureRequest.CONTROL_AF_MODE_OFF;
				break;
			default:
				mCurrentFocusMode = CaptureRequest.CONTROL_AF_MODE_AUTO;
				break;
			}
		}
		reconfigureCamera();
	}

	/**
	 * Update the focal distance of the camera (relative to the minimal focal distance).
	 *
	 * @param relativeFocalDistance The new relative focal distance.
	 */
	public final void setRelativeFocalDistance(final float relativeFocalDistance) {
		mCurrentRelativeFocalDistance = relativeFocalDistance;
		reconfigureCamera();
	}

	@Override
	public final void setRelativeZoom(final float relativeZoom) {
		mCurrentRelativeZoom = relativeZoom;
		reconfigureCamera();
	}

	/**
	 * Reconfigure the camera with new flash and focus settings.
	 */
	private void reconfigureCamera() {
		if (mCameraDevice != null && mCaptureSession != null) {
			try {
				mCaptureSession.stopRepeating();

				mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
				mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);

				doPreviewConfiguration();
			}
			catch (CameraAccessException | IllegalStateException e) {
				mCameraCallback.onCameraError("Failed to reconfigure the camera", "rec1", e);
			}
		}
	}

	/**
	 * Do the setting of flash and focus settings.
	 */
	private void doPreviewConfiguration() {
		if (mCameraDevice != null && mCaptureSession != null) {
			mState = CameraState.STATE_PREVIEW;
			try {
				// Need to recreate the complete request from scratch - reuse will fail.
				mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
				mPreviewRequestBuilder.addTarget(mSurface);

				mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, mCurrentFocusMode);
				mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, mCurrentFlashMode);
				mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, mCurrentAutoExposureMode);
				mPreviewRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, mMinimalFocalDistance * mCurrentRelativeFocalDistance);
				mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, getCroppingRect(mCurrentRelativeZoom));
				if (mMaximalFocalLength > 0) {
					mPreviewRequestBuilder.set(CaptureRequest.LENS_FOCAL_LENGTH, mMaximalFocalLength);
				}

				mPreviewRequestBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, mNoiseReductionMode);
				mPreviewRequestBuilder.set(CaptureRequest.EDGE_MODE, mEdgeMode);
				mPreviewRequestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, mOpticalStabilizationMode);

				mPreviewRequest = mPreviewRequestBuilder.build();
				mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
			}
			catch (CameraAccessException | IllegalStateException | NullPointerException e) {
				mCameraCallback.onCameraError("Failed to do the preview configuration", "pre6", e);
			}
		}
	}

	/**
	 * Get the cropping rectangle for zooming the camera image.
	 *
	 * @param relativeZoom The relative zoom factor.
	 * @return The cropping rectangle.
	 */
	private Rect getCroppingRect(final float relativeZoom) {
		double zoomFactor = (float) Math.pow(mMaxDigitalZoom, relativeZoom);
		int targetWidth = (int) (mArraySize.width() / zoomFactor);
		int targetHeight = (int) (mArraySize.height() / zoomFactor);
		int horizontalBoundary = (mArraySize.width() - targetWidth) / 2;
		int verticalBoundary = (mArraySize.height() - targetHeight) / 2;
		return new Rect(horizontalBoundary, verticalBoundary, horizontalBoundary + targetWidth, verticalBoundary + targetHeight);
	}


	/**
	 * Update the available focus modes.
	 */
	private void updateAvailableFocusModes() {
		List<FocusMode> focusModes = new ArrayList<>();
		int[] availableFocusModes = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);

		Float minFocalDistance = mCameraCharacteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
		if (minFocalDistance != null) {
			mMinimalFocalDistance = minFocalDistance;
		}

		for (int focusMode : availableFocusModes != null ? availableFocusModes : new int[0]) {
			if (focusMode == CameraCharacteristics.CONTROL_AF_MODE_OFF) {
				if (SystemUtil.hasManualSensor() && minFocalDistance != null && minFocalDistance != 0) {
					focusModes.add(FocusMode.MANUAL);
				}
				else {
					focusModes.add(FocusMode.FIXED);
				}
			}
			else if (focusMode == CameraCharacteristics.CONTROL_AF_MODE_MACRO) {
				focusModes.add(FocusMode.MACRO);
			}
			else if (focusMode == CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_PICTURE) {
				focusModes.add(FocusMode.CONTINUOUS);
			}
			else if (focusMode == CameraCharacteristics.CONTROL_AF_MODE_AUTO) {
				focusModes.add(FocusMode.AUTO);
			}
		}

		float[] focalLengths = mCameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
		if (focalLengths != null && focalLengths.length > 0) {
			float maxFocalLength = 0;
			for (float focalLength : focalLengths) {
				if (focalLength > maxFocalLength) {
					maxFocalLength = focalLength;
				}
			}
			mMaximalFocalLength = maxFocalLength;
		}

		Float maxDigitalZoom = mCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
		Rect arraySize = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
		if (maxDigitalZoom != null && arraySize != null) {
			mMaxDigitalZoom = maxDigitalZoom;
			mArraySize = arraySize;
			mCameraCallback.updateAvailableZoom(maxDigitalZoom > 1);
		}

		mCameraCallback.updateAvailableFocusModes(focusModes);
	}

	/**
	 * Update the available flash modes.
	 */
	private void updateAvailableFlashModes() {
		List<FlashMode> flashModes = new ArrayList<>();
		flashModes.add(FlashMode.OFF);

		boolean isFlashAvailable = mCameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
		if (isFlashAvailable) {
			flashModes.add(FlashMode.TORCH);
			flashModes.add(FlashMode.ON);
		}

		mCameraCallback.updateAvailableFlashModes(flashModes);
	}

	/**
	 * Update the optical stabilization mode.
	 */
	private void updateAvailableOtherModes() {
		int[] availableStabilizationModes = mCameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION);
		if (availableStabilizationModes != null) {
			for (int mode : availableStabilizationModes) {
				if (mode == CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_ON) {
					mOpticalStabilizationMode = mode;
				}
			}
		}
		int[] availableNoiseReductionModes = mCameraCharacteristics.get(CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES);
		if (availableNoiseReductionModes != null) {
			for (int mode : availableNoiseReductionModes) {
				if (mode == CameraCharacteristics.NOISE_REDUCTION_MODE_FAST) {
					mNoiseReductionMode = mode;
				}
			}
		}
		int[] availableEdgeModes = mCameraCharacteristics.get(CameraCharacteristics.EDGE_AVAILABLE_EDGE_MODES);
		if (availableEdgeModes != null) {
			for (int mode : availableEdgeModes) {
				if (mode == CameraCharacteristics.EDGE_MODE_FAST) {
					mEdgeMode = mode;
				}
			}
		}
	}

	/**
	 * Compares two {@code Size}s based on their smallest side.
	 */
	private static class CompareSizesBySmallestSide implements Comparator<Size> {
		@Override
		public int compare(@NonNull final Size lhs, @NonNull final Size rhs) {
			int leftSize = Math.min(lhs.getWidth(), lhs.getHeight());
			int rightSize = Math.min(rhs.getWidth(), rhs.getHeight());

			if (leftSize > rightSize) {
				return 1;
			}
			else if (rightSize > leftSize) {
				return -1;
			}

			// for equal min side, prefer widest
			return Integer.signum(lhs.getWidth() - rhs.getWidth());
		}
	}

	/**
	 * Camera states.
	 */
	private enum CameraState {
		/**
		 * Camera state: Showing camera preview.
		 */
		STATE_PREVIEW,

		/**
		 * Camera state: Waiting for the focus to be locked.
		 */
		STATE_WAITING_LOCK,

		/**
		 * Camera state: Waiting for the exposure to be precapture state.
		 */
		STATE_WAITING_PRECAPTURE,

		/**
		 * Camera state: Waiting for the exposure state to be something other than precapture.
		 */
		STATE_WAITING_NON_PRECAPTURE,

		/**
		 * Camera state: Taking picture.
		 */
		STATE_TAKING_PICTURE,

		/**
		 * Camera state: Picture was taken.
		 */
		STATE_PICTURE_TAKEN
	}

}
