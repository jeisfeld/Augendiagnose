package de.eisfeldj.augendiagnosefx.util;

import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.OVERLAY_1_PREFIX;

import java.net.URL;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import de.eisfeldj.augendiagnosefx.util.EyePhoto.RightLeft;

/**
 * Utility for handling images.
 */
public final class ImageUtil {
	/**
	 * The size of the overlays (in pixels).
	 */
	private static final int OVERLAY_SIZE = 1024;

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
	 * @return the image.
	 */
	public static Image getImage(final URL url) {
		int maxSize = PreferenceUtil.getPreferenceInt(PreferenceUtil.KEY_MAX_BITMAP_SIZE);
		return new Image(url.toExternalForm(), maxSize, maxSize, true, true);
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
	 * @return The overlay image.
	 */
	public static Image getOverlayImage(final int overlayType, final RightLeft side, final Color color) {
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

		URL imageURL = ClassLoader.getSystemResource("overlay/" + baseName + suffix);

		Image image = getImage(imageURL);

		// Type 2 is not changed in color.
		if (overlayType == 2) {
			return image;
		}

		Canvas canvas = new Canvas(OVERLAY_SIZE, OVERLAY_SIZE);

		Blend effect = new Blend(
				BlendMode.SRC_ATOP,
				null,
				new ColorInput(
						0,
						0,
						OVERLAY_SIZE,
						OVERLAY_SIZE,
						color
				)
				);

		canvas.getGraphicsContext2D().setEffect(effect);
		canvas.getGraphicsContext2D().drawImage(image, 0, 0, OVERLAY_SIZE, OVERLAY_SIZE);
		SnapshotParameters parameters = new SnapshotParameters();
		parameters.setFill(Color.TRANSPARENT);
		return canvas.snapshot(parameters, null);
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
	 * @return The image with overlay.
	 */
	private static Image getImageWithOverlay(final Image baseImage, final int overlayType, final RightLeft side,
			final Color color, final double xPosition, final double yPosition, final double scaleFactor) {
		double width = baseImage.getWidth();
		double height = baseImage.getHeight();
		double overlaySize = Math.max(width, height) * scaleFactor;

		Image overlayImage = null;
		overlayImage = getOverlayImage(overlayType, side, color);

		Canvas canvas = new Canvas(width, height);
		canvas.getGraphicsContext2D().drawImage(baseImage, 0, 0, width, height);
		canvas.getGraphicsContext2D().setGlobalAlpha(color.getOpacity());

		canvas.getGraphicsContext2D().drawImage(overlayImage, xPosition * width - overlaySize / 2,
				yPosition * height - overlaySize / 2, overlaySize, overlaySize);

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
	 * @return The image with overlay.
	 */
	public static Image getImageWithOverlay(final EyePhoto eyePhoto, final Integer overlayType, final Color color) {
		Image image = eyePhoto.getImage();
		JpegMetadata metadata = eyePhoto.getImageMetadata();

		if (metadata != null && metadata.hasOverlayPosition() && overlayType != null) {
			return ImageUtil.getImageWithOverlay(image, overlayType, eyePhoto.getRightLeft(), color,
					metadata.xCenter, metadata.yCenter,
					metadata.overlayScaleFactor);
		}
		else {
			return image;
		}
	}

}
