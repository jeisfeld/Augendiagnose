package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import de.eisfeldj.augendiagnose.fragments.ListPicturesForNameBaseFragment;
import de.eisfeldj.augendiagnose.fragments.ListPicturesForSecondNameFragment;
import de.eisfeldj.augendiagnose.util.ImageSelectionAndDisplayHandler;

/**
 * Activity to display the pictures in an eye photo folder (in pairs) This is for the selection of a second picture for
 * display.
 */
public class ListPicturesForSecondNameActivity extends ListPicturesForNameBaseActivity {
	public static final int REQUEST_CODE = 3;
	private static final String STRING_EXTRA_NAME = "de.eisfeldj.augendiagnose.NAME";
	private static final String STRING_EXTRA_PARENTFOLDER = "de.eisfeldj.augendiagnose.PARENTFOLDER";
	public static final String STRING_EXTRA_FILEPATH = "de.eisfeldj.augendiagnose.FILEPATH";

	/**
	 * Static helper method to start the activity, passing the path of the parent folder and the name of the current
	 * folder.
	 * 
	 * @param activity
	 * @param parentFolder
	 * @param name
	 */
	public static void startActivity(Activity activity, String parentFolder, String name) {
		Intent intent = new Intent(activity, ListPicturesForSecondNameActivity.class);
		intent.putExtra(STRING_EXTRA_PARENTFOLDER, parentFolder);
		intent.putExtra(STRING_EXTRA_NAME, name);
		activity.startActivityForResult(intent, REQUEST_CODE);
	}

	/**
	 * Get the fragment displayed in the activity
	 */
	@Override
	protected ListPicturesForNameBaseFragment getFragment() {
		return new ListPicturesForSecondNameFragment();
	}

	/**
	 * Static helper method to extract the name of the selected file from the activity response
	 * 
	 * @param resultCode
	 * @param data
	 *            The activity response
	 * @return
	 */
	public static String getResult(int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Bundle res = data.getExtras();
			return res.getString(STRING_EXTRA_FILEPATH);
		}
		else {
			return "";
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initialize the handler which manages the clicks
		ImageSelectionAndDisplayHandler.getInstance().setSecondActivity(this);
	}

}
