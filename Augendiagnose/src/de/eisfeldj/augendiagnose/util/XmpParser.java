package de.eisfeldj.augendiagnose.util;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.XMPPathFactory;
import com.adobe.xmp.XMPSchemaRegistry;
import com.adobe.xmp.options.PropertyOptions;

public class XmpParser {
	private static final String NS_DC = "http://purl.org/dc/elements/1.1/";
	private static final String NS_MP1 = "http://ns.microsoft.com/photo/1.0/";
	private static final String NS_MP2 = "http://ns.microsoft.com/photo/1.2/";
	private static final String NS_MPRI = "http://ns.microsoft.com/photo/1.2/t/RegionInfo#";
	private static final String NS_MPREG = "http://ns.microsoft.com/photo/1.2/t/Region#";
	private static final String NS_EXIF = "http://ns.adobe.com/exif/1.0/";
	private static final String NS_JE = "http://ns.jeisfeld.de/augenfotos/1.0/";
	private static boolean prepared = false;

	private XMPMeta xmpMeta;

	/**
	 * Create an XmpParser from an xmp String
	 * 
	 * @param xmpString
	 */
	public XmpParser(String xmpString) {
		prepareRegistry();
		try {
			xmpMeta = XMPMetaFactory.parseFromString(xmpString);
		}
		catch (Exception e) {
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
	public String getTitle() {
		return getDcItem("title");
	}

	/**
	 * Retrieve the image description
	 * 
	 * @return
	 */
	public String getDescription() {
		return getDcItem("description");
	}

	/**
	 * Retrieve the image subject
	 * 
	 * @return
	 */
	public String getSubject() {
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
	public String getPerson() {
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
	 * Set an entry in the DC namespace
	 * 
	 * @param item
	 * @param value
	 * @throws XMPException
	 */
	private void setDcItem(String item, String value) throws XMPException {
		if(value != null) {
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
	public void setTitle(String title) throws XMPException {
		setDcItem("title", title);
	}

	/**
	 * Set the image description
	 * 
	 * @return
	 * @throws XMPException
	 */
	public void setDescription(String description) throws XMPException {
		setDcItem("description", description);
	}

	/**
	 * Set the image subject
	 * 
	 * @return
	 * @throws XMPException
	 */
	public void setSubject(String subject) throws XMPException {
		setDcItem("subject", subject);
	}

	/**
	 * Set the User Comment
	 * 
	 * @return
	 * @throws XMPException
	 */
	public void setUserComment(String userComment) throws XMPException {
		if(userComment != null) {
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
	public void setPerson(String name) throws XMPException {
		if(name != null) {
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
