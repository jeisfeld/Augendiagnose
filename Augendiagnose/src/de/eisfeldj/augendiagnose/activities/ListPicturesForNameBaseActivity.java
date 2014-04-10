package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.fragments.ListPicturesForNameBaseFragment;

/**
 * Base activity to display the pictures in an eye photo folder (in pairs) Abstract class - child classes determine the
 * detailed actions.
 */
public abstract class ListPicturesForNameBaseActivity extends Activity {
	private static final String STRING_EXTRA_NAME = "de.eisfeldj.augendiagnose.NAME";
	private static final String STRING_EXTRA_PARENTFOLDER = "de.eisfeldj.augendiagnose.PARENTFOLDER";

	public ListPicturesForNameBaseFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String name = getIntent().getStringExtra(STRING_EXTRA_NAME);
		String parentFolder = getIntent().getStringExtra(STRING_EXTRA_PARENTFOLDER);

		setContentView(R.layout.activity_fragments_single);

		fragment = getFragment();
		fragment.setParameters(parentFolder, name);

		getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
	}

	/**
	 * Factory method to retrieve the fragment
	 */
	protected abstract ListPicturesForNameBaseFragment getFragment();

	/**
	 * onClick action for Button "additional pictures"
	 * 
	 * @param view
	 */
	public void selectDifferentPictureActivity(View view) {
		ListFoldersForDisplaySecondActivity.startActivity(this, fragment.parentFolder);
	}
}
