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
public final class ImageSelectionAndDisplayHandler extends BaseImageSelectionHandler {
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
	 * Hide default constructor, to ensure singleton use.
	 */
	private ImageSelectionAndDisplayHandler() {
		// default constructor
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
	 * Unselect the selected view.
	 */
	@Override
	public void cleanSelectedView() {
		if (selectedView != null) {
			super.cleanSelectedView();

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
	@Override
	protected void selectView(final EyeImageView view) {
		super.selectView(view);

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

	@Override
	protected Activity getActivity() {
		return activity;
	}
}
