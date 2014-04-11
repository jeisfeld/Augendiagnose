package de.eisfeldj.augendiagnose.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.fragments.ListPicturesForNameBaseFragment;
import de.eisfeldj.augendiagnose.fragments.ListPicturesForNameFragment;
import de.eisfeldj.augendiagnose.fragments.ListPicturesForNameFragment.ListPicturesForNameFragmentHolder;
import de.eisfeldj.augendiagnose.util.ImageSelectionAndDisplayHandler;

/**
 * Activity to display the pictures in an eye photo folder (in pairs) Either pictures from this folder can be displayed
 * directly, or another folder can be selected for a second picture.
 */
public class ListPicturesForNameActivity extends ListPicturesForNameBaseActivity implements ListPicturesForNameFragmentHolder {
	/**
	 * Static helper method to start the activity, passing the path of the parent folder and the name of the current
	 * folder.
	 * 
	 * @param activity
	 * @param parentFolder
	 * @param name
	 */
	public static void startActivity(Context context, String parentFolder, String name) {
		Intent intent = new Intent(context, ListPicturesForNameActivity.class);
		intent.putExtra(STRING_EXTRA_PARENTFOLDER, parentFolder);
		intent.putExtra(STRING_EXTRA_NAME, name);
		context.startActivity(intent);
	}


	/**
	 * Get the fragment displayed in the activity
	 */
	@Override
	protected ListPicturesForNameBaseFragment createFragment() {
		return new ListPicturesForNameFragment();
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Initialize the handler which manages the clicks
		ImageSelectionAndDisplayHandler.getInstance().setActivity(this);
	}

	/**
	 * Inflate options menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_only_help, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Handle menu actions
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_help:
			DisplayHtmlActivity.startActivity(this, R.string.html_display_photos);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// implementation of ListPicturesForNameFragmentHolder

	@Override
	public ListPicturesForNameFragment getListPicturesForNameFragment() {
		return (ListPicturesForNameFragment) fragment;
	}


	@Override
	public void setListPicturesForNameFragment(ListPicturesForNameFragment fragment) {
		this.fragment = fragment;
	}


}
