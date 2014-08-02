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
	 * Get the listFoldersFragment displayed in the activity
	 */
	@Override
	protected ListPicturesForNameBaseFragment createFragment() {
		return new ListPicturesForSecondNameFragment();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initialize the handler which manages the clicks
		ImageSelectionAndDisplayHandler.getInstance().setSecondActivity(this);
	}

}
