package de.eisfeldj.augendiagnose.util;

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
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.MicrosoftTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.imaging.util.IoUtils;

import com.adobe.xmp.XMPException;

public abstract class ImageTaggingUtil {

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
			Logger.log(item.getTiffField().toString());
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
		Logger.log(new XmpParser(xmpString).getXmpString());
	}

	/**
	 * Validate that the file is a JPEG file
	 * 
	 * @param jpegImageFileName
	 * @throws IOException
	 * @throws ImageReadException
	 */
	private static void checkJpeg(String jpegImageFileName) throws IOException, ImageReadException {
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
		XmpParser parser = new XmpParser(xmpString);
		result.description = parser.getDescription();
		result.subject = parser.getSubject();
		result.person = parser.getPerson();
		result.title = parser.getTitle();
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

			String title = tiffImageMetadata.findField(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION).getStringValue();
			String comment = tiffImageMetadata.findField(ExifTagConstants.EXIF_TAG_USER_COMMENT).getStringValue();
			if (title != null) {
				result.title = title;
			}
			if (comment != null) {
				result.comment = comment;
			}
			if (result.subject == null) {
				result.subject = tiffImageMetadata.findField(MicrosoftTagConstants.EXIF_TAG_XPSUBJECT).getStringValue();
			}
		}
		catch (Exception e) {
		}

		return result;
	}

	/**
	 * Change metadata of the image
	 * 
	 * @param jpegImageFileName
	 * @param title
	 * @param description
	 * @param subject
	 * @param comment
	 * @param person
	 * @throws IOException
	 * @throws ImageReadException
	 * @throws ImageWriteException
	 * @throws XMPException
	 */
	public static void changeMetadata(final String jpegImageFileName, Metadata metadata) throws IOException,
			ImageReadException, ImageWriteException, XMPException {
		checkJpeg(jpegImageFileName);
		changeXmpMetadata(jpegImageFileName, metadata);
		changeExifMetadata(jpegImageFileName, metadata);
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
			throw new IOException("tempFile " + tempFileName + " already exists");
		}

		OutputStream os = null;
		boolean canThrow = false;
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

			canThrow = true;
		}
		finally {
			IoUtils.closeQuietly(canThrow, os);
		}

		jpegImageFile.delete();
		tempFile.renameTo(jpegImageFile);

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
			throw new IOException("tempFile " + tempFileName + " already exists");
		}

		OutputStream os = null;
		boolean canThrow = false;
		try {
			final String xmpString = Imaging.getXmpXml(jpegImageFile);

			XmpParser parser = new XmpParser(xmpString);

			parser.setTitle(metadata.title);
			parser.setDescription(metadata.description);
			parser.setSubject(metadata.subject);
			parser.setUserComment(metadata.comment);
			parser.setPerson(metadata.person);

			os = new FileOutputStream(tempFile);
			os = new BufferedOutputStream(os);

			new JpegXmpRewriter().updateXmpXml(jpegImageFile, os, parser.getXmpString());

			canThrow = true;
		}
		finally {
			IoUtils.closeQuietly(canThrow, os);
		}

		jpegImageFile.delete();
		tempFile.renameTo(jpegImageFile);
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

		public Metadata() {

		}

		public Metadata(String title, String description, String subject, String comment, String person) {
			this.title = title;
			this.description = description;
			this.subject = subject;
			this.comment = comment;
			this.person = person;
		}
		
		@Override
		public String toString() {
			StringBuffer str = new StringBuffer();
			str.append("Title: " + title + "\n");
			str.append("Description: " + description + "\n");
			str.append("Subject: " + subject + "\n");
			str.append("Comment: " + comment + "\n");
			str.append("Person: " + person + "\n");
			return str.toString();
		}

	}

}
