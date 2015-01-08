package de.eisfeldj.augendiagnosefx.util;

import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.OVERLAY_1_PREFIX;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
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
	 * @param file
	 *            The image file.
	 * @return the image.
	 */
	public static Image getImage(final File file) {
		int maxSize = PreferenceUtil.getPreferenceInt(PreferenceUtil.KEY_MAX_BITMAP_SIZE);
		try {
			return new Image(file.toURI().toURL().toExternalForm(), maxSize, maxSize, true, true);
		}
		catch (MalformedURLException e) {
			Logger.error("Could not create image " + file.getAbsolutePath());
			return null;
		}
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
	public static Image getOverlayImage(final int overlayType, final RightLeft side, final Paint color) {
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

		File imageFile = new File("resources/overlay/" + baseName + suffix);
		if (!imageFile.exists()) {
			throw new RuntimeException(new FileNotFoundException("Could not find image file " + "overlay/" + baseName
					+ suffix));
		}

		Image image = getImage(imageFile);

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
	public static Image getImageWithOverlay(final Image baseImage, final int overlayType, final RightLeft side,
			final Paint color, final double xPosition, final double yPosition, final double scaleFactor) {
		double width = baseImage.getWidth();
		double height = baseImage.getHeight();
		double overlaySize = Math.max(width, height) * scaleFactor;

		Image overlayImage = getOverlayImage(overlayType, side, color);

		Canvas canvas = new Canvas(width, height);
		canvas.getGraphicsContext2D().drawImage(baseImage, 0, 0, width, height);
		canvas.getGraphicsContext2D().drawImage(overlayImage, xPosition * width - overlaySize / 2,
				yPosition * height - overlaySize / 2, overlaySize, overlaySize);

		return canvas.snapshot(null, null);
	}
}
