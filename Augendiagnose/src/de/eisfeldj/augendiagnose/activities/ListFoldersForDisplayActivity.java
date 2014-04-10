package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.fragments.ListFoldersBaseFragment;
import de.eisfeldj.augendiagnose.fragments.ListFoldersForDisplayFragment;
import de.eisfeldj.augendiagnose.fragments.ListPicturesForNameFragment;
import de.eisfeldj.augendiagnose.util.ImageSelectionAndDisplayHandler;

/**
 * Activity to display the list of subfolders of the eye photo folder with the goal to display them after selection.
 */
public class ListFoldersForDisplayActivity extends Activity {

	/**
	 * Static helper method to start the activity, passing the path of the folder
	 * 
	 * @param context
	 * @param foldername
	 */
	public static void startActivity(Context context, String foldername) {
		Intent intent = new Intent(context, ListFoldersForDisplayActivity.class);
		intent.putExtra(ListFoldersBaseFragment.STRING_EXTRA_FOLDER, foldername);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		ListFoldersForDisplayFragment fragment = new ListFoldersForDisplayFragment();

		if (Application.isTablet()) {
			setContentView(R.layout.activity_fragments_list_detail);
			getFragmentManager().beginTransaction().replace(R.id.fragment_list, fragment).commit();
			getFragmentManager().executePendingTransactions();
		}
		else {
			setContentView(R.layout.activity_fragments_single);
			getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
			getFragmentManager().executePendingTransactions();
		}

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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ImageSelectionAndDisplayHandler.clean();
	}

	/**
	 * Display the list of pictures for a selected name
	 */
	public void listPicturesForName(String parentFolder, String name) {
		if (Application.isTablet()) {
			ImageSelectionAndDisplayHandler.getInstance().setActivity(this);
			
			ListPicturesForNameFragment fragment = new ListPicturesForNameFragment();
			fragment.setParameters(parentFolder, name);
			
			FragmentTransaction transaction = getFragmentManager().beginTransaction().replace(R.id.fragment_detail, fragment);
			if (findViewById(R.id.listViewForName) != null) {
				// if right pane is filled, then add it to the back stack
				transaction.addToBackStack(null);
			}
			transaction.commit();
			getFragmentManager().executePendingTransactions();
		}
		else {
			ListPicturesForNameActivity.startActivity(this, parentFolder, name);
		}
	}

}
