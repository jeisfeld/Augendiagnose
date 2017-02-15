package de.eisfeldj.augendiagnosefx.util.imagefile;

import java.util.Date;

import de.eisfeldj.augendiagnosefx.util.DateUtil;

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

	public final void setRightEye(final EyePhoto rightEye) {
		this.mRightEye = rightEye;
	}

	public final EyePhoto getLeftEye() {
		return mLeftEye;
	}

	public final void setLeftEye(final EyePhoto leftEye) {
		this.mLeftEye = leftEye;
	}

	/**
	 * Set the right or left eye photo in the pair (dependent on the information stored in the photo).
	 *
	 * @param eyePhoto
	 *            the photo to be stored.
	 */
	public final void setEyePhoto(final EyePhoto eyePhoto) {
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
	 * @return the formatted date.
	 */
	public final String getDateDisplayString() {
		return DateUtil.format(getDate());
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
	 * @param targetFolder
	 *            the target folder.
	 * @return true if the move was successful on both eyes.
	 */
	public final boolean moveToFolder(final String targetFolder) {
		return (mRightEye == null || mRightEye.moveToFolder(targetFolder))
				&& (mLeftEye == null || mLeftEye.moveToFolder(targetFolder));
	}

	/**
	 * Change the date of the eye photo pair.
	 *
	 * @param newDate
	 *            the new date.
	 * @return true if the change operation was successful on both eyes.
	 */
	public final boolean changeDate(final Date newDate) {
		return (mRightEye == null || mRightEye.changeDate(newDate))
				&& (mLeftEye == null || mLeftEye.changeDate(newDate));
	}

}
