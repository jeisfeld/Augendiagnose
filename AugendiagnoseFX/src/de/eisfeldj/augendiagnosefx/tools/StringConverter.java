package de.eisfeldj.augendiagnosefx.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Tool to convert Strings from Android app to Windows application.
 */
public final class StringConverter {
	/**
	 * The global Strings file.
	 */
	private static final File XML_FILE_GLOBAL = new File("../Augendiagnose/res/values/strings.xml");

	/**
	 * The German String file.
	 */
	private static final File XML_FILE_DE = new File("../Augendiagnose/res/values-de/strings.xml");

	/**
	 * The Spanish String file.
	 */
	private static final File XML_FILE_ES = new File("../Augendiagnose/res/values-es/strings.xml");

	/**
	 * The global Properties file.
	 */
	private static final File PROP_FILE_GLOBAL = new File("resources/bundles/Strings.properties");

	/**
	 * The German Properties file.
	 */
	private static final File PROP_FILE_DE = new File("resources/bundles/Strings_de.properties");

	/**
	 * The Spanish Properties file.
	 */
	private static final File PROP_FILE_ES = new File("resources/bundles/Strings_es.properties");

	/**
	 * The class containing resource constants.
	 */
	private static final File RESOURCE_CLASS = new File("src/de/eisfeldj/augendiagnosefx/util/ResourceConstants.java");

	/**
	 * Suffix for file while writing.
	 */
	private static final String FILE_SUFFIX = ".tmp";

	/**
	 * An instance of this class.
	 */
	private static StringConverter instance;

	/**
	 * The SAX parser factory used.
	 */
	private SAXParserFactory factory = SAXParserFactory.newInstance();

	/**
	 * Private constructor.
	 */
	private StringConverter() {
		// do nothing.
	}

	/**
	 * Main method.
	 *
	 * @param args
	 *            The command line arguments.
	 */
	public static void main(final String[] args) {
		instance = new StringConverter();

		instance.process();
	}

	/**
	 * Process the resource files and update valus from XML files, as well as ResourceConstants.
	 */
	private void process() {
		File tmpPropFile = new File(PROP_FILE_GLOBAL.getPath() + FILE_SUFFIX);
		File tmpPropFileDe = new File(PROP_FILE_DE.getPath() + FILE_SUFFIX);
		File tmpPropFileEs = new File(PROP_FILE_ES.getPath() + FILE_SUFFIX);

		PROP_FILE_GLOBAL.renameTo(tmpPropFile);
		PROP_FILE_DE.renameTo(tmpPropFileDe);
		PROP_FILE_ES.renameTo(tmpPropFileEs);

		try (FileReader reader = new FileReader(tmpPropFile);
				FileWriter writer = new FileWriter(PROP_FILE_GLOBAL);
				FileReader readerDe = new FileReader(tmpPropFileDe);
				FileWriter writerDe = new FileWriter(PROP_FILE_DE);
				FileReader readerEs = new FileReader(tmpPropFileEs);
				FileWriter writerEs = new FileWriter(PROP_FILE_ES)) {

			AlphabeticProperties props = new AlphabeticProperties();
			props.load(reader);

			AlphabeticProperties propsDe = new AlphabeticProperties();
			propsDe.load(readerDe);

			AlphabeticProperties propsEs = new AlphabeticProperties();
			propsEs.load(readerEs);

			Enumeration<?> e = props.propertyNames();

			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String xmlValue = getValueFromXml(XML_FILE_GLOBAL, key);
				if (xmlValue != null) {
					props.setProperty(key, xmlValue);
				}
				String xmlValueDe = getValueFromXml(XML_FILE_DE, key);
				if (xmlValueDe != null) {
					propsDe.setProperty(key, xmlValueDe);
				}
				String xmlValueEs = getValueFromXml(XML_FILE_ES, key);
				if (xmlValueEs != null) {
					propsEs.setProperty(key, xmlValueEs);
				}
			}

			props.store(writer, "String resources");
			propsDe.store(writerDe, "String resources DE");
			propsEs.store(writerEs, "String resources ES");

			createResourceConstants(props);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		tmpPropFile.delete();
		tmpPropFileDe.delete();
		tmpPropFileEs.delete();
	}

	/**
	 * Create the resource constants class.
	 *
	 * @param props
	 *            The properties from which to create resource constants.
	 */
	private void createResourceConstants(final Properties props) {
		String header = "package de.eisfeldj.augendiagnosefx.util;\n\n"
				+ "/**\n"
				+ " * Constants for resource strings.\n"
				+ " */\n"
				+ "public final class ResourceConstants {\n\n"
				+ "	/**\n"
				+ "	 * Private default constructor.\n"
				+ "	 */\n"
				+ "	private ResourceConstants() {\n"
				+ "		// do nothing.\n"
				+ "	}\n\n"
				+ "	// JAVADOC:OFF\n";
		String footer = "	// JAVADOC:ON\n}\n";

		File tmpResourceClass = new File(RESOURCE_CLASS.getAbsolutePath() + FILE_SUFFIX);
		RESOURCE_CLASS.renameTo(tmpResourceClass);

		try (PrintWriter writer = new PrintWriter(RESOURCE_CLASS)) {
			writer.print(header);

			Enumeration<?> e = props.keys();

			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();

				writer.println("	public static final String " + key.toUpperCase() + " = \"" + key + "\";");
			}

			writer.print(footer);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		tmpResourceClass.delete();
	}

	/**
	 * Get a String value from an android String file.
	 *
	 * @param file
	 *            The file.
	 * @param key
	 *            The key.
	 * @return The String value.
	 */
	private String getValueFromXml(final File file, final String key) {
		String result = null;

		SAXParser parser;
		try {
			parser = factory.newSAXParser();
			SAXHandler handler = new SAXHandler(key);
			parser.parse(file, handler);
			result = handler.getValue();
		}
		catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * SAX handler to parse the strings.xml file.
	 */
	private static class SAXHandler extends DefaultHandler {
		/**
		 * Storage for the resource key.
		 */
		private String key = null;
		/**
		 * Storage for the resource value.
		 */
		private String value = null;

		/**
		 * The String to be looked for.
		 */
		private String searchKey = null;

		/**
		 * Constructor passing a search key.
		 *
		 * @param searchKey
		 *            The String to be looked for.
		 */
		public SAXHandler(final String searchKey) {
			this.searchKey = searchKey;
		}

		@Override
		public void startElement(final String uri, final String localName, final String qName,
				final Attributes attributes) throws SAXException {
			if (qName.equals("string")) {
				key = attributes.getValue("name");
			}
		}

		@Override
		public void characters(final char[] ch, final int start, final int length) throws SAXException {
			if (key != null && key.equals(searchKey)) {
				value = new String(ch, start, length);
			}
		}

		@Override
		public void endElement(final String uri, final String localName,
				final String qName) throws SAXException {
			if (qName.equals("string")) {
				key = null;
			}
		}

		/**
		 * Getter for the found value.
		 *
		 * @return The found value.
		 */
		public String getValue() {
			return value;
		}
	}

	/**
	 * Variant of properties that stores values in alphabetical order.
	 */
	private static class AlphabeticProperties extends Properties {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public synchronized Enumeration<Object> keys() {
			return Collections.enumeration(new TreeSet<Object>(super.keySet()));
		}
	}
}
