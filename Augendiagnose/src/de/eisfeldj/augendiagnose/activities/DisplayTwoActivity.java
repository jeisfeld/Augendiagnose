package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.components.ContextMenuReferenceHolder;
import de.eisfeldj.augendiagnose.fragments.DisplayImageFragment;
import de.eisfeldj.augendiagnose.fragments.DisplayImageFragmentHalfscreen;
import de.eisfeldj.augendiagnose.fragments.EditCommentFragment;
import de.eisfeldj.augendiagnose.fragments.EditCommentFragment.EditCommentStarterActivity;
import de.eisfeldj.augendiagnose.util.AndroidBug5497Workaround;
import de.eisfeldj.augendiagnose.util.AndroidBug5497Workaround.ActivityWithExplicitLayoutTrigger;

/**
 * Activity to display two pictures on full screen (screen split in two halves)
 */
public class DisplayTwoActivity extends Activity implements ContextMenuReferenceHolder, EditCommentStarterActivity,
		ActivityWithExplicitLayoutTrigger {
	private static final String STRING_EXTRA_FILE1 = "de.eisfeldj.augendiagnose.FILE1";
	private static final String STRING_EXTRA_FILE2 = "de.eisfeldj.augendiagnose.FILE2";

	private static final String FRAGMENT_IMAGE1_TAG = "FRAGMENT_IMAGE1_TAG";
	private static final String FRAGMENT_IMAGE2_TAG = "FRAGMENT_IMAGE2_TAG";
	private static final String FRAGMENT_EDIT_TAG = "FRAGMENT_EDIT_TAG";

	private View viewFragmentImage1, viewFragmentImage2, viewFragmentEdit, viewLayoutMain;

	private DisplayImageFragment fragmentImage1, fragmentImage2;
	private EditCommentFragment fragmentEdit;

	// Required to differentiate between "current fragment" and "other fragment" when editing picture comment
	private View viewFragmentOther;
	private DisplayImageFragment fragmentThis;

	private Object contextMenuReference;

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
				fragmentThis = fragmentImage2;
			}
			else {
				viewFragmentOther = viewFragmentImage2;
				fragmentThis = fragmentImage1;
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
	protected DisplayImageFragment createFragment() {
		return new DisplayImageFragmentHalfscreen();
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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("fragmentEditVisibility", viewFragmentEdit.getVisibility());
		outState.putInt("fragmentImage1Visibility", viewFragmentImage1.getVisibility());
		outState.putInt("fragmentImage2Visibility", viewFragmentImage2.getVisibility());
	}

	// implementation of interface ContactMenuReferenceHolder

	/**
	 * Store a reference to the context menu holder
	 * 
	 * @param o
	 */
	@Override
	public void setContextMenuReference(Object o) {
		contextMenuReference = o;
	}

	/**
	 * Retrieve a reference to the context menu holder
	 * 
	 * @return
	 */
	@Override
	public Object getContextMenuReference() {
		return contextMenuReference;
	}

	// implementation of interface EditCommentStarterActivity

	@Override
	public void startEditComment(DisplayImageFragment fragment, String text) {
		if (fragment == fragmentImage1) {
			viewFragmentOther = viewFragmentImage2;
			fragmentThis = fragmentImage1;
		}
		else {
			viewFragmentOther = viewFragmentImage1;
			fragmentThis = fragmentImage2;
		}

		fragmentEdit = new EditCommentFragment();
		fragmentEdit.setParameters(text);

		getFragmentManager().beginTransaction().add(R.id.fragment_edit, fragmentEdit, FRAGMENT_EDIT_TAG).commit();
		getFragmentManager().executePendingTransactions();

		viewFragmentOther.setVisibility(View.GONE);
		viewFragmentEdit.setVisibility(View.VISIBLE);
		requestLayout();
	}

	@Override
	public void processUpdatedComment(String text, boolean success) {
		if (success) {
			fragmentThis.storeComment(text);
		}

		getFragmentManager().beginTransaction().remove(fragmentEdit).commit();
		getFragmentManager().executePendingTransactions();

		viewFragmentOther.setVisibility(View.VISIBLE);
		viewFragmentEdit.setVisibility(View.GONE);
		requestLayout();
	}

	// implemenation of interface ActivityWithExplicitLayoutTrigger

	@Override
	public void requestLayout() {
		viewLayoutMain.invalidate();
		fragmentImage1.requestLayout();
		fragmentImage2.requestLayout();
	}

}
