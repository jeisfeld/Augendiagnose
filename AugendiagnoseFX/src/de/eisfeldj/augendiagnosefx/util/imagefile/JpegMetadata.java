package de.eisfeldj.augendiagnosefx.util.imagefile;

import java.util.Date;

import de.eisfeldj.augendiagnosefx.util.imagefile.EyePhoto.RightLeft;

/**
 * Helper class holding the metadata to be written into the file.
 */
public final class JpegMetadata {
	// JAVADOC:OFF
	private static final String LINE_BREAK = "\n";

	// PUBLIC_FIELDS:START
	public String mTitle = null;
	public String mDescription = null;
	public String mSubject = null;
	public String mComment = null;
	public String mPerson = null;
	public Float mXCenter = null;
	public Float mYCenter = null;
	public Float mOverlayScaleFactor = null;
	public Float mXPosition = null;
	public Float mYPosition = null;
	public Float mZoomFactor = null;
	public Date mOrganizeDate = null;
	public RightLeft mRightLeft = null;
	public Float mBrightness = null;
	public Float mContrast = null;
	public Integer mOverlayColor = null;

	// PUBLIC_FIELDS:END
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

	// JAVADOC:ON

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
		return str.toString();
	}

}
