package de.jeisfeld.augendiagnoselib.activities;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
	 * Static helper method to start the activity, passing the path of the folder.
	 *
	 * @param activity   The activity starting this activity.
	 * @param foldername The image folder.
	 */
	public static void startActivity(@NonNull final OrganizeNewPhotosActivity activity, final String foldername) {
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
		Intent intent = new Intent(activity, SelectTwoPicturesActivity.class);
		intent.putExtra(STRING_EXTRA_FILENAMES, fileNames);
		activity.startActivityForResult(intent, REQUEST_CODE);
	}

	/**
	 * Static helper method to extract the first selected filename from the activity response.
	 *
	 * @param resultCode The result code indicating if the response was successful.
	 * @param data       The activity response data.
	 * @return The returned file name.
	 */
	public static String getResultFile1(final int resultCode, @NonNull final Intent data) {
		if (resultCode == RESULT_OK) {
			Bundle res = data.getExtras();
			return res.getString(STRING_RESULT_FILENAME1);
		}
		else {
			return null;
		}
	}

	/**
	 * Static helper method to extract the second selected filename from the activity response.
	 *
	 * @param resultCode The result code indicating if the response was successful.
	 * @param data       The activity response data.
	 * @return The returned file name.
	 */
	public static String getResultFile2(final int resultCode, @NonNull final Intent data) {
		if (resultCode == RESULT_OK) {
			Bundle res = data.getExtras();
			return res.getString(STRING_RESULT_FILENAME2);
		}
		else {
			return null;
		}
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_select_two_pictures);

		String folderName = getIntent().getStringExtra(STRING_EXTRA_FOLDER);
		if (folderName != null) {
			mFolder = new File(folderName);
		}
		mFileNames = getIntent().getStringArrayExtra(STRING_EXTRA_FILENAMES);

		// Prepare the view
		mGridView = (GridView) findViewById(R.id.gridViewSelectTwoPictures);
		mGridView.setAdapter(new SelectTwoPicturesArrayAdapter(this, getEyePhotos()));

		displayButtons(TwoImageSelectionHandler.getInstance().getSelectedImages().size() > 0);
	}

	@Override
	protected final void onDestroy() {
		super.onDestroy();
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

					if (success) {
						if (TwoImageSelectionHandler.getInstance().getSelectedImages().contains(mSelectedView.getEyePhoto())) {
							TwoImageSelectionHandler.getInstance().deselectView(mSelectedView);
						}
					}
					else {
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
							setResult(OrganizeNewPhotosActivity.RESULT_FINISH);
							finish();
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
	 * onClick action for Button "Preview".
	 *
	 * @param view The view triggering the onClick action.
	 */
	public final void onPreviewClick(final View view) {
		List<EyePhoto> selectedImages = TwoImageSelectionHandler.getInstance().getSelectedImages();
		if (selectedImages.size() >= 2) {
			DisplayTwoActivity.startActivity(this, selectedImages.get(0).getAbsolutePath(), selectedImages.get(1).getAbsolutePath(), false);
		}
		else if (selectedImages.size() == 1) {
			DisplayOneActivity.startActivity(this, selectedImages.get(0).getAbsolutePath());
		}
	}

	/**
	 * onClick action for Button "Apply Selection".
	 *
	 * @param view The view triggering the onClick action.
	 */
	public final void onSelectClick(final View view) {
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
	 * Display or hide the activity buttons.
	 *
	 * @param display true will display teh buttons, false will hide them.
	 */
	public final void displayButtons(final boolean display) {
		findViewById(R.id.layoutSelectTwoButtons).setVisibility(display ? View.VISIBLE : View.GONE);
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
}
