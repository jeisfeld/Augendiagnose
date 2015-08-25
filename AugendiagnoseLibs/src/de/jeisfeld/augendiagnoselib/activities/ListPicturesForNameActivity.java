package de.jeisfeld.augendiagnoselib.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.fragments.ListPicturesForNameBaseFragment;
import de.jeisfeld.augendiagnoselib.fragments.ListPicturesForNameFragment;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.ImageSelectionAndDisplayHandler;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;

/**
 * Activity to display the pictures in an eye photo folder (in pairs) Either pictures from this folder can be displayed
 * directly, or another folder can be selected for a second picture.
 */
public class ListPicturesForNameActivity extends ListPicturesForNameBaseActivity {
	/**
	 * Static helper method to start the activity, passing the path of the parent folder and the name of the current
	 * folder.
	 *
	 * @param context
	 *            The context in which the activity is started.
	 * @param parentFolder
	 *            The parent folder of the application.
	 * @param name
	 *            The name of the image folder to be shown.
	 */
	public static final void startActivity(final Context context, final String parentFolder, final String name) {
		Intent intent = new Intent(context, ListPicturesForNameActivity.class);
		intent.putExtra(STRING_EXTRA_PARENTFOLDER, parentFolder);
		intent.putExtra(STRING_EXTRA_NAME, name);
		context.startActivity(intent);
	}

	/*
	 * Get the listFoldersFragment displayed in the activity.
	 */
	@Override
	protected final ListPicturesForNameBaseFragment createFragment() {
		return new ListPicturesForNameFragment();
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Initialize the handler which manages the clicks
		ImageSelectionAndDisplayHandler.getInstance().setActivity(this);

		if (savedInstanceState == null) {
			PreferenceUtil.incrementCounter(R.string.key_statistics_countlistpictures);
		}

		DialogUtil.displayTip(this, R.string.message_tip_displaypictures, R.string.key_tip_displaypictures);

		requestBannerAdIfEligible();
	}

}
