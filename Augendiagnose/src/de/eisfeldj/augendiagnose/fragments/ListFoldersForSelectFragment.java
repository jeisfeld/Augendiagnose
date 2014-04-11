package de.eisfeldj.augendiagnose.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.activities.ListFoldersForSelectActivity;

/**
 * Activity to display the list of subfolders of the eye photo folder as dialog with the goal to select a name for
 * ordering new pictures. (The folder names equal the person names.)
 */
public class ListFoldersForSelectFragment extends ListFoldersBaseFragment {
	private static final String STRING_PRESELECTED_NAME = "de.eisfeldj.augendiagnose.PRESELECTED_NAME";

	private String preselectedName;

	/**
	 * Return an instance of the fragment and initialize it with parentFolder and preselectedName
	 * 
	 * @param parentFolder
	 * @param preselectedName
	 * @return
	 */
	public void setParameters(String parentFolder, String preselectedName) {
		Bundle args = new Bundle();
		args.putString(STRING_FOLDER, parentFolder);
		args.putString(STRING_PRESELECTED_NAME, preselectedName);

		setArguments(args);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setOnItemLongClickListener(new RenameOnLongClickListener());
	}

	@Override
	protected void setOnItemClickListener() {
		getListView().setOnItemClickListener(new ShowContentsOnClickListener());
	}

	/**
	 * Add a "new name" entry on top of the list
	 */
	@Override
	protected void createList() {
		super.createList();
		preselectedName = getArguments().getString(STRING_PRESELECTED_NAME);
		if (preselectedName == null || preselectedName.length() == 0) {
			folderNames.add(0, getString(R.string.display_new_name));
		}
		else {
			folderNames.add(0, "[" + preselectedName + "]");
		}

	}

	@Override
	public void onDestroyView() {
		super.onDestroy();
		((RenameOnLongClickListener) getListView().getOnItemLongClickListener()).closeDialog();
	}

	/**
	 * Extension of the name change listener, taking care of the fact that the first item of the list has been added as
	 * "new name" selector.
	 */
	private class RenameOnLongClickListener extends ListFoldersBaseFragment.RenameOnLongClickListener {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, final long rowId) {
			if (position == 0) {
				return true;
			}
			else {
				return super.onItemLongClick(parent, view, position - 1, rowId - 1);
			}
		}
	}

	/**
	 * On item click, either return the selected file (if the click is not on the first entry) Or display a dialog for
	 * entering a new name.
	 * 
	 */
	private class ShowContentsOnClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (position == 0) {
				final ListFoldersForSelectFragment fragment = ListFoldersForSelectFragment.this;
				final EditText input = new EditText(fragment.getActivity());
				if (preselectedName != null && preselectedName.length() > 0) {
					input.setText(preselectedName);
				}
				else {
					input.setHint(getString(R.string.hint_insert_name));
				}

				EnterNameDialogFragment enterNameFragment = new EnterNameDialogFragment();
				Bundle bundle = new Bundle();
				bundle.putString("preselectedName", preselectedName);
				enterNameFragment.setArguments(bundle);
				enterNameFragment.show(getFragmentManager(), EnterNameDialogFragment.class.toString());
			}
			else {
				ListFoldersForSelectActivity activity = (ListFoldersForSelectActivity) getActivity();
				activity.returnResult(((TextView) view).getText());
			}

		}
	}

	/**
	 * Dialog fragment to enter a new name. The fragment allows to pass a preselected name which will be displayed on
	 * start. It has a "clear" button that allows to clean the entered name.
	 */
	public static class EnterNameDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			String preselectedName = getArguments().getString("preselectedName");

			final ListFoldersForSelectActivity activity = (ListFoldersForSelectActivity) getActivity();

			final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_enter_name, null);
			final EditText input = (EditText) view.findViewById(R.id.dialogEnterNameInput);

			if (preselectedName != null && preselectedName.length() > 0) {
				input.setText(preselectedName);
			}

			final AlertDialog dialog = new AlertDialog.Builder(activity) //
					.setTitle(R.string.title_dialog_new_name) //
					.setView(view) //
					.create();

			final Button buttonCancel = (Button) view.findViewById(R.id.dialogEnterNameButtonCancel);
			buttonCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			final Button buttonClear = (Button) view.findViewById(R.id.dialogEnterNameButtonClear);
			buttonClear.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					input.setText("");
				}
			});

			final Button buttonOk = (Button) view.findViewById(R.id.dialogEnterNameButtonOk);
			buttonOk.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					activity.returnResult(input.getText().toString().trim());
					dialog.dismiss();
				}
			});

			// Automatically show keyboard
			dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);

			return dialog;
		}

	}

}
