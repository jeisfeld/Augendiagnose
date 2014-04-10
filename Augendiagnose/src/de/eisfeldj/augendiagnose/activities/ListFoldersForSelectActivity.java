package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import de.eisfeldj.augendiagnose.fragments.ListFoldersBaseFragment;
import de.eisfeldj.augendiagnose.fragments.ListFoldersForSelectFragment;

/**
 * Activity to display the list of subfolders of the eye photo folder as dialog with the goal to select a name for
 * ordering new pictures. (The folder names equal the person names.)
 */
public class ListFoldersForSelectActivity extends ListFoldersBaseActivity {
	public static final int REQUEST_CODE = 1;
	private static final String STRING_EXTRA_NAME = "de.eisfeldj.augendiagnose.NAME";
	public static final String STRING_EXTRA_PRESELECTED_NAME = "de.eisfeldj.augendiagnose.PRESELECTED_NAME";

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
	 * Get the fragment displayed in the activity
	 */
	@Override
	protected ListFoldersBaseFragment getFragment() {
		return new ListFoldersForSelectFragment();
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

	/**
	 * Helper method: Return the selected name and finish the activity
	 */
	public void returnResult(CharSequence result) {
		Bundle resultData = new Bundle();
		resultData.putCharSequence(STRING_EXTRA_NAME, result);
		Intent intent = new Intent();
		intent.putExtras(resultData);
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

}
