package de.eisfeldj.augendiagnosefx.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

import com.adobe.xmp.XMPException;

/**
 * Helper clase to retrieve and save metadata in a JPEG file.
 */
public final class JpegMetadataUtil {

	/**
	 * Hide default constructor.
	 */
	private JpegMetadataUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Log all Exif data of the file.
	 *
	 * @param imageFile
	 *            the image file.
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
		else {
			return;
		}

		@SuppressWarnings("unchecked")
		List<TiffImageMetadata.Item> items = (List<TiffImageMetadata.Item>) tiffImageMetadata.getItems();

		for (TiffImageMetadata.Item item : items) {
			Logger.info(item.getTiffField().toString());
		}

	}

	/**
	 * Log all XML data of the file.
	 *
	 * @param imageFile
	 *            the file.
	 * @throws ImageReadException
	 * @throws IOException
	 * @throws XMPException
	 */
	public static void printAllXmpData(final File imageFile) throws ImageReadException, IOException, XMPException {
		final String xmpString = Imaging.getXmpXml(imageFile);
		Logger.info(new XmpHandler(xmpString).getXmpString());
	}

	/**
	 * Validate that the file is a JPEG file.
	 *
	 * @param jpegImageFileName
	 *            the file to be validated.
	 * @throws IOException
	 *             thrown if the file is no jpg.
	 * @throws ImageReadException
	 */
	protected static void checkJpeg(final String jpegImageFileName) throws IOException, ImageReadException {
		File file = new File(jpegImageFileName);
		String mimeType = Imaging.getImageInfo(file).getMimeType();
		if (!"image/jpeg".equals(mimeType)) {
			throw new IOException("Bad MIME type " + mimeType + " - can handle metadata only for image/jpeg.");
		}
	}

	/**
	 * Retrieve the relevant metadata of an image file.
	 *
	 * @param jpegImageFileName
	 *            the file for which metadata should be retrieved.
	 * @return the metadata of the file.
	 * @throws IOException
	 * @throws ImageReadException
	 */
	public static JpegMetadata getMetadata(final String jpegImageFileName) throws ImageReadException, IOException {
		checkJpeg(jpegImageFileName);
		JpegMetadata result = new JpegMetadata();
		final File imageFile = new File(jpegImageFileName);

		// Retrieve XMP data
		String xmpString = Imaging.getXmpXml(imageFile);
		XmpHandler parser = new XmpHandler(xmpString);

		// Standard fields are pre-filled with custom data
		result.title = parser.getJeItem(XmpHandler.ITEM_TITLE);
		result.description = parser.getJeItem(XmpHandler.ITEM_DESCRIPTION);
		result.subject = parser.getJeItem(XmpHandler.ITEM_SUBJECT);
		result.comment = parser.getJeItem(XmpHandler.ITEM_COMMENT);
		result.person = parser.getJeItem(XmpHandler.ITEM_PERSON);

		result.setXCenter(parser.getJeItem(XmpHandler.ITEM_X_CENTER));
		result.setYCenter(parser.getJeItem(XmpHandler.ITEM_Y_CENTER));
		result.setOverlayScaleFactor(parser.getJeItem(XmpHandler.ITEM_OVERLAY_SCALE_FACTOR));
		result.setXPosition(parser.getJeItem(XmpHandler.ITEM_X_POSITION));
		result.setYPosition(parser.getJeItem(XmpHandler.ITEM_Y_POSITION));
		result.setZoomFactor(parser.getJeItem(XmpHandler.ITEM_ZOOM_FACTOR));
		result.organizeDate = parser.getJeDate(XmpHandler.ITEM_ORGANIZE_DATE);
		result.setRightLeft(parser.getJeItem(XmpHandler.ITEM_RIGHT_LEFT));
		result.setBrightness(parser.getJeItem(XmpHandler.ITEM_BRIGHTNESS));
		result.setContrast(parser.getJeItem(XmpHandler.ITEM_CONTRAST));
		result.setOverlayColor(parser.getJeItem(XmpHandler.ITEM_OVERLAY_COLOR));

		// For standard fields, use custom data only if there is no other data.
		if (result.description == null) {
			result.description = parser.getDcDescription();
		}
		if (result.subject == null) {
			result.subject = parser.getDcSubject();
		}
		if (result.person == null) {
			result.person = parser.getMicrosoftPerson();
		}
		if (result.title == null) {
			result.title = parser.getDcTitle();
		}
		if (result.comment == null) {
			result.comment = parser.getUserComment();
		}

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
			else {
				return result;
			}

			TiffField title = tiffImageMetadata.findField(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION);
			TiffField comment = tiffImageMetadata.findField(ExifTagConstants.EXIF_TAG_USER_COMMENT);
			TiffField comment2 = tiffImageMetadata.findField(MicrosoftTagConstants.EXIF_TAG_XPCOMMENT);
			TiffField subject = tiffImageMetadata.findField(MicrosoftTagConstants.EXIF_TAG_XPSUBJECT);

			// EXIF data have precedence only if saving EXIF is allowed
			if (title != null && (changeExifAllowed() || result.title == null)) {
				result.title = title.getStringValue().trim();
			}
			String exifComment = null;
			if (comment != null && comment.getStringValue().trim().length() > 0) {
				exifComment = comment.getStringValue().trim();
			}
			if (comment2 != null && comment2.getStringValue().trim().length() > 0) {
				// XPComment takes precedence if existing
				exifComment = comment2.getStringValue().trim();
			}
			if (exifComment != null && (changeExifAllowed() || result.comment == null)) {
				result.comment = exifComment;
			}
			if (subject != null && (changeExifAllowed() || result.subject == null)) {
				result.subject = subject.getStringValue().trim();
			}
		}
		catch (Exception e) {
			Logger.warning("Error when retrieving Exif data: " + e.toString());
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
	 * Change metadata of the image (EXIF and XMP as far as applicable).
	 *
	 * @param jpegImageFileName
	 *            the file for which metadata should be changed.
	 * @param metadata
	 *            the new metadata.
	 * @throws IOException
	 * @throws ImageReadException
	 * @throws ImageWriteException
	 * @throws XMPException
	 */
	public static void changeMetadata(final String jpegImageFileName, final JpegMetadata metadata) throws IOException,
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
	 * Change the EXIF metadata.
	 *
	 * @param jpegImageFileName
	 *            the file for which metadata should be changed.
	 * @param metadata
	 *            the new metadata
	 * @throws IOException
	 * @throws ImageReadException
	 * @throws ImageWriteException
	 */
	@SuppressWarnings("resource")
	private static void changeExifMetadata(final String jpegImageFileName, final JpegMetadata metadata)
			throws IOException,
			ImageReadException, ImageWriteException {
		File jpegImageFile = new File(jpegImageFileName);
		String tempFileName = jpegImageFileName + ".temp";
		File tempFile = new File(tempFileName);

		verifyTempFile(tempFile);

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

			try {
				os = new FileOutputStream(tempFile);
				os = new BufferedOutputStream(os);
				new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os, outputSet);
			}
			catch (Exception e) {
				Logger.warning("Error storing EXIF data lossless - try lossy approach");
				os = new FileOutputStream(tempFile);
				os = new BufferedOutputStream(os);
				new ExifRewriter().updateExifMetadataLossy(jpegImageFile, os, outputSet);
			}

			IoUtils.closeQuietly(true, os);

			if (!FileUtil.moveFile(tempFile, jpegImageFile)) {
				throw new IOException("Failed to rename file " + tempFileName + " to " + jpegImageFileName);
			}
		}
		finally {
			IoUtils.closeQuietly(false, os);
		}

	}

	/**
	 * Change the XMP metadata.
	 *
	 * @param jpegImageFileName
	 *            the file for which metadata should be changed.
	 * @param metadata
	 *            the new metadata.
	 *
	 * @throws IOException
	 * @throws ImageReadException
	 * @throws ImageWriteException
	 * @throws XMPException
	 */
	@SuppressWarnings("resource")
	private static void changeXmpMetadata(final String jpegImageFileName, final JpegMetadata metadata)
			throws IOException,
			ImageReadException, ImageWriteException, XMPException {
		File jpegImageFile = new File(jpegImageFileName);
		String tempFileName = jpegImageFileName + ".temp";
		File tempFile = new File(tempFileName);

		verifyTempFile(tempFile);

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
			parser.setJeItem(XmpHandler.ITEM_X_POSITION, metadata.getXPositionString());
			parser.setJeItem(XmpHandler.ITEM_Y_POSITION, metadata.getYPositionString());
			parser.setJeItem(XmpHandler.ITEM_ZOOM_FACTOR, metadata.getZoomFactorString());
			parser.setJeDate(XmpHandler.ITEM_ORGANIZE_DATE, metadata.organizeDate);
			parser.setJeItem(XmpHandler.ITEM_RIGHT_LEFT, metadata.getRightLeftString());
			parser.setJeItem(XmpHandler.ITEM_BRIGHTNESS, metadata.getBrightnessString());
			parser.setJeItem(XmpHandler.ITEM_CONTRAST, metadata.getContrastString());
			parser.setJeItem(XmpHandler.ITEM_OVERLAY_COLOR, metadata.getOverlayColorString());

			os = new FileOutputStream(tempFile);
			os = new BufferedOutputStream(os);

			new JpegXmpRewriter().updateXmpXml(jpegImageFile, os, parser.getXmpString());

			IoUtils.closeQuietly(true, os);

			if (!FileUtil.moveFile(tempFile, jpegImageFile)) {
				throw new IOException("Failed to rename file " + tempFileName + " to " + jpegImageFileName);
			}
		}
		finally {
			IoUtils.closeQuietly(false, os);
		}
	}

	/**
	 * Verify if the temporary file already exists. If yes, delete it.
	 *
	 * @param tempFile
	 *            the temporary file.
	 */
	private static void verifyTempFile(final File tempFile) {
		if (tempFile.exists()) {
			Logger.warning("tempFile " + tempFile.getName() + " already exists - deleting it");
			boolean success = tempFile.delete();
			if (!success) {
				Logger.warning("Failed to delete file" + tempFile.getName());
			}
		}
	}

	/**
	 * Check if the settings allow a change of the JPEG.
	 *
	 * @return true if it is allowed to change image files.
	 */
	public static boolean changeJpegAllowed() {
		int storeOption = PreferenceUtil.getPreferenceInt(PreferenceUtil.KEY_STORE_OPTION);
		return storeOption > 0;
	}

	/**
	 * Check if the settings allow a change of the EXIF data.
	 *
	 * @return true if it is allowed to change EXIF data.
	 */
	private static boolean changeExifAllowed() {
		int storeOption = PreferenceUtil.getPreferenceInt(PreferenceUtil.KEY_STORE_OPTION);
		return storeOption == 2;
	}

}
