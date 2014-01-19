package de.eisfeldj.augendiagnose.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.provider.MediaStore;
import android.util.Log;
import de.eisfeldj.augendiagnose.Application;

/**
 * Utility class to handle an eye photo, in particular regarding name policies.
 */
public class EyePhoto {
	public static final int MINI_THUMB_SIZE = 512;
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private boolean formattedName = false;
	private String path;
	private String filename;
	private String name;
	private Date date;
	private RightLeft rightLeft;
	private String suffix;

	/**
	 * Create the EyePhoto, giving a filename
	 * 
	 * @param filename
	 */
	public EyePhoto(String filename) {
		this(new File(filename));
	}

	/**
	 * Create the EyePhoto, giving a file resource
	 * 
	 * @param file
	 */
	public EyePhoto(File file) {
		setPath(file.getParent());
		setFilename(file.getName());
	}

	/**
	 * Create the EyePhoto, giving details
	 * 
	 * @param path
	 *            The file path
	 * @param name
	 *            The person name
	 * @param date
	 *            The date
	 * @param rightLeft
	 *            right or left eye?
	 * @param suffix
	 *            File suffix (".jpg")
	 */
	public EyePhoto(String path, String name, Date date, RightLeft rightLeft, String suffix) {
		setPath(path);
		setName(name);
		setDate(date);
		setRightLeft(rightLeft);
		setSuffix(suffix);
		formattedName = true;
	}

	/**
	 * Retrieve the filename (excluding path)
	 * 
	 * @return
	 */
	public String getFilename() {
		if (formattedName) {
			return getName() + " " + getDateString(DATE_FORMAT) + " " + getRightLeft().toShortString() + "."
					+ getSuffix();
		}
		else {
			return filename;
		}
	}

	/**
	 * Retrieve the file path
	 * 
	 * @return
	 */
	public String getAbsolutePath() {
		return getFile().getAbsolutePath();
	}

	/**
	 * Set the filename (extracting from it the person name, the date and the left/right property)
	 * 
	 * @param filename
	 */
	private void setFilename(String filename) {
		this.filename = filename;
		int suffixPosition = filename.lastIndexOf('.');
		int rightLeftPosition = filename.lastIndexOf(' ', suffixPosition);
		int datePosition = filename.lastIndexOf(' ', rightLeftPosition - 1);

		if (datePosition > 0) {
			setName(filename.substring(0, datePosition));
			formattedName = setDateString(filename.substring(datePosition + 1, rightLeftPosition), DATE_FORMAT);
			setRightLeft(RightLeft.fromShortString(filename.substring(rightLeftPosition + 1, suffixPosition)));
			setSuffix(filename.substring(suffixPosition + 1));

			if (!formattedName) {
				getExifDate();
			}
		}
		else {
			if (suffixPosition > 0) {
				setSuffix(filename.substring(suffixPosition + 1));
			}
			getExifDate();
			formattedName = false;
		}
	}

	/**
	 * Retrieve the file path
	 * 
	 * @return
	 */
	public String getPath() {
		return path;
	}

	private void setPath(String path) {
		this.path = path;
	}

	/**
	 * Retrieve the right/left information
	 * 
	 * @return
	 */
	public RightLeft getRightLeft() {
		return rightLeft;
	}

	private void setRightLeft(RightLeft rightLeft) {
		this.rightLeft = rightLeft;
	}

	/**
	 * Retrieve the person name (use getFilename for the file name)
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	private void setName(String name) {
		if (name == null) {
			this.name = null;

		}
		else {
			this.name = name.trim();
		}
	}

	/**
	 * Retrieve the date as a string
	 * 
	 * @return
	 */
	public String getDateString(String format) {
		String dateString = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
		dateString = dateFormat.format(getDate());
		return dateString;
	}

	/**
	 * Set the date from a String
	 * 
	 * @param dateString
	 * @return
	 */
	private boolean setDateString(String dateString, String format) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
			setDate(dateFormat.parse(dateString));
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	/**
	 * Retrieve the date
	 * @return
	 */
	public Date getDate() {
		return date;
	}

	private void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Retrieve the date for display
	 * @param calendar
	 * @return
	 */
	public static String getDisplayDate(Calendar calendar) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("d. MMMM yyyy", Locale.getDefault());
		return dateFormat.format(calendar.getTime());
	}
	
	/**
	 * Retrieve the file suffix
	 * @return
	 */
	public String getSuffix() {
		return suffix;
	}

	private void setSuffix(String suffix) {
		this.suffix = suffix.toLowerCase(Locale.getDefault());
	}

	/**
	 * Check if the file name is formatted as eye photo
	 * @return
	 */
	public boolean isFormatted() {
		return formattedName;
	}

	/**
	 * Retrieve the phoso as File
	 * @return
	 */
	public File getFile() {
		return new File(getPath(), getFilename());
	}

	/**
	 * Check if the file exists
	 * @return
	 */
	public boolean exists() {
		return getFile().exists();
	}

	/**
	 * Move the eye photo to a target path and target name (given via EyePhoto object)
	 * @param target
	 * @return
	 */
	public boolean moveTo(EyePhoto target) {
		return getFile().renameTo(target.getFile());
	}

	/**
	 * Change the name (keeping the path)
	 * @param targetName
	 * @return
	 */
	public boolean changeName(String targetName) {
		EyePhoto target = cloneFromPath();
		target.setName(targetName);
		return moveTo(target);
	}

	/**
	 * Set the date field with the EXIF date from the file
	 * If not existing, use the last modified date.
	 */
	private void getExifDate() {
		Date retrievedDate = null;
		try {
			ExifInterface exif = new ExifInterface(getAbsolutePath());
			String dateString = exif.getAttribute(ExifInterface.TAG_DATETIME);

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
			retrievedDate = dateFormat.parse(dateString);
		}
		catch (Exception e) {
			Log.w("JE", e.toString() + "Cannot retrieve EXIF date for " + getAbsolutePath());
		}
		if (retrievedDate == null) {
			File f = new File(getAbsolutePath());
			retrievedDate = new Date(f.lastModified());
		}
		setDate(retrievedDate);
	}

	/**
	 * Retrieve a clone of this object from the absolute path
	 * @return
	 */
	public EyePhoto cloneFromPath() {
		return new EyePhoto(getAbsolutePath());
	}

	/**
	 * Return a bitmap of this photo
	 * @param maxSize The maximum size of this bitmap. If bigger, it will be resized
	 * @return
	 */
	public Bitmap getImageBitmap(int maxSize) {
		Bitmap bitmap = null;

		if (maxSize <= MINI_THUMB_SIZE) {
			bitmap = getThumbnail(maxSize);
		}

		if (bitmap == null) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = getBitmapFactor(getAbsolutePath(), maxSize);
			bitmap = BitmapFactory.decodeFile(getAbsolutePath(), options);
			if (bitmap.getWidth() > maxSize) {
				int targetHeight = bitmap.getHeight() * maxSize / bitmap.getWidth();
				bitmap = Bitmap.createScaledBitmap(bitmap, maxSize, targetHeight, false);
			}
			if (bitmap.getHeight() > maxSize) {
				int targetWidth = bitmap.getWidth() * maxSize / bitmap.getHeight();
				bitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, maxSize, false);
			}
		}
		return bitmap;
	}

	/**
	 * Utility to retrieve the sample size for BitmapFactory.decodeFile
	 * @param filepath
	 * @param targetSize
	 * @return
	 */
	private int getBitmapFactor(String filepath, int targetSize) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filepath, options);
		int size = Math.max(options.outWidth, options.outWidth);
		return size / targetSize;
	}

	/**
	 * Utility to retrieve the bitmap as thumbnail via the MediaStore
	 * @param maxSize The maximum size of this bitmap (used for selecting the sample size)
	 * @return
	 */
	private Bitmap getThumbnail(int maxSize) {
		ContentResolver resolver = Application.getAppContext().getContentResolver();

		Cursor imagecursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Images.Media._ID }, MediaStore.Images.Media.DATA + " = ?",
				new String[] { getAbsolutePath() }, MediaStore.Images.Media.DATE_ADDED + " desc");
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

	/**
	 * Compare two bitmaps for equality (by path)
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof EyePhoto)) {
			return false;
		}
		EyePhoto otherPhoto = (EyePhoto) other;
		return otherPhoto.getAbsolutePath().equals(getAbsolutePath());
	}

	/**
	 * Enumeration for left eye vs. right eye
	 */
	public enum RightLeft {
		RIGHT, LEFT;

		public String toShortString() {
			switch (this) {
			case LEFT:
				return "li";
			case RIGHT:
				return "re";
			default:
				return null;
			}
		}

		public static RightLeft fromShortString(String shortString) {
			if (shortString != null && shortString.startsWith("r")) {
				return RIGHT;
			}
			else {
				return LEFT;
			}
		}
	}

}
