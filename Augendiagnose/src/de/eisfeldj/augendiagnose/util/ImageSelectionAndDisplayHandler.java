package de.eisfeldj.augendiagnose.util;

import android.app.Activity;
import android.view.View;
import de.eisfeldj.augendiagnose.activities.DisplayOneActivity;
import de.eisfeldj.augendiagnose.activities.DisplayTwoActivity;
import de.eisfeldj.augendiagnose.activities.ListFoldersForDisplayActivity;
import de.eisfeldj.augendiagnose.activities.ListPicturesForNameActivity;
import de.eisfeldj.augendiagnose.activities.ListPicturesForSecondNameActivity;
import de.eisfeldj.augendiagnose.components.EyeImageView;
import de.eisfeldj.augendiagnose.fragments.ListPicturesForNameFragment;

/**
 * A class handling the selection of up to two pictures for display, and the display of these pictures
 */
public class ImageSelectionAndDisplayHandler {
	private EyeImageView selectedView = null;
	private Activity activity = null;
	private ListPicturesForNameFragment fragment = null;
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
	 * Set the activity for first selection (mobile design)
	 * 
	 * @param activity
	 */
	public void setActivity(ListPicturesForNameActivity activity) {
		this.activity = activity;
		this.fragment = (ListPicturesForNameFragment) activity.fragment;
	}

	/**
	 * Set the activity for first selection (tablet design)
	 * 
	 * @param activity
	 */
	public void setActivity(ListFoldersForDisplayActivity activity) {
		this.activity = activity;
		this.fragment = null;
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
					DisplayOneActivity.startActivity(activity, view.getEyePhoto().getAbsolutePath());
				}
				else if (selectedView == view) {
					cleanSelectedView();
					DisplayOneActivity.startActivity(activity, view.getEyePhoto().getAbsolutePath());
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
			
			if(fragment != null) {
				fragment.deactivateButtonAdditionalPictures();
			}
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
		
		if(fragment != null) {
			fragment.activateButtonAdditionalPictures();
		}
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
