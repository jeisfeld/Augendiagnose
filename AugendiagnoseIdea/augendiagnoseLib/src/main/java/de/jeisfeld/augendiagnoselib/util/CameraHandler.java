package de.jeisfeld.augendiagnoselib.util;

import de.jeisfeld.augendiagnoselib.activities.CameraActivity.FlashMode;
import de.jeisfeld.augendiagnoselib.activities.CameraActivity.FocusMode;

/**
 * An interface to take pictures with the camera.
 */
public interface CameraHandler {
	/**
	 * Set the flashlight mode.
	 *
	 * @param flashlightMode The new flashlight mode.
	 */
	void setFlashlightMode(final FlashMode flashlightMode);

	/**
	 * Set the focus mode.
	 *
	 * @param focusMode The new focus mode.
	 */
	void setFocusMode(final FocusMode focusMode);

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
