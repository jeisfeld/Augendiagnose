package de.eisfeldj.augendiagnose.util;

import android.view.View;
import de.eisfeldj.augendiagnose.activities.DisplayOneActivityOverlay;
import de.eisfeldj.augendiagnose.activities.DisplayTwoActivity;
import de.eisfeldj.augendiagnose.activities.ListPicturesForNameActivity;
import de.eisfeldj.augendiagnose.activities.ListPicturesForSecondNameActivity;

/**
 * A class handling the selection of up to two pictures for display, and the display of these pictures
 */
public class ImageSelectionAndDisplayHandler {
	private EyeImageView selectedView = null;
	private ListPicturesForNameActivity activity = null;
	private ListPicturesForSecondNameActivity secondActivity = null;
	private static ImageSelectionAndDisplayHandler singleton;

	/**
	 * Get an instance of the handler - it is handled as singleton.
	 * 
	 * @return
	 */
	public static ImageSelectionAndDisplayHandler getInstance() {
		if (singleton == null) {
			singleton = new ImageSelectionAndDisplayHandler();
		}
		return singleton;
	}

	private ImageSelectionAndDisplayHandler() {
		// Ensure handling as singleton
	}

	/**
	 * Set the activity for first selection
	 * 
	 * @param activity
	 */
	public void setActivity(ListPicturesForNameActivity activity) {
		this.activity = activity;
	}

	/**
	 * Set the activiy for second selection
	 * 
	 * @param activity
	 */
	public void setSecondActivity(ListPicturesForSecondNameActivity activity) {
		this.secondActivity = activity;
	}

	/**
	 * Clean all references
	 */
	public static void clean() {
		singleton = null;
	}

	/**
	 * Prepare an EyeImageView for selection of the first picture
	 * 
	 * @param view
	 */
	public void prepareViewForFirstSelection(final EyeImageView view) {
		// Ensure that selected view stays selected after rotating device
		if ((selectedView != null) && selectedView.getEyePhoto().equals(view.getEyePhoto())) {
			selectView(view);
		}

		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (selectedView == null) {
					DisplayOneActivityOverlay.startActivity(activity, view.getEyePhoto().getAbsolutePath());
				}
				else if (selectedView == view) {
					cleanSelectedView();
					DisplayOneActivityOverlay.startActivity(activity, view.getEyePhoto().getAbsolutePath());
				}
				else {
					DisplayTwoActivity.startActivity(activity, getSelectedImagePath(), view.getEyePhoto()
							.getAbsolutePath());
					cleanSelectedView();
				}
			}
		});

		view.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (selectedView == view) {
					cleanSelectedView();
				}
				else {
					cleanSelectedView();
					selectView(view);
				}
				return true;
			}
		});
	}

	/**
	 * Prepare an EyeImageView for selection of the second picture
	 * 
	 * @param view
	 */
	public void prepareViewForSecondSelection(final EyeImageView view) {
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DisplayTwoActivity
						.startActivity(activity, getSelectedImagePath(), view.getEyePhoto().getAbsolutePath());
				cleanSelectedView();
				secondActivity.finish();
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
			activity.deactivateButtonAdditionalPictures();
		}
	}

	/**
	 * Select a specific view
	 * 
	 * @param view
	 */
	private void selectView(EyeImageView view) {
		selectedView = view;
		highlightSelectedView(true);
		activity.activateButtonAdditionalPictures();
	}

	/**
	 * Retrieve the path of the selected image
	 * 
	 * @return
	 */
	public String getSelectedImagePath() {
		if (selectedView == null) {
			return null;
		}
		else {
			return selectedView.getEyePhoto().getAbsolutePath();
		}
	}
}
