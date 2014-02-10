package de.eisfeldj.augendiagnose.util;

import java.util.HashMap;

import android.os.AsyncTask;
import android.util.Log;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.util.JpegMetadataUtil.Metadata;

public abstract class JpegSynchronizationUtil {

	private static HashMap<String, Metadata> runningSaveRequests = new HashMap<String, Metadata>();
	private static HashMap<String, Metadata> queuedSaveRequests = new HashMap<String, Metadata>();
	private static final String TAG = Application.TAG + ".JpegSynchronizationUtil";

	/**
	 * This method handles a request to retrieve metadata for a file. If there is no running async task
	 * to update metadata for this file, then the data is taken directly from the file. Otherwise, it is
	 * taken from the last metadata to be stored for this file.
	 * @param pathname
	 * @return null for non-JPEG files. The metadata from the file if readable. Otherwise empty metadata.
	 */
	public static Metadata getJpegMetadata(String pathname) {
		Metadata cachedMetadata = null;
		
		try {
			JpegMetadataUtil.checkJpeg(pathname);
		}
		catch (Exception e) {
			Log.w(TAG, e.getMessage());
			return null;
		}

		synchronized (JpegSynchronizationUtil.class) {
			if (queuedSaveRequests.containsKey(pathname)) {
				cachedMetadata = queuedSaveRequests.get(pathname);
			}
			else if (runningSaveRequests.containsKey(pathname)) {
				cachedMetadata = runningSaveRequests.get(pathname);
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
				return new Metadata();
			}
		}
	}

	/**
	 * This method handles a request to update metadata on a file. If no such request on the file is in process,
	 * then an async task is started to update the metadata. Otherwise, it is put on the queue.
	 * @param pathname
	 * @param metadata
	 */
	public static void storeJpegMetadata(String pathname, Metadata metadata) {
		try {
			JpegMetadataUtil.checkJpeg(pathname);
		}
		catch (Exception e) {
			Log.w(TAG, e.getMessage());
			return;
		}
		
		synchronized (JpegSynchronizationUtil.class) {
			if (runningSaveRequests.containsKey(pathname)) {
				queuedSaveRequests.put(pathname, metadata);
			}
			else {
				triggerJpegSaverTask(pathname, metadata);
			}
		}
	}

	/**
	 * Do cleanup from the last JpegSaverTask and trigger the next task on the same file, if existing.
	 * @param pathname
	 */
	private static void triggerNextFromQueue(String pathname) {
		synchronized (JpegSynchronizationUtil.class) {
			runningSaveRequests.remove(pathname);
			if (queuedSaveRequests.containsKey(pathname)) {
				Log.i(TAG, "Executing queued store request for file " + pathname);
				Metadata newMetadata = queuedSaveRequests.get(pathname);
				queuedSaveRequests.remove(pathname);
				triggerJpegSaverTask(pathname, newMetadata);
			}
		}
	}

	/**
	 * Utility method to start the JpegSaverTask
	 * @param pathname
	 * @param metadata
	 */
	private static void triggerJpegSaverTask(String pathname, Metadata metadata) {
		runningSaveRequests.put(pathname, metadata);
		JpegSaverTask task = new JpegSaverTask(pathname, metadata);
		task.execute();
	}

	/**
	 * Task to save a JPEG file asynchronously with changed metadata
	 */
	private static class JpegSaverTask extends AsyncTask<Void, Void, Exception> {
		private String pathname;
		private Metadata metadata;

		public JpegSaverTask(String pathname, Metadata metadata) {
			this.pathname = pathname;
			this.metadata = metadata;
		}

		@Override
		protected void onPreExecute() {
			Log.d(TAG, "Starting thread to save file " + pathname);
		}

		@Override
		protected Exception doInBackground(Void... nothing) {
			try {
				JpegMetadataUtil.changeMetadata(pathname, metadata);
				return null;
			}
			catch (Exception e) {
				return e;
			}
		}

		@Override
		protected void onPostExecute(Exception e) {
			if (e != null) {
				Log.e(TAG, "Failed to save file " + pathname, e);
			}
			else {
				Log.d(TAG, "Successfully saved file " + pathname);
			}
			triggerNextFromQueue(pathname);
		}
	}

}

