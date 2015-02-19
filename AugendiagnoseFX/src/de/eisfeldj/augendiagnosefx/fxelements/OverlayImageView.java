package de.eisfeldj.augendiagnosefx.fxelements;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import de.eisfeldj.augendiagnosefx.util.ImageUtil;

/**
 * Pane containing an image that can be resized and can display overlays.
 */
public class OverlayImageView extends SizableImageView {
	/**
	 * The limiting value of contrast (to avoid infinity or gray).
	 */
	private static final float CONTRAST_LIMIT = 0.98f;

	/**
	 * The current overlay displayed.
	 */
	private Integer overlayType;

	/**
	 * The current overlay color displayed.
	 */
	private Color overlayColor;

	/**
	 * The current brightness of the image.
	 */
	private float brightness = 0;

	/**
	 * The current contrast of the image.
	 */
	private float contrast = 1;

	/**
	 * Display the overlay.
	 *
	 * @param newOverlayType
	 *            The overlay type to be displayed.
	 *
	 * @param newOverlayColor
	 *            The color of the overlay.
	 */
	public final void displayOverlay(final Integer newOverlayType, final Color newOverlayColor) {
		overlayType = newOverlayType;
		overlayColor = newOverlayColor;
		redisplay(false);
	}

	/**
	 * Change brightness and contrast.
	 *
	 * @param newBrightness
	 *            The brightness
	 * @param thumbnail
	 *            Indicator if image should be shown in thumbnail resolution.
	 */
	public final void setBrightness(final float newBrightness, final boolean thumbnail) {
		brightness = newBrightness;
		redisplay(thumbnail);
	}

	/**
	 * Change contrast.
	 *
	 * @param newContrast
	 *            The contrast
	 * @param thumbnail
	 *            Indicator if image should be shown in thumbnail resolution.
	 */
	public final void setContrast(final float newContrast, final boolean thumbnail) {
		contrast = seekbarContrastToStoredContrast(newContrast);
		redisplay(thumbnail);
	}

	/**
	 * Initialize brightness and contrast before displaying the image.
	 *
	 * @param newBrightness
	 *            The brightness
	 * @param newContrast
	 *            The contrast
	 */
	public final void initializeBrightnessContrast(final float newBrightness, final float newContrast) {
		brightness = newBrightness;
		contrast = newContrast;
	}

	/**
	 * Redisplay. (Can be used to switch between non-thumbnail and thumbnail view.
	 *
	 * @param thumbnail
	 *            Indicator if image should be shown in thumbnail resolution.
	 */
	public final void redisplay(final boolean thumbnail) {
		getImageView()
				.setImage(
						ImageUtil.getImageForDisplay(getEyePhoto(), overlayType, overlayColor, brightness, contrast,
								thumbnail));
	}

	/*
	 * Override in order to ensure that brightness/contrast are kept.
	 *
	 * (non-Javadoc)
	 *
	 * @see de.eisfeldj.augendiagnosefx.fxelements.SizableImageView#displayImage(javafx.scene.image.Image)
	 */
	@Override
	protected final void displayImage(final Image image) {
		Image enhancedImage =
				ImageUtil.getImageForDisplay(getEyePhoto(), overlayType, overlayColor, brightness, contrast,
						false);

		super.displayImage(enhancedImage);
	}

	/**
	 * Convert contrast from (-1,1) scale to (0,infty) scale.
	 *
	 * @param seekbarContrast
	 *            the contrast on (-1,1) scale.
	 * @return the contrast on (0,infty) scale.
	 */
	public static float seekbarContrastToStoredContrast(final float seekbarContrast) {
		float contrastImd = (float) (Math.asin(seekbarContrast) * 2 / Math.PI);
		return 2f / (1f - contrastImd * CONTRAST_LIMIT) - 1f;
	}

	/**
	 * Convert contrast from (0,infty) scale to (-1,1) scale.
	 *
	 * @param storedContrast
	 *            the contrast on (0,infty) scale.
	 * @return the contrast on (-1,1) scale.
	 */
	public static float storedContrastToSeekbarContrast(final float storedContrast) {
		float contrastImd = (1f - 2f / (storedContrast + 1f)) / CONTRAST_LIMIT;
		return (float) Math.sin(Math.PI * contrastImd / 2);
	}
}
