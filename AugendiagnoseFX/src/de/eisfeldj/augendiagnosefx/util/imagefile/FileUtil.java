package de.eisfeldj.augendiagnosefx.util.imagefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import de.eisfeldj.augendiagnosefx.util.Logger;

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
			Logger.error(
					"Error when copying file from " + source.getAbsolutePath() + " to " + target.getAbsolutePath(), e);
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

	/**
	 * Move a file.
	 *
	 * @param source
	 *            The source file
	 * @param target
	 *            The target file
	 * @return true if the copying was successful.
	 */
	public static boolean moveFile(final File source, final File target) {
		// First try the simple way
		boolean success = source.renameTo(target);

		if (!success) {
			// Special handling for platforms where renameTo does not overwrite.
			String backupSuffix = ".bak." + System.currentTimeMillis();
			File backupFile = new File(target.getAbsolutePath() + backupSuffix);

			success = target.renameTo(backupFile);
			if (success) {
				success = source.renameTo(target);
				if (success) {
					success = backupFile.delete();
				}
			}

		}

		return success;
	}
}
