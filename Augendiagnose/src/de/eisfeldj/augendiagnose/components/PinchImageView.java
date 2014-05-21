package de.eisfeldj.augendiagnose.components;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
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
import de.eisfeldj.augendiagnose.util.ImageUtil;

/**
 * A view for displaying an image, allowing moving and resizing with pinching
 */
public class PinchImageView extends ImageView {
	protected static final int INVALID_POINTER_ID = -1;
	protected boolean mInitialized = false;

	/**
	 * Field used to check if a gesture was moving the image (then no context menu will appear)
	 */
	protected boolean mHasMoved = false;

	/**
	 * These are the relative positions of the Bitmap which are displayed in center of the screen. Range: [0,1]
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
	 * @param activity
	 *            The triggering activity (required for bitmap caching)
	 * @param cacheIndex
	 *            A unique index of the view in the activity
	 */
	public void setImage(final String pathName, Activity activity, int cacheIndex) {
		// retrieve bitmap from cache if possible
		final RetainFragment retainFragment = RetainFragment.findOrCreateRetainFragment(activity.getFragmentManager(),
				cacheIndex);
		mBitmap = retainFragment.bitmap;

		if (mBitmap == null || !pathName.equals(mPathName)) {
			// populate bitmaps in separate thread, so that screen keeps fluid.
			// This also ensures that this happens only after view is visible and sized.
			new Thread() {
				@Override
				public void run() {
					mBitmap = ImageUtil.getImageBitmap(pathName, maxBitmapSize);
					retainFragment.bitmap = mBitmap;
					mPathName = pathName;
					post(new Runnable() {
						@Override
						public void run() {
							PinchImageView.super.setImageBitmap(mBitmap);
							doInitialScaling();
						}
					});
				}
			}.start();
		}
		else {
			super.setImageBitmap(mBitmap);
			doInitialScaling();
		}
	}

	/**
	 * Fill with an image from image resource, making the image fit into the view.
	 * 
	 * @param imageResource
	 *            The image resource id
	 * @param activity
	 *            The triggering activity (required for bitmap caching)
	 * @param cacheIndex
	 *            A unique index of the view in the activity
	 */
	public void setImage(final int imageResource, Activity activity, int cacheIndex) {
		// retrieve bitmap from cache if possible
		final RetainFragment retainFragment = RetainFragment.findOrCreateRetainFragment(activity.getFragmentManager(),
				cacheIndex);
		mBitmap = retainFragment.bitmap;

		if (mBitmap == null || imageResource != mImageResource) {
			new Thread() {
				@Override
				public void run() {
					mBitmap = BitmapFactory.decodeResource(getResources(), imageResource);
					retainFragment.bitmap = mBitmap;
					mImageResource = imageResource;
					post(new Runnable() {
						@Override
						public void run() {
							PinchImageView.super.setImageBitmap(mBitmap);
							doInitialScaling();
						}
					});
				}
			}.start();
			mBitmap = BitmapFactory.decodeResource(getResources(), imageResource);
			mImageResource = imageResource;
		}
		else {
			super.setImageBitmap(mBitmap);
			doInitialScaling();
		}

	}

	/**
	 * Return the natural scale factor that fits the image into the view
	 * 
	 * @return
	 */
	protected float getNaturalScaleFactor() {
		float heightFactor = 1f * getHeight() / mBitmap.getHeight();
		float widthFactor = 1f * getWidth() / mBitmap.getWidth();
		float result = Math.min(widthFactor, heightFactor);
		return result == 0 ? 1f : result;
	}

	/**
	 * Return an orientation independent scale factor that fits the smaller image dimension into the smaller view
	 * dimension
	 * 
	 * @return
	 */
	protected float getOrientationIndependentScaleFactor() {
		float viewSize = Math.min(getWidth(), getHeight());
		float imageSize = Math.min(mBitmap.getWidth(), mBitmap.getHeight());
		return 1f * viewSize / imageSize;
	}

	/**
	 * Scale the image to fit into the view
	 */
	protected void doInitialScaling() {
		if (!mInitialized) {
			mPosX = 0.5f;
			mPosY = 0.5f;
			mScaleFactor = getNaturalScaleFactor();
			mInitialized = true;
		}
		mLastScaleFactor = mScaleFactor;
		requestLayout();
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
		matrix.setTranslate(-mPosX * mBitmap.getWidth(), -mPosY * mBitmap.getHeight());
		matrix.postScale(mScaleFactor, mScaleFactor);
		matrix.postTranslate(getWidth() / 2, getHeight() / 2);
		setImageMatrix(matrix);
	}

	/**
	 * Override invalidate to reposition the image
	 */
	@Override
	public void requestLayout() {
		super.requestLayout();
		if (mBitmap != null) {
			setMatrix();
		}
	}

	/**
	 * Method to do the scaling based on pinching
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// Let the ScaleGestureDetector inspect all events.
		mScaleDetector.onTouchEvent(ev);
		final int action = ev.getActionMasked();
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			mHasMoved = false;
			mLastTouchX = ev.getX();
			mLastTouchY = ev.getY();
			mActivePointerId = ev.getPointerId(0);
			break;
		}
		case MotionEvent.ACTION_POINTER_DOWN: {
			mHasMoved = true;
			if (ev.getPointerCount() == 2) {
				final int pointerIndex = (ev.getActionIndex());
				mActivePointerId2 = ev.getPointerId(pointerIndex);
				mLastTouchX0 = (ev.getX(pointerIndex) + mLastTouchX) / 2;
				mLastTouchY0 = (ev.getY(pointerIndex) + mLastTouchY) / 2;
			}
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			// Prevent NullPointerException if bitmap is not yet loaded
			if (mBitmap != null) {
				boolean moved = handlePointerMove(ev);
				mHasMoved = mHasMoved || moved;
			}
			break;
		}
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL: {
			mHasMoved = false;
			mActivePointerId = INVALID_POINTER_ID;
			mActivePointerId2 = INVALID_POINTER_ID;
			finishPointerMove(ev);
			break;
		}
		case MotionEvent.ACTION_POINTER_UP: {
			final int pointerIndex = (ev.getActionIndex());
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

		if (isLongClickable()) {
			return super.onTouchEvent(ev);
		}
		else {
			return true;
		}
	}

	/**
	 * Perform long click only if no move has happened
	 */
	@Override
	public boolean performLongClick() {
		if (!mHasMoved) {
			return super.performLongClick();
		}
		else {
			return true;
		}
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
	protected boolean handlePointerMove(MotionEvent ev) {
		boolean moved = false;
		final int pointerIndex = ev.findPointerIndex(mActivePointerId);
		final float x = ev.getX(pointerIndex);
		final float y = ev.getY(pointerIndex);

		if (mActivePointerId2 == INVALID_POINTER_ID) {
			// Only move if the ScaleGestureDetector isn't processing a gesture.
			final float dx = x - mLastTouchX;
			final float dy = y - mLastTouchY;
			mPosX -= dx / mScaleFactor / mBitmap.getWidth();
			mPosY -= dy / mScaleFactor / mBitmap.getHeight();
		}
		else {
			// When resizing, move according to the center of the two pinch points
			final int pointerIndex2 = ev.findPointerIndex(mActivePointerId2);
			final float x0 = (ev.getX(pointerIndex2) + x) / 2;
			final float y0 = (ev.getY(pointerIndex2) + y) / 2;
			final float dx = x0 - mLastTouchX0;
			final float dy = y0 - mLastTouchY0;
			mPosX -= dx / mScaleFactor / mBitmap.getWidth();
			mPosY -= dy / mScaleFactor / mBitmap.getHeight();
			if (mScaleFactor != mLastScaleFactor) {
				// When resizing, then position also changes
				final float changeFactor = mScaleFactor / mLastScaleFactor;
				mPosX = mPosX + (x0 - getWidth() / 2) * (changeFactor - 1) / mScaleFactor / mBitmap.getWidth();
				mPosY = mPosY + (y0 - getHeight() / 2) * (changeFactor - 1) / mScaleFactor / mBitmap.getHeight();
				mLastScaleFactor = mScaleFactor;
				moved = true;
			}
			mLastTouchX0 = x0;
			mLastTouchY0 = y0;
		}

		if (x != mLastTouchX || y != mLastTouchY) {
			mLastTouchX = x;
			mLastTouchY = y;
			moved = true;
		}

		setMatrix();
		invalidate();
		return moved;
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
		bundle.putBoolean("mInitialized", mInitialized);
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
			this.mInitialized = bundle.getBoolean("mInitialized");
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

	/**
	 * Helper fragment to retain the bitmap on configuration change
	 */
	protected static class RetainFragment extends Fragment {
		private static final String TAG = "RetainFragment";
		public Bitmap bitmap;

		public RetainFragment() {
		}

		public static RetainFragment findOrCreateRetainFragment(FragmentManager fm, int index) {
			RetainFragment fragment = (RetainFragment) fm.findFragmentByTag(TAG + index);
			if (fragment == null) {
				fragment = new RetainFragment();
				fm.beginTransaction().add(fragment, TAG + index).commit();
			}
			return fragment;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setRetainInstance(true);
		}
	}

}
