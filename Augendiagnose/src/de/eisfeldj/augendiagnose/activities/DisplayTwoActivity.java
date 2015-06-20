package de.eisfeldj.augendiagnose.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.fragments.DisplayImageFragment;
import de.eisfeldj.augendiagnose.fragments.DisplayImageFragmentHalfscreen;
import de.eisfeldj.augendiagnose.fragments.EditCommentFragment;
import de.eisfeldj.augendiagnose.util.AutoKeyboardLayoutUtility;
import de.eisfeldj.augendiagnose.util.DialogUtil;
import de.eisfeldj.augendiagnose.util.EyePhoto.RightLeft;

/**
 * Activity to display two pictures on full screen (screen split in two halves).
 */
public class DisplayTwoActivity extends DisplayImageActivity {
	/**
	 * The resource key for the first file to be displayed.
	 */
	private static final String STRING_EXTRA_FILE1 = "de.eisfeldj.augendiagnose.FILE1";
	/**
	 * The resource key for the second file to be displayed.
	 */
	private static final String STRING_EXTRA_FILE2 = "de.eisfeldj.augendiagnose.FILE2";
	/**
	 * The resource key for the flag indicating if images should be pre-configured as right/left.
	 */
	private static final String BOOLEAN_EXTRA_PRESETRIGHTLEFT = "de.eisfeldj.augendiagnose.PRESETRIGHTLEFT";

	/**
	 * The fragment tag for the first image fragment.
	 */
	private static final String FRAGMENT_IMAGE1_TAG = "FRAGMENT_IMAGE1_TAG";
	/**
	 * The fragment tag for the second image fragment.
	 */
	private static final String FRAGMENT_IMAGE2_TAG = "FRAGMENT_IMAGE2_TAG";

	/**
	 * Flag indicating if the images are flagged right/left independent of the metadata.
	 */
	private boolean presetRightLeft;

	/**
	 * The views displaying the files.
	 */
	private View viewFragmentImage1, viewFragmentImage2;

	/**
	 * The fragments displaying the files.
	 */
	private DisplayImageFragment fragmentImage1, fragmentImage2;

	/**
	 * The view displaying the "other" file. Required to differentiate between "current listFoldersFragment" and
	 * "other listFoldersFragment" when editing picture comment.
	 */
	private View viewFragmentOther, viewFragmentThis;

	/**
	 * Static helper method to start the activity, passing the paths of the two pictures.
	 *
	 * @param context
	 *            The context in which the activity is started.
	 * @param filename1
	 *            The filename of the first picture.
	 * @param filename2
	 *            The filename of the second picture.
	 * @param presetRightLeft
	 *            Flag indicating if the images are flagged right/left independent of the metadata.
	 */
	public static void startActivity(final Context context, final String filename1, final String filename2,
			final boolean presetRightLeft) {
		Intent intent = new Intent(context, DisplayTwoActivity.class);
		intent.putExtra(STRING_EXTRA_FILE1, filename1);
		intent.putExtra(STRING_EXTRA_FILE2, filename2);
		intent.putExtra(BOOLEAN_EXTRA_PRESETRIGHTLEFT, presetRightLeft);
		context.startActivity(intent);
	}

	/*
	 * Build the screen on creation.
	 */
	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String file1 = getIntent().getStringExtra(STRING_EXTRA_FILE1);
		String file2 = getIntent().getStringExtra(STRING_EXTRA_FILE2);
		presetRightLeft = getIntent().getBooleanExtra(BOOLEAN_EXTRA_PRESETRIGHTLEFT, false);

		setContentView(R.layout.activity_display_two);

		fragmentImage1 = (DisplayImageFragment) getFragmentManager().findFragmentByTag(FRAGMENT_IMAGE1_TAG);
		if (fragmentImage1 == null) {
			fragmentImage1 = createFragment();
			fragmentImage1.setParameters(file1, 1, presetRightLeft ? RightLeft.RIGHT : null);

			getFragmentManager().beginTransaction().add(R.id.fragment_image1, fragmentImage1, FRAGMENT_IMAGE1_TAG)
					.commit();
		}

		fragmentImage2 = (DisplayImageFragment) getFragmentManager().findFragmentByTag(FRAGMENT_IMAGE2_TAG);
		if (fragmentImage2 == null) {
			fragmentImage2 = createFragment();
			fragmentImage2.setParameters(file2, 2, presetRightLeft ? RightLeft.LEFT : null);

			getFragmentManager().beginTransaction().add(R.id.fragment_image2, fragmentImage2, FRAGMENT_IMAGE2_TAG)
					.commit();
		}

		getFragmentManager().executePendingTransactions();

		viewFragmentImage1 = findViewById(R.id.fragment_image1);
		viewFragmentImage2 = findViewById(R.id.fragment_image2);
		viewFragmentEdit = findViewById(R.id.fragment_edit);
		viewLayoutMain = findViewById(android.R.id.content);
		viewSeparatorBeforeEdit = findViewById(R.id.separator_before_edit);
		viewSeparatorAfterEdit = findViewById(R.id.separator_after_edit);

		// Restore in case of orientation change
		fragmentEdit = (EditCommentFragment) getFragmentManager().findFragmentByTag(FRAGMENT_EDIT_TAG);

		if (savedInstanceState != null) {
			int fragmentEditVisibility = savedInstanceState.getInt("fragmentEditVisibility");
			int fragmentImage1Visibility = savedInstanceState.getInt("fragmentImage1Visibility");
			int fragmentImage2Visibility = savedInstanceState.getInt("fragmentImage2Visibility");

			viewFragmentEdit.setVisibility(fragmentEditVisibility);
			viewFragmentImage1.setVisibility(fragmentImage1Visibility);
			viewFragmentImage2.setVisibility(fragmentImage2Visibility);

			if (fragmentImage1Visibility == View.GONE) {
				viewFragmentOther = viewFragmentImage1;
				fragmentEditedImage = fragmentImage2;
			}
			else {
				viewFragmentOther = viewFragmentImage2;
				fragmentEditedImage = fragmentImage1;
			}
		}

		// ensure that layout is refreshed if view gets resized
		AutoKeyboardLayoutUtility.assistActivity(this);

		DialogUtil.displayTip(this, R.string.message_tip_displaydetails, R.string.key_tip_displaydetails);
	}

	/**
	 * Helper method to create the DisplayImageFragment.
	 *
	 * @return the fragment.
	 */
	private DisplayImageFragment createFragment() {
		return new DisplayImageFragmentHalfscreen();
	}

	@Override
	protected final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("fragmentImage1Visibility", viewFragmentImage1.getVisibility());
		outState.putInt("fragmentImage2Visibility", viewFragmentImage2.getVisibility());
	}

	@Override
	public final void startEditComment(final DisplayImageFragment fragment, final String text) {
		// Determine which image listFoldersFragment needs to be hidden
		if (fragment == fragmentImage1) {
			viewFragmentThis = viewFragmentImage1;
			viewFragmentOther = viewFragmentImage2;
		}
		else {
			viewFragmentThis = viewFragmentImage2;
			viewFragmentOther = viewFragmentImage1;
		}

		super.startEditComment(fragment, text);
	}

	@Override
	protected final void showEditFragment(final String text) {
		super.showEditFragment(text);
		viewFragmentOther.setVisibility(View.GONE);
		if (viewFragmentThis == viewFragmentImage2) {
			viewSeparatorBeforeEdit.setVisibility(View.GONE);
			viewSeparatorAfterEdit.setVisibility(View.VISIBLE);
		}
		viewFragmentThis.findViewById(R.id.buttonComment).setEnabled(false);
	}

	@Override
	protected final void hideEditFragment() {
		super.hideEditFragment();
		viewFragmentOther.setVisibility(View.VISIBLE);
		if (viewFragmentThis == viewFragmentImage2) {
			viewSeparatorBeforeEdit.setVisibility(View.VISIBLE);
			viewSeparatorAfterEdit.setVisibility(View.GONE);
		}
		viewFragmentThis.findViewById(R.id.buttonComment).setEnabled(true);
	}

	/**
	 * Initialize the images.
	 */
	@Override
	protected final void initializeImages() {
		fragmentImage1.initializeImages();
		fragmentImage2.initializeImages();
	}

	// implemenation of interface ActivityWithExplicitLayoutTrigger

	@Override
	public final void requestLayout() {
		viewLayoutMain.invalidate();
		fragmentImage1.requestLayout();
		fragmentImage2.requestLayout();
	}

}
