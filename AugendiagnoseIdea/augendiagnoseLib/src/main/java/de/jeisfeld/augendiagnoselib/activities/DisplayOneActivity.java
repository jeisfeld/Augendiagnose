package de.jeisfeld.augendiagnoselib.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.fragments.DisplayImageFragment;
import de.jeisfeld.augendiagnoselib.fragments.EditCommentFragment;
import de.jeisfeld.augendiagnoselib.util.AutoKeyboardLayoutUtility;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;

/**
 * Variant of DisplayOneFragment that includes overlay handling.
 *
 * @author Joerg
 */
public class DisplayOneActivity extends DisplayImageActivity {
	/**
	 * The resource key for the file path.
	 */
	private static final String STRING_EXTRA_FILE = "de.jeisfeld.augendiagnoselib.FILE";

	/**
	 * The fragment tag.
	 */
	private static final String FRAGMENT_TAG = "FRAGMENT_TAG";

	/**
	 * The view displaying the files.
	 */
	private View mViewFragmentImage;

	/**
	 * The fragment displaying the image.
	 */
	private DisplayImageFragment mFragmentImage;

	/**
	 * Static helper method to start the activity, passing the path of the picture.
	 *
	 * @param context  The context in which the activity is started.
	 * @param filename The filename of the picture.
	 */
	public static void startActivity(@NonNull final Context context, final String filename) {
		Intent intent = new Intent(context, DisplayOneActivity.class);
		intent.putExtra(STRING_EXTRA_FILE, filename);
		context.startActivity(intent);
	}

	/*
	 * Build the screen on creation.
	 */
	@Override
	protected final void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String file = getIntent().getStringExtra(STRING_EXTRA_FILE);

		setContentView(R.layout.activity_display_one);

		mFragmentImage = (DisplayImageFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);

		if (mFragmentImage == null) {
			mFragmentImage = new DisplayImageFragment();
			mFragmentImage.setParameters(file, 1, null);

			getSupportFragmentManager().beginTransaction().add(R.id.fragment_image, mFragmentImage, FRAGMENT_TAG).commit();
			getSupportFragmentManager().executePendingTransactions();
		}

		mViewFragmentImage = findViewById(R.id.fragment_image);
		mViewFragmentEdit = findViewById(R.id.fragment_edit);
		mViewLayoutMain = findViewById(android.R.id.content);
		mViewSeparatorAfterEdit = findViewById(R.id.separator_after_edit);

		// Restore in case of orientation change
		mFragmentEdit = (EditCommentFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_EDIT_TAG);

		if (savedInstanceState != null) {
			int fragmentEditVisibility = savedInstanceState.getInt("fragmentEditVisibility");
			//noinspection ResourceType
			mViewFragmentEdit.setVisibility(fragmentEditVisibility);

			if (fragmentEditVisibility == View.VISIBLE) {
				mFragmentEditedImage = mFragmentImage;
			}
		}

		// ensure that layout is refreshed if view gets resized
		AutoKeyboardLayoutUtility.assistActivity(this);

		if (savedInstanceState == null) {
			PreferenceUtil.incrementCounter(R.string.key_statistics_countdisplay);
		}

		DialogUtil.displayTip(this, R.string.message_tip_displaydetails, R.string.key_tip_displaydetails);
	}

	@Override
	protected final void showEditFragment(final String text) {
		super.showEditFragment(text);
		mViewSeparatorAfterEdit.setVisibility(View.VISIBLE);
		mViewFragmentImage.findViewById(R.id.buttonComment).setEnabled(false);
	}

	@Override
	protected final void hideEditFragment() {
		super.hideEditFragment();
		mViewSeparatorAfterEdit.setVisibility(View.GONE);
		mViewFragmentImage.findViewById(R.id.buttonComment).setEnabled(true);
	}

	/**
	 * Initialize the images.
	 */
	@Override
	protected final void initializeImages() {
		mFragmentImage.initializeImages();
	}

	// implementation of interface ActivityWithExplicitLayoutTrigger

	@Override
	public final void requestLayout() {
		mViewLayoutMain.invalidate();
		mFragmentImage.requestLayout();
	}
}
