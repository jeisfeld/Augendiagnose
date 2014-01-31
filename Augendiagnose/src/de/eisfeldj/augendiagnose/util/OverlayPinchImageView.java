package de.eisfeldj.augendiagnose.util;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import de.eisfeldj.augendiagnose.R;

/**
 * Extension of PinchImageView which adds the Iristopography overlays to the view.
 * 
 * @author Joerg
 * 
 */
public class OverlayPinchImageView extends PinchImageView {
	public static final int OVERLAY_COUNT = 5;
	private static final int OVERLAY_SIZE = 1024;
	private static final int OVERLAY_COLOR = Color.RED;

	private Drawable[] overlayCache = new Drawable[OVERLAY_COUNT];

	private float mOverlayX, mOverlayY;
	private float mOverlayScaleFactor;

	private boolean[] mShowOverlay = new boolean[OVERLAY_COUNT];
	private boolean mLocked = false;
	private EyePhoto mEyePhoto;
	private LayerDrawable mLayerDrawable;
	private Bitmap mCanvasBitmap;
	private Canvas mCanvas;

	private float mBrightness = 0f;
	private float mContrast = 1f;

	private Bitmap mBitmapSmall;

	public OverlayPinchImageView(Context context) {
		this(context, null, 0);
	}

	public OverlayPinchImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public OverlayPinchImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * Fill with an image, initializing the overlay position
	 */
	@Override
	public void setImage(String pathName) {
		mEyePhoto = new EyePhoto(pathName);
		if (!pathName.equals(mPathName)) {
			mBitmap = mEyePhoto.getImageBitmap(maxBitmapSize);
			mPathName = pathName;

			float size = Math.min(mBitmap.getWidth(), mBitmap.getHeight());

			// initial position of overlay
			mOverlayScaleFactor = size / OVERLAY_SIZE;
			mOverlayX = mBitmap.getWidth() / 2;
			mOverlayY = mBitmap.getHeight() / 2;

			mBitmapSmall = mEyePhoto.getImageBitmap(MediaStoreUtil.MINI_THUMB_SIZE);
		}

		mCanvasBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mCanvasBitmap);
		doInitialScaling();
		updateScaleGestureDetector();
		refresh(true);
	}

	/**
	 * Update the bitmap with the correct set of overlays
	 */
	public void refresh(boolean strict) {
		if (mCanvas == null) {
			return;
		}
		// Determine overlays to be shown
		ArrayList<Integer> overlayPositions = new ArrayList<Integer>();
		for (int i = 0; i < mShowOverlay.length; i++) {
			if (mShowOverlay[i]) {
				overlayPositions.add(i);
			}
		}

		Drawable[] layers = new Drawable[overlayPositions.size() + 1];
		Bitmap modBitmap;
		if (strict) {
			modBitmap = changeBitmapContrastBrightness(mBitmap, mContrast, mBrightness);
		}
		else {
			modBitmap = changeBitmapContrastBrightness(mBitmapSmall, mContrast, mBrightness);
		}
		layers[0] = new BitmapDrawable(getResources(), modBitmap);

		for (int i = 0; i < overlayPositions.size(); i++) {
			layers[i + 1] = getOverlayDrawable(overlayPositions.get(i));
		}

		mLayerDrawable = new LayerDrawable(layers);

		int width = mBitmap.getWidth();
		int height = mBitmap.getHeight();

		// position overlays
		for (int i = 1; i < mLayerDrawable.getNumberOfLayers(); i++) {
			mLayerDrawable.setLayerInset(i, (int) (mOverlayX - OVERLAY_SIZE / 2 * mOverlayScaleFactor),
					(int) (mOverlayY - OVERLAY_SIZE / 2 * mOverlayScaleFactor), //
					(int) (width - mOverlayX - OVERLAY_SIZE / 2 * mOverlayScaleFactor), //
					(int) (height - mOverlayY - OVERLAY_SIZE / 2 * mOverlayScaleFactor));
		}

		mLayerDrawable.setBounds(0, 0, width, height);

		mLayerDrawable.draw(mCanvas);

		super.setImageBitmap(mCanvasBitmap);
		invalidate();
	}

	/**
	 * Change the status of an overlay
	 * 
	 * @param position
	 *            number of the overlay
	 * @param show
	 *            flag indicating if the overlay should be shown
	 */
	public void showOverlay(int position, boolean show) {
		mShowOverlay[position] = show;
		refresh(true);
		updateScaleGestureDetector();
	}

	/**
	 * Trigger one overlay. If it is active, it will be deactivated. If it was inactive, then it will be activated, and
	 * the previous one will be deactivated.
	 * 
	 * @param position
	 *            number of the overlay
	 */
	public void triggerOverlay(int position) {
		for (int i = 0; i < mShowOverlay.length; i++) {
			if (i == position) {
				mShowOverlay[i] = !mShowOverlay[i];
			}
			else {
				mShowOverlay[i] = false;
			}
		}
		refresh(true);
		updateScaleGestureDetector();
	}

	/**
	 * Switch the lock status of the overlays
	 * 
	 * @param lock
	 */
	public void lockOverlay(boolean lock) {
		this.mLocked = lock;
		updateScaleGestureDetector();
	}

	/**
	 * Set the correct ScaleGestureDetector
	 */
	private void updateScaleGestureDetector() {
		if (pinchAll()) {
			mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
		}
		else {
			mScaleDetector = new ScaleGestureDetector(getContext(), new OverlayScaleListener());
		}
	}

	/**
	 * Helper method to return the overlay drawable of position i
	 * 
	 * @param position
	 * @return
	 */
	private Drawable getOverlayDrawable(int position) {
		if (overlayCache[position] == null) {
			int resource;

			switch (position) {
			case 1:
				if (mEyePhoto.getRightLeft().equals(EyePhoto.RightLeft.RIGHT)) {
					resource = R.drawable.overlay_topo1_r;
				}
				else {
					resource = R.drawable.overlay_topo1_l;
				}
				overlayCache[position] = getColouredDrawable(resource, OVERLAY_COLOR);
				break;
			case 2:
				if (mEyePhoto.getRightLeft().equals(EyePhoto.RightLeft.RIGHT)) {
					resource = R.drawable.overlay_topo2_r;
				}
				else {
					resource = R.drawable.overlay_topo2_l;
				}
				overlayCache[position] = getResources().getDrawable(resource);
				break;
			case 3:
				if (mEyePhoto.getRightLeft().equals(EyePhoto.RightLeft.RIGHT)) {
					resource = R.drawable.overlay_topo3_r;
				}
				else {
					resource = R.drawable.overlay_topo3_l;
				}
				overlayCache[position] = getColouredDrawable(resource, OVERLAY_COLOR);
				break;
			case 4:
				if (mEyePhoto.getRightLeft().equals(EyePhoto.RightLeft.RIGHT)) {
					resource = R.drawable.overlay_topo4_r;
				}
				else {
					resource = R.drawable.overlay_topo4_l;
				}
				overlayCache[position] = getColouredDrawable(resource, OVERLAY_COLOR);
				break;
			default:
				if (mEyePhoto.getRightLeft().equals(EyePhoto.RightLeft.RIGHT)) {
					resource = R.drawable.overlay_circle_l;
				}
				else {
					resource = R.drawable.overlay_circle_r;
				}
				overlayCache[position] = getColouredDrawable(resource, OVERLAY_COLOR);
				break;
			}
		}

		return overlayCache[position];
	}

	/**
	 * Create a drawable from a black image resource, having a changed colour
	 * 
	 * @param resource
	 * @param color
	 * @return
	 */
	private Drawable getColouredDrawable(int resource, int color) {
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resource);
		return new BitmapDrawable(getResources(), changeBitmapColor(bitmap, Color.RED));
	}

	/**
	 * Utility method to change a bitmap colour
	 * 
	 * @param sourceBitmap
	 * @param image
	 * @param color
	 */
	private static Bitmap changeBitmapColor(Bitmap bmp, int color) {
		Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

		Paint p = new Paint();
		ColorFilter filter = new LightingColorFilter(0, color);
		p.setColorFilter(filter);
		Canvas canvas = new Canvas(ret);
		canvas.drawBitmap(bmp, 0, 0, p);
		return ret;
	}

	/**
	 * Utility method to check if pinching includes overlays and the main picture.
	 * 
	 * @return
	 */
	private boolean pinchAll() {
		if (mLocked) {
			return true;
		}

		int overlayCount = 0;
		for (boolean b : mShowOverlay) {
			if (b) {
				overlayCount++;
			}
		}
		return overlayCount == 0;
	}

	/**
	 * Set the brightness
	 * 
	 * @param brightness
	 *            on a scale -1 to 1
	 */
	public void setBrightness(float brightness) {
		mBrightness = brightness;
		refresh(false);
	}

	/**
	 * Set the contrast
	 * 
	 * @param contrast
	 *            on a positive scale 0 to infinity, 1 is unchanged.
	 */
	public void setContrast(float contrast) {
		mContrast = contrast;
		refresh(false);
	}

	/**
	 * Utility method to make the calculations in case of a pointer move Overridden to handle zooming of overlay
	 * 
	 * @param ev
	 */
	@Override
	protected void handlePointerMove(MotionEvent ev) {
		if (pinchAll()) {
			super.handlePointerMove(ev);
			return;
		}

		final int pointerIndex = ev.findPointerIndex(mActivePointerId);
		final float x = ev.getX(pointerIndex);
		final float y = ev.getY(pointerIndex);

		if (mActivePointerId2 == INVALID_POINTER_ID) {
			// Only move if the ScaleGestureDetector isn't processing a gesture.
			final float dx = x - mLastTouchX;
			final float dy = y - mLastTouchY;
			mOverlayX += dx / mScaleFactor;
			mOverlayY += dy / mScaleFactor;
		}
		else {
			// When resizing, move according to the center of the two pinch points
			final int pointerIndex2 = ev.findPointerIndex(mActivePointerId2);
			final float x0 = (ev.getX(pointerIndex2) + x) / 2;
			final float y0 = (ev.getY(pointerIndex2) + y) / 2;
			final float dx = x0 - mLastTouchX0;
			final float dy = y0 - mLastTouchY0;
			mOverlayX += dx / mScaleFactor;
			mOverlayY += dy / mScaleFactor;
			mLastTouchX0 = x0;
			mLastTouchY0 = y0;
		}
		mLastTouchX = x;
		mLastTouchY = y;
		refresh(false);
	}

	/**
	 * Overridden to refresh the view in full details
	 * 
	 * @param ev
	 */
	protected void finishPointerMove(MotionEvent ev) {
		refresh(true);
	}

	/**
	 * @param bmp
	 *            input bitmap
	 * @param contrast
	 *            0..infinity - 1 is default
	 * @param brightness
	 *            -1..1 - 0 is default
	 * @return new bitmap
	 */
	protected static Bitmap changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness) {
		float offset = 255f / 2 * (1 - contrast + brightness * contrast + brightness);

		ColorMatrix cm = new ColorMatrix(new float[] { //
				contrast, 0, 0, 0, offset, //
						0, contrast, 0, 0, offset, //
						0, 0, contrast, 0, offset, //
						0, 0, 0, 1, 0 });

		Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

		Canvas canvas = new Canvas(ret);

		Paint paint = new Paint();
		paint.setColorFilter(new ColorMatrixColorFilter(cm));
		canvas.drawBitmap(bmp, 0, 0, paint);

		return ret;
	}

	/**
	 * Save brightness, contrast and overlay position
	 */
	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable("instanceState", super.onSaveInstanceState());
		bundle.putFloat("mOverlayX", this.mOverlayX);
		bundle.putFloat("mOverlayY", this.mOverlayY);
		bundle.putFloat("mOverlayScaleFactor", this.mOverlayScaleFactor);
		bundle.putBooleanArray("mShowOverlay", this.mShowOverlay);
		bundle.putBoolean("mLocked", this.mLocked);
		bundle.putFloat("mBrightness", this.mBrightness);
		bundle.putFloat("mContrast", this.mContrast);
		bundle.putParcelable("mBitmapSmall", mBitmapSmall);
		return bundle;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			this.mOverlayX = bundle.getFloat("mOverlayX");
			this.mOverlayY = bundle.getFloat("mOverlayY");
			this.mOverlayScaleFactor = bundle.getFloat("mOverlayScaleFactor");
			this.mShowOverlay = bundle.getBooleanArray("mShowOverlay");
			this.mLocked = bundle.getBoolean("mLocked");
			this.mBrightness = bundle.getFloat("mBrightness");
			this.mContrast = bundle.getFloat("mContrast");
			this.mBitmapSmall = bundle.getParcelable("mBitmapSmall");
			state = bundle.getParcelable("instanceState");
		}
		super.onRestoreInstanceState(state);
	}

	/**
	 * A listener determining the scale factor.
	 */
	private class OverlayScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mOverlayScaleFactor *= detector.getScaleFactor();
			// Don't let the object get too small or too large.
			mOverlayScaleFactor = Math.max(0.2f, Math.min(mOverlayScaleFactor, 5.0f));
			invalidate();
			return true;
		}
	}
}
