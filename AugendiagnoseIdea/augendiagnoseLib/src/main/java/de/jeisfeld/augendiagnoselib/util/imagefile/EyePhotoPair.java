package de.jeisfeld.augendiagnoselib.util.imagefile;

import java.util.Date;

import androidx.annotation.NonNull;
import de.jeisfeld.augendiagnoselib.util.DateUtil;

/**
 * Utility class to handle a pair of eye photos (right and left).
 */
public class EyePhotoPair {
	/**
	 * The eye photos contained in the pair.
	 */
	private EyePhoto mRightEye, mLeftEye;

	public final EyePhoto getRightEye() {
		return mRightEye;
	}

	private void setRightEye(final EyePhoto rightEye) {
		this.mRightEye = rightEye;
	}

	public final EyePhoto getLeftEye() {
		return mLeftEye;
	}

	private void setLeftEye(final EyePhoto leftEye) {
		this.mLeftEye = leftEye;
	}

	/**
	 * Set the right or left eye photo in the pair (dependent on the information stored in the photo).
	 *
	 * @param eyePhoto the photo to be stored.
	 */
	public final void setEyePhoto(@NonNull final EyePhoto eyePhoto) {
		switch (eyePhoto.getRightLeft()) {
		case RIGHT:
			setRightEye(eyePhoto);
			break;
		case LEFT:
			setLeftEye(eyePhoto);
			break;
		default:
			break;
		}
	}

	/**
	 * Returns the date of the right photo. (Assumption: both should have the same date.)
	 *
	 * @return the date of the right photo.
	 */
	public final Date getDate() {
		return mRightEye == null ? mLeftEye.getDate() : mRightEye.getDate();
	}

	/**
	 * Returns the person name of the right photo. (Assumption: both should have the same date.)
	 *
	 * @return the person name of the right photo.
	 */
	public final String getPersonName() {
		return mRightEye == null ? mLeftEye.getPersonName() : mRightEye.getPersonName();
	}

	/**
	 * Return the date as String for display (Assumption: both photos should have the same date.).
	 *
	 * @param format the date format
	 * @return the formatted date.
	 */
	public final String getDateDisplayString(final String format) {
		return DateUtil.format(getDate(), format);
	}

	/**
	 * Return information if the object contains both eyes.
	 *
	 * @return true if both eyes are available.
	 */
	public final boolean isComplete() {
		return mLeftEye != null && mRightEye != null;
	}

	/**
	 * Delete the eye photo pair.
	 *
	 * @return true if the deletion was successful on both eyes.
	 */
	public final boolean delete() {
		return (mRightEye == null || mRightEye.delete())
				&& (mLeftEye == null || mLeftEye.delete());
	}

	/**
	 * Move the eye photo pair to a different folder.
	 *
	 * @param targetFolder the target folder.
	 * @param createUnique if true, then a unique target file name is created if a file with the same name exists in the target folder.
	 * @return true if the move was successful on both eyes.
	 */
	public final boolean moveToFolder(@NonNull final String targetFolder, final boolean createUnique) {
		return (mRightEye == null || mRightEye.moveToFolder(targetFolder, createUnique))
				&& (mLeftEye == null || mLeftEye.moveToFolder(targetFolder, createUnique));
	}

	/**
	 * Check if the date of the eye photos is changeable to the given date.
	 *
	 * @param newDate the new date.
	 * @return true if the change operation was successful on both eyes.
	 */
	public final boolean isDateChangeable(final Date newDate) {
		return (mRightEye == null || mRightEye.isDateChangeable(newDate))
				&& (mLeftEye == null || mLeftEye.isDateChangeable(newDate));
	}

	/**
	 * Change the date of the eye photo pair.
	 *
	 * @param newDate the new date.
	 * @return true if the change operation was successful on both eyes.
	 */
	public final boolean changeDate(final Date newDate) {
		return (mRightEye == null || mRightEye.changeDate(newDate))
				&& (mLeftEye == null || mLeftEye.changeDate(newDate));
	}

	/**
	 * Delete the thumbnails of this pair from the media store.
	 */
	public final void deleteThumbnailsFromMediastore() {
		if (mLeftEye != null) {
			MediaStoreUtil.deleteThumbnail(mLeftEye.getAbsolutePath());
		}
		if (mRightEye != null) {
			MediaStoreUtil.deleteThumbnail(mRightEye.getAbsolutePath());
		}
	}

}
