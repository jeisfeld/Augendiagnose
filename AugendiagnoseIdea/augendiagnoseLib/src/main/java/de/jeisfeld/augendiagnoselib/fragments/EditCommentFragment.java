package de.jeisfeld.augendiagnoselib.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.activities.DisplayImageActivity;
import de.jeisfeld.augendiagnoselib.util.KeyboardUtil;

/**
 * Fragment to add the comment of a picture.
 */
public class EditCommentFragment extends Fragment {

	/**
	 * The resource key for the initial text.
	 */
	private static final String STRING_TEXT = "de.jeisfeld.augendiagnoselib.TEXT";

	/**
	 * The EditText displaying the comment.
	 */
	private EditText mEditText;

	/**
	 * The comment text.
	 */
	@Nullable
	private String mText;

	/**
	 * Initialize the EditCommentFragment with the text.
	 *
	 * @param initialText The initial text of the comment.
	 */
	public final void setParameters(final String initialText) {
		Bundle args = new Bundle();
		args.putString(STRING_TEXT, initialText);

		setArguments(args);
	}

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mText = getArguments().getString(STRING_TEXT);
	}

	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
								   final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_edit_comment, container, false);
	}

	@Override
	public final void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (getView() == null) {
			return;
		}

		mEditText = (EditText) getView().findViewById(R.id.input_edit_comment);
		mEditText.setText(mText);

		mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(final View v, final boolean hasFocus) {
				if (hasFocus) {
					getActivity().getWindow().setSoftInputMode(
							WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
			}
		});

		final Button buttonCancel = (Button) getView().findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				DisplayImageActivity activity = (DisplayImageActivity) getActivity();
				activity.processUpdatedComment(mEditText.getText().toString(), false);
			}
		});

		final Button buttonClear = (Button) getView().findViewById(R.id.buttonClear);
		buttonClear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				mEditText.setText("");
			}
		});

		final Button buttonOk = (Button) getView().findViewById(R.id.buttonOk);
		buttonOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				DisplayImageActivity activity = (DisplayImageActivity) getActivity();
				activity.processUpdatedComment(mEditText.getText().toString(), true);
			}
		});

	}

	/**
	 * Hide the soft keyboard triggered from this listFoldersFragment.
	 */
	public final void hideKeyboard() {
		KeyboardUtil.hideKeyboard(getActivity(), mEditText);
	}

}
