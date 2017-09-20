package de.jeisfeld.augendiagnoselib.util.imagefile;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto.RightLeft;

/**
 * Helper class holding the metadata to be written into the file.
 */
public final class JpegMetadata implements Parcelable {
	/**
	 * Flag indicating that the overlay size has been set automatically by camera activity and not by user.
	 */
	public static final int FLAG_OVERLAY_SET_BY_CAMERA_ACTIVITY = 0b10;
	/**
	 * Flag indicating that the overlay size has been determined automatically by PupilAndIrisDetector.
	 */
	public static final int FLAG_OVERLAY_POSITION_DETERMINED_AUTOMATICALLY = 0b100;

	// JAVADOC:OFF
	private static final String LINE_BREAK = "\n";

	@Nullable
	private String mTitle = null;
	@Nullable
	private String mDescription = null;
	@Nullable
	private String mSubject = null;
	@Nullable
	private String mComment = null;
	@Nullable
	private String mPerson = null;
	@Nullable
	private Float mXCenter = null;
	@Nullable
	private Float mYCenter = null;
	@Nullable
	private Float mOverlayScaleFactor = null;
	@Nullable
	private Float mXPosition = null;
	@Nullable
	private Float mYPosition = null;
	@Nullable
	private Float mZoomFactor = null;
	@Nullable
	private Date mOrganizeDate = null;
	@Nullable
	private RightLeft mRightLeft = null;
	@Nullable
	private Float mBrightness = null;
	@Nullable
	private Float mContrast = null;
	@Nullable
	private Float mSaturation = null;
	@Nullable
	private Float mColorTemperature = null;
	@Nullable
	private Float mPupilSize = null;
	@Nullable
	private Float mPupilXOffset = null;
	@Nullable
	private Float mPupilYOffset = null;
	@Nullable
	private Integer mOverlayColor = null;
	private int mFlags = 0;

	@Nullable
	public String getTitle() {
		return mTitle;
	}

	public void setTitle(final String title) {
		this.mTitle = title;
	}

	@Nullable
	public String getDescription() {
		return mDescription;
	}

	public void setDescription(final String description) {
		this.mDescription = description;
	}

	@Nullable
	public String getSubject() {
		return mSubject;
	}

	public void setSubject(final String subject) {
		this.mSubject = subject;
	}

	@Nullable
	public String getComment() {
		return mComment;
	}

	public void setComment(final String comment) {
		this.mComment = comment;
	}

	@Nullable
	public String getPerson() {
		return mPerson;
	}

	public void setPerson(final String person) {
		this.mPerson = person;
	}

	@Nullable
	public Float getXCenter() {
		return mXCenter;
	}

	public void setXCenter(final Float xCenter) {
		this.mXCenter = xCenter;
	}

	@Nullable
	public Float getYCenter() {
		return mYCenter;
	}

	public void setYCenter(final Float yCenter) {
		this.mYCenter = yCenter;
	}

	@Nullable
	public Float getOverlayScaleFactor() {
		return mOverlayScaleFactor;
	}

	public void setOverlayScaleFactor(final Float overlayScaleFactor) {
		this.mOverlayScaleFactor = overlayScaleFactor;
	}

	@Nullable
	public Float getXPosition() {
		return mXPosition;
	}

	public void setXPosition(final Float xPosition) {
		this.mXPosition = xPosition;
	}

	@Nullable
	public Float getYPosition() {
		return mYPosition;
	}

	public void setYPosition(final Float yPosition) {
		this.mYPosition = yPosition;
	}

	@Nullable
	public Float getZoomFactor() {
		return mZoomFactor;
	}

	public void setZoomFactor(final Float zoomFactor) {
		this.mZoomFactor = zoomFactor;
	}

	@Nullable
	public Date getOrganizeDate() {
		return mOrganizeDate;
	}

	public void setOrganizeDate(final Date organizeDate) {
		this.mOrganizeDate = organizeDate;
	}

	@Nullable
	public RightLeft getRightLeft() {
		return mRightLeft;
	}

	public void setRightLeft(final RightLeft rightLeft) {
		this.mRightLeft = rightLeft;
	}

	@Nullable
	public Float getBrightness() {
		return mBrightness;
	}

	public void setBrightness(final Float brightness) {
		this.mBrightness = brightness;
	}

	@Nullable
	public Float getContrast() {
		return mContrast;
	}

	public void setContrast(final Float contrast) {
		this.mContrast = contrast;
	}

	@Nullable
	public Float getSaturation() {
		return mSaturation;
	}

	public void setSaturation(final Float saturation) {
		this.mSaturation = saturation;
	}

	@Nullable
	public Float getColorTemperature() {
		return mColorTemperature;
	}

	public void setColorTemperature(final Float colorTemparature) {
		this.mColorTemperature = colorTemparature;
	}

	@Nullable
	public Integer getOverlayColor() {
		return mOverlayColor;
	}

	public void setOverlayColor(final Integer overlayColor) {
		this.mOverlayColor = overlayColor;
	}

	@Nullable
	public Float getPupilSize() {
		return mPupilSize;
	}

	public void setPupilSize(final Float pupilSize) {
		this.mPupilSize = pupilSize;
	}

	@Nullable
	public Float getPupilXOffset() {
		return mPupilXOffset;
	}

	public void setPupilXOffset(final Float pupilXOffset) {
		this.mPupilXOffset = pupilXOffset;
	}

	@Nullable
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

	/**
	 * The EXIF orientation is not persisted in the XMP Metadata structure, but only used for storage in EXIF.
	 */
	@Nullable
	private Short mOrientation = null;

	@Nullable
	public Short getOrientation() {
		return mOrientation;
	}

	public void setOrientation(final Short orientation) {
		this.mOrientation = orientation;
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

	public void setXCenter(@Nullable final String value) {
		mXCenter = value == null ? null : Float.parseFloat(value);
	}

	@Nullable
	public String getXCenterString() {
		return mXCenter == null ? null : mXCenter.toString();
	}

	public void setYCenter(@Nullable final String value) {
		mYCenter = value == null ? null : Float.parseFloat(value);
	}

	@Nullable
	public String getYCenterString() {
		return mYCenter == null ? null : mYCenter.toString();
	}

	public void setOverlayScaleFactor(@Nullable final String value) {
		mOverlayScaleFactor = value == null ? null : Float.parseFloat(value);
	}

	@Nullable
	public String getOverlayScaleFactorString() {
		return mOverlayScaleFactor == null ? null : mOverlayScaleFactor.toString();
	}

	public void setXPosition(@Nullable final String value) {
		mXPosition = value == null ? null : Float.parseFloat(value);
	}

	@Nullable
	public String getXPositionString() {
		return mXPosition == null ? null : mXPosition.toString();
	}

	public void setYPosition(@Nullable final String value) {
		mYPosition = value == null ? null : Float.parseFloat(value);
	}

	@Nullable
	public String getYPositionString() {
		return mYPosition == null ? null : mYPosition.toString();
	}

	public void setZoomFactor(@Nullable final String value) {
		mZoomFactor = value == null ? null : Float.parseFloat(value);
	}

	@Nullable
	public String getZoomFactorString() {
		return mZoomFactor == null ? null : mZoomFactor.toString();
	}

	private long getOrganizeDateLong() {
		return mOrganizeDate == null ? 0 : mOrganizeDate.getTime();
	}

	private void setOrganizeDateFromLong(final long timestamp) {
		mOrganizeDate = timestamp == 0 ? null : new Date(timestamp);
	}

	public void setRightLeft(@Nullable final String value) {
		mRightLeft = value == null ? null : RightLeft.fromString(value);
	}

	@Nullable
	public String getRightLeftString() {
		return mRightLeft == null ? null : mRightLeft.toString();
	}

	public void setBrightness(@Nullable final String value) {
		mBrightness = value == null ? null : Float.parseFloat(value);
	}

	@Nullable
	public String getBrightnessString() {
		return mBrightness == null ? null : mBrightness.toString();
	}

	public void setContrast(@Nullable final String value) {
		mContrast = value == null ? null : Float.parseFloat(value);
	}

	@Nullable
	public String getContrastString() {
		return mContrast == null ? null : mContrast.toString();
	}

	public void setSaturation(@Nullable final String value) {
		mSaturation = value == null ? null : Float.parseFloat(value);
	}

	@Nullable
	public String getSaturationString() {
		return mSaturation == null ? null : mSaturation.toString();
	}

	public void setColorTemperature(@Nullable final String value) {
		mColorTemperature = value == null ? null : Float.parseFloat(value);
	}

	@Nullable
	public String getColorTemperatureString() {
		return mColorTemperature == null ? null : mColorTemperature.toString();
	}

	public void setOverlayColor(@Nullable final String value) {
		mOverlayColor = value == null ? null : (int) Long.parseLong(value, 16); // MAGIC_NUMBER
	}

	@Nullable
	public String getOverlayColorString() {
		return mOverlayColor == null ? null : Integer.toHexString(mOverlayColor);
	}

	public void setPupilSize(@Nullable final String value) {
		mPupilSize = value == null ? null : Float.parseFloat(value);
	}

	@Nullable
	public String getPupilSizeString() {
		return mPupilSize == null ? null : mPupilSize.toString();
	}

	public void setPupilXOffset(@Nullable final String value) {
		mPupilXOffset = value == null ? null : Float.parseFloat(value);
	}

	@Nullable
	public String getPupilXOffsetString() {
		return mPupilXOffset == null ? null : mPupilXOffset.toString();
	}

	public void setPupilYOffset(@Nullable final String value) {
		mPupilYOffset = value == null ? null : Float.parseFloat(value);
	}

	@Nullable
	public String getPupilYOffsetString() {
		return mPupilYOffset == null ? null : mPupilYOffset.toString();
	}

	private void setOrientation(@Nullable final String value) {
		mOrientation = value == null ? null : Short.parseShort(value);
	}

	@Nullable
	private String getOrientationString() {
		return mOrientation == null ? null : Short.toString(mOrientation);
	}

	// JAVADOC:ON

	/**
	 * Add a flag from the constants JpegMetadata.FLAG_XXX.
	 *
	 * @param flag The flag to be added.
	 */
	public void addFlag(final int flag) {
		mFlags |= flag;
	}

	/**
	 * Remove a flag from the constants JpegMetadata.FLAG_XXX.
	 *
	 * @param flag The flag to be removed.
	 */
	public void removeFlag(final int flag) {
		mFlags &= ~flag;
	}

	/**
	 * Get a flag from the constants JpegMetadata.FLAG_XXX.
	 *
	 * @param flag The flag to be retrieved.
	 * @return true if the flag is set.
	 */
	public boolean hasFlag(final int flag) {
		return (mFlags & flag) != 0;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Title: ").append(mTitle).append(LINE_BREAK);
		str.append("Description: ").append(mDescription).append(LINE_BREAK);
		str.append("Subject: ").append(mSubject).append(LINE_BREAK);
		str.append("Comment: ").append(mComment).append(LINE_BREAK);
		str.append("Person: ").append(mPerson).append(LINE_BREAK);
		str.append("X-Center: ").append(mXCenter).append(LINE_BREAK);
		str.append("Y-Center: ").append(mYCenter).append(LINE_BREAK);
		str.append("OverlayScaleFactor: ").append(mOverlayScaleFactor).append(LINE_BREAK);
		str.append("X-Position: ").append(mXPosition).append(LINE_BREAK);
		str.append("Y-Position: ").append(mYPosition).append(LINE_BREAK);
		str.append("ZoomFactor: ").append(mZoomFactor).append(LINE_BREAK);
		str.append("OrganizeDate: ").append(mOrganizeDate).append(LINE_BREAK);
		str.append("RightLeft: ").append(mRightLeft).append(LINE_BREAK);
		str.append("Brightness: ").append(mBrightness).append(LINE_BREAK);
		str.append("RightLeft: ").append(mRightLeft).append(LINE_BREAK);
		str.append("Brightness: ").append(mBrightness).append(LINE_BREAK);
		str.append("Contrast: ").append(mContrast).append(LINE_BREAK);
		str.append("Saturation: ").append(mSaturation).append(LINE_BREAK);
		str.append("ColorTemperature: ").append(mColorTemperature).append(LINE_BREAK);
		str.append("OverlayColor: ").append(getOverlayColorString()).append(LINE_BREAK);
		str.append("Pupil-Size: ").append(mPupilSize).append(LINE_BREAK);
		str.append("Pupil-X-Offset: ").append(mPupilXOffset).append(LINE_BREAK);
		str.append("Pupil-Y-Offset: ").append(mPupilYOffset).append(LINE_BREAK);
		str.append("Flags: ").append(mFlags).append(LINE_BREAK);
		str.append("Orientation: ").append(getOrientationString()).append(LINE_BREAK);
		return str.toString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(@NonNull final Parcel dest, final int flags) {
		// using String values in order to be fine with null values
		dest.writeString(mTitle);
		dest.writeString(mDescription);
		dest.writeString(mSubject);
		dest.writeString(mComment);
		dest.writeString(mPerson);
		dest.writeString(getXCenterString());
		dest.writeString(getYCenterString());
		dest.writeString(getOverlayScaleFactorString());
		dest.writeLong(getOrganizeDateLong());
		dest.writeString(getRightLeftString());
		dest.writeString(getBrightnessString());
		dest.writeString(getContrastString());
		dest.writeString(getSaturationString());
		dest.writeString(getColorTemperatureString());
		dest.writeString(getOverlayColorString());
		dest.writeString(getPupilSizeString());
		dest.writeString(getPupilXOffsetString());
		dest.writeString(getPupilYOffsetString());
		dest.writeInt(mFlags);
		dest.writeString(getOrientationString());
	}

	/**
	 * Required field for Parcelable implementation.
	 */
	public static final Parcelable.Creator<JpegMetadata> CREATOR = new Parcelable.Creator<JpegMetadata>() {
		@NonNull
		@Override
		public JpegMetadata createFromParcel(@NonNull final Parcel in) {
			JpegMetadata metadata = new JpegMetadata();
			metadata.mTitle = in.readString();
			metadata.mDescription = in.readString();
			metadata.mSubject = in.readString();
			metadata.mComment = in.readString();
			metadata.mPerson = in.readString();
			metadata.setXCenter(in.readString());
			metadata.setYCenter(in.readString());
			metadata.setOverlayScaleFactor(in.readString());
			metadata.setOrganizeDateFromLong(in.readLong());
			metadata.setRightLeft(in.readString());
			metadata.setBrightness(in.readString());
			metadata.setContrast(in.readString());
			metadata.setSaturation(in.readString());
			metadata.setColorTemperature(in.readString());
			metadata.setOverlayColor(in.readString());
			metadata.setPupilSize(in.readString());
			metadata.setPupilXOffset(in.readString());
			metadata.setPupilYOffset(in.readString());
			metadata.mFlags = in.readInt();
			metadata.setOrientation(in.readString());
			return metadata;
		}

		@NonNull
		@Override
		public JpegMetadata[] newArray(final int size) {
			return new JpegMetadata[size];
		}
	};
}
