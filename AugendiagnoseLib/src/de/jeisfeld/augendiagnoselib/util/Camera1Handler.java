package de.jeisfeld.augendiagnoselib.util;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.activities.CameraActivity.FlashMode;
import de.jeisfeld.augendiagnoselib.activities.CameraActivity.OnPictureTakenHandler;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A handler to take pictures with the camera via the old Camera interface.
 */
@SuppressWarnings("deprecation")
public class Camera1Handler implements CameraHandler {
	/**
	 * The camera used by the activity.
	 */
	private Camera mCamera;

	/**
	 * A flag indicating if the preview is active.
	 */
	private boolean mIsInPreview = false;

	/**
	 * A flag indicating if preview is requested.
	 */
	private boolean mIsPreviewRequested = false;

	/**
	 * A flag indicating if the camera is configured.
	 */
	private boolean mIsCameraConfigured = false;

	/**
	 * A flag indicating if the surface is created.
	 */
	private boolean mIsSurfaceCreated = false;

	/**
	 * The current flashlight mode.
	 */
	private String mCurrentFlashlightMode = null;

	/**
	 * The activity using the handler.
	 */
	private Activity mActivity;

	/**
	 * The FrameLayout holding the preview.
	 */
	private FrameLayout mPreviewFrame;

	/**
	 * The surface of the preview.
	 */
	private SurfaceHolder mPreviewHolder = null;

	/**
	 * The handler called when the picture is taken.
	 */
	private OnPictureTakenHandler mOnPictureTakenHandler;

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
	public Camera1Handler(final Activity activity, final FrameLayout previewFrame, final SurfaceView preview,
			final OnPictureTakenHandler onPictureTakenHandler) {
		this.mActivity = activity;
		this.mPreviewFrame = previewFrame;
		this.mOnPictureTakenHandler = onPictureTakenHandler;

		mPreviewHolder = preview.getHolder();
		mPreviewHolder.addCallback(mSurfaceCallback);
		mPreviewHolder.setKeepScreenOn(true);
	}

	@Override
	public final void setFlashlightMode(final FlashMode flashlightMode) {
		if (flashlightMode == null) {
			mCurrentFlashlightMode = null;
			return;
		}

		switch (flashlightMode) {
		case OFF:
			mCurrentFlashlightMode = Parameters.FLASH_MODE_OFF;
			break;
		case ON:
			mCurrentFlashlightMode = Parameters.FLASH_MODE_ON;
			break;
		case TORCH:
			mCurrentFlashlightMode = Parameters.FLASH_MODE_TORCH;
			break;
		default:
			mCurrentFlashlightMode = null;
			break;
		}

		updateFlashlight();
	}

	/**
	 * Update the flashlight.
	 */
	private void updateFlashlight() {
		if (mCamera != null) {
			Parameters parameters = mCamera.getParameters();
			if (parameters.getSupportedFlashModes().contains(mCurrentFlashlightMode)) {
				parameters.setFlashMode(mCurrentFlashlightMode);
			}
			mCamera.setParameters(parameters);
		}
	}

	/**
	 * Initialize the camera.
	 */
	private void initPreview() {
		if (mCamera != null && mPreviewHolder.getSurface() != null) {
			try {
				mCamera.setPreviewDisplay(mPreviewHolder);
			}
			catch (Throwable t) {
				Log.e(Application.TAG, "Exception in setPreviewDisplay()", t);
				DialogUtil.displayToast(mActivity, R.string.message_dialog_failed_to_open_camera_display);
			}

			if (!mIsCameraConfigured) {
				Parameters parameters = mCamera.getParameters();
				Camera.Size pictureSize = getBiggestPictureSize(parameters);
				if (pictureSize == null) {
					return;
				}
				Camera.Size previewSsize = getBestPreviewSize(((float) pictureSize.width) / pictureSize.height, parameters);
				if (previewSsize == null) {
					return;
				}

				parameters.setPreviewSize(previewSsize.width, previewSsize.height);
				parameters.setPictureSize(pictureSize.width, pictureSize.height);
				parameters.setPictureFormat(ImageFormat.JPEG);

				try {
					// getSupportedFocusModes is not reliable.
					parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
				}
				catch (Exception e) {
					parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
				}

				mCamera.setParameters(parameters);

				if (mCurrentFlashlightMode != null) {
					updateFlashlight();
				}

				// Resize frame to match aspect ratio
				float aspectRatio = ((float) pictureSize.width) / pictureSize.height;

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

				mIsCameraConfigured = true;
			}
		}
	}

	@Override
	public final void startPreview() {
		if (Parameters.FLASH_MODE_ON.equals(mCurrentFlashlightMode)) {
			stopPreview();
		}
		mIsPreviewRequested = true;

		if (mIsInPreview) {
			return;
		}

		if (mCamera == null) {
			mCamera = getCameraInstance();

			if (mCamera == null) {
				// The activity depends on the camera.
				DialogUtil.displayError(mActivity, R.string.message_dialog_failed_to_open_camera, true);
				return;
			}
		}

		if (mIsSurfaceCreated && !mIsCameraConfigured) {
			initPreview();
		}

		if (mIsCameraConfigured) {
			mCamera.startPreview();
			mIsInPreview = true;
		}
	}

	@Override
	public final void stopPreview() {
		mIsPreviewRequested = false;

		if (mCamera != null) {
			if (mIsInPreview) {
				mCamera.stopPreview();
			}

			mCamera.release();
			mCamera = null;
			mIsCameraConfigured = false;
			mIsInPreview = false;
		}
	}

	@Override
	public final void takePicture() {
		mCamera.takePicture(null, null, mPhotoCallback);
		mOnPictureTakenHandler.onTakingPicture();
	}

	/**
	 * The callback client for the preview.
	 */
	private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
		@Override
		public void surfaceCreated(final SurfaceHolder holder) {
			mIsSurfaceCreated = true;
			try {
				if (mIsPreviewRequested) {
					startPreview();
				}
			}
			catch (Exception e) {
				DialogUtil.displayError(mActivity, R.string.message_dialog_failed_to_open_camera, true);
			}
		}

		@Override
		public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
			// do nothing.
		}

		@Override
		public void surfaceDestroyed(final SurfaceHolder holder) {
			stopPreview();
			mIsSurfaceCreated = false;
		}
	};

	/**
	 * The callback called when pictures are taken.
	 */
	private PictureCallback mPhotoCallback = new PictureCallback() {
		@Override
		@SuppressFBWarnings(value = "VA_PRIMITIVE_ARRAY_PASSED_TO_OBJECT_VARARG", justification = "Intentionally sending byte array")
		public void onPictureTaken(final byte[] data, final Camera photoCamera) {
			mIsInPreview = false;
			mOnPictureTakenHandler.onPictureTaken(data);
		}
	};

	/**
	 * Get a camera instance.
	 *
	 * @return The camera instance.
	 */
	private static Camera getCameraInstance() {
		int cameraId = findBackFacingCamera();
		if (cameraId < 0) {
			return null;
		}

		try {
			return Camera.open(cameraId);
		}
		catch (Exception e) {
			Log.e(Application.TAG, "Could not open camera: " + e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Find the id of a front facing camera.
	 *
	 * @return The camera id.
	 */
	private static int findBackFacingCamera() {
		int numberOfCameras = Camera.getNumberOfCameras();

		if (numberOfCameras == 0) {
			return -1;
		}

		for (int cameraId = 0; cameraId < numberOfCameras; cameraId++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(cameraId, info);
			if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
				return cameraId;
			}
		}

		// no back facing camera found.
		return 0;
	}

	/**
	 * Get the best preview size.
	 *
	 * @param aspectRatio
	 *            The aspect ratio of the picture.
	 * @param parameters
	 *            The camera parameters.
	 * @return The best preview size.
	 */
	private static Camera.Size getBestPreviewSize(final float aspectRatio, final Camera.Parameters parameters) {
		Camera.Size result = null;
		float bestAspectRatioDifference = 0;

		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			float newAspectRatioDifference = Math.abs(((float) size.width) / size.height - aspectRatio);
			if (result == null) {
				result = size;
				bestAspectRatioDifference = newAspectRatioDifference;
			}
			else {
				if ((newAspectRatioDifference < bestAspectRatioDifference - 0.01f) // MAGIC_NUMBER
						|| (size.width > result.width && newAspectRatioDifference < bestAspectRatioDifference + 0.01f)) { // MAGIC_NUMBER
					result = size;
					bestAspectRatioDifference = newAspectRatioDifference;
				}
			}
		}

		return result;
	}

	/**
	 * Get the biggest possible picture size.
	 *
	 * @param parameters
	 *            The camera parameters.
	 * @return The biggest picture size.
	 */
	private static Camera.Size getBiggestPictureSize(final Camera.Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPictureSizes()) {
			if (result == null) {
				result = size;
			}
			else {
				int resultSize = Math.min(result.width, result.height);
				int newSize = Math.min(size.width, size.height);

				if (newSize > resultSize || (newSize == resultSize && size.width > size.height)) {
					result = size;
				}
			}
		}

		return result;
	}

}
