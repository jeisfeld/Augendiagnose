package de.eisfeldj.augendiagnose.activities;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.components.SelectTwoPicturesArrayAdapter;
import de.eisfeldj.augendiagnose.util.EyePhoto;
import de.eisfeldj.augendiagnose.util.ImageUtil;
import de.eisfeldj.augendiagnose.util.TwoImageSelectionHandler;

/**
 * Activity to select a pair of eye photos from a folder and return the paths to the parent activity.
 */
public class SelectTwoPicturesActivity extends Activity {
	/**
	 * The requestCode with which this activity is started.
	 */
	public static final int REQUEST_CODE = 2;

	/**
	 * The resource key for the folder name.
	 */
	private static final String STRING_EXTRA_FOLDER = "de.eisfeldj.augendiagnose.FOLDER";
	/**
	 * The resource key for the array of filenames.
	 */
	private static final String STRING_EXTRA_FILENAMES = "de.eisfeldj.augendiagnose.FILENAMES";
	/**
	 * The resource key for the name of the first selected file.
	 */
	private static final String STRING_RESULT_FILENAME1 = "de.eisfeldj.augendiagnose.FILENAME1";
	/**
	 * The resource key for the name of the second selected file.
	 */
	private static final String STRING_RESULT_FILENAME2 = "de.eisfeldj.augendiagnose.FILENAME2";

	/**
	 * The image folder.
	 */
	private File folder;
	/**
	 * The list of image files.
	 */
	private String[] fileNames;

	/**
	 * Static helper method to start the activity, passing the path of the folder.
	 *
	 * @param activity
	 *            The activity starting this activity.
	 * @param foldername
	 *            The image folder.
	 */
	public static final void startActivity(final Activity activity, final String foldername) {
		Intent intent = new Intent(activity, SelectTwoPicturesActivity.class);
		intent.putExtra(STRING_EXTRA_FOLDER, foldername);
		activity.startActivityForResult(intent, REQUEST_CODE);
	}

	/**
	 * Static helper method to start the activity, passing the list of files.
	 *
	 * @param activity
	 *            The activity starting this activity.
	 * @param fileNames
	 *            The list of image files.
	 */
	public static final void startActivity(final Activity activity, final String[] fileNames) {
		Intent intent = new Intent(activity, SelectTwoPicturesActivity.class);
		intent.putExtra(STRING_EXTRA_FILENAMES, fileNames);
		activity.startActivityForResult(intent, REQUEST_CODE);
	}

	/**
	 * Static helper method to extract the selected filenames from the activity response.
	 *
	 * @param resultCode
	 *            The result code indicating if the response was successful.
	 * @param data
	 *            The activity response data.
	 * @return The returned file names.
	 */
	public static final FilePair getResult(final int resultCode, final Intent data) {
		if (resultCode == RESULT_OK) {
			Bundle res = data.getExtras();
			return new FilePair(res.getString(STRING_RESULT_FILENAME1), res.getString(STRING_RESULT_FILENAME2));
		}
		else {
			return null;
		}
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_two_pictures);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		String folderName = getIntent().getStringExtra(STRING_EXTRA_FOLDER);
		if (folderName != null) {
			folder = new File(folderName);
		}
		fileNames = getIntent().getStringArrayExtra(STRING_EXTRA_FILENAMES);

		// Prepare the view
		GridView gridview = (GridView) findViewById(R.id.gridViewSelectTwoPictures);
		gridview.setAdapter(new SelectTwoPicturesArrayAdapter(this, getEyePhotos()));

		// Prepare the handler class
		TwoImageSelectionHandler.getInstance().setActivity(this);
		TwoImageSelectionHandler.getInstance().prepareViewForSelection(gridview);
	}

	/**
	 * Helper method to retrieve the list of photos in the folder as EyePhoto objects.
	 *
	 * @return The list of eye photos.
	 */
	private EyePhoto[] getEyePhotos() {
		File[] files;

		if (folder != null) {
			// Get files from folder
			files = folder.listFiles(new ImageUtil.ImageFileFilter());
			Arrays.sort(files, new Comparator<File>() {
				@Override
				public int compare(final File f1, final File f2) {
					return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
				}
			});
		}
		else {
			files = new File[fileNames.length];
			for (int i = 0; i < fileNames.length; i++) {
				files[i] = new File(fileNames[i]);
			}
		}

		EyePhoto[] result = new EyePhoto[files.length];
		for (int i = 0; i < files.length; i++) {
			result[i] = new EyePhoto(files[i]);
		}
		return result;
	}

	/**
	 * Helper method: Return the selected filenames and finish the activity.
	 *
	 * @param filename1
	 *            The first filename.
	 * @param filename2
	 *            The second filename.
	 */
	public final void returnResult(final String filename1, final String filename2) {
		Bundle resultData = new Bundle();
		resultData.putCharSequence(STRING_RESULT_FILENAME1, filename1);
		resultData.putCharSequence(STRING_RESULT_FILENAME2, filename2);
		Intent intent = new Intent();
		intent.putExtras(resultData);
		setResult(RESULT_OK, intent);
		finish();
	}

	/**
	 * Container for two files.
	 */
	public static class FilePair {
		/**
		 * Constructor to create a pair of files.
		 *
		 * @param name1
		 *            The first file.
		 * @param name2
		 *            The second file.
		 */
		public FilePair(final String name1, final String name2) {
			file1 = new File(name1);
			file2 = new File(name2);
		}

		/**
		 * The two files stored in the container.
		 */
		private File file1, file2;

		public final File getFile1() {
			return file1;
		}

		public final File getFile2() {
			return file2;
		}
	}
}
