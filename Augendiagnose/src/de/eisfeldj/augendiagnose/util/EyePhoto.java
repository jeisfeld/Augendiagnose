package de.eisfeldj.augendiagnose.util;

import java.io.File;
import java.util.Date;
import java.util.Locale;

import android.graphics.Bitmap;
import android.util.Log;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;

/**
 * Utility class to handle an eye photo, in particular regarding personName policies.
 */
public class EyePhoto {
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private boolean formattedName = false;
	private String path;
	private String filename;
	private String personName;
	private Date date;
	private RightLeft rightLeft;
	private String suffix;
	private Bitmap cachedBitmap;
	private int cachedSize;

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

		if (filename != null && !filename.equals(getFilename())) {
			boolean success = new File(getPath(), filename).renameTo(new File(getPath(), getFilename()));
			if (!success) {
				Log.w(Application.TAG, "Failed to rename file" + filename + " to " + getFilename());
			}
		}
	}

	/**
	 * Create the EyePhoto, giving details
	 *
	 * @param path
	 *            The file path
	 * @param personName
	 *            The person personName
	 * @param date
	 *            The date
	 * @param rightLeft
	 *            right or left eye?
	 * @param suffix
	 *            File suffix (".jpg")
	 */
	public EyePhoto(String path, String name, Date date, RightLeft rightLeft, String suffix) {
		setPath(path);
		setPersonName(name);
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
			return getPersonName() + " " + getDateString(DATE_FORMAT) + " " + getRightLeft().toShortString() + "."
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
	 * Set the filename (extracting from it the person personName, the date and the left/right property)
	 *
	 * @param filename
	 */
	private void setFilename(String filename) {
		this.filename = filename;
		int suffixPosition = filename.lastIndexOf('.');
		int rightLeftPosition = filename.lastIndexOf(' ', suffixPosition);
		int datePosition = filename.lastIndexOf(' ', rightLeftPosition - 1);

		if (datePosition > 0) {
			setPersonName(filename.substring(0, datePosition));
			formattedName = setDateString(filename.substring(datePosition + 1, rightLeftPosition), DATE_FORMAT);
			setRightLeft(RightLeft.fromString(filename.substring(rightLeftPosition + 1, suffixPosition)));
			setSuffix(filename.substring(suffixPosition + 1));

			if (!formattedName) {
				setDate(ImageUtil.getExifDate(getAbsolutePath()));
			}
		}
		else {
			if (suffixPosition > 0) {
				setSuffix(filename.substring(suffixPosition + 1));
			}
			setDate(ImageUtil.getExifDate(getAbsolutePath()));
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
	 * Retrieve the person personName (use getFilename for the file personName)
	 *
	 * @return
	 */
	public String getPersonName() {
		return personName;
	}

	private void setPersonName(String name) {
		if (name == null) {
			this.personName = null;

		}
		else {
			this.personName = name.trim();
		}
	}

	/**
	 * Retrieve the date as a string
	 *
	 * @return
	 */
	public String getDateString(String format) {
		return DateUtil.format(getDate(), format);
	}

	/**
	 * Set the date from a String
	 *
	 * @param dateString
	 * @return
	 */
	private boolean setDateString(String dateString, String format) {
		try {
			setDate(DateUtil.parse(dateString, format));
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	/**
	 * Retrieve the date
	 *
	 * @return
	 */
	public Date getDate() {
		return date;
	}

	private void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Retrieve the file suffix
	 *
	 * @return
	 */
	public String getSuffix() {
		return suffix;
	}

	private void setSuffix(String suffix) {
		this.suffix = suffix.toLowerCase(Locale.getDefault());
	}

	/**
	 * Check if the file personName is formatted as eye photo
	 *
	 * @return
	 */
	public boolean isFormatted() {
		return formattedName;
	}

	/**
	 * Retrieve the phoso as File
	 *
	 * @return
	 */
	public File getFile() {
		return new File(getPath(), getFilename());
	}

	/**
	 * Check if the file exists
	 *
	 * @return
	 */
	public boolean exists() {
		return getFile().exists();
	}

	/**
	 * Delete the eye photo from the file system
	 *
	 * @return
	 */
	public boolean delete() {
		return getFile().delete();
	}

	/**
	 * Move the eye photo to a target path and target personName (given via EyePhoto object)
	 *
	 * @param target
	 * @return
	 */
	public boolean moveTo(EyePhoto target) {
		if (target.getFile().exists()) {
			// do not allow overwriting
			return false;
		}

		return getFile().renameTo(target.getFile());
	}

	/**
	 * Copy the eye photo to a target path and target personName (given via EyePhoto object)
	 *
	 * @param target
	 * @return
	 */
	public boolean copyTo(EyePhoto target) {
		if (target.getFile().exists()) {
			// do not allow overwriting
			return false;
		}

		return FileUtil.copyFile(getFile(), target.getFile());
	}

	/**
	 * Change the personName renaming the file (keeping the path)
	 *
	 * @param targetName
	 * @return
	 */
	public boolean changePersonName(String targetName) {
		EyePhoto target = cloneFromPath();
		target.setPersonName(targetName);
		boolean success = moveTo(target);

		if (success) {
			// update metadata
			JpegMetadata metadata = target.getImageMetadata();
			if (metadata == null) {
				metadata = new JpegMetadata();
				target.updateMetadataWithDefaults(metadata);
			}
			if (metadata.person == null || metadata.person.length() == 0 || metadata.person.equals(getPersonName())) {
				metadata.person = targetName;
			}
			target.storeImageMetadata(metadata);
		}

		return success;
	}

	/**
	 * Change the date renaming the file (keeping the path)
	 *
	 * @param newDate
	 * @return
	 */
	public boolean changeDate(Date newDate) {
		EyePhoto target = cloneFromPath();
		target.setDate(newDate);
		boolean success = moveTo(target);

		if (success) {
			// update metadata
			JpegMetadata metadata = target.getImageMetadata();
			if (metadata == null) {
				metadata = new JpegMetadata();
				target.updateMetadataWithDefaults(metadata);
			}
			metadata.organizeDate = newDate;
			target.storeImageMetadata(metadata);
		}

		return success;
	}

	/**
	 * Add the photo to the media store. Must be used carefully - may lead to failures if the photo is later moved away
	 * again.
	 */
	public void addToMediaStore() {
		MediaStoreUtil.addPictureToMediaStore(getAbsolutePath());
	}

	/**
	 * Retrieve a clone of this object from the absolute path
	 *
	 * @return
	 */
	public EyePhoto cloneFromPath() {
		return new EyePhoto(getAbsolutePath());
	}

	/**
	 * Calculate a bitmap of this photo and store it for later retrieval.
	 *
	 * @param maxSize
	 */
	public synchronized void precalculateImageBitmap(int maxSize) {
		if (maxSize != cachedSize || cachedBitmap == null) {
			cachedBitmap = ImageUtil.getImageBitmap(getAbsolutePath(), maxSize);
			cachedSize = maxSize;
		}
	}

	/**
	 * Return a bitmap of this photo
	 *
	 * @param maxSize
	 *            The maximum size of this bitmap. If bigger, it will be resized
	 * @return
	 */
	public Bitmap getImageBitmap(int maxSize) {
		precalculateImageBitmap(maxSize);
		return cachedBitmap;
	}

	/**
	 * Get the metadata stored in the file
	 *
	 * @return
	 */
	public JpegMetadata getImageMetadata() {
		return JpegSynchronizationUtil.getJpegMetadata(getAbsolutePath());
	}

	/**
	 * Store the metadata in the file
	 *
	 * @param metadata
	 * @return true if successful
	 */
	public void storeImageMetadata(JpegMetadata metadata) {
		JpegSynchronizationUtil.storeJpegMetadata(getAbsolutePath(), metadata);
	}

	/**
	 * Update metadata object with default metadata, based on the file name.
	 *
	 * @return
	 */
	public void updateMetadataWithDefaults(JpegMetadata metadata) {
		metadata.person = getPersonName();
		metadata.organizeDate = getDate();
		metadata.rightLeft = getRightLeft();
		metadata.title = getPersonName() + " - " + getRightLeft().getTitleSuffix();
	}

	/**
	 * Store person, date and rightLeft in the metadata
	 *
	 * @return
	 */
	public void storeDefaultMetadata() {
		JpegMetadata metadata = getImageMetadata();
		if (metadata == null) {
			metadata = new JpegMetadata();
		}
		updateMetadataWithDefaults(metadata);
		storeImageMetadata(metadata);
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
	 * Ensure that hashCode() matches equals()
	 */
	@Override
	public int hashCode() {
		return getAbsolutePath().hashCode();
	}

	/**
	 * Enumeration for left eye vs. right eye
	 */
	public enum RightLeft {
		RIGHT, LEFT;

		public String toShortString() {
			switch (this) {
			case LEFT:
				return Application.getResourceString(R.string.file_infix_left);
			case RIGHT:
				return Application.getResourceString(R.string.file_infix_right);
			default:
				return "";
			}
		}

		@Override
		public String toString() {
			switch (this) {
			case LEFT:
				return "LEFT";
			case RIGHT:
				return "RIGHT";
			default:
				return "";
			}
		}

		public String getTitleSuffix() {
			switch (this) {
			case LEFT:
				return Application.getResourceString(R.string.suffix_title_left);
			case RIGHT:
				return Application.getResourceString(R.string.suffix_title_right);
			default:
				return null;
			}
		}

		public static RightLeft fromString(String rightLeftString) {
			if (rightLeftString != null && (rightLeftString.startsWith("r") || rightLeftString.startsWith("R"))) {
				return RIGHT;
			}
			else {
				return LEFT;
			}
		}
	}

}
