package de.eisfeldj.augendiagnose.util;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import de.eisfeldj.augendiagnose.activities.SelectTwoPicturesActivity;
import de.eisfeldj.augendiagnose.components.EyeImageView;

/**
 * A class handling the selection two pictures, returning the pictures.
 */
public final class TwoImageSelectionHandler extends BaseImageSelectionHandler {
	/**
	 * The activity for selection.
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
	 * Hide default constructor, to ensure singleton use.
	 */
	private TwoImageSelectionHandler() {
		// default constructor
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
					selectView((EyeImageView) v);
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
	 * Return the paths of the two selected files to the parent activity.
	 *
	 * @param view1
	 *            the first selected path.
	 * @param view2
	 *            the second selected path.
	 */
	private void createResponse(final EyeImageView view1, final EyeImageView view2) {
		activity.returnResult(view1.getEyePhoto().getAbsolutePath(), view2.getEyePhoto()
				.getAbsolutePath());
	}

	@Override
	protected Activity getActivity() {
		return activity;
	}
}
