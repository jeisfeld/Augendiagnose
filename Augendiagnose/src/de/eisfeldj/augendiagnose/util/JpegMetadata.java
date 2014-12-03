package de.eisfeldj.augendiagnose.util;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import de.eisfeldj.augendiagnose.util.EyePhoto.RightLeft;

/**
 * Helper class holding the metadata to be written into the file.
 */
public final class JpegMetadata implements Parcelable {
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

	private long getOrganizeDateLong() {
		return organizeDate == null ? 0 : organizeDate.getTime();
	}

	private void setOrganizeDateFromLong(final long timestamp) {
		organizeDate = timestamp == 0 ? null : new Date(timestamp);
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
		str.append("X-Position: " + xCenter + "\n");
		str.append("Y-Position: " + yCenter + "\n");
		str.append("OverlayScaleFactor: " + overlayScaleFactor + "\n");
		str.append("OrganizeDate: " + organizeDate + "\n");
		str.append("RightLeft: " + rightLeft + "\n");
		str.append("Brightness: " + brightness + "\n");
		str.append("Contrast: " + contrast + "\n");
		str.append("OverlayColor: " + getOverlayColorString() + "\n");
		return str.toString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		// using String values in order to be fine with null values
		dest.writeString(title);
		dest.writeString(description);
		dest.writeString(subject);
		dest.writeString(comment);
		dest.writeString(person);
		dest.writeString(getXCenterString());
		dest.writeString(getYCenterString());
		dest.writeString(getOverlayScaleFactorString());
		dest.writeLong(getOrganizeDateLong());
		dest.writeString(getRightLeftString());
		dest.writeString(getBrightnessString());
		dest.writeString(getContrastString());
		dest.writeString(getOverlayColorString());
	}

	/**
	 * Required field for Parcelable implementation.
	 */
	public static final Parcelable.Creator<JpegMetadata> CREATOR = new Parcelable.Creator<JpegMetadata>() {
		@Override
		public JpegMetadata createFromParcel(final Parcel in) {
			JpegMetadata metadata = new JpegMetadata();
			metadata.title = in.readString();
			metadata.description = in.readString();
			metadata.subject = in.readString();
			metadata.comment = in.readString();
			metadata.person = in.readString();
			metadata.setXCenter(in.readString());
			metadata.setYCenter(in.readString());
			metadata.setOverlayScaleFactor(in.readString());
			metadata.setOrganizeDateFromLong(in.readLong());
			metadata.setRightLeft(in.readString());
			metadata.setBrightness(in.readString());
			metadata.setContrast(in.readString());
			metadata.setOverlayColor(in.readString());
			return metadata;
		}

		@Override
		public JpegMetadata[] newArray(final int size) {
			return new JpegMetadata[size];
		}
	};
}