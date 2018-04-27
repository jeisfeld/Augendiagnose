package de.eisfeldj.augendiagnosefx.util.imagefile;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.eisfeldj.augendiagnosefx.util.DateUtil;
import de.eisfeldj.augendiagnosefx.util.Logger;
import de.eisfeldj.augendiagnosefx.util.ResourceConstants;
import de.eisfeldj.augendiagnosefx.util.ResourceUtil;
import de.eisfeldj.augendiagnosefx.util.imagefile.ImageUtil.Resolution;

import javafx.scene.image.Image;

/**
 * Utility class to handle an eye photo, in particular regarding personName policies.
 */
public class EyePhoto {
	/**
	 * The date format used for the file name.
	 */
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	/**
	 * The maximum size of the image cache.
	 */
	private static final int MAX_IMAGE_CACHE = 4;

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
	private Image mCachedImage;

	/**
	 * A cache of the thumbnail.
	 */
	private Image mCachedThumbnail;

	/**
	 * The list of eye photos having a cached image.
	 */
	private static final List<WeakReference<EyePhoto>> CACHED_EYE_PHOTOS = new ArrayList<>();

	/**
	 * A map from path to EyePhoto objects - for reuse.
	 *
	 * <p>Note: WeakHashMap cannot be used, as the garbage collection should be dependent on values, not on keys.
	 */
	private static final HashMap<String, WeakReference<EyePhoto>> EYE_PHOTO_MAP = new HashMap<>();

	/**
	 * Create the EyePhoto, giving a file resource.
	 *
	 * @param file
	 *            the file.
	 */
	public EyePhoto(final File file) {
		setPath(file.getParent());
		setFilename(file.getName());

		// Auto-correct file name if safely possible
		if (mFilename != null && !mFilename.equals(getFilename()) && !getFile().exists()) {
			boolean success = new File(getPath(), mFilename).renameTo(new File(getPath(), getFilename()));
			if (!success) {
				Logger.warning("Failed to rename file" + mFilename + " to " + getFilename());
			}
		}
	}

	/**
	 * Get an EyePhoto, giving a file resource (returning an existing instance if available).
	 *
	 * @param file The file.
	 * @return The EyePhoto.
	 */
	public static EyePhoto fromFile(final File file) {
		synchronized (EYE_PHOTO_MAP) {
			EyePhoto eyePhoto = null;
			WeakReference<EyePhoto> eyePhotoReference = EYE_PHOTO_MAP.get(file.getAbsolutePath());
			if (eyePhotoReference != null) {
				eyePhoto = eyePhotoReference.get();
			}
			if (eyePhoto == null) {
				eyePhoto = new EyePhoto(file);
				EYE_PHOTO_MAP.put(file.getAbsolutePath(), new WeakReference<>(eyePhoto));
			}
			return eyePhoto;
		}
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
	 * @param filename
	 *            the filename
	 */
	private void setFilename(final String filename) {
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
			mFormattedName = false;
		}
	}

	/**
	 * Retrieve the file path.
	 *
	 * @return the file path.
	 */
	public final String getPath() {
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

	private void setRightLeft(final RightLeft rightLeft) {
		this.mRightLeft = rightLeft;
	}

	/**
	 * Retrieve the person name (use getFilename for the file name).
	 *
	 * @return the person name.
	 */
	public final String getPersonName() {
		return mPersonName;
	}

	/**
	 * Set the person name (trimmed).
	 *
	 * @param name
	 *            the person name
	 */
	private void setPersonName(final String name) {
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
	 * @param format
	 *            the date format.
	 * @return the date string.
	 */
	public final String getDateString(final String format) {
		return DateUtil.format(getDate(), format);
	}

	/**
	 * Set the date from a String.
	 *
	 * @param dateString
	 *            the date string
	 * @param format
	 *            the date format
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

	private void setSuffix(final String suffix) {
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
	public final File getFile() {
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
		return getFile().delete();
	}

	/**
	 * Move the eye photo to a target path and target personName (given via EyePhoto object).
	 *
	 * @param target
	 *            the file information of the target file.
	 * @return true if the renaming was successful.
	 */
	public final boolean moveTo(final EyePhoto target) {
		if (target.getFile().exists()) {
			// do not allow overwriting
			return false;
		}

		return getFile().renameTo(target.getFile());
	}

	/**
	 * Move the eye photo to a target folder.
	 *
	 * @param folderName
	 *            the target folder
	 * @return true if the move was successful.
	 */
	public final boolean moveToFolder(final String folderName) {
		File folder = new File(folderName);
		if (!folder.exists() || !folder.isDirectory()) {
			// target folder does not exist
			return false;
		}

		File targetFile = new File(folder, getFilename());
		if (targetFile.exists()) {
			// do not overwrite
			return false;
		}

		return getFile().renameTo(targetFile);
	}

	/**
	 * Copy the eye photo to a target path and target personName (given via EyePhoto object).
	 *
	 * @param target
	 *            the file information of the target file.
	 * @return true if the copying was successful.
	 */
	public final boolean copyTo(final EyePhoto target) {
		if (target.getFile().exists()) {
			// do not allow overwriting
			return false;
		}

		return FileUtil.copyFile(getFile(), target.getFile());
	}

	/**
	 * Return an Image of this photo.
	 *
	 * @param resolution
	 *            Indicator of the resolution in which the image should be returned.
	 * @return the Image
	 */
	public final Image getImage(final Resolution resolution) {
		switch (resolution) {
		case THUMB:
			if (mCachedThumbnail == null) {
				mCachedThumbnail = ImageUtil.getImage(getFile(), Resolution.THUMB);
			}
			return mCachedThumbnail;
		case NORMAL:
			Image result = mCachedImage;
			if (result == null) {
				result = ImageUtil.getImage(getFile(), Resolution.NORMAL);
				synchronized (CACHED_EYE_PHOTOS) {
					mCachedImage = result;
					CACHED_EYE_PHOTOS.add(new WeakReference<>(this));
					// Ensure that not too many images are cached
					if (CACHED_EYE_PHOTOS.size() > MAX_IMAGE_CACHE) {
						EyePhoto firstInList = CACHED_EYE_PHOTOS.get(0).get();
						if (firstInList != null) {
							firstInList.mCachedImage = null;
						}
						CACHED_EYE_PHOTOS.remove(0);
					}
				}
			}
			else {
				synchronized (CACHED_EYE_PHOTOS) {
					int index = -1;
					for (int i = 0; i < CACHED_EYE_PHOTOS.size(); i++) {
						if (CACHED_EYE_PHOTOS.get(i).get() == this) {
							index = i;
							break;
						}
					}
					if (index >= 0) {
						CACHED_EYE_PHOTOS.remove(index);
						CACHED_EYE_PHOTOS.add(new WeakReference<>(this));
					}
				}
			}
			return result;
		case FULL:
			// Full size image is not cached.
			return ImageUtil.getImage(getFile(), Resolution.FULL);
		default:
			return null;
		}
	}

	/**
	 * Change the personName renaming the file (keeping the path).
	 *
	 * @param targetName
	 *            the target name
	 * @return true if the renaming was successful.
	 */
	public final boolean changePersonName(final String targetName) {
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
	 * @param newDate
	 *            the target date.
	 * @return true if the change was successful.
	 */
	public final boolean changeDate(final Date newDate) {
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
			metadata.setOrganizeDate(newDate);
			target.storeImageMetadata(metadata);
		}

		return success;
	}

	/**
	 * Retrieve a clone of this object from the absolute path.
	 *
	 * @return a clone (recreation) of this object having the same absolute path.
	 */
	public final EyePhoto cloneFromPath() {
		return new EyePhoto(new File(getAbsolutePath()));
	}

	/**
	 * Get the metadata stored in the file.
	 *
	 * @return the metadata.
	 */
	public final JpegMetadata getImageMetadata() {
		return JpegSynchronizationUtil.getJpegMetadata(getAbsolutePath());
	}

	/**
	 * Store the metadata in the file.
	 *
	 * @param metadata
	 *            the metadata to be stored.
	 */
	public final void storeImageMetadata(final JpegMetadata metadata) {
		JpegSynchronizationUtil.storeJpegMetadata(getAbsolutePath(), metadata);
	}

	/**
	 * Update metadata object with default metadata, based on the file name.
	 *
	 * @param metadata
	 *            the metadata object to be enhanced by the default information.
	 */
	public final void updateMetadataWithDefaults(final JpegMetadata metadata) {
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
	 * @param other
	 *            the other image to be compared to
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
				return ResourceUtil.getString(ResourceConstants.FILE_INFIX_LEFT);
			case RIGHT:
				return ResourceUtil.getString(ResourceConstants.FILE_INFIX_RIGHT);
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
				return ResourceUtil.getString(ResourceConstants.SUFFIX_TITLE_LEFT);
			case RIGHT:
				return ResourceUtil.getString(ResourceConstants.SUFFIX_TITLE_RIGHT);
			default:
				return null;
			}
		}

		/**
		 * Convert a String into a RightLeft enum (by first letter).
		 *
		 * @param rightLeftString
		 *            The String to be converted.
		 * @return the converted RightString.
		 */
		public static RightLeft fromString(final String rightLeftString) {
			if (rightLeftString != null && rightLeftString.matches("[rRdD].*")) {
				return RIGHT;
			}
			else {
				return LEFT;
			}
		}
	}
}
