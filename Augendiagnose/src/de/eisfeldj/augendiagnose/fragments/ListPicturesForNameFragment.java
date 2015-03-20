package de.eisfeldj.augendiagnose.fragments;

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
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.components.ListPicturesForNameArrayAdapter;
import de.eisfeldj.augendiagnose.util.DateUtil;
import de.eisfeldj.augendiagnose.util.DialogUtil;
import de.eisfeldj.augendiagnose.util.DialogUtil.ConfirmDialogFragment.ConfirmDialogListener;
import de.eisfeldj.augendiagnose.util.EyePhotoPair;
import de.eisfeldj.augendiagnose.util.MediaStoreUtil;
import de.eisfeldj.augendiagnose.util.PreferenceUtil;

/**
 * Fragment to display the pictures in an eye photo folder (in pairs) Either pictures from this folder can be displayed
 * directly, or another folder can be selected for a second picture.
 */
public class ListPicturesForNameFragment extends ListPicturesForNameBaseFragment {
	/**
	 * The Button for selecting an additional picture.
	 */
	private Button buttonAdditionalPictures;

	/**
	 * The position in the context menu which has been selected.
	 */
	private int contextMenuPosition;

	/**
	 * The adapter displaying the list of pictures.
	 */
	private ListPicturesForNameArrayAdapter adapter;

	/**
	 * The date of the pictures - used in case of date change.
	 */
	private Calendar pictureDate = new GregorianCalendar();

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_list_pictures_for_name, container, false);
	}

	@Override
	public final void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (isDismiss()) {
			return;
		}

		buttonAdditionalPictures = (Button) getView().findViewById(R.id.buttonSelectAdditionalPicture);

		adapter = new ListPicturesForNameArrayAdapter(getActivity(), this, getEyePhotoPairs());
		getListView().setAdapter(adapter);
	}

	/**
	 * Update the list of eye photo pairs.
	 */
	protected final void updateEyePhotoPairs() {
		boolean isPhotosRemaining = createAndStoreEyePhotoList(false);

		if (isPhotosRemaining) {
			adapter = new ListPicturesForNameArrayAdapter(getActivity(), this, getEyePhotoPairs());
			getListView().setAdapter(adapter);
		}
	}

	/**
	 * Display the button "additional pictures" after one photo is selected.
	 */
	public final void activateButtonAdditionalPictures() {
		buttonAdditionalPictures.setVisibility(View.VISIBLE);
		getListView().invalidate();
	}

	/**
	 * Undisplay the button "additional pictures" if photo selection has been removed.
	 */
	public final void deactivateButtonAdditionalPictures() {
		buttonAdditionalPictures.setVisibility(View.GONE);
		getListView().invalidate();
	}

	/*
	 * Create the context menu.
	 */
	@Override
	public final void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.context_picture_date, menu);
		contextMenuPosition = adapter.getRow((TextView) v);
	}

	/*
	 * Handle items in the context menu.
	 */
	@Override
	public final boolean onContextItemSelected(final MenuItem item) {
		if (item.getGroupId() == R.id.menugroup_picture_date) {
			final EyePhotoPair pairToModify = getEyePhotoPairs()[contextMenuPosition];

			switch (item.getItemId()) {
			case R.id.action_change_date:
				// ensure that activity is linked to the correct instance of this listFoldersFragment
				((ListPicturesForNameFragmentHolder) getActivity()).setListPicturesForNameFragment(this);

				pictureDate.setTime(pairToModify.getDate());
				DateChangeDialogFragment fragment = new DateChangeDialogFragment();
				Bundle bundle = new Bundle();
				bundle.putInt("Year", pictureDate.get(Calendar.YEAR));
				bundle.putInt("Month", pictureDate.get(Calendar.MONTH));
				bundle.putInt("Date", pictureDate.get(Calendar.DAY_OF_MONTH));
				bundle.putInt("position", contextMenuPosition);
				fragment.setArguments(bundle);
				fragment.show(getFragmentManager(), DateChangeDialogFragment.class.toString());
				return true;

			case R.id.action_delete_images:
				ConfirmDialogListener listenerDelete = new ConfirmDialogListener() {
					private static final long serialVersionUID = -7137767075780390391L;

					@Override
					public void onDialogPositiveClick(final DialogFragment dialog) {
						// delete images
						boolean success = pairToModify.delete();
						// update list of images
						updateEyePhotoPairs();

						if (!success) {
							DialogUtil.displayError(ListPicturesForNameFragment.this.getActivity(),
									R.string.message_dialog_failed_to_delete_file_for_date, false, pairToModify
											.getLeftEye().getPersonName(), pairToModify
											.getDateDisplayString("dd.MM.yyyy"));

						}
					}

					@Override
					public void onDialogNegativeClick(final DialogFragment dialog) {
						// Do nothing
					}
				};

				DialogUtil.displayConfirmationMessage(getActivity(), listenerDelete, R.string.button_delete,
						R.string.message_dialog_confirm_delete_date, pairToModify.getLeftEye().getPersonName(),
						pairToModify.getDateDisplayString("dd.MM.yyyy"));
				return true;

			case R.id.action_move_to_input_folder:
				ConfirmDialogListener listenerMove = new ConfirmDialogListener() {
					private static final long serialVersionUID = -7137767075780390391L;

					@Override
					public void onDialogPositiveClick(final DialogFragment dialog) {
						// delete old thumbnails, in so that other photos can get the same names
						MediaStoreUtil.deleteThumbnail(pairToModify.getLeftEye().getAbsolutePath());
						MediaStoreUtil.deleteThumbnail(pairToModify.getRightEye().getAbsolutePath());

						// delete images
						boolean success =
								pairToModify.moveToFolder(PreferenceUtil
										.getSharedPreferenceString(R.string.key_folder_input));
						// update list of images
						updateEyePhotoPairs();

						if (!success) {
							DialogUtil.displayError(ListPicturesForNameFragment.this.getActivity(),
									R.string.message_dialog_failed_to_move_file_for_date, false, pairToModify
											.getLeftEye().getPersonName(), pairToModify
											.getDateDisplayString("dd.MM.yyyy"));

						}
					}

					@Override
					public void onDialogNegativeClick(final DialogFragment dialog) {
						// Do nothing
					}
				};

				DialogUtil.displayConfirmationMessage(getActivity(), listenerMove, R.string.button_move,
						R.string.message_dialog_confirm_move_to_input_folder,
						pairToModify.getLeftEye().getPersonName(),
						pairToModify.getDateDisplayString("dd.MM.yyyy"));
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
	 * Fragment for the dialog to change the date.
	 */
	public static class DateChangeDialogFragment extends DialogFragment {
		@Override
		public final Dialog onCreateDialog(final Bundle savedInstanceState) {
			int year = getArguments().getInt("Year");
			int month = getArguments().getInt("Month");
			int date = getArguments().getInt("Date");
			int position = getArguments().getInt("position");

			final Activity activity = getActivity();
			final ListPicturesForNameFragment fragment = ((ListPicturesForNameFragmentHolder) activity)
					.getListPicturesForNameFragment();

			final EyePhotoPair pairToUpdate = fragment.getEyePhotoPairs()[position];

			final DatePickerDialog dialog = new DatePickerDialog(getActivity(), null, year, month, date);

			// Workaround due to Android bug
			dialog.setCancelable(true);
			dialog.setCanceledOnTouchOutside(true);
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.button_cancel), (OnClickListener) null);
			dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.button_ok), //
					new OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialogInterface, final int which) {
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
	 * Interface to be implemented by activities running this listFoldersFragment. Required for communication with
	 * context menu dialogs.
	 */
	public interface ListPicturesForNameFragmentHolder {
		/**
		 * Get the ListPicturesForNameFragment.
		 *
		 * @return the fragment.
		 */
		ListPicturesForNameFragment getListPicturesForNameFragment();

		/**
		 * Set the ListPicturesForNameFragment.
		 *
		 * @param fragment
		 *            the fragment.
		 */
		void setListPicturesForNameFragment(final ListPicturesForNameFragment fragment);
	}

}
