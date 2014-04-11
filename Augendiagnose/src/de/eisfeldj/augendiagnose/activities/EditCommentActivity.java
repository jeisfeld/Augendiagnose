package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import de.eisfeldj.augendiagnose.R;

public class EditCommentActivity extends Activity {

	private static final String STRING_EXTRA_TEXT = "de.eisfeldj.augendiagnose.TEXT";
	private static final String STRING_RESULT_TEXT = "de.eisfeldj.augendiagnose.RESULTTEXT";
	public static final int REQUEST_CODE = 1;

	private EditText editText;

	/**
	 * Static helper method to start the activity, passing the old value of the text
	 * 
	 * @param context
	 * @param resource
	 */
	public static void startActivity(Activity activity, String text) {
		Intent intent = new Intent(activity, EditCommentActivity.class);
		intent.putExtra(STRING_EXTRA_TEXT, text);
		activity.startActivityForResult(intent, REQUEST_CODE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_comment);
		getActionBar().setDisplayHomeAsUpEnabled(false);

		String text = getIntent().getStringExtra(STRING_EXTRA_TEXT);

		editText = (EditText) findViewById(R.id.input_edit_comment);
		editText.setText(text);

		editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
			}
		});

		final Button buttonCancel = (Button) findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				returnResult(editText.getText().toString(), false);
			}
		});

		final Button buttonClear = (Button) findViewById(R.id.buttonClear);
		buttonClear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				editText.setText("");
			}
		});

		final Button buttonOk = (Button) findViewById(R.id.buttonOk);
		buttonOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				returnResult(editText.getText().toString(), true);
			}
		});

	}

	/**
	 * Helper method: Return the edited text and finish the activity
	 */
	public void returnResult(String text, boolean success) {
		Bundle resultData = new Bundle();
		resultData.putCharSequence(STRING_RESULT_TEXT, text);
		Intent intent = new Intent();
		intent.putExtras(resultData);
		if (success) {
			setResult(RESULT_OK, intent);
		}
		else {
			setResult(RESULT_CANCELED, intent);
		}
		finish();
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
			return res.getCharSequence(STRING_RESULT_TEXT);
		}
		else {
			return null;
		}
	}
}
