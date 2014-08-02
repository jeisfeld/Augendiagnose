package de.eisfeldj.augendiagnose.fragments;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.components.ListPicturesForNameArrayAdapter;
import de.eisfeldj.augendiagnose.util.DateUtil;
import de.eisfeldj.augendiagnose.util.DialogUtil;
import de.eisfeldj.augendiagnose.util.DialogUtil.ConfirmDeleteDialogFragment.ConfirmDeleteDialogListener;
import de.eisfeldj.augendiagnose.util.EyePhotoPair;

/**
 * Fragment to display the pictures in an eye photo folder (in pairs) Either pictures from this folder can be displayed
 * directly, or another folder can be selected for a second picture.
 */
public class ListPicturesForNameFragment extends ListPicturesForNameBaseFragment {
	private Button buttonAdditionalPictures;
	private int contextMenuPosition;
	private ListPicturesForNameArrayAdapter adapter;
	private Calendar pictureDate = new GregorianCalendar();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_list_pictures_for_name, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (dismiss) {
			return;
		}

		buttonAdditionalPictures = (Button) getView().findViewById(R.id.buttonSelectAdditionalPicture);

		adapter = new ListPicturesForNameArrayAdapter(getActivity(), this, eyePhotoPairs);
		listview.setAdapter(adapter);
	}

	/**
	 * Update the list of eye photo pairs
	 * 
	 * @param eyePhotoPairs
	 */
	public void updateEyePhotoPairs() {
		eyePhotoPairs = createEyePhotoList(new File(parentFolder, name));
		if (eyePhotoPairs == null || eyePhotoPairs.length == 0) {
			if (eyePhotoPairs == null || eyePhotoPairs.length == 0) {
				DialogUtil.displayError(getActivity(), R.string.message_dialog_no_photos_for_name,
						!Application.isTablet(), name);
				return;
			}
		}

		adapter = new ListPicturesForNameArrayAdapter(getActivity(), this, eyePhotoPairs);
		listview.setAdapter(adapter);
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
	 * Create the context menu
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.context_picture_date, menu);
		contextMenuPosition = adapter.getRow((TextView) v);
	}

	/**
	 * Handle items in the context menu
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getGroupId() == R.id.menugroup_picture_date) {
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
							DialogUtil.displayError(ListPicturesForNameFragment.this.getActivity(),
									R.string.message_dialog_failed_to_delete_file_for_date, false, pairToDelete
											.getLeftEye().getPersonName(), pairToDelete
											.getDateDisplayString("dd.MM.yyyy"));

						}
					}

					@Override
					public void onDialogNegativeClick(DialogFragment dialog) {
						// Do nothing
					}
				};

				DialogUtil.displayDeleteConfirmationMessage(getActivity(), listener,
						R.string.message_dialog_confirm_delete_date, pairToDelete.getLeftEye().getPersonName(),
						pairToDelete.getDateDisplayString("dd.MM.yyyy"));
				return true;
			case R.id.action_change_date:
				// ensure that activity is linked to the correct instance of this listFoldersFragment
				((ListPicturesForNameFragmentHolder) getActivity()).setListPicturesForNameFragment(this);

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
		else {
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

			final Activity activity = getActivity();
			final ListPicturesForNameFragment fragment = ((ListPicturesForNameFragmentHolder) activity)
					.getListPicturesForNameFragment();

			final EyePhotoPair pairToUpdate = fragment.eyePhotoPairs[position];

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

							fragment.pictureDate = new GregorianCalendar(yearSelected, monthOfYear, dayOfMonth);
							Date newDate = new Date(fragment.pictureDate.getTimeInMillis());
							boolean success = pairToUpdate.changeDate(newDate);
							fragment.updateEyePhotoPairs();

							if (!success) {
								DialogUtil.displayError(activity, R.string.message_dialog_failed_to_change_date, false,
										pairToUpdate.getLeftEye().getPersonName(),
										pairToUpdate.getDateDisplayString("dd.MM.yyyy"),
										DateUtil.format(newDate, "dd.MM.yyyy"));
							}
						}
					});

			return dialog;
		}
	}

	/**
	 * Interface to be implemented by activities running this listFoldersFragment. Required for communication with context menu
	 * dialogs.
	 */
	public interface ListPicturesForNameFragmentHolder {
		public ListPicturesForNameFragment getListPicturesForNameFragment();

		public void setListPicturesForNameFragment(ListPicturesForNameFragment fragment);
	}

}
