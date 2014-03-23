package de.eisfeldj.augendiagnose.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.IImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.jpeg.xmp.JpegXmpRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.MicrosoftTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.imaging.util.IoUtils;

import android.util.Log;

import com.adobe.xmp.XMPException;

import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.util.EyePhoto.RightLeft;

/**
 * Helper clase to retrieve and save metadata in a JPEG file
 */
public abstract class JpegMetadataUtil {

	/**
	 * Log all Exif data of the file
	 * 
	 * @param imageFile
	 * @throws ImageReadException
	 * @throws IOException
	 */
	public static void printAllExifData(final File imageFile) throws ImageReadException, IOException {
		final IImageMetadata metadata = Imaging.getMetadata(imageFile);

		TiffImageMetadata tiffImageMetadata = null;
		if (metadata instanceof JpegImageMetadata) {
			tiffImageMetadata = ((JpegImageMetadata) metadata).getExif();
		}
		else if (metadata instanceof TiffImageMetadata) {
			tiffImageMetadata = (TiffImageMetadata) metadata;
		}

		@SuppressWarnings("unchecked")
		List<TiffImageMetadata.Item> items = (List<TiffImageMetadata.Item>) tiffImageMetadata.getItems();

		for (TiffImageMetadata.Item item : items) {
			Log.i(Application.TAG, item.getTiffField().toString());
		}

	}

	/**
	 * Log all XML data of the file
	 * 
	 * @param imageFile
	 * @throws ImageReadException
	 * @throws IOException
	 * @throws XMPException
	 */
	public static void printAllXmpData(final File imageFile) throws ImageReadException, IOException, XMPException {
		final String xmpString = Imaging.getXmpXml(imageFile);
		Log.i(Application.TAG, new XmpHandler(xmpString).getXmpString());
	}

	/**
	 * Validate that the file is a JPEG file
	 * 
	 * @param jpegImageFileName
	 * @throws IOException
	 * @throws ImageReadException
	 */
	protected static void checkJpeg(String jpegImageFileName) throws IOException, ImageReadException {
		File file = new File(jpegImageFileName);
		String mimeType = Imaging.getImageInfo(file).getMimeType();
		if (!mimeType.equals("image/jpeg")) {
			throw new IOException("Bad MIME type " + mimeType + " - can handle metadata only for image/jpeg.");
		}
	}

	/**
	 * Retrieve the relevant metadata of an image file
	 * 
	 * @param jpegImageFileName
	 * @return
	 * @throws IOException
	 * @throws ImageReadException
	 */
	public static Metadata getMetadata(final String jpegImageFileName) throws ImageReadException, IOException {
		checkJpeg(jpegImageFileName);
		Metadata result = new Metadata();
		final File imageFile = new File(jpegImageFileName);

		// Retrieve XMP data
		String xmpString = Imaging.getXmpXml(imageFile);
		XmpHandler parser = new XmpHandler(xmpString);

		// Some fields can be filled only from custom data
		result.setXCenter(parser.getJeItem(XmpHandler.ITEM_X_CENTER));
		result.setYCenter(parser.getJeItem(XmpHandler.ITEM_Y_CENTER));
		result.setOverlayScaleFactor(parser.getJeItem(XmpHandler.ITEM_OVERLAY_SCALE_FACTOR));
		result.organizeDate = parser.getJeDate(XmpHandler.ITEM_ORGANIZE_DATE);
		result.setRightLeft(parser.getJeItem(XmpHandler.ITEM_RIGHT_LEFT));
		result.setBrightness(parser.getJeItem(XmpHandler.ITEM_BRIGHTNESS));
		result.setContrast(parser.getJeItem(XmpHandler.ITEM_CONTRAST));

		// For standard fields, use custom data only if there is no other data.
		result.description = parser.getDcDescription();
		result.subject = parser.getDcSubject();
		result.person = parser.getMicrosoftPerson();
		result.title = parser.getDcTitle();
		result.comment = parser.getUserComment();

		// Retrieve EXIF data
		try {
			final IImageMetadata metadata = Imaging.getMetadata(imageFile);

			TiffImageMetadata tiffImageMetadata = null;
			if (metadata instanceof JpegImageMetadata) {
				tiffImageMetadata = ((JpegImageMetadata) metadata).getExif();
			}
			else if (metadata instanceof TiffImageMetadata) {
				tiffImageMetadata = (TiffImageMetadata) metadata;
			}

			TiffField title = tiffImageMetadata.findField(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION);
			TiffField comment = tiffImageMetadata.findField(ExifTagConstants.EXIF_TAG_USER_COMMENT);
			TiffField comment2 = tiffImageMetadata.findField(MicrosoftTagConstants.EXIF_TAG_XPCOMMENT);
			TiffField subject = tiffImageMetadata.findField(MicrosoftTagConstants.EXIF_TAG_XPSUBJECT);

			if (title != null) {
				result.title = title.getStringValue().trim();
			}
			if (comment != null && comment.getStringValue().trim().length() > 0) {
				result.comment = comment.getStringValue().trim();
			}
			if (comment2 != null && comment2.getStringValue().trim().length() > 0) {
				// XPComment takes precedence if existing
				result.comment = comment2.getStringValue().trim();
			}
			if (result.subject == null && subject != null) {
				result.subject = subject.getStringValue().trim();
			}
		}
		catch (Exception e) {
			Log.w(Application.TAG, "Error when retrieving Exif data", e);
		}

		// If fields are still null, try to get them from custom XMP
		if (result.description == null) {
			result.description = parser.getJeItem(XmpHandler.ITEM_DESCRIPTION);
		}
		if (result.subject == null) {
			result.subject = parser.getJeItem(XmpHandler.ITEM_SUBJECT);
		}
		if (result.person == null) {
			result.person = parser.getJeItem(XmpHandler.ITEM_PERSON);
		}
		if (result.title == null) {
			result.title = parser.getJeItem(XmpHandler.ITEM_TITLE);
		}
		if (result.comment == null) {
			result.comment = parser.getJeItem(XmpHandler.ITEM_COMMENT);
		}

		return result;
	}

	/**
	 * Change metadata of the image
	 * 
	 * @param jpegImageFileName
	 * @param metadata
	 * @throws IOException
	 * @throws ImageReadException
	 * @throws ImageWriteException
	 * @throws XMPException
	 */
	public static void changeMetadata(final String jpegImageFileName, Metadata metadata) throws IOException,
			ImageReadException, ImageWriteException, XMPException {
		if (changeJpegAllowed()) {
			checkJpeg(jpegImageFileName);
			changeXmpMetadata(jpegImageFileName, metadata);

			if (changeExifAllowed()) {
				changeExifMetadata(jpegImageFileName, metadata);
			}
		}
	}

	/**
	 * Change the EXIF metadata
	 */
	private static void changeExifMetadata(final String jpegImageFileName, Metadata metadata) throws IOException,
			ImageReadException, ImageWriteException {
		File jpegImageFile = new File(jpegImageFileName);
		String tempFileName = jpegImageFileName + ".temp";
		File tempFile = new File(tempFileName);

		if (tempFile.exists()) {
			Log.w(Application.TAG, "tempFile " + tempFileName + " already exists - deleting it");
			tempFile.delete();
		}

		OutputStream os = null;
		try {
			TiffOutputSet outputSet = null;

			// note that metadata might be null if no metadata is found.
			final IImageMetadata imageMetadata = Imaging.getMetadata(jpegImageFile);
			final JpegImageMetadata jpegMetadata = (JpegImageMetadata) imageMetadata;
			if (null != jpegMetadata) {
				// note that exif might be null if no Exif metadata is found.
				final TiffImageMetadata exif = jpegMetadata.getExif();

				if (null != exif) {
					outputSet = exif.getOutputSet();
				}
			}

			if (null == outputSet) {
				outputSet = new TiffOutputSet();
			}

			final TiffOutputDirectory rootDirectory = outputSet.getOrCreateRootDirectory();
			final TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();

			if (metadata.title != null) {
				rootDirectory.removeField(MicrosoftTagConstants.EXIF_TAG_XPTITLE);
				rootDirectory.add(MicrosoftTagConstants.EXIF_TAG_XPTITLE, metadata.title);

				rootDirectory.removeField(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION);
				rootDirectory.add(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION, metadata.title);
			}

			if (metadata.comment != null) {
				rootDirectory.removeField(MicrosoftTagConstants.EXIF_TAG_XPCOMMENT);
				rootDirectory.add(MicrosoftTagConstants.EXIF_TAG_XPCOMMENT, metadata.comment);
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_USER_COMMENT);
				exifDirectory.add(ExifTagConstants.EXIF_TAG_USER_COMMENT, metadata.comment);
			}

			if (metadata.subject != null) {
				rootDirectory.removeField(MicrosoftTagConstants.EXIF_TAG_XPSUBJECT);
				rootDirectory.add(MicrosoftTagConstants.EXIF_TAG_XPSUBJECT, metadata.subject);
			}

			os = new FileOutputStream(tempFile);
			os = new BufferedOutputStream(os);

			new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os, outputSet);

			IoUtils.closeQuietly(true, os);

			if (!tempFile.renameTo(jpegImageFile)) {
				throw new IOException("Failed to rename file " + tempFileName + " to " + jpegImageFileName);
			}
		}
		finally {
			IoUtils.closeQuietly(false, os);
		}

	}

	/**
	 * Change the XMP metadata
	 */
	private static void changeXmpMetadata(final String jpegImageFileName, Metadata metadata) throws IOException,
			ImageReadException, ImageWriteException, XMPException {
		File jpegImageFile = new File(jpegImageFileName);
		String tempFileName = jpegImageFileName + ".temp";
		File tempFile = new File(tempFileName);

		if (tempFile.exists()) {
			Log.w(Application.TAG, "tempFile " + tempFileName + " already exists - deleting it");
			tempFile.delete();
		}

		OutputStream os = null;
		try {
			final String xmpString = Imaging.getXmpXml(jpegImageFile);

			XmpHandler parser = new XmpHandler(xmpString);

			if (changeExifAllowed()) {
				// Change standard fields only if EXIF allowed
				parser.setDcTitle(metadata.title);
				parser.setDcDescription(metadata.description);
				parser.setDcSubject(metadata.subject);
				parser.setUserComment(metadata.comment);
				parser.setMicrosoftPerson(metadata.person);
			}

			parser.setJeItem(XmpHandler.ITEM_TITLE, metadata.title);
			parser.setJeItem(XmpHandler.ITEM_DESCRIPTION, metadata.description);
			parser.setJeItem(XmpHandler.ITEM_SUBJECT, metadata.subject);
			parser.setJeItem(XmpHandler.ITEM_COMMENT, metadata.comment);
			parser.setJeItem(XmpHandler.ITEM_PERSON, metadata.person);
			parser.setJeItem(XmpHandler.ITEM_X_CENTER, metadata.getXCenterString());
			parser.setJeItem(XmpHandler.ITEM_Y_CENTER, metadata.getYCenterString());
			parser.setJeItem(XmpHandler.ITEM_OVERLAY_SCALE_FACTOR, metadata.getOverlayScaleFactorString());
			parser.setJeDate(XmpHandler.ITEM_ORGANIZE_DATE, metadata.organizeDate);
			parser.setJeItem(XmpHandler.ITEM_RIGHT_LEFT, metadata.getRightLeftString());
			parser.setJeItem(XmpHandler.ITEM_BRIGHTNESS, metadata.getBrightnessString());
			parser.setJeItem(XmpHandler.ITEM_CONTRAST, metadata.getContrastString());

			os = new FileOutputStream(tempFile);
			os = new BufferedOutputStream(os);

			new JpegXmpRewriter().updateXmpXml(jpegImageFile, os, parser.getXmpString());

			IoUtils.closeQuietly(true, os);

			if (!tempFile.renameTo(jpegImageFile)) {
				throw new IOException("Failed to rename file " + tempFileName + " to " + jpegImageFileName);
			}
		}
		finally {
			IoUtils.closeQuietly(false, os);
		}
	}

	/**
	 * Check if the settings allow a change of the JPEG
	 * 
	 * @return
	 */
	private static boolean changeJpegAllowed() {
		int storeOption = Integer.parseInt(Application.getSharedPreferenceString(R.string.key_store_option));
		return storeOption > 0;
	}

	/**
	 * Check if the settings allow a change of the EXIF data
	 * 
	 * @return
	 */
	private static boolean changeExifAllowed() {
		int storeOption = Integer.parseInt(Application.getSharedPreferenceString(R.string.key_store_option));
		return storeOption == 2;
	}

	/**
	 * Helper class for storing the metadata to be written into the file
	 */
	public static class Metadata {
		public String title = null;
		public String description = null;
		public String subject = null;
		public String comment = null;
		public String person = null;
		public Float xCenter = null;
		public Float yCenter = null;
		public Float overlayScaleFactor = null;
		public Date organizeDate = null;
		public RightLeft rightLeft = null;
		public Float brightness = null;
		public Float contrast = null;

		public Metadata() {

		}

		public Metadata(String title, String description, String subject, String comment, String person,
				Float xPosition, Float yPosition, Float scaleFactor, Date organizeDate, RightLeft rightLeft,
				Float brightness, Float contrast) {
			this.title = title;
			this.description = description;
			this.subject = subject;
			this.comment = comment;
			this.person = person;
			this.xCenter = xPosition;
			this.yCenter = yPosition;
			this.overlayScaleFactor = scaleFactor;
			this.organizeDate = organizeDate;
			this.rightLeft = rightLeft;
			this.brightness = brightness;
			this.contrast = contrast;
		}

		public boolean hasCoordinates() {
			return xCenter != null && yCenter != null && overlayScaleFactor != null;
		}

		public boolean hasBrightnessContrast() {
			return brightness != null && contrast != null;
		}

		public void setXCenter(String value) {
			xCenter = value == null ? null : Float.parseFloat(value);
		}

		public String getXCenterString() {
			return xCenter == null ? null : xCenter.toString();
		}

		public void setYCenter(String value) {
			yCenter = value == null ? null : Float.parseFloat(value);
		}

		public String getYCenterString() {
			return yCenter == null ? null : yCenter.toString();
		}

		public void setOverlayScaleFactor(String value) {
			overlayScaleFactor = value == null ? null : Float.parseFloat(value);
		}

		public String getOverlayScaleFactorString() {
			return overlayScaleFactor == null ? null : overlayScaleFactor.toString();
		}

		public void setRightLeft(String value) {
			rightLeft = value == null ? null : RightLeft.fromString(value);
		}

		public String getRightLeftString() {
			return rightLeft == null ? null : rightLeft.toString();
		}

		public void setBrightness(String value) {
			brightness = value == null ? null : Float.parseFloat(value);
		}

		public String getBrightnessString() {
			return brightness == null ? null : brightness.toString();
		}


		public void setContrast(String value) {
			contrast = value == null ? null : Float.parseFloat(value);
		}

		public String getContrastString() {
			return contrast == null ? null : contrast.toString();
		}
		
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
			return str.toString();
		}

	}

}
