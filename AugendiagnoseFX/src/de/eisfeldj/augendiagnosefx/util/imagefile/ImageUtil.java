package de.eisfeldj.augendiagnosefx.util.imagefile;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

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
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;

import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.OVERLAY_1_PREFIX;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.OVERLAY_2_PREFIX;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.OVERLAY_3_PREFIX;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.OVERLAY_4_PREFIX;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.OVERLAY_5_PREFIX;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.OVERLAY_6_PREFIX;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.OVERLAY_7_PREFIX;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.OVERLAY_8_PREFIX;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.OVERLAY_9_PREFIX;

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
	private static final float[] ORIG_PUPIL_SIZES = {0.25f, 0.28f, 0.28f, 0.21f, 0.24f, 0.24f, 0.21f, 0.24f, 0.16f, 0.24f};

	/**
	 * The number four.
	 */
	private static final int FOUR = 4;
	/**
	 * The size of a byte.
	 */
	private static final int BYTE = 0xFF;

	/**
	 * A cache of one overlay - to prevent frequent recalculation while sliding brightness and contrast.
	 */
	// JAVADOC:OFF
	private static Image mCachedOverlay;
	private static Integer mCachedOverlayType = null;
	private static RightLeft mCachedOverlaySide;
	private static Color mCachedOverlayColor;
	private static float mCachedPupilSize;
	private static float mCachedPupilXOffset;
	private static float mCachedPupilYOffset;

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
				return new Image(url.toExternalForm());
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
		case 8: // MAGIC_NUMBER
			baseName = ResourceUtil.getString(OVERLAY_8_PREFIX);
			break;
		case 9: // MAGIC_NUMBER
			baseName = ResourceUtil.getString(OVERLAY_9_PREFIX);
			break;
		default:
			return null;
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
		if (mCachedOverlayType != null && overlayType == mCachedOverlayType // BOOLEAN_EXPRESSION_COMPLEXITY
				&& side == mCachedOverlaySide && color.equals(mCachedOverlayColor)
				&& pupilXOffset == mCachedPupilXOffset && pupilYOffset == mCachedPupilYOffset && pupilSize == mCachedPupilSize) {
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
		float linTransM = pupilSize == 1 ? 0 : (1 - origPupilSize) / (1 - pupilSize);
		float linTransB = 1 - linTransM;

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

					float sourceRelativeDistance = linTransM * relativeDistance + linTransB;
					if (relativeDistance < pupilSize) {
						sourceRelativeDistance -= linTransB * Math.pow(1 - relativeDistance / pupilSize, 1.1f); // MAGIC_NUMBER
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
		mCachedPupilSize = pupilSize;
		mCachedPupilXOffset = pupilXOffset;
		mCachedPupilYOffset = pupilYOffset;
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
	 * @param saturation
	 *            The saturation of the image.
	 * @param colorTemperature
	 *            The color temperature of the imabe.
	 * @param resolution
	 *            Indicator of the resolution of the image.
	 * @return The image with overlay.
	 */
	private static Image getImageWithOverlay( // SUPPRESS_CHECKSTYLE Too many parameters
			final Image baseImage, final Integer overlayType, final RightLeft side,
			final Color color, final float xPosition, final float yPosition, final float scaleFactor,
			final float pupilXOffset, final float pupilYOffset, final float pupilSize,
			final float brightness, final float contrast,
			final float saturation, final float colorTemperature, final Resolution resolution) {
		boolean hasOriginalColors = brightness == 0 && contrast == 1 && saturation == 1 && colorTemperature == 0;
		if (hasOriginalColors && overlayType == null) {
			return baseImage;
		}

		int width = (int) baseImage.getWidth();
		int height = (int) baseImage.getHeight();
		double overlaySize = Math.max(width, height) * scaleFactor;

		// logic of brightness and contrast does not work very well. Therefore, simulating logic from android
		// OverlayPinghImageView.changeBitmapContrastBrightness
		Canvas canvas = new Canvas(width, height);
		GraphicsContext gc = canvas.getGraphicsContext2D();

		if (hasOriginalColors) {
			gc.drawImage(baseImage, 0, 0, width, height);
		}
		else {
			Color temperatureColor = convertTemperatureToColor(colorTemperature);
			float factorBlue = 1 / (float) temperatureColor.getBlue();
			float factorGreen = 1 / (float) temperatureColor.getGreen();
			float factorRed = 1 / (float) temperatureColor.getRed();
			float correctionFactor = (float) Math.pow(factorRed * factorGreen * factorBlue, -1f / 3); // MAGIC_NUMBER
			factorBlue *= correctionFactor * contrast;
			factorGreen *= correctionFactor * contrast;
			factorRed *= correctionFactor * contrast;
			float offset = BYTE / 2f * (1 - contrast + brightness * contrast + brightness);
			float oppositeSaturation = (1 - saturation) / 2;

			WritablePixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteBgraInstance();
			byte[] buffer = new byte[FOUR * width * height];

			baseImage.getPixelReader().getPixels(0, 0, width, height, pixelFormat, buffer, 0, FOUR * width);

			for (int i = 0; i < buffer.length; i += FOUR) {
				float blueIn = (buffer[i] & BYTE) * factorBlue;
				float greenIn = (buffer[i + 1] & BYTE) * factorGreen;
				float redIn = (buffer[i + 2] & BYTE) * factorRed;

				buffer[i] = toColorByte(saturation * blueIn + oppositeSaturation * greenIn + oppositeSaturation * redIn + offset);
				buffer[i + 1] = toColorByte(oppositeSaturation * blueIn + saturation * greenIn + oppositeSaturation * redIn + offset);
				buffer[i + 2] = toColorByte(oppositeSaturation * blueIn + oppositeSaturation * greenIn + saturation * redIn + offset);
			}

			gc.getPixelWriter().setPixels(0, 0, width, height, pixelFormat, buffer, 0, FOUR * width);
		}

		if (overlayType != null) {
			Image overlayImage = getOverlayImage(overlayType, side, color, pupilXOffset, pupilYOffset, pupilSize);
			gc.setEffect(null);
			gc.setGlobalBlendMode(BlendMode.SRC_OVER);
			gc.drawImage(overlayImage, xPosition * width - overlaySize / 2,
					yPosition * height - overlaySize / 2, overlaySize, overlaySize);
		}

//		PupilAndIrisDetector detector = new PupilAndIrisDetector(baseImage);
//		gc.setStroke(Color.RED);
//		gc.setLineWidth(5); // MAGIC_NUMBER
//		int irisRadius = (int) (detector.getIrisRadius() * Math.max(baseImage.getHeight(), baseImage.getWidth()));
//		int irisXCenter = (int) (detector.getIrisXCenter() * baseImage.getWidth());
//		int irisYCenter = (int) (detector.getIrisYCenter() * baseImage.getHeight());
//		gc.strokeOval(irisXCenter - irisRadius, irisYCenter - irisRadius, 2 * irisRadius, 2 * irisRadius);
//		int pupilRadius = (int) (detector.getPupilRadius() * Math.max(baseImage.getHeight(), baseImage.getWidth()));
//		int pupilXCenter = (int) (detector.getPupilXCenter() * baseImage.getWidth());
//		int pupilYCenter = (int) (detector.getPupilYCenter() * baseImage.getHeight());
//		gc.strokeOval(pupilXCenter - pupilRadius, pupilYCenter - pupilRadius, 2 * pupilRadius, 2 * pupilRadius);

		return canvas.snapshot(null, null);
	}

	/**
	 * Convert a number into a byte (ensuring the appropriate range).
	 *
	 * @param number The number.
	 * @return The resulting byte.
	 */
	private static byte toColorByte(final float number) {
		return (byte) Math.min(BYTE, Math.max(0, number));
	}

	/**
	 * Convert a temperature into a color value representing the color of this temperature.
	 *
	 * @param temperature The temperature value (in the range -1..1).
	 * @return The color value.
	 */
	private static Color convertTemperatureToColor(final double temperature) {
		if (temperature >= 0) {
			return Color.rgb((int) (BYTE - 150 * temperature), (int) (BYTE - 105 * temperature), BYTE); // MAGIC_NUMBER
		}
		else {
			return Color.rgb(BYTE, (int) (BYTE + 80 * temperature), (int) (BYTE + 145 * temperature)); // MAGIC_NUMBER
		}
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
	 * @param saturation
	 *            The saturation of the image.
	 * @param colorTemperature
	 *            The color temperature of the image.
	 * @param resolution
	 *            Indicator of the resolution of the image.
	 * @return The image with overlay.
	 */
	public static Image getImageForDisplay(final EyePhoto eyePhoto, // SUPPRESS_CHECKSTYLE Too many parameters
			final Integer overlayType, final Color color, final float brightness, final float contrast,
			final float saturation, final float colorTemperature, final Resolution resolution) {
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
						0, 0, 0.25f, brightness, contrast, saturation, colorTemperature, resolution); // MAGIC_NUMBER
			}
			else {
				return ImageUtil.getImageWithOverlay(image, overlayType, eyePhoto.getRightLeft(), color,
						metadata.getXCenter(), metadata.getYCenter(), metadata.getOverlayScaleFactor(),
						metadata.getPupilXOffset(), metadata.getPupilYOffset(), metadata.getPupilSize(),
						brightness, contrast, saturation, colorTemperature, resolution);
			}
		}
		else {
			return ImageUtil.getImageWithOverlay(image, null, eyePhoto.getRightLeft(), color,
					0, 0, 1, 0, 0, 0.25f, brightness, contrast, saturation, colorTemperature, resolution); // MAGIC_NUMBER
		}
	}

	/**
	 * Resize an image to the given size.
	 *
	 * @param baseImage The original image.
	 * @param targetSize The target size.
	 * @param allowGrowing flag indicating if the image is allowed to grow.
	 * @return the resized image.
	 */
	public static Image resizeImage(final Image baseImage, final int targetSize, final boolean allowGrowing) {
		if (baseImage == null || baseImage.getWidth() == 0 || baseImage.getHeight() == 0) {
			return baseImage;
		}
		if (baseImage.getWidth() <= targetSize && baseImage.getHeight() <= targetSize && !allowGrowing) {
			return baseImage;
		}
		int targetWidth;
		int targetHeight;
		if (baseImage.getWidth() > baseImage.getHeight()) {
			targetWidth = targetSize;
			targetHeight = (int) (targetSize * baseImage.getHeight() / baseImage.getWidth());
		}
		else {
			targetWidth = (int) (targetSize * baseImage.getWidth() / baseImage.getHeight());
			targetHeight = targetSize;
		}

		Canvas canvas = new Canvas(targetWidth, targetHeight);
		canvas.getGraphicsContext2D().drawImage(baseImage, 0, 0, targetWidth, targetHeight);
		return canvas.snapshot(null, null);
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
