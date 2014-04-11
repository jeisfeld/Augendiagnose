package de.eisfeldj.augendiagnose.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import de.eisfeldj.augendiagnose.fragments.ListFoldersForDisplaySecondFragment;

/**
 * Activity to display the list of subfolders of the eye photo folder as dialog with the goal to select a second picture
 * for display.
 */
public class ListFoldersForDisplaySecondActivity extends ListFoldersBaseActivity {
	/**
	 * Static helper method to start the activity, passing the path of the folder
	 * 
	 * @param context
	 * @param foldername
	 */
	public static void startActivity(Context context, String foldername) {
		Intent intent = new Intent(context, ListFoldersForDisplaySecondActivity.class);
		intent.putExtra(STRING_EXTRA_FOLDER, foldername);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ListFoldersForDisplaySecondFragment fragment = new ListFoldersForDisplaySecondFragment();
		setFragmentParameters(fragment);
		displayOnFullScreen(fragment);
	}

	/**
	 * When getting the response from the picture selection, return the name of the selected picture and finish the
	 * activity.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ListPicturesForSecondNameActivity.REQUEST_CODE:
			// When picture is selected, close also the list of names
			finish();
			break;
		}
	}

}
