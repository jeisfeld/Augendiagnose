package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import de.eisfeldj.augendiagnose.R;

/**
 * Activity to display the list of subfolders of the eye photo folder as dialog with the goal to select a name for
 * ordering new pictures. (The folder names equal the person names.)
 */
public class ListFoldersForSelectActivity extends ListFoldersBaseActivity {
	public static final int REQUEST_CODE = 1;
	private static final String STRING_EXTRA_NAME = "de.eisfeldj.augendiagnose.NAME";
	private static final String STRING_EXTRA_PRESELECTED_NAME = "de.eisfeldj.augendiagnose.PRESELECTED_NAME";
	private String preselectedName;

	/**
	 * Static helper method to start the activity, passing the path of the folder and a potentially preselected new
	 * name.
	 * 
	 * @param context
	 * @param foldername
	 */
	public static void startActivity(Activity activity, String foldername, String preselectedName) {
		Intent intent = new Intent(activity, ListFoldersForSelectActivity.class);
		intent.putExtra(STRING_EXTRA_FOLDER, foldername);
		intent.putExtra(STRING_EXTRA_PRESELECTED_NAME, preselectedName);
		activity.startActivityForResult(intent, REQUEST_CODE);
	}

	/**
	 * Static helper method to extract the name of the selected folder (= person name) from the activity response
	 * 
	 * @param resultCode
	 * @param data
	 *            The activity response
	 * @return
	 */
	public static CharSequence getResult(int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Bundle res = data.getExtras();
			return res.getCharSequence(STRING_EXTRA_NAME);
		}
		else {
			return "";
		}
	}

	@Override
	protected void setOnItemLongClickListener() {
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
		preselectedName = getIntent().getStringExtra(STRING_EXTRA_PRESELECTED_NAME);
		if (preselectedName == null || preselectedName.length() == 0) {
			folderNames.add(0, getString(R.string.display_new_name));
		}
		else {
			folderNames.add(0, "[" + preselectedName + "]");
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		((RenameOnLongClickListener) getListView().getOnItemLongClickListener()).closeDialog();
	}

	/**
	 * Helper method: Return the selected name and finish the activity
	 */
	private void returnResult(CharSequence result) {
		Bundle resultData = new Bundle();
		resultData.putCharSequence(STRING_EXTRA_NAME, result);
		Intent intent = new Intent();
		intent.putExtras(resultData);
		setResult(RESULT_OK, intent);
		finish();
	}

	/**
	 * Extension of the name change listener, taking care of the fact that the first item of the list has been added as
	 * "new name" selector.
	 */
	private class RenameOnLongClickListener extends ListFoldersBaseActivity.RenameOnLongClickListener {
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
				final ListFoldersForSelectActivity activity = ListFoldersForSelectActivity.this;
				final EditText input = new EditText(activity);
				if (preselectedName != null && preselectedName.length() > 0) {
					input.setText(preselectedName);
				}
				else {
					input.setHint(getString(R.string.hint_insert_name));
				}

				EnterNameDialogFragment fragment = new EnterNameDialogFragment();
				Bundle bundle = new Bundle();
				bundle.putString("preselectedName", preselectedName);
				fragment.setArguments(bundle);
				fragment.show(getFragmentManager(), EnterNameDialogFragment.class.toString());
			}
			else {
				returnResult(((TextView) view).getText());
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
