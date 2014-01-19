package de.eisfeldj.augendiagnose.activities;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.util.DialogUtil;
import de.eisfeldj.augendiagnose.util.EyePhoto;
import de.eisfeldj.augendiagnose.util.TwoImageSelectionHandler;
import de.eisfeldj.augendiagnose.util.EyePhoto.RightLeft;

/**
 * Activity to display a pair of new eye photos, choose a name and a date for them, and shift them into the
 * application's eye photo folder (with renaming)
 */
public class OrganizeNewPhotosActivity extends Activity {

	private static final String STRING_EXTRA_EYEFIFOLDER = "de.eisfeldj.augendiagnose.EYEFIFOLDER";
	private static final String STRING_EXTRA_FOLDER = "de.eisfeldj.augendiagnose.FOLDER";
	private static final String BOOL_EXTRA_RIGHTEYELAST = "de.eisfeldj.augendiagnose.RIGHTEYELAST";

	private File eyefiFolder;
	private File parentFolder;
	private Calendar pictureDate = Calendar.getInstance();
	private boolean rightEyeLast;
	private boolean hasSelectName = false;

	private ImageView imageRight, imageLeft;
	private EditText editName, editDate;

	private EyePhoto photoRight, photoLeft;

	/**
	 * Static helper method to start the activity, passing the source folder, the target folder, and a flag indicating
	 * if the last picture is the right or the left eye.
	 * 
	 * @param context
	 * @param eyefifoldername
	 * @param foldername
	 * @param rightEyeLast
	 */
	public static void startActivity(Context context, String eyefifoldername, String foldername, boolean rightEyeLast) {
		Intent intent = new Intent(context, OrganizeNewPhotosActivity.class);
		intent.putExtra(STRING_EXTRA_EYEFIFOLDER, eyefifoldername);
		intent.putExtra(STRING_EXTRA_FOLDER, foldername);
		intent.putExtra(BOOL_EXTRA_RIGHTEYELAST, rightEyeLast);
		context.startActivity(intent);
	}

	/**
	 * Create the activity, build the view, fill all content and add listeners.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_organize_new_photos);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		eyefiFolder = new File(getIntent().getStringExtra(STRING_EXTRA_EYEFIFOLDER));
		parentFolder = new File(getIntent().getStringExtra(STRING_EXTRA_FOLDER));
		rightEyeLast = getIntent().getBooleanExtra(BOOL_EXTRA_RIGHTEYELAST, true);

		if (savedInstanceState != null) {
			hasSelectName = savedInstanceState.getBoolean("hasSelectName", false);
			if(savedInstanceState.getString("rightEyePhoto")!=null) {
				photoRight = new EyePhoto(savedInstanceState.getString("rightEyePhoto"));
				photoLeft = new EyePhoto(savedInstanceState.getString("leftEyePhoto"));
			}
		}

		imageRight = (ImageView) findViewById(R.id.imageOrganizeRight);
		imageLeft = (ImageView) findViewById(R.id.imageOrganizeLeft);

		// when touching the "name" field, open a dialog.
		editName = (EditText) findViewById(R.id.editName);
		editName.setInputType(InputType.TYPE_NULL);
		editName.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					openNameDialog(v);
				}
				return true;
			}
		});

		// when touching the "date" field, open a dialog.
		editDate = (EditText) findViewById(R.id.editDate);
		editDate.setInputType(InputType.TYPE_NULL);
		editDate.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					openDateDialog(v);
				}
				return true;
			}
		});
		
		if(photoLeft==null || photoRight==null) {
			// initial fill
			setPicturesAndValues();
		}
		else {
			// only load predefined images
			updateImages();
			pictureDate.setTime(photoRight.getDate());
			editDate.setText(EyePhoto.getDisplayDate(pictureDate));
			editDate.invalidate();
		}
		
		// Ensure that target folder exists
		if(!parentFolder.exists()) {
			parentFolder.mkdirs();
		}
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		TwoImageSelectionHandler.clean();
	}
	
	/**
	 * Helper methods to load the pictures and to preset the date (from the pictures).
	 */
	private void setPicturesAndValues() {
		// List all JPG files
		File[] files = eyefiFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().toUpperCase(Locale.getDefault()).endsWith(".JPG");
			}
		});

		if (files == null) {
			DialogUtil.displayErrorAndReturn(this, R.string.message_dialog_folder_does_not_exist,
					eyefiFolder.getAbsolutePath());
			return;
		}

		// Sort files by date
		Arrays.sort(files, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
			}
		});

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
			editDate.setText(EyePhoto.getDisplayDate(pictureDate));
			editDate.invalidate();
		}
		else {
			// Error message if there are less than two files
			DialogUtil.displayErrorAndReturn(this, R.string.message_dialog_no_picture);
		}
	}

	/**
	 * Display the two images. As these are only two thumbnails, we do this in the main thread.
	 */
	private void updateImages() {
		imageRight.post(new Runnable () {
			@Override
			public void run() {
				imageRight.setImageBitmap(photoRight.getImageBitmap(EyePhoto.MINI_THUMB_SIZE));
				imageRight.invalidate();
			}			
		});

		imageLeft.post(new Runnable () {
			@Override
			public void run() {
				imageLeft.setImageBitmap(photoLeft.getImageBitmap(EyePhoto.MINI_THUMB_SIZE));
				imageLeft.invalidate();
			}			
		});
	}

	/**
	 * Helper method to display an error message
	 * 
	 * @param resource
	 * @param args
	 */
	private void displayError(int resource, Object... args) {
		DialogUtil.displayError(this, resource, args);
	}

	/**
	 * onClick action for Button "Switch images"
	 * 
	 * @param view
	 */
	public void switchImages(View view) {
		rightEyeLast = !rightEyeLast;
		EyePhoto temp = photoLeft;
		photoLeft = photoRight;
		photoRight = temp;
		updateImages();
	}

	/**
	 * onClick action for Button "Other photos"
	 * 
	 * @param view
	 */
	public void selectOtherPhotos(View view) {
		SelectTwoPicturesActivity.startActivity(this, eyefiFolder.getAbsolutePath());
	}

	/**
	 * onClick action for Button "Finish" Moves and renames the selected files.
	 * 
	 * @param view
	 */
	public void finishActivity(View view) {
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

		// TODO: Change IPTC settings

		finish();
	}

	/**
	 * onClick action for Name field. Opens a dialog for selecting the name from the list of folders.
	 * 
	 * @param view
	 */
	public void openNameDialog(View view) {
		// open only if it is not already there
		if (!hasSelectName) {
			hasSelectName = true;
			ListFoldersForSelectActivity.startActivity(this, parentFolder.getAbsolutePath(), editName.getText()
					.toString());
		}
	}

	/**
	 * When getting the response from the name selection, update the name field in the display.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ListFoldersForSelectActivity.REQUEST_CODE:
			// Update the name with the result of ListFoldersForSelectActivity
			CharSequence result = ListFoldersForSelectActivity.getResult(resultCode, data);
			if (result != null && result.length() > 0) {
				editName.setText(ListFoldersForSelectActivity.getResult(resultCode, data));
				editName.invalidate();
			}
			hasSelectName = false;
			break;
		case SelectTwoPicturesActivity.REQUEST_CODE:
			SelectTwoPicturesActivity.FilePair filePair = SelectTwoPicturesActivity.getResult(resultCode, data);
			if(filePair!=null) {
				photoRight = new EyePhoto(filePair.file1);
				photoLeft = new EyePhoto(filePair.file2);
				updateImages();
			}
		}
	}

	/**
	 * onClick action for date field. Opens a date picker dialog.
	 * 
	 * @param view
	 */
	public void openDateDialog(View view) {
		DateDialogFragment fragment = new DateDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("Year", pictureDate.get(Calendar.YEAR));
		bundle.putInt("Month", pictureDate.get(Calendar.MONTH));
		bundle.putInt("Date", pictureDate.get(Calendar.DAY_OF_MONTH));
		fragment.setArguments(bundle);
		fragment.show(getFragmentManager(), DateDialogFragment.class.toString());

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("hasSelectName", hasSelectName);
		if(photoRight!=null && photoLeft!=null) {
			outState.putString("rightEyePhoto", photoRight.getAbsolutePath());
			outState.putString("leftEyePhoto", photoLeft.getAbsolutePath());
		}
	}

	/**
	 * onClick action for displaying the two pictures.
	 * 
	 * @param view
	 */
	public void displayNewImages(View view) {
		DisplayTwoActivity.startActivity(this, photoRight.getAbsolutePath(), photoLeft.getAbsolutePath());
	}

	/**
	 * onClick action - overrides other onClick action to ensure that nothing happens
	 * 
	 * @param view
	 */
	public void doNothing(View view) {
	}

	/**
	 * Fragment for the date dialog
	 */
	public static class DateDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int year = getArguments().getInt("Year");
			int month = getArguments().getInt("Month");
			int date = getArguments().getInt("Date");

			DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
				OrganizeNewPhotosActivity activity = (OrganizeNewPhotosActivity) getActivity();

				public void onDateSet(DatePicker view, int yearSelected, int monthOfYear, int dayOfMonth) {
					activity.pictureDate = new GregorianCalendar(yearSelected, monthOfYear, dayOfMonth);
					activity.editDate.setText(EyePhoto.getDisplayDate(activity.pictureDate));
					activity.editDate.invalidate();
				}
			};
			return new DatePickerDialog(getActivity(), dateSetListener, year, month, date);
		}
	}

}
