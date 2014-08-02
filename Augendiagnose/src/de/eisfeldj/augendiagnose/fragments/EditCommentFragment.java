package de.eisfeldj.augendiagnose.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.activities.DisplayImageActivity;
import de.eisfeldj.augendiagnose.util.KeyboardUtil;

/**
 * Fragment to add the comment of a picture
 */
public class EditCommentFragment extends Fragment {

	private static final String STRING_TEXT = "de.eisfeldj.augendiagnose.TEXT";

	private EditText editText;
	private String text;

	/**
	 * Initialize the listFoldersFragment with the text
	 * 
	 * @param text
	 * @return
	 */
	public void setParameters(String text) {
		Bundle args = new Bundle();
		args.putString(STRING_TEXT, text);

		setArguments(args);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		text = getArguments().getString(STRING_TEXT);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_edit_comment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		editText = (EditText) getView().findViewById(R.id.input_edit_comment);
		editText.setText(text);

		editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					getActivity().getWindow().setSoftInputMode(
							WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
			}
		});

		final Button buttonCancel = (Button) getView().findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DisplayImageActivity activity = (DisplayImageActivity) getActivity();
				activity.processUpdatedComment(editText.getText().toString(), false);
			}
		});

		final Button buttonClear = (Button) getView().findViewById(R.id.buttonClear);
		buttonClear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				editText.setText("");
			}
		});

		final Button buttonOk = (Button) getView().findViewById(R.id.buttonOk);
		buttonOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DisplayImageActivity activity = (DisplayImageActivity) getActivity();
				activity.processUpdatedComment(editText.getText().toString(), true);
			}
		});

	}

	/**
	 * Hide the soft keyboard triggered from this listFoldersFragment
	 */
	public void hideKeyboard() {
		KeyboardUtil.hideKeyboard(getActivity(), editText);
	}

}
