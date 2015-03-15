package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.components.ContextMenuReferenceHolder;
import de.eisfeldj.augendiagnose.fragments.DisplayImageFragment;
import de.eisfeldj.augendiagnose.fragments.EditCommentFragment;
import de.eisfeldj.augendiagnose.util.AutoKeyboardLayoutUtility.ActivityWithExplicitLayoutTrigger;

/**
 * Base class for displaying images.
 *
 * @author Joerg
 */
public abstract class DisplayImageActivity extends Activity implements ContextMenuReferenceHolder,
		ActivityWithExplicitLayoutTrigger {
	/**
	 * The fragment tag for the edit fragment.
	 */
	protected static final String FRAGMENT_EDIT_TAG = "FRAGMENT_EDIT_TAG";

	// PUBLIC_FIELDS:START
	// Fields required by subclasses.

	/**
	 * The fragment for editing the image comment.
	 */
	protected EditCommentFragment fragmentEdit;
	/**
	 * The fragment for displaying the image.
	 */
	protected DisplayImageFragment fragmentEditedImage;

	/**
	 * The view for the edit fragment.
	 */
	protected View viewFragmentEdit;
	/**
	 * The view for the main layout.
	 */
	protected View viewLayoutMain;

	/**
	 * The separator line after the edit field.
	 */
	protected View viewSeparatorAfterEdit;

	/**
	 * The separator line before the edit field.
	 */
	protected View viewSeparatorBeforeEdit = null;

	// PUBLIC_FIELDS:END

	/**
	 * A field for storing the fragment which was triggering the context menu. (For implementation of
	 * ContextMenuReferenceHoler).
	 */
	private Object contextMenuReference;

	/**
	 * Initialize the fragment(s) with the images.
	 */
	protected abstract void initializeImages();

	/*
	 * Workaround to ensure that all views have restored status before images are re-initialized.
	 */
	@Override
	public final void onWindowFocusChanged(final boolean hasFocus) {
		if (hasFocus) {
			initializeImages();
		}
	}

	// OVERRIDABLE
	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("fragmentEditVisibility", viewFragmentEdit.getVisibility());
	}

	/**
	 * Start the EditCommentFragment.
	 *
	 * @param fragment
	 *            the DisplayImageFragment starting the activity
	 * @param text
	 *            the old value of the text
	 */
	// OVERRIDABLE
	public void startEditComment(final DisplayImageFragment fragment, final String text) {
		fragmentEditedImage = fragment;
		showEditFragment(text);
		requestLayout();
	}

	/**
	 * Process the updated comment returned from the EditCommentFragment.
	 *
	 * @param text
	 *            The new comment text.
	 * @param success
	 *            flag indicating if the comment fragment was finished successfully.
	 */
	public final void processUpdatedComment(final String text, final boolean success) {
		if (success) {
			fragmentEditedImage.storeComment(text);
		}

		hideEditFragment();
		requestLayout();
	}

	/**
	 * Show the edit listFoldersFragment.
	 *
	 * @param text
	 *            The initial text to be displayed
	 */
	// OVERRIDABLE
	protected void showEditFragment(final String text) {
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
	 * Hide the edit listFoldersFragment.
	 */
	// OVERRIDABLE
	protected void hideEditFragment() {
		fragmentEdit.hideKeyboard();
		getFragmentManager().beginTransaction().remove(fragmentEdit).commit();
		getFragmentManager().executePendingTransactions();
		fragmentEdit = null;

		viewFragmentEdit.setVisibility(View.GONE);
	}

	/**
	 * If in editing mode, just remove the editing frame when pressing back.
	 */
	@Override
	public final void onBackPressed() {
		if (isEditingComment()) {
			hideEditFragment();
			requestLayout();
		}
		else {
			super.onBackPressed();
		}
	}

	// implementation of interface ContactMenuReferenceHolder

	/*
	 * Store a reference to the context menu holder.
	 */
	@Override
	public final void setContextMenuReference(final Object o) {
		contextMenuReference = o;
	}

	/*
	 * Retrieve a reference to the context menu holder.
	 */
	@Override
	public final Object getContextMenuReference() {
		return contextMenuReference;
	}

	/**
	 * Get information if the EditCommentFragment is active.
	 *
	 * @return true if the EditCommentFragment is active.
	 */
	public final boolean isEditingComment() {
		return fragmentEdit != null;
	}

}
