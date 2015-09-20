package de.jeisfeld.augendiagnoselib.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
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
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.activities.CameraActivity.FlashMode;
import de.jeisfeld.augendiagnoselib.activities.CameraActivity.OnPictureTakenHandler;

/**
 * A handler to take pictures with the camera via the new Camera interface.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@SuppressWarnings("static-access")
public class Camera2Handler implements CameraHandler {

	/**
	 * The activity using the handler.
	 */
	private Activity activity;

	/**
	 * The FrameLayout holding the preview.
	 */
	private FrameLayout previewFrame;

	/**
	 * Flag indicating if the camera is in preview state.
	 */
	private boolean inPreview = false;

	/**
	 * The handler called when the picture is taken.
	 */
	private OnPictureTakenHandler onPictureTakenHandler;

	/**
	 * Constructor of the Camera1Handler.
	 *
	 * @param activity
	 *            The activity using the handler.
	 * @param previewFrame
	 *            The FrameLayout holding the preview.
	 * @param preview
	 *            The view holding the preview.
	 * @param onPictureTakenHandler
	 *            The handler called when the picture is taken.
	 */
	public Camera2Handler(final Activity activity, final FrameLayout previewFrame, final TextureView preview,
			final OnPictureTakenHandler onPictureTakenHandler) {
		this.activity = activity;
		this.previewFrame = previewFrame;
		this.mTextureView = preview;
		this.onPictureTakenHandler = onPictureTakenHandler;
	}

	/**
	 * Conversion from screen rotation to JPEG orientation.
	 */
	private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

	static {
		ORIENTATIONS.append(Surface.ROTATION_0, 90); // MAGIC_NUMBER
		ORIENTATIONS.append(Surface.ROTATION_90, 0);
		ORIENTATIONS.append(Surface.ROTATION_180, 270); // MAGIC_NUMBER
		ORIENTATIONS.append(Surface.ROTATION_270, 180); // MAGIC_NUMBER
	}

	/**
	 * Tag for the {@link Log}.
	 */
	private static final String TAG = "Camera2Handler";

	/**
	 * Camera state: Showing camera preview.
	 */
	private static final int STATE_PREVIEW = 0;

	/**
	 * Camera state: Waiting for the focus to be locked.
	 */
	private static final int STATE_WAITING_LOCK = 1;

	/**
	 * Camera state: Waiting for the exposure to be precapture state.
	 */
	private static final int STATE_WAITING_PRECAPTURE = 2;

	/**
	 * Camera state: Waiting for the exposure state to be something other than precapture.
	 */
	private static final int STATE_WAITING_NON_PRECAPTURE = 3;

	/**
	 * Camera state: Picture was taken.
	 */
	private static final int STATE_PICTURE_TAKEN = 4;

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
	 * An {@link AutoFitTextureView} for camera preview.
	 */
	private TextureView mTextureView;

	/**
	 * A {@link CameraCaptureSession } for camera preview.
	 */
	private CameraCaptureSession mCaptureSession;

	/**
	 * A reference to the opened {@link CameraDevice}.
	 */
	private CameraDevice mCameraDevice;

	/**
	 * The {@link android.util.Size} of camera preview.
	 */
	private Size mPreviewSize;

	/**
	 * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
	 */
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
		}

		@Override
		public void onError(@NonNull final CameraDevice cameraDevice, final int error) {
			mCameraOpenCloseLock.release();
			cameraDevice.close();
			mCameraDevice = null;

			Log.e(Application.TAG, "Error on camera - " + error);
			DialogUtil.displayToast(activity, R.string.message_dialog_failed_to_open_camera);
		}

	};

	/**
	 * An {@link ImageReader} that handles still image capture.
	 */
	private ImageReader mImageReader;

	/**
	 * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
	 * still image is ready to be saved.
	 */
	private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
		@Override
		public void onImageAvailable(final ImageReader reader) {
			Image image = reader.acquireNextImage();
			ByteBuffer buffer = image.getPlanes()[0].getBuffer();
			byte[] data = new byte[buffer.remaining()];
			buffer.get(data);
			image.close();

			onPictureTakenHandler.onPictureTaken(data);
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
	private int mState = STATE_PREVIEW;

	/**
	 * A {@link Semaphore} to prevent the app from exiting before closing the camera.
	 */
	private Semaphore mCameraOpenCloseLock = new Semaphore(1);

	/**
	 * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
	 */
	private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

		private void process(final CaptureResult result) {
			switch (mState) {
			case STATE_PREVIEW:
				// We have nothing to do when the camera preview is working normally.
				break;
			case STATE_WAITING_LOCK:
				Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
				if (afState == null) {
					captureStillPicture();
				}
				else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState
						|| CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
					// CONTROL_AE_STATE can be null on some devices
					Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
					if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
						mState = STATE_PICTURE_TAKEN;
						captureStillPicture();
					}
					else {
						runPrecaptureSequence();
					}
				}
				break;
			case STATE_WAITING_PRECAPTURE:
				// CONTROL_AE_STATE can be null on some devices
				Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
				if (aeState == null
						|| aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE
						|| aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
					mState = STATE_WAITING_NON_PRECAPTURE;
				}
				break;
			case STATE_WAITING_NON_PRECAPTURE:
				// CONTROL_AE_STATE can be null on some devices
				Integer aeState2 = result.get(CaptureResult.CONTROL_AE_STATE);
				if (aeState2 == null || aeState2 != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
					mState = STATE_PICTURE_TAKEN;
					captureStillPicture();
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
	 * Shows a {@link Toast} on the UI thread.
	 *
	 * @param text
	 *            The message to show
	 */
	private void showToast(final String text) {
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	/**
	 * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
	 * width and height are at least as large as the respective requested values, and whose aspect
	 * ratio matches with the specified value.
	 *
	 * @param choices
	 *            The list of sizes that the camera supports for the intended output class
	 * @param width
	 *            The minimum desired width
	 * @param height
	 *            The minimum desired height
	 * @param aspectRatio
	 *            The aspect ratio
	 * @return The optimal {@code Size}, or an arbitrary one if none were big enough
	 */
	private static Size chooseOptimalSize(final Size[] choices, final int width, final int height, final Size aspectRatio) {
		// Collect the supported resolutions that are at least as big as the preview Surface
		List<Size> bigEnough = new ArrayList<Size>();
		int w = aspectRatio.getWidth();
		int h = aspectRatio.getHeight();
		for (Size option : choices) {
			if (option.getHeight() == option.getWidth() * h / w && option.getWidth() >= width && option.getHeight() >= height) {
				bigEnough.add(option);
			}
		}

		// Pick the smallest of those, assuming we found any
		if (bigEnough.size() > 0) {
			return Collections.min(bigEnough, new CompareSizesByArea());
		}
		else {
			Log.e(TAG, "Couldn't find any suitable preview size");
			return choices[0];
		}
	}

	@Override
	public final void startPreview() {
		if (inPreview) {
			unlockFocus();
		}
		else {
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
		inPreview = false;
		closeCamera();
	}

	/**
	 * Sets up member variables related to camera.
	 *
	 * @param width
	 *            The width of available size for camera preview
	 * @param height
	 *            The height of available size for camera preview
	 */
	private void setUpCameraOutputs(final int width, final int height) {
		CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
		try {
			for (String cameraId : manager.getCameraIdList()) {
				CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

				// We don't use a front facing camera in this sample.
				Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
				if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
					continue;
				}

				StreamConfigurationMap map = characteristics.get(
						CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
				if (map == null) {
					continue;
				}

				// For still image captures, we use the largest available size.
				Size largest = Collections.max(
						Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
						new CompareSizesByArea());
				mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, /* maxImages */2);
				mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);

				// Danger, W.R.! Attempting to use too large a preview size could exceed the camera
				// bus' bandwidth limitation, resulting in gorgeous previews but the storage of
				// garbage capture data.
				mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
						width, height, largest);

				// Resize frame to match aspect ratio
				float aspectRatio = ((float) mPreviewSize.getWidth()) / mPreviewSize.getHeight();

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

				mCameraId = cameraId;
				return;
			}
		}
		catch (CameraAccessException e) {
			e.printStackTrace();
		}
		catch (NullPointerException e) {
			Log.e(Application.TAG, "Camera2 API seems to be not supported", e);
			DialogUtil.displayToast(activity, R.string.message_dialog_failed_to_open_camera);
			// TODO: fallback to normal Camera API
		}
	}

	/**
	 * Opens the camera specified by {@link Camera2Handler#mCameraId}.
	 *
	 * @param width
	 *            the width of the preview
	 * @param height
	 *            the height of the preview
	 */
	private void openCamera(final int width, final int height) {
		inPreview = true;
		setUpCameraOutputs(width, height);
		configureTransform(width, height);
		CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
		try {
			if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) { // MAGIC_NUMBER
				throw new RuntimeException("Time out waiting to lock camera opening.");
			}
			manager.openCamera(mCameraId, mStateCallback, null);
		}
		catch (CameraAccessException e) {
			e.printStackTrace();
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
			throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
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
			Surface surface = new Surface(texture);

			// We set up a CaptureRequest.Builder with the output Surface.
			mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			mPreviewRequestBuilder.addTarget(surface);

			// Here, we create a CameraCaptureSession for camera preview.
			mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
					new CameraCaptureSession.StateCallback() {

						@Override
						public void onConfigured(@NonNull final CameraCaptureSession cameraCaptureSession) {
							// The camera is already closed
							if (null == mCameraDevice) {
								return;
							}

							// When the session is ready, we start displaying the preview.
							mCaptureSession = cameraCaptureSession;
							try {
								// Auto focus should be continuous for camera preview.
								mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
										CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
								// Flash is automatically enabled when necessary.
								mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
										CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

								// Finally, we start displaying the camera preview.
								mPreviewRequest = mPreviewRequestBuilder.build();
								mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, null);
							}
							catch (CameraAccessException e) {
								e.printStackTrace();
							}
						}

						@Override
						public void onConfigureFailed(
								@NonNull final CameraCaptureSession cameraCaptureSession) {
							showToast("Failed");
						}
					}, null);
		}
		catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
	 * This method should be called after the camera preview size is determined in
	 * setUpCameraOutputs and also the size of `mTextureView` is fixed.
	 *
	 * @param viewWidth
	 *            The width of `mTextureView`
	 * @param viewHeight
	 *            The height of `mTextureView`
	 */
	private void configureTransform(final int viewWidth, final int viewHeight) {
		if (null == mTextureView || null == mPreviewSize || null == activity) {
			return;
		}
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
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
		lockFocus();
	}

	/**
	 * Lock the focus as the first step for a still image capture.
	 */
	private void lockFocus() {
		try {
			// This is how to tell the camera to lock focus.
			mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
			// Tell #mCaptureCallback to wait for the lock.
			mState = STATE_WAITING_LOCK;

			mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, null);
		}
		catch (CameraAccessException e) {
			e.printStackTrace();
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
			mState = STATE_WAITING_PRECAPTURE;
			mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, null);
		}
		catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Capture a still picture. This method should be called when we get a response in
	 * {@link #mCaptureCallback} from both {@link #lockFocus()}.
	 */
	private void captureStillPicture() {
		try {
			if (null == activity || null == mCameraDevice) {
				return;
			}
			// This is the CaptureRequest.Builder that we use to take a picture.
			final CaptureRequest.Builder captureBuilder =
					mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
			captureBuilder.addTarget(mImageReader.getSurface());

			// Use the same AE and AF modes as the preview.
			captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
					CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
			captureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
					CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

			// Orientation
			int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
			captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

			mCaptureSession.stopRepeating();
			mCaptureSession.capture(captureBuilder.build(), mCaptureCallback, null);
		}
		catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Unlock the focus. This method should be called when still image capture sequence is
	 * finished.
	 */
	private void unlockFocus() {
		try {
			// Reset the auto-focus trigger
			mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
					CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
			mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
					CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
			mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, null);
			// After this, the camera will go back to the normal state of preview.
			mState = STATE_PREVIEW;
			mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, null);
		}
		catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public final void setFlashlightMode(final FlashMode flashlightMode) {
		// TODO implement flash
	}

	/**
	 * Compares two {@code Size}s based on their areas.
	 */
	static class CompareSizesByArea implements Comparator<Size> {

		@Override
		public int compare(final Size lhs, final Size rhs) {
			// We cast here to ensure the multiplications won't overflow
			return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
		}

	}

}
