package de.jeisfeld.augendiagnoselib.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.components.ContextMenuReferenceHolder;
import de.jeisfeld.augendiagnoselib.fragments.DisplayImageFragment;
import de.jeisfeld.augendiagnoselib.fragments.EditCommentFragment;
import de.jeisfeld.augendiagnoselib.util.AutoKeyboardLayoutUtility.ActivityWithExplicitLayoutTrigger;
import de.jeisfeld.augendiagnoselib.util.TrackingUtil;

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

	/**
	 * Flag checking if the image is initialized.
	 */
	private boolean mIsInitialized = false;

	// PUBLIC_FIELDS:START
	// Fields required by subclasses.

	/**
	 * The fragment for editing the image comment.
	 */
	@Nullable
	protected EditCommentFragment mFragmentEdit;
	/**
	 * The fragment for displaying the image.
	 */
	protected DisplayImageFragment mFragmentEditedImage;

	/**
	 * The view for the edit fragment.
	 */
	protected View mViewFragmentEdit;
	/**
	 * The view for the main layout.
	 */
	protected View mViewLayoutMain;

	/**
	 * The separator line after the edit field.
	 */
	protected View mViewSeparatorAfterEdit;

	/**
	 * The separator line before the edit field.
	 */
	@Nullable
	protected View mViewSeparatorBeforeEdit = null;

	// PUBLIC_FIELDS:END

	/**
	 * A field for storing the fragment which was triggering the context menu. (For implementation of
	 * ContextMenuReferenceHolder).
	 */
	private Object mContextMenuReference;

	/**
	 * Initialize the fragment(s) with the images.
	 */
	protected abstract void initializeImages();

	@Override
	protected final void onResume() {
		super.onResume();
		TrackingUtil.sendScreen(this);
	}

	/*
	 * Workaround to ensure that all views have restored status before images are re-initialized.
	 */
	@Override
	public final void onWindowFocusChanged(final boolean hasFocus) {
		if (hasFocus && !mIsInitialized) {
			initializeImages();
			mIsInitialized = true;
		}
	}

	// OVERRIDABLE
	@Override
	protected void onSaveInstanceState(@NonNull final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("fragmentEditVisibility", mViewFragmentEdit.getVisibility());
	}

	/**
	 * Start the EditCommentFragment.
	 *
	 * @param fragment the DisplayImageFragment starting the activity
	 * @param text     the old value of the text
	 */
	// OVERRIDABLE
	public void startEditComment(final DisplayImageFragment fragment, final String text) {
		mFragmentEditedImage = fragment;
		showEditFragment(text);
		requestLayout();
	}

	/**
	 * Process the updated comment returned from the EditCommentFragment.
	 *
	 * @param text    The new comment text.
	 * @param success flag indicating if the comment fragment was finished successfully.
	 */
	public final void processUpdatedComment(final String text, final boolean success) {
		if (success) {
			mFragmentEditedImage.storeComment(text);
		}

		hideEditFragment();
		requestLayout();
	}

	/**
	 * Show the edit listFoldersFragment.
	 *
	 * @param text The initial text to be displayed
	 */
	// OVERRIDABLE
	protected void showEditFragment(final String text) {
		// Do not create duplicate edit fragments
		if (mFragmentEdit == null) {
			mFragmentEdit = new EditCommentFragment();
			mFragmentEdit.setParameters(text);

			getFragmentManager().beginTransaction().add(R.id.fragment_edit, mFragmentEdit, FRAGMENT_EDIT_TAG).commit();
			getFragmentManager().executePendingTransactions();
		}

		mViewFragmentEdit.setVisibility(View.VISIBLE);
	}

	/**
	 * Hide the edit listFoldersFragment.
	 */
	// OVERRIDABLE
	protected void hideEditFragment() {
		mFragmentEdit.hideKeyboard();
		getFragmentManager().beginTransaction().remove(mFragmentEdit).commit();
		getFragmentManager().executePendingTransactions();
		mFragmentEdit = null;

		mViewFragmentEdit.setVisibility(View.GONE);
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
		mContextMenuReference = o;
	}

	/*
	 * Retrieve a reference to the context menu holder.
	 */
	@Override
	public final Object getContextMenuReference() {
		return mContextMenuReference;
	}

	/**
	 * Get information if the EditCommentFragment is active.
	 *
	 * @return true if the EditCommentFragment is active.
	 */
	private boolean isEditingComment() {
		return mFragmentEdit != null;
	}

}
