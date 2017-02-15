package de.jeisfeld.augendiagnoselib.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.Application.AuthorizationLevel;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.components.InstantAutoCompleteTextView;
import de.jeisfeld.augendiagnoselib.fragments.ListFoldersBaseFragment;
import de.jeisfeld.augendiagnoselib.util.DateUtil;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.DialogUtil.ConfirmDialogFragment.ConfirmDialogListener;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.TrackingUtil;
import de.jeisfeld.augendiagnoselib.util.TrackingUtil.Category;
import de.jeisfeld.augendiagnoselib.util.TwoImageSelectionHandler;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto.RightLeft;
import de.jeisfeld.augendiagnoselib.util.imagefile.FileUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.ImageUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.JpegMetadataUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.MediaStoreUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.PupilAndIrisDetector;

/**
 * Activity to display a pair of new eye photos, choose a name and a date for them, and shift them into the
 * application's eye photo folder (with renaming).
 *
 * <p>The activity can be started either with a folder name, or with an array of file names.
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
	 * Result code passed to indicate that the activity should be finished.
	 */
	public static final int RESULT_FINISH = 2;

	/**
	 * The input folder for images.
	 */
	private File mInputFolder;
	/**
	 * The parent output folder for images.
	 */
	@Nullable
	private File mParentFolder;
	/**
	 * The currently selected date to be given to the pictures.
	 */
	private Calendar mPictureDate = Calendar.getInstance();
	/**
	 * The flag indicating if the last picture is the right eye.
	 */
	private boolean mRightEyeLast;
	/**
	 * The next action to be done after organizing a pair of images.
	 */
	private NextAction mNextAction;
	/**
	 * The list of input images.
	 */
	private String[] mFileNames;

	/**
	 * The total number of images in the input folder.
	 */
	private int mTotalImageCount;

	/**
	 * The ImageViews displaying the eye photos.
	 */
	private ImageView mImageRight, mImageLeft;
	/**
	 * The EditText with the name to which the photos should be assigned.
	 */
	private InstantAutoCompleteTextView mEditName;
	/**
	 * The EditText with the data to which the photos should be assigned.
	 */
	private EditText mEditDate;
	/**
	 * The currently chosen eye photos.
	 */
	@Nullable
	private EyePhoto mPhotoRight, mPhotoLeft;

	/**
	 * Static helper method to start the activity, passing the source folder, the target folder, and a flag indicating
	 * if the last picture is the right or the left eye.
	 *
	 * @param context         The context in which the activity is started.
	 * @param inputFolderName The folder containing the input files.
	 * @param rightEyeLast    A flag indicating if the last picture is the right eye.
	 * @param nextAction      The next action to be done after organizing a pair of images.
	 */
	public static void startActivity(@NonNull final Context context, final String inputFolderName,
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
	 * @param context      The context in which the activity is started.
	 * @param fileNames    The list of files.
	 * @param rightEyeLast A flag indicating if the last picture is the right eye.
	 * @param nextAction   The next action to be done after organizing a pair of images.
	 */
	public static void startActivity(@NonNull final Context context, final String[] fileNames,
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
	protected final void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_organize_new_photos);

		String inputFolderString = getIntent().getStringExtra(STRING_EXTRA_INPUTFOLDER);
		if (inputFolderString != null) {
			mInputFolder = new File(inputFolderString);
		}

		mParentFolder = new File(PreferenceUtil.getSharedPreferenceString(R.string.key_folder_photos));
		mRightEyeLast = getIntent().getBooleanExtra(STRING_EXTRA_RIGHTEYELAST, false);
		mNextAction = (NextAction) getIntent().getSerializableExtra(STRING_EXTRA_NEXTACTION);
		mFileNames = getIntent().getStringArrayExtra(STRING_EXTRA_FILENAMES);

		if (savedInstanceState != null) {
			mTotalImageCount = savedInstanceState.getInt("totalImageCount");
			String rightEyePath = savedInstanceState.getString("rightEyePhoto");
			mPhotoRight = rightEyePath == null ? null : new EyePhoto(rightEyePath);
			String leftEyePath = savedInstanceState.getString("leftEyePhoto");
			mPhotoLeft = leftEyePath == null ? null : new EyePhoto(leftEyePath);
		}

		mImageRight = (ImageView) findViewById(R.id.imageOrganizeRight);
		mImageLeft = (ImageView) findViewById(R.id.imageOrganizeLeft);

		// when editing the "name" field, show suggestions
		mEditName = (InstantAutoCompleteTextView) findViewById(R.id.editName);
		mEditName.setAdapter(new ArrayAdapter<>(this, R.layout.adapter_list_names, ListFoldersBaseFragment
				.getFolderNames(mParentFolder)));
		// Ensure that Keyboard "ok" click already triggers next step.
		mEditName.setOnEditorActionListener(new OnEditorActionListener() {
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
		mEditDate = (EditText) findViewById(R.id.editDate);
		mEditDate.setInputType(InputType.TYPE_NULL);
		mEditDate.setOnTouchListener(new View.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(final View v, @NonNull final MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					openDateDialog(v);
				}
				return true;
			}
		});

		if (mPhotoLeft == null && mPhotoRight == null) {
			// initial fill
			setPicturesAndValues(false);
		}
		else {
			// only load predefined images
			updateImages(true);
		}

		// Ensure that target folder exists
		if (!mParentFolder.exists()) {
			boolean success = mParentFolder.mkdirs();
			if (!success) {
				Log.w(Application.TAG, "Failed to create folder" + mParentFolder);
			}
		}

		configureButtons();

		if (getString(R.string.section_select_photos).length() == 0) {
			findViewById(R.id.textViewSelectPhotos).setVisibility(View.GONE);
		}

		if (savedInstanceState == null) {
			PreferenceUtil.incrementCounter(R.string.key_statistics_countorganizestart);
			TrackingUtil.sendEvent(Category.EVENT_USER, "Organize", "Start");
		}

		DialogUtil.displayTip(this, R.string.message_tip_organizephotos, R.string.key_tip_organizephotos);
	}

	/**
	 * Configure the buttons "Cancel" and "Other Pictures".
	 */
	private void configureButtons() {
		// Set on-click action for selecting other pictures and for cancelling activity.
		Button buttonOtherPictures = (Button) findViewById(R.id.buttonOrganizeOtherPictures);
		Button buttonCancel = (Button) findViewById(R.id.buttonOrganizeCancel);

		if (getResources().getBoolean(R.bool.flag_overwrite_by_camera_from_organize)) {
			buttonOtherPictures.setText(getString(R.string.button_camera));
			buttonOtherPictures.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					CameraActivity.startActivity(OrganizeNewPhotosActivity.this,
							mPhotoRight == null ? null : mPhotoRight.getAbsolutePath(),
							mPhotoLeft == null ? null : mPhotoLeft.getAbsolutePath());
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
							if (mPhotoLeft != null) {
								mPhotoLeft.delete();
							}
							if (mPhotoRight != null) {
								mPhotoRight.delete();
							}
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
					if (mInputFolder != null) {
						SelectTwoPicturesActivity.startActivity(OrganizeNewPhotosActivity.this, mInputFolder.getAbsolutePath());
					}
					else {
						SelectTwoPicturesActivity.startActivity(OrganizeNewPhotosActivity.this, mFileNames);
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
	 * @param update Value true means that values are not initially filled, but updated after organizing an eye photo pair.
	 */
	private void setPicturesAndValues(final boolean update) {
		File[] files;
		if (mInputFolder != null) {
			// retrieve files from Input Folder
			files = mInputFolder.listFiles(new ImageUtil.ImageFileFilter());

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
				public int compare(@NonNull final File f1, @NonNull final File f2) {
					return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
				}
			});
		}
		else {
			ArrayList<File> fileList = new ArrayList<>();
			ArrayList<String> fileNameList = new ArrayList<>();

			for (String fileName : mFileNames) {
				File file = new File(fileName);
				if (file.exists() && file.isFile()) {
					if (!update || !isCurrentlyDisplayed(file)) {
						fileList.add(file);
						fileNameList.add(fileName);
					}
				}
			}

			files = fileList.toArray(new File[fileList.size()]);
			mFileNames = fileNameList.toArray(new String[fileNameList.size()]);
		}

		if (files.length > 0) {
			EyePhoto photoLast = new EyePhoto(files[0]);
			EyePhoto photoLastButOne = files.length == 1 ? null : new EyePhoto(files[1]);

			// Override last modified time by EXIF time
			if (photoLastButOne != null) {
				boolean isRealLast = photoLast.getDate().compareTo(photoLastButOne.getDate()) >= 0;
				if (!isRealLast) {
					EyePhoto temp = photoLast;
					photoLast = photoLastButOne;
					photoLastButOne = temp;
				}
			}

			// Organize left vs. right
			if (photoLast.getRightLeft() == RightLeft.RIGHT) {
				mPhotoRight = photoLast;
				mPhotoLeft = photoLastButOne;
			}
			else if (photoLast.getRightLeft() == RightLeft.LEFT) {
				mPhotoLeft = photoLast;
				mPhotoRight = photoLastButOne;
			}
			else if (mRightEyeLast) {
				mPhotoRight = photoLast;
				mPhotoLeft = photoLastButOne;
			}
			else {
				mPhotoLeft = photoLast;
				mPhotoRight = photoLastButOne;
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

		mTotalImageCount = files.length;
	}

	/**
	 * Check if a file is currently displayed as right or left eye.
	 *
	 * @param file the file.
	 * @return true if it is displayed.
	 */
	private boolean isCurrentlyDisplayed(final File file) {
		return (mPhotoRight != null && mPhotoRight.getAbsolutePath().equals(file.getAbsolutePath()))
				|| (mPhotoLeft != null && mPhotoLeft.getAbsolutePath().equals(file.getAbsolutePath()));
	}

	/**
	 * Display the two images. As these are only two thumbnails, we do this in the main thread. Separate thread may lead
	 * to issues when returning from SelectTwoImages after orientation change
	 *
	 * @param updateDate if true, then the date will be updated from the images.
	 */
	private void updateImages(final boolean updateDate) {
		if (mPhotoRight == null) {
			mImageRight.setImageResource(android.R.color.transparent);
		}
		else {
			mImageRight.setImageBitmap(mPhotoRight.getImageBitmap(MediaStoreUtil.MINI_THUMB_SIZE));
			mImageRight.invalidate();
		}
		if (mPhotoLeft == null) {
			mImageLeft.setImageResource(android.R.color.transparent);
		}
		else {
			mImageLeft.setImageBitmap(mPhotoLeft.getImageBitmap(MediaStoreUtil.MINI_THUMB_SIZE));
			mImageLeft.invalidate();
		}

		if (updateDate) {
			Date date = mPhotoRight == null ? mPhotoLeft.getDate() : mPhotoRight.getDate();
			mPictureDate.setTime(date);
			mEditDate.setText(DateUtil.getDisplayDate(mPictureDate));
			mEditDate.invalidate();
		}

		if (mInputFolder != null) {
			PupilAndIrisDetector.determineAndStoreIrisPosition(mPhotoRight);
			PupilAndIrisDetector.determineAndStoreIrisPosition(mPhotoLeft);
		}
	}

	/**
	 * Helper method to display an error message.
	 *
	 * @param resource The resource containing the error message.
	 * @param args     The arguments of the error message.
	 */
	private void displayError(final int resource, final Object... args) {
		DialogUtil.displayError(this, resource, false, args);
	}

	/**
	 * onClick action for Button "Switch images".
	 *
	 * @param view The view triggering the onClick action.
	 */
	public final void switchImages(final View view) {
		mRightEyeLast = !mRightEyeLast;
		EyePhoto temp = mPhotoLeft;
		mPhotoLeft = mPhotoRight;
		mPhotoRight = temp;
		updateImages(false);
	}

	/**
	 * onClick action for Button "Ok". Moves and renames the selected files after making JPG validation.
	 *
	 * @param view The view triggering the onClick action.
	 */
	public final void onOkClick(final View view) {
		final String name = mEditName.getText().toString().trim();
		if (name.length() < 1) {
			displayError(R.string.message_dialog_select_name);
			return;
		}

		List<String> existingNames = ListFoldersBaseFragment.getFolderNames(mParentFolder);
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
	 */
	private void validateAndMovePhotos() {
		final String name = mEditName.getText().toString();
		if (name.length() < 1) {
			displayError(R.string.message_dialog_select_name);
			return;
		}

		Date date = new Date(mPictureDate.getTimeInMillis());

		File targetFolder = new File(mParentFolder, name);

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

		final EyePhoto targetPhotoRight =
				mPhotoRight == null ? null : new EyePhoto(targetFolder.getAbsolutePath(), name, date, RightLeft.RIGHT, mPhotoRight.getSuffix());
		final EyePhoto targetPhotoLeft =
				mPhotoLeft == null ? null : new EyePhoto(targetFolder.getAbsolutePath(), name, date, RightLeft.LEFT, mPhotoLeft.getSuffix());

		try {
			if (mPhotoRight != null) {
				JpegMetadataUtil.checkJpeg(mPhotoRight.getAbsolutePath());
			}
			if (mPhotoLeft != null) {
				JpegMetadataUtil.checkJpeg(mPhotoLeft.getAbsolutePath());
			}
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

		if (mPhotoRight != null && !mPhotoRight.exists()) {
			displayError(R.string.message_dialog_file_does_not_exist, mPhotoRight.getAbsolutePath());
			return;
		}
		if (mPhotoLeft != null && !mPhotoLeft.exists()) {
			displayError(R.string.message_dialog_file_does_not_exist, mPhotoLeft.getAbsolutePath());
			return;
		}

		if ((targetPhotoRight != null && targetPhotoRight.exists()) || (targetPhotoLeft != null && targetPhotoLeft.exists())) {
			DialogUtil.displayConfirmationMessage(this, new ConfirmDialogListener() {
				/**
				 * The serial version id.
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void onDialogPositiveClick(final DialogFragment dialog) {
					if (targetPhotoRight != null && targetPhotoRight.exists()) {
						MediaStoreUtil.deleteThumbnail(targetPhotoRight.getAbsolutePath());
					}
					if (targetPhotoLeft != null && targetPhotoLeft.exists()) {
						MediaStoreUtil.deleteThumbnail(targetPhotoLeft.getAbsolutePath());
					}
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

		if (mInputFolder == null) {
			PupilAndIrisDetector.determineAndStoreIrisPosition(targetPhotoRight);
			PupilAndIrisDetector.determineAndStoreIrisPosition(targetPhotoLeft);
		}
	}

	/**
	 * Move and rename the selected files.
	 *
	 * @param targetPhotoRight The right eye photo.
	 * @param targetPhotoLeft  The left eye photo.
	 * @param name             The selected name.
	 */
	private void movePhotos(final EyePhoto targetPhotoRight, final EyePhoto targetPhotoLeft, final String name) {
		if (mInputFolder != null) {
			// in case of input folder, move files
			if (mPhotoRight != null && !mPhotoRight.moveTo(targetPhotoRight, true)) {
				displayError(R.string.message_dialog_failed_to_move_file, mPhotoRight.getAbsolutePath(),
						targetPhotoRight.getAbsolutePath());
				return;
			}
			if (mPhotoLeft != null && !mPhotoLeft.moveTo(targetPhotoLeft, true)) {
				displayError(R.string.message_dialog_failed_to_move_file, mPhotoLeft.getAbsolutePath(),
						targetPhotoLeft.getAbsolutePath());
				return;
			}
		}
		else {
			// in case of input files, copy files
			if (mPhotoRight != null && !mPhotoRight.copyTo(targetPhotoRight)) {
				displayError(R.string.message_dialog_failed_to_move_file, mPhotoRight.getAbsolutePath(),
						targetPhotoRight.getAbsolutePath());
				return;
			}
			if (mPhotoLeft != null && !mPhotoLeft.copyTo(targetPhotoLeft)) {
				displayError(R.string.message_dialog_failed_to_move_file, mPhotoLeft.getAbsolutePath(),
						targetPhotoLeft.getAbsolutePath());
				return;
			}
		}

		if (mPhotoRight != null && targetPhotoRight != null) {
			targetPhotoRight.storeDefaultMetadata();
			targetPhotoRight.addToMediaStore();
		}
		if (mPhotoLeft != null && targetPhotoLeft != null) {
			targetPhotoLeft.storeDefaultMetadata();
			targetPhotoLeft.addToMediaStore();
		}

		// Store the name so that it may be opened automatically
		PreferenceUtil.setSharedPreferenceString(R.string.key_internal_last_name, name);
		PreferenceUtil.setSharedPreferenceBoolean(R.string.key_internal_organized_new_photo, true);

		PreferenceUtil.incrementCounter(R.string.key_statistics_countorganizeend);
		TrackingUtil.sendEvent(Category.EVENT_USER, "Organize", "End");

		switch (mNextAction) {
		case NEXT_IMAGES:
			setPicturesAndValues(true);
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
			if (resultCode == RESULT_FINISH) {
				finish();
				return;
			}
			String fileName1 = SelectTwoPicturesActivity.getResultFile1(resultCode, data);
			String fileName2 = SelectTwoPicturesActivity.getResultFile2(resultCode, data);
			if (fileName1 == null && fileName2 == null) {
				setPicturesAndValues(true);
			}
			else {
				mPhotoRight = fileName1 == null ? null : new EyePhoto(fileName1);
				mPhotoLeft = fileName2 == null ? null : new EyePhoto(fileName2);
				updateImages(true);
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	/**
	 * onClick action for date field. Opens a date picker dialog.
	 *
	 * @param view The view triggering the onClick action.
	 */
	private void openDateDialog(final View view) {
		Bundle bundle = new Bundle();
		bundle.putInt("Year", mPictureDate.get(Calendar.YEAR));
		bundle.putInt("Month", mPictureDate.get(Calendar.MONTH));
		bundle.putInt("Date", mPictureDate.get(Calendar.DAY_OF_MONTH));
		DateDialogFragment fragment = new DateDialogFragment();
		fragment.setArguments(bundle);
		fragment.show(getFragmentManager(), DateDialogFragment.class.toString());

	}

	@Override
	protected final void onSaveInstanceState(@NonNull final Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mPhotoRight != null || mPhotoLeft != null) {
			outState.putInt("totalImageCount", mTotalImageCount);
			if (mPhotoRight != null) {
				outState.putString("rightEyePhoto", mPhotoRight.getAbsolutePath());
			}
			if (mPhotoLeft != null) {
				outState.putString("leftEyePhoto", mPhotoLeft.getAbsolutePath());
			}
		}
	}

	/**
	 * Set the displayed date.
	 *
	 * @param yearSelected The year.
	 * @param monthOfYear  The month of the year.
	 * @param dayOfMonth   The day of the month.
	 */
	private void setDate(final int yearSelected, final int monthOfYear, final int dayOfMonth) {
		mPictureDate = new GregorianCalendar(yearSelected, monthOfYear, dayOfMonth);
		mEditDate.setText(DateUtil.getDisplayDate(mPictureDate));
		mEditDate.invalidate();
	}

	/**
	 * onClick action for displaying the two pictures.
	 *
	 * @param view The view triggering the onClick action.
	 */
	public final void displayNewImages(final View view) {
		if (mPhotoRight != null && mPhotoLeft != null) {
			DisplayTwoActivity.startActivity(this, mPhotoRight.getAbsolutePath(), mPhotoLeft.getAbsolutePath(), true);
		}
		else if (mPhotoRight != null) {
			DisplayOneActivity.startActivity(this, mPhotoRight.getAbsolutePath());
		}
		else if (mPhotoLeft != null) {
			DisplayOneActivity.startActivity(this, mPhotoLeft.getAbsolutePath());
		}
	}

	/**
	 * onClick action - overrides other onClick action to ensure that nothing happens.
	 *
	 * @param view The view triggering the onClick action.
	 */
	public final void doNothing(final View view) {
		// do nothing
	}

	/**
	 * Fragment for the date dialog.
	 */
	public static class DateDialogFragment extends DialogFragment {
		@NonNull
		@Override
		public final Dialog onCreateDialog(final Bundle savedInstanceState) {
			int year = getArguments().getInt("Year");
			int month = getArguments().getInt("Month");
			int date = getArguments().getInt("Date");

			DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
				@Override
				public void onDateSet(final DatePicker view, final int yearSelected, final int monthOfYear,
									  final int dayOfMonth) {
					((OrganizeNewPhotosActivity) getActivity()).setDate(yearSelected, monthOfYear, dayOfMonth);
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
		 * Continue with viewing images.
		 */
		VIEW_IMAGES
	}

}
