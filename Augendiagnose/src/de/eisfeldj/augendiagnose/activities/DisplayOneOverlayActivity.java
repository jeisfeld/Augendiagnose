package de.eisfeldj.augendiagnose.activities;

import android.content.Context;
import android.content.Intent;
import de.eisfeldj.augendiagnose.fragments.DisplayOneFragment;
import de.eisfeldj.augendiagnose.fragments.DisplayOneOverlayFragment;

/**
 * Variant of DisplayOneFragment that includes overlay handling
 * 
 * @author Joerg
 */
public class DisplayOneOverlayActivity extends DisplayOneActivity {
	/**
	 * Static helper method to start the activity, passing the path of the picture.
	 * 
	 * @param context
	 * @param filename
	 */
	public static void startActivity(Context context, String filename) {
		Intent intent = new Intent(context, DisplayOneOverlayActivity.class);
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
		Intent intent = new Intent(context, DisplayOneOverlayActivity.class);
		intent.putExtra(STRING_EXTRA_FILERESOURCE, fileResource);
		intent.putExtra(STRING_EXTRA_TYPE, TYPE_FILERESOURCE);
		context.startActivity(intent);
	}

	/**
	 * Factory method to return the fragment
	 * 
	 * @return
	 */
	@Override
	protected DisplayOneFragment createFragment() {
		fragment = new DisplayOneOverlayFragment();
		return fragment;
	}

}
