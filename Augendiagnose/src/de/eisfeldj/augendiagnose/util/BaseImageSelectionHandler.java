package de.eisfeldj.augendiagnose.util;

import android.app.Activity;
import de.eisfeldj.augendiagnose.components.EyeImageView;

/**
 * Base handler for the selection of images. Contains only methods to handle the highlighting.
 */
public abstract class BaseImageSelectionHandler {
	/**
	 * The view containing the first selected image.
	 */
	// PUBLIC_FIELDS:START
	protected EyeImageView selectedView = null;

	// PUBLIC_FIELDS:END

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

	// OVERRIDABLE:START

	/**
	 * Unselect the selected view.
	 */
	public void cleanSelectedView() {
		if (selectedView != null) {
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
	protected void selectView(final EyeImageView view) {
		selectedView = view;
		highlightSelectedView(true);
	}

	// OVERRIDABLE:END

}
