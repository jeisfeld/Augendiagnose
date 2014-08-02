package de.eisfeldj.augendiagnose.util;

import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import de.eisfeldj.augendiagnose.activities.SelectTwoPicturesActivity;
import de.eisfeldj.augendiagnose.components.EyeImageView;

/**
 * A class handling the selection two pictures, returning the pictures.
 */
public final class TwoImageSelectionHandler {
	/**
	 * The view containing the first selected image.
	 */
	private EyeImageView selectedView = null;
	/**
	 * A reference to the activity.
	 */
	private SelectTwoPicturesActivity activity = null;
	/**
	 * A holder of the TwoImageSelectionHandler as singleton.
	 */
	private static volatile TwoImageSelectionHandler singleton;

	/**
	 * Get an instance of the handler - it is handled as singleton.
	 *
	 * @return an instance of the handler.
	 */
	public static TwoImageSelectionHandler getInstance() {
		if (singleton == null) {
			singleton = new TwoImageSelectionHandler();
		}
		return singleton;
	}

	/**
	 * Make constructor private to ensure singleton use.
	 */
	private TwoImageSelectionHandler() {
		// Ensure handling as singleton
	}

	/**
	 * Set the activity for first selection.
	 *
	 * @param activity
	 *            The activity.
	 */
	public void setActivity(final SelectTwoPicturesActivity activity) {
		this.activity = activity;
	}

	/**
	 * Clean all references.
	 */
	public static void clean() {
		singleton = null;
	}

	/**
	 * Highlight an EyeImageView if it is selected.
	 *
	 * @param view
	 *            the EyeImageView.
	 */
	public void highlightIfSelected(final EyeImageView view) {
		if ((selectedView != null) && selectedView.getEyePhoto().equals(view.getEyePhoto())) {
			selectView(view);
		}
	}

	/**
	 * Prepare a GridView for selection of the pictures.
	 *
	 * @param view
	 *            The GridView to be prepared.
	 */
	public void prepareViewForSelection(final GridView view) {
		view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> parent, final View v, final int position, final long id) {
				if (selectedView == null) {
					selectView(v);
				}
				else if (selectedView == v) {
					cleanSelectedView();
				}
				else {
					createResponse(selectedView, (EyeImageView) v);
					cleanSelectedView();
				}
			}
		});

	}

	/**
	 * Change highlight setting of the selected view.
	 *
	 * @param highlight indicator if the view should be highlighted
	 */
	private void highlightSelectedView(final boolean highlight) {
		if (highlight) {
			selectedView.setBackgroundColor(activity.getResources().getColor(android.R.color.holo_orange_light));
		}
		else {
			selectedView.setBackgroundColor(activity.getResources().getColor(android.R.color.transparent));
		}
	}

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
	 * @param view the view to be selected.
	 */
	private void selectView(final View view) {
		selectedView = (EyeImageView) view;
		highlightSelectedView(true);
	}

	/**
	 * Return the paths of the two selected files to the parent activity.
	 *
	 * @param view1 the first selected path.
	 * @param view2 the second selected path.
	 */
	private void createResponse(final EyeImageView view1, final EyeImageView view2) {
		activity.returnResult(view1.getEyePhoto().getAbsolutePath(), view2.getEyePhoto().getAbsolutePath());
	}

}
