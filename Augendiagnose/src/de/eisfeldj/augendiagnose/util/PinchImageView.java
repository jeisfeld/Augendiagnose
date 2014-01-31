package de.eisfeldj.augendiagnose.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

/**
 * A view for displaying an image, allowing moving and resizing with pinching
 */
public class PinchImageView extends ImageView {
	protected static final int INVALID_POINTER_ID = -1;
	protected boolean initialized = false;

	/**
	 * These are the relative positions of the Bitmap which are displayed in center. (These are maintained when
	 * recreating the view after rotating device)
	 */
	protected float mPosX, mPosY;
	protected float mScaleFactor = 1.f;
	private float mLastScaleFactor = 1.f;

	protected float mLastTouchX, mLastTouchY;
	protected float mLastTouchX0, mLastTouchY0;
	protected int mActivePointerId = INVALID_POINTER_ID;
	protected int mActivePointerId2 = INVALID_POINTER_ID;

	protected ScaleGestureDetector mScaleDetector;

	protected String mPathName = null;
	protected int mImageResource = -1;
	protected Bitmap mBitmap;

	protected static int maxBitmapSize = 2048;

	public PinchImageView(Context context) {
		this(context, null, 0);
	}

	public PinchImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PinchImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setScaleType(ScaleType.MATRIX);
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
	}

	/**
	 * Fill with an image, making the image fit into the view. If the pathName is unchanged (restored), then it is not
	 * refilled. The sizing (for fit) happens only once at first initialization of the view.
	 * 
	 * @param pathName
	 *            The pathname of the image
	 */
	public void setImage(String pathName) {
		if (!pathName.equals(mPathName)) {
			mBitmap = ImageUtil.getImageBitmap(pathName, maxBitmapSize);
			mPathName = pathName;
		}

		super.setImageBitmap(mBitmap);
		doInitialScaling();
	}

	/**
	 * Fill with an image from image resource, making the image fit into the view.
	 * 
	 * @param pathName
	 *            The image resource id
	 */
	public void setImage(int imageResource) {
		if (imageResource != mImageResource) {
			mBitmap = BitmapFactory.decodeResource(getResources(), imageResource);
			mImageResource = imageResource;
		}

		super.setImageBitmap(mBitmap);
		doInitialScaling();
	}

	/**
	 * Scale the image to fit into the view
	 */
	protected void doInitialScaling() {
		if (!initialized) {
			mPosX = 0;
			mPosY = 0;
			mScaleFactor = 1f;
			if (getHeight() > 0 && getWidth() > 0) {
				final float heightFactor = 1f * getHeight() / mBitmap.getHeight();
				final float widthFactor = 1f * getWidth() / mBitmap.getWidth();
				mScaleFactor = Math.min(widthFactor, heightFactor);
				mPosX = mBitmap.getWidth() / 2;
				mPosY = mBitmap.getHeight() / 2;
			}
			initialized = true;
		}
		mLastScaleFactor = mScaleFactor;
		setMatrix();
		invalidate();
	}

	/**
	 * Set the maximum size in which a bitmap is held in memory
	 * 
	 * @param size
	 */
	public static void setMaxBitmapSize(int size) {
		maxBitmapSize = size;
	}

	/**
	 * Redo the scaling
	 */
	private void setMatrix() {
		Matrix matrix = new Matrix();
		matrix.setTranslate(-mPosX, -mPosY);
		matrix.postScale(mScaleFactor, mScaleFactor);
		matrix.postTranslate(getWidth() / 2, getHeight() / 2);
		setImageMatrix(matrix);
	}

	/**
	 * Method to do the scaling based on pinching
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// Let the ScaleGestureDetector inspect all events.
		mScaleDetector.onTouchEvent(ev);
		final int action = ev.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			mLastTouchX = ev.getX();
			mLastTouchY = ev.getY();
			mActivePointerId = ev.getPointerId(0);
			break;
		}
		case MotionEvent.ACTION_POINTER_DOWN: {
			if (ev.getPointerCount() == 2) {
				final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
				mActivePointerId2 = ev.getPointerId(pointerIndex);
				mLastTouchX0 = (ev.getX(pointerIndex) + mLastTouchX) / 2;
				mLastTouchY0 = (ev.getY(pointerIndex) + mLastTouchY) / 2;
			}
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			handlePointerMove(ev);
			break;
		}
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL: {
			mActivePointerId = INVALID_POINTER_ID;
			mActivePointerId2 = INVALID_POINTER_ID;
			finishPointerMove(ev);
			break;
		}
		case MotionEvent.ACTION_POINTER_UP: {
			final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			final int pointerId = ev.getPointerId(pointerIndex);
			if (pointerId == mActivePointerId) {
				// This was our active pointer going up. Choose a new active pointer and adjust accordingly.
				final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
				mLastTouchX = ev.getX(newPointerIndex);
				mLastTouchY = ev.getY(newPointerIndex);
				mActivePointerId = ev.getPointerId(newPointerIndex);
				if (mActivePointerId == mActivePointerId2) {
					mActivePointerId2 = INVALID_POINTER_ID;
				}
			}
			else if (pointerId == mActivePointerId2) {
				mActivePointerId2 = INVALID_POINTER_ID;
			}
			break;
		}
		}
		return true;
	}

	/**
	 * Utility method to do the refresh after finishing the pointer move
	 * 
	 * @param ev
	 */
	protected void finishPointerMove(MotionEvent ev) {

	}

	/**
	 * Utility method to make the calculations in case of a pointer move
	 * 
	 * @param ev
	 */
	protected void handlePointerMove(MotionEvent ev) {
		final int pointerIndex = ev.findPointerIndex(mActivePointerId);
		final float x = ev.getX(pointerIndex);
		final float y = ev.getY(pointerIndex);

		if (mActivePointerId2 == INVALID_POINTER_ID) {
			// Only move if the ScaleGestureDetector isn't processing a gesture.
			final float dx = x - mLastTouchX;
			final float dy = y - mLastTouchY;
			mPosX -= dx / mScaleFactor;
			mPosY -= dy / mScaleFactor;
		}
		else {
			// When resizing, move according to the center of the two pinch points
			final int pointerIndex2 = ev.findPointerIndex(mActivePointerId2);
			final float x0 = (ev.getX(pointerIndex2) + x) / 2;
			final float y0 = (ev.getY(pointerIndex2) + y) / 2;
			final float dx = x0 - mLastTouchX0;
			final float dy = y0 - mLastTouchY0;
			mPosX -= dx / mScaleFactor;
			mPosY -= dy / mScaleFactor;
			if (mScaleFactor != mLastScaleFactor) {
				// When resizing, then position also changes
				final float changeFactor = mScaleFactor / mLastScaleFactor;
				mPosX = mPosX + (x0 - getWidth() / 2) * (changeFactor - 1) / mScaleFactor;
				mPosY = mPosY + (y0 - getHeight() / 2) * (changeFactor - 1) / mScaleFactor;
				mLastScaleFactor = mScaleFactor;
			}
			mLastTouchX0 = x0;
			mLastTouchY0 = y0;
		}
		mLastTouchX = x;
		mLastTouchY = y;

		setMatrix();
		invalidate();
	}

	/**
	 * Save scale factor, center position, path name and bitmap. (Bitmap to be retained if the view is recreated with
	 * same pathname.)
	 */
	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable("instanceState", super.onSaveInstanceState());
		bundle.putFloat("mScaleFactor", this.mScaleFactor);
		bundle.putFloat("mPosX", this.mPosX);
		bundle.putFloat("mPosY", this.mPosY);
		bundle.putString("mPathName", this.mPathName);
		bundle.putInt("mImageResource", this.mImageResource);
		bundle.putParcelable("mBitmap", mBitmap);
		return bundle;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			this.mScaleFactor = bundle.getFloat("mScaleFactor");
			this.mPosX = bundle.getFloat("mPosX");
			this.mPosY = bundle.getFloat("mPosY");
			this.mPathName = bundle.getString("mPathName");
			this.mImageResource = bundle.getInt("mImageResource");
			this.mBitmap = bundle.getParcelable("mBitmap");
			initialized = true;
			state = bundle.getParcelable("instanceState");
		}
		super.onRestoreInstanceState(state);
	}

	/**
	 * A listener determining the scale factor.
	 */
	protected class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScaleFactor *= detector.getScaleFactor();
			// Don't let the object get too small or too large.
			mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));
			invalidate();
			return true;
		}
	}
}
