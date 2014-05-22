package de.eisfeldj.augendiagnose.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.fragments.DisplayImageFragment;
import de.eisfeldj.augendiagnose.fragments.EditCommentFragment;
import de.eisfeldj.augendiagnose.util.AndroidBug5497Workaround;

/**
 * Variant of DisplayOneFragment that includes overlay handling
 * 
 * @author Joerg
 */
public class DisplayOneActivity extends DisplayImageActivity {
	private static final String STRING_EXTRA_TYPE = "de.eisfeldj.augendiagnose.TYPE";
	private static final String STRING_EXTRA_FILE = "de.eisfeldj.augendiagnose.FILE";
	private static final String STRING_EXTRA_FILERESOURCE = "de.eisfeldj.augendiagnose.FILERESOURCE";
	private static final int TYPE_FILENAME = 1;
	private static final int TYPE_FILERESOURCE = 2;

	private static final String FRAGMENT_TAG = "FRAGMENT_TAG";

	private DisplayImageFragment fragmentImage;

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

		setContentView(R.layout.activity_display_one);

		fragmentImage = (DisplayImageFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);

		if (fragmentImage == null) {
			fragmentImage = new DisplayImageFragment();
			if (type == TYPE_FILENAME) {
				fragmentImage.setParameters(file, 1);
			}
			else {
				fragmentImage.setParameters(fileResource, 1);
			}

			getFragmentManager().beginTransaction().add(R.id.fragment_image, fragmentImage, FRAGMENT_TAG).commit();
			getFragmentManager().executePendingTransactions();
		}

		viewFragmentEdit = findViewById(R.id.fragment_edit);
		viewLayoutMain = findViewById(android.R.id.content);

		// Restore in case of orientation change
		fragmentEdit = (EditCommentFragment) getFragmentManager().findFragmentByTag(FRAGMENT_EDIT_TAG);

		if (savedInstanceState != null) {
			int fragmentEditVisibility = savedInstanceState.getInt("fragmentEditVisibility");
			viewFragmentEdit.setVisibility(fragmentEditVisibility);
		}

		// ensure that layout is refreshed if view gets resized
		AndroidBug5497Workaround.assistActivity(this);
	}

	/**
	 * Initialize the images
	 */
	@Override
	protected void initializeImages() {
		fragmentImage.initializeImages();
	}

	// implemenation of interface ActivityWithExplicitLayoutTrigger

	@Override
	public void requestLayout() {
		viewLayoutMain.invalidate();
		fragmentImage.requestLayout();
	}
}
