package de.eisfeldj.augendiagnose.activities;

import java.io.File;

import android.app.Fragment;
import android.app.FragmentManager;
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
import de.eisfeldj.augendiagnose.fragments.ListPicturesForNameFragment.ListPicturesForNameFragmentHolder;
import de.eisfeldj.augendiagnose.util.ImageSelectionAndDisplayHandler;

/**
 * Activity to display the list of subfolders of the eye photo folder with the goal to display them after selection.
 */
public class ListFoldersForDisplayActivity extends ListFoldersBaseActivity implements ListPicturesForNameFragmentHolder {
	private static final String FRAGMENT_TAG = "FRAGMENT_TAG";
	private static final String FRAGMENT_LISTPICTURES_TAG = "FRAGMENT_LISTPICTURES_TAG";
	public ListPicturesForNameFragment listPicturesFragment;

	/**
	 * Static helper method to start the activity, passing the path of the folder
	 * 
	 * @param context
	 * @param foldername
	 */
	public static void startActivity(Context context, String foldername) {
		Intent intent = new Intent(context, ListFoldersForDisplayActivity.class);
		intent.putExtra(STRING_EXTRA_FOLDER, foldername);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		if (Application.isTablet()) {
			setContentView(R.layout.activity_fragments_list_detail);
		}
		else {
			setContentView(R.layout.activity_fragments_single);
		}

		fragment = (ListFoldersBaseFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);

		if (fragment == null) {
			fragment = new ListFoldersForDisplayFragment();
			setFragmentParameters(fragment);

			if (Application.isTablet()) {
				getFragmentManager().beginTransaction().add(R.id.fragment_list, fragment, FRAGMENT_TAG).commit();

				String defaultName = Application.getSharedPreferenceString(R.string.key_internal_last_name);
				if (defaultName.length() > 0 && new File(parentFolder, defaultName).exists()) {
					listPicturesForName(defaultName);
				}

				getFragmentManager().executePendingTransactions();
			}
			else {
				displayOnFullScreen(fragment, FRAGMENT_TAG);
			}
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
	public void listPicturesForName(String name) {
		if (Application.isTablet()) {
			ImageSelectionAndDisplayHandler.getInstance().setActivity(this);

			listPicturesFragment = new ListPicturesForNameFragment();
			listPicturesFragment.setParameters(parentFolder, name);

			FragmentTransaction transaction = getFragmentManager().beginTransaction().replace(R.id.fragment_detail,
					listPicturesFragment, FRAGMENT_LISTPICTURES_TAG);
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

		// Store the name so that it may be opened automatically next time
		Application.setSharedPreferenceString(R.string.key_internal_last_name, name);
		Application.setSharedPreferenceBoolean(R.string.key_internal_organized_new_photo, false);
	}

	/**
	 * Remove the back stack on the "ListPicturesForName" pane and clean the pane.
	 */
	public void popBackStack() {
		if (Application.isTablet()) {
			getFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

			Fragment fragment = getFragmentManager().findFragmentByTag(FRAGMENT_LISTPICTURES_TAG);
			if (fragment != null) {
				getFragmentManager().beginTransaction().remove(fragment).commit();
				getFragmentManager().executePendingTransactions();
			}
		}
	}

	// implementation of ListPicturesForNameFragmentHolder

	@Override
	public ListPicturesForNameFragment getListPicturesForNameFragment() {
		return listPicturesFragment;
	}

	@Override
	public void setListPicturesForNameFragment(ListPicturesForNameFragment fragment) {
		this.listPicturesFragment = fragment;
	}

}
