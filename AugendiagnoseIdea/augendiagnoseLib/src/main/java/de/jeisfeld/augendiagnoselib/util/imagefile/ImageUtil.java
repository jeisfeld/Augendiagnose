package de.jeisfeld.augendiagnoselib.util.imagefile;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.components.OverlayPinchImageView;
import de.jeisfeld.augendiagnoselib.util.DateUtil;

/**
 * Utility class for operations with images.
 */
public final class ImageUtil {
	// JAVADOC:OFF
	// Rotation angles
	private static final int ROTATION_90 = 90;
	private static final int ROTATION_180 = 180;
	private static final int ROTATION_270 = 270;

	// JAVADOC:ON

	/**
	 * The size of the mesh used to deform the overlays.
	 */
	private static final int OVERLAY_MESH_SIZE = 128;

	/**
	 * Number of milliseconds for retry of getting bitmap.
	 */
	private static final long BITMAP_RETRY = 50;

	/**
	 * The file endings considered as image files.
	 */
	private static final List<String> IMAGE_SUFFIXES = Arrays.asList("JPG", "JPEG", "PNG", "BMP", "TIF", "TIFF", "GIF");

	/**
	 * The precision for saving jpeg of views.
	 */
	private static final int JPEG_PRECISION = 95;

	/**
	 * The max size of a byte.
	 */
	private static final int BYTE = 255;

	/**
	 * Hide default constructor.
	 */
	private ImageUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the date field with the EXIF date from the file If not existing, use the last modified date.
	 *
	 * @param path The file path of the image
	 * @return the date stored in the EXIF data.
	 */
	public static Date getExifDate(@NonNull final String path) {
		Date retrievedDate = null;
		try {
			ExifInterface exif = new ExifInterface(path);
			String dateString = exif.getAttribute(ExifInterface.TAG_DATETIME);

			if (dateString == null) {
				dateString = JpegMetadataUtil.getExifDate(new File(path));
			}

			retrievedDate = DateUtil.parse(dateString, "yyyy:MM:dd HH:mm:ss");
		}
		catch (Exception e) {
			Log.w(Application.TAG, e + " - Cannot retrieve EXIF date for " + path);
		}
		if (retrievedDate == null) {
			File f = new File(path);
			retrievedDate = new Date(f.lastModified());
		}
		return retrievedDate;
	}

	/**
	 * Retrieve the image orientation from the Exif data of an image.
	 *
	 * @param path The file path of the image
	 * @return the orientation stored in the exif data.
	 */
	private static int getExifOrientation(@NonNull final String path) {
		try {
			ExifInterface exif = new ExifInterface(path);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

			if (orientation == ExifInterface.ORIENTATION_UNDEFINED) {
				// Use custom implementation, as the previous one is not always reliable
				orientation = JpegMetadataUtil.getExifOrientation(new File(path));
			}

			return orientation;
		}
		catch (Exception e) {
			Log.w(Application.TAG, "Exception when getting EXIF rotation");
			return ExifInterface.ORIENTATION_NORMAL;
		}
	}

	/**
	 * Convert the orientation as stored in EXIF metadata into degrees.
	 *
	 * @param exifOrientation The orientation as stored in the exif data.
	 * @return the rotation in degrees.
	 */
	private static int convertExifOrientationToRotation(final int exifOrientation) {
		switch (exifOrientation) {
		case ExifInterface.ORIENTATION_ROTATE_270:
			return ROTATION_270;
		case ExifInterface.ORIENTATION_ROTATE_180:
			return ROTATION_180;
		case ExifInterface.ORIENTATION_ROTATE_90:
			return ROTATION_90;
		default:
			return 0;
		}
	}

	/**
	 * Get the EXIF angle after rotating the image.
	 *
	 * @param originalAngle The original EXIF angle
	 * @param rotationAngle The EXIF style rotation angle
	 * @return the EXIF angle after rotation
	 */
	public static short getRotatedExifAngle(final Short originalAngle, final short rotationAngle) {
		if (originalAngle == null) {
			return rotationAngle;
		}

		switch (originalAngle) {
		case ExifInterface.ORIENTATION_NORMAL:
			return rotationAngle;
		case ExifInterface.ORIENTATION_ROTATE_90:
			switch (rotationAngle) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				return ExifInterface.ORIENTATION_ROTATE_180;
			case ExifInterface.ORIENTATION_ROTATE_180:
				return ExifInterface.ORIENTATION_ROTATE_270;
			case ExifInterface.ORIENTATION_ROTATE_270:
				return ExifInterface.ORIENTATION_NORMAL;
			default:
				return originalAngle;
			}
		case ExifInterface.ORIENTATION_ROTATE_180:
			switch (rotationAngle) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				return ExifInterface.ORIENTATION_ROTATE_270;
			case ExifInterface.ORIENTATION_ROTATE_180:
				return ExifInterface.ORIENTATION_NORMAL;
			case ExifInterface.ORIENTATION_ROTATE_270:
				return ExifInterface.ORIENTATION_ROTATE_90;
			default:
				return originalAngle;
			}
		case ExifInterface.ORIENTATION_ROTATE_270:
			switch (rotationAngle) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				return ExifInterface.ORIENTATION_NORMAL;
			case ExifInterface.ORIENTATION_ROTATE_180:
				return ExifInterface.ORIENTATION_ROTATE_90;
			case ExifInterface.ORIENTATION_ROTATE_270:
				return ExifInterface.ORIENTATION_ROTATE_180;
			default:
				return originalAngle;
			}
		default:
			return originalAngle;
		}
	}

	/**
	 * Return a bitmap of this photo.
	 *
	 * @param path        The file path of the image.
	 * @param maxSize     The maximum size of this bitmap. If bigger, it will be resized.
	 * @return the bitmap.
	 */
	@Nullable
	public static Bitmap getImageBitmap(@NonNull final String path, final int maxSize) {
		Bitmap bitmap = null;
		boolean needToAdjustRotation = true;

		if (maxSize <= 0) {
			bitmap = BitmapFactory.decodeFile(path);
		}
		else {

			if (maxSize <= MediaStoreUtil.MINI_THUMB_SIZE) {
				bitmap = MediaStoreUtil.getThumbnailFromPath(path, maxSize);
				needToAdjustRotation = bitmap == null;
			}

			if (bitmap == null) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = getBitmapFactor(path, maxSize);
				// options.inPurgeable = true;
				bitmap = BitmapFactory.decodeFile(path, options);
				if (bitmap == null) {
					// cannot create bitmap - try once more in case that the image was just in process of saving
					// metadata
					try {
						Thread.sleep(BITMAP_RETRY);
					}
					catch (InterruptedException e) {
						// ignore exception
					}
					bitmap = BitmapFactory.decodeFile(path, options);

					if (bitmap == null) {
						// cannot create bitmap - return dummy
						Log.w(Application.TAG, "Cannot create bitmap from path " + path + " - return dummy bitmap");
						return getDummyBitmap();
					}
				}
			}
			if (bitmap.getWidth() == 0 || bitmap.getHeight() == 0) {
				return bitmap;
			}

			if (bitmap.getWidth() > maxSize || bitmap.getHeight() > maxSize
					|| maxSize <= MediaStoreUtil.MINI_THUMB_SIZE) {
				// Only if bitmap is bigger than maxSize, then resize it - but don't trust the thumbs from media store.
				if (bitmap.getWidth() > bitmap.getHeight()) {
					int targetWidth = maxSize;
					int targetHeight = bitmap.getHeight() * maxSize / bitmap.getWidth();
					bitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false);
				}
				else {
					int targetWidth = bitmap.getWidth() * maxSize / bitmap.getHeight();
					int targetHeight = maxSize;
					bitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false);
				}
			}

		}

		if (needToAdjustRotation) {
			bitmap = rotateBitmap(bitmap, (short) getExifOrientation(path));
		}

		return bitmap;
	}

	/**
	 * Return a bitmap of a photo directly from byte array data.
	 *
	 * @param data    The byte array data of the bitmap.
	 * @param maxSize The maximum size of this bitmap. If bigger, it will be resized.
	 * @return the bitmap.
	 */
	public static Bitmap getImageBitmap(@NonNull final byte[] data, final int maxSize) {
		Bitmap bitmap;

		if (maxSize <= 0) {
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		}
		else {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = getBitmapFactor(data, maxSize);
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
			bitmap = resizeBitmap(bitmap, maxSize, false);
		}

		return bitmap;
	}

	/**
	 * Resize a bitmap to the given size.
	 *
	 * @param baseBitmap   The original bitmap.
	 * @param targetSize   The target size.
	 * @param allowGrowing flag indicating if the image is allowed to grow.
	 * @return the resized image.
	 */
	public static Bitmap resizeBitmap(final Bitmap baseBitmap, final int targetSize, final boolean allowGrowing) {
		if (baseBitmap.getWidth() == 0 || baseBitmap.getHeight() == 0) {
			return baseBitmap;
		}
		else if (baseBitmap.getWidth() > targetSize || baseBitmap.getHeight() > targetSize || allowGrowing) {
			if (baseBitmap.getWidth() > baseBitmap.getHeight()) {
				int targetWidth = targetSize;
				int targetHeight = baseBitmap.getHeight() * targetSize / baseBitmap.getWidth();
				return Bitmap.createScaledBitmap(baseBitmap, targetWidth, targetHeight, false);
			}
			else {
				int targetWidth = baseBitmap.getWidth() * targetSize / baseBitmap.getHeight();
				int targetHeight = targetSize;
				return Bitmap.createScaledBitmap(baseBitmap, targetWidth, targetHeight, false);
			}
		}
		else {
			return baseBitmap;
		}
	}

	/**
	 * Retrieve a part of a bitmap in full resolution.
	 *
	 * @param fullBitmap The bitmap from which to get the part.
	 * @param minX       The minimum X position to retrieve.
	 * @param maxX       The maximum X position to retrieve.
	 * @param minY       The minimum Y position to retrieve.
	 * @param maxY       The maximum Y position to retrieve.
	 * @return The bitmap.
	 */
	public static Bitmap getPartialBitmap(@NonNull final Bitmap fullBitmap, final float minX, final float maxX,
										  final float minY,
										  final float maxY) {

		return Bitmap.createBitmap(fullBitmap, Math.round(minX * fullBitmap.getWidth()),
				Math.round(minY * fullBitmap.getHeight()),
				Math.round((maxX - minX) * fullBitmap.getWidth()),
				Math.round((maxY - minY) * fullBitmap.getHeight()));
	}

	/**
	 * Utility to retrieve the sample size for BitmapFactory.decodeFile.
	 *
	 * @param filepath   the path of the bitmap.
	 * @param targetSize the target size of the bitmap
	 * @return the sample size to be used.
	 */
	private static int getBitmapFactor(final String filepath, final int targetSize) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filepath, options);
		int size = Math.max(options.outWidth, options.outHeight);
		return size / targetSize;
	}

	/**
	 * Utility to retrieve the sample size for BitmapFactory.decodeFile.
	 *
	 * @param data       the data of the bitmap.
	 * @param targetSize the target size of the bitmap
	 * @return the sample size to be used.
	 */
	private static int getBitmapFactor(@NonNull final byte[] data, final int targetSize) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);
		int size = Math.max(options.outWidth, options.outHeight);
		return size / targetSize;
	}

	/**
	 * Rotate a bitmap.
	 *
	 * @param source The original bitmap
	 * @param orientation  The EXIF orientation
	 * @return the rotated bitmap.
	 */
	public static Bitmap rotateBitmap(@NonNull final Bitmap source, final int orientation) {
		int angle = convertExifOrientationToRotation(orientation);
		if (angle == 0) {
			return source;
		}

		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}

	/**
	 * Update contrast and brightness of a bitmap.
	 *
	 * @param bmp              input bitmap
	 * @param contrast         0..infinity - 1 is default
	 * @param brightness       -1..1 - 0 is default
	 * @param saturation       1/3..infinity - 1 is default
	 * @param colorTemperature -1..1 - 0 is default
	 * @return new bitmap
	 */
	public static Bitmap changeBitmapColors(@NonNull final Bitmap bmp, final float contrast, final float brightness,
											final float saturation, final float colorTemperature) {
		if (contrast == 1 && brightness == 0 && saturation == 1 && colorTemperature == 0) {
			return bmp;
		}

		// some baseCalculations for the mapping matrix
		int temperatureColor = ImageUtil.convertTemperatureToColor(colorTemperature);
		float factorRed = (float) BYTE / Color.red(temperatureColor);
		float factorGreen = (float) BYTE / Color.green(temperatureColor);
		float factorBlue = (float) BYTE / Color.blue(temperatureColor);
		float correctionFactor = (float) Math.pow(factorRed * factorGreen * factorBlue, -1f / 3); // MAGIC_NUMBER
		factorRed *= correctionFactor * contrast;
		factorGreen *= correctionFactor * contrast;
		factorBlue *= correctionFactor * contrast;
		float offset = BYTE / 2f * (1 - contrast + brightness * contrast + brightness);
		float oppositeSaturation = (1 - saturation) / 2;

		ColorMatrix cm = new ColorMatrix(new float[]{ //
				factorRed * saturation, factorGreen * oppositeSaturation, factorBlue * oppositeSaturation, 0, offset, //
				factorRed * oppositeSaturation, factorGreen * saturation, factorBlue * oppositeSaturation, 0, offset, //
				factorRed * oppositeSaturation, factorGreen * oppositeSaturation, factorBlue * saturation, 0, offset, //
				0, 0, 0, 1, 0});

		Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

		Canvas canvas = new Canvas(ret);

		Paint paint = new Paint();
		paint.setColorFilter(new ColorMatrixColorFilter(cm));
		canvas.drawBitmap(bmp, 0, 0, paint);

		return ret;
	}

	/**
	 * Convert a temperature into a color value representing the color of this temperature.
	 *
	 * @param temperature The temperature value (in the range -1..1).
	 * @return The color value.
	 */
	private static int convertTemperatureToColor(final double temperature) {
		if (temperature >= 0) {
			return Color.rgb((int) (BYTE - 150 * temperature), (int) (BYTE - 105 * temperature), BYTE); // MAGIC_NUMBER
		}
		else {
			return Color.rgb(BYTE, (int) (BYTE + 80 * temperature), (int) (BYTE + 145 * temperature)); // MAGIC_NUMBER
		}
	}

	/**
	 * Get Mime type from URI.
	 *
	 * @param uri The URI
	 * @return the mime type.
	 */
	public static String getMimeType(@NonNull final Uri uri) {
		ContentResolver contentResolver = Application.getAppContext().getContentResolver();
		String mimeType = contentResolver.getType(uri);
		if (mimeType == null) {
			String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
			if (extension != null) {
				extension = extension.toLowerCase(Locale.getDefault());
			}
			mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
			if (mimeType == null) {
				mimeType = "unknown";
			}
		}
		return mimeType;
	}

	/**
	 * Check if a file is an image file.
	 *
	 * @param file   The file
	 * @param strict if true, then the file content will be checked, otherwise the suffix is sufficient.
	 * @return true if it is an image file.
	 */
	private static boolean isImage(@Nullable final File file, final boolean strict) {
		if (file == null || !file.exists() || file.isDirectory()) {
			return false;
		}
		if (!strict) {
			String fileName = file.getName();
			int index = fileName.lastIndexOf('.');
			if (index >= 0) {
				String suffix = fileName.substring(index + 1);
				if (IMAGE_SUFFIXES.contains(suffix.toUpperCase(Locale.getDefault()))) {
					return true;
				}
			}
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file.getPath(), options);
		return options.outWidth >= 0 && options.outHeight >= 0;
	}

	/**
	 * Get the list of image files in a folder.
	 *
	 * @param folderName The folder name.
	 * @return The list of image files in this folder.
	 */
	@NonNull
	public static ArrayList<String> getImagesInFolder(@Nullable final String folderName) {
		ArrayList<String> fileNames = new ArrayList<>();
		if (folderName == null) {
			return fileNames;
		}
		File folder = new File(folderName);
		if (!folder.exists() || !folder.isDirectory()) {
			return fileNames;
		}
		File[] imageFiles = folder.listFiles(file -> isImage(file, false));
		if (imageFiles == null) {
			return fileNames;
		}

		for (File file : imageFiles) {
			fileNames.add(file.getAbsolutePath());
		}
		return fileNames;
	}

	/**
	 * Utility method to change a bitmap colour.
	 *
	 * @param sourceBitmap The original bitmap
	 * @param color        The target color
	 * @return the bitmap with the target color.
	 */
	public static Bitmap changeBitmapColor(@NonNull final Bitmap sourceBitmap, final int color) {
		Bitmap ret = Bitmap.createBitmap(sourceBitmap.getWidth(), sourceBitmap.getHeight(), sourceBitmap.getConfig());

		Paint p = new Paint();
		ColorFilter filter = new LightingColorFilter(0, color);
		p.setAlpha(color >>> 24); // MAGIC_NUMBER
		p.setColorFilter(filter);
		Canvas canvas = new Canvas(ret);
		canvas.drawBitmap(sourceBitmap, 0, 0, p);
		return ret;
	}

	/**
	 * Deform the overlay bitmap according to a different pupil size.
	 *
	 * @param sourceBitmap  The original overlay bitmap.
	 * @param origPupilSize The pupil size (relative to iris) in the original overlay bitmap.
	 * @param destPupilSize The pupil size (relative to iris) in the target overlay bitmap.
	 * @param pupilOffsetX  The x offset of the pupil center, relative to the iris size
	 * @param pupilOffsetY  The y offset of the pupil center, relative to the iris size
	 * @return The deformed overlay bitmap.
	 */
	public static Bitmap deformOverlayByPupilSize(@NonNull final Bitmap sourceBitmap, final float origPupilSize, final float destPupilSize,
												  @Nullable final Float pupilOffsetX, @Nullable final Float pupilOffsetY) {
		if (origPupilSize == 0) {
			// non-deformable overlay, such as pupil overlay.
			return sourceBitmap;
		}

		int overlaySize = sourceBitmap.getWidth();
		int overlayHalfSize = overlaySize / 2;
		float irisRadius = overlayHalfSize * OverlayPinchImageView.OVERLAY_CIRCLE_RATIO;

		// the center of enlargement
		float targetCenterX = overlayHalfSize;
		float targetCenterY = overlayHalfSize;
		if (pupilOffsetX != null) {
			targetCenterX += 2 * irisRadius * pupilOffsetX / (1 - destPupilSize);
		}
		if (pupilOffsetY != null) {
			targetCenterY += 2 * irisRadius * pupilOffsetY / (1 - destPupilSize);
		}

		// Constants used for linear transformation of the iris part of the overlay.
		float linTransB = (destPupilSize - origPupilSize) / (1 - origPupilSize);
		float linTransM = (1 - destPupilSize) / (irisRadius * (1 - origPupilSize));
		float absOrigPupilSize = origPupilSize * irisRadius;

		Bitmap ret = Bitmap.createBitmap(overlaySize, overlaySize, sourceBitmap.getConfig());
		Canvas canvas = new Canvas(ret);

		float[] verts = new float[2 * (OVERLAY_MESH_SIZE + 1) * (OVERLAY_MESH_SIZE + 1)];
		int vertsIndex = 0;
		for (int y = 0; y <= OVERLAY_MESH_SIZE; y++) {
			for (int x = 0; x <= OVERLAY_MESH_SIZE; x++) {
				// The positions of the original mesh vertices in pixels relative to the center
				float xPos = (float) x * overlaySize / OVERLAY_MESH_SIZE - overlayHalfSize;
				float yPos = (float) y * overlaySize / OVERLAY_MESH_SIZE - overlayHalfSize;
				float centerDist = (float) Math.sqrt(xPos * xPos + yPos * yPos);

				if (centerDist >= irisRadius) {
					// outside the iris, take original position
					verts[vertsIndex++] = overlayHalfSize + xPos;
					verts[vertsIndex++] = overlayHalfSize + yPos;
				}
				else if (centerDist == 0) {
					verts[vertsIndex++] = targetCenterX;
					verts[vertsIndex++] = targetCenterY;
				}
				else {
					// original direction
					float xDirection = xPos / centerDist;
					float yDirection = yPos / centerDist;

					// corresponding iris boundary point
					float xBound = overlayHalfSize + xDirection * irisRadius;
					float yBound = overlayHalfSize + yDirection * irisRadius;

					float radialPosition = linTransM * centerDist + linTransB;
					if (centerDist < absOrigPupilSize) {
						radialPosition -= linTransB * Math.pow(1 - centerDist / absOrigPupilSize, 1.5f); // MAGIC_NUMBER
					}

					verts[vertsIndex++] = targetCenterX + (xBound - targetCenterX) * radialPosition;
					verts[vertsIndex++] = targetCenterY + (yBound - targetCenterY) * radialPosition;
				}
			}
		}

		Paint paint = new Paint();
		paint.setFilterBitmap(true);
		canvas.drawBitmapMesh(sourceBitmap, OVERLAY_MESH_SIZE, OVERLAY_MESH_SIZE, verts, 0, null, 0, paint);

		return ret;
	}

	/**
	 * Retrieves a dummy bitmap (for the case that an image file is not readable).
	 *
	 * @return the dummy bitmap.
	 */
	private static Bitmap getDummyBitmap() {
		return BitmapFactory.decodeResource(Application.getAppContext().getResources(), R.drawable.cannot_read_image);
	}

	/**
	 * Store a bitmap in a temporary file and return the URL.
	 *
	 * @param bitmap       the bitmap
	 * @param tempFileName The name of the temporary file.
	 * @return The URL.
	 */
	public static Uri getUriForFullResolutionBitmap(final Bitmap bitmap, final String tempFileName) {
		if (bitmap == null) {
			return null;
		}
		try {
			File cachePath = new File(Application.getAppContext().getCacheDir(), "images");
			cachePath.mkdirs();
			File imageFile = new File(cachePath, tempFileName + ".jpg");
			FileOutputStream stream = new FileOutputStream(imageFile);
			bitmap.compress(CompressFormat.JPEG, JPEG_PRECISION, stream);
			stream.close();

			return FileProvider.getUriForFile(Application.getAppContext(), Application.getAppContext().getPackageName() + ".fileprovider", imageFile);
		}
		catch (IOException e) {
			Log.e(Application.TAG, "Failed to store bitmap", e);
			return null;
		}
	}

	/**
	 * File filter class to identify image files.
	 */
	public static class ImageFileFilter implements FileFilter {
		@Override
		public final boolean accept(@NonNull final File file) {
			Uri uri = Uri.fromFile(file);
			return file.exists() && file.isFile() && ImageUtil.getMimeType(uri).startsWith("image/");
		}

	}

}
