package de.jeisfeld.augendiagnoselib.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.fragments.DisplayImageFragment;
import de.jeisfeld.augendiagnoselib.fragments.DisplayImageFragmentHalfscreen;
import de.jeisfeld.augendiagnoselib.fragments.EditCommentFragment;
import de.jeisfeld.augendiagnoselib.util.AutoKeyboardLayoutUtility;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto.RightLeft;

/**
 * Activity to display two pictures on full screen (screen split in two halves).
 */
public class DisplayTwoActivity extends DisplayImageActivity {
	/**
	 * The resource key for the first file to be displayed.
	 */
	private static final String STRING_EXTRA_FILE1 = "de.jeisfeld.augendiagnoselib.FILE1";
	/**
	 * The resource key for the second file to be displayed.
	 */
	private static final String STRING_EXTRA_FILE2 = "de.jeisfeld.augendiagnoselib.FILE2";
	/**
	 * The resource key for the flag indicating if images should be pre-configured as right/left.
	 */
	private static final String BOOLEAN_EXTRA_PRESETRIGHTLEFT = "de.jeisfeld.augendiagnoselib.PRESETRIGHTLEFT";

	/**
	 * The fragment tag for the first image fragment.
	 */
	private static final String FRAGMENT_IMAGE1_TAG = "FRAGMENT_IMAGE1_TAG";
	/**
	 * The fragment tag for the second image fragment.
	 */
	private static final String FRAGMENT_IMAGE2_TAG = "FRAGMENT_IMAGE2_TAG";

	/**
	 * The views displaying the files.
	 */
	private View mViewFragmentImage1, mViewFragmentImage2;

	/**
	 * The fragments displaying the files.
	 */
	private DisplayImageFragment mFragmentImage1, mFragmentImage2;

	/**
	 * The view displaying the "other" file. Required to differentiate between "current listFoldersFragment" and
	 * "other listFoldersFragment" when editing picture comment.
	 */
	private View mViewFragmentOther, mViewFragmentThis;

	/**
	 * Static helper method to start the activity, passing the paths of the two pictures.
	 *
	 * @param context         The context in which the activity is started.
	 * @param filename1       The filename of the first picture.
	 * @param filename2       The filename of the second picture.
	 * @param presetRightLeft Flag indicating if the images are flagged right/left independent of the metadata.
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
		boolean presetRightLeft = getIntent().getBooleanExtra(BOOLEAN_EXTRA_PRESETRIGHTLEFT, false);

		setContentView(R.layout.activity_display_two);

		mFragmentImage1 = (DisplayImageFragment) getFragmentManager().findFragmentByTag(FRAGMENT_IMAGE1_TAG);
		if (mFragmentImage1 == null) {
			mFragmentImage1 = createFragment();
			mFragmentImage1.setParameters(file1, 1, presetRightLeft ? RightLeft.RIGHT : null);

			getFragmentManager().beginTransaction().add(R.id.fragment_image1, mFragmentImage1, FRAGMENT_IMAGE1_TAG)
					.commit();
		}

		mFragmentImage2 = (DisplayImageFragment) getFragmentManager().findFragmentByTag(FRAGMENT_IMAGE2_TAG);
		if (mFragmentImage2 == null) {
			mFragmentImage2 = createFragment();
			mFragmentImage2.setParameters(file2, 2, presetRightLeft ? RightLeft.LEFT : null);

			getFragmentManager().beginTransaction().add(R.id.fragment_image2, mFragmentImage2, FRAGMENT_IMAGE2_TAG)
					.commit();
		}

		getFragmentManager().executePendingTransactions();

		mViewFragmentImage1 = findViewById(R.id.fragment_image1);
		mViewFragmentImage2 = findViewById(R.id.fragment_image2);
		mViewFragmentEdit = findViewById(R.id.fragment_edit);
		mViewLayoutMain = findViewById(android.R.id.content);
		mViewSeparatorBeforeEdit = findViewById(R.id.separator_before_edit);
		mViewSeparatorAfterEdit = findViewById(R.id.separator_after_edit);

		// Restore in case of orientation change
		mFragmentEdit = (EditCommentFragment) getFragmentManager().findFragmentByTag(FRAGMENT_EDIT_TAG);

		if (savedInstanceState != null) {
			int fragmentEditVisibility = savedInstanceState.getInt("fragmentEditVisibility");
			int fragmentImage1Visibility = savedInstanceState.getInt("fragmentImage1Visibility");
			int fragmentImage2Visibility = savedInstanceState.getInt("fragmentImage2Visibility");

			//noinspection ResourceType
			mViewFragmentEdit.setVisibility(fragmentEditVisibility);
			//noinspection ResourceType
			mViewFragmentImage1.setVisibility(fragmentImage1Visibility);
			//noinspection ResourceType
			mViewFragmentImage2.setVisibility(fragmentImage2Visibility);

			if (fragmentImage1Visibility == View.GONE) {
				mViewFragmentOther = mViewFragmentImage1;
				mFragmentEditedImage = mFragmentImage2;
			}
			else {
				mViewFragmentOther = mViewFragmentImage2;
				mFragmentEditedImage = mFragmentImage1;
			}
		}

		// ensure that layout is refreshed if view gets resized
		AutoKeyboardLayoutUtility.assistActivity(this);

		if (savedInstanceState == null) {
			PreferenceUtil.incrementCounter(R.string.key_statistics_countdisplay);
		}

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
		outState.putInt("fragmentImage1Visibility", mViewFragmentImage1.getVisibility());
		outState.putInt("fragmentImage2Visibility", mViewFragmentImage2.getVisibility());
	}

	@Override
	public final void startEditComment(final DisplayImageFragment fragment, final String text) {
		// Determine which image listFoldersFragment needs to be hidden
		if (fragment == mFragmentImage1) {
			mViewFragmentThis = mViewFragmentImage1;
			mViewFragmentOther = mViewFragmentImage2;
		}
		else {
			mViewFragmentThis = mViewFragmentImage2;
			mViewFragmentOther = mViewFragmentImage1;
		}

		super.startEditComment(fragment, text);
	}

	@Override
	protected final void showEditFragment(final String text) {
		super.showEditFragment(text);
		mViewFragmentOther.setVisibility(View.GONE);
		if (mViewFragmentThis == mViewFragmentImage2) {
			mViewSeparatorBeforeEdit.setVisibility(View.GONE);
			mViewSeparatorAfterEdit.setVisibility(View.VISIBLE);
		}
		mViewFragmentThis.findViewById(R.id.buttonComment).setEnabled(false);
	}

	@Override
	protected final void hideEditFragment() {
		super.hideEditFragment();
		mViewFragmentOther.setVisibility(View.VISIBLE);
		if (mViewFragmentThis == mViewFragmentImage2) {
			mViewSeparatorBeforeEdit.setVisibility(View.VISIBLE);
			mViewSeparatorAfterEdit.setVisibility(View.GONE);
		}
		mViewFragmentThis.findViewById(R.id.buttonComment).setEnabled(true);
	}

	/**
	 * Initialize the images.
	 */
	@Override
	protected final void initializeImages() {
		mFragmentImage1.initializeImages();
		mFragmentImage2.initializeImages();
	}

	// implementation of interface ActivityWithExplicitLayoutTrigger

	@Override
	public final void requestLayout() {
		mViewLayoutMain.invalidate();
		mFragmentImage1.requestLayout();
		mFragmentImage2.requestLayout();
	}

}
