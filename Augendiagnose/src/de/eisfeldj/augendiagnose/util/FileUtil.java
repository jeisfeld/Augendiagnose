package de.eisfeldj.augendiagnose.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;

/**
 * Utility class for helping parsing file systems.
 */
public abstract class FileUtil {
	/**
	 * Determine the camera folder. There seems to be no Android API to work for real devices, so this is a best guess.
	 *
	 * @return the default camera folder.
	 */
	public static String getDefaultCameraFolder() {
		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		if (path.exists()) {
			File test1 = new File(path, "Camera/");
			if (test1.exists()) {
				path = test1;
			}
			else {
				File test2 = new File(path, "100ANDRO/");
				if (test2.exists()) {
					path = test2;
				}
				else {
					File test3 = new File(path, "100MEDIA/");
					path = test3;
				}
			}
		}
		else {
			File test3 = new File(path, "Camera/");
			path = test3;
		}
		return path.getAbsolutePath();
	}

	/**
	 * Copy a file. The target file may even be on external SD card for Kitkat.
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
		OutputStream outStream = null;
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		try {
			inStream = new FileInputStream(source);

			if (isWritable(target)) {
				// standard way
				outStream = new FileOutputStream(target);
				inChannel = inStream.getChannel();
				outChannel = ((FileOutputStream) outStream).getChannel();
				inChannel.transferTo(0, inChannel.size(), outChannel);
			}
			else {
				// Workaround for Kitkat ext SD card
				Uri uri = MediaStoreUtil.getUriFromFile(target.getAbsolutePath());
				outStream = Application.getAppContext().getContentResolver().openOutputStream(uri);
				byte[] buffer = new byte[4096]; // MAGIC_NUMBER
				int bytesRead;
				while ((bytesRead = inStream.read(buffer)) != -1) {
					outStream.write(buffer, 0, bytesRead);
				}
			}
		}
		catch (Exception e) {
			Log.e(Application.TAG,
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
	 * Delete a file. May be even on external SD card.
	 *
	 * @param file
	 *            the file to be deleted.
	 * @return True if successfully deleted.
	 */
	public static final boolean deleteFile(final File file) {
		// First try the normal deletion.
		if (file.delete()) {
			return true;
		}

		// Try via media store.
		ContentResolver resolver = Application.getAppContext().getContentResolver();

		try {
			Uri uri = MediaStoreUtil.getUriFromFile(file.getAbsolutePath());
			resolver.delete(uri, null, null);
			return !file.exists();
		}
		catch (Exception e) {
			Log.e(Application.TAG, "Error when deleting file " + file.getAbsolutePath(), e);
			return false;
		}
	}

	/**
	 * Move a file. The target file may even be on external SD card.
	 *
	 * @param source
	 *            The source file
	 * @param target
	 *            The target file
	 * @return true if the copying was successful.
	 */
	public static final boolean moveFile(final File source, final File target) {
		// First try the normal rename.
		if (source.renameTo(target)) {
			return true;
		}

		boolean success = copyFile(source, target);
		if (success) {
			success = deleteFile(source);
		}
		return success;
	}

	/**
	 * Rename a folder. In case of extSdCard in Kitkat, the old folder stays in place, but files are moved.
	 *
	 * @param source
	 *            The source folder.
	 * @param target
	 *            The target folder.
	 * @return true if the renaming was successful.
	 */
	public static final boolean renameFolder(final File source, final File target) {
		// First try the normal rename.
		if (source.renameTo(target)) {
			return true;
		}
		if (target.exists()) {
			return false;
		}

		// Try the workaround.
		if (!mkdir(target)) {
			return false;
		}

		File[] sourceFiles = source.listFiles();

		for (File sourceFile : sourceFiles) {
			String fileName = sourceFile.getName();
			File targetFile = new File(target, fileName);
			if (!copyFile(sourceFile, targetFile)) {
				// stop on first error
				return false;
			}
		}
		// Only after successfully copying all files, delete files on source folder.
		for (File sourceFile : sourceFiles) {
			if (!deleteFile(sourceFile)) {
				// stop on first error
				return false;
			}
		}
		return true;
	}

	/**
	 * Get a temp file.
	 *
	 * @param file
	 *            The base file for which to create a temp file.
	 * @return The temp file.
	 */
	public static final File getTempFile(final File file) {
		File extDir = Application.getAppContext().getExternalFilesDir(null);
		File tempFile = new File(extDir, file.getName());
		return tempFile;
	}

	/**
	 * Create a folder. The folder may even be on external SD card for Kitkat.
	 *
	 * @param file
	 *            The folder to be created.
	 * @return True if creation was successful.
	 */
	public static boolean mkdir(final File file) {
		if (file.exists()) {
			// nothing to create.
			return file.isDirectory();
		}

		// Try the normal way
		if (file.mkdir()) {
			return true;
		}

		// Try the Kitkat workaround.
		ContentResolver resolver = Application.getAppContext().getContentResolver();
		File tempFile = new File(file, "dummyImage.jpg");

		File dummySong = copyDummyFiles();
		int albumId = MediaStoreUtil.getAlbumIdFromAudioFile(dummySong);
		Uri albumArtUri = Uri.parse("content://media/external/audio/albumart/" + albumId);

		ContentValues contentValues = new ContentValues();
		contentValues.put(MediaStore.MediaColumns.DATA, tempFile.getAbsolutePath());
		contentValues.put(MediaStore.Audio.AlbumColumns.ALBUM_ID, albumId);

		if (resolver.update(albumArtUri, contentValues, null, null) == 0) {
			resolver.insert(Uri.parse("content://media/external/audio/albumart"), contentValues);
		}
		try {
			ParcelFileDescriptor fd = resolver.openFileDescriptor(albumArtUri, "r");
			fd.close();
		}
		catch (Exception e) {
			Log.e(Application.TAG, "Could not open file", e);
			return false;
		}
		finally {
			FileUtil.deleteFile(tempFile);
		}

		return true;
	}

	/**
	 * Delete a folder.
	 *
	 * @param file
	 *            The folder name.
	 *
	 * @return true if successful.
	 */
	public static boolean rmdir(final File file) {
		if (!file.exists()) {
			return true;
		}
		if (!file.isDirectory()) {
			return false;
		}
		if (file.list() != null && file.list().length > 0) {
			// Delete only empty folder.
			return false;
		}

		// Try the normal way
		if (file.delete()) {
			return true;
		}

		ContentResolver resolver = Application.getAppContext().getContentResolver();
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
		resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

		// Delete the created entry, such that content provider will delete the file.
		resolver.delete(MediaStore.Files.getContentUri("external"), MediaStore.MediaColumns.DATA + "=?",
				new String[] { file.getAbsolutePath() });

		return !file.exists();
	}

	/**
	 * Delete all files in a folder.
	 *
	 * @param folder
	 *            the folder
	 * @return true if successful.
	 */
	public static final boolean deleteFilesInFolder(final File folder) {
		boolean totalSuccess = true;

		String[] children = folder.list();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				File file = new File(folder, children[i]);
				if (!file.isDirectory()) {
					boolean success = FileUtil.deleteFile(file);
					if (!success) {
						Log.w(Application.TAG, "Failed to delete file" + children[i]);
						totalSuccess = false;
					}
				}
			}
		}
		return totalSuccess;
	}

	/**
	 * Delete a directory asynchronously.
	 *
	 * @param activity
	 *            The activity calling this method.
	 * @param file
	 *            The folder name.
	 * @param postActions
	 *            Commands to be executed after success.
	 */
	public static void rmdirAsynchronously(final Activity activity, final File file, final Runnable postActions) {
		new Thread() {
			@Override
			public void run() {
				int retryCounter = 5; // MAGIC_NUMBER
				while (!FileUtil.rmdir(file) && retryCounter > 0) {
					try {
						Thread.sleep(100); // MAGIC_NUMBER
					}
					catch (InterruptedException e) {
						// do nothing
					}
					retryCounter--;
				}
				if (file.exists()) {
					DialogUtil.displayError(activity, R.string.message_dialog_failed_to_delete_folder, false,
							file.getAbsolutePath());
				}
				else {
					activity.runOnUiThread(postActions);
				}

			}
		}.start();
	}

	/**
	 * Check is a file is writable. Detects write issues on external SD card.
	 *
	 * @param file
	 *            The file
	 * @return true if the file is writable.
	 */
	public static final boolean isWritable(final File file) {
		try {
			FileOutputStream output = new FileOutputStream(file, true);
			try {
				output.close();
			}
			catch (IOException e) {
				// do nothing.
			}
		}
		catch (FileNotFoundException e) {
			return false;
		}
		return file.canWrite();
	}

	/**
	 * Copy a resource file into a private target directory, if the target does not yet exist.
	 *
	 * @param resource
	 *            The resource file.
	 * @param folderName
	 *            The folder below app folder where the file is copied to.
	 * @param targetName
	 *            The name of the target file.
	 * @throws IOException
	 * @return the dummy file.
	 */
	private static File copyDummyFile(final int resource, final String folderName, final String targetName)
			throws IOException {
		File externalFilesDir = Application.getAppContext().getExternalFilesDir(folderName);
		if (externalFilesDir == null) {
			return null;
		}
		File targetFile = new File(externalFilesDir, targetName);

		if (!targetFile.exists()) {
			InputStream in = null;
			OutputStream out = null;
			try {
				in = Application.getAppContext().getResources().openRawResource(resource);
				out = new FileOutputStream(targetFile);
				byte[] buffer = new byte[4096]; // MAGIC_NUMBER
				int bytesRead;
				while ((bytesRead = in.read(buffer)) != -1) {
					out.write(buffer, 0, bytesRead);
				}
			}
			finally {
				if (in != null) {
					try {
						in.close();
					}
					catch (IOException ex) {
						// do nothing
					}
				}
				if (out != null) {
					try {
						out.close();
					}
					catch (IOException ex) {
						// do nothing
					}
				}
			}
		}
		return targetFile;
	}

	/**
	 * Copy the dummy image and dummy mp3 into the private folder, if not yet there.
	 *
	 * @return the dummy mp3.
	 */
	private static File copyDummyFiles() {
		try {
			copyDummyFile(R.raw.albumart, "mkdirFiles", "albumart.jpg");
			return copyDummyFile(R.raw.silence, "mkdirFiles", "silence.mp3");

		}
		catch (IOException e) {
			Log.e(Application.TAG, "Could not copy dummy files.", e);
			return null;
		}
	}

}
