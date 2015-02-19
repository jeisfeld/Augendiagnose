package de.eisfeldj.augendiagnosefx.util;

import java.util.Date;

import de.eisfeldj.augendiagnosefx.util.EyePhoto.RightLeft;

/**
 * Helper class holding the metadata to be written into the file.
 */
public final class JpegMetadata {
	// JAVADOC:OFF
	// PUBLIC_FIELDS:START

	public String title = null;
	public String description = null;
	public String subject = null;
	public String comment = null;
	public String person = null;
	public Float xCenter = null;
	public Float yCenter = null;
	public Float overlayScaleFactor = null;
	public Float xPosition = null;
	public Float yPosition = null;
	public Float zoomFactor = null;
	public Date organizeDate = null;
	public RightLeft rightLeft = null;
	public Float brightness = null;
	public Float contrast = null;
	public Integer overlayColor = null;

	// PUBLIC_FIELDS:END
	// JAVADOC:ON

	/**
	 * Check if overlay position is stored.
	 *
	 * @return true if overlay position is stored.
	 */
	public boolean hasOverlayPosition() {
		return xCenter != null && yCenter != null && overlayScaleFactor != null;
	}

	/**
	 * Check if the position of the image in the view is stored.
	 *
	 * @return true if the image position is stored.
	 */
	public boolean hasViewPosition() {
		return xPosition != null && yPosition != null && zoomFactor != null;
	}

	/**
	 * Check if brightness and contrast are stored.
	 *
	 * @return true if brightness and contrast are stored.
	 */
	public boolean hasBrightnessContrast() {
		return brightness != null && contrast != null;
	}

	// JAVADOC:OFF

	// Getters and setters with type conversion.

	public void setXCenter(final String value) {
		xCenter = value == null ? null : Float.parseFloat(value);
	}

	public String getXCenterString() {
		return xCenter == null ? null : xCenter.toString();
	}

	public void setYCenter(final String value) {
		yCenter = value == null ? null : Float.parseFloat(value);
	}

	public String getYCenterString() {
		return yCenter == null ? null : yCenter.toString();
	}

	public void setOverlayScaleFactor(final String value) {
		overlayScaleFactor = value == null ? null : Float.parseFloat(value);
	}

	public String getOverlayScaleFactorString() {
		return overlayScaleFactor == null ? null : overlayScaleFactor.toString();
	}

	public void setXPosition(final String value) {
		xPosition = value == null ? null : Float.parseFloat(value);
	}

	public String getXPositionString() {
		return xPosition == null ? null : xPosition.toString();
	}

	public void setYPosition(final String value) {
		yPosition = value == null ? null : Float.parseFloat(value);
	}

	public String getYPositionString() {
		return yPosition == null ? null : yPosition.toString();
	}

	public void setZoomFactor(final String value) {
		zoomFactor = value == null ? null : Float.parseFloat(value);
	}

	public String getZoomFactorString() {
		return zoomFactor == null ? null : zoomFactor.toString();
	}

	public void setRightLeft(final String value) {
		rightLeft = value == null ? null : RightLeft.fromString(value);
	}

	public String getRightLeftString() {
		return rightLeft == null ? null : rightLeft.toString();
	}

	public void setBrightness(final String value) {
		brightness = value == null ? null : Float.parseFloat(value);
	}

	public String getBrightnessString() {
		return brightness == null ? null : brightness.toString();
	}

	public void setContrast(final String value) {
		contrast = value == null ? null : Float.parseFloat(value);
	}

	public String getContrastString() {
		return contrast == null ? null : contrast.toString();
	}

	public void setOverlayColor(final String value) {
		overlayColor = value == null ? null : (int) Long.parseLong(value, 16); // MAGIC_NUMBER
	}

	public String getOverlayColorString() {
		return overlayColor == null ? null : Integer.toHexString(overlayColor);
	}

	// JAVADOC:ON

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("Title: " + title + "\n");
		str.append("Description: " + description + "\n");
		str.append("Subject: " + subject + "\n");
		str.append("Comment: " + comment + "\n");
		str.append("Person: " + person + "\n");
		str.append("X-Center: " + xCenter + "\n");
		str.append("Y-Center: " + yCenter + "\n");
		str.append("OverlayScaleFactor: " + overlayScaleFactor + "\n");
		str.append("X-Position: " + xPosition + "\n");
		str.append("Y-Position: " + yPosition + "\n");
		str.append("ZoomFactor: " + zoomFactor + "\n");
		str.append("OrganizeDate: " + organizeDate + "\n");
		str.append("RightLeft: " + rightLeft + "\n");
		str.append("Brightness: " + brightness + "\n");
		str.append("Contrast: " + contrast + "\n");
		str.append("OverlayColor: " + getOverlayColorString() + "\n");
		return str.toString();
	}

}