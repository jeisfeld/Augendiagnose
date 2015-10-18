package de.eisfeldj.augendiagnosefx.util.imagefile;

import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.OVERLAY_1_PREFIX;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.OVERLAY_2_PREFIX;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.OVERLAY_3_PREFIX;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.OVERLAY_4_PREFIX;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.OVERLAY_5_PREFIX;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.OVERLAY_6_PREFIX;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.OVERLAY_7_PREFIX;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import de.eisfeldj.augendiagnosefx.util.Logger;
import de.eisfeldj.augendiagnosefx.util.PreferenceUtil;
import de.eisfeldj.augendiagnosefx.util.ResourceUtil;
import de.eisfeldj.augendiagnosefx.util.imagefile.EyePhoto.RightLeft;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.effect.DisplacementMap;
import javafx.scene.effect.FloatMap;
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
	 * The relative radius of the iris on the overlay.
	 */
	private static final float OVERLAY_CIRCLE_RATIO = 0.75f;

	/**
	 * The pupil sizes in the original overlay images.
	 */
	private static final float[] ORIG_PUPIL_SIZES = { 0.25f, 0.28f, 0.28f, 0.21f, 0.24f, 0.21f, 0.24f, 0.16f };

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
	 * @param file
	 *            The image file.
	 * @param resolution
	 *            Indicator of the resolution in which the image should be returned.
	 * @return the image.
	 */
	public static Image getImage(final File file, final Resolution resolution) {
		URL url = null;
		try {
			url = file.toURI().toURL();
		}
		catch (MalformedURLException e) {
			Logger.error("Could not convert to URL", e);
			throw new RuntimeException(e);
		}

		int maxSize = resolution == Resolution.THUMB
				? PreferenceUtil.getPreferenceInt(PreferenceUtil.KEY_THUMBNAIL_SIZE)
				: PreferenceUtil.getPreferenceInt(PreferenceUtil.KEY_MAX_BITMAP_SIZE);

		int rotation = JpegMetadataUtil.getExifOrientationAngle(file);

		if (rotation == 0) {
			if (resolution == Resolution.FULL) {
				return new Image(url.toExternalForm(), true);
			}
			else {
				return new Image(url.toExternalForm(), maxSize, maxSize, true, true, true);
			}
		}
		else {
			// need to load in foreground and apply rotation.
			Image image;
			if (resolution == Resolution.FULL) {
				image = new Image(url.toExternalForm());
			}
			else {
				image = new Image(url.toExternalForm(), maxSize, maxSize, true, true);
			}

			double width = image.getWidth();
			double height = image.getHeight();

			Canvas canvas;
			GraphicsContext gc;

			switch (rotation) {
			case 90: // MAGIC_NUMBER
				canvas = new Canvas(height, width);
				gc = canvas.getGraphicsContext2D();
				gc.setTransform(0, 1, -1, 0, height, 0);
				break;
			case 180: // MAGIC_NUMBER
				canvas = new Canvas(width, height);
				gc = canvas.getGraphicsContext2D();
				gc.setTransform(-1, 0, 0, -1, width, height);
				break;
			case 270: // MAGIC_NUMBER
				canvas = new Canvas(height, width);
				gc = canvas.getGraphicsContext2D();
				gc.setTransform(0, -1, 1, 0, 0, width);
				break;
			default:
				canvas = new Canvas(width, height);
				gc = canvas.getGraphicsContext2D();
			}

			gc.drawImage(image, 0, 0);

			return canvas.snapshot(null, null);
		}
	}

	/**
	 * Get the name of an overlay image file.
	 *
	 * @param overlayType
	 *            The type of the overlay.
	 * @param side
	 *            The side of the eye.
	 * @return The file name of the overlay file.
	 */
	private static String getOverlayFileName(final int overlayType, final RightLeft side) {
		String baseName = "";
		switch (overlayType) {
		case 0:
			baseName = "overlay_circle";
			break;
		case 1:
			baseName = ResourceUtil.getString(OVERLAY_1_PREFIX);
			break;
		case 2:
			baseName = ResourceUtil.getString(OVERLAY_2_PREFIX);
			break;
		case 3: // MAGIC_NUMBER
			baseName = ResourceUtil.getString(OVERLAY_3_PREFIX);
			break;
		case 4: // MAGIC_NUMBER
			baseName = ResourceUtil.getString(OVERLAY_4_PREFIX);
			break;
		case 5: // MAGIC_NUMBER
			baseName = ResourceUtil.getString(OVERLAY_5_PREFIX);
			break;
		case 6: // MAGIC_NUMBER
			baseName = ResourceUtil.getString(OVERLAY_6_PREFIX);
			break;
		case 7: // MAGIC_NUMBER
			baseName = ResourceUtil.getString(OVERLAY_7_PREFIX);
			break;
		default:
			baseName = "overlay_topo" + overlayType;
		}

		String suffix = ".png";

		if (overlayType > 0) {
			switch (side) {
			case LEFT:
				suffix = "_l" + suffix;
				break;
			case RIGHT:
				suffix = "_r" + suffix;
				break;
			default:
			}
		}

		return baseName + suffix;
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
	private static Image getOverlayImage(final int overlayType, final RightLeft side, final Color color) {
		URL imageUrl = ClassLoader.getSystemResource("overlay/" + getOverlayFileName(overlayType, side));

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
						colorNoAlpha));

		// Type 2 is not changed in color.
		if (overlayType != 2) {
			canvas.getGraphicsContext2D().setEffect(effect);
		}
		canvas.getGraphicsContext2D().setGlobalAlpha(color.getOpacity());
		canvas.getGraphicsContext2D().drawImage(image, 0, 0, OVERLAY_SIZE, OVERLAY_SIZE);
		SnapshotParameters parameters = new SnapshotParameters();
		parameters.setFill(Color.TRANSPARENT);

		return canvas.snapshot(parameters, null);
	}

	/**
	 * Retrieve an overlay image, warped due to pupil size and position.
	 *
	 * @param overlayType
	 *            The overlay type.
	 * @param side
	 *            The side of the eye.
	 * @param color
	 *            The overlay color.
	 * @param pupilXOffset
	 *            The horizontal offset of the pupil.
	 * @param pupilYOffset
	 *            The vertical offset of the pupil.
	 * @param pupilSize
	 *            The relative size of the pupil.
	 * @return The overlay image.
	 */
	private static Image getOverlayImage(final int overlayType, final RightLeft side, final Color color,
			final float pupilXOffset, final float pupilYOffset, final float pupilSize) {
		if (mCachedOverlayType != null && overlayType == mCachedOverlayType
				&& side == mCachedOverlaySide
				&& color.equals(mCachedOverlayColor)) {
			return mCachedOverlay;
		}

		Image originalImage = getOverlayImage(overlayType, side, color);
		Canvas canvas = new Canvas(OVERLAY_SIZE, OVERLAY_SIZE);

		int overlayHalfSize = OVERLAY_SIZE / 2;
		int irisRadius = (int) (OVERLAY_CIRCLE_RATIO * overlayHalfSize);
		long irisRadiusSquare = irisRadius * irisRadius;
		float pupilXCenter = OVERLAY_SIZE * OVERLAY_CIRCLE_RATIO * pupilXOffset / (1 - pupilSize);
		float pupilYCenter = OVERLAY_SIZE * OVERLAY_CIRCLE_RATIO * pupilYOffset / (1 - pupilSize);
		float origPupilSize = ORIG_PUPIL_SIZES[overlayType];
		float pupilShrinkFactor = origPupilSize / pupilSize;
		float linTransA = pupilSize == 1 ? 0 : (1 - origPupilSize) / (1 - pupilSize);
		float linTransB = 1 - linTransA;

		FloatMap floatMap = new FloatMap(OVERLAY_SIZE, OVERLAY_SIZE);
		for (int x = 0; x < OVERLAY_SIZE; x++) {
			int xPos = x - overlayHalfSize;
			float xPosP = xPos - pupilXCenter;

			for (int y = 0; y < OVERLAY_SIZE; y++) {
				int yPos = y - overlayHalfSize;
				float yPosP = yPos - pupilYCenter;

				long centerDistSquare = xPos * xPos + yPos * yPos;
				float pupilCenterDistSquare = xPosP * xPosP + yPosP * yPosP;

				if (centerDistSquare >= irisRadiusSquare) {
					floatMap.setSamples(x, y, 0, 0);
				}
				else if (pupilCenterDistSquare == 0) {
					floatMap.setSamples(x, y, -xPos / OVERLAY_SIZE, -yPos / OVERLAY_SIZE);
				}
				else {
					// Determine corresponding iris boundary point via quadratic equation
					float plusMinusTerm = (float) Math.sqrt(2 * xPosP * yPosP * pupilXCenter * pupilYCenter
							+ irisRadius * irisRadius * pupilCenterDistSquare
							- (pupilXCenter * pupilXCenter * yPosP * yPosP)
							- (pupilYCenter * pupilYCenter * xPosP * xPosP));

					float xBound = (yPosP * yPosP * pupilXCenter - yPosP * xPosP * pupilYCenter + xPosP * plusMinusTerm) / pupilCenterDistSquare;
					float yBound = (xPosP * xPosP * pupilYCenter - xPosP * yPosP * pupilXCenter + yPosP * plusMinusTerm) / pupilCenterDistSquare;

					// distance of the current point from the center - 1 corresponds to iris boundary
					float relativeDistance = (float) Math.sqrt(pupilCenterDistSquare
							/ ((xBound - pupilXCenter) * (xBound - pupilXCenter) + (yBound - pupilYCenter) * (yBound - pupilYCenter)));

					float sourceRelativeDistance;
					if (relativeDistance <= pupilSize) {
						sourceRelativeDistance = relativeDistance * pupilShrinkFactor;
					}
					else {
						sourceRelativeDistance = linTransA * relativeDistance + linTransB;
					}

					float sourceX = xBound * sourceRelativeDistance;
					float sourceY = yBound * sourceRelativeDistance;

					floatMap.setSamples(x, y, (sourceX - xPos) / OVERLAY_SIZE, (sourceY - yPos) / OVERLAY_SIZE);
				}

			}
		}
		DisplacementMap displacementMap = new DisplacementMap(floatMap);
		canvas.getGraphicsContext2D().setEffect(displacementMap);
		canvas.getGraphicsContext2D().drawImage(originalImage, 0, 0, OVERLAY_SIZE, OVERLAY_SIZE);

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
	 * @param pupilXOffset
	 *            The horizontal offset of the pupil.
	 * @param pupilYOffset
	 *            The vertical offset of the pupil.
	 * @param pupilSize
	 *            The relative size of the pupil.
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
			final Color color, final float xPosition, final float yPosition, final float scaleFactor,
			final float pupilXOffset, final float pupilYOffset, final float pupilSize,
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
			Image overlayImage = getOverlayImage(overlayType, side, color, pupilXOffset, pupilYOffset, pupilSize);
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
			if (metadata.getPupilSize() == null) {
				return ImageUtil.getImageWithOverlay(image, overlayType, eyePhoto.getRightLeft(), color,
						metadata.getXCenter(), metadata.getYCenter(), metadata.getOverlayScaleFactor(),
						0, 0, 0.25f, brightness, contrast, resolution); // MAGIC_NUMBER
			}
			else {
				return ImageUtil.getImageWithOverlay(image, overlayType, eyePhoto.getRightLeft(), color,
						metadata.getXCenter(), metadata.getYCenter(), metadata.getOverlayScaleFactor(),
						metadata.getPupilXOffset(), metadata.getPupilYOffset(), metadata.getPupilSize(),
						brightness, contrast, resolution);
			}
		}
		else {
			return ImageUtil.getImageWithOverlay(image, null, eyePhoto.getRightLeft(), color,
					0, 0, 1, 0, 0, 0.25f, brightness, contrast, resolution); // MAGIC_NUMBER
		}
	}

	/**
	 * Clean the overlay cache.
	 */
	public static void cleanOverlayCache() {
		mCachedOverlay = null;
		mCachedOverlayColor = null;
		mCachedOverlaySide = null;
		mCachedOverlayType = null;
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
