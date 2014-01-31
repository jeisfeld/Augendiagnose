package de.eisfeldj.augendiagnose.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

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
	public void setEyePhoto(EyePhoto eyePhoto) {
		Logger.log("A " + eyePhoto);
		Logger.log("B " + eyePhoto.getImageBitmap(MediaStoreUtil.MINI_THUMB_SIZE));
		this.eyePhoto = eyePhoto;
		setImageBitmap(eyePhoto.getImageBitmap(MediaStoreUtil.MINI_THUMB_SIZE));
		invalidate();
		initialized = true;
	}
	
	/**
	 * Retrieve the eyePhoto object
	 * @return
	 */
	public EyePhoto getEyePhoto() {
		return eyePhoto;
	}

	/**
	 * Mark as initialized to prevent double initialization
	 */
	public void setInitialized() {
		initialized = true;
	}

	/**
	 * Check if it is initialized
	 * @return
	 */
	public boolean isInitialized() {
		return initialized;
	}
}
