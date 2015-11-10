package de.jeisfeld.augendiagnoselib.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.fragments.ListFoldersBaseFragment;
import de.jeisfeld.augendiagnoselib.fragments.ListFoldersForDisplaySecondFragment;

/**
 * Activity to display the list of subfolders of the eye photo folder as dialog with the goal to select a second picture
 * for display.
 */
public class ListFoldersForDisplaySecondActivity extends ListFoldersBaseActivity {
	/**
	 * The fragment tag.
	 */
	private static final String FRAGMENT_TAG = "FRAGMENT_TAG";

	/**
	 * Static helper method to start the activity, passing the path of the folder.
	 *
	 * @param context The context in which the activity is started.
	 */
	public static final void startActivity(final Context context) {
		Intent intent = new Intent(context, ListFoldersForDisplaySecondActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fragments_single);

		setListFoldersFragment((ListFoldersBaseFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG));

		if (getListFoldersFragment() == null) {
			setListFoldersFragment(new ListFoldersForDisplaySecondFragment());
			setFragmentParameters(getListFoldersFragment());
			displayOnFullScreen(getListFoldersFragment(), FRAGMENT_TAG);
		}
	}

	/*
	 * When getting the response from the picture selection, return the name of the selected picture and finish the
	 * activity.
	 */
	@Override
	protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
		case ListPicturesForSecondNameActivity.REQUEST_CODE:
			// When picture is selected, close also the list of names
			finish();
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

}
