package de.eisfeldj.augendiagnose.activities;

import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.fragments.ListFoldersBaseFragment;
import android.app.Activity;
import android.os.Bundle;

/**
 * Base activity to display the list of subfolders of a folder Abstract class - child classes determine the detailed
 * actions. The folders should contain eye photos (following the name policy).
 */
public abstract class ListFoldersBaseActivity extends Activity {
	protected static final String STRING_EXTRA_FOLDER = "de.eisfeldj.augendiagnose.FOLDER";

	protected String parentFolder;
	public ListFoldersBaseFragment listFoldersFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		parentFolder = getIntent().getStringExtra(STRING_EXTRA_FOLDER);
	}

	/**
	 * Populate the listFoldersFragment with parameters
	 *
	 * @param listFoldersFragment
	 */
	protected void setFragmentParameters(ListFoldersBaseFragment fragment) {
		fragment.setParameters(parentFolder);
	}


	/**
	 * Display the listFoldersFragment in the main listFoldersFragment container
	 *
	 * @param listFoldersFragment
	 */
	protected void displayOnFullScreen(ListFoldersBaseFragment fragment, String tag) {
		getFragmentManager().beginTransaction().add(R.id.fragment_container, fragment, tag).commit();
		getFragmentManager().executePendingTransactions();
	}

}
