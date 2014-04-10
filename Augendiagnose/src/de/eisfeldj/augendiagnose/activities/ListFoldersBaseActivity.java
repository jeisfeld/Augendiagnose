package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.os.Bundle;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.fragments.ListFoldersBaseFragment;

/**
 * Base activity to display the list of subfolders of a folder Abstract class - child classes determine the detailed
 * actions. The folders should contain eye photos (following the name policy).
 */
public abstract class ListFoldersBaseActivity extends Activity {
	public static final String STRING_EXTRA_FOLDER = "de.eisfeldj.augendiagnose.FOLDER";
	
	protected ListFoldersBaseFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_one_frame);

		fragment = getFragment();

		getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
	}

	/**
	 * Factory method to retrieve the fragment
	 */
	protected abstract ListFoldersBaseFragment getFragment();

}
