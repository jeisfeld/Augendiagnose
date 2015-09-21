package de.eisfeldj.augendiagnosefx.util.imagefile;

import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.OVERLAY_1_PREFIX;

import java.net.URL;

import de.eisfeldj.augendiagnosefx.util.PreferenceUtil;
import de.eisfeldj.augendiagnosefx.util.ResourceUtil;
import de.eisfeldj.augendiagnosefx.util.imagefile.EyePhoto.RightLeft;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Utility for handling images.
 */
public final class ImageUtil {
	/**
	 * The size of the overlays (in pixels).
	 */
	private static final int OVERLAY_SIZE = 1024;

	/**
	 * A cache of one overlay - to prevent frequent recalculation while sliding brightness and contrast.
	 */
	// JAVADOC:OFF
	private static Image mCachedOverlay;
	private static Integer mCachedOverlayType = null;
	private static RightLeft mCachedOverlaySide;
	private static Color mCachedOverlayColor;

	// JAVADOC:ON

	/**
	 * Do not allow instantiation.
	 */
	private ImageUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get an image from a file.
	 *
	 * @param url
	 *            The image URL.
	 * @param resolution
	 *            Indicator of the resolution in which the image should be returned.
	 * @return the image.
	 */
	public static Image getImage(final URL url, final Resolution resolution) {
		if (resolution == Resolution.FULL) {
			// no specification of size required in case of full resolution.
			return new Image(url.toExternalForm());
		}

		int maxSize = resolution == Resolution.THUMB
				? PreferenceUtil.getPreferenceInt(PreferenceUtil.KEY_THUMBNAIL_SIZE)
				: PreferenceUtil.getPreferenceInt(PreferenceUtil.KEY_MAX_BITMAP_SIZE);
		// Load the thumbnail in the background. Main image needs to be loaded in foreground, as dimensions are
		// required.
		return new Image(url.toExternalForm(), maxSize, maxSize, true, true, true);
	}

	/**
	 * Retrieve an overlay image.
	 *
	 * @param overlayType
	 *            The overlay type.
	 * @param side
	 *            The side of the eye.
	 * @param color
	 *            The overlay color.
	 *
	 * @return The overlay image.
	 */
	public static Image getOverlayImage(final int overlayType, final RightLeft side, final Color color) {
		if (mCachedOverlayType != null && overlayType == mCachedOverlayType
				&& side == mCachedOverlaySide
				&& color.equals(mCachedOverlayColor)) {
			return mCachedOverlay;
		}

		String baseName = "";
		switch (overlayType) {
		case 0:
			baseName = "overlay_circle";
			break;
		case 1:
			baseName = ResourceUtil.getString(OVERLAY_1_PREFIX);
			break;
		default:
			baseName = "overlay_topo" + overlayType;
		}

		String suffix = "";
		switch (side) {
		case LEFT:
			suffix = "_l.png";
			break;
		case RIGHT:
			suffix = "_r.png";
			break;
		default:
		}

		URL imageUrl = ClassLoader.getSystemResource("overlay/" + baseName + suffix);

		Image image = new Image(imageUrl.toExternalForm());

		Canvas canvas = new Canvas(OVERLAY_SIZE, OVERLAY_SIZE);
		Color colorNoAlpha = new Color(color.getRed(), color.getGreen(), color.getBlue(), 1);

		Blend effect = new Blend(
				BlendMode.SRC_ATOP,
				null,
				new ColorInput(
						0,
						0,
						OVERLAY_SIZE,
						OVERLAY_SIZE,
						colorNoAlpha
				)
				);

		// Type 2 is not changed in color.
		if (overlayType != 2) {
			canvas.getGraphicsContext2D().setEffect(effect);
		}
		canvas.getGraphicsContext2D().setGlobalAlpha(color.getOpacity());
		canvas.getGraphicsContext2D().drawImage(image, 0, 0, OVERLAY_SIZE, OVERLAY_SIZE);
		SnapshotParameters parameters = new SnapshotParameters();
		parameters.setFill(Color.TRANSPARENT);

		mCachedOverlay = canvas.snapshot(parameters, null);
		mCachedOverlayType = overlayType;
		mCachedOverlaySide = side;
		mCachedOverlayColor = color;
		return mCachedOverlay;
	}

	/**
	 * Get an image with a displayed overlay.
	 *
	 * @param baseImage
	 *            the base image.
	 * @param overlayType
	 *            The overlay type.
	 * @param side
	 *            The side of the eye.
	 * @param color
	 *            The overlay color.
	 * @param xPosition
	 *            The x position of the overlay.
	 * @param yPosition
	 *            The y position of the overlay.
	 * @param scaleFactor
	 *            The scale factor of the overlay.
	 * @param brightness
	 *            The brightness of the image.
	 * @param contrast
	 *            The contrast of the imabe.
	 * @param resolution
	 *            Indicator of the resolution of the image.
	 * @return The image with overlay.
	 */
	private static Image getImageWithOverlay( // SUPPRESS_CHECKSTYLE Too many parameters
			final Image baseImage, final Integer overlayType, final RightLeft side,
			final Color color, final double xPosition, final double yPosition, final double scaleFactor,
			final float brightness, final float contrast, final Resolution resolution) {
		if (brightness == 0 && contrast == 1 && overlayType == null) {
			return baseImage;
		}

		double width = baseImage.getWidth();
		double height = baseImage.getHeight();
		double overlaySize = Math.max(width, height) * scaleFactor;

		// logic of brightness and contrast does not work very well. Therefore, simulating logic from android
		// OverlayPinghImageView.changeBitmapContrastBrightness
		Canvas canvas = new Canvas(width, height);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.drawImage(baseImage, 0, 0, width, height);

		if (contrast != 1 || brightness != 0) {
			// The offset which needs to be added after multiplying by contrast.
			float offset = 1f / 2 * (1 - contrast + brightness * contrast + brightness); // MAGIC_NUMBER

			// The following just wants to multiply by contrast, followed by addition of offset.
			// We achieve this by overlaying grey rectangles with varying blend mode.
			// Various cases to ensure that all intermediate values are in the range [0,1]
			if (contrast <= 1) {
				gc.setGlobalBlendMode(BlendMode.MULTIPLY);
				gc.setFill(new Color(contrast, contrast, contrast, 1));
				gc.fillRect(0, 0, width, height);
				if (offset > 0) {
					gc.setGlobalBlendMode(BlendMode.ADD);
					gc.setFill(new Color(offset, offset, offset, 1));
					gc.fillRect(0, 0, width, height);
				}
				else {
					// Subtract is achieved by difference - add - difference.
					gc.setGlobalBlendMode(BlendMode.DIFFERENCE);
					gc.setFill(Color.WHITE);
					gc.fillRect(0, 0, width, height);
					gc.setGlobalBlendMode(BlendMode.ADD);
					gc.setFill(new Color(-offset, -offset, -offset, 1));
					gc.fillRect(0, 0, width, height);
					gc.setGlobalBlendMode(BlendMode.DIFFERENCE);
					gc.setFill(Color.WHITE);
					gc.fillRect(0, 0, width, height);
				}
			}
			else {
				float invContrast = 1 - (1 / contrast);
				if (offset >= 0) {
					gc.setGlobalBlendMode(BlendMode.COLOR_DODGE);
					gc.setFill(new Color(invContrast, invContrast, invContrast, 1));
					gc.fillRect(0, 0, width, height);
					gc.setGlobalBlendMode(BlendMode.ADD);
					gc.setFill(new Color(offset, offset, offset, 1));
					gc.fillRect(0, 0, width, height);
				}
				else {
					gc.setGlobalBlendMode(BlendMode.DIFFERENCE);
					gc.setFill(Color.WHITE);
					gc.fillRect(0, 0, width, height);
					float delta = -offset / contrast;
					gc.setGlobalBlendMode(BlendMode.ADD);
					gc.setFill(new Color(delta, delta, delta, 1));
					gc.fillRect(0, 0, width, height);
					gc.setGlobalBlendMode(BlendMode.DIFFERENCE);
					gc.setFill(Color.WHITE);
					gc.fillRect(0, 0, width, height);
					gc.setGlobalBlendMode(BlendMode.COLOR_DODGE);
					gc.setFill(new Color(invContrast, invContrast, invContrast, 1));
					gc.fillRect(0, 0, width, height);
				}
			}
		}

		if (overlayType != null) {
			Image overlayImage = getOverlayImage(overlayType, side, color);
			gc.setEffect(null);
			gc.setGlobalBlendMode(BlendMode.SRC_OVER);
			gc.drawImage(overlayImage, xPosition * width - overlaySize / 2,
					yPosition * height - overlaySize / 2, overlaySize, overlaySize);
		}

		return canvas.snapshot(null, null);
	}

	/**
	 * Get an eye photo image with a displayed overlay, positioned via the metadata.
	 *
	 * @param eyePhoto
	 *            The eye photo image.
	 * @param overlayType
	 *            The overlay type.
	 * @param color
	 *            The overlay color.
	 * @param brightness
	 *            The brightness of the image.
	 * @param contrast
	 *            The contrast of the image.
	 * @param resolution
	 *            Indicator of the resolution of the image.
	 * @return The image with overlay.
	 */
	public static Image getImageForDisplay(final EyePhoto eyePhoto, final Integer overlayType,
			final Color color, final float brightness, final float contrast, final Resolution resolution) {
		Image image = eyePhoto.getImage(resolution);
		JpegMetadata metadata = eyePhoto.getImageMetadata();
		if (resolution == Resolution.FULL) {
			// Full resolution does not allow use of Canvas to set brightness, contrast and overlay.
			return image;
		}
		else if (metadata != null && metadata.hasOverlayPosition() && overlayType != null) {
			return ImageUtil.getImageWithOverlay(image, overlayType, eyePhoto.getRightLeft(), color,
					metadata.mXCenter, metadata.mYCenter,
					metadata.mOverlayScaleFactor, brightness, contrast, resolution);
		}
		else {
			return ImageUtil.getImageWithOverlay(image, null, eyePhoto.getRightLeft(), color,
					0, 0, 0, brightness, contrast, resolution);
		}
	}

	/**
	 * Enumeration indicating the resolution with which the image should be displayed.
	 */
	public enum Resolution {
		/**
		 * The resolution values.
		 */
		FULL, NORMAL, THUMB;
	}
}
