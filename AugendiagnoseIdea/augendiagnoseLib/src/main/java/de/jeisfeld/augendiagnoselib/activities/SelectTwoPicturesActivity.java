package de.jeisfeld.augendiagnoselib.activities;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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
	private File mFolder;
	/**
	 * The list of image files.
	 */
	private String[] mFileNames;

	/**
	 * The view showing the eye photos.
	 */
	private GridView mGridView;

	/**
	 * The selected view when displaying the context menu.
	 */
	private EyeImageView mSelectedView;

	/**
	 * The organize activity which has triggered the selection. Temporary static storage.
	 */
	@Nullable
	private static OrganizeNewPhotosActivity mStaticParentActivity;

	/**
	 * The organize activity which has triggered the selection.
	 */
	@Nullable
	private OrganizeNewPhotosActivity mParentActivity;

	/**
	 * Static helper method to start the activity, passing the path of the folder.
	 *
	 * @param activity   The activity starting this activity.
	 * @param foldername The image folder.
	 */
	public static void startActivity(@NonNull final OrganizeNewPhotosActivity activity, final String foldername) {
		mStaticParentActivity = activity;
		Intent intent = new Intent(activity, SelectTwoPicturesActivity.class);
		intent.putExtra(STRING_EXTRA_FOLDER, foldername);
		activity.startActivityForResult(intent, REQUEST_CODE);
	}

	/**
	 * Static helper method to start the activity, passing the list of files.
	 *
	 * @param activity  The activity starting this activity.
	 * @param fileNames The list of image files.
	 */
	public static void startActivity(@NonNull final OrganizeNewPhotosActivity activity, final String[] fileNames) {
		mStaticParentActivity = activity;
		Intent intent = new Intent(activity, SelectTwoPicturesActivity.class);
		intent.putExtra(STRING_EXTRA_FILENAMES, fileNames);
		activity.startActivityForResult(intent, REQUEST_CODE);
	}

	/**
	 * Static helper method to extract the selected filenames from the activity response.
	 *
	 * @param resultCode The result code indicating if the response was successful.
	 * @param data       The activity response data.
	 * @return The returned file names.
	 */
	public static FilePair getResult(final int resultCode, @NonNull final Intent data) {
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

		mParentActivity = mStaticParentActivity;
		mStaticParentActivity = null;

		setContentView(R.layout.activity_select_two_pictures);

		String folderName = getIntent().getStringExtra(STRING_EXTRA_FOLDER);
		if (folderName != null) {
			mFolder = new File(folderName);
		}
		mFileNames = getIntent().getStringArrayExtra(STRING_EXTRA_FILENAMES);

		// Prepare the view
		mGridView = (GridView) findViewById(R.id.gridViewSelectTwoPictures);
		mGridView.setAdapter(new SelectTwoPicturesArrayAdapter(this, getEyePhotos()));

		// Prepare the handler class
		TwoImageSelectionHandler.getInstance().setActivity(this);
	}

	@Override
	protected final void onDestroy() {
		super.onDestroy();
		mParentActivity = null;
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
	@NonNull
	private EyePhoto[] getEyePhotos() {
		File[] files;

		if (isStartedWithInputFolder()) {
			// Get files from folder
			files = mFolder.listFiles(new ImageUtil.ImageFileFilter());
			if (files == null) {
				files = new File[0];
			}
			Arrays.sort(files, new Comparator<File>() {
				@Override
				public int compare(@NonNull final File f1, @NonNull final File f2) {
					return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
				}
			});
		}
		else {
			files = new File[mFileNames.length];
			for (int i = 0; i < mFileNames.length; i++) {
				files[i] = new File(mFileNames[i]);
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
	 * @param filename1 The first filename.
	 * @param filename2 The second filename.
	 */
	public final void returnResult(final String filename1, final String filename2) {
		Bundle resultData = new Bundle();
		resultData.putCharSequence(STRING_RESULT_FILENAME1, filename1);
		resultData.putCharSequence(STRING_RESULT_FILENAME2, filename2);
		Intent intent = new Intent();

		if (filename1 == null && filename2 == null) {
			setResult(RESULT_CANCELED, intent);
		}
		else {
			intent.putExtras(resultData);
			setResult(RESULT_OK, intent);
		}

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
		mSelectedView = (EyeImageView) v;
	}

	/*
	 * Handle items in the context menu.
	 */
	@Override
	public final boolean onContextItemSelected(@NonNull final MenuItem item) {
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
					boolean success = mSelectedView.getEyePhoto().delete();
					updateEyePhotoList();

					if (!success) {
						DialogUtil.displayError(SelectTwoPicturesActivity.this,
								R.string.message_dialog_failed_to_delete_file, false, mSelectedView
										.getEyePhoto().getFilename());

					}
				}

				@Override
				public void onDialogNegativeClick(final DialogFragment dialog) {
					// Do nothing
				}
			};
			DialogUtil.displayConfirmationMessage(this, listenerDelete, R.string.button_delete,
					R.string.message_dialog_confirm_delete_photo, mSelectedView.getEyePhoto().getFilename());
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
						boolean success = FileUtil.deleteFilesInFolder(mFolder);
						updateEyePhotoList();

						if (success) {
							finish();
							mParentActivity.finish();
						}
						else {
							DialogUtil.displayError(SelectTwoPicturesActivity.this,
									R.string.message_dialog_failed_to_delete_all_files, false, mSelectedView.getEyePhoto().getFilename());
						}
					}
				}

				@Override
				public void onDialogNegativeClick(final DialogFragment dialog) {
					// Do nothing
				}
			};
			DialogUtil.displayConfirmationMessage(this, listenerDeleteAll, R.string.button_delete,
					R.string.message_dialog_confirm_delete_all_photos, mSelectedView.getEyePhoto().getFilename());
			return true;
		}
		else {
			return super.onContextItemSelected(item);
		}

	}

	/**
	 * onClick action for Button "Ok".
	 *
	 * @param view The view triggering the onClick action.
	 */
	public final void onOkClick(final View view) {
		List<EyePhoto> selectedImages = TwoImageSelectionHandler.getInstance().getSelectedImages();
		if (selectedImages.size() >= 2) {
			returnResult(selectedImages.get(0).getAbsolutePath(), selectedImages.get(1).getAbsolutePath());
		}
		else if (selectedImages.size() == 1) {
			returnResult(selectedImages.get(0).getAbsolutePath(), null);
		}
		else {
			returnResult(null, null);
		}
	}

	/**
	 * Update the list of eye photo pairs.
	 */
	private void updateEyePhotoList() {
		mGridView.setAdapter(new SelectTwoPicturesArrayAdapter(this, getEyePhotos()));
	}

	/**
	 * Gives information if the activity is started via input folder or via list of files.
	 *
	 * @return true if started via input folder.
	 */
	public final boolean isStartedWithInputFolder() {
		return mFolder != null;
	}

	/**
	 * Container for two files.
	 */
	public static final class FilePair {
		/**
		 * Constructor to create a pair of files.
		 *
		 * @param name1 The first file.
		 * @param name2 The second file.
		 */
		private FilePair(final String name1, final String name2) {
			mFile1 = name1 == null ? null : new File(name1);
			mFile2 = name2 == null ? null : new File(name2);
		}

		/**
		 * The first file stored in the container.
		 */
		private final File mFile1;
		/**
		 * The second file stored in the container.
		 */
		private final File mFile2;

		public File getFile1() {
			return mFile1;
		}

		public File getFile2() {
			return mFile2;
		}
	}
}
