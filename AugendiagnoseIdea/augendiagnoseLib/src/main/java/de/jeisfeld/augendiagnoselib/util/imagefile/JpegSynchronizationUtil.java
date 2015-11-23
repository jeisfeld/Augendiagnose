package de.jeisfeld.augendiagnoselib.util.imagefile;

import java.util.HashMap;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.JpegMetadataUtil.ExifStorageException;

/**
 * Utility class to help storing metadata in jpg files in a synchronized way, preventing to store the same file twice in
 * parallel.
 */
public final class JpegSynchronizationUtil {

	/**
	 * Hide default constructor.
	 */
	private JpegSynchronizationUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Storage for currently running save tasks.
	 */
	private static final HashMap<String, JpegMetadata> RUNNING_SAVE_REQUESTS = new HashMap<>();
	/**
	 * Storage for queued save tasks.
	 */
	private static final HashMap<String, JpegMetadata> QUEUED_SAVE_REQUESTS = new HashMap<>();
	/**
	 * The tag for logging.
	 */
	private static final String TAG = Application.TAG + ".JSU";

	/**
	 * This method handles a request to retrieve metadata for a file. If there is no running async task to update
	 * metadata for this file, then the data is taken directly from the file. Otherwise, it is taken from the last
	 * metadata to be stored for this file.
	 *
	 * @param pathname the path of the jpg file.
	 * @return null for non-JPEG files. The metadata from the file if readable. Otherwise empty metadata.
	 */
	public static JpegMetadata getJpegMetadata(@NonNull final String pathname) {
		JpegMetadata cachedMetadata = null;

		try {
			JpegMetadataUtil.checkJpeg(pathname);
		}
		catch (Exception e) {
			Log.w(TAG, e.getMessage());
			return null;
		}

		synchronized (JpegSynchronizationUtil.class) {
			if (QUEUED_SAVE_REQUESTS.containsKey(pathname)) {
				cachedMetadata = QUEUED_SAVE_REQUESTS.get(pathname);
			}
			else if (RUNNING_SAVE_REQUESTS.containsKey(pathname)) {
				cachedMetadata = RUNNING_SAVE_REQUESTS.get(pathname);
			}
		}

		if (cachedMetadata != null) {
			Log.i(TAG, "Retrieve cached metadata for file " + pathname);
			return cachedMetadata;
		}
		else {
			try {
				return JpegMetadataUtil.getMetadata(pathname);
			}
			catch (Exception e) {
				Log.e(TAG, "Failed to retrieve metadata for file " + pathname, e);
				return new JpegMetadata();
			}
		}
	}

	/**
	 * This method handles a request to update metadata on a file. If no such request on the file is in process, then an
	 * async task is started to update the metadata. Otherwise, it is put on the queue.
	 *
	 * @param pathname the path of the jpg file.
	 * @param metadata the metadata.
	 */
	public static void storeJpegMetadata(@NonNull final String pathname, final JpegMetadata metadata) {
		try {
			JpegMetadataUtil.checkJpeg(pathname);
		}
		catch (Exception e) {
			Log.w(TAG, e.getMessage());
			return;
		}

		synchronized (JpegSynchronizationUtil.class) {
			if (RUNNING_SAVE_REQUESTS.containsKey(pathname)) {
				QUEUED_SAVE_REQUESTS.put(pathname, metadata);
			}
			else {
				triggerJpegSaverTask(pathname, metadata);
			}
		}
	}

	/**
	 * Do cleanup from the last JpegSaverTask and trigger the next task on the same file, if existing.
	 *
	 * @param pathname The path of the jpg file.
	 */
	private static void triggerNextFromQueue(final String pathname) {
		synchronized (JpegSynchronizationUtil.class) {
			RUNNING_SAVE_REQUESTS.remove(pathname);
			if (QUEUED_SAVE_REQUESTS.containsKey(pathname)) {
				Log.i(TAG, "Executing queued store request for file " + pathname);
				JpegMetadata newMetadata = QUEUED_SAVE_REQUESTS.get(pathname);
				QUEUED_SAVE_REQUESTS.remove(pathname);
				triggerJpegSaverTask(pathname, newMetadata);
			}
		}
	}

	/**
	 * Utility method to start the JpegSaverTask so save a jpg file with metadata.
	 *
	 * @param pathname the path of the jpg file.
	 * @param metadata the metadata.
	 */
	private static void triggerJpegSaverTask(final String pathname, final JpegMetadata metadata) {
		RUNNING_SAVE_REQUESTS.put(pathname, metadata);
		JpegSaverTask task = new JpegSaverTask(pathname, metadata);
		task.execute();

		PreferenceUtil.incrementCounter(R.string.key_statistics_countsave);
	}

	/**
	 * Get information if there is an image in the process of being saved.
	 *
	 * @return true if an image is currently saved.
	 */
	public static boolean isSaving() {
		return RUNNING_SAVE_REQUESTS.size() > 0;
	}

	/**
	 * Task to save a JPEG file asynchronously with changed metadata.
	 */
	private static final class JpegSaverTask extends AsyncTask<Void, Void, Exception> {
		/**
		 * The path of the jpg file.
		 */
		private final String mPathname;
		/**
		 * The changed metadata.
		 */
		private final JpegMetadata mMetadata;

		/**
		 * Constructor for the task.
		 *
		 * @param pathname the path of the jpg file.
		 * @param metadata the metadata.
		 */
		private JpegSaverTask(final String pathname, final JpegMetadata metadata) {
			this.mPathname = pathname;
			this.mMetadata = metadata;
		}

		@Override
		protected void onPreExecute() {
			Log.d(TAG, "Starting thread to save file " + mPathname);
		}

		@Override
		protected Exception doInBackground(final Void... nothing) {
			try {
				JpegMetadataUtil.changeMetadata(mPathname, mMetadata);
				return null;
			}
			catch (Exception e) {
				return e;
			}
		}

		@Override
		protected void onPostExecute(@Nullable final Exception e) {
			if (e != null) {
				if (e instanceof ExifStorageException) {
					Log.e(TAG, "Failed to save file " + mPathname, e);
					DialogUtil.displayToast(Application.getAppContext(),
							R.string.message_dialog_failed_to_store_exif, mPathname);
				}
				else {
					Log.e(TAG, "Failed to store EXIF data for file " + mPathname, e);
					DialogUtil.displayToast(Application.getAppContext(),
							R.string.message_dialog_failed_to_store_metadata, mPathname);
				}
			}
			else {
				Log.d(TAG, "Successfully saved file " + mPathname);
			}
			triggerNextFromQueue(mPathname);
		}
	}
}
