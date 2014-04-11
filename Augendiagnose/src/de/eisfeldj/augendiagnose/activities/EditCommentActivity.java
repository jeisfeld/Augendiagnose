package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.fragments.EditCommentFragment;
import de.eisfeldj.augendiagnose.fragments.EditCommentFragment.EditCommentCallback;

/**
 * Activity to add the comment of a picture
 */
public class EditCommentActivity extends Activity implements EditCommentCallback {

	private static final String STRING_EXTRA_TEXT = "de.eisfeldj.augendiagnose.TEXT";
	private static final String STRING_RESULT_TEXT = "de.eisfeldj.augendiagnose.RESULTTEXT";
	public static final int REQUEST_CODE = 1;

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
		getActionBar().setDisplayHomeAsUpEnabled(false);

		String text = getIntent().getStringExtra(STRING_EXTRA_TEXT);

		setContentView(R.layout.activity_fragments_single);

		EditCommentFragment fragment = new EditCommentFragment();
		fragment.setParameters(text);

		getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
		getFragmentManager().executePendingTransactions();
	}

	/**
	 * Helper method: Process the updated comment returned from the fragment
	 */
	public void processUpdatedComment(String text, boolean success) {
		returnResult(text, success);
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
