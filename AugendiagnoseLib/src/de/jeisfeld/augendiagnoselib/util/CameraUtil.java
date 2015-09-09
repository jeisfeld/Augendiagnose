package de.jeisfeld.augendiagnoselib.util;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.util.Log;
import de.jeisfeld.augendiagnoselib.Application;

/**
 * Utility class for handling the camera.
 */
@SuppressWarnings("deprecation")
public final class CameraUtil {
	/**
	 * Hide default constructor.
	 */
	private CameraUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get a camera instance.
	 *
	 * @return The camera instance.
	 */
	public static Camera getCameraInstance() {
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
	public static Camera.Size getBestPreviewSize(final float aspectRatio, final Camera.Parameters parameters) {
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
	public static Camera.Size getBiggestPictureSize(final Camera.Parameters parameters) {
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
