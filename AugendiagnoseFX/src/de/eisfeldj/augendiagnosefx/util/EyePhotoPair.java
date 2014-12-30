package de.eisfeldj.augendiagnosefx.util;

import java.util.Date;

/**
 * Utility class to handle a pair of eye photos (right and left).
 */
public class EyePhotoPair {
	/**
	 * The eye photos contained in the pair.
	 */
	private EyePhoto rightEye, leftEye;

	public final EyePhoto getRightEye() {
		return rightEye;
	}

	public final void setRightEye(final EyePhoto rightEye) {
		this.rightEye = rightEye;
	}

	public final EyePhoto getLeftEye() {
		return leftEye;
	}

	public final void setLeftEye(final EyePhoto leftEye) {
		this.leftEye = leftEye;
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
		return rightEye.getDate();
	}

	/**
	 * Return the date as String for display (Assumption: both photos should have the same date.).
	 *
	 * @param format
	 *            the date format
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
		return leftEye != null && rightEye != null;
	}

	/**
	 * Delete the eye photo pair.
	 *
	 * @return true if the deletion was successful on both eyes.
	 */
	public final boolean delete() {
		return rightEye.delete() && leftEye.delete();
	}

	/**
	 * Move the eye photo pair to a different folder.
	 *
	 * @param targetFolder
	 *            the target folder.
	 * @return true if the move was successful on both eyes.
	 */
	public final boolean moveToFolder(final String targetFolder) {
		return rightEye.moveToFolder(targetFolder) && leftEye.moveToFolder(targetFolder);
	}

//	/**
//	 * Change the date of the eye photo pair.
//	 *
//	 * @param newDate
//	 *            the new date.
//	 * @return true if the change operation was successful on both eyes.
//	 */
//	public final boolean changeDate(final Date newDate) {
//		return rightEye.changeDate(newDate) && leftEye.changeDate(newDate);
//	}

}
