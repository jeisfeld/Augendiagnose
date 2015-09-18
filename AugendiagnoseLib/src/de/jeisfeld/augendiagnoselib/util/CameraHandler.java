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
	 * Update the flashlight.
	 */
	void updateFlashlight();

	/**
	 * Start the camera preview.
	 */
	void startPreview();

	/**
	 * Stop the camera preview.
	 */
	void stopPreview();

	/**
	 * Take a picture.
	 */
	void takePicture();

	/**
	 * Get the default scale factor of the overlay (dependent on the surface).
	 *
	 * @return
	 */
	float getDefaultOverlayScaleFactor();
}
