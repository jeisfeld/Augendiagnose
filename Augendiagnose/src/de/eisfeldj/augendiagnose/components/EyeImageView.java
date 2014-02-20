package de.eisfeldj.augendiagnose.components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import de.eisfeldj.augendiagnose.util.EyePhoto;
import de.eisfeldj.augendiagnose.util.MediaStoreUtil;

/**
 * A view for displaying an image.
 */
public class EyeImageView extends ImageView {
	private EyePhoto eyePhoto;
	private boolean initialized = false;

	public EyeImageView(Context context) {
		this(context, null, 0);
	}

	public EyeImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public EyeImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * Set the eye photo and create the bitmap
	 * 
	 * @param eyePhoto
	 */
	public void setEyePhoto(final EyePhoto eyePhoto, final Runnable postActivities) {
		this.eyePhoto = eyePhoto;
		// Fill pictures in separate thread, for performance reasons
		new Thread() {
			@Override
			public void run() {
				eyePhoto.precalculateImageBitmap(MediaStoreUtil.MINI_THUMB_SIZE);
				post(new Runnable() {
					@Override
					public void run() {
						setImageBitmap(eyePhoto.getImageBitmap(MediaStoreUtil.MINI_THUMB_SIZE));
						invalidate();
						initialized = true;
						if(postActivities != null) {
							postActivities.run();
						}
					}
				});
			}
		}.start();
	}

	/**
	 * Retrieve the eyePhoto object
	 * 
	 * @return
	 */
	public EyePhoto getEyePhoto() {
		return eyePhoto;
	}

	/**
	 * Mark as mInitialized to prevent double initialization
	 */
	public void setInitialized() {
		initialized = true;
	}

	/**
	 * Check if it is mInitialized
	 * 
	 * @return
	 */
	public boolean isInitialized() {
		return initialized;
	}
}
