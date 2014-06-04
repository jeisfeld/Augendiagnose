package de.eisfeldj.augendiagnose.fragments;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.activities.ListFoldersForSelectActivity;

/**
 * Fragment to display the list of subfolders of the eye photo folder as dialog with the goal to select a name for
 * ordering new pictures. (The folder names equal the person names.)
 */
public class ListFoldersForSelectFragment extends ListFoldersBaseFragment {
	private static final String STRING_PRESELECTED_NAME = "de.eisfeldj.augendiagnose.PRESELECTED_NAME";
	private static int IME_ACTION = EditorInfo.IME_ACTION_DONE;

	private String preselectedName;

	/**
	 * Initialize the fragment with parentFolder and preselectedName
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		preselectedName = args.getString(STRING_PRESELECTED_NAME);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		listView.setOnItemLongClickListener(new RenameOnLongClickListener());

		editText.setHint(R.string.hint_insert_name);
		editText.setImeActionLabel(getString(R.string.button_select), IME_ACTION);
		editText.setText(preselectedName);
		editText.requestFocus();
		editText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == IME_ACTION) {
					ListFoldersForSelectActivity activity = (ListFoldersForSelectActivity) getActivity();
					activity.returnResult(v.getText().toString().trim());
				}
				return true;
			}
		});
	}

	@Override
	protected void setOnItemClickListener() {
		listView.setOnItemClickListener(new ReturnNameOnClickListener());
	}

	@Override
	public void onDestroyView() {
		super.onDestroy();
		((RenameOnLongClickListener) listView.getOnItemLongClickListener()).closeDialog();
	}

	/**
	 * On item click, return the selected file
	 * 
	 */
	private class ReturnNameOnClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			ListFoldersForSelectActivity activity = (ListFoldersForSelectActivity) getActivity();
			activity.returnResult(((TextView) view).getText());
		}
	}

}
