package de.eisfeldj.augendiagnose.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Date;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;

/**
 * Utility class for operations with images
 */
public abstract class ImageUtil {

	/**
	 * Get the date field with the EXIF date from the file If not existing, use the last modified date.
	 * 
	 * @param path
	 *            The file path of the image
	 */
	public static Date getExifDate(String path) {
		Date retrievedDate = null;
		try {
			ExifInterface exif = new ExifInterface(path);
			String dateString = exif.getAttribute(ExifInterface.TAG_DATETIME);

			retrievedDate = DateUtil.parse(dateString, "yyyy:MM:dd HH:mm:ss");
		}
		catch (Exception e) {
			Log.w(Application.TAG, e.toString() + " - Cannot retrieve EXIF date for " + path);
		}
		if (retrievedDate == null) {
			File f = new File(path);
			retrievedDate = new Date(f.lastModified());
		}
		return retrievedDate;
	}

	/**
	 * Retrieve the rotation angle from the Exif data of an image
	 * 
	 * @param path
	 *            The file path of the image
	 * @return
	 */
	public static int getExifRotation(String path) {
		int rotation = 0;
		try {
			ExifInterface exif = new ExifInterface(path);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotation = 270;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotation = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotation = 90;
				break;
			}
		}
		catch (Exception e) {
		}
		return rotation;
	}

	/**
	 * Return a bitmap of this photo
	 * 
	 * @param path
	 *            The file path of the image
	 * @param maxSize
	 *            The maximum size of this bitmap. If bigger, it will be resized
	 * @return
	 */
	public static Bitmap getImageBitmap(String path, int maxSize) {
		Bitmap bitmap = null;

		if (maxSize <= MediaStoreUtil.MINI_THUMB_SIZE) {
			bitmap = MediaStoreUtil.getThumbnailFromPath(path, maxSize);
		}

		if (bitmap == null) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = getBitmapFactor(path, maxSize);
			// options.inPurgeable = true;
			bitmap = BitmapFactory.decodeFile(path, options);
			if (bitmap == null) {
				// cannot create bitmap - return dummy
				Log.w(Application.TAG, "Cannot create bitmap from path " + path + " - return dummy bitmap");
				return getDummyBitmap();
			}
			if (bitmap.getWidth() > maxSize) {
				int targetHeight = bitmap.getHeight() * maxSize / bitmap.getWidth();
				bitmap = Bitmap.createScaledBitmap(bitmap, maxSize, targetHeight, false);
			}
			if (bitmap.getHeight() > maxSize) {
				int targetWidth = bitmap.getWidth() * maxSize / bitmap.getHeight();
				bitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, maxSize, false);
			}
		}

		int rotation = getExifRotation(path);
		if (rotation != 0) {
			bitmap = rotateBitmap(bitmap, rotation);
		}

		return bitmap;
	}

	/**
	 * Utility to retrieve the sample size for BitmapFactory.decodeFile
	 * 
	 * @param filepath
	 * @param targetSize
	 * @return
	 */
	private static int getBitmapFactor(String filepath, int targetSize) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filepath, options);
		int size = Math.max(options.outWidth, options.outWidth);
		return size / targetSize;
	}

	/**
	 * Copy a file
	 * 
	 * @param source
	 *            The source file
	 * @param target
	 *            The target file
	 * @return
	 */
	public static boolean copyFile(File source, File target) {
		FileInputStream inStream = null;
		FileOutputStream outStream = null;
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		try {
			inStream = new FileInputStream(source);
			outStream = new FileOutputStream(target);
			inChannel = inStream.getChannel();
			outChannel = outStream.getChannel();
			inChannel.transferTo(0, inChannel.size(), outChannel);
		}
		catch (Exception e) {
			Log.e(Application.TAG,
					"Error when copying file from " + source.getAbsolutePath() + " to " + target.getAbsolutePath(), e);
			return false;
		}
		finally {
			try {
				inStream.close();
				outStream.close();
				inChannel.close();
				outChannel.close();
			}
			catch (Exception e) {

			}
		}
		return true;
	}

	/**
	 * Rotate a bitmap
	 * 
	 * @param source
	 *            The original bitmap
	 * @param angle
	 *            The rotation angle
	 * @return
	 */
	public static Bitmap rotateBitmap(Bitmap source, float angle) {
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}

	/**
	 * Get Mime type from URI
	 * 
	 * @param uri
	 *            The URI
	 * @return
	 */
	public static String getMimeType(Uri uri) {
		ContentResolver contentResolver = Application.getAppContext().getContentResolver();
		String mimeType = contentResolver.getType(uri);
		if (mimeType == null) {
			String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
			mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
			if (mimeType == null) {
				mimeType = "unknown";
			}
		}
		return mimeType;
	}

	/**
	 * Retrieves a dummy bitmap (for the case that an image file is not readable)
	 * 
	 * @return
	 */
	public static Bitmap getDummyBitmap() {
		return BitmapFactory.decodeResource(Application.getAppContext().getResources(), R.drawable.bad_file_format);
	}

	/**
	 * File filter class to identify image files
	 */
	public static class ImageFileFilter implements FileFilter {
		@Override
		public boolean accept(File file) {
			Uri uri = Uri.fromFile(file);
			return file.exists() && file.isFile() && ImageUtil.getMimeType(uri).startsWith("image/");
		}

	}

}
