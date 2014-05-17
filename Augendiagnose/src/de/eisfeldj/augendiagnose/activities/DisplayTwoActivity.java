package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.fragments.DisplayOneFragment;
import de.eisfeldj.augendiagnose.fragments.DisplayOneOverlayFragmentHalfscreen;

/**
 * Activity to display two pictures on full screen (screen split in two halves)
 */
public class DisplayTwoActivity extends Activity {
	private static final String STRING_EXTRA_FILE1 = "de.eisfeldj.augendiagnose.FILE1";
	private static final String STRING_EXTRA_FILE2 = "de.eisfeldj.augendiagnose.FILE2";

	private static final String FRAGMENT_IMAGE1_TAG = "FRAGMENT_IMAGE1_TAG";
	private static final String FRAGMENT_IMAGE2_TAG = "FRAGMENT_IMAGE2_TAG";

	private DisplayOneFragment fragmentImage1, fragmentImage2;

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

		setContentView(R.layout.fragment_display_two);

		fragmentImage1 = (DisplayOneFragment) getFragmentManager().findFragmentByTag(FRAGMENT_IMAGE1_TAG);
		if (fragmentImage1 == null) {
			fragmentImage1 = createFragment();
			fragmentImage1.setParameters(file1, 1);

			getFragmentManager().beginTransaction().add(R.id.fragment_image1, fragmentImage1, FRAGMENT_IMAGE1_TAG)
					.commit();
		}

		fragmentImage2 = (DisplayOneFragment) getFragmentManager().findFragmentByTag(FRAGMENT_IMAGE2_TAG);
		if (fragmentImage2 == null) {
			fragmentImage2 = createFragment();
			fragmentImage2.setParameters(file2, 2);

			getFragmentManager().beginTransaction().add(R.id.fragment_image2, fragmentImage2, FRAGMENT_IMAGE2_TAG)
					.commit();
		}

		getFragmentManager().executePendingTransactions();

	}

	/**
	 * Helper method to create the fragment
	 * 
	 * @return
	 */
	protected DisplayOneFragment createFragment() {
		return new DisplayOneOverlayFragmentHalfscreen();
	}

	/**
	 * Workaround to ensure that all views have restored status before images are re-initialized
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus) {
			fragmentImage1.initializeImages();
			fragmentImage2.initializeImages();
		}
	}

}
