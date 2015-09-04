package de.jeisfeld.augendiagnoselib.activities;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.components.EyeImageView;
import de.jeisfeld.augendiagnoselib.components.SelectTwoPicturesArrayAdapter;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.DialogUtil.ConfirmDialogFragment.ConfirmDialogListener;
import de.jeisfeld.augendiagnoselib.util.TwoImageSelectionHandler;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto;
import de.jeisfeld.augendiagnoselib.util.imagefile.FileUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.ImageUtil;

/**
 * Activity to select a pair of eye photos from a folder and return the paths to the parent activity.
 */
public class SelectTwoPicturesActivity extends BaseActivity {
	/**
	 * The requestCode with which this activity is started.
	 */
	public static final int REQUEST_CODE = 2;

	/**
	 * The resource key for the folder name.
	 */
	private static final String STRING_EXTRA_FOLDER = "de.jeisfeld.augendiagnoselib.FOLDER";
	/**
	 * The resource key for the array of filenames.
	 */
	private static final String STRING_EXTRA_FILENAMES = "de.jeisfeld.augendiagnoselib.FILENAMES";
	/**
	 * The resource key for the name of the first selected file.
	 */
	private static final String STRING_RESULT_FILENAME1 = "de.jeisfeld.augendiagnoselib.FILENAME1";
	/**
	 * The resource key for the name of the second selected file.
	 */
	private static final String STRING_RESULT_FILENAME2 = "de.jeisfeld.augendiagnoselib.FILENAME2";

	/**
	 * The image folder.
	 */
	private File folder;
	/**
	 * The list of image files.
	 */
	private String[] fileNames;

	/**
	 * The view showing the eye photos.
	 */
	private GridView gridView;

	/**
	 * The selected view when displaying the context menu.
	 */
	private EyeImageView selectedView;

	/**
	 * The organize activity which has triggered the selection. Temporary static storage.
	 */
	private static OrganizeNewPhotosActivity parentActivityStatic;

	/**
	 * The organize activity which has triggered the selection.
	 */
	private OrganizeNewPhotosActivity parentActivity;

	/**
	 * Static helper method to start the activity, passing the path of the folder.
	 *
	 * @param activity
	 *            The activity starting this activity.
	 * @param foldername
	 *            The image folder.
	 */
	public static final void startActivity(final OrganizeNewPhotosActivity activity, final String foldername) {
		parentActivityStatic = activity;
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
	public static final void startActivity(final OrganizeNewPhotosActivity activity, final String[] fileNames) {
		parentActivityStatic = activity;
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

		parentActivity = parentActivityStatic;
		parentActivityStatic = null;

		setContentView(R.layout.activity_select_two_pictures);

		String folderName = getIntent().getStringExtra(STRING_EXTRA_FOLDER);
		if (folderName != null) {
			folder = new File(folderName);
		}
		fileNames = getIntent().getStringArrayExtra(STRING_EXTRA_FILENAMES);

		// Prepare the view
		gridView = (GridView) findViewById(R.id.gridViewSelectTwoPictures);
		gridView.setAdapter(new SelectTwoPicturesArrayAdapter(this, getEyePhotos()));

		// Prepare the handler class
		TwoImageSelectionHandler.getInstance().setActivity(this);
	}

	@Override
	protected final void onDestroy() {
		super.onDestroy();
		parentActivity = null;
	}

	@Override
	protected final int getHelpResource() {
		return R.string.html_organize_photos;
	}

	/**
	 * Helper method to retrieve the list of photos in the folder as EyePhoto objects.
	 *
	 * @return The list of eye photos.
	 */
	private EyePhoto[] getEyePhotos() {
		File[] files;

		if (isStartedWithInputFolder()) {
			// Get files from folder
			files = folder.listFiles(new ImageUtil.ImageFileFilter());
			if (files == null) {
				files = new File[0];
			}
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

	/*
	 * Create the context menu.
	 */
	@Override
	public final void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_select_two, menu);
		selectedView = (EyeImageView) v;
	}

	/*
	 * Handle items in the context menu.
	 */
	@Override
	public final boolean onContextItemSelected(final MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_delete_selected_image) {
			ConfirmDialogListener listenerDelete = new ConfirmDialogListener() {
				/**
				 * The serial version id.
				 */
				private static final long serialVersionUID = -3186094978749077352L;

				@Override
				public void onDialogPositiveClick(final DialogFragment dialog) {
					// delete image
					boolean success = selectedView.getEyePhoto().delete();
					updateEyePhotoList();

					if (!success) {
						DialogUtil.displayError(SelectTwoPicturesActivity.this,
								R.string.message_dialog_failed_to_delete_file, false, selectedView
										.getEyePhoto().getFilename());

					}
				}

				@Override
				public void onDialogNegativeClick(final DialogFragment dialog) {
					// Do nothing
				}
			};
			DialogUtil.displayConfirmationMessage(this, listenerDelete, R.string.button_delete,
					R.string.message_dialog_confirm_delete_photo, selectedView
							.getEyePhoto().getFilename());
			return true;
		}
		else if (itemId == R.id.action_delete_all_images) {
			ConfirmDialogListener listenerDeleteAll = new ConfirmDialogListener() {
				/**
				 * The serial version id.
				 */
				private static final long serialVersionUID = -4135840952030307156L;

				@Override
				public void onDialogPositiveClick(final DialogFragment dialog) {
					if (isStartedWithInputFolder()) {
						// delete images
						boolean success = FileUtil.deleteFilesInFolder(folder);
						updateEyePhotoList();

						if (success) {
							finish();
							parentActivity.finish();
						}
						else {
							DialogUtil.displayError(SelectTwoPicturesActivity.this,
									R.string.message_dialog_failed_to_delete_all_files, false, selectedView
											.getEyePhoto().getFilename());
						}
					}
				}

				@Override
				public void onDialogNegativeClick(final DialogFragment dialog) {
					// Do nothing
				}
			};
			DialogUtil.displayConfirmationMessage(this, listenerDeleteAll, R.string.button_delete,
					R.string.message_dialog_confirm_delete_all_photos, selectedView
							.getEyePhoto().getFilename());
			return true;
		}
		else {
			return super.onContextItemSelected(item);
		}

	}

	/**
	 * Update the list of eye photo pairs.
	 */
	private void updateEyePhotoList() {
		gridView.setAdapter(new SelectTwoPicturesArrayAdapter(this, getEyePhotos()));
	}

	/**
	 * Gives information if the activity is started via input folder or via list of files.
	 *
	 * @return true if started via input folder.
	 */
	public final boolean isStartedWithInputFolder() {
		return folder != null;
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
