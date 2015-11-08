package de.eisfeldj.augendiagnosefx.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;

import de.eisfeldj.augendiagnosefx.Application;

/**
 * Utility class for interaction with operating system.
 */
public final class SystemUtil {
	/**
	 * Waiting time in seconds before it is assumed that application has been successfully closed.
	 */
	private static final int WAITING_TIME = 1;

	/**
	 * Quote string required in cmd shell.
	 */
	private static final String QUOTE = "\"";

	/**
	 * Hide default constructor.
	 */
	private SystemUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Find out if application is running on 64bit java.
	 *
	 * @return true if 64bit
	 */
	public static boolean is64Bit() {
		return "64".equals(System.getProperty("sun.arch.data.model"));
	}

	/**
	 * Get the path of the jar file.
	 *
	 * @return The path of the jar file.
	 */
	public static File getJarPath() {
		try {
			return new File(Application.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		}
		catch (URISyntaxException e) {
			return null;
		}
	}

	/**
	 * Get the application temporary directory.
	 *
	 * @return The temporary directory.
	 */
	public static File getTempDir() {
		return new File(new File(System.getProperty("java.io.tmpdir")), Application.APPLICATION_NAME);
	}

	/**
	 * Get the path of the JVM.
	 *
	 * @return The path of the JVM.
	 */
	public static String getJavaExecutable() {
		File javaHome = new File(System.getProperties().getProperty("java.home"));

		// first look for java executable
		File binaryDir = new File(javaHome, "bin");
		if (binaryDir.exists() && binaryDir.isDirectory()) {
			File executable;
			if (System.getProperty("os.name").startsWith("Win")) {
				executable = new File(binaryDir, "java.exe");
			}
			else {
				executable = new File(binaryDir, "java");
			}
			if (executable.exists()) {
				return executable.getAbsolutePath();
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}

	/**
	 * Get the path of the Application executable.
	 *
	 * @return The path of the application executable
	 */
	public static String getApplicationExecutable() {
		File javaHome = new File(System.getProperties().getProperty("java.home"));

		File applicationDir1 = javaHome.getParentFile();
		File applicationDir2 = applicationDir1.getParentFile();

		File[] files = applicationDir1.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				return name.startsWith(Application.APPLICATION_NAME) && name.endsWith(".exe");
			}
		});

		if (files != null && files.length > 0) {
			return files[0].getAbsolutePath();
		}
		else {
			files = applicationDir2.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(final File dir, final String name) {
					return name.startsWith(Application.APPLICATION_NAME) && name.endsWith(".exe");
				}
			});

			if (files != null && files.length > 0) {
				return files[0].getAbsolutePath();
			}
			else {
				return null;
			}
		}
	}

	/**
	 * Get the current classpath.
	 *
	 * @return The classpath.
	 */
	public static String getClasspath() {
		return System.getProperty("java.class.path");
	}

	/**
	 * Run multiple Windows commands in one shell after a short waiting time.
	 *
	 * @param waitingTime
	 *            The waiting time (in seconds)
	 * @param commands
	 *            The Windows commands.
	 */
	public static void runMultipleWindowsCommands(final int waitingTime, final String... commands) {
		StringBuilder command = new StringBuilder();

		// Waiting shortly.
		command.append("ping -n " + (waitingTime + 1) + " 127.0.0.1 > NUL");

		if (commands.length > 0) {
			for (int i = 0; i < commands.length; i++) {
				command.append(" && ");
				command.append(commands[i]);
			}
		}

		ProcessBuilder processBuilder =
				new ProcessBuilder("cmd", "/c", command.toString());

		try {
			processBuilder.start();
			Logger.info("Started application process " + command.toString());
		}
		catch (IOException e) {
			Logger.error("Could not start application process " + command.toString());
		}
	}

	/**
	 * Run multiple Unix commands in one shell after a short waiting time.
	 *
	 * @param waitingTime
	 *            The waiting time (in seconds)
	 * @param commands
	 *            The Unix commands.
	 */
	public static void runMultipleUnixCommands(final int waitingTime, final String... commands) {
		StringBuilder command = new StringBuilder();

		// Waiting shortly.
		command.append("sleep " + waitingTime);

		if (commands.length > 0) {
			for (int i = 0; i < commands.length; i++) {
				command.append("; ");
				command.append(commands[i]);
			}
		}

		ProcessBuilder processBuilder = new ProcessBuilder(command.toString());

		try {
			processBuilder.start();
			Logger.info("Started application process " + command.toString());
		}
		catch (IOException e) {
			Logger.error("Could not start application process " + command.toString());
		}
	}

	/**
	 * Move the jar file via Windows command, and then restart the application.
	 *
	 * @param sourcePath
	 *            The source file
	 * @param targetPath
	 *            The target file
	 */
	public static void updateApplication(final String sourcePath, final String targetPath) {
		String javaExecutable = getJavaExecutable();
		String applicationExecutable = getApplicationExecutable();

		if (javaExecutable != null) {
			String command = QUOTE + javaExecutable + QUOTE + " -classpath \"" + getClasspath() + "\" -Xmx1024m "
					+ Application.class.getCanonicalName();
			if (System.getProperty("os.name").startsWith("Win")) {
				runMultipleWindowsCommands(WAITING_TIME,
						"move /Y " + QUOTE + sourcePath + QUOTE + ' ' + QUOTE + targetPath + QUOTE,
						command);
			}
			else {
				runMultipleUnixCommands(WAITING_TIME,
						"mv " + QUOTE + sourcePath + QUOTE + ' ' + QUOTE + targetPath + QUOTE,
						command);
			}
		}
		else if (applicationExecutable != null) {
			runMultipleWindowsCommands(WAITING_TIME,
					"move /Y " + QUOTE + sourcePath + QUOTE + ' ' + QUOTE + targetPath + QUOTE,
					QUOTE + applicationExecutable + QUOTE);
		}
		else {
			Logger.error("Did not find executable.");
			return;
		}

	}

	/**
	 * Restart the application.
	 */
	public static void restartApplication() {
		String javaExecutable = getJavaExecutable();
		String applicationExecutable = getApplicationExecutable();

		if (javaExecutable != null) {
			String command = QUOTE + javaExecutable + QUOTE + " -classpath \"" + getClasspath() + "\" -Xmx1024m "
					+ Application.class.getCanonicalName();
			if (System.getProperty("os.name").startsWith("Win")) {
				runMultipleWindowsCommands(0, command);
			}
			else {
				runMultipleUnixCommands(0, command);
			}
		}
		else if (applicationExecutable != null) {
			runMultipleWindowsCommands(0, QUOTE + applicationExecutable + QUOTE);
		}
		else {
			Logger.error("Did not find executable.");
			return;
		}

	}

}
