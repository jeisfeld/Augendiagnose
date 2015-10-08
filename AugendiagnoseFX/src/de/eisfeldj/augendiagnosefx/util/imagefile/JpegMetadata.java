package de.eisfeldj.augendiagnosefx.util.imagefile;

import java.util.Date;

import de.eisfeldj.augendiagnosefx.util.imagefile.EyePhoto.RightLeft;

/**
 * Helper class holding the metadata to be written into the file.
 */
public final class JpegMetadata {
	/**
	 * Flag indicating that the overlay size has been set automatically by camera activity ant not by user.
	 */
	public static final int FLAG_OVERLAY_SET_BY_CAMERA_ACTIVITY = 2;

	// JAVADOC:OFF
	private static final String LINE_BREAK = "\n";

	private String mTitle = null;
	private String mDescription = null;
	private String mSubject = null;
	private String mComment = null;
	private String mPerson = null;
	private Float mXCenter = null;
	private Float mYCenter = null;
	private Float mOverlayScaleFactor = null;
	private Float mXPosition = null;
	private Float mYPosition = null;
	private Float mZoomFactor = null;
	private Date mOrganizeDate = null;
	private RightLeft mRightLeft = null;
	private Float mBrightness = null;
	private Float mContrast = null;
	private Float mPupilSize = null;
	private Float mPupilXOffset = null;
	private Float mPupilYOffset = null;
	private Integer mOverlayColor = null;
	private int mFlags = 0;

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(final String title) {
		this.mTitle = title;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(final String description) {
		this.mDescription = description;
	}

	public String getSubject() {
		return mSubject;
	}

	public void setSubject(final String subject) {
		this.mSubject = subject;
	}

	public String getComment() {
		return mComment;
	}

	public void setComment(final String comment) {
		this.mComment = comment;
	}

	public String getPerson() {
		return mPerson;
	}

	public void setPerson(final String person) {
		this.mPerson = person;
	}

	public Float getXCenter() {
		return mXCenter;
	}

	public void setXCenter(final Float xCenter) {
		this.mXCenter = xCenter;
	}

	public Float getYCenter() {
		return mYCenter;
	}

	public void setYCenter(final Float yCenter) {
		this.mYCenter = yCenter;
	}

	public Float getOverlayScaleFactor() {
		return mOverlayScaleFactor;
	}

	public void setOverlayScaleFactor(final Float overlayScaleFactor) {
		this.mOverlayScaleFactor = overlayScaleFactor;
	}

	public Float getXPosition() {
		return mXPosition;
	}

	public void setXPosition(final Float xPosition) {
		this.mXPosition = xPosition;
	}

	public Float getYPosition() {
		return mYPosition;
	}

	public void setYPosition(final Float yPosition) {
		this.mYPosition = yPosition;
	}

	public Float getZoomFactor() {
		return mZoomFactor;
	}

	public void setZoomFactor(final Float zoomFactor) {
		this.mZoomFactor = zoomFactor;
	}

	public Date getOrganizeDate() {
		return mOrganizeDate;
	}

	public void setOrganizeDate(final Date organizeDate) {
		this.mOrganizeDate = organizeDate;
	}

	public RightLeft getRightLeft() {
		return mRightLeft;
	}

	public void setRightLeft(final RightLeft rightLeft) {
		this.mRightLeft = rightLeft;
	}

	public Float getBrightness() {
		return mBrightness;
	}

	public void setBrightness(final Float brightness) {
		this.mBrightness = brightness;
	}

	public Float getContrast() {
		return mContrast;
	}

	public void setContrast(final Float contrast) {
		this.mContrast = contrast;
	}

	public Integer getOverlayColor() {
		return mOverlayColor;
	}

	public void setOverlayColor(final Integer overlayColor) {
		this.mOverlayColor = overlayColor;
	}

	public Float getPupilSize() {
		return mPupilSize;
	}

	public void setPupilSize(final Float pupilSize) {
		this.mPupilSize = pupilSize;
	}

	public Float getPupilXOffset() {
		return mPupilXOffset;
	}

	public void setPupilXOffset(final Float pupilXOffset) {
		this.mPupilXOffset = pupilXOffset;
	}

	public Float getPupilYOffset() {
		return mPupilYOffset;
	}

	public void setPupilYOffset(final Float pupilYOffset) {
		this.mPupilYOffset = pupilYOffset;
	}

	protected int getFlags() {
		return mFlags;
	}

	protected void setFlags(final int flags) {
		this.mFlags = flags;
	}

	// JAVADOC:ON

	/**
	 * Check if overlay position is stored.
	 *
	 * @return true if overlay position is stored.
	 */
	public boolean hasOverlayPosition() {
		return mXCenter != null && mYCenter != null && mOverlayScaleFactor != null;
	}

	/**
	 * Check if the position of the image in the view is stored.
	 *
	 * @return true if the image position is stored.
	 */
	public boolean hasViewPosition() {
		return mXPosition != null && mYPosition != null && mZoomFactor != null;
	}

	/**
	 * Check if brightness and contrast are stored.
	 *
	 * @return true if brightness and contrast are stored.
	 */
	public boolean hasBrightnessContrast() {
		return mBrightness != null && mContrast != null;
	}

	// JAVADOC:OFF

	// Getters and setters with type conversion.

	public void setXCenter(final String value) {
		mXCenter = value == null ? null : Float.parseFloat(value);
	}

	public String getXCenterString() {
		return mXCenter == null ? null : mXCenter.toString();
	}

	public void setYCenter(final String value) {
		mYCenter = value == null ? null : Float.parseFloat(value);
	}

	public String getYCenterString() {
		return mYCenter == null ? null : mYCenter.toString();
	}

	public void setOverlayScaleFactor(final String value) {
		mOverlayScaleFactor = value == null ? null : Float.parseFloat(value);
	}

	public String getOverlayScaleFactorString() {
		return mOverlayScaleFactor == null ? null : mOverlayScaleFactor.toString();
	}

	public void setXPosition(final String value) {
		mXPosition = value == null ? null : Float.parseFloat(value);
	}

	public String getXPositionString() {
		return mXPosition == null ? null : mXPosition.toString();
	}

	public void setYPosition(final String value) {
		mYPosition = value == null ? null : Float.parseFloat(value);
	}

	public String getYPositionString() {
		return mYPosition == null ? null : mYPosition.toString();
	}

	public void setZoomFactor(final String value) {
		mZoomFactor = value == null ? null : Float.parseFloat(value);
	}

	public String getZoomFactorString() {
		return mZoomFactor == null ? null : mZoomFactor.toString();
	}

	public void setRightLeft(final String value) {
		mRightLeft = value == null ? null : RightLeft.fromString(value);
	}

	public String getRightLeftString() {
		return mRightLeft == null ? null : mRightLeft.toString();
	}

	public void setBrightness(final String value) {
		mBrightness = value == null ? null : Float.parseFloat(value);
	}

	public String getBrightnessString() {
		return mBrightness == null ? null : mBrightness.toString();
	}

	public void setContrast(final String value) {
		mContrast = value == null ? null : Float.parseFloat(value);
	}

	public String getContrastString() {
		return mContrast == null ? null : mContrast.toString();
	}

	public void setOverlayColor(final String value) {
		mOverlayColor = value == null ? null : (int) Long.parseLong(value, 16); // MAGIC_NUMBER
	}

	public String getOverlayColorString() {
		return mOverlayColor == null ? null : Integer.toHexString(mOverlayColor);
	}

	public void setPupilSize(final String value) {
		mPupilSize = value == null ? null : Float.parseFloat(value);
	}

	public String getPupilSizeString() {
		return mPupilSize == null ? null : mPupilSize.toString();
	}

	public void setPupilXOffset(final String value) {
		mPupilXOffset = value == null ? null : Float.parseFloat(value);
	}

	public String getPupilXOffsetString() {
		return mPupilXOffset == null ? null : mPupilXOffset.toString();
	}

	public void setPupilYOffset(final String value) {
		mPupilYOffset = value == null ? null : Float.parseFloat(value);
	}

	public String getPupilYOffsetString() {
		return mPupilYOffset == null ? null : mPupilYOffset.toString();
	}

	// JAVADOC:ON

	/**
	 * Add a flag from the constants JpegMetadata.FLAG_XXX.
	 *
	 * @param flag
	 *            The flag to be added.
	 */
	public void addFlag(final int flag) {
		mFlags |= flag;
	}

	/**
	 * Remove a flag from the constants JpegMetadata.FLAG_XXX.
	 *
	 * @param flag
	 *            The flag to be removed.
	 */
	public void removeFlag(final int flag) {
		mFlags &= ~flag;
	}

	/**
	 * Get a flag from the constants JpegMetadata.FLAG_XXX.
	 *
	 * @param flag
	 *            The flag to be retrieved.
	 * @return true if the flag is set.
	 */
	public boolean hasFlag(final int flag) {
		return (mFlags & flag) != 0;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("Title: " + mTitle + LINE_BREAK);
		str.append("Description: " + mDescription + LINE_BREAK);
		str.append("Subject: " + mSubject + LINE_BREAK);
		str.append("Comment: " + mComment + LINE_BREAK);
		str.append("Person: " + mPerson + LINE_BREAK);
		str.append("X-Center: " + mXCenter + LINE_BREAK);
		str.append("Y-Center: " + mYCenter + LINE_BREAK);
		str.append("OverlayScaleFactor: " + mOverlayScaleFactor + LINE_BREAK);
		str.append("X-Position: " + mXPosition + LINE_BREAK);
		str.append("Y-Position: " + mYPosition + LINE_BREAK);
		str.append("ZoomFactor: " + mZoomFactor + LINE_BREAK);
		str.append("OrganizeDate: " + mOrganizeDate + LINE_BREAK);
		str.append("RightLeft: " + mRightLeft + LINE_BREAK);
		str.append("Brightness: " + mBrightness + LINE_BREAK);
		str.append("Contrast: " + mContrast + LINE_BREAK);
		str.append("OverlayColor: " + getOverlayColorString() + LINE_BREAK);
		str.append("Pupil-Size: " + mPupilSize + LINE_BREAK);
		str.append("Pupil-X-Offset: " + mPupilXOffset + LINE_BREAK);
		str.append("Pupil-Y-Offset: " + mPupilYOffset + LINE_BREAK);
		str.append("Flags: " + mFlags + LINE_BREAK);
		return str.toString();
	}

}
