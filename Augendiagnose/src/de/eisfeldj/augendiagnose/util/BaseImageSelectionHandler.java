package de.eisfeldj.augendiagnose.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.View;
import de.eisfeldj.augendiagnose.components.EyeImageView;

/**
 * Base handler for the selection of images. Contains only methods to handle the highlighting. Multiple views may be
 * selected, but currently only one image is supported.
 */
public abstract class BaseImageSelectionHandler {
	/**
	 * The views containing the selected image.
	 */
	private List<EyeImageView> selectedViews = new ArrayList<EyeImageView>();

	/**
	 * Get the activity holding the images.
	 *
	 * @return the activity
	 */
	protected abstract Activity getActivity();

	/**
	 * Change highlight setting of the selected views.
	 *
	 * @param highlight
	 *            indicator if the views should be highlighted
	 */
	protected final void highlightSelectedViews(final boolean highlight) {
		for (EyeImageView view : selectedViews) {
			if (highlight) {
				view.setBackgroundColor(getActivity().getResources().getColor(android.R.color.holo_orange_light));
			}
			else {
				view.setBackgroundColor(getActivity().getResources().getColor(android.R.color.transparent));
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
			selectedViews.clear();
		}
	}

	/**
	 * Select a specific view.
	 *
	 * @param view
	 *            the view to be selected.
	 */
	// OVERRIDABLE
	protected void selectView(final EyeImageView view) {
		selectedViews.add(view);
		highlightSelectedViews(true);
	}

	/**
	 * Get information if any view is selected.
	 *
	 * @return true if a view is selected.
	 */
	protected final boolean hasSelectedView() {
		return selectedViews.size() > 0;
	}

	/**
	 * Get information if a given view is selected.
	 *
	 * @param view
	 *            The given view.
	 * @return true if the given view is selected.
	 */
	protected final boolean isSelectedView(final View view) {
		return selectedViews.contains(view);
	}

	/**
	 * Get a selected eye photo, if existing.
	 *
	 * @return The selected eye photo.
	 */
	protected final EyePhoto getSelectedImage() {
		if (hasSelectedView()) {
			return selectedViews.get(0).getEyePhoto();
		}
		else {
			return null;
		}
	}

}
