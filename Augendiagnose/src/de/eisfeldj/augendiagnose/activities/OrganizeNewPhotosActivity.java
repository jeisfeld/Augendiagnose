package de.eisfeldj.augendiagnose.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.components.InstantAutoCompleteTextView;
import de.eisfeldj.augendiagnose.fragments.ListFoldersBaseFragment;
import de.eisfeldj.augendiagnose.util.DateUtil;
import de.eisfeldj.augendiagnose.util.DialogUtil;
import de.eisfeldj.augendiagnose.util.EyePhoto;
import de.eisfeldj.augendiagnose.util.EyePhoto.RightLeft;
import de.eisfeldj.augendiagnose.util.ImageUtil;
import de.eisfeldj.augendiagnose.util.MediaStoreUtil;
import de.eisfeldj.augendiagnose.util.TwoImageSelectionHandler;

/**
 * Activity to display a pair of new eye photos, choose a name and a date for them, and shift them into the
 * application's eye photo folder (with renaming)
 *
 * The activity can be started either with a folder name, or with an array of file names.
 */
public class OrganizeNewPhotosActivity extends Activity {

	/**
	 * The resource key for the input folder.
	 */
	private static final String STRING_EXTRA_INPUTFOLDER = "de.eisfeldj.augendiagnose.INPUTFOLDER";
	/**
	 * The resource key for the target (parent) folder.
	 */
	private static final String STRING_EXTRA_FOLDER = "de.eisfeldj.augendiagnose.FOLDER";
	/**
	 * The resource key for the list of input files.
	 */
	private static final String STRING_EXTRA_FILENAMES = "de.eisfeldj.augendiagnose.FILENAMES";
	/**
	 * The resource key for the flag indicating if the last picture is the right eye.
	 */
	private static final String BOOL_EXTRA_RIGHTEYELAST = "de.eisfeldj.augendiagnose.RIGHTEYELAST";

	/**
	 * The input folder for images.
	 */
	private File inputFolder;
	/**
	 * The parent output folder for images.
	 */
	private File parentFolder;
	/**
	 * The currently selected date to be given to the pictures.
	 */
	private Calendar pictureDate = Calendar.getInstance();
	/**
	 * The flag indicating if the last picture is the right eye.
	 */
	private boolean rightEyeLast;
	/**
	 * The list of input images.
	 */
	private String[] fileNames;

	/**
	 * The ImageViews displaying the eye photos.
	 */
	private ImageView imageRight, imageLeft;
	/**
	 * The EditText with the name to which the photos should be assigned.
	 */
	private InstantAutoCompleteTextView editName;
	/**
	 * The EditText with the data to which the photos should be assigned.
	 */
	private EditText editDate;
	/**
	 * The currently chosen eye photos.
	 */
	private EyePhoto photoRight, photoLeft;

	/**
	 * Static helper method to start the activity, passing the source folder, the target folder, and a flag indicating
	 * if the last picture is the right or the left eye.
	 *
	 * @param context
	 *            The context in which the activity is started.
	 * @param inputFolderName
	 *            The folder containing the input files.
	 * @param folderName
	 *            The target Folder.
	 * @param rightEyeLast
	 *            A flag indicating if the last picture is the right eye.
	 */
	public static final void startActivity(final Context context, final String inputFolderName,
			final String folderName, final boolean rightEyeLast) {
		Intent intent = new Intent(context, OrganizeNewPhotosActivity.class);
		intent.putExtra(STRING_EXTRA_INPUTFOLDER, inputFolderName);
		intent.putExtra(STRING_EXTRA_FOLDER, folderName);
		intent.putExtra(BOOL_EXTRA_RIGHTEYELAST, rightEyeLast);
		context.startActivity(intent);
	}

	/**
	 * Static helper method to start the activity, passing the list of files, the target folder, and a flag indicating
	 * if the last picture is the right or the left eye.
	 *
	 * @param context
	 *            The context in which the activity is started.
	 * @param fileNames
	 *            The list of files.
	 * @param folderName
	 *            The target folder.
	 * @param rightEyeLast
	 *            A flag indicating if the last picture is the right eye.
	 */
	public static final void startActivity(final Context context, final String[] fileNames, final String folderName,
			final boolean rightEyeLast) {
		Intent intent = new Intent(context, OrganizeNewPhotosActivity.class);
		intent.putExtra(STRING_EXTRA_FILENAMES, fileNames);
		intent.putExtra(STRING_EXTRA_FOLDER, folderName);
		intent.putExtra(BOOL_EXTRA_RIGHTEYELAST, rightEyeLast);
		context.startActivity(intent);
	}

	/*
	 * Create the activity, build the view, fill all content and add listeners.
	 */
	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_organize_new_photos);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		String inputFolderString = getIntent().getStringExtra(STRING_EXTRA_INPUTFOLDER);
		if (inputFolderString != null) {
			inputFolder = new File(getIntent().getStringExtra(STRING_EXTRA_INPUTFOLDER));
		}

		parentFolder = new File(getIntent().getStringExtra(STRING_EXTRA_FOLDER));
		rightEyeLast = getIntent().getBooleanExtra(BOOL_EXTRA_RIGHTEYELAST, true);
		fileNames = getIntent().getStringArrayExtra(STRING_EXTRA_FILENAMES);

		if (savedInstanceState != null && savedInstanceState.getString("rightEyePhoto") != null) {
			photoRight = new EyePhoto(savedInstanceState.getString("rightEyePhoto"));
			photoLeft = new EyePhoto(savedInstanceState.getString("leftEyePhoto"));
		}

		imageRight = (ImageView) findViewById(R.id.imageOrganizeRight);
		imageLeft = (ImageView) findViewById(R.id.imageOrganizeLeft);

		// when editing the "name" field, show suggestions
		editName = (InstantAutoCompleteTextView) findViewById(R.id.editName);
		editName.setAdapter(new ArrayAdapter<String>(this, R.layout.adapter_list_names, ListFoldersBaseFragment
				.getFolderNames(parentFolder)));

		// when touching the "date" field, open a dialog.
		editDate = (EditText) findViewById(R.id.editDate);
		editDate.setInputType(InputType.TYPE_NULL);
		editDate.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(final View v, final MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					openDateDialog(v);
				}
				return true;
			}
		});

		if (photoLeft == null || photoRight == null) {
			// initial fill
			setPicturesAndValues();
		}
		else {
			// only load predefined images
			updateImages();
			pictureDate.setTime(photoRight.getDate());
			editDate.setText(DateUtil.getDisplayDate(pictureDate));
			editDate.invalidate();
		}

		// Ensure that target folder exists
		if (!parentFolder.exists()) {
			boolean success = parentFolder.mkdirs();
			if (!success) {
				Log.w(Application.TAG, "Failed to create folder" + parentFolder);
			}
		}

	}

	/*
	 * Inflate options menu.
	 */
	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_only_help, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * Handle menu actions.
	 */
	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_help:
			DisplayHtmlActivity.startActivity(this, R.string.html_organize_photos);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected final void onDestroy() {
		super.onDestroy();
		TwoImageSelectionHandler.clean();
	}

	/**
	 * Helper methods to load the pictures and to preset the date (from the pictures).
	 */
	private void setPicturesAndValues() {
		File[] files;
		if (inputFolder != null) {
			// retrieve files from Input Folder
			files = inputFolder.listFiles(new ImageUtil.ImageFileFilter());

			if (files == null) {
				DialogUtil.displayError(this, R.string.message_dialog_folder_does_not_exist, true,
						inputFolder.getAbsolutePath());
				return;
			}

			// Sort files by date
			Arrays.sort(files, new Comparator<File>() {
				@Override
				public int compare(final File f1, final File f2) {
					return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
				}
			});
		}
		else {
			ArrayList<File> fileList = new ArrayList<File>();
			ArrayList<String> fileNameList = new ArrayList<String>();
			for (String fileName : fileNames) {
				File file = new File(fileName);
				if (file.exists() && file.isFile()) {
					fileList.add(file);
					fileNameList.add(fileName);
				}
			}
			files = fileList.toArray(new File[fileList.size()]);
			fileNames = fileNameList.toArray(new String[fileNameList.size()]);
		}

		if (files.length > 1) {
			EyePhoto photoLast = new EyePhoto(files[0]);
			EyePhoto photoLastButOne = new EyePhoto(files[1]);

			// Override last modified time by EXIF time
			boolean isRealLast = photoLast.getDate().compareTo(photoLastButOne.getDate()) >= 0;
			if (!isRealLast) {
				EyePhoto temp = photoLast;
				photoLast = photoLastButOne;
				photoLastButOne = temp;
			}

			// Organize left vs. right
			if (rightEyeLast) {
				photoRight = photoLast;
				photoLeft = photoLastButOne;
			}
			else {
				photoLeft = photoLast;
				photoRight = photoLastButOne;
			}

			updateImages();

			pictureDate.setTime(photoRight.getDate());
			editDate.setText(DateUtil.getDisplayDate(pictureDate));
			editDate.invalidate();
		}
		else {
			// Error message if there are less than two files
			DialogUtil.displayError(this, R.string.message_dialog_no_picture, true);
		}
	}

	/**
	 * Display the two images. As these are only two thumbnails, we do this in the main thread. Separate thread may lead
	 * to issues when returning from SelectTwoImages after orientation change
	 */
	private void updateImages() {
		imageRight.setImageBitmap(photoRight.getImageBitmap(MediaStoreUtil.MINI_THUMB_SIZE));
		imageRight.invalidate();
		imageLeft.setImageBitmap(photoLeft.getImageBitmap(MediaStoreUtil.MINI_THUMB_SIZE));
		imageLeft.invalidate();
	}

	/**
	 * Helper method to display an error message.
	 *
	 * @param resource
	 *            The resource containing the error message.
	 * @param args
	 *            The arguments of the error message.
	 */
	private void displayError(final int resource, final Object... args) {
		DialogUtil.displayError(this, resource, false, args);
	}

	/**
	 * onClick action for Button "Switch images".
	 *
	 * @param view
	 *            The view triggering the onClick action.
	 */
	public final void switchImages(final View view) {
		rightEyeLast = !rightEyeLast;
		EyePhoto temp = photoLeft;
		photoLeft = photoRight;
		photoRight = temp;
		updateImages();
	}

	/**
	 * onClick action for Button "Other photos".
	 *
	 * @param view
	 *            The view triggering the onClick action.
	 */
	public final void selectOtherPhotos(final View view) {
		if (inputFolder != null) {
			SelectTwoPicturesActivity.startActivity(this, inputFolder.getAbsolutePath());
		}
		else {
			SelectTwoPicturesActivity.startActivity(this, fileNames);
		}
	}

	/**
	 * onClick action for Button "Finish" Moves and renames the selected files.
	 *
	 * @param view
	 *            The view triggering the onClick action.
	 */
	public final void finishActivity(final View view) {
		String name = editName.getText().toString();
		if (name == null || name.length() < 1) {
			displayError(R.string.message_dialog_select_name);
			return;
		}

		Date date = new Date(pictureDate.getTimeInMillis());
		String suffixRight = photoRight.getSuffix();
		String suffixLeft = photoLeft.getSuffix();

		File targetFolder = new File(parentFolder, name);

		if (targetFolder.exists() && !targetFolder.isDirectory()) {
			displayError(R.string.message_dialog_cannot_create_folder, targetFolder.getAbsolutePath());
			return;
		}
		else if (!targetFolder.exists()) {
			boolean success = targetFolder.mkdir();
			if (!success) {
				displayError(R.string.message_dialog_cannot_create_folder, targetFolder.getAbsolutePath());
				return;
			}
		}

		EyePhoto targetPhotoRight = new EyePhoto(targetFolder.getAbsolutePath(), name, date, RightLeft.RIGHT,
				suffixRight);
		EyePhoto targetPhotoLeft = new EyePhoto(targetFolder.getAbsolutePath(), name, date, RightLeft.LEFT, suffixLeft);

		if (!photoRight.exists()) {
			displayError(R.string.message_dialog_file_does_not_exist, photoRight.getAbsolutePath());
			return;
		}
		if (!photoLeft.exists()) {
			displayError(R.string.message_dialog_file_does_not_exist, photoLeft.getAbsolutePath());
			return;
		}

		if (targetPhotoRight.exists()) {
			displayError(R.string.message_dialog_file_already_exists, targetPhotoRight.getAbsolutePath());
			return;
		}
		if (targetPhotoLeft.exists()) {
			displayError(R.string.message_dialog_file_already_exists, targetPhotoLeft.getAbsolutePath());
			return;
		}

		if (inputFolder != null) {
			// in case of input folder, move files
			if (!photoRight.moveTo(targetPhotoRight)) {
				displayError(R.string.message_dialog_failed_to_move_file, photoRight.getAbsolutePath(),
						targetPhotoRight.getAbsolutePath());
				return;
			}
			if (!photoLeft.moveTo(targetPhotoLeft)) {
				displayError(R.string.message_dialog_failed_to_move_file, photoLeft.getAbsolutePath(),
						targetPhotoLeft.getAbsolutePath());
				return;
			}
		}
		else {
			// in case of input files, copy files
			if (!photoRight.copyTo(targetPhotoRight)) {
				displayError(R.string.message_dialog_failed_to_move_file, photoRight.getAbsolutePath(),
						targetPhotoRight.getAbsolutePath());
				return;
			}
			if (!photoLeft.copyTo(targetPhotoLeft)) {
				displayError(R.string.message_dialog_failed_to_move_file, photoLeft.getAbsolutePath(),
						targetPhotoLeft.getAbsolutePath());
				return;
			}
		}

		targetPhotoRight.storeDefaultMetadata();
		targetPhotoLeft.storeDefaultMetadata();

		targetPhotoRight.addToMediaStore();
		targetPhotoLeft.addToMediaStore();

		// Store the name so that it may be opened automatically
		Application.setSharedPreferenceString(R.string.key_internal_last_name, name);
		Application.setSharedPreferenceBoolean(R.string.key_internal_organized_new_photo, true);

		finish();
	}

	/*
	 * Handle the result of a called activity - either the selection of the name or the selection of two pictures.
	 */
	@Override
	protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
		case SelectTwoPicturesActivity.REQUEST_CODE:
			SelectTwoPicturesActivity.FilePair filePair = SelectTwoPicturesActivity.getResult(resultCode, data);
			if (filePair != null) {
				photoRight = new EyePhoto(filePair.getFile1());
				photoLeft = new EyePhoto(filePair.getFile2());
				updateImages();
			}
			break;
		default:
			break;
		}
	}

	/**
	 * onClick action for date field. Opens a date picker dialog.
	 *
	 * @param view
	 *            The view triggering the onClick action.
	 */
	public final void openDateDialog(final View view) {
		DateDialogFragment fragment = new DateDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("Year", pictureDate.get(Calendar.YEAR));
		bundle.putInt("Month", pictureDate.get(Calendar.MONTH));
		bundle.putInt("Date", pictureDate.get(Calendar.DAY_OF_MONTH));
		fragment.setArguments(bundle);
		fragment.show(getFragmentManager(), DateDialogFragment.class.toString());

	}

	@Override
	protected final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		if (photoRight != null && photoLeft != null) {
			outState.putString("rightEyePhoto", photoRight.getAbsolutePath());
			outState.putString("leftEyePhoto", photoLeft.getAbsolutePath());
		}
	}

	/**
	 * Set the displayed date.
	 *
	 * @param yearSelected
	 *            The year.
	 * @param monthOfYear
	 *            The month of the year.
	 * @param dayOfMonth
	 *            The day of the month.
	 */
	public final void setDate(final int yearSelected, final int monthOfYear, final int dayOfMonth) {
		pictureDate = new GregorianCalendar(yearSelected, monthOfYear, dayOfMonth);
		editDate.setText(DateUtil.getDisplayDate(pictureDate));
		editDate.invalidate();
	}

	/**
	 * onClick action for displaying the two pictures.
	 *
	 * @param view
	 *            The view triggering the onClick action.
	 */
	public final void displayNewImages(final View view) {
		DisplayTwoActivity.startActivity(this, photoRight.getAbsolutePath(), photoLeft.getAbsolutePath());
	}

	/**
	 * onClick action - overrides other onClick action to ensure that nothing happens.
	 *
	 * @param view
	 *            The view triggering the onClick action.
	 */
	public final void doNothing(final View view) {
		// do nothing
	}

	/**
	 * Fragment for the date dialog.
	 */
	public class DateDialogFragment extends DialogFragment {
		@Override
		public final Dialog onCreateDialog(final Bundle savedInstanceState) {
			int year = getArguments().getInt("Year");
			int month = getArguments().getInt("Month");
			int date = getArguments().getInt("Date");

			DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
				@Override
				public void onDateSet(final DatePicker view, final int yearSelected, final int monthOfYear,
						final int dayOfMonth) {
					setDate(yearSelected, monthOfYear, dayOfMonth);
				}
			};
			return new DatePickerDialog(getActivity(), dateSetListener, year, month, date);
		}
	}

}
