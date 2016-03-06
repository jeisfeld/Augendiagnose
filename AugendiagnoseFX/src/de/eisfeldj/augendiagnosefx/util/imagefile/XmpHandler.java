package de.eisfeldj.augendiagnosefx.util.imagefile;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.adobe.xmp.XMPDateTime;
import com.adobe.xmp.XMPDateTimeFactory;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.XMPPathFactory;
import com.adobe.xmp.XMPSchemaRegistry;
import com.adobe.xmp.options.PropertyOptions;

import de.eisfeldj.augendiagnosefx.util.Logger;

/**
 * Helper class to handle XML data in a JPEG file.
 */
public class XmpHandler {
	// JAVADOC:OFF
	private static final String USER_COMMENT = "UserComment";

	// Standard namespaces
	private static final String NS_DC = "http://purl.org/dc/elements/1.1/";
	private static final String NS_MP1 = "http://ns.microsoft.com/photo/1.0/";
	private static final String NS_MP2 = "http://ns.microsoft.com/photo/1.2/";
	private static final String NS_MPRI = "http://ns.microsoft.com/photo/1.2/t/RegionInfo#";
	private static final String NS_MPREG = "http://ns.microsoft.com/photo/1.2/t/Region#";
	private static final String NS_EXIF = "http://ns.adobe.com/exif/1.0/";

	// The custom namespace
	private static final String NS_JE = "http://ns.jeisfeld.de/augenfotos/1.0/";

	// Items from the custom namespace
	public static final String ITEM_TITLE = "title";
	public static final String ITEM_DESCRIPTION = "description";
	public static final String ITEM_SUBJECT = "subject";
	public static final String ITEM_COMMENT = "comment";
	public static final String ITEM_PERSON = "person";
	public static final String ITEM_X_CENTER = "xCenter";
	public static final String ITEM_Y_CENTER = "yCenter";
	public static final String ITEM_OVERLAY_SCALE_FACTOR = "overlayScaleFactor";
	public static final String ITEM_X_POSITION = "xPosition";
	public static final String ITEM_Y_POSITION = "yPosition";
	public static final String ITEM_ZOOM_FACTOR = "zoomFactor";
	public static final String ITEM_ORGANIZE_DATE = "organizeDate";
	public static final String ITEM_RIGHT_LEFT = "rightLeft";
	public static final String ITEM_BRIGHTNESS = "brightness";
	public static final String ITEM_CONTRAST = "contrast";
	public static final String ITEM_SATURATION = "saturation";
	public static final String ITEM_COLOR_TEMPERATURE = "colorTemperature";
	public static final String ITEM_PUPIL_SIZE = "pupilSize";
	public static final String ITEM_PUPIL_X_OFFSET = "pupilXOffset";
	public static final String ITEM_PUPIL_Y_OFFSET = "pupilYOffset";
	public static final String ITEM_OVERLAY_COLOR = "overlayColor";
	public static final String ITEM_FLAGS = "flags";

	// JAVADOC:ON

	/**
	 * Store if the registry is prepared via prepareRegistry.
	 */
	private static boolean mIsPrepared = false;

	/**
	 * The XMP JpegMetadata stored in the handler.
	 */
	private XMPMeta mXmpMeta;

	/**
	 * Create an XmpHandler from an XMP String.
	 *
	 * @param xmpString
	 *            the XMP String.
	 */
	public XmpHandler(final String xmpString) {
		prepareRegistry();

		if (xmpString == null) {
			Logger.warning("xmpString is null");
			mXmpMeta = XMPMetaFactory.create();
			return;
		}

		try {
			String updatedXmpString = xmpString.trim();
			int i = updatedXmpString.lastIndexOf('<');
			if (i > 0 && updatedXmpString.substring(i).startsWith("<?xpacket end")) {
				updatedXmpString = updatedXmpString.substring(0, i);
			}
			mXmpMeta = XMPMetaFactory.parseFromString(updatedXmpString);
		}
		catch (Exception e) {
			Logger.warning("Error when parsing XMP Data: " + e.toString());
			mXmpMeta = XMPMetaFactory.create();
		}
	}

	/**
	 * Add namespaces to the registry.
	 */
	private static void prepareRegistry() {
		if (!mIsPrepared) {
			XMPSchemaRegistry registry = XMPMetaFactory.getSchemaRegistry();
			try {
				registry.registerNamespace(NS_JE, "je:");
				registry.registerNamespace(NS_MP1, "MicrosoftPhoto:");
				registry.registerNamespace(NS_MP2, "MP:");
				registry.registerNamespace(NS_MPRI, "MPRI:");
				registry.registerNamespace(NS_MPREG, "MPReg:");
				registry.registerNamespace(NS_DC, "dc:");
				registry.registerNamespace(NS_EXIF, "exif:");
				mIsPrepared = true;
			}
			catch (XMPException e) {
				Logger.error("Exception while preparing XMP registry", e);
			}
		}
	}

	/**
	 * Get an item from the custom namespace.
	 *
	 * @param item
	 *            the name of the item.
	 * @return the value of the item.
	 */
	public final String getJeItem(final String item) {
		try {
			return mXmpMeta.getPropertyString(NS_JE, item);
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get an int item from the custom namespace.
	 *
	 * @param item
	 *            the name of the item.
	 * @return the value of the item.
	 */
	public final int getJeInt(final String item) {
		try {
			return mXmpMeta.getPropertyInteger(NS_JE, item);
		}
		catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Get a date item from the custom namespace.
	 *
	 * @param item
	 *            the name of the item.
	 * @return the value of the item.
	 */
	public final Date getJeDate(final String item) {
		try {
			XMPDateTime dateTime = mXmpMeta.getPropertyDate(NS_JE, item);
			return dateTime.getCalendar().getTime();
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get an item from the DC namespace.
	 *
	 * @param item
	 *            the name of the item.
	 * @return the value of the item.
	 */
	private String getDcItem(final String item) {
		try {
			return mXmpMeta.getArrayItem(NS_DC, item, 1).getValue();
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Retrieve the image title.
	 *
	 * @return the image title.
	 */
	public final String getDcTitle() {
		return getDcItem("title");
	}

	/**
	 * Retrieve the image description.
	 *
	 * @return the image description.
	 */
	public final String getDcDescription() {
		return getDcItem("description");
	}

	/**
	 * Retrieve the image subject.
	 *
	 * @return the image subject.
	 */
	public final String getDcSubject() {
		return getDcItem("subject");
	}

	/**
	 * Retrieve the user comment.
	 *
	 * @return the user comment.
	 */
	public final String getUserComment() {
		try {
			return mXmpMeta.getArrayItem(NS_EXIF, USER_COMMENT, 1).getValue();
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Retrieve the image person name, from Microsoft namespace^.
	 *
	 * @return the image person name.
	 */
	public final String getMicrosoftPerson() {
		try {
			String path = "RegionInfo"
					+ XMPPathFactory.composeArrayItemPath(XMPPathFactory.composeStructFieldPath(NS_MPRI, "Regions"), 1)
					+ XMPPathFactory.composeStructFieldPath(NS_MPREG, "PersonDisplayName");
			// String path = "RegionInfo"
			// + XMPPathFactory.composeArrayItemPath(XMPPathFactory.composeStructFieldPath(NS_MPRI, "Regions"), 1);

			return mXmpMeta.getPropertyString(NS_MP2, path);
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Dump the complete XMP object.
	 *
	 * @return a dump of the XMP object.
	 */
	public final String dumpObject() {
		return mXmpMeta.dumpObject();
	}

	/**
	 * Set an entry in the custom namespace.
	 *
	 * @param item
	 *            the name of the entry.
	 * @param value
	 *            the value of the entry.
	 * @throws XMPException
	 *             thrown in case of issues with XML handling.
	 */
	public final void setJeItem(final String item, final String value) throws XMPException {
		if (value != null) {
			mXmpMeta.setProperty(NS_JE, item, value);
		}
		else {
			removeJeItem(item);
		}
	}

	/**
	 * Set an int entry in the custom namespace.
	 *
	 * @param item
	 *            the name of the entry.
	 * @param value
	 *            the value of the entry.
	 * @throws XMPException
	 *             thrown in case of issues with XMP handling.
	 */
	public final void setJeInt(final String item, final int value) throws XMPException {
		mXmpMeta.setProperty(NS_JE, item, value);
	}

	/**
	 * Set a date entry in the custom namespace.
	 *
	 * @param item
	 *            the name of the entry.
	 * @param date
	 *            the value of the entry.
	 * @throws XMPException
	 *             thrown in case of issues with XML handling.
	 */
	public final void setJeDate(final String item, final Date date) throws XMPException {
		if (date != null) {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			XMPDateTime xmpDate = XMPDateTimeFactory.createFromCalendar(calendar);
			mXmpMeta.setPropertyDate(NS_JE, item, xmpDate);
		}
	}

	/**
	 * Delete an entry from the custom namespace.
	 *
	 * @param item
	 *            the name of the entry.
	 * @throws XMPException
	 *             thrown in case of issues with XML handling.
	 */
	public final void removeJeItem(final String item) throws XMPException {
		mXmpMeta.deleteProperty(NS_JE, item);
	}

	/**
	 * Set an entry in the DC namespace.
	 *
	 * @param item
	 *            the name of the entry.
	 * @param value
	 *            the value of the entry.
	 * @throws XMPException
	 *             thrown in case of issues with XML handling.
	 */
	private void setDcItem(final String item, final String value) throws XMPException {
		if (value != null) {
			if (mXmpMeta.doesArrayItemExist(NS_DC, item, 1)) {
				mXmpMeta.setArrayItem(NS_DC, item, 1, value);
			}
			else {
				mXmpMeta.appendArrayItem(NS_DC, item, new PropertyOptions().setArray(true), value, null);
			}
		}
	}

	/**
	 * Set the image title.
	 *
	 * @param title
	 *            the image title.
	 * @throws XMPException
	 *             thrown in case of issues with XML handling.
	 */
	public final void setDcTitle(final String title) throws XMPException {
		setDcItem("title", title);
	}

	/**
	 * Set the image description.
	 *
	 * @param description
	 *            the image description.
	 * @throws XMPException
	 *             thrown in case of issues with XML handling.
	 */
	public final void setDcDescription(final String description) throws XMPException {
		setDcItem("description", description);
	}

	/**
	 * Set the image subject.
	 *
	 * @param subject
	 *            the image subject.
	 * @throws XMPException
	 *             thrown in case of issues with XML handling.
	 */
	public final void setDcSubject(final String subject) throws XMPException {
		setDcItem("subject", subject);
	}

	/**
	 * Set the User Comment.
	 *
	 * @param userComment
	 *            the user comment.
	 * @throws XMPException
	 *             thrown in case of issues with XML handling.
	 */
	public final void setUserComment(final String userComment) throws XMPException {
		if (userComment != null) {
			if (mXmpMeta.doesArrayItemExist(NS_EXIF, USER_COMMENT, 1)) {
				mXmpMeta.setArrayItem(NS_EXIF, USER_COMMENT, 1, userComment);
			}
			else {
				mXmpMeta.appendArrayItem(NS_EXIF, USER_COMMENT, new PropertyOptions().setArray(true), userComment, null);
			}
		}
	}

	/**
	 * Set the image person name.
	 *
	 * @param name
	 *            the image person name.
	 * @throws XMPException
	 *             thrown in case of issues with XML handling.
	 */
	public final void setMicrosoftPerson(final String name) throws XMPException {
		if (name != null) {
			String path = "RegionInfo"
					+ XMPPathFactory.composeArrayItemPath(XMPPathFactory.composeStructFieldPath(NS_MPRI, "Regions"), 1)
					+ XMPPathFactory.composeStructFieldPath(NS_MPREG, "PersonDisplayName");
			String path1 = "RegionInfo" + XMPPathFactory.composeStructFieldPath(NS_MPRI, "Regions");

			if (!mXmpMeta.doesArrayItemExist(NS_MP2, path1, 1)) {
				mXmpMeta.appendArrayItem(NS_MP2, path1, new PropertyOptions().setArray(true), null,
						new PropertyOptions().setStruct(true));
			}
			mXmpMeta.setProperty(NS_MP2, path, name);
		}
	}

	/**
	 * Get the XMP String.
	 *
	 * @return the XMP String.
	 * @throws XMPException
	 *             thrown in case of issues with XML handling.
	 */
	public final String getXmpString() throws XMPException {
		return XMPMetaFactory.serializeToString(mXmpMeta, null);
	}

}
