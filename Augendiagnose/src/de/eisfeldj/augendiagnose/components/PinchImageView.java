package de.eisfeldj.augendiagnose.components;

import android.annotation.SuppressLint;
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
 * A view for displaying an image, allowing moving and resizing with pinching.
 */
public class PinchImageView extends ImageView {
	/**
	 * The default maximum resolution of a bitmap.
	 */
	private static final int DEFAULT_MAX_BITMAP_SIZE = 2048;

	/**
	 * One half - the relative middle position.
	 */
	protected static final float ONE_HALF = 0.5f;

	/**
	 * The minimum scale factor allowed.
	 */
	private static final float MIN_SCALE_FACTOR = 0.1f;

	/**
	 * The maximum scale factor allowed.
	 */
	private static final float MAX_SCALE_FACTOR = 10f;

	// PUBLIC_FIELDS:START
	// Fields are used also in OverlayPinchImageView.

	/**
	 * Pointer id used in case of invalid pointer.
	 */
	protected static final int INVALID_POINTER_ID = -1;

	/**
	 * Indicator if the view is initialized with the image bitmap.
	 */
	protected boolean mInitialized = false;

	/**
	 * Field used to check if a gesture was moving the image (then no context menu will appear).
	 */
	protected boolean mHasMoved = false;

	/**
	 * These are the relative positions of the Bitmap which are displayed in center of the screen. Range: [0,1]
	 */
	protected float mPosX, mPosY;

	/**
	 * This is the scale factor of the image.
	 */
	protected float mScaleFactor = 1.f;

	/**
	 * The last touch position.
	 */
	protected float mLastTouchX, mLastTouchY;

	/**
	 * The last average touch position (used when pinching and moving at the same time).
	 */
	protected float mLastTouchX0, mLastTouchY0;

	/**
	 * The primary pointer id.
	 */
	protected int mActivePointerId = INVALID_POINTER_ID;

	/**
	 * The secondary pointer id.
	 */
	protected int mActivePointerId2 = INVALID_POINTER_ID;

	/**
	 * A ScaleGestureDetector detecting the scale change.
	 */
	protected ScaleGestureDetector mScaleDetector;

	/**
	 * The path name of the displayed image.
	 */
	protected String mPathName = null;

	/**
	 * The resource id of the displayed image.
	 */
	protected int mImageResource = -1;

	/**
	 * The displayed bitmap.
	 */
	protected Bitmap mBitmap;

	/**
	 * The maximum allowed resolution of the bitmap. The image is scaled to this size.
	 */
	protected static int maxBitmapSize = DEFAULT_MAX_BITMAP_SIZE;

	// PUBLIC_FIELDS:END

	/**
	 * The last scale factor.
	 */
	private float mLastScaleFactor = 1.f;

	// JAVADOC:OFF
	/**
	 * Standard constructor to be implemented for all views.
	 *
	 * @see #View(Context)
	 */
	public PinchImageView(final Context context) {
		this(context, null, 0);
	}

	/**
	 * Standard constructor to be implemented for all views.
	 *
	 * @see #View(Context, AttributeSet)
	 */
	public PinchImageView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Standard constructor to be implemented for all views.
	 *
	 * @see #View(Context, AttributeSet, int)
	 */
	public PinchImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		setScaleType(ScaleType.MATRIX);
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
	}

	// JAVADOC:ON

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
	// OVERRIDABLE
	public void setImage(final String pathName, final Activity activity, final int cacheIndex) {
		// retrieve bitmap from cache if possible
		final RetainFragment retainFragment = RetainFragment.findOrCreateRetainFragment(activity.getFragmentManager(),
				cacheIndex);
		mBitmap = retainFragment.bitmap;

		if (mBitmap == null || !pathName.equals(mPathName)) {
			// populate bitmaps in separate thread, so that screen keeps fluid.
			// This also ensures that this happens only after view is visible and sized.
			Thread thread = new Thread() {
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
			};
			thread.start();
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
	public final void setImage(final int imageResource, final Activity activity, final int cacheIndex) {
		// retrieve bitmap from cache if possible
		final RetainFragment retainFragment = RetainFragment.findOrCreateRetainFragment(activity.getFragmentManager(),
				cacheIndex);
		mBitmap = retainFragment.bitmap;

		if (mBitmap == null || imageResource != mImageResource) {
			Thread thread = new Thread() {
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
			};
			thread.start();
			mBitmap = BitmapFactory.decodeResource(getResources(), imageResource);
			mImageResource = imageResource;
		}
		else {
			super.setImageBitmap(mBitmap);
			doInitialScaling();
		}

	}

	/**
	 * Return the natural scale factor that fits the image into the view.
	 *
	 * @return The natural scale factor fitting the image into the view.
	 */
	protected final float getNaturalScaleFactor() {
		float heightFactor = 1f * getHeight() / mBitmap.getHeight();
		float widthFactor = 1f * getWidth() / mBitmap.getWidth();
		float result = Math.min(widthFactor, heightFactor);
		return result == 0 ? 1f : result;
	}

	/**
	 * Return an orientation independent scale factor that fits the smaller image dimension into the smaller view
	 * dimension.
	 *
	 * @return A scale factor fitting the image independent of the orientation.
	 */
	protected final float getOrientationIndependentScaleFactor() {
		float viewSize = Math.min(getWidth(), getHeight());
		float imageSize = Math.min(mBitmap.getWidth(), mBitmap.getHeight());
		return 1f * viewSize / imageSize;
	}

	/**
	 * Scale the image to fit into the view.
	 */
	// OVERRIDABLE
	protected void doInitialScaling() {
		if (!mInitialized) {
			mPosX = ONE_HALF;
			mPosY = ONE_HALF;
			mScaleFactor = getNaturalScaleFactor();
			mInitialized = true;
		}
		mLastScaleFactor = mScaleFactor;
		requestLayout();
		invalidate();
	}

	/**
	 * Set the maximum size in which a bitmap is held in memory.
	 *
	 * @param size
	 *            the maximum size (pixels)
	 */
	public static void setMaxBitmapSize(final int size) {
		maxBitmapSize = size;
	}

	/**
	 * Redo the scaling.
	 */
	private void setMatrix() {
		Matrix matrix = new Matrix();
		matrix.setTranslate(-mPosX * mBitmap.getWidth(), -mPosY * mBitmap.getHeight());
		matrix.postScale(mScaleFactor, mScaleFactor);
		matrix.postTranslate(getWidth() / 2, getHeight() / 2);
		setImageMatrix(matrix);
	}

	/**
	 * Override invalidate to reposition the image.
	 */
	@Override
	public final void requestLayout() {
		super.requestLayout();
		if (mBitmap != null) {
			setMatrix();
		}
	}

	/*
	 * Method to do the scaling based on pinching.
	 */
	// OVERRIDABLE
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(final MotionEvent ev) {
		// Let the ScaleGestureDetector inspect all events.
		mScaleDetector.onTouchEvent(ev);
		final int action = ev.getActionMasked();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mHasMoved = false;
			mLastTouchX = ev.getX();
			mLastTouchY = ev.getY();
			mActivePointerId = ev.getPointerId(0);
			break;

		case MotionEvent.ACTION_POINTER_DOWN:
			mHasMoved = true;
			if (ev.getPointerCount() == 2) {
				final int pointerIndex = ev.getActionIndex();
				mActivePointerId2 = ev.getPointerId(pointerIndex);
				mLastTouchX0 = (ev.getX(pointerIndex) + mLastTouchX) / 2;
				mLastTouchY0 = (ev.getY(pointerIndex) + mLastTouchY) / 2;
			}
			break;

		case MotionEvent.ACTION_MOVE:
			// Prevent NullPointerException if bitmap is not yet loaded
			if (mBitmap != null) {
				boolean moved = handlePointerMove(ev);
				mHasMoved = mHasMoved || moved;
			}
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mHasMoved = false;
			mActivePointerId = INVALID_POINTER_ID;
			mActivePointerId2 = INVALID_POINTER_ID;
			finishPointerMove(ev);
			break;

		case MotionEvent.ACTION_POINTER_UP:
			final int pointerIndex = ev.getActionIndex();
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

		default:
			break;

		}

		if (isLongClickable()) {
			return super.onTouchEvent(ev);
		}
		else {
			return true;
		}
	}

	/*
	 * Perform long click only if no move has happened.
	 */
	@Override
	public final boolean performLongClick() {
		if (!mHasMoved) {
			return super.performLongClick();
		}
		else {
			return true;
		}
	}

	/**
	 * Utility method to do the refresh after finishing the pointer move.
	 *
	 * @param ev
	 *            The motion event.
	 */
	// OVERRIDABLE
	protected void finishPointerMove(final MotionEvent ev) {
		// do nothing
	}

	/**
	 * Utility method to make the calculations in case of a pointer move.
	 *
	 * @param ev
	 *            The motion event.
	 * @return true if a move has been made (i.e. the position of the image changed).
	 */
	// OVERRIDABLE
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY",
			justification = "Using floating point equality to see if value has changed")
	protected boolean handlePointerMove(final MotionEvent ev) {
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

	/*
	 * Save scale factor, center position, path name and bitmap. (Bitmap to be retained if the view is recreated with
	 * same pathname.)
	 */
	// OVERRIDABLE
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

	// OVERRIDABLE
	@Override
	protected void onRestoreInstanceState(final Parcelable state) {
		Parcelable enhancedState = state;
		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			this.mScaleFactor = bundle.getFloat("mScaleFactor");
			this.mPosX = bundle.getFloat("mPosX");
			this.mPosY = bundle.getFloat("mPosY");
			this.mPathName = bundle.getString("mPathName");
			this.mImageResource = bundle.getInt("mImageResource");
			this.mInitialized = bundle.getBoolean("mInitialized");
			enhancedState = bundle.getParcelable("instanceState");
		}
		super.onRestoreInstanceState(enhancedState);
	}

	/**
	 * A listener determining the scale factor.
	 */
	protected class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public final boolean onScale(final ScaleGestureDetector detector) {
			mScaleFactor *= detector.getScaleFactor();
			// Don't let the object get too small or too large.
			mScaleFactor = Math.max(MIN_SCALE_FACTOR, Math.min(mScaleFactor, MAX_SCALE_FACTOR));
			invalidate();
			return true;
		}
	}

	/**
	 * Helper listFoldersFragment to retain the bitmap on configuration change.
	 */
	public static class RetainFragment extends Fragment {
		/**
		 * Tag to be used as identifier of the fragment.
		 */
		private static final String TAG = "RetainFragment";
		/**
		 * The bitmap to be stored.
		 */
		private Bitmap bitmap;

		public final Bitmap getBitmap() {
			return bitmap;
		}

		public final void setBitmap(final Bitmap bitmap) {
			this.bitmap = bitmap;
		}

		/**
		 * Get the retainFragment - search it by the index. If not found, create a new one.
		 *
		 * @param fm
		 *            The fragment manager handling this fragment.
		 * @param index
		 *            The index of the view (required in case of multiple PinchImageViews to be retained).
		 * @return the retainFragment.
		 */
		public static RetainFragment findOrCreateRetainFragment(final FragmentManager fm, final int index) {
			RetainFragment fragment = (RetainFragment) fm.findFragmentByTag(TAG + index);
			if (fragment == null) {
				fragment = new RetainFragment();
				fm.beginTransaction().add(fragment, TAG + index).commit();
			}
			return fragment;
		}

		@Override
		public final void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setRetainInstance(true);
		}
	}

}
