package de.eisfeldj.augendiagnosefx.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * Utility class for helping parsing file systems.
 */
public abstract class FileUtil {

	/**
	 * Copy a file.
	 *
	 * @param source
	 *            The source file
	 * @param target
	 *            The target file
	 * @return true if the copying was successful.
	 */
	@SuppressWarnings("null")
	public static boolean copyFile(final File source, final File target) {
		FileInputStream inStream = null;
		FileOutputStream outStream = null;
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		try {
			inStream = new FileInputStream(source);
			outStream = new FileOutputStream(target);
			inChannel = inStream.getChannel();
			outChannel = outStream.getChannel();
			inChannel.transferTo(0, inChannel.size(), outChannel);
		}
		catch (Exception e) {
			Logger.error("Error when copying file from " + source.getAbsolutePath() + " to " + target.getAbsolutePath()
					+ ": " + e.toString());
			return false;
		}
		finally {
			try {
				inStream.close();
			}
			catch (Exception e) {
				// ignore exception
			}
			try {
				outStream.close();
			}
			catch (Exception e) {
				// ignore exception
			}
			try {
				inChannel.close();
			}
			catch (Exception e) {
				// ignore exception
			}
			try {
				outChannel.close();
			}
			catch (Exception e) {
				// ignore exception
			}
		}
		return true;
	}
}
