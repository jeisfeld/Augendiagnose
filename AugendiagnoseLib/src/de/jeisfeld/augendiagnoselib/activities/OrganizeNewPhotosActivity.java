package de.jeisfeld.augendiagnoselib.activities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.Application.AuthorizationLevel;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.components.InstantAutoCompleteTextView;
import de.jeisfeld.augendiagnoselib.fragments.ListFoldersBaseFragment;
import de.jeisfeld.augendiagnoselib.util.DateUtil;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.DialogUtil.ConfirmDialogFragment.ConfirmDialogListener;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.TwoImageSelectionHandler;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto.RightLeft;
import de.jeisfeld.augendiagnoselib.util.imagefile.FileUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.ImageUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.JpegMetadataUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.MediaStoreUtil;

/**
 * Activity to display a pair of new eye photos, choose a name and a date for them, and shift them into the
 * application's eye photo folder (with renaming)
 *
 * <p>
 * The activity can be started either with a folder name, or with an array of file names.
 */
public class OrganizeNewPhotosActivity extends BaseActivity {

	/**
	 * The resource key for the input folder.
	 */
	private static final String STRING_EXTRA_INPUTFOLDER = "de.jeisfeld.augendiagnoselib.INPUTFOLDER";
	/**
	 * The resource key for the list of input files.
	 */
	private static final String STRING_EXTRA_FILENAMES = "de.jeisfeld.augendiagnoselib.FILENAMES";
	/**
	 * The resource key for the flag indicating if the last picture is the right eye.
	 */
	private static final String STRING_EXTRA_RIGHTEYELAST = "de.jeisfeld.augendiagnoselib.RIGHTEYELAST";
	/**
	 * The resource key for the next action to be done after organizing a pair of images.
	 */
	private static final String STRING_EXTRA_NEXTACTION = "de.jeisfeld.augendiagnoselib.NEXTACTION";

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
	 * The next action to be done after organizing a pair of images.
	 */
	private NextAction nextAction;
	/**
	 * The list of input images.
	 */
	private String[] fileNames;

	/**
	 * The total number of images in the input folder.
	 */
	private int totalImageCount;

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
	 * @param rightEyeLast
	 *            A flag indicating if the last picture is the right eye.
	 * @param nextAction
	 *            The next action to be done after organizing a pair of images.
	 */
	public static final void startActivity(final Context context, final String inputFolderName,
			final boolean rightEyeLast, final NextAction nextAction) {
		Intent intent = new Intent(context, OrganizeNewPhotosActivity.class);
		intent.putExtra(STRING_EXTRA_INPUTFOLDER, inputFolderName);
		intent.putExtra(STRING_EXTRA_RIGHTEYELAST, rightEyeLast);
		intent.putExtra(STRING_EXTRA_NEXTACTION, nextAction);
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
	 * @param rightEyeLast
	 *            A flag indicating if the last picture is the right eye.
	 * @param nextAction
	 *            The next action to be done after organizing a pair of images.
	 */
	public static final void startActivity(final Context context, final String[] fileNames,
			final boolean rightEyeLast, final NextAction nextAction) {
		Intent intent = new Intent(context, OrganizeNewPhotosActivity.class);
		intent.putExtra(STRING_EXTRA_FILENAMES, fileNames);
		intent.putExtra(STRING_EXTRA_RIGHTEYELAST, rightEyeLast);
		intent.putExtra(STRING_EXTRA_NEXTACTION, nextAction);
		context.startActivity(intent);
	}

	/*
	 * Create the activity, build the view, fill all content and add listeners.
	 */
	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_organize_new_photos);

		String inputFolderString = getIntent().getStringExtra(STRING_EXTRA_INPUTFOLDER);
		if (inputFolderString != null) {
			inputFolder = new File(inputFolderString);
		}

		parentFolder = new File(PreferenceUtil.getSharedPreferenceString(R.string.key_folder_photos));
		rightEyeLast = getIntent().getBooleanExtra(STRING_EXTRA_RIGHTEYELAST, false);
		nextAction = (NextAction) getIntent().getSerializableExtra(STRING_EXTRA_NEXTACTION);
		fileNames = getIntent().getStringArrayExtra(STRING_EXTRA_FILENAMES);

		if (savedInstanceState != null && savedInstanceState.getString("rightEyePhoto") != null) {
			photoRight = new EyePhoto(savedInstanceState.getString("rightEyePhoto"));
			photoLeft = new EyePhoto(savedInstanceState.getString("leftEyePhoto"));
			totalImageCount = savedInstanceState.getInt("totalImageCount");
		}

		imageRight = (ImageView) findViewById(R.id.imageOrganizeRight);
		imageLeft = (ImageView) findViewById(R.id.imageOrganizeLeft);

		// when editing the "name" field, show suggestions
		editName = (InstantAutoCompleteTextView) findViewById(R.id.editName);
		editName.setAdapter(new ArrayAdapter<String>(this, R.layout.adapter_list_names, ListFoldersBaseFragment
				.getFolderNames(parentFolder)));
		// Ensure that Keyboard "ok" click already triggers next step.
		editName.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					onOkClick(v);
					return true;
				}
				return false;
			}
		});

		// when touching the "date" field, open a dialog.
		editDate = (EditText) findViewById(R.id.editDate);
		editDate.setInputType(InputType.TYPE_NULL);
		editDate.setOnTouchListener(new View.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
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
			setPicturesAndValues(false);
		}
		else {
			// only load predefined images
			updateImages(true);
		}

		// Ensure that target folder exists
		if (!parentFolder.exists()) {
			boolean success = parentFolder.mkdirs();
			if (!success) {
				Log.w(Application.TAG, "Failed to create folder" + parentFolder);
			}
		}

		// Set on-click action for selecting other pictures and for cancelling activity.
		Button buttonOtherPictures = (Button) findViewById(R.id.buttonOrganizeOtherPictures);
		Button buttonCancel = (Button) findViewById(R.id.buttonOrganizeCancel);

		if (totalImageCount == 2 && inputFolder != null) {
			buttonOtherPictures.setText(getString(R.string.button_camera));
			buttonOtherPictures.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					CameraActivity.startActivity(OrganizeNewPhotosActivity.this, photoRight.getAbsolutePath(), photoLeft.getAbsolutePath());
					finish();
				}
			});

			buttonCancel.setText(getString(R.string.button_delete));
			buttonCancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					ConfirmDialogListener listenerDelete = new ConfirmDialogListener() {
						/**
						 * The serial version id.
						 */
						private static final long serialVersionUID = 1L;

						@Override
						public void onDialogPositiveClick(final DialogFragment dialog) {
							// delete images
							photoLeft.delete();
							photoRight.delete();
							finish();
						}

						@Override
						public void onDialogNegativeClick(final DialogFragment dialog) {
							finish();
						}
					};
					DialogUtil.displayConfirmationMessage(OrganizeNewPhotosActivity.this, listenerDelete, R.string.button_delete,
							R.string.message_dialog_confirm_delete_two_photos);
				}
			});
		}
		else {
			buttonOtherPictures.setText(getString(R.string.button_other_pictures));
			buttonOtherPictures.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					if (inputFolder != null) {
						SelectTwoPicturesActivity.startActivity(OrganizeNewPhotosActivity.this, inputFolder.getAbsolutePath());
					}
					else {
						SelectTwoPicturesActivity.startActivity(OrganizeNewPhotosActivity.this, fileNames);
					}
				}
			});

			buttonCancel.setText(getString(R.string.button_cancel));
			buttonCancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					finish();
				}
			});
		}

		if (savedInstanceState == null) {
			PreferenceUtil.incrementCounter(R.string.key_statistics_countorganizestart);
		}

		DialogUtil.displayTip(this, R.string.message_tip_organizephotos, R.string.key_tip_organizephotos);
	}

	@Override
	protected final int getHelpResource() {
		return R.string.html_organize_photos;
	}

	@Override
	protected final void onDestroy() {
		super.onDestroy();
		TwoImageSelectionHandler.clean();
	}

	/**
	 * Helper methods to load the pictures and to preset the date (from the pictures).
	 *
	 * @param update
	 *            Value true means that values are not initially filled, but updated after organizing an eye photo pair.
	 */
	private void setPicturesAndValues(final boolean update) {
		File[] files;
		if (inputFolder != null) {
			// retrieve files from Input Folder
			files = inputFolder.listFiles(new ImageUtil.ImageFileFilter());

			if (files == null) {
				if (update) {
					finish();
				}
				else {
					DialogUtil.displayError(this, R.string.message_dialog_no_picture, true);
				}
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
					if (!update || (!photoLeft.getAbsolutePath().equals(file.getAbsolutePath())
							&& !photoRight.getAbsolutePath().equals(file.getAbsolutePath()))) {
						fileList.add(file);
						fileNameList.add(fileName);
					}
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
			if (photoLast.getRightLeft() == RightLeft.RIGHT && photoLastButOne.getRightLeft() == RightLeft.LEFT) {
				photoRight = photoLast;
				photoLeft = photoLastButOne;
			}
			else if (photoLast.getRightLeft() == RightLeft.LEFT && photoLastButOne.getRightLeft() == RightLeft.RIGHT) {
				photoLeft = photoLast;
				photoRight = photoLastButOne;
			}
			else if (rightEyeLast) {
				photoRight = photoLast;
				photoLeft = photoLastButOne;
			}
			else {
				photoLeft = photoLast;
				photoRight = photoLastButOne;
			}

			updateImages(true);
		}
		else {
			if (update) {
				finish();
			}
			else {
				// Error message if there are less than two files
				DialogUtil.displayError(this, R.string.message_dialog_no_picture, true);
			}
		}

		totalImageCount = files.length;
	}

	/**
	 * Display the two images. As these are only two thumbnails, we do this in the main thread. Separate thread may lead
	 * to issues when returning from SelectTwoImages after orientation change
	 *
	 * @param updateDate
	 *            if true, then the date will be updated from the images.
	 */
	private void updateImages(final boolean updateDate) {
		imageRight.setImageBitmap(photoRight.getImageBitmap(MediaStoreUtil.MINI_THUMB_SIZE));
		imageRight.invalidate();
		imageLeft.setImageBitmap(photoLeft.getImageBitmap(MediaStoreUtil.MINI_THUMB_SIZE));
		imageLeft.invalidate();

		if (updateDate) {
			pictureDate.setTime(photoRight.getDate());
			editDate.setText(DateUtil.getDisplayDate(pictureDate));
			editDate.invalidate();
		}
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
		updateImages(false);
	}

	/**
	 * onClick action for Button "Ok". Moves and renames the selected files after making JPG validation.
	 *
	 * @param view
	 *            The view triggering the onClick action.
	 */
	public final void onOkClick(final View view) {
		final String name = editName.getText().toString().trim();
		if (name == null || name.length() < 1) {
			displayError(R.string.message_dialog_select_name);
			return;
		}

		List<String> existingNames = ListFoldersBaseFragment.getFolderNames(parentFolder);
		if (Application.getAuthorizationLevel() == AuthorizationLevel.TRIAL_ACCESS && !existingNames.contains(name)
				&& existingNames.size() >= ListFoldersBaseFragment.TRIAL_MAX_NAMES) {
			// Error due to trial version.
			DialogUtil.displayAuthorizationError(this, R.string.message_dialog_trial_names);
		}
		else {
			validateAndMovePhotos();
		}
	}

	/**
	 * Move and rename the selected files after making JPG validation.
	 *
	 */
	public final void validateAndMovePhotos() {
		final String name = editName.getText().toString();
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
			boolean success = FileUtil.mkdir(targetFolder);
			if (!success) {
				displayError(R.string.message_dialog_cannot_create_folder, targetFolder.getAbsolutePath());
				return;
			}
		}

		final EyePhoto targetPhotoRight = new EyePhoto(targetFolder.getAbsolutePath(), name, date, RightLeft.RIGHT,
				suffixRight);
		final EyePhoto targetPhotoLeft =
				new EyePhoto(targetFolder.getAbsolutePath(), name, date, RightLeft.LEFT, suffixLeft);

		try {
			JpegMetadataUtil.checkJpeg(photoRight.getAbsolutePath());
			JpegMetadataUtil.checkJpeg(photoLeft.getAbsolutePath());
		}
		catch (IOException e) {
			ConfirmDialogListener confirmationListener = new ConfirmDialogListener() {
				/**
				 * The serial version id.
				 */
				private static final long serialVersionUID = -3186094978749077352L;

				@Override
				public void onDialogPositiveClick(final DialogFragment dialog) {
					movePhotos(targetPhotoRight, targetPhotoLeft, name);
				}

				@Override
				public void onDialogNegativeClick(final DialogFragment dialog) {
					// Do nothing
				}
			};

			DialogUtil.displayConfirmationMessage(this, confirmationListener, R.string.button_move,
					R.string.message_dialog_confirm_no_jpeg);
			return;
		}

		if (!photoRight.exists()) {
			displayError(R.string.message_dialog_file_does_not_exist, photoRight.getAbsolutePath());
			return;
		}
		if (!photoLeft.exists()) {
			displayError(R.string.message_dialog_file_does_not_exist, photoLeft.getAbsolutePath());
			return;
		}

		if (targetPhotoRight.exists() || targetPhotoLeft.exists()) {
			DialogUtil.displayConfirmationMessage(this, new ConfirmDialogListener() {
				/**
				 * The serial version id.
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void onDialogPositiveClick(final DialogFragment dialog) {
					MediaStoreUtil.deleteThumbnail(targetPhotoLeft.getAbsolutePath());
					MediaStoreUtil.deleteThumbnail(targetPhotoRight.getAbsolutePath());
					movePhotos(targetPhotoRight, targetPhotoLeft, name);
				}

				@Override
				public void onDialogNegativeClick(final DialogFragment dialog) {
					// Do nothing
				}
			}, R.string.button_overwrite, R.string.message_dialog_confirm_overwrite, name, DateUtil.format(date));
			return;
		}

		movePhotos(targetPhotoRight, targetPhotoLeft, name);
	}

	/**
	 * Move and rename the selected files.
	 *
	 * @param targetPhotoRight
	 *            The right eye photo.
	 * @param targetPhotoLeft
	 *            The left eye photo.
	 * @param name
	 *            The selected name.
	 */
	private void movePhotos(final EyePhoto targetPhotoRight, final EyePhoto targetPhotoLeft, final String name) {
		if (inputFolder != null) {
			// in case of input folder, move files
			if (!photoRight.moveTo(targetPhotoRight, true)) {
				displayError(R.string.message_dialog_failed_to_move_file, photoRight.getAbsolutePath(),
						targetPhotoRight.getAbsolutePath());
				return;
			}
			if (!photoLeft.moveTo(targetPhotoLeft, true)) {
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
		PreferenceUtil.setSharedPreferenceString(R.string.key_internal_last_name, name);
		PreferenceUtil.setSharedPreferenceBoolean(R.string.key_internal_organized_new_photo, true);

		PreferenceUtil.incrementCounter(R.string.key_statistics_countorganizeend);

		switch (nextAction) {
		case NEXT_IMAGES:
			setPicturesAndValues(true);
			break;
		case FINISH:
			finish();
			break;
		case VIEW_IMAGES:
			ListFoldersForDisplayActivity.startActivity(this);
			finish();
			break;
		default:
			break;
		}
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
				updateImages(true);
			}
			else {
				setPicturesAndValues(true);
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
		Bundle bundle = new Bundle();
		bundle.putInt("Year", pictureDate.get(Calendar.YEAR));
		bundle.putInt("Month", pictureDate.get(Calendar.MONTH));
		bundle.putInt("Date", pictureDate.get(Calendar.DAY_OF_MONTH));
		DateDialogFragment fragment = new DateDialogFragment();
		fragment.setArguments(bundle);
		fragment.show(getFragmentManager(), DateDialogFragment.class.toString());

	}

	@Override
	protected final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		if (photoRight != null && photoLeft != null) {
			outState.putString("rightEyePhoto", photoRight.getAbsolutePath());
			outState.putString("leftEyePhoto", photoLeft.getAbsolutePath());
			outState.putInt("totalImageCount", totalImageCount);
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
		DisplayTwoActivity.startActivity(this, photoRight.getAbsolutePath(), photoLeft.getAbsolutePath(), true);
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

	/**
	 * The next action to be done after organizing photos.
	 */
	public enum NextAction {
		/**
		 * Continue the activity with the next image pair (and finish if not existing).
		 */
		NEXT_IMAGES,
		/**
		 * Finish the activity.
		 */
		FINISH,
		/**
		 * Continue with viewing images.
		 */
		VIEW_IMAGES
	}

}
