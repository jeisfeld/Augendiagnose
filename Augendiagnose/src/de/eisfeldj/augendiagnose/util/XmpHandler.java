package de.eisfeldj.augendiagnose.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.util.Log;

import com.adobe.xmp.XMPDateTime;
import com.adobe.xmp.XMPDateTimeFactory;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.XMPPathFactory;
import com.adobe.xmp.XMPSchemaRegistry;
import com.adobe.xmp.options.PropertyOptions;

import de.eisfeldj.augendiagnose.Application;

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
	public static final String ITEM_OVERLAY_COLOR = "overlayColor";

	// JAVADOC:ON

	/**
	 * Store if the registry is prepared via prepareRegistry.
	 */
	private static boolean prepared = false;

	/**
	 * The XMP JpegMetadata stored in the handler.
	 */
	private XMPMeta xmpMeta;

	/**
	 * Create an XmpHandler from an XMP String.
	 *
	 * @param xmpString
	 *            the XMP String.
	 */
	public XmpHandler(final String xmpString) {
		prepareRegistry();

		if (xmpString == null) {
			Log.w(Application.TAG, "xmpString is null ");
			xmpMeta = XMPMetaFactory.create();
			return;
		}

		try {
			String updatedXmpString = xmpString.trim();
			int i = updatedXmpString.lastIndexOf('<');
			if (i > 0 && updatedXmpString.substring(i).startsWith("<?xpacket end")) {
				updatedXmpString = updatedXmpString.substring(0, i);
			}
			xmpMeta = XMPMetaFactory.parseFromString(updatedXmpString);
		}
		catch (Exception e) {
			Log.w(Application.TAG, "Error when parsing XMP Data ", e);
			xmpMeta = XMPMetaFactory.create();
		}
	}

	/**
	 * Add namespaces to the registry.
	 */
	private static void prepareRegistry() {
		if (!prepared) {
			XMPSchemaRegistry registry = XMPMetaFactory.getSchemaRegistry();
			try {
				registry.registerNamespace(NS_JE, "je:");
				registry.registerNamespace(NS_MP1, "MicrosoftPhoto:");
				registry.registerNamespace(NS_MP2, "MP:");
				registry.registerNamespace(NS_MPRI, "MPRI:");
				registry.registerNamespace(NS_MPREG, "MPReg:");
				registry.registerNamespace(NS_DC, "dc:");
				registry.registerNamespace(NS_EXIF, "exif:");
				prepared = true;
			}
			catch (XMPException e) {
				Log.e(Application.TAG, "Exception while preparing XMP registry", e);
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
			return xmpMeta.getPropertyString(NS_JE, item);
		}
		catch (Exception e) {
			return null;
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
			XMPDateTime dateTime = xmpMeta.getPropertyDate(NS_JE, item);
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
			return xmpMeta.getArrayItem(NS_DC, item, 1).getValue();
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
			return xmpMeta.getArrayItem(NS_EXIF, USER_COMMENT, 1).getValue();
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

			return xmpMeta.getPropertyString(NS_MP2, path);
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
		return xmpMeta.dumpObject();
	}

	/**
	 * Set an entry in the custom namespace.
	 *
	 * @param item
	 *            the name of the entry.
	 * @param value
	 *            the value of the entry.
	 * @throws XMPException
	 */
	public final void setJeItem(final String item, final String value) throws XMPException {
		if (value != null) {
			xmpMeta.setProperty(NS_JE, item, value);
		}
		else {
			removeJeItem(item);
		}
	}

	/**
	 * Set a date entry in the custom namespace.
	 *
	 * @param item
	 *            the name of the entry.
	 * @param date
	 *            the value of the entry.
	 * @throws XMPException
	 */
	public final void setJeDate(final String item, final Date date) throws XMPException {
		if (date != null) {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			XMPDateTime xmpDate = XMPDateTimeFactory.createFromCalendar(calendar);
			xmpMeta.setPropertyDate(NS_JE, item, xmpDate);
		}
	}

	/**
	 * Delete an entry from the custom namespace.
	 *
	 * @param item
	 *            the name of the entry.
	 * @throws XMPException
	 */
	public final void removeJeItem(final String item) throws XMPException {
		xmpMeta.deleteProperty(NS_JE, item);
	}

	/**
	 * Set an entry in the DC namespace.
	 *
	 * @param item
	 *            the name of the entry.
	 * @param value
	 *            the value of the entry.
	 * @throws XMPException
	 */
	private void setDcItem(final String item, final String value) throws XMPException {
		if (value != null) {
			if (xmpMeta.doesArrayItemExist(NS_DC, item, 1)) {
				xmpMeta.setArrayItem(NS_DC, item, 1, value);
			}
			else {
				xmpMeta.appendArrayItem(NS_DC, item, new PropertyOptions().setArray(true), value, null);
			}
		}
	}

	/**
	 * Set the image title.
	 *
	 * @param title
	 *            the image title.
	 * @throws XMPException
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
	 */
	public final void setUserComment(final String userComment) throws XMPException {
		if (userComment != null) {
			if (xmpMeta.doesArrayItemExist(NS_EXIF, USER_COMMENT, 1)) {
				xmpMeta.setArrayItem(NS_EXIF, USER_COMMENT, 1, userComment);
			}
			else {
				xmpMeta.appendArrayItem(NS_EXIF, USER_COMMENT, new PropertyOptions().setArray(true), userComment, null);
			}
		}
	}

	/**
	 * Set the image person name.
	 *
	 * @param name
	 *            the image person name.
	 * @throws XMPException
	 */
	public final void setMicrosoftPerson(final String name) throws XMPException {
		if (name != null) {
			String path = "RegionInfo"
					+ XMPPathFactory.composeArrayItemPath(XMPPathFactory.composeStructFieldPath(NS_MPRI, "Regions"), 1)
					+ XMPPathFactory.composeStructFieldPath(NS_MPREG, "PersonDisplayName");
			String path1 = "RegionInfo" + XMPPathFactory.composeStructFieldPath(NS_MPRI, "Regions");

			if (!xmpMeta.doesArrayItemExist(NS_MP2, path1, 1)) {
				xmpMeta.appendArrayItem(NS_MP2, path1, new PropertyOptions().setArray(true), null,
						new PropertyOptions().setStruct(true));
			}
			xmpMeta.setProperty(NS_MP2, path, name);
		}
	}

	/**
	 * Get the XMP String.
	 *
	 * @return the XMP String.
	 * @throws XMPException
	 */
	public final String getXmpString() throws XMPException {
		return XMPMetaFactory.serializeToString(xmpMeta, null);
	}

}
