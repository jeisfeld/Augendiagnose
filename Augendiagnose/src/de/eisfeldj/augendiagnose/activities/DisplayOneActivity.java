package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.fragments.DisplayOneFragment;

/**
 * Activity to display one photo on full screen
 * 
 * @author Joerg
 */
public class DisplayOneActivity extends Activity {
	protected static final String STRING_EXTRA_TYPE = "de.eisfeldj.augendiagnose.TYPE";
	protected static final String STRING_EXTRA_FILE = "de.eisfeldj.augendiagnose.FILE";
	protected static final String STRING_EXTRA_FILERESOURCE = "de.eisfeldj.augendiagnose.FILERESOURCE";
	protected static final int TYPE_FILENAME = 1;
	protected static final int TYPE_FILERESOURCE = 2;

	private static final String FRAGMENT_TAG = "FRAGMENT_TAG";
	protected DisplayOneFragment fragment;

	/**
	 * Static helper method to start the activity, passing the path of the picture.
	 * 
	 * @param context
	 * @param filename
	 */
	public static void startActivity(Context context, String filename) {
		Intent intent = new Intent(context, DisplayOneActivity.class);
		intent.putExtra(STRING_EXTRA_FILE, filename);
		intent.putExtra(STRING_EXTRA_TYPE, TYPE_FILENAME);
		context.startActivity(intent);
	}

	/**
	 * Static helper method to start the activity, passing the path of the picture.
	 * 
	 * @param context
	 * @param filename
	 */
	public static void startActivity(Context context, int fileResource) {
		Intent intent = new Intent(context, DisplayOneActivity.class);
		intent.putExtra(STRING_EXTRA_FILERESOURCE, fileResource);
		intent.putExtra(STRING_EXTRA_TYPE, TYPE_FILERESOURCE);
		context.startActivity(intent);
	}

	/**
	 * Build the screen on creation
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int type = getIntent().getIntExtra(STRING_EXTRA_TYPE, -1);
		String file = getIntent().getStringExtra(STRING_EXTRA_FILE);
		int fileResource = getIntent().getIntExtra(STRING_EXTRA_FILERESOURCE, -1);

		setContentView(R.layout.activity_fragments_single);

		fragment = (DisplayOneFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);

		if (fragment == null) {
			fragment = createFragment();
			if (type == TYPE_FILENAME) {
				fragment.setParameters(file, 1);
			}
			else {
				fragment.setParameters(fileResource, 1);
			}

			getFragmentManager().beginTransaction().add(R.id.fragment_container, fragment, FRAGMENT_TAG)
					.commit();
			getFragmentManager().executePendingTransactions();
		}
	}

	/**
	 * Factory method to return the fragment
	 * 
	 * @return
	 */
	protected DisplayOneFragment createFragment() {
		return new DisplayOneFragment();
	}

	/**
	 * Workaround to ensure that all views have restored status before images are re-initialized
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus) {
			fragment.initializeImages();
		}
	}
}
