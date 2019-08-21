package de.jeisfeld.augendiagnoselib.components;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto;
import de.jeisfeld.augendiagnoselib.util.imagefile.MediaStoreUtil;

/**
 * A view for displaying an eye image.
 */
public class EyeImageView extends ImageView {
	/**
	 * The EyePhoto shown in the view.
	 */
	@Nullable
	private EyePhoto mEyePhoto;
	/**
	 * Indicates if the view is initialized.
	 */
	private boolean mInitialized = false;

	/**
	 * Standard constructor to be implemented for all views.
	 *
	 * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
	 * @see android.view.View#View(Context)
	 */
	public EyeImageView(final Context context) {
		this(context, null, 0);
	}

	/**
	 * Standard constructor to be implemented for all views.
	 *
	 * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
	 * @param attrs   The attributes of the XML tag that is inflating the view.
	 * @see android.view.View#View(Context, AttributeSet)
	 */
	public EyeImageView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Standard constructor to be implemented for all views.
	 *
	 * @param context  The Context the view is running in, through which it can access the current theme, resources, etc.
	 * @param attrs    The attributes of the XML tag that is inflating the view.
	 * @param defStyle An attribute in the current theme that contains a reference to a style resource that supplies default
	 *                 values for the view. Can be 0 to not look for defaults.
	 * @see android.view.View#View(Context, AttributeSet, int)
	 */
	public EyeImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * Set the eye photo and create the bitmap.
	 *
	 * @param activity       The activity holding the view.
	 * @param newEyePhoto    The eyePhoto to be displayed.
	 * @param postActivities Activities that may be run on the UI thread after loading the image.
	 */
	public final void setEyePhoto(@NonNull final Activity activity, @NonNull final EyePhoto newEyePhoto, @Nullable final Runnable postActivities) {
		this.mEyePhoto = newEyePhoto;
		// Fill pictures in separate thread, for performance reasons
		Thread thread = new Thread() {
			@Override
			public void run() {
				newEyePhoto.precalculateImageBitmap(MediaStoreUtil.MINI_THUMB_SIZE);
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						setImageBitmap(newEyePhoto.getImageBitmap(MediaStoreUtil.MINI_THUMB_SIZE));
						invalidate();
						mInitialized = true;
						if (postActivities != null) {
							postActivities.run();
						}
					}
				});
			}
		};
		thread.start();
	}

	/**
	 * Clean the eye photo from the view.
	 */
	public final void cleanEyePhoto() {
		this.mEyePhoto = null;
		setImageBitmap(null);
	}

	/**
	 * Retrieve the eyePhoto object.
	 *
	 * @return the eye photo.
	 */
	@Nullable
	public final EyePhoto getEyePhoto() {
		return mEyePhoto;
	}

	/**
	 * Mark as mInitialized to prevent double initialization.
	 */
	public final void setInitialized() {
		mInitialized = true;
	}

	/**
	 * Check if it is mInitialized.
	 *
	 * @return true if it is initialized.
	 */
	public final boolean isInitialized() {
		return mInitialized;
	}
}
