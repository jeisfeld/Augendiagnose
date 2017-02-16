package de.jeisfeld.augendiagnoselib.util.imagefile;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.commons.imaging.ImageReadException;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.util.DateUtil;

/**
 * Utility class to handle an eye photo, in particular regarding personName policies.
 */
public class EyePhoto {
	/**
	 * The date format used for the file name.
	 */
	private static final String DATE_FORMAT = "yyyy-MM-dd";

	/**
	 * Indicator if the file has already a formatted name.
	 */
	private boolean mFormattedName = false;

	/**
	 * The path of the file.
	 */
	private String mPath;

	/**
	 * The filename.
	 */
	private String mFilename;

	/**
	 * The name of the person.
	 */
	@Nullable
	private String mPersonName;

	/**
	 * The date of the image.
	 */
	private Date mDate;

	/**
	 * The information of right/left eye.
	 */
	private RightLeft mRightLeft;

	/**
	 * The file suffix.
	 */
	private String mSuffix;

	/**
	 * A cache of the bitmap (to avoid too frequent generation).
	 */
	private Bitmap mCachedBitmap;

	/**
	 * The size of the cached bitmap (to avoid getting a badly sized bitmap from the cache).
	 */
	private int mCachedSize;

	/**
	 * Create the EyePhoto, giving a filename.
	 *
	 * @param filename the file name.
	 */
	public EyePhoto(@NonNull final String filename) {
		this(new File(filename));
	}

	/**
	 * Create the EyePhoto, giving a file resource.
	 *
	 * @param file the file.
	 */
	public EyePhoto(@NonNull final File file) {
		setPath(file.getParent());
		setFilename(file.getName());

		// Auto-correct file name if safely possible
		if (mFilename != null && !mFilename.equals(getFilename()) && !getFile().exists()) {
			boolean success = FileUtil.moveFile(file, getFile());
			if (!success) {
				Log.w(Application.TAG, "Failed to rename file" + file.getName() + " to " + getAbsolutePath());
			}
		}
	}

	/**
	 * Create the EyePhoto, giving details.
	 *
	 * @param path      The file path
	 * @param name      The person name
	 * @param date      The date
	 * @param rightLeft right or left eye?
	 * @param suffix    File suffix (".jpg")
	 */
	public EyePhoto(final String path, final String name, final Date date, final RightLeft rightLeft,
					@NonNull final String suffix) {
		setPath(path);
		setPersonName(name);
		setDate(date);
		setRightLeft(rightLeft);
		setSuffix(suffix);
		mFormattedName = true;
	}

	/**
	 * Retrieve the filename (excluding path).
	 *
	 * @return the filename.
	 */
	public final String getFilename() {
		if (mFormattedName) {
			return getPersonName() + " " + getDateString(DATE_FORMAT) + " " + getRightLeft().toShortString() + "."
					+ getSuffix();
		}
		else {
			return mFilename;
		}
	}

	/**
	 * Retrieve the file path.
	 *
	 * @return the file path.
	 */
	public final String getAbsolutePath() {
		return getFile().getAbsolutePath();
	}

	/**
	 * Set the filename (extracting from it the person personName, the date and the left/right property).
	 *
	 * @param filename the filename
	 */
	private void setFilename(@NonNull final String filename) {
		this.mFilename = filename;
		int suffixPosition = filename.lastIndexOf('.');
		int rightLeftPosition = filename.lastIndexOf(' ', suffixPosition);
		int datePosition = filename.lastIndexOf(' ', rightLeftPosition - 1);

		if (datePosition > 0) {
			setPersonName(filename.substring(0, datePosition));
			mFormattedName = setDateString(filename.substring(datePosition + 1, rightLeftPosition), DATE_FORMAT);
			setRightLeft(RightLeft.fromString(filename.substring(rightLeftPosition + 1, suffixPosition)));
			setSuffix(filename.substring(suffixPosition + 1));

		}
		else {
			if (suffixPosition > 0) {
				setSuffix(filename.substring(suffixPosition + 1));
			}
			setDate(ImageUtil.getExifDate(getAbsolutePath()));
		}

		if (!mFormattedName) {
			try {
				JpegMetadata metadata = JpegMetadataUtil.getMetadata(getAbsolutePath());
				setDate(metadata.getOrganizeDate());
				setRightLeft(metadata.getRightLeft());
			}
			catch (ImageReadException | IOException e) {
				// ignore
			}

			if (getDate() == null) {
				setDate(ImageUtil.getExifDate(getAbsolutePath()));
			}
		}
	}

	/**
	 * Retrieve the file path.
	 *
	 * @return the file path.
	 */
	private String getPath() {
		return mPath;
	}

	private void setPath(final String path) {
		this.mPath = path;
	}

	/**
	 * Retrieve the right/left information.
	 *
	 * @return the right/left information.
	 */
	public final RightLeft getRightLeft() {
		return mRightLeft;
	}

	public final void setRightLeft(final RightLeft rightLeft) {
		this.mRightLeft = rightLeft;
	}

	/**
	 * Retrieve the person name (use getFilename for the file name).
	 *
	 * @return the person name.
	 */
	@Nullable
	public final String getPersonName() {
		return mPersonName;
	}

	/**
	 * Set the person name (trimmed).
	 *
	 * @param name the person name
	 */
	private void setPersonName(@Nullable final String name) {
		if (name == null) {
			this.mPersonName = null;

		}
		else {
			this.mPersonName = name.trim();
		}
	}

	/**
	 * Retrieve the date as a string.
	 *
	 * @param format the date format.
	 * @return the date string.
	 */
	private String getDateString(final String format) {
		return DateUtil.format(getDate(), format);
	}

	/**
	 * Retrieve the date as a string with default date format.
	 *
	 * @return the date string.
	 */
	public final String getDateString() {
		return DateUtil.format(getDate());
	}

	/**
	 * Set the date from a String.
	 *
	 * @param dateString the date string
	 * @param format     the date format
	 * @return true if successful.
	 */
	private boolean setDateString(final String dateString, final String format) {
		try {
			setDate(DateUtil.parse(dateString, format));
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	/**
	 * Retrieve the date.
	 *
	 * @return the date.
	 */
	public final Date getDate() {
		return mDate;
	}

	private void setDate(final Date date) {
		this.mDate = date;
	}

	/**
	 * Retrieve the file suffix.
	 *
	 * @return the suffix.
	 */
	public final String getSuffix() {
		return mSuffix;
	}

	private void setSuffix(@NonNull final String suffix) {
		this.mSuffix = suffix.toLowerCase(Locale.getDefault());
	}

	/**
	 * Check if the file name is formatted as eye photo.
	 *
	 * @return true if the file name is formatted as eye photo.
	 */
	public final boolean isFormatted() {
		return mFormattedName;
	}

	/**
	 * Retrieve the photo as File.
	 *
	 * @return the file
	 */
	@NonNull
	private File getFile() {
		return new File(getPath(), getFilename());
	}

	/**
	 * Check if the file exists.
	 *
	 * @return true if the file exists.
	 */
	public final boolean exists() {
		return getFile().exists();
	}

	/**
	 * Delete the eye photo from the file system.
	 *
	 * @return true if the deletion was successful.
	 */
	public final boolean delete() {
		return FileUtil.deleteFile(getFile());
	}

	/**
	 * Move the eye photo to a target path and target personName (given via EyePhoto object).
	 *
	 * @param target         the file information of the target file.
	 * @param allowOverwrite if true, then an existing file is overwritten.
	 * @return true if the renaming was successful.
	 */
	public final boolean moveTo(final EyePhoto target, final boolean allowOverwrite) {
		if (target == null || (target.getFile().exists() && !allowOverwrite)) {
			return false;
		}

		return FileUtil.moveFile(getFile(), target.getFile());
	}

	/**
	 * Move the eye photo to a target folder.
	 *
	 * @param folderName   the target folder
	 * @param createUnique if true, then a unique target file name is created if a file with the same name exists in the target folder.
	 * @return true if the move was successful.
	 */
	public final boolean moveToFolder(@NonNull final String folderName, final boolean createUnique) {
		File folder = new File(folderName);
		if (!folder.exists() || !folder.isDirectory()) {
			// target folder does not exist
			return false;
		}
		EyePhoto newPhoto = new EyePhoto(new File(folder, getFilename()));

		if (newPhoto.exists() && !createUnique) {
			return false;
		}

		return FileUtil.moveFile(getFile(), newPhoto.getNonExistingEyePhoto().getFile());
	}

	/**
	 * Create a non-existing File object in the same folder.
	 *
	 * @return a non-existing File object in the same folder.
	 */
	private EyePhoto getNonExistingEyePhoto() {
		if (!exists()) {
			return this;
		}
		if (!new File(getPath()).isDirectory()) {
			return null;
		}

		if (mFormattedName) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(getDate());
			EyePhoto eyePhoto;

			do {
				calendar.add(Calendar.DATE, 1);
				eyePhoto = new EyePhoto(getPath(), getPersonName(), calendar.getTime(), getRightLeft(), getSuffix());
			}
			while (eyePhoto.exists());
			return eyePhoto;
		}
		else {
			String fileNameBase = mFilename;
			String fileNameSuffix = "";
			int suffixIndex = mFilename.lastIndexOf('.');
			if (suffixIndex >= 0) {
				fileNameBase = mFilename.substring(0, suffixIndex) + "-";
				fileNameSuffix = mFilename.substring(suffixIndex);
			}
			int i = 0;
			while (new File(getPath(), fileNameBase + i + fileNameSuffix).exists()) {
				i++;
			}
			return new EyePhoto(new File(getPath(), fileNameBase + i + fileNameSuffix).getAbsolutePath());
		}
	}


	/**
	 * Copy the eye photo to a target path and target personName (given via EyePhoto object).
	 *
	 * @param target the file information of the target file.
	 * @return true if the copying was successful.
	 */
	public final boolean copyTo(final EyePhoto target) {
		if (target == null || target.getFile().exists()) {
			// do not allow overwriting
			return false;
		}

		return FileUtil.copyFile(getFile(), target.getFile());
	}

	/**
	 * Change the personName renaming the file (keeping the path).
	 *
	 * @param targetName the target name
	 * @return true if the renaming was successful.
	 */
	public final boolean changePersonName(final String targetName) {
		EyePhoto target = cloneFromPath();
		target.setPersonName(targetName);
		boolean success = moveTo(target, false);

		if (success) {
			// update metadata
			JpegMetadata metadata = target.getImageMetadata();
			if (metadata == null) {
				metadata = new JpegMetadata();
				target.updateMetadataWithDefaults(metadata);
			}
			if (metadata.getPerson() == null || metadata.getPerson().length() == 0 || metadata.getPerson().equals(getPersonName())) {
				metadata.setPerson(targetName);
			}
			target.storeImageMetadata(metadata);
		}

		return success;
	}

	/**
	 * Change the date renaming the file (keeping the path).
	 *
	 * @param newDate the target date.
	 * @return true if the change was successful.
	 */
	public final boolean changeDate(final Date newDate) {
		EyePhoto target = cloneFromPath();
		target.setDate(newDate);
		boolean success = moveTo(target, false);

		if (success) {
			// update metadata
			JpegMetadata metadata = target.getImageMetadata();
			if (metadata == null) {
				metadata = new JpegMetadata();
				target.updateMetadataWithDefaults(metadata);
			}
			metadata.setOrganizeDate(newDate);
			target.storeImageMetadata(metadata);
		}

		return success;
	}

	/**
	 * Add the photo to the media store. Must be used carefully - may lead to failures if the photo is later moved away
	 * again.
	 */
	public final void addToMediaStore() {
		MediaStoreUtil.addPictureToMediaStore(getAbsolutePath());
	}

	/**
	 * Retrieve a clone of this object from the absolute path.
	 *
	 * @return a clone (recreation) of this object having the same absolute path.
	 */
	@NonNull
	private EyePhoto cloneFromPath() {
		return new EyePhoto(getAbsolutePath());
	}

	/**
	 * Calculate a bitmap of this photo and store it for later retrieval.
	 *
	 * @param maxSize the target size of the bitmap
	 */
	public final synchronized void precalculateImageBitmap(final int maxSize) {
		if (maxSize != mCachedSize || mCachedBitmap == null) {
			mCachedBitmap = ImageUtil.getImageBitmap(getAbsolutePath(), maxSize);
			mCachedSize = maxSize;
		}
	}

	/**
	 * Return a bitmap of this photo.
	 *
	 * @param maxSize The maximum size of this bitmap. If bigger, it will be resized
	 * @return the bitmap
	 */
	public final Bitmap getImageBitmap(final int maxSize) {
		precalculateImageBitmap(maxSize);
		return mCachedBitmap;
	}

	/**
	 * Retrieve a bitmap of this photo in full resolution.
	 *
	 * @return The bitmap.
	 */
	public final Bitmap getFullBitmap() {
		return ImageUtil.getImageBitmap(getAbsolutePath(), 0);
	}

	/**
	 * Get the metadata stored in the file.
	 *
	 * @return the metadata.
	 */
	@Nullable
	public final JpegMetadata getImageMetadata() {
		return JpegSynchronizationUtil.getJpegMetadata(getAbsolutePath());
	}

	/**
	 * Store the metadata in the file.
	 *
	 * @param metadata the metadata to be stored.
	 */
	public final void storeImageMetadata(final JpegMetadata metadata) {
		JpegSynchronizationUtil.storeJpegMetadata(getAbsolutePath(), metadata);
	}

	/**
	 * Update metadata object with default metadata, based on the file name.
	 *
	 * @param metadata the metadata object to be enhanced by the default information.
	 */
	public final void updateMetadataWithDefaults(@NonNull final JpegMetadata metadata) {
		metadata.setPerson(getPersonName());
		metadata.setOrganizeDate(getDate());
		metadata.setRightLeft(getRightLeft());
		metadata.setTitle(getPersonName() + " - " + getRightLeft().getTitleSuffix());
	}

	/**
	 * Store person, date and rightLeft in the metadata.
	 */
	public final void storeDefaultMetadata() {
		JpegMetadata metadata = getImageMetadata();
		if (metadata == null) {
			metadata = new JpegMetadata();
		}
		updateMetadataWithDefaults(metadata);
		storeImageMetadata(metadata);
	}

	/**
	 * Compare two images for equality (by path).
	 *
	 * @param other the other image to be compared to
	 * @return true if the bitmaps have the same path.
	 */
	@Override
	public final boolean equals(final Object other) {
		if (!(other instanceof EyePhoto)) {
			return false;
		}
		EyePhoto otherPhoto = (EyePhoto) other;
		return otherPhoto.getAbsolutePath().equals(getAbsolutePath());
	}

	/**
	 * Ensure that hashCode() matches equals().
	 *
	 * @return the hashCode.
	 */
	@Override
	public final int hashCode() {
		return getAbsolutePath().hashCode();
	}

	/**
	 * Enumeration for left eye vs. right eye.
	 */
	public enum RightLeft {
		/**
		 * Enumeration values for right eye and left eye.
		 */
		RIGHT, LEFT;

		/**
		 * Convert into a short string, to be used for filenames.
		 *
		 * @return the short string (dependent on language!)
		 */
		public final String toShortString() {
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

		/**
		 * The suffix to be used for the title of the image.
		 *
		 * @return the title suffix.
		 */
		public final String getTitleSuffix() {
			switch (this) {
			case LEFT:
				return Application.getResourceString(R.string.suffix_title_left);
			case RIGHT:
				return Application.getResourceString(R.string.suffix_title_right);
			default:
				return null;
			}
		}

		/**
		 * Convert a String into a RightLeft enum (by first letter).
		 *
		 * @param rightLeftString The String to be converted.
		 * @return the converted RightString.
		 */
		@NonNull
		public static RightLeft fromString(@Nullable final String rightLeftString) {
			if (rightLeftString != null && rightLeftString.matches("[rRdD].*")) {
				return RIGHT;
			}
			else {
				return LEFT;
			}
		}
	}
}
