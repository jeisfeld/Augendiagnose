package de.eisfeldj.augendiagnose.util;

import java.util.Date;

/**
 * Utility class to handle a pair of eye photos (right and left)
 */
public class EyePhotoPair {
	private EyePhoto rightEye, leftEye;

	public EyePhoto getRightEye() {
		return rightEye;
	}

	public void setRightEye(EyePhoto rightEye) {
		this.rightEye = rightEye;
	}

	public EyePhoto getLeftEye() {
		return leftEye;
	}

	public void setLeftEye(EyePhoto leftEye) {
		this.leftEye = leftEye;
	}

	public void setEyePhoto(EyePhoto eyePhoto) {
		switch (eyePhoto.getRightLeft()) {
		case RIGHT:
			setRightEye(eyePhoto);
			break;
		case LEFT:
			setLeftEye(eyePhoto);
		}
	}

	/**
	 * Returns the date of the right photo. (Assumption: both should have the same date.)
	 * 
	 * @return
	 */
	public Date getDate() {
		return rightEye.getDate();
	}

	/**
	 * Return the date as String for display (Assumption: both photos should have the same date.)
	 * 
	 * @param format
	 * @return
	 */
	public String getDateDisplayString(String format) {
		return DateUtil.format(getDate(), format);
	}

	/**
	 * Return information if the object contains both eyes
	 * 
	 * @return
	 */
	public boolean isComplete() {
		return leftEye != null && rightEye != null;
	}

	/**
	 * Delete the eye photo pair
	 * 
	 * @return
	 */
	public boolean delete() {
		return rightEye.delete() && leftEye.delete();
	}

	/**
	 * Change the date of the eye photo pair
	 * 
	 * @param newDate
	 * @return
	 */
	public boolean changeDate(Date newDate) {
		return rightEye.changeDate(newDate) && leftEye.changeDate(newDate);
	}

}
