package de.eisfeldj.augendiagnose.util;

import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import de.eisfeldj.augendiagnose.activities.SelectTwoPicturesActivity;

/**
 * A class handling the selection two pictures, returning the pictures
 */
public class TwoImageSelectionHandler {
	private EyeImageView selectedView = null;
	private SelectTwoPicturesActivity activity = null;
	private static TwoImageSelectionHandler singleton;

	/**
	 * Get an instance of the handler - it is handled as singleton.
	 * 
	 * @return
	 */
	public static TwoImageSelectionHandler getInstance() {
		if (singleton == null) {
			singleton = new TwoImageSelectionHandler();
		}
		return singleton;
	}

	private TwoImageSelectionHandler() {
		// Ensure handling as singleton
	}

	/**
	 * Set the activity for first selection
	 * 
	 * @param activity
	 */
	public void setActivity(SelectTwoPicturesActivity activity) {
		this.activity = activity;
	}

	/**
	 * Clean all references
	 */
	public static void clean() {
		singleton = null;
	}

	/**
	 * Prepare a GridView for selection of the pictures
	 * 
	 * @param view
	 */
	public void highlightIfSelected(final EyeImageView view) {
		if ((selectedView != null) && selectedView.getEyePhoto().equals(view.getEyePhoto())) {
			selectView(view);
		}
	}

	/**
	 * Prepare a GridView for selection of the pictures
	 * 
	 * @param view
	 */
	public void prepareViewForSelection(final GridView view) {
		// TODO: Ensure that selected view stays selected after rotating device

		view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
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
	 * Highlight the selected view
	 * 
	 * @param type
	 */
	private void highlightSelectedView(boolean highlight) {
		if (highlight) {
			selectedView.setBackgroundColor(activity.getResources().getColor(android.R.color.holo_orange_light));
		}
		else {
			selectedView.setBackgroundColor(activity.getResources().getColor(android.R.color.transparent));
		}
	}

	/**
	 * Unselect the selected view
	 */
	public void cleanSelectedView() {
		if (selectedView != null) {
			highlightSelectedView(false);
			selectedView = null;
		}
	}

	/**
	 * Select a specific view
	 * 
	 * @param view
	 */
	private void selectView(View view) {
		selectedView = (EyeImageView) view;
		highlightSelectedView(true);
	}

	/**
	 * Return the paths of the two selected files to the parent activity
	 * 
	 * @param view1
	 * @param view2
	 */
	private void createResponse(EyeImageView view1, EyeImageView view2) {
		activity.returnResult(view1.getEyePhoto().getAbsolutePath(), view2.getEyePhoto().getAbsolutePath());
	}

}
