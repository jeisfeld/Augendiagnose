package de.eisfeldj.augendiagnosefx.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility class to copy HTML from web page into Android project resources.
 */
public final class HtmlStringCreator {
	/**
	 * The Android resource file name.
	 */
	private static final String RESOURCE_FILE_NAME = "strings_html.xml";

	/**
	 * A map from the HTML language path names to the Anrdoid language path names.
	 */
	private static final Map<String, String> LANGUAGE_MAP = new TreeMap<>();

	/**
	 * A map from the HTML file names to the Android string resource names.
	 */
	private static final Map<String, String> PAGE_MAP = new TreeMap<>();

	/**
	 * A map from app name to the URL of the web page.
	 */
	private static final Map<String, String> APP_URL_MAP = new TreeMap<>();

	/**
	 * A map from app name to the resource folder of the Android app.
	 */
	private static final Map<String, File> APP_RESOURCE_FOLDER_MAP = new TreeMap<>();

	/**
	 * The name of the Mininris app.
	 */
	private static final String MINIRIS = "Miniris";

	/**
	 * The base name of the Augendiagnose app.
	 */
	private static final String AUGENDIAGNOSE = "Augendiagnose";

	/**
	 * Abbreviation of language English.
	 */
	private static final String EN = "en";
	/**
	 * Abbreviation of language German.
	 */
	private static final String DE = "de";
	/**
	 * Abbreviation of language Spanish.
	 */
	private static final String ES = "es";

	/**
	 * Abbreviation of language Portuguese.
	 */
	private static final String PT = "pt";

	static {
		LANGUAGE_MAP.put(EN, "values");
		LANGUAGE_MAP.put(DE, "values-de");
		LANGUAGE_MAP.put(ES, "values-es");
		LANGUAGE_MAP.put(PT, "values-pt");

		PAGE_MAP.put("overview.php", "html_overview");
		PAGE_MAP.put("settings.php", "html_settings");
		PAGE_MAP.put("organize_photos.php", "html_organize_photos");
		PAGE_MAP.put("display_photos.php", "html_display_photos");

		APP_URL_MAP.put(AUGENDIAGNOSE, "http://localhost:8002");
		APP_URL_MAP.put(MINIRIS, "http://localhost:8007");

		APP_RESOURCE_FOLDER_MAP.put(AUGENDIAGNOSE, new File("../AugendiagnoseIdea/augendiagnose/src/main/res"));
		APP_RESOURCE_FOLDER_MAP.put(MINIRIS, new File("../AugendiagnoseIdea/miniris/src/main/res"));
	}

	/**
	 * An instance of this class.
	 */
	private static HtmlStringCreator mInstance;

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
	 * @throws IOException
	 *             thrown if there any read/write issues.
	 */
	public static void main(final String[] args) throws IOException {
		mInstance = new HtmlStringCreator();

		for (String app : APP_URL_MAP.keySet()) {
			for (String language : LANGUAGE_MAP.keySet()) {
				mInstance.updateResourceFile(app, language);
			}
		}
	}

	/**
	 * Update the HTML resource file for a given app and a given language.
	 *
	 * @param app
	 *            the app.
	 * @param language
	 *            The language.
	 * @throws IOException
	 *             thrown if there are issues writing the file.
	 */
	private void updateResourceFile(final String app, final String language) throws IOException {
		String resourceFileEncoding = "UTF-8";
		File resourceFile = new File(new File(APP_RESOURCE_FOLDER_MAP.get(app), LANGUAGE_MAP.get(language)), RESOURCE_FILE_NAME);
		String resourceFileContent = readFile(resourceFile, resourceFileEncoding);

		for (String htmlFile : PAGE_MAP.keySet()) {
			String htmlFileContent = readHtml(language, app, htmlFile);
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
	 * @param app
	 *            the app,
	 * @param fileName
	 *            The name of the page.
	 * @return The content of the page.
	 * @throws IOException
	 *             thrown if there are issues reading the file.
	 */
	private String readHtml(final String language, final String app, final String fileName) throws IOException {
		URL oracle = getUrl(app, language, fileName);
		BufferedReader in = new BufferedReader(
				new InputStreamReader(oracle.openStream()));

		String inputLine;
		StringBuffer htmlBuffer = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			htmlBuffer.append(inputLine).append("\n");
		}
		in.close();
		String htmlContent = htmlBuffer.toString();
		return htmlContent;
	}

	/**
	 * Get the URL from where the HTML can be retrieved.
	 *
	 * @param app
	 *            The application name.
	 * @param language
	 *            The language.
	 * @param fileName
	 *            The file name.
	 * @return The URL.
	 */
	private URL getUrl(final String app, final String language, final String fileName) {
		try {
			return new URL(APP_URL_MAP.get(app) + "/" + language + "/" + fileName + "?createHtmlString=true");
		}
		catch (MalformedURLException e) {
			return null;
		}
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
	 *             thrown if there are issues reading the file.
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
	 *             thrown if there are issues writing the file.
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
