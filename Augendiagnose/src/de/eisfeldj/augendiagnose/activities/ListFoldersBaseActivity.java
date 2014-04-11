package de.eisfeldj.augendiagnose.activities;

import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.fragments.ListFoldersBaseFragment;
import android.app.Activity;
import android.os.Bundle;

/**
 * Base Activity to display the list of subfolders of the eye photo folder
 */
public abstract class ListFoldersBaseActivity extends Activity {
	protected static final String STRING_EXTRA_FOLDER = "de.eisfeldj.augendiagnose.FOLDER";

	protected String parentFolder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		parentFolder = getIntent().getStringExtra(STRING_EXTRA_FOLDER);
	}

	/**
	 * Populate the fragment with parameters
	 * 
	 * @param fragment
	 */
	protected void setFragmentParameters(ListFoldersBaseFragment fragment) {
		fragment.setParameters(parentFolder);
	}
	

	/**
	 * Display the fragment in the main fragment container
	 * 
	 * @param fragment
	 */
	protected void displayOnFullScreen(ListFoldersBaseFragment fragment) {
		setContentView(R.layout.activity_fragments_single);
		getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
		getFragmentManager().executePendingTransactions();
	}

}
