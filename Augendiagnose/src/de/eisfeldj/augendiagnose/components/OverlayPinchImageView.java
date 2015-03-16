package de.eisfeldj.augendiagnose.components;

import java.util.ArrayList;

import android.app.Activity;
import android.app.FragmentManager;
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
import de.eisfeldj.augendiagnose.util.EyePhoto;
import de.eisfeldj.augendiagnose.util.EyePhoto.RightLeft;
import de.eisfeldj.augendiagnose.util.JpegMetadata;
import de.eisfeldj.augendiagnose.util.MediaStoreUtil;

/**
 * Extension of PinchImageView which adds the Iristopography overlays to the view.
 *
 * @author Joerg
 *
 */
public class OverlayPinchImageView extends PinchImageView {
	/**
	 * The number of overlays (including circle).
	 */
	public static final int OVERLAY_COUNT = 8;

	/**
	 * The size of the overlays (in pixels).
	 */
	private static final int OVERLAY_SIZE = 1024;

	/**
	 * The minimum scale factor allowed.
	 */
	private static final float MIN_OVERLAY_SCALE_FACTOR = 0.2f;

	/**
	 * The maximum scale factor allowed.
	 */
	private static final float MAX_OVERLAY_SCALE_FACTOR = 5f;

	/**
	 * The limiting value of contrast (must ensure that offset is smaller than 2^15).
	 */
	private static final float CONTRAST_LIMIT = 0.98f;

	/**
	 * The color of the one-colored overlays.
	 */
	private int mOverlayColor = Color.RED;

	/**
	 * An array of the available overlays.
	 */
	private Drawable[] mOverlayCache = new Drawable[OVERLAY_COUNT];

	/**
	 * These are the relative positions of the overlay center on the bitmap. Range: [0,1]
	 */
	private float mOverlayX, mOverlayY;

	/**
	 * The scale factor of the overlays.
	 */
	private float mOverlayScaleFactor, mLastOverlayScaleFactor;

	/**
	 * An array indicating which overlays are displayed.
	 */
	private boolean[] mShowOverlay = new boolean[OVERLAY_COUNT];

	/**
	 * Flag indicating if the overlays are locked.
	 */
	private boolean mLocked = false;

	/**
	 * The eye photo displayed.
	 */
	private EyePhoto mEyePhoto;

	/**
	 * The drawable on which eye photo and overlays are drawn.
	 */
	private LayerDrawable mLayerDrawable;

	/**
	 * The bitmap drawn on the canvas.
	 */
	private Bitmap mCanvasBitmap;

	/**
	 * The canvas on which the drawing is done.
	 */
	private Canvas mCanvas;

	/**
	 * The brightness (-1 to 1) of the bitmap. Default: 0.
	 */
	private float mBrightness = 0f;

	/**
	 * The contrast (0 to infinity) of the bitmap. Default: 1.
	 */
	private float mContrast = 1f;

	/**
	 * A small version of the bitmap.
	 */
	private Bitmap mBitmapSmall;

	/**
	 * The metadata of the image.
	 */
	private JpegMetadata mMetadata;

	/**
	 * Flag indicating if the overlay position is defined for the eye photo.
	 */
	private boolean mHasOverlayPosition = false;

	/**
	 * Flag indicating if the view position is stored for the eye photo.
	 */
	private boolean mHasViewPosition = false;

	/**
	 * Flag indicating if the view is showing a full resolution snapshot.
	 */
	private boolean mIsFullResolutionShapshot = false;

	/**
	 * Callback class to update the GUI elements from the view.
	 */
	private GuiElementUpdater mGuiElementUpdater;

	// JAVADOC:OFF
	/**
	 * Standard constructor to be implemented for all views.
	 *
	 * @see #View(Context)
	 */
	public OverlayPinchImageView(final Context context) {
		this(context, null, 0);
	}

	/**
	 * Standard constructor to be implemented for all views.
	 *
	 * @see #View(Context, AttributeSet)
	 */
	public OverlayPinchImageView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Standard constructor to be implemented for all views.
	 *
	 * @see #View(Context, AttributeSet, int)
	 */
	public OverlayPinchImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	// JAVADOC:ON

	/**
	 * Fill with an image, initializing from metadata.
	 *
	 * @param pathName
	 *            The pathname of the image
	 * @param activity
	 *            The triggering activity (required for bitmap caching)
	 * @param cacheIndex
	 *            A unique index of the view in the activity
	 */
	@Override
	public final void setImage(final String pathName, final Activity activity, final int cacheIndex) {
		mEyePhoto = new EyePhoto(pathName);

		final RetainFragment retainFragment = RetainFragment.findOrCreateRetainFragment(activity.getFragmentManager(),
				cacheIndex);
		mBitmap = retainFragment.getBitmap();
		mBitmapSmall = retainFragment.getBitmapSmall();

		if (mBitmap == null || !pathName.equals(mPathName)) {
			mHasOverlayPosition = false;
			mPathName = pathName;
			mBitmap = null;

			// Do image loading in separate thread
			Thread thread = new Thread() {
				@Override
				public void run() {
					mBitmap = mEyePhoto.getImageBitmap(maxBitmapSize);
					mBitmapSmall = mEyePhoto.getImageBitmap(MediaStoreUtil.MINI_THUMB_SIZE);
					mMetadata = mEyePhoto.getImageMetadata();
					retainFragment.setBitmap(mBitmap);
					retainFragment.setBitmapSmall(mBitmapSmall);

					post(new Runnable() {
						@Override
						public void run() {
							if (mMetadata != null && mMetadata.hasOverlayPosition()) {
								// stored position of overlay
								mHasOverlayPosition = true;
								mOverlayX = mMetadata.xCenter;
								mOverlayY = mMetadata.yCenter;
								mOverlayScaleFactor = mMetadata.overlayScaleFactor
										* Math.max(mBitmap.getHeight(), mBitmap.getWidth()) / OVERLAY_SIZE;
								lockOverlay(true, false);
								if (mGuiElementUpdater != null) {
									mGuiElementUpdater.setLockChecked(true);
								}
							}
							else {
								// initial position of overlay
								resetOverlayPosition(false);
							}
							if (mMetadata != null && mMetadata.hasViewPosition()) {
								mHasViewPosition = true;
							}
							if (mMetadata != null && mMetadata.hasBrightnessContrast()) {
								mBrightness = mMetadata.brightness.floatValue();
								mContrast = mMetadata.contrast.floatValue();
								if (mGuiElementUpdater != null) {
									mGuiElementUpdater.updateSeekbarBrightness(mBrightness);
									mGuiElementUpdater
											.updateSeekbarContrast(storedContrastToSeekbarContrast(mContrast));
								}
							}
							if (mMetadata != null && mMetadata.overlayColor != null) {
								mOverlayColor = mMetadata.overlayColor.intValue();
								mGuiElementUpdater.updateOverlayColorButton(mOverlayColor);
							}

							mLastOverlayScaleFactor = mOverlayScaleFactor;

							mCanvasBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(),
									Bitmap.Config.ARGB_8888);
							mCanvas = new Canvas(mCanvasBitmap);
							doInitialScaling();
							updateScaleGestureDetector();
							refresh(true);
						}
					});
				}
			};
			thread.start();
		}
		else {
			mCanvasBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
			mCanvas = new Canvas(mCanvasBitmap);
			doInitialScaling();
			updateScaleGestureDetector();
			refresh(true);
		}

	}

	@Override
	protected final void doInitialScaling() {
		// If available, use stored position
		if (!mInitialized && mHasViewPosition) {
			mPosX = mMetadata.xPosition;
			mPosY = mMetadata.yPosition;
			mScaleFactor = mMetadata.zoomFactor * getOrientationIndependentScaleFactor();
			mInitialized = true;
		}
		// Otherwise, if available, use overlay position
		if (!mInitialized && mHasOverlayPosition) {
			mPosX = mOverlayX;
			mPosY = mOverlayY;
			mScaleFactor = 1f;
			if (getHeight() > 0 && getWidth() > 0) {
				final float size = Math.min(getHeight(), getWidth());
				mScaleFactor = size / (OVERLAY_SIZE * mOverlayScaleFactor);
				mInitialized = true;
			}
		}
		// Otherwise, use default (set if mInitialized = false)
		super.doInitialScaling();
	}

	/**
	 * Update the bitmap with the correct set of overlays.
	 *
	 * @param strict
	 *            indicates if full resolution is required
	 */
	public final void refresh(final boolean strict) {
		if (mCanvas == null || !mInitialized) {
			return;
		}
		// Determine overlays to be shown
		ArrayList<Integer> overlayPositions = new ArrayList<Integer>();

		if (canHandleOverlays()) {
			for (int i = 0; i < mShowOverlay.length; i++) {
				if (mShowOverlay[i]) {
					overlayPositions.add(i);
				}
			}
		}

		Drawable[] layers = new Drawable[overlayPositions.size() + 1];
		Bitmap modBitmap;
		if (strict) {
			modBitmap = changeBitmapContrastBrightness(mBitmap, mContrast, mBrightness);
		}
		else {
			// for performance reasons, use only low resolution bitmap while pinching
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
			mLayerDrawable.setLayerInset(i, (int) (mOverlayX * mBitmap.getWidth() - OVERLAY_SIZE / 2
					* mOverlayScaleFactor), (int) (mOverlayY * mBitmap.getHeight() - OVERLAY_SIZE / 2
					* mOverlayScaleFactor), //
					(int) (width - mOverlayX * mBitmap.getWidth() - OVERLAY_SIZE / 2 * mOverlayScaleFactor), //
					(int) (height - mOverlayY * mBitmap.getHeight() - OVERLAY_SIZE / 2 * mOverlayScaleFactor));
		}

		mLayerDrawable.setBounds(0, 0, width, height);

		mLayerDrawable.draw(mCanvas);

		setImageBitmap(mCanvasBitmap);
		invalidate();
	}

	/**
	 * Get information if the view can handle overlays.
	 *
	 * @return true if the view can handle overlays. This is possible only if the right/left position of the eye photo
	 *         is defined.
	 */
	public final boolean canHandleOverlays() {
		return mEyePhoto != null && mEyePhoto.getRightLeft() != null;
	}

	/**
	 * Change the status of an overlay.
	 *
	 * @param position
	 *            number of the overlay
	 * @param show
	 *            flag indicating if the overlay should be shown
	 */
	public final void showOverlay(final int position, final boolean show) {
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
	public final void triggerOverlay(final int position) {
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
	 * Switch the lock status of the overlays.
	 *
	 * @param lock
	 *            the target lock status
	 * @param store
	 *            a flag indicating if the lock status should be stored.
	 */
	public final void lockOverlay(final boolean lock, final boolean store) {
		this.mLocked = lock;
		updateScaleGestureDetector();

		if (lock && store && mInitialized) {
			if (mMetadata != null) {
				if (mMetadata.rightLeft == null) {
					// If image did not yet pass metadata setting, do it now.
					mEyePhoto.updateMetadataWithDefaults(mMetadata);
				}

				mMetadata.xCenter = mOverlayX;
				mMetadata.yCenter = mOverlayY;
				mMetadata.overlayScaleFactor = mOverlayScaleFactor / Math.max(mBitmap.getWidth(), mBitmap.getHeight())
						* OVERLAY_SIZE;
				mEyePhoto.storeImageMetadata(mMetadata);
			}
		}
	}

	/**
	 * Reset the overlay position.
	 *
	 * @param store
	 *            flag indicating if the overlay position should be stored.
	 */
	public final void resetOverlayPosition(final boolean store) {
		float size = Math.min(mBitmap.getWidth(), mBitmap.getHeight());
		mOverlayScaleFactor = size / OVERLAY_SIZE;
		mOverlayX = ONE_HALF;
		mOverlayY = ONE_HALF;
		if (store && mInitialized) {
			if (mMetadata != null) {
				mMetadata.xCenter = null;
				mMetadata.yCenter = null;
				mMetadata.overlayScaleFactor = null;
				mEyePhoto.storeImageMetadata(mMetadata);
			}
		}

		mLocked = false;
		for (int i = 0; i < OVERLAY_COUNT; i++) {
			mShowOverlay[i] = false;
		}
		if (mGuiElementUpdater != null) {
			mGuiElementUpdater.setLockChecked(false);
			mGuiElementUpdater.resetOverlays();
		}
	}

	/**
	 * Set the correct ScaleGestureDetector.
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
	 * Helper method to create the overlay drawable of position i.
	 *
	 * @param position
	 *            The position of the overlay drawable.
	 * @return The overlay drawable.
	 */
	private Drawable getOverlayDrawable(final int position) {
		if (mOverlayCache[position] == null) {
			int resource;

			switch (position) {
			case 1:
				if (mEyePhoto.getRightLeft().equals(RightLeft.RIGHT)) {
					resource = R.drawable.overlay_topo1_r;
				}
				else {
					resource = R.drawable.overlay_topo1_l;
				}
				mOverlayCache[position] = getColouredDrawable(resource, mOverlayColor);
				break;
			case 2:
				if (mEyePhoto.getRightLeft().equals(RightLeft.RIGHT)) {
					resource = R.drawable.overlay_topo2_r;
				}
				else {
					resource = R.drawable.overlay_topo2_l;
				}
				mOverlayCache[position] = getResources().getDrawable(resource);
				break;
			case 3: // MAGIC_NUMBER
				if (mEyePhoto.getRightLeft().equals(RightLeft.RIGHT)) {
					resource = R.drawable.overlay_topo5_r;
				}
				else {
					resource = R.drawable.overlay_topo5_l;
				}
				mOverlayCache[position] = getColouredDrawable(resource, mOverlayColor);
				break;
			case 4: // MAGIC_NUMBER
				if (mEyePhoto.getRightLeft().equals(RightLeft.RIGHT)) {
					resource = R.drawable.overlay_topo3_r;
				}
				else {
					resource = R.drawable.overlay_topo3_l;
				}
				mOverlayCache[position] = getColouredDrawable(resource, mOverlayColor);
				break;
			case 5: // MAGIC_NUMBER
				if (mEyePhoto.getRightLeft().equals(RightLeft.RIGHT)) {
					resource = R.drawable.overlay_topo4_r;
				}
				else {
					resource = R.drawable.overlay_topo4_l;
				}
				mOverlayCache[position] = getColouredDrawable(resource, mOverlayColor);
				break;
			case 6: // MAGIC_NUMBER
				if (mEyePhoto.getRightLeft().equals(RightLeft.RIGHT)) {
					resource = R.drawable.overlay_topo6_r;
				}
				else {
					resource = R.drawable.overlay_topo6_l;
				}
				mOverlayCache[position] = getColouredDrawable(resource, mOverlayColor);
				break;
			case 7: // MAGIC_NUMBER
				if (mEyePhoto.getRightLeft().equals(RightLeft.RIGHT)) {
					resource = R.drawable.overlay_topo7_r;
				}
				else {
					resource = R.drawable.overlay_topo7_l;
				}
				mOverlayCache[position] = getColouredDrawable(resource, mOverlayColor);
				break;
			default:
				if (mEyePhoto.getRightLeft().equals(RightLeft.RIGHT)) {
					resource = R.drawable.overlay_circle_l;
				}
				else {
					resource = R.drawable.overlay_circle_r;
				}
				mOverlayCache[position] = getColouredDrawable(resource, mOverlayColor);
				break;
			}
		}

		return mOverlayCache[position];
	}

	/**
	 * Create a drawable from a black image resource, having a changed colour.
	 *
	 * @param resource
	 *            The black image resource
	 * @param color
	 *            The target color
	 * @return The modified drawable, with the intended color.
	 */
	private Drawable getColouredDrawable(final int resource, final int color) {
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resource);
		return new BitmapDrawable(getResources(), changeBitmapColor(bitmap, color));
	}

	/**
	 * Utility method to change a bitmap colour.
	 *
	 * @param sourceBitmap
	 *            The original bitmap
	 * @param color
	 *            The target color
	 * @return the bitmap with the target color.
	 */
	private static Bitmap changeBitmapColor(final Bitmap sourceBitmap, final int color) {
		Bitmap ret = Bitmap.createBitmap(sourceBitmap.getWidth(), sourceBitmap.getHeight(), sourceBitmap.getConfig());

		Paint p = new Paint();
		ColorFilter filter = new LightingColorFilter(0, color);
		p.setAlpha(color >>> 24); // MAGIC_NUMBER
		p.setColorFilter(filter);
		Canvas canvas = new Canvas(ret);
		canvas.drawBitmap(sourceBitmap, 0, 0, p);
		return ret;
	}

	/**
	 * Utility method to check if pinching includes overlays and the main picture.
	 *
	 * @return true if pinching includes everything. false if pinching should just pinch the overlay.
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
	 * Set the brightness.
	 *
	 * @param brightness
	 *            on a scale -1 to 1
	 */
	public final void setBrightness(final float brightness) {
		mBrightness = brightness;
		refresh(false);
	}

	/**
	 * Set the contrast.
	 *
	 * @param contrast
	 *            on a positive scale 0 to infinity, 1 is unchanged.
	 */
	public final void setContrast(final float contrast) {
		// input goes from -1 to 1. Output goes from 0 to infinity.
		mContrast = seekbarContrastToStoredContrast(contrast);
		refresh(false);
	}

	/**
	 * Set the overlay color.
	 *
	 * @param overlayColor
	 *            the overlay color (such as Color.RED)
	 */
	public final void setOverlayColor(final int overlayColor) {
		mOverlayColor = overlayColor;
		mOverlayCache = new Drawable[OVERLAY_COUNT];
		mGuiElementUpdater.updateOverlayColorButton(overlayColor);
		refresh(true);
	}

	/**
	 * Get the overlay color.
	 *
	 * @return the overlay color (such as Color.RED)
	 */
	public final int getOverlayColor() {
		return mOverlayColor;
	}

	/**
	 * Store brightness and contrast in the image metadata.
	 *
	 * @param delete
	 *            delete brightness and contrast from metadata.
	 */
	public final void storeBrightnessContrast(final boolean delete) {
		if (mInitialized && mMetadata != null) {
			if (delete) {
				mMetadata.brightness = null;
				mMetadata.contrast = null;
				mBrightness = 0;
				mContrast = 1;
				if (mGuiElementUpdater != null) {
					mGuiElementUpdater.updateSeekbarBrightness(mBrightness);
					mGuiElementUpdater.updateSeekbarContrast(storedContrastToSeekbarContrast(mContrast));
				}
			}
			else {
				mMetadata.brightness = mBrightness;
				mMetadata.contrast = mContrast;
			}

			mEyePhoto.storeImageMetadata(mMetadata);
		}
	}

	/**
	 * Convert contrast from (-1,1) scale to (0,infty) scale.
	 *
	 * @param seekbarContrast
	 *            the contrast on (-1,1) scale.
	 * @return the contrast on (0,infty) scale.
	 */
	private static float seekbarContrastToStoredContrast(final float seekbarContrast) {
		float contrastImd = (float) (Math.asin(seekbarContrast) * 2 / Math.PI);
		return 2f / (1f - contrastImd * CONTRAST_LIMIT) - 1f;
	}

	/**
	 * Convert contrast from (0,infty) scale to (-1,1) scale.
	 *
	 * @param storedContrast
	 *            the contrast on (0,infty) scale.
	 * @return the contrast on (-1,1) scale.
	 */
	private static float storedContrastToSeekbarContrast(final float storedContrast) {
		float contrastImd = (1f - 2f / (storedContrast + 1f)) / CONTRAST_LIMIT;
		return (float) Math.sin(Math.PI * contrastImd / 2);
	}

	/**
	 * Store position and zoom in the image metadata.
	 *
	 * @param delete
	 *            delete position and zoom from metadata.
	 */
	public final void storePositionZoom(final boolean delete) {
		if (mInitialized && mMetadata != null) {
			if (delete) {
				mHasViewPosition = false;
				mMetadata.xPosition = null;
				mMetadata.yPosition = null;
				mMetadata.zoomFactor = null;

				// Reset to original view size
				mInitialized = false;
				doInitialScaling();
			}
			else {
				mHasViewPosition = true;
				mMetadata.xPosition = mPosX;
				mMetadata.yPosition = mPosY;
				mMetadata.zoomFactor = mScaleFactor / getOrientationIndependentScaleFactor();
			}

			mEyePhoto.storeImageMetadata(mMetadata);
		}
	}

	/**
	 * Store the overlay color in the image metadata.
	 *
	 * @param delete
	 *            delete the overlay color from metadata.
	 */
	public final void storeOverlayColor(final boolean delete) {
		if (mInitialized && mMetadata != null) {
			if (delete) {
				mMetadata.overlayColor = null;
				setOverlayColor(mGuiElementUpdater.getOverlayDefaultColor());
			}
			else {
				mMetadata.overlayColor = mOverlayColor;
			}

			mEyePhoto.storeImageMetadata(mMetadata);
		}
	}

	/*
	 * Utility method to make the calculations in case of a pointer move Overridden to handle zooming of overlay.
	 */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY",
			justification = "Using floating point equality to see if value has changed")
	@Override
	protected final boolean handlePointerMove(final MotionEvent ev) {
		if (mIsFullResolutionShapshot) {
			setImageBitmap(mCanvasBitmap);
			mIsFullResolutionShapshot = false;
		}

		if (pinchAll()) {
			return super.handlePointerMove(ev);
		}
		boolean moved = false;

		final int pointerIndex = ev.findPointerIndex(mActivePointerId);
		final float x = ev.getX(pointerIndex);
		final float y = ev.getY(pointerIndex);

		if (mActivePointerId2 == INVALID_POINTER_ID) {
			// Only move if the ScaleGestureDetector isn't processing a gesture.
			final float dx = x - mLastTouchX;
			final float dy = y - mLastTouchY;
			mOverlayX += dx / mScaleFactor / mBitmap.getWidth();
			mOverlayY += dy / mScaleFactor / mBitmap.getHeight();
		}
		else {
			// When resizing, move according to the center of the two pinch points
			final int pointerIndex2 = ev.findPointerIndex(mActivePointerId2);
			final float x0 = (ev.getX(pointerIndex2) + x) / 2;
			final float y0 = (ev.getY(pointerIndex2) + y) / 2;
			final float dx = x0 - mLastTouchX0;
			final float dy = y0 - mLastTouchY0;
			mOverlayX += dx / mScaleFactor / mBitmap.getWidth();
			mOverlayY += dy / mScaleFactor / mBitmap.getHeight();
			if (mOverlayScaleFactor != mLastOverlayScaleFactor) {
				// When resizing, then position also changes
				final float changeFactor = mOverlayScaleFactor / mLastOverlayScaleFactor;
				final float pinchX = (x0 - getWidth() / 2) / mScaleFactor / mBitmap.getWidth() + mPosX;
				final float pinchY = (y0 - getHeight() / 2) / mScaleFactor / mBitmap.getHeight() + mPosY;
				mOverlayX = pinchX + (mOverlayX - pinchX) * changeFactor;
				mOverlayY = pinchY + (mOverlayY - pinchY) * changeFactor;
				mLastOverlayScaleFactor = mOverlayScaleFactor;
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

		if (mOverlayX < 0) {
			mOverlayX = 0;
		}
		if (mOverlayY < 0) {
			mOverlayY = 0;
		}
		if (mOverlayX > 1) {
			mOverlayX = 1f;
		}
		if (mOverlayY > 1) {
			mOverlayY = 1f;
		}

		refresh(false);
		return moved;
	}

	/*
	 * Overridden to refresh the view in full details.
	 */
	@Override
	protected final void finishPointerMove(final MotionEvent ev) {
		refresh(true);
	}

	/**
	 * Update contrast and brightness of a bitmap.
	 *
	 * @param bmp
	 *            input bitmap
	 * @param contrast
	 *            0..infinity - 1 is default
	 * @param brightness
	 *            -1..1 - 0 is default
	 * @return new bitmap
	 */
	private static Bitmap
			changeBitmapContrastBrightness(final Bitmap bmp, final float contrast, final float brightness) {
		float offset = 255f / 2 * (1 - contrast + brightness * contrast + brightness); // MAGIC_NUMBER for 1 byte
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
	 * Retrieve the metadata of the image.
	 *
	 * @return the metadata of the image
	 */
	public final JpegMetadata getMetadata() {
		return mMetadata;
	}

	/**
	 * Show the current view in full resolution.
	 */
	public final void showFullResolutionSnapshot() {
		// The image pixels which are in the corners of the view
		float leftX = mPosX * mBitmap.getWidth() - getWidth() / 2 / mScaleFactor;
		float rightX = mPosX * mBitmap.getWidth() + getWidth() / 2 / mScaleFactor;
		float upperY = mPosY * mBitmap.getHeight() - getHeight() / 2 / mScaleFactor;
		float lowerY = mPosY * mBitmap.getHeight() + getHeight() / 2 / mScaleFactor;

		// The image part which needs to be displayed
		float minX = Math.max(0, leftX / mBitmap.getWidth());
		float maxX = Math.min(1, rightX / mBitmap.getWidth());
		float minY = Math.max(0, upperY / mBitmap.getHeight());
		float maxY = Math.min(1, lowerY / mBitmap.getHeight());

		if (maxX <= minX || maxY <= minY) {
			// Image is outside of the view
			return;
		}

		// The distance of the displayed image from the view borders.
		int offsetX = Math.round(-Math.min(0, leftX) * mScaleFactor);
		int offsetY = Math.round(-Math.min(0, upperY) * mScaleFactor);
		int offsetMaxX = Math.round(Math.max(rightX - mBitmap.getWidth(), 0) * mScaleFactor);
		int offsetMaxY = Math.round(Math.max(lowerY - mBitmap.getHeight(), 0) * mScaleFactor);

		Bitmap partialBitmap =
				mEyePhoto.getPartialBitmap(minX, maxX, minY, maxY);
		Bitmap scaledPartialBitmap =
				Bitmap.createScaledBitmap(partialBitmap, getWidth() - offsetMaxX - offsetX, getHeight() - offsetMaxY
						- offsetY, false);
		Bitmap partialBitmapWithBrightness =
				changeBitmapContrastBrightness(scaledPartialBitmap, mContrast, mBrightness);

		Bitmap fullViewBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(fullViewBitmap);
		canvas.drawBitmap(partialBitmapWithBrightness, offsetX, offsetY, null);

		// Make a straight display of this bitmap withoht any matrix transformation.
		// Will be reset by regular view as soon as the screen is touched again.
		setImageBitmap(fullViewBitmap);
		setImageMatrix(null);
		mIsFullResolutionShapshot = true;
	}

	/**
	 * Store the comment in the image.
	 *
	 * @param comment
	 *            the comment to be stored.
	 */
	public final void storeComment(final String comment) {
		if (mInitialized && mMetadata != null) {
			mMetadata.comment = comment;
			mEyePhoto.storeImageMetadata(mMetadata);
		}
	}

	/*
	 * Save brightness, contrast and overlay position.
	 */
	@Override
	protected final Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable("instanceState", super.onSaveInstanceState());
		bundle.putFloat("mOverlayX", this.mOverlayX);
		bundle.putFloat("mOverlayY", this.mOverlayY);
		bundle.putFloat("mOverlayScaleFactor", this.mOverlayScaleFactor);
		bundle.putBooleanArray("mShowOverlay", this.mShowOverlay);
		bundle.putBoolean("mLocked", this.mLocked);
		bundle.putFloat("mBrightness", this.mBrightness);
		bundle.putFloat("mContrast", this.mContrast);
		bundle.putInt("mOverlayColor", mOverlayColor);
		bundle.putParcelable("mMetadata", mMetadata);
		return bundle;
	}

	@Override
	protected final void onRestoreInstanceState(final Parcelable state) {
		Parcelable enhancedState = state;
		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			this.mOverlayX = bundle.getFloat("mOverlayX");
			this.mOverlayY = bundle.getFloat("mOverlayY");
			this.mOverlayScaleFactor = bundle.getFloat("mOverlayScaleFactor");
			mLastOverlayScaleFactor = mOverlayScaleFactor;
			this.mShowOverlay = bundle.getBooleanArray("mShowOverlay");
			this.mLocked = bundle.getBoolean("mLocked");
			this.mBrightness = bundle.getFloat("mBrightness");
			this.mContrast = bundle.getFloat("mContrast");
			this.mOverlayColor = bundle.getInt("mOverlayColor");
			this.mMetadata = bundle.getParcelable("mMetadata");
			enhancedState = bundle.getParcelable("instanceState");
		}
		super.onRestoreInstanceState(enhancedState);
	}

	/**
	 * A listener determining the scale factor.
	 */
	private class OverlayScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(final ScaleGestureDetector detector) {
			mOverlayScaleFactor *= detector.getScaleFactor();
			// Don't let the object get too small or too large.
			mOverlayScaleFactor =
					Math.max(MIN_OVERLAY_SCALE_FACTOR, Math.min(mOverlayScaleFactor, MAX_OVERLAY_SCALE_FACTOR));
			invalidate();
			return true;
		}
	}

	/**
	 * Set the reference that allows GUI updates.
	 *
	 * @param updater
	 *            The GUI Element updater
	 */
	public final void setGuiElementUpdater(final GuiElementUpdater updater) {
		mGuiElementUpdater = updater;
	}

	/**
	 * Interface that allows the view to update GUI elements from the activity holding the view.
	 */
	public interface GuiElementUpdater {
		/**
		 * Set the checked status of the lock button.
		 *
		 * @param checked
		 *            the lock status.
		 */
		void setLockChecked(boolean checked);

		/**
		 * Update the brightness bar.
		 *
		 * @param brightness
		 *            The brightness.
		 */
		void updateSeekbarBrightness(float brightness);

		/**
		 * Update the contrast bar.
		 *
		 * @param contrast
		 *            The contrast.
		 */
		void updateSeekbarContrast(float contrast);

		/**
		 * Update the overlay color button.
		 *
		 * @param color
		 *            The color displayed in the button.
		 */
		void updateOverlayColorButton(int color);

		/**
		 * Retrieve the default color for the overlay.
		 *
		 * @return The default color for the overlay.
		 */
		int getOverlayDefaultColor();

		/**
		 * Reset the overlays.
		 */
		void resetOverlays();
	}

	/**
	 * Helper listFoldersFragment to retain the bitmap on configuration change.
	 */
	public static class RetainFragment extends PinchImageView.RetainFragment {
		/**
		 * Tag to be used as identifier of the fragment.
		 */
		private static final String TAG = "RetainFragment";

		/**
		 * The small version of the bitmap.
		 */
		private Bitmap bitmapSmall;

		private Bitmap getBitmapSmall() {
			return bitmapSmall;
		}

		private void setBitmapSmall(final Bitmap bitmapSmall) {
			this.bitmapSmall = bitmapSmall;
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
		public static final RetainFragment findOrCreateRetainFragment(final FragmentManager fm, final int index) {
			RetainFragment fragment = (RetainFragment) fm.findFragmentByTag(TAG + index);
			if (fragment == null) {
				fragment = new RetainFragment();
				fm.beginTransaction().add(fragment, TAG + index).commit();
			}
			return fragment;
		}

	}

}
