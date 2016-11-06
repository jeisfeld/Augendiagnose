package de.jeisfeld.augendiagnoselib.util;

import java.util.ArrayList;
import java.util.List;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.activities.CameraActivity.CameraCallback;
import de.jeisfeld.augendiagnoselib.activities.CameraActivity.FlashMode;
import de.jeisfeld.augendiagnoselib.activities.CameraActivity.FocusMode;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A handler to take pictures with the camera via the old Camera interface.
 */
@SuppressWarnings("deprecation")
public class Camera1Handler implements CameraHandler {
	/**
	 * The camera used by the activity.
	 */
	@Nullable
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
	@Nullable
	private String mCurrentFlashlightMode = null;

	/**
	 * The current focus mode.
	 */
	@Nullable
	private String mCurrentFocusMode = null;

	/**
	 * The FrameLayout holding the preview.
	 */
	private final FrameLayout mPreviewFrame;

	/**
	 * The surface of the preview.
	 */
	@Nullable
	private SurfaceHolder mPreviewHolder = null;

	/**
	 * The handler called when the picture is taken.
	 */
	private final CameraCallback mCameraCallback;

	/**
	 * The current relative zoom.
	 */
	private float mCurrentRelativeZoom = 0;
	/**
	 * The maximal digital zoom.
	 */
	private float mMaxDigitalZoom = 1;

	/**
	 * Constructor of the Camera1Handler.
	 *
	 * @param previewFrame   The FrameLayout holding the preview.
	 * @param preview        The view holding the preview.
	 * @param cameraCallback The handler called when the picture is taken.
	 */
	public Camera1Handler(final FrameLayout previewFrame, @NonNull final SurfaceView preview,
						  final CameraCallback cameraCallback) {
		this.mPreviewFrame = previewFrame;
		this.mCameraCallback = cameraCallback;

		mPreviewHolder = preview.getHolder();
		mPreviewHolder.addCallback(getSurfaceCallback());
		mPreviewHolder.setKeepScreenOn(true);
	}

	@Override
	public final void setFlashlightMode(@Nullable final FlashMode flashlightMode) {
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

	@Override
	public final void setFocusMode(@Nullable final FocusMode focusMode) {
		if (focusMode == null) {
			mCurrentFocusMode = null;
			return;
		}

		switch (focusMode) {
		case AUTO:
			mCurrentFocusMode = Parameters.FOCUS_MODE_AUTO;
			break;
		case MACRO:
			mCurrentFocusMode = Parameters.FOCUS_MODE_MACRO;
			break;
		case CONTINUOUS:
			mCurrentFocusMode = Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
			break;
		default:
			mCurrentFocusMode = null;
			break;
		}

		updateFocus();
	}

	@Override
	public final void setRelativeZoom(final float relativeZoom) {
		mCurrentRelativeZoom = relativeZoom;
		updateZoom();
	}

	/**
	 * Update the zoom.
	 */
	private void updateZoom() {
		if (mCamera != null) {
			Parameters parameters = mCamera.getParameters();
			if (parameters.isZoomSupported()) {
				int zoomFactor = (int) ((mMaxDigitalZoom + 0.99999) * mCurrentRelativeZoom); // MAGIC_NUMBER
				parameters.setZoom(zoomFactor);
			}
			mCamera.setParameters(parameters);
		}
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
	 * Update the focus.
	 */
	private void updateFocus() {
		if (mCamera != null) {
			Parameters parameters = mCamera.getParameters();
			parameters.setFocusMode(mCurrentFocusMode);
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
			catch (Exception e) {
				mCameraCallback.onCameraError("Cannot set preview", "pre1", e);
				return;
			}

			if (!mIsCameraConfigured) {
				Parameters parameters = mCamera.getParameters();
				Camera.Size pictureSize = getBiggestPictureSize(parameters);
				if (pictureSize == null) {
					return;
				}
				Camera.Size previewSize = getBestPreviewSize(((float) pictureSize.width) / pictureSize.height, parameters);
				if (previewSize == null) {
					return;
				}

				parameters.setPreviewSize(previewSize.width, previewSize.height);
				parameters.setPictureSize(pictureSize.width, pictureSize.height);
				parameters.setPictureFormat(ImageFormat.JPEG);

				updateAvailableModes(parameters.getSupportedFocusModes());

				mCameraCallback.updateAvailableZoom(parameters.isZoomSupported());
				mMaxDigitalZoom = parameters.getMaxZoom();

				try {
					parameters.setFocusMode(mCurrentFocusMode);
				}
				catch (Exception e) {
					parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
				}

				mCamera.setParameters(parameters);

				if (mCurrentFlashlightMode != null) {
					updateFlashlight();
				}
				if (parameters.isZoomSupported()) {
					updateZoom();
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
				mCameraCallback.onCameraError("Cannot open camera", "ope1", null);
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
		mCameraCallback.onTakingPicture();
	}

	/**
	 * Creage the callback client for the preview.
	 *
	 * @return the callback client.
	 */
	@NonNull
	private SurfaceHolder.Callback getSurfaceCallback() {
		return new SurfaceHolder.Callback() {
			@Override
			public void surfaceCreated(final SurfaceHolder holder) {
				mIsSurfaceCreated = true;
				try {
					if (mIsPreviewRequested) {
						startPreview();
					}
				}
				catch (Exception e) {
					mCameraCallback.onCameraError("Failed to start preview", "pre2", e);
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
	}

	/**
	 * Update the available focus modes.
	 *
	 * @param supportedFocusModes the supported focus modes.
	 */
	private void updateAvailableModes(@NonNull final List<String> supportedFocusModes) {
		List<FocusMode> focusModes = new ArrayList<>();
		for (String focusMode : supportedFocusModes) {
			if (Camera.Parameters.FOCUS_MODE_AUTO.equals(focusMode)) {
				focusModes.add(FocusMode.AUTO);
			}
			else if (Camera.Parameters.FOCUS_MODE_MACRO.equals(focusMode)) {
				focusModes.add(FocusMode.MACRO);
			}
			else if (Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE.equals(focusMode)) {
				focusModes.add(FocusMode.CONTINUOUS);
			}
		}

		mCameraCallback.updateAvailableModes(focusModes);
	}

	/**
	 * The callback called when pictures are taken.
	 */
	private final PictureCallback mPhotoCallback = new PictureCallback() {
		@Override
		@SuppressFBWarnings(value = "VA_PRIMITIVE_ARRAY_PASSED_TO_OBJECT_VARARG", justification = "Intentionally sending byte array")
		public void onPictureTaken(final byte[] data, final Camera photoCamera) {
			mIsInPreview = false;
			mCameraCallback.onPictureTaken(data);
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
	 * @param aspectRatio The aspect ratio of the picture.
	 * @param parameters  The camera parameters.
	 * @return The best preview size.
	 */
	@Nullable
	private static Camera.Size getBestPreviewSize(final float aspectRatio, @NonNull final Camera.Parameters parameters) {
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
	 * @param parameters The camera parameters.
	 * @return The biggest picture size.
	 */
	@Nullable
	private static Camera.Size getBiggestPictureSize(@NonNull final Camera.Parameters parameters) {
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
