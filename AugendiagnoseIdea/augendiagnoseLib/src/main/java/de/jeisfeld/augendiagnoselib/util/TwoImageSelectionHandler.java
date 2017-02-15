package de.jeisfeld.augendiagnoselib.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;

import de.jeisfeld.augendiagnoselib.activities.SelectTwoPicturesActivity;
import de.jeisfeld.augendiagnoselib.components.EyeImageView;

/**
 * A class handling the selection two pictures, returning the pictures.
 */
public final class TwoImageSelectionHandler extends BaseImageSelectionHandler {
	/**
	 * A holder of the TwoImageSelectionHandler as singleton.
	 */
	@Nullable
	private static volatile TwoImageSelectionHandler mSingleton;

	/**
	 * Get an instance of the handler - it is handled as singleton.
	 *
	 * @return an instance of the handler.
	 */
	@Nullable
	public static TwoImageSelectionHandler getInstance() {
		if (mSingleton == null) {
			mSingleton = new TwoImageSelectionHandler();
		}
		return mSingleton;
	}

	/**
	 * Hide default constructor, to ensure singleton use.
	 */
	private TwoImageSelectionHandler() {
		// default constructor
	}

	/**
	 * Clean all references.
	 */
	public static void clean() {
		mSingleton = null;
	}

	/**
	 * Prepare a GridView for selection of the pictures.
	 *
	 * @param activity       The Activity using the handler.
	 * @param view           The GridView to be prepared.
	 * @param hasContextMenu Flag indicating if a context menu should be enabled.
	 */
	public void prepareViewForSelection(final SelectTwoPicturesActivity activity, @NonNull final EyeImageView view, final boolean hasContextMenu) {
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (getSelectedImages().contains(view.getEyePhoto())) {
					deselectView(view);
					if (getSelectedImages().size() == 0) {
						activity.displayButtons(false);
					}
				}
				else {
					if (getSelectedImages().size() >= 2) {
						cleanSelectedViews();
					}
					selectView(view);
					activity.displayButtons(true);
				}
			}
		});

		if (hasContextMenu) {
			view.setOnCreateContextMenuListener(activity);
		}
	}
}
