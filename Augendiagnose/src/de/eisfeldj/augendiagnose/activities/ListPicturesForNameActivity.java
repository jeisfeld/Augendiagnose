package de.eisfeldj.augendiagnose.activities;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.components.ListPicturesForNameArrayAdapter;
import de.eisfeldj.augendiagnose.util.DateUtil;
import de.eisfeldj.augendiagnose.util.DialogUtil;
import de.eisfeldj.augendiagnose.util.DialogUtil.ConfirmDeleteDialogFragment.ConfirmDeleteDialogListener;
import de.eisfeldj.augendiagnose.util.EyePhotoPair;
import de.eisfeldj.augendiagnose.util.ImageSelectionAndDisplayHandler;

/**
 * Activity to display the pictures in an eye photo folder (in pairs) Either pictures from this folder can be displayed
 * directly, or another folder can be selected for a second picture.
 */
public class ListPicturesForNameActivity extends ListPicturesForNameBaseActivity {
	private static final String STRING_EXTRA_NAME = "de.eisfeldj.augendiagnose.NAME";
	private static final String STRING_EXTRA_PARENTFOLDER = "de.eisfeldj.augendiagnose.PARENTFOLDER";

	private Button buttonAdditionalPictures;
	private int contextMenuPosition;
	private ListPicturesForNameArrayAdapter adapter;
	private Calendar pictureDate = new GregorianCalendar();

	/**
	 * Static helper method to start the activity, passing the path of the parent folder and the name of the current
	 * folder.
	 * 
	 * @param activity
	 * @param parentFolder
	 * @param name
	 */
	public static void startActivity(Context context, String parentFolder, String name) {
		Intent intent = new Intent(context, ListPicturesForNameActivity.class);
		intent.putExtra(STRING_EXTRA_PARENTFOLDER, parentFolder);
		intent.putExtra(STRING_EXTRA_NAME, name);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (dismiss) {
			return;
		}

		getActionBar().setDisplayHomeAsUpEnabled(true);

		buttonAdditionalPictures = (Button) findViewById(R.id.buttonSelectAdditionalPicture);

		adapter = new ListPicturesForNameArrayAdapter(this, eyePhotoPairs);
		listview.setAdapter(adapter);

		// Initialize the handler which manages the clicks
		ImageSelectionAndDisplayHandler.getInstance().setActivity(this);
	}

	/**
	 * Update the list of eye photo pairs
	 * 
	 * @param eyePhotoPairs
	 */
	public void updateEyePhotoPairs() {
		eyePhotoPairs = createEyePhotoList(new File(parentFolder, name));
		adapter = new ListPicturesForNameArrayAdapter(this, eyePhotoPairs);
		listview.setAdapter(adapter);
	}

	/**
	 * Inflate options menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_only_help, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Handle menu actions
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_help:
			DisplayHtmlActivity.startActivity(this, R.string.html_display_photos);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected int getContentView() {
		return R.layout.activity_list_pictures_for_name;
	}

	/**
	 * Display the button "additional pictures" after one photo is selected
	 */
	public void activateButtonAdditionalPictures() {
		buttonAdditionalPictures.setVisibility(View.VISIBLE);
		listview.invalidate();
	}

	/**
	 * Undisplay the button "additional pictures" if photo selection has been removed.
	 */
	public void deactivateButtonAdditionalPictures() {
		buttonAdditionalPictures.setVisibility(View.GONE);
		listview.invalidate();
	}

	/**
	 * onClick action for Button "additional pictures"
	 * 
	 * @param view
	 */
	public void selectDifferentPictureActivity(View view) {
		ListFoldersForDisplaySecondActivity.startActivity(this, parentFolder);
	}

	/**
	 * Create the context menu
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_picture_date, menu);
		contextMenuPosition = adapter.getRow((TextView) v);
	}

	/**
	 * Handle items in the context menu
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final EyePhotoPair pairToDelete = eyePhotoPairs[contextMenuPosition];

		switch (item.getItemId()) {
		case R.id.action_delete_images:
			ConfirmDeleteDialogListener listener = new ConfirmDeleteDialogListener() {
				private static final long serialVersionUID = -7137767075780390391L;

				@Override
				public void onDialogPositiveClick(DialogFragment dialog) {
					// delete images
					boolean success = pairToDelete.delete();
					// update list of images
					updateEyePhotoPairs();

					if (!success) {
						DialogUtil.displayError(ListPicturesForNameActivity.this,
								R.string.message_dialog_failed_to_delete_file_for_date, pairToDelete.getLeftEye()
										.getPersonName(), pairToDelete.getDateDisplayString("dd.MM.yyyy"));

					}
				}

				@Override
				public void onDialogNegativeClick(DialogFragment dialog) {
					// Do nothing
				}
			};

			DialogUtil.displayDeleteConfirmationMessage(this, listener, R.string.message_dialog_confirm_delete_date,
					pairToDelete.getLeftEye().getPersonName(), pairToDelete.getDateDisplayString("dd.MM.yyyy"));
			return true;
		case R.id.action_change_date:
			pictureDate.setTime(pairToDelete.getDate());
			DateChangeDialogFragment fragment = new DateChangeDialogFragment();
			Bundle bundle = new Bundle();
			bundle.putInt("Year", pictureDate.get(Calendar.YEAR));
			bundle.putInt("Month", pictureDate.get(Calendar.MONTH));
			bundle.putInt("Date", pictureDate.get(Calendar.DAY_OF_MONTH));
			bundle.putInt("position", contextMenuPosition);
			fragment.setArguments(bundle);
			fragment.show(getFragmentManager(), DateChangeDialogFragment.class.toString());
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	/**
	 * Fragment for the dialog to change the date
	 */
	public static class DateChangeDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int year = getArguments().getInt("Year");
			int month = getArguments().getInt("Month");
			int date = getArguments().getInt("Date");
			int position = getArguments().getInt("position");
			final ListPicturesForNameActivity activity = (ListPicturesForNameActivity) getActivity();
			final EyePhotoPair pairToDelete = activity.eyePhotoPairs[position];

			final DatePickerDialog dialog = new DatePickerDialog(getActivity(), null, year, month, date);

			// Workaround due to Android bug
			dialog.setCancelable(true);
			dialog.setCanceledOnTouchOutside(true);
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.button_cancel), (OnClickListener) null);
			dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.button_ok), //
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int which) {
							int yearSelected = dialog.getDatePicker().getYear();
							int monthOfYear = dialog.getDatePicker().getMonth();
							int dayOfMonth = dialog.getDatePicker().getDayOfMonth();

							activity.pictureDate = new GregorianCalendar(yearSelected, monthOfYear, dayOfMonth);
							Date newDate = new Date(activity.pictureDate.getTimeInMillis());
							boolean success = pairToDelete.changeDate(newDate);
							activity.updateEyePhotoPairs();

							if (!success) {
								DialogUtil.displayError(activity, R.string.message_dialog_failed_to_change_date,
										pairToDelete.getLeftEye().getPersonName(),
										pairToDelete.getDateDisplayString("dd.MM.yyyy"),
										DateUtil.format(newDate, "dd.MM.yyyy"));
							}
						}
					});

			return dialog;
		}
	}

}
