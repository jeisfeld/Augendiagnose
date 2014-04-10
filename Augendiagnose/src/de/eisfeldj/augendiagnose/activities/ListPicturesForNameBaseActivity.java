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
	public ListPicturesForNameBaseFragment fragment;

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
