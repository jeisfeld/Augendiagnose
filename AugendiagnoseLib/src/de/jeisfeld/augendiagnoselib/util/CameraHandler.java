package de.jeisfeld.augendiagnoselib.util;

import de.jeisfeld.augendiagnoselib.activities.CameraActivity.FlashMode;

/**
 * An interface to take pictures with the camera.
 */
public interface CameraHandler {
	/**
	 * Set the flashlight mode.
	 *
	 * @param flashlightMode
	 *            The new flashlight mode.
	 */
	void setFlashlightMode(final FlashMode flashlightMode);

	/**
	 * Start the camera preview. Should be idempotent.
	 */
	void startPreview();

	/**
	 * Stop the camera preview. Should be idempotent.
	 */
	void stopPreview();

	/**
	 * Take a picture.
	 */
	void takePicture();
}
