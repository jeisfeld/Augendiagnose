package de.eisfeldj.augendiagnose.util;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import de.eisfeldj.augendiagnose.Application;

/**
 * Utility class for handling the media store
 */
public abstract class MediaStoreUtil {
	public static final int MINI_THUMB_SIZE = 512;

	/**
	 * Get a real file path from the URI of the media store
	 * 
	 * @param contentUri
	 *            Thr URI of the media store
	 * @return
	 */
	public static String getRealPathFromURI(Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = Application.getAppContext().getContentResolver().query(contentUri, proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		catch(Exception e) {
			return null;
		}
		finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	/**
	 * Retrieve a thumbnail of a bitmap from the mediastore
	 * 
	 * @param path
	 *            The path of the image
	 * @param maxSize
	 *            The maximum size of this bitmap (used for selecting the sample size)
	 * @return
	 */
	public static Bitmap getThumbnailFromPath(String path, int maxSize) {
		ContentResolver resolver = Application.getAppContext().getContentResolver();

		Cursor imagecursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Images.Media._ID }, MediaStore.Images.Media.DATA + " = ?",
				new String[] { path }, MediaStore.Images.Media.DATE_ADDED + " desc");
		imagecursor.moveToFirst();

		if (!imagecursor.isAfterLast()) {
			int imageId = imagecursor.getInt(imagecursor.getColumnIndex(MediaStore.Images.Media._ID));
			imagecursor.close();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = MINI_THUMB_SIZE / maxSize;
			options.inDither = true;
			return MediaStore.Images.Thumbnails.getThumbnail(resolver, imageId, MediaStore.Images.Thumbnails.MINI_KIND,
					options);
		}
		else {
			imagecursor.close();
			return null;
		}
	}

}
