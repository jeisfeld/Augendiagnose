package de.eisfeldj.augendiagnosefx.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility class to copy HTML from web page into Android project resources.
 */
public final class HtmlStringCreator {
	/**
	 * The base path of web pages of the Autendiagnose page.
	 */
	private static final File WEB_BASE_FOLDER = new File("../../Webdevelopment/augendiagnose/web");

	/**
	 * The base path of Android resources.
	 */
	private static final File ANDROID_BASE_FOLDER = new File("../Augendiagnose/res");

	/**
	 * The Android resource file name.
	 */
	private static final String RESOURCE_FILE_NAME = "strings_html.xml";

	/**
	 * A map from the HTML language path names to the Anrdoid language path names.
	 */
	private static final Map<String, String> LANGUAGE_MAP = new TreeMap<String, String>();

	/**
	 * A map from the HTML file names to the Android string resource names.
	 */
	private static final Map<String, String> PAGE_MAP = new TreeMap<String, String>();

	static {
		LANGUAGE_MAP.put("en", "values");
		LANGUAGE_MAP.put("de", "values-de");
		LANGUAGE_MAP.put("es", "values-es");

		PAGE_MAP.put("overview.html", "html_overview");
		PAGE_MAP.put("settings.html", "html_settings");
		PAGE_MAP.put("organize_photos.html", "html_organize_photos");
		PAGE_MAP.put("display_photos.html", "html_display_photos");
	}

	/**
	 * An instance of this class.
	 */
	private static HtmlStringCreator instance;

	/**
	 * Private constructor.
	 */
	private HtmlStringCreator() {
		// do nothing.
	}

	/**
	 * Main method.
	 *
	 * @param args
	 *            The command line arguments.
	 */
	public static void main(final String[] args) throws IOException {
		instance = new HtmlStringCreator();

		for (String language : LANGUAGE_MAP.keySet()) {
			instance.updateResourceFile(language);
		}
	}

	/**
	 * Update the HTML resource file for a given language.
	 *
	 * @param language
	 *            The language.
	 * @throws IOException
	 */
	private void updateResourceFile(final String language) throws IOException {
		String resourceFileEncoding = "UTF-8";
		File resourceFile = new File(new File(ANDROID_BASE_FOLDER, LANGUAGE_MAP.get(language)), RESOURCE_FILE_NAME);
		String resourceFileContent = readFile(resourceFile, resourceFileEncoding);

		for (String htmlFile : PAGE_MAP.keySet()) {
			String htmlFileContent = readHtmlFile(language, htmlFile);
			resourceFileContent = replaceStringResource(resourceFileContent, PAGE_MAP.get(htmlFile), htmlFileContent);
		}

		writeFile(resourceFile, resourceFileContent, resourceFileEncoding);
	}

	/**
	 * Replace one resource value in a resource file. This method relies stongly on the formatting of the resource file!
	 *
	 * @param resourceString
	 *            The content of the resource file
	 * @param resourceKey
	 *            The String resource to be replaced.
	 * @param resourceValue
	 *            The new value of this resource.
	 * @return The resource file content with replaced value
	 */
	private String replaceStringResource(final String resourceString, final String resourceKey,
			final String resourceValue) {
		String beginString = "<string name=\"" + resourceKey + "\" formatted=\"false\">\n<![CDATA[\n";
		String endString = "]]>\n    </string>";
		int indexStart = resourceString.indexOf(beginString);
		if (indexStart < 0) {
			return resourceString;
		}
		String beforeString = resourceString.substring(0, indexStart + beginString.length());
		String rest = resourceString.substring(indexStart + beginString.length());

		int indexEnd = rest.indexOf(endString);
		rest = rest.substring(indexEnd);

		return beforeString + resourceValue + rest;
	}

	/**
	 * Read the contents of one of the web pages.
	 *
	 * @param language
	 *            The language.
	 * @param fileName
	 *            The name of the page.
	 * @return The content of the page.
	 * @throws IOException
	 */
	private String readHtmlFile(final String language, final String fileName) throws IOException {
		File file = new File(new File(WEB_BASE_FOLDER, language), fileName);
		return readFile(file, "ISO8859-1");
	}

	/**
	 * Read the contents of a file into a String.
	 *
	 * @param file
	 *            The file
	 * @param encoding
	 *            The encoding to be used.
	 * @return The contents of the file.
	 * @throws IOException
	 */
	private String readFile(final File file, final String encoding) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
		try {
			StringBuilder sb = new StringBuilder();
			String line = reader.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = reader.readLine();
			}
			return sb.toString();
		}
		finally {
			reader.close();
		}
	}

	/**
	 * Write a String to a file (overwriting the file).
	 *
	 * @param file
	 *            The file.
	 * @param content
	 *            The String to be written.
	 * @param encoding
	 *            The encoding to be used.
	 * @throws IOException
	 */
	private void writeFile(final File file, final String content, final String encoding) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
		try {
			writer.write(content);
		}
		finally {
			writer.close();
		}
	}

}
