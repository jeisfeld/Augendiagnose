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
 * Helper class to handle XML data in a JPEG file
 */
public class XmpHandler {
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
	public static final String ITEM_ORGANIZE_DATE = "organizeDate";
	public static final String ITEM_RIGHT_LEFT = "rightLeft";
	public static final String ITEM_BRIGHTNESS = "brightness";
	public static final String ITEM_CONTRAST = "contrast";

	private static boolean prepared = false;

	private XMPMeta xmpMeta;

	/**
	 * Create an XmpHandler from an xmp String
	 * 
	 * @param xmpString
	 */
	public XmpHandler(String xmpString) {
		prepareRegistry();
		
		if(xmpString == null) {
			Log.w(Application.TAG, "xmpString is null ");
			xmpMeta = XMPMetaFactory.create();
			return;
		}
		
		try {
			xmpString = xmpString.trim();
			int i = xmpString.lastIndexOf("<");
			if(i>0 && xmpString.substring(i).startsWith("<?xpacket end")) {
				xmpString = xmpString.substring(0,i);
			}
			xmpMeta = XMPMetaFactory.parseFromString(xmpString);
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
			}
		}
	}

	/**
	 * Get an item from the custom namespace
	 * 
	 * @param item
	 * @return
	 */
	public String getJeItem(String item) {
		try {
			return xmpMeta.getPropertyString(NS_JE, item);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Get a date item from the custom namespace
	 * 
	 * @param item
	 * @return
	 */
	public Date getJeDate(String item) {
		try {
			XMPDateTime dateTime = xmpMeta.getPropertyDate(NS_JE, item);
			return dateTime.getCalendar().getTime();
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get an item from the DC namespace
	 * 
	 * @param item
	 * @return
	 */
	private String getDcItem(String item) {
		try {
			return xmpMeta.getArrayItem(NS_DC, item, 1).getValue();
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Retrieve the image title
	 * 
	 * @return
	 */
	public String getDcTitle() {
		return getDcItem("title");
	}

	/**
	 * Retrieve the image description
	 * 
	 * @return
	 */
	public String getDcDescription() {
		return getDcItem("description");
	}

	/**
	 * Retrieve the image subject
	 * 
	 * @return
	 */
	public String getDcSubject() {
		return getDcItem("subject");
	}

	/**
	 * Retrieve the user comment
	 * 
	 * @return
	 */
	public String getUserComment() {
		try {
			return xmpMeta.getArrayItem(NS_EXIF, "UserComment", 1).getValue();
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Retrieve the image person name
	 * 
	 * @return
	 */
	public String getMicrosoftPerson() {
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
	 * Dump the complect XMP object
	 * 
	 * @return
	 */
	public String dumpObject() {
		return xmpMeta.dumpObject();
	}

	/**
	 * Set an entry in the custom namespace
	 * 
	 * @param item
	 * @param value
	 * @throws XMPException
	 */
	public void setJeItem(String item, String value) throws XMPException {
		if (value != null) {
			xmpMeta.setProperty(NS_JE, item, value);
		}
		else {
			removeJeItem(item);
		}
	}

	
	/**
	 * Set a date entry in the custom namespace
	 * 
	 * @param item
	 * @param date
	 * @throws XMPException
	 */
	public void setJeDate(String item, Date date) throws XMPException {
		if (date != null) {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			XMPDateTime xmpDate = XMPDateTimeFactory.createFromCalendar(calendar);
			xmpMeta.setPropertyDate(NS_JE, item, xmpDate);
		}
	}
	
	/**
	 * Delete an entry from the custom namespace
	 * 
	 * @param item
	 * @throws XMPException
	 */
	public void removeJeItem(String item) throws XMPException {
		xmpMeta.deleteProperty(NS_JE, item);
	}

	/**
	 * Set an entry in the DC namespace
	 * 
	 * @param item
	 * @param value
	 * @throws XMPException
	 */
	private void setDcItem(String item, String value) throws XMPException {
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
	 * Set the image title
	 * 
	 * @return
	 * @throws XMPException
	 */
	public void setDcTitle(String title) throws XMPException {
		setDcItem("title", title);
	}

	/**
	 * Set the image description
	 * 
	 * @return
	 * @throws XMPException
	 */
	public void setDcDescription(String description) throws XMPException {
		setDcItem("description", description);
	}

	/**
	 * Set the image subject
	 * 
	 * @return
	 * @throws XMPException
	 */
	public void setDcSubject(String subject) throws XMPException {
		setDcItem("subject", subject);
	}

	/**
	 * Set the User Comment
	 * 
	 * @return
	 * @throws XMPException
	 */
	public void setUserComment(String userComment) throws XMPException {
		if (userComment != null) {
			if (xmpMeta.doesArrayItemExist(NS_EXIF, "UserComment", 1)) {
				xmpMeta.setArrayItem(NS_EXIF, "UserComment", 1, userComment);
			}
			else {
				xmpMeta.appendArrayItem(NS_EXIF, "UserComment", new PropertyOptions().setArray(true), userComment, null);
			}
		}
	}

	/**
	 * Set the image person name
	 * 
	 * @return
	 * @throws XMPException
	 */
	public void setMicrosoftPerson(String name) throws XMPException {
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
	 * Get the XMP String
	 * 
	 * @return
	 * @throws XMPException
	 */
	public String getXmpString() throws XMPException {
		return XMPMetaFactory.serializeToString(xmpMeta, null);
	}

}
