package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.components.ContextMenuReferenceHolder;
import de.eisfeldj.augendiagnose.fragments.DisplayImageFragment;
import de.eisfeldj.augendiagnose.fragments.EditCommentFragment;
import de.eisfeldj.augendiagnose.util.AndroidBug5497Workaround.ActivityWithExplicitLayoutTrigger;

/**
 * Variant of DisplayOneFragment that includes overlay handling
 * 
 * @author Joerg
 */
public abstract class DisplayImageActivity extends Activity implements ContextMenuReferenceHolder,
		ActivityWithExplicitLayoutTrigger {
	protected static final String FRAGMENT_EDIT_TAG = "FRAGMENT_EDIT_TAG";

	protected EditCommentFragment fragmentEdit;
	protected DisplayImageFragment fragmentEditedImage;
	protected View viewFragmentEdit, viewLayoutMain;

	private Object contextMenuReference;

	protected abstract void initializeImages();

	/**
	 * Workaround to ensure that all views have restored status before images are re-initialized
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus) {
			initializeImages();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("fragmentEditVisibility", viewFragmentEdit.getVisibility());
	}

	/**
	 * Start the EditCommentFragment
	 * 
	 * @param fragment
	 *            the fragment starting the activity
	 * @param text
	 *            the old value of the text
	 */
	public void startEditComment(DisplayImageFragment fragment, String text) {
		fragmentEditedImage = fragment;
		showEditFragment(text);
		requestLayout();
	}

	/**
	 * Process the updated comment returned from the EditCommentFragment
	 */
	public void processUpdatedComment(String text, boolean success) {
		if (success) {
			fragmentEditedImage.storeComment(text);
		}

		hideEditFragment();
		requestLayout();
	}

	/**
	 * Show the edit fragment
	 * 
	 * @param text
	 *            The initial text to be displayed
	 */
	protected void showEditFragment(String text) {
		// Do not create duplicate edit fragments
		if (fragmentEdit == null) {
			fragmentEdit = new EditCommentFragment();
			fragmentEdit.setParameters(text);

			getFragmentManager().beginTransaction().add(R.id.fragment_edit, fragmentEdit, FRAGMENT_EDIT_TAG).commit();
			getFragmentManager().executePendingTransactions();
		}

		viewFragmentEdit.setVisibility(View.VISIBLE);
	}

	/**
	 * Hide the edit fragment
	 */
	protected void hideEditFragment() {
		fragmentEdit.hideKeyboard();
		getFragmentManager().beginTransaction().remove(fragmentEdit).commit();
		getFragmentManager().executePendingTransactions();
		fragmentEdit = null;

		viewFragmentEdit.setVisibility(View.GONE);
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

}
