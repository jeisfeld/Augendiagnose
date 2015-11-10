package de.jeisfeld.augendiagnoselib.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.View;

import de.jeisfeld.augendiagnoselib.components.EyeImageView;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto;

/**
 * Base handler for the selection of images. Contains only methods to handle the highlighting. Multiple views may be
 * selected, but currently only one image is supported.
 */
public abstract class BaseImageSelectionHandler {
	/**
	 * The views containing the selected image.
	 */
	private final List<EyeImageView> mSelectedViews = new ArrayList<>();

	/**
	 * Get the activity holding the images.
	 *
	 * @return the activity
	 */
	protected abstract Activity getActivity();

	/**
	 * Change highlight setting of the selected views.
	 *
	 * @param highlight indicator if the views should be highlighted
	 */
	private void highlightSelectedViews(final boolean highlight) {
		for (EyeImageView view : mSelectedViews) {
			if (highlight) {
				view.setBackgroundColor(getActivity().getResources().getColor(android.R.color.holo_orange_light, null));
			}
			else {
				view.setBackgroundColor(getActivity().getResources().getColor(android.R.color.transparent, null));
			}
		}
	}

	/**
	 * Unselect the selected view.
	 */
	// OVERRIDABLE
	public void cleanSelectedViews() {
		if (hasSelectedView()) {
			highlightSelectedViews(false);
			mSelectedViews.clear();
		}
	}

	/**
	 * Select a specific view.
	 *
	 * @param view the view to be selected.
	 */
	// OVERRIDABLE
	protected void selectView(final EyeImageView view) {
		mSelectedViews.add(view);
		highlightSelectedViews(true);
	}

	/**
	 * Get information if any view is selected.
	 *
	 * @return true if a view is selected.
	 */
	protected final boolean hasSelectedView() {
		return mSelectedViews.size() > 0;
	}

	/**
	 * Get information if a given view is selected.
	 *
	 * @param view The given view.
	 * @return true if the given view is selected.
	 */
	protected final boolean isSelectedView(final View view) {
		return view instanceof EyeImageView && mSelectedViews.contains(view);
	}

	/**
	 * Get a selected eye photo, if existing.
	 *
	 * @return The selected eye photo.
	 */
	protected final EyePhoto getSelectedImage() {
		if (hasSelectedView()) {
			return mSelectedViews.get(0).getEyePhoto();
		}
		else {
			return null;
		}
	}

}
