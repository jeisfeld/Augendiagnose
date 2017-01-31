package de.eisfeldj.augendiagnosefx.fxelements;

import de.eisfeldj.augendiagnosefx.util.imagefile.ImageUtil;
import de.eisfeldj.augendiagnosefx.util.imagefile.ImageUtil.Resolution;
import de.eisfeldj.augendiagnosefx.util.imagefile.JpegMetadata;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

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
	private Integer mOverlayType;

	/**
	 * The current overlay color displayed.
	 */
	private Color mOverlayColor;

	/**
	 * The current brightness of the image.
	 */
	private float mBrightness = 0;

	/**
	 * The current contrast of the image.
	 */
	private float mContrast = 1;

	/**
	 * The current saturation of the image.
	 */
	private float mSaturation = 1;

	/**
	 * The current color temperature of the image.
	 */
	private float mColorTemperature = 0;

	/**
	 * The current resolution of the image - used to adapt zoomFactor if resolution changes.
	 */
	private Resolution mCurrentResolution = Resolution.NORMAL;

	/**
	 * The current image width - used to adapt zoomFactor if resolution changes.
	 */
	private double mCurrentImageWidth;

	/**
	 * Display the overlay.
	 *
	 * @param newOverlayType
	 *            The overlay type to be displayed.
	 * @param newOverlayColor
	 *            The color of the overlay.
	 * @param resolution
	 *            Indicator of the resolution of the image.
	 */
	public final void displayOverlay(final Integer newOverlayType, final Color newOverlayColor,
			final Resolution resolution) {
		mOverlayType = newOverlayType;
		mOverlayColor = newOverlayColor;
		mCurrentResolution = resolution;
		redisplay(mCurrentResolution);
	}

	/**
	 * Change color settings.
	 *
	 * @param newBrightness
	 *            The brightness
	 * @param newContrast
	 *            The contrast from slider
	 * @param newSaturation
	 *            The saturation from slider
	 * @param newColorTemperature
	 *            The color temperature
	 * @param resolution
	 *            Indicator of the resolution of the image.
	 */
	public final void setColorSettings(final Float newBrightness, final Float newContrast,
			final Float newSaturation, final Float newColorTemperature, final Resolution resolution) {
		if (newBrightness != null) {
			mBrightness = newBrightness;
		}
		if (newContrast != null) {
			mContrast = seekbarContrastToStoredContrast(newContrast);
		}
		if (newSaturation != null) {
			mSaturation = seekbarSaturationToStoredSaturation(newSaturation);
		}
		if (newColorTemperature != null) {
			mColorTemperature = newColorTemperature;
		}
		redisplay(resolution);
	}

	/**
	 * Initialize the color settings before displaying the image.
	 *
	 * @param newBrightness
	 *            The brightness
	 * @param newContrast
	 *            The contrast from metadata
	 * @param newSaturation
	 *            The saturation from metadata
	 * @param newColorTemperature
	 *            The color temperature
	 */
	public final void initializeColorSettings(final float newBrightness, final float newContrast,
			final float newSaturation, final float newColorTemperature) {
		mBrightness = newBrightness;
		mContrast = newContrast;
		mSaturation = newSaturation;
		mColorTemperature = newColorTemperature;
	}

	/**
	 * Redisplay. (Can be used to switch between non-thumbnail and thumbnail view.
	 *
	 * @param resolution
	 *            Indicator of the resolution of the image.
	 */
	public final void redisplay(final Resolution resolution) {
		Image newImage = ImageUtil.getImageForDisplay(getEyePhoto(), mOverlayType, mOverlayColor,
				mBrightness, mContrast, mSaturation, mColorTemperature, resolution);
		if (resolution != mCurrentResolution) {
			multiplyZoomProperty(mCurrentImageWidth / newImage.getWidth());
			mCurrentImageWidth = newImage.getWidth();
			mCurrentResolution = resolution;
		}

		getImageView().setImage(newImage);
	}

	/*
	 * Override in order to ensure that brightness/contrast are kept in case sliders have been initialized from
	 * metadata.
	 *
	 * (non-Javadoc)
	 *
	 * @see de.eisfeldj.augendiagnosefx.fxelements.SizableImageView#displayImage(javafx.scene.image.Image)
	 */
	@Override
	protected final void displayImage(final Image image) {
		Image enhancedImage = ImageUtil.getImageForDisplay(getEyePhoto(), mOverlayType, mOverlayColor,
				mBrightness, mContrast, mSaturation, mColorTemperature, Resolution.NORMAL);
		mCurrentResolution = Resolution.NORMAL;
		mCurrentImageWidth = enhancedImage.getWidth();

		super.displayImage(enhancedImage);
	}

	@Override
	public final void setImage(final JpegMetadata metadata, final Image image) {
		super.setImage(metadata, image);
		mCurrentResolution = Resolution.NORMAL;
		mCurrentImageWidth = image.getWidth();
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

	/**
	 * Convert saturation from (-1,1) scale to (1/3,infty) scale.
	 *
	 * @param seekbarSaturation
	 *            the saturation on (-1,1) scale.
	 * @return the saturation on (1/3,infty) scale.
	 */
	public static float seekbarSaturationToStoredSaturation(final float seekbarSaturation) {
		return 4f / 3 / (1f - seekbarSaturation * CONTRAST_LIMIT) - 1f / 3; // MAGIC_NUMBER
	}

	/**
	 * Convert saturation from (0,infty) scale to (-1,1) scale.
	 *
	 * @param storedSaturation
	 *            the saturation on (0,infty) scale.
	 * @return the saturation on (-1,1) scale.
	 */
	public static float storedSaturationToSeekbarSaturation(final float storedSaturation) {
		return (1f - 4f / 3 / (storedSaturation + 1f / 3)) / CONTRAST_LIMIT; // MAGIC_NUMBER
	}

	/**
	 * Clone the contents from another instance.
	 *
	 * @param view The other instance.
	 */
	public void cloneContents(final OverlayImageView view) {
		super.cloneContents(view);
		mOverlayType = view.mOverlayType;
		mOverlayColor = view.mOverlayColor;
		mCurrentResolution = view.mCurrentResolution;
		mBrightness = view.mBrightness;
		mContrast = view.mContrast;
		mSaturation = view.mSaturation;
		mColorTemperature = view.mColorTemperature;
	}

}
