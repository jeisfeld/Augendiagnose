package de.jeisfeld.augendiagnoselib.util;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.List;

import de.jeisfeld.augendiagnoselib.activities.DisplayOneActivity;
import de.jeisfeld.augendiagnoselib.activities.DisplayTwoActivity;
import de.jeisfeld.augendiagnoselib.activities.ListFoldersForDisplayActivity;
import de.jeisfeld.augendiagnoselib.activities.ListPicturesForNameActivity;
import de.jeisfeld.augendiagnoselib.activities.ListPicturesForSecondNameActivity;
import de.jeisfeld.augendiagnoselib.components.EyeImageView;
import de.jeisfeld.augendiagnoselib.fragments.ListPicturesForNameFragment;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto;

/**
 * A class handling the selection of up to two pictures for display, and the display of these pictures.
 */
public final class ImageSelectionAndDisplayHandler extends BaseImageSelectionHandler {
	/**
	 * The activity for first selection.
	 */
	@Nullable
	private Activity mActivity = null;
	/**
	 * The activity for second selection.
	 */
	@Nullable
	private ListPicturesForSecondNameActivity mSecondActivity = null;
	/**
	 * The fragment for first selection.
	 */
	@Nullable
	private ListPicturesForNameFragment mFragment = null;
	/**
	 * An instance of the ImageSelectionAndDisplayHandler - as singleton.
	 */
	@Nullable
	private static volatile ImageSelectionAndDisplayHandler mSingleton;

	/**
	 * Get an instance of the handler - it is handled as singleton.
	 *
	 * @return an instance of this class (as singleton).
	 */
	@Nullable
	public static ImageSelectionAndDisplayHandler getInstance() {
		if (mSingleton == null) {
			mSingleton = new ImageSelectionAndDisplayHandler();
		}
		return mSingleton;
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
	 * @param activity The activity to be set.
	 */
	public void setActivity(@NonNull final ListPicturesForNameActivity activity) {
		this.mActivity = activity;
		this.mFragment = activity.getListPicturesForNameFragment();
	}

	/**
	 * Set the activity for first selection (tablet design).
	 *
	 * @param activity The activity to be set.
	 */
	public void setActivity(final ListFoldersForDisplayActivity activity) {
		this.mActivity = activity;
		this.mFragment = null;
	}

	/**
	 * Set the activity for second selection.
	 *
	 * @param secondActivity The activity to be set.
	 */
	public void setSecondActivity(final ListPicturesForSecondNameActivity secondActivity) {
		this.mSecondActivity = secondActivity;
	}

	/**
	 * Clean all references.
	 */
	public static void clean() {
		mSingleton = null;
	}

	/**
	 * Prepare an EyeImageView for selection of the first picture.
	 *
	 * @param view The view to be prepared.
	 */
	public void prepareViewForFirstSelection(@NonNull final EyeImageView view) {
		// Ensure that selected view stays selected after rotating device
		if (getSelectedImage() != null && getSelectedImage().equals(view.getEyePhoto())) {
			selectView(view);
		}

		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (mActivity == null) {
					// Prevent NullPointerException
					return;
				}

				if (!hasSelectedView()) {
					DisplayOneActivity.startActivity(mActivity, view.getEyePhoto().getAbsolutePath());
				}
				else if (isSelectedView(view)) {
					cleanSelectedViews();
					DisplayOneActivity.startActivity(mActivity, view.getEyePhoto().getAbsolutePath());
				}
				else {
					DisplayTwoActivity.startActivity(mActivity, getSelectedImage().getAbsolutePath(), view.getEyePhoto()
							.getAbsolutePath(), false);
					cleanSelectedViews();
				}
			}
		});

		view.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(final View v) {
				if (isSelectedView(view)) {
					cleanSelectedViews();
				}
				else {
					cleanSelectedViews();
					selectView(view);
				}
				return true;
			}
		});
	}

	/**
	 * Prepare an EyeImageView for selection of the second picture.
	 *
	 * @param view The view to be prepared.
	 */
	public void prepareViewForSecondSelection(@NonNull final EyeImageView view) {
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				DisplayTwoActivity
						.startActivity(mActivity, getSelectedImage().getAbsolutePath(), view.getEyePhoto()
								.getAbsolutePath(), false);
				cleanSelectedViews();
				mSecondActivity.finish();
			}
		});
	}

	/**
	 * Unselect the selected view.
	 */
	@Override
	public void cleanSelectedViews() {
		if (hasSelectedView()) {
			super.cleanSelectedViews();

			if (mFragment != null) {
				mFragment.deactivateButtonAdditionalPictures();
			}
		}
	}

	/**
	 * Select a specific view.
	 *
	 * @param view the view to be selected.
	 */
	@Override
	protected void selectView(final EyeImageView view) {
		super.selectView(view);

		if (mFragment != null) {
			mFragment.activateButtonAdditionalPictures();
		}
	}

	/**
	 * Get the first selected eye photo, if existing.
	 *
	 * @return The selected eye photo.
	 */
	private EyePhoto getSelectedImage() {
		List<EyePhoto> selectedImages = getSelectedImages();
		if (selectedImages.size() == 0) {
			return null;
		}
		else {
			return selectedImages.get(0);
		}
	}
}
