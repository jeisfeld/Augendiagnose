package de.eisfeldj.augendiagnose.util;

import android.app.Activity;
import android.view.View;
import de.eisfeldj.augendiagnose.components.EyeImageView;

/**
 * Base handler for the selection of images. Contains only methods to handle the highlighting.
 */
public abstract class BaseImageSelectionHandler {
	/**
	 * The view containing the first selected image.
	 */
	private EyeImageView selectedView = null;

	/**
	 * Get the activity holding the images.
	 *
	 * @return the activity
	 */
	protected abstract Activity getActivity();

	/**
	 * Change highlight setting of the selected view.
	 *
	 * @param highlight
	 *            indicator if the view should be highlighted
	 */
	protected final void highlightSelectedView(final boolean highlight) {
		if (highlight) {
			selectedView.setBackgroundColor(getActivity().getResources().getColor(android.R.color.holo_orange_light));
		}
		else {
			selectedView.setBackgroundColor(getActivity().getResources().getColor(android.R.color.transparent));
		}
	}

	/**
	 * Unselect the selected view.
	 */
	// OVERRIDABLE
	public void cleanSelectedView() {
		if (hasSelectedView()) {
			highlightSelectedView(false);
			selectedView = null;
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
		selectedView = view;
		highlightSelectedView(true);
	}

	/**
	 * Get information if any view is selected.
	 *
	 * @return true if a view is selected.
	 */
	protected final boolean hasSelectedView() {
		return selectedView != null;
	}

	/**
	 * Get information if a given view is selected.
	 *
	 * @param view
	 *            The given view.
	 * @return true if the given view is selected.
	 */
	protected final boolean isSelectedView(final View view) {
		return selectedView == view;
	}

	/**
	 * Get the selected eye photo, if existing.
	 *
	 * @return The selected eye photo.
	 */
	protected final EyePhoto getSelectedImage() {
		if (hasSelectedView()) {
			return selectedView.getEyePhoto();
		}
		else {
			return null;
		}
	}

}
