package de.jeisfeld.augendiagnoselib.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import de.jeisfeld.augendiagnoselib.fragments.ListPicturesForNameBaseFragment;
import de.jeisfeld.augendiagnoselib.fragments.ListPicturesForSecondNameFragment;
import de.jeisfeld.augendiagnoselib.util.ImageSelectionAndDisplayHandler;

/**
 * Activity to display the pictures in an eye photo folder (in pairs) This is for the selection of a second picture for
 * display.
 */
public class ListPicturesForSecondNameActivity extends ListPicturesForNameBaseActivity {
	/**
	 * The requestCode with which this activity is started.
	 */
	public static final int REQUEST_CODE = 1;

	/**
	 * Static helper method to start the activity, passing the path of the parent folder and the name of the current
	 * folder.
	 *
	 * @param activity     The activity triggering this activity.
	 * @param parentFolder The parent folder of the application.
	 * @param name         The name of the image folder to be shown.
	 */
	public static final void startActivity(final Activity activity, final String parentFolder, final String name) {
		Intent intent = new Intent(activity, ListPicturesForSecondNameActivity.class);
		intent.putExtra(STRING_EXTRA_PARENTFOLDER, parentFolder);
		intent.putExtra(STRING_EXTRA_NAME, name);
		activity.startActivityForResult(intent, REQUEST_CODE);
	}

	/*
	 * Get the listFoldersFragment displayed in the activity.
	 */
	@Override
	protected final ListPicturesForNameBaseFragment createFragment() {
		return new ListPicturesForSecondNameFragment();
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initialize the handler which manages the clicks
		ImageSelectionAndDisplayHandler.getInstance().setSecondActivity(this);
	}

}
