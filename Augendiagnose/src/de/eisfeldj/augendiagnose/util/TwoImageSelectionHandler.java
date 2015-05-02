package de.eisfeldj.augendiagnose.util;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
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
		// TODO: fix issue on selecting first image - somehow this method is called then again with different view,
		// which leads to wrong highlighting on deselecting.
		if ((selectedView != null) && selectedView.getEyePhoto().equals(view.getEyePhoto())) {
			selectView(view);
		}
	}

	/**
	 * Prepare a GridView for selection of the pictures.
	 *
	 * @param view
	 *            The GridView to be prepared.
	 * @param hasContextMenu
	 *            Flag indicating if a context menu should be enabled.
	 */
	public void prepareViewForSelection(final EyeImageView view, final boolean hasContextMenu) {
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (selectedView == null) {
					selectView(view);
				}
				else if (selectedView.getEyePhoto().equals(view.getEyePhoto())) {
					cleanSelectedView();
				}
				else {
					createResponse(selectedView, view);
					cleanSelectedView();
				}
			}
		});

		if (hasContextMenu) {
			view.setOnCreateContextMenuListener(getActivity());
		}
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
