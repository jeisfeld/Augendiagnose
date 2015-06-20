package de.eisfeldj.augendiagnose.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.fragments.DisplayImageFragment;
import de.eisfeldj.augendiagnose.fragments.EditCommentFragment;
import de.eisfeldj.augendiagnose.util.AutoKeyboardLayoutUtility;
import de.eisfeldj.augendiagnose.util.DialogUtil;

/**
 * Variant of DisplayOneFragment that includes overlay handling.
 *
 * @author Joerg
 */
public class DisplayOneActivity extends DisplayImageActivity {
	/**
	 * The resource key for the image type (TYPE_FILENAME or TYPE_FILERESOURCE).
	 */
	private static final String STRING_EXTRA_TYPE = "de.eisfeldj.augendiagnose.TYPE";
	/**
	 * The resource key for the file path.
	 */
	private static final String STRING_EXTRA_FILE = "de.eisfeldj.augendiagnose.FILE";
	/**
	 * The resource key for the file resource.
	 */
	private static final String STRING_EXTRA_FILERESOURCE = "de.eisfeldj.augendiagnose.FILERESOURCE";

	/**
	 * Type value set if the activity shows an image by filename.
	 */
	private static final int TYPE_FILENAME = 1;
	/**
	 * Type value set if the activity shows an image by resource id.
	 */
	private static final int TYPE_FILERESOURCE = 2;

	/**
	 * The fragment tag.
	 */
	private static final String FRAGMENT_TAG = "FRAGMENT_TAG";

	/**
	 * The view displaying the files.
	 */
	private View viewFragmentImage;

	/**
	 * The fragment displaying the image.
	 */
	private DisplayImageFragment fragmentImage;

	/**
	 * Static helper method to start the activity, passing the path of the picture.
	 *
	 * @param context
	 *            The context in which the activity is started.
	 * @param filename
	 *            The filename of the picture.
	 */
	public static void startActivity(final Context context, final String filename) {
		Intent intent = new Intent(context, DisplayOneActivity.class);
		intent.putExtra(STRING_EXTRA_FILE, filename);
		intent.putExtra(STRING_EXTRA_TYPE, TYPE_FILENAME);
		context.startActivity(intent);
	}

	/**
	 * Static helper method to start the activity, passing the path of the picture.
	 *
	 * @param context
	 *            The context in which the activity is started.
	 * @param fileResource
	 *            The resource id of the picture.
	 */
	public static void startActivity(final Context context, final int fileResource) {
		Intent intent = new Intent(context, DisplayOneActivity.class);
		intent.putExtra(STRING_EXTRA_FILERESOURCE, fileResource);
		intent.putExtra(STRING_EXTRA_TYPE, TYPE_FILERESOURCE);
		context.startActivity(intent);
	}

	/*
	 * Build the screen on creation.
	 */
	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int type = getIntent().getIntExtra(STRING_EXTRA_TYPE, -1);
		String file = getIntent().getStringExtra(STRING_EXTRA_FILE);
		int fileResource = getIntent().getIntExtra(STRING_EXTRA_FILERESOURCE, -1);

		setContentView(R.layout.activity_display_one);

		fragmentImage = (DisplayImageFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);

		if (fragmentImage == null) {
			fragmentImage = new DisplayImageFragment();
			if (type == TYPE_FILENAME) {
				fragmentImage.setParameters(file, 1, null);
			}
			else {
				fragmentImage.setParameters(fileResource, 1);
			}

			getFragmentManager().beginTransaction().add(R.id.fragment_image, fragmentImage, FRAGMENT_TAG).commit();
			getFragmentManager().executePendingTransactions();
		}

		viewFragmentImage = findViewById(R.id.fragment_image);
		viewFragmentEdit = findViewById(R.id.fragment_edit);
		viewLayoutMain = findViewById(android.R.id.content);
		viewSeparatorAfterEdit = findViewById(R.id.separator_after_edit);

		// Restore in case of orientation change
		fragmentEdit = (EditCommentFragment) getFragmentManager().findFragmentByTag(FRAGMENT_EDIT_TAG);

		if (savedInstanceState != null) {
			int fragmentEditVisibility = savedInstanceState.getInt("fragmentEditVisibility");
			viewFragmentEdit.setVisibility(fragmentEditVisibility);

			if (fragmentEditVisibility == View.VISIBLE) {
				fragmentEditedImage = fragmentImage;
			}
		}

		// ensure that layout is refreshed if view gets resized
		AutoKeyboardLayoutUtility.assistActivity(this);

		DialogUtil.displayTip(this, R.string.message_tip_displaydetails, R.string.key_tip_displaydetails);
	}

	@Override
	protected final void showEditFragment(final String text) {
		super.showEditFragment(text);
		viewSeparatorAfterEdit.setVisibility(View.VISIBLE);
		viewFragmentImage.findViewById(R.id.buttonComment).setEnabled(false);
	}

	@Override
	protected final void hideEditFragment() {
		super.hideEditFragment();
		viewSeparatorAfterEdit.setVisibility(View.GONE);
		viewFragmentImage.findViewById(R.id.buttonComment).setEnabled(true);
	}

	/**
	 * Initialize the images.
	 */
	@Override
	protected final void initializeImages() {
		fragmentImage.initializeImages();
	}

	// implemenation of interface ActivityWithExplicitLayoutTrigger

	@Override
	public final void requestLayout() {
		viewLayoutMain.invalidate();
		fragmentImage.requestLayout();
	}
}
