package de.eisfeldj.augendiagnosefx.util;

import java.util.HashMap;

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
	private static HashMap<String, JpegMetadata> runningSaveRequests = new HashMap<String, JpegMetadata>();
	/**
	 * Storage for queued save tasks.
	 */
	private static HashMap<String, JpegMetadata> queuedSaveRequests = new HashMap<String, JpegMetadata>();

	/**
	 * This method handles a request to retrieve metadata for a file. If there is no running async task to update
	 * metadata for this file, then the data is taken directly from the file. Otherwise, it is taken from the last
	 * metadata to be stored for this file.
	 *
	 * @param pathname
	 *            the path of the jpg file.
	 * @return null for non-JPEG files. The metadata from the file if readable. Otherwise empty metadata.
	 */
	public static JpegMetadata getJpegMetadata(final String pathname) {
		JpegMetadata cachedMetadata = null;

		try {
			JpegMetadataUtil.checkJpeg(pathname);
		}
		catch (Exception e) {
			Logger.warning(e.getMessage());
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
			Logger.info("Retrieve cached metadata for file " + pathname);
			return cachedMetadata;
		}
		else {
			try {
				return JpegMetadataUtil.getMetadata(pathname);
			}
			catch (Exception e) {
				Logger.error("Failed to retrieve metadata for file " + pathname + ": " + e.toString());
				return new JpegMetadata();
			}
		}
	}

	/**
	 * This method handles a request to update metadata on a file. If no such request on the file is in process, then an
	 * async task is started to update the metadata. Otherwise, it is put on the queue.
	 *
	 * @param pathname
	 *            the path of the jpg file.
	 * @param metadata
	 *            the metadata.
	 */
	public static void storeJpegMetadata(final String pathname, final JpegMetadata metadata) {
		try {
			JpegMetadataUtil.checkJpeg(pathname);
		}
		catch (Exception e) {
			Logger.warning(e.getMessage());
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
	 *
	 * @param pathname
	 *            The path of the jpg file.
	 */
	private static void triggerNextFromQueue(final String pathname) {
		synchronized (JpegSynchronizationUtil.class) {
			runningSaveRequests.remove(pathname);
			if (queuedSaveRequests.containsKey(pathname)) {
				Logger.info("Executing queued store request for file " + pathname);
				JpegMetadata newMetadata = queuedSaveRequests.get(pathname);
				queuedSaveRequests.remove(pathname);
				triggerJpegSaverTask(pathname, newMetadata);
			}
		}
	}

	/**
	 * Utility method to start the JpegSaverTask so save a jpg file with metadata.
	 *
	 * @param pathname
	 *            the path of the jpg file.
	 * @param metadata
	 *            the metadata.
	 */
	private static void triggerJpegSaverTask(final String pathname, final JpegMetadata metadata) {
		runningSaveRequests.put(pathname, metadata);
		JpegSaverTask task = new JpegSaverTask(pathname, metadata);
		task.execute();
	}

	/**
	 * Task to save a JPEG file asynchronously with changed metadata.
	 */
	private static class JpegSaverTask extends AsyncTask<Void, Void, Exception> {
		/**
		 * The path of the jpg file.
		 */
		private String pathname;
		/**
		 * The changed metadata.
		 */
		private JpegMetadata metadata;

		/**
		 * Constructor for the task.
		 *
		 * @param pathname
		 *            the path of the jpg file.
		 * @param metadata
		 *            the metadata.
		 */
		public JpegSaverTask(final String pathname, final JpegMetadata metadata) {
			this.pathname = pathname;
			this.metadata = metadata;
		}

		@Override
		protected void onPreExecute() {
			Logger.info("Starting thread to save file " + pathname);
		}

		@Override
		protected Exception doInBackground(final Void... nothing) {
			try {
				JpegMetadataUtil.changeMetadata(pathname, metadata);
				return null;
			}
			catch (Exception e) {
				return e;
			}
		}

		@Override
		protected void onPostExecute(final Exception e) {
			if (e != null) {
				Logger.error("Failed to save file " + pathname + ": " + e.toString());
				DialogUtil.displayErrorAsToast(Application.getAppContext(),
						R.string.message_dialog_failed_to_store_metadata, pathname);
			}
			else {
				Logger.info("Successfully saved file " + pathname);
			}
			triggerNextFromQueue(pathname);
		}
	}

}
