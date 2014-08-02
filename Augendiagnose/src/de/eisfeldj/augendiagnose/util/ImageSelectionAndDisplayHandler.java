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
 * A class handling the selection of up to two pictures for display, and the display of these pictures.
 */
public final class ImageSelectionAndDisplayHandler {
	/**
	 * The selected view.
	 */
	private EyeImageView selectedView = null;
	/**
	 * The activity for first selection.
	 */
	private Activity activity = null;
	/**
	 * The activity for second selection.
	 */
	private ListPicturesForSecondNameActivity secondActivity = null;
	/**
	 * The fragment for first selection.
	 */
	private ListPicturesForNameFragment fragment = null;
	/**
	 * An instance of the ImageSelectionAndDisplayHandler - as singleton.
	 */
	private static volatile ImageSelectionAndDisplayHandler singleton;

	/**
	 * Get an instance of the handler - it is handled as singleton.
	 *
	 * @return an instance of this class (as singleton).
	 */
	public static ImageSelectionAndDisplayHandler getInstance() {
		if (singleton == null) {
			singleton = new ImageSelectionAndDisplayHandler();
		}
		return singleton;
	}

	/**
	 * Hide default constructor.
	 */
	private ImageSelectionAndDisplayHandler() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Set the activity for first selection (mobile design).
	 *
	 * @param activity
	 *            The activity to be set.
	 */
	public void setActivity(final ListPicturesForNameActivity activity) {
		this.activity = activity;
		this.fragment = (ListPicturesForNameFragment) activity.fragment;
	}

	/**
	 * Set the activity for first selection (tablet design).
	 *
	 * @param activity
	 *            The activity to be set.
	 */
	public void setActivity(final ListFoldersForDisplayActivity activity) {
		this.activity = activity;
		this.fragment = null;
	}

	/**
	 * Set the activiy for second selection.
	 *
	 * @param secondActivity
	 *            The activity to be set.
	 */
	public void setSecondActivity(final ListPicturesForSecondNameActivity secondActivity) {
		this.secondActivity = secondActivity;
	}

	/**
	 * Clean all references.
	 */
	public static void clean() {
		singleton = null;
	}

	/**
	 * Prepare an EyeImageView for selection of the first picture.
	 *
	 * @param view
	 *            The view to be prepared.
	 */
	public void prepareViewForFirstSelection(final EyeImageView view) {
		// Ensure that selected view stays selected after rotating device
		if ((selectedView != null) && selectedView.getEyePhoto().equals(view.getEyePhoto())) {
			selectView(view);
		}

		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (activity == null) {
					// Prevent NullPointerException
					return;
				}

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
			public boolean onLongClick(final View v) {
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
	 * Prepare an EyeImageView for selection of the second picture.
	 *
	 * @param view
	 *            The view to be prepared.
	 */
	public void prepareViewForSecondSelection(final EyeImageView view) {
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				DisplayTwoActivity
						.startActivity(activity, getSelectedImagePath(), view.getEyePhoto().getAbsolutePath());
				cleanSelectedView();
				secondActivity.finish();
			}
		});
	}

	/**
	 * Change the highlighting setting of the selected view.
	 *
	 * @param highlight
	 *            true if the view should be highlighted.
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

			if (fragment != null) {
				fragment.deactivateButtonAdditionalPictures();
			}
		}
	}

	/**
	 * Select a specific view.
	 *
	 * @param view
	 *            the view to be selected.
	 */
	private void selectView(final EyeImageView view) {
		selectedView = view;
		highlightSelectedView(true);

		if (fragment != null) {
			fragment.activateButtonAdditionalPictures();
		}
	}

	/**
	 * Retrieve the path of the selected image.
	 *
	 * @return the path of the selected image.
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
