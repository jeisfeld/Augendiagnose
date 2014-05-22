package de.eisfeldj.augendiagnose.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.fragments.DisplayImageFragment;
import de.eisfeldj.augendiagnose.fragments.DisplayImageFragmentHalfscreen;
import de.eisfeldj.augendiagnose.fragments.EditCommentFragment;
import de.eisfeldj.augendiagnose.util.AndroidBug5497Workaround;

/**
 * Activity to display two pictures on full screen (screen split in two halves)
 */
public class DisplayTwoActivity extends DisplayImageActivity {
	private static final String STRING_EXTRA_FILE1 = "de.eisfeldj.augendiagnose.FILE1";
	private static final String STRING_EXTRA_FILE2 = "de.eisfeldj.augendiagnose.FILE2";

	private static final String FRAGMENT_IMAGE1_TAG = "FRAGMENT_IMAGE1_TAG";
	private static final String FRAGMENT_IMAGE2_TAG = "FRAGMENT_IMAGE2_TAG";

	private View viewFragmentImage1, viewFragmentImage2;

	private DisplayImageFragment fragmentImage1, fragmentImage2;

	// Required to differentiate between "current fragment" and "other fragment" when editing picture comment
	private View viewFragmentOther;

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

		setContentView(R.layout.activity_display_two);

		fragmentImage1 = (DisplayImageFragment) getFragmentManager().findFragmentByTag(FRAGMENT_IMAGE1_TAG);
		if (fragmentImage1 == null) {
			fragmentImage1 = createFragment();
			fragmentImage1.setParameters(file1, 1);

			getFragmentManager().beginTransaction().add(R.id.fragment_image1, fragmentImage1, FRAGMENT_IMAGE1_TAG)
					.commit();
		}

		fragmentImage2 = (DisplayImageFragment) getFragmentManager().findFragmentByTag(FRAGMENT_IMAGE2_TAG);
		if (fragmentImage2 == null) {
			fragmentImage2 = createFragment();
			fragmentImage2.setParameters(file2, 2);

			getFragmentManager().beginTransaction().add(R.id.fragment_image2, fragmentImage2, FRAGMENT_IMAGE2_TAG)
					.commit();
		}

		getFragmentManager().executePendingTransactions();

		viewFragmentImage1 = findViewById(R.id.fragment_image1);
		viewFragmentImage2 = findViewById(R.id.fragment_image2);
		viewFragmentEdit = findViewById(R.id.fragment_edit);
		viewLayoutMain = findViewById(android.R.id.content);

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
		AndroidBug5497Workaround.assistActivity(this);
	}

	/**
	 * Helper method to create the fragment
	 * 
	 * @return
	 */
	private DisplayImageFragment createFragment() {
		return new DisplayImageFragmentHalfscreen();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("fragmentImage1Visibility", viewFragmentImage1.getVisibility());
		outState.putInt("fragmentImage2Visibility", viewFragmentImage2.getVisibility());
	}

	@Override
	public void startEditComment(DisplayImageFragment fragment, String text) {
		// Determine which image fragment needs to be hidden
		if (fragment == fragmentImage1) {
			viewFragmentOther = viewFragmentImage2;
		}
		else {
			viewFragmentOther = viewFragmentImage1;
		}

		super.startEditComment(fragment, text);
	}

	@Override
	protected void showEditFragment(String text) {
		super.showEditFragment(text);
		viewFragmentOther.setVisibility(View.GONE);
	}

	@Override
	protected void hideEditFragment() {
		super.hideEditFragment();
		viewFragmentOther.setVisibility(View.VISIBLE);
	}

	/**
	 * Initialize the images
	 */
	@Override
	protected void initializeImages() {
		fragmentImage1.initializeImages();
		fragmentImage2.initializeImages();
	}

	// implemenation of interface ActivityWithExplicitLayoutTrigger

	@Override
	public void requestLayout() {
		viewLayoutMain.invalidate();
		fragmentImage1.requestLayout();
		fragmentImage2.requestLayout();
	}

}
