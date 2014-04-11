package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.fragments.DisplayTwoFragment;

/**
 * Activity to display two pictures on full screen (screen split in two halves)
 */
public class DisplayTwoActivity extends Activity {
	private static final String STRING_EXTRA_FILE1 = "de.eisfeldj.augendiagnose.FILE1";
	private static final String STRING_EXTRA_FILE2 = "de.eisfeldj.augendiagnose.FILE2";

	private static final String FRAGMENT_TAG = "FRAGMENT_TAG";
	
	private DisplayTwoFragment fragment;

	/**
	 * Static helper method to start the activity, passing the paths of the two pictures.
	 * 
	 * @param context
	 * @param filename1
	 * @param filename2
	 */
	public static void startActivity(Context context, String filename1, String filename2) {
		Intent intent = new Intent(context, DisplayTwoActivity.class);
		intent.putExtra(STRING_EXTRA_FILE1, filename1);
		intent.putExtra(STRING_EXTRA_FILE2, filename2);
		context.startActivity(intent);
	}

	/**
	 * Build the screen on creation
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String file1 = getIntent().getStringExtra(STRING_EXTRA_FILE1);
		String file2 = getIntent().getStringExtra(STRING_EXTRA_FILE2);

		setContentView(R.layout.activity_fragments_single);
		
		fragment = (DisplayTwoFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);

		if (fragment == null) {
			fragment = new DisplayTwoFragment();
			fragment.setParameters(file1, file2);

			getFragmentManager().beginTransaction().add(R.id.fragment_container, fragment, FRAGMENT_TAG).commit();
			getFragmentManager().executePendingTransactions();
		}
	}
	
	/**
	 * Workaround to ensure that all views have restored status before images are re-initialized
	 */
	@Override
    public void onWindowFocusChanged(boolean hasFocus) {
		if(hasFocus) {
			fragment.initializeImages();
		}
    }
	

}
