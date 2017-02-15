package de.jeisfeld.augendiagnoselib.util;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import de.jeisfeld.augendiagnoselib.components.EyeImageView;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto;

/**
 * Base handler for the selection of images. Contains only methods to handle the highlighting. Multiple views may be
 * selected, but currently only one image is supported.
 */
public abstract class BaseImageSelectionHandler {
	/**
	 * The views containing the selected image.
	 */
	private final List<EyeImageView> mSelectedViews = new ArrayList<>();

	/**
	 * Get the activity holding the images.
	 *
	 * @return the activity
	 */
	@Nullable
	protected abstract Activity getActivity();

	/**
	 * Change highlight setting of the selected views.
	 *
	 * @param highlight indicator if the views should be highlighted
	 */
	@SuppressWarnings("deprecation")
	private void highlightSelectedViews(final boolean highlight) {
		for (EyeImageView view : mSelectedViews) {
			if (highlight) {
				view.setBackgroundColor(getActivity().getResources().getColor(android.R.color.holo_orange_light));
			}
			else {
				view.setBackgroundColor(getActivity().getResources().getColor(android.R.color.transparent));
			}
		}
	}

	/**
	 * Unselect the selected view.
	 */
	// OVERRIDABLE
	public void cleanSelectedViews() {
		if (hasSelectedView()) {
			highlightSelectedViews(false);
			mSelectedViews.clear();
		}
	}

	/**
	 * Select a specific view.
	 *
	 * @param view the view to be selected.
	 */
	// OVERRIDABLE
	protected void selectView(final EyeImageView view) {
		mSelectedViews.add(view);
		highlightSelectedViews(true);
	}

	/**
	 * Deselect a specific view.
	 *
	 * @param view the view to be deselected.
	 */
	public void deselectView(final EyeImageView view) {
		if (getSelectedImages().contains(view.getEyePhoto())) {
			highlightSelectedViews(false);
			deselectEyePhoto(view.getEyePhoto());
			highlightSelectedViews(true);
		}
	}

	/**
	 * Remove the selections for an eye photo.
	 *
	 * @param eyePhoto The eye photo.
	 */
	private void deselectEyePhoto(final EyePhoto eyePhoto) {
		if (getSelectedImages().contains(eyePhoto)) {
			List<EyeImageView> newSelectedViews = new ArrayList<>();
			for (EyeImageView selectedView : mSelectedViews) {
				if (!selectedView.getEyePhoto().equals(eyePhoto)) {
					newSelectedViews.add(selectedView);
				}
			}
			cleanSelectedViews();
			for (EyeImageView selectedView : newSelectedViews) {
				selectView(selectedView);
			}
		}
	}

	/**
	 * Highlight an EyeImageView if it is selected.
	 *
	 * @param view the EyeImageView.
	 */
	public void highlightIfSelected(final EyeImageView view) {
		if (getSelectedImages().contains(view.getEyePhoto())) {
			selectView(view);
		}
	}

	/**
	 * Get information if any view is selected.
	 *
	 * @return true if a view is selected.
	 */
	protected final boolean hasSelectedView() {
		return mSelectedViews.size() > 0;
	}

	/**
	 * Get information if a given view is selected.
	 *
	 * @param view The given view.
	 * @return true if the given view is selected.
	 */
	protected final boolean isSelectedView(final View view) {
		return view instanceof EyeImageView && mSelectedViews.contains(view);
	}

	/**
	 * Get the selected eye photos.
	 *
	 * @return The selected eye photos.
	 */
	public final List<EyePhoto> getSelectedImages() {
		List<EyePhoto> selectedPhotos = new ArrayList<>();
		for (EyeImageView eyeImageView : mSelectedViews) {
			if (!selectedPhotos.contains(eyeImageView.getEyePhoto())) {
				selectedPhotos.add(eyeImageView.getEyePhoto());
			}
		}
		return selectedPhotos;
	}

}
