package de.jeisfeld.augendiagnoselib.components;

import static de.jeisfeld.augendiagnoselib.components.OverlayPinchImageView.Resolution.FULL;
import static de.jeisfeld.augendiagnoselib.components.OverlayPinchImageView.Resolution.HIGH;
import static de.jeisfeld.augendiagnoselib.components.OverlayPinchImageView.Resolution.LOW;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto.RightLeft;
import de.jeisfeld.augendiagnoselib.util.imagefile.ImageUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.JpegMetadata;
import de.jeisfeld.augendiagnoselib.util.imagefile.MediaStoreUtil;

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
	public static final int OVERLAY_COUNT = Application.getAppContext().getResources().getIntArray(R.array.overlay_types).length;

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
	 * The partial bitmap with full resolution.
	 */
	private Bitmap mPartialBitmapFullResolution;

	/**
	 * The full bitmap (full resolution).
	 */
	private Bitmap mBitmapFull = null;

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
	 * Flag indicating if brightness/contrast is changed, but not yet applied to mBitmap.
	 */
	private boolean mRequiresBrightnessContrastUpdate = false;

	/**
	 * Last height of the view. Used to make sure that full resolution is abandoned as soon as view size changes.
	 */
	private int mLastHeight;

	/**
	 * Last width of the view. Used to make sure that full resolution is abandoned as soon as view size changes.
	 */
	private int mLastWidth;

	/**
	 * Thread showing the view in full resolution. The first is the running thread. Another one may be queuing.
	 */
	private List<Thread> mFullResolutionThreads = new ArrayList<Thread>();

	/**
	 * Callback class to update the GUI elements from the view.
	 */
	private GuiElementUpdater mGuiElementUpdater;

	/**
	 * A String indicating if full resolution image should be automatically loaded or even kept in memory.
	 */
	private boolean mFullResolutionFlag;

	/**
	 * The retain fragment.
	 */
	private RetainFragment mRetainFragment;

	/**
	 * Standard constructor to be implemented for all views.
	 *
	 * @param context
	 *            The Context the view is running in, through which it can access the current theme, resources, etc.
	 * @see android.view.View#View(Context)
	 */
	public OverlayPinchImageView(final Context context) {
		this(context, null, 0);
	}

	/**
	 * Standard constructor to be implemented for all views.
	 *
	 * @param context
	 *            The Context the view is running in, through which it can access the current theme, resources, etc.
	 * @param attrs
	 *            The attributes of the XML tag that is inflating the view.
	 * @see android.view.View#View(Context, AttributeSet)
	 */
	public OverlayPinchImageView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Standard constructor to be implemented for all views.
	 *
	 * @param context
	 *            The Context the view is running in, through which it can access the current theme, resources, etc.
	 * @param attrs
	 *            The attributes of the XML tag that is inflating the view.
	 * @param defStyle
	 *            An attribute in the current theme that contains a reference to a style resource that supplies default
	 *            values for the view. Can be 0 to not look for defaults.
	 * @see android.view.View#View(Context, AttributeSet, int)
	 */
	public OverlayPinchImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * Get the EyePhoto displayed in the view.
	 *
	 * @return the EyePhoto
	 */
	public final EyePhoto getEyePhoto() {
		return mEyePhoto;
	}

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
		mRetainFragment = retainFragment;
		mBitmap = retainFragment.getBitmap();
		mBitmapSmall = retainFragment.getBitmapSmall();
		mBitmapFull = retainFragment.getBitmapFullResolution();
		mPartialBitmapFullResolution = null;

		if (mBitmap == null || !pathName.equals(mPathName)) {
			mHasOverlayPosition = false;
			mPathName = pathName;
			mBitmap = null;

			// Do image loading in separate thread
			Thread thread = new Thread() {
				@Override
				public void run() {
					mBitmap = mEyePhoto.getImageBitmap(mMaxBitmapSize);
					mBitmapSmall = mEyePhoto.getImageBitmap(MediaStoreUtil.MINI_THUMB_SIZE);
					mMetadata = mEyePhoto.getImageMetadata();
					retainFragment.setBitmap(mBitmap);
					retainFragment.setBitmapSmall(mBitmapSmall);
					mIsBitmapSet = true;

					post(new Runnable() {
						@Override
						public void run() {
							if (mMetadata != null && mMetadata.hasOverlayPosition()) {
								// stored position of overlay
								mHasOverlayPosition = true;
								mOverlayX = mMetadata.getXCenter();
								mOverlayY = mMetadata.getYCenter();
								mOverlayScaleFactor = mMetadata.getOverlayScaleFactor()
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
								mBrightness = mMetadata.getBrightness().floatValue();
								mContrast = mMetadata.getContrast().floatValue();
								if (mGuiElementUpdater != null) {
									mGuiElementUpdater.updateSeekbarBrightness(mBrightness);
									mGuiElementUpdater
											.updateSeekbarContrast(storedContrastToSeekbarContrast(mContrast));
								}
							}
							if (mMetadata != null && mMetadata.getOverlayColor() != null) {
								mOverlayColor = mMetadata.getOverlayColor().intValue();
								mGuiElementUpdater.updateOverlayColorButton(mOverlayColor);
							}

							mLastOverlayScaleFactor = mOverlayScaleFactor;

							mCanvasBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(),
									Bitmap.Config.ARGB_8888);
							mCanvas = new Canvas(mCanvasBitmap);
							doInitialScaling();
							updateScaleGestureDetector();

							refresh(HIGH);
							showFullResolutionSnapshot(true);
						}
					});
				}
			};
			thread.start();
		}
		else {
			// orientation change
			mIsBitmapSet = true;
			mCanvasBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
			mCanvas = new Canvas(mCanvasBitmap);
			doInitialScaling();
			updateScaleGestureDetector();
			refresh(HIGH);
			showFullResolutionSnapshot(true);
		}

	}

	@Override
	protected final void doInitialScaling() {
		// If available, use stored position
		if (!mInitialized && mHasViewPosition) {
			mPosX = mMetadata.getXPosition();
			mPosY = mMetadata.getYPosition();
			mScaleFactor = mMetadata.getZoomFactor() * getOrientationIndependentScaleFactor();
			mLastScaleFactor = mScaleFactor;
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
			mLastScaleFactor = mScaleFactor;
		}
		// Otherwise, use default (set if mInitialized = false)
		super.doInitialScaling();
	}

	/**
	 * Update the bitmap with the correct set of overlays.
	 *
	 * @param resolution
	 *            indicates what resolution is required
	 */
	private void refresh(final Resolution resolution) {
		if (mCanvas == null || !mInitialized) {
			return;
		}
		if (resolution == FULL) {
			showFullResolutionSnapshot(false);
			return;
		}

		// Determine overlays to be shown
		List<Integer> overlayPositions = getOverlayPositions();

		Drawable[] layers = new Drawable[overlayPositions.size() + 1];
		Bitmap modBitmap;

		if (resolution == LOW) {
			// for performance reasons, use only low resolution bitmap while pinching
			modBitmap = changeBitmapContrastBrightness(mBitmapSmall, mContrast, mBrightness);
		}
		else {
			modBitmap = changeBitmapContrastBrightness(mBitmap, mContrast, mBrightness);
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

		mRequiresBrightnessContrastUpdate = false;
	}

	/**
	 * Refresh with high resolution (or full resolution if applicable).
	 */
	public final void refresh() {
		refresh(mPartialBitmapFullResolution == null ? HIGH : FULL);
	}

	/**
	 * Get the list of currently displayed overlay indices.
	 *
	 * @return The current overlay indices.
	 */
	private List<Integer> getOverlayPositions() {
		ArrayList<Integer> overlayPositions = new ArrayList<Integer>();

		if (canHandleOverlays()) {
			for (int i = 0; i < mShowOverlay.length; i++) {
				if (mShowOverlay[i]) {
					overlayPositions.add(i);
				}
			}
		}

		return overlayPositions;
	}

	/**
	 * Get information if an overlay is shown.
	 *
	 * @return true if an overlay is snown.
	 */
	private boolean hasOverlay() {
		return getOverlayPositions().size() > 0;
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
		refresh(HIGH);
		updateScaleGestureDetector();

		if (hasOverlay()) {
			showNormalResolution();
		}
		else {
			showFullResolutionSnapshot(true);
		}
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
				if (mMetadata.getRightLeft() == null) {
					// If image did not yet pass metadata setting, do it now.
					mEyePhoto.updateMetadataWithDefaults(mMetadata);
				}

				mMetadata.setXCenter(mOverlayX);
				mMetadata.setYCenter(mOverlayY);
				mMetadata.setOverlayScaleFactor(mOverlayScaleFactor / Math.max(mBitmap.getWidth(), mBitmap.getHeight()) * OVERLAY_SIZE);
				mEyePhoto.storeImageMetadata(mMetadata);

				PreferenceUtil.incrementCounter(R.string.key_statistics_countlock);
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
				mMetadata.setXCenter((Float) null);
				mMetadata.setYCenter((Float) null);
				mMetadata.setOverlayScaleFactor((Float) null);
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
			int[] overlayTypes = getResources().getIntArray(R.array.overlay_types);

			TypedArray overlaysLeft = getResources().obtainTypedArray(R.array.overlays_left);
			TypedArray overlaysRight = getResources().obtainTypedArray(R.array.overlays_right);

			if (position < overlayTypes.length) {
				String origPupilSizeString = getResources().getStringArray(R.array.overlay_pupil_sizes)[position];
				float origPupilSize = Float.parseFloat(origPupilSizeString);

				// TODO: take from metadata
				String destPupilSizeString = getResources().getString(R.string.overlay_default_pupil_size);
				float destPupilSize = Float.parseFloat(destPupilSizeString);

				Drawable drawable;
				if (mEyePhoto.getRightLeft().equals(RightLeft.RIGHT)) {
					drawable = overlaysRight.getDrawable(position);
				}
				else {
					drawable = overlaysLeft.getDrawable(position);
				}
				if (overlayTypes[position] == 1) {
					mOverlayCache[position] = getModifiedDrawable(drawable, mOverlayColor, origPupilSize, destPupilSize, 0, 0);
				}
				else {
					mOverlayCache[position] = getModifiedDrawable(drawable, -1, origPupilSize, destPupilSize, 0, 0);
				}
			}
			overlaysLeft.recycle();
			overlaysRight.recycle();
		}

		return mOverlayCache[position];
	}

	/**
	 * Create a drawable from a black image drawable, having a changed colour.
	 *
	 * @param sourceDrawable
	 *            The black image drawable
	 * @param color
	 *            The target color
	 * @param origPupilSize
	 *            The pupil size (relative to iris) in the original overlay bitmap.
	 * @param destPupilSize
	 *            The pupil size (relative to iris) in the target overlay bitmap.
	 * @param pupilOffsetX
	 *            The relative x offset of the pupil center
	 * @param pupilOffsetY
	 *            The relative y offset of the pupil center
	 * @return The modified drawable, with the intended color.
	 */
	private Drawable getModifiedDrawable(final Drawable sourceDrawable, final int color, final float origPupilSize, final float destPupilSize,
			final float pupilOffsetX, final float pupilOffsetY) {
		Bitmap bitmap = ((BitmapDrawable) sourceDrawable).getBitmap();
		Bitmap colouredBitmap = color == -1 ? bitmap : ImageUtil.changeBitmapColor(bitmap, color);
		Bitmap deformedBitmap = ImageUtil.deformOverlayByPupilSize(colouredBitmap, origPupilSize, destPupilSize, pupilOffsetX, pupilOffsetY);
		return new BitmapDrawable(getResources(), deformedBitmap);
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
		mRequiresBrightnessContrastUpdate = true;
		refresh(mPartialBitmapFullResolution == null ? LOW : FULL);
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
		mRequiresBrightnessContrastUpdate = true;
		refresh(mPartialBitmapFullResolution == null ? LOW : FULL);
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
		refresh(HIGH);
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
				mMetadata.setBrightness((Float) null);
				mMetadata.setContrast((Float) null);
				mBrightness = 0;
				mContrast = 1;
				if (mGuiElementUpdater != null) {
					mGuiElementUpdater.updateSeekbarBrightness(mBrightness);
					mGuiElementUpdater.updateSeekbarContrast(storedContrastToSeekbarContrast(mContrast));
				}
			}
			else {
				mMetadata.setBrightness(mBrightness);
				mMetadata.setContrast(mContrast);
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
				mMetadata.setXPosition((Float) null);
				mMetadata.setYPosition((Float) null);
				mMetadata.setZoomFactor((Float) null);

				// Reset to original view size
				mInitialized = false;
				doInitialScaling();
			}
			else {
				mHasViewPosition = true;
				mMetadata.setXPosition(mPosX);
				mMetadata.setYPosition(mPosY);
				mMetadata.setZoomFactor(mScaleFactor / getOrientationIndependentScaleFactor());
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
				mMetadata.setOverlayColor((Integer) null);
				setOverlayColor(mGuiElementUpdater.getOverlayDefaultColor());
			}
			else {
				mMetadata.setOverlayColor(mOverlayColor);
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
		showNormalResolution();

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

		refresh(LOW);
		return moved;
	}

	/*
	 * Overridden to refresh the view in full details.
	 */
	@Override
	protected final void startPointerMove(final MotionEvent ev) {
		showNormalResolution();
	}

	/*
	 * Overridden to refresh the view in full details.
	 */
	@Override
	protected final void finishPointerMove(final MotionEvent ev) {
		refresh(HIGH);
		showFullResolutionSnapshot(true);
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
	private static Bitmap changeBitmapContrastBrightness(final Bitmap bmp, final float contrast, final float brightness) {
		if (contrast == 1 && brightness == 0) {
			return bmp;
		}

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
	 * Create a bitmap containing the current view in full resolution (incl. brightness/contrast).
	 *
	 * @return The bitmap in full resolution.
	 */
	private Bitmap createFullResolutionBitmap() {
		if (mBitmap == null) {
			return null;
		}

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
			return null;
		}

		// The distance of the displayed image from the view borders.
		int offsetX = Math.round(-Math.min(0, leftX) * mScaleFactor);
		int offsetY = Math.round(-Math.min(0, upperY) * mScaleFactor);
		int offsetMaxX = Math.round(Math.max(rightX - mBitmap.getWidth(), 0) * mScaleFactor);
		int offsetMaxY = Math.round(Math.max(lowerY - mBitmap.getHeight(), 0) * mScaleFactor);

		try {
			Bitmap bitmapFull = mBitmapFull;
			if (bitmapFull == null) {
				bitmapFull = mEyePhoto.getFullBitmap();
				if (mFullResolutionFlag) {
					mBitmapFull = bitmapFull;
					if (mRetainFragment != null) {
						mRetainFragment.setBitmapFullResolution(bitmapFull);
					}
				}
			}
			Bitmap partialBitmap =
					ImageUtil.getPartialBitmap(bitmapFull, minX, maxX, minY, maxY);
			Bitmap scaledPartialBitmap =
					Bitmap.createScaledBitmap(partialBitmap, getWidth() - offsetMaxX - offsetX, getHeight()
							- offsetMaxY
							- offsetY, false);

			Bitmap bitmapFullResolution = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmapFullResolution);
			canvas.drawBitmap(scaledPartialBitmap, offsetX, offsetY, null);

			return bitmapFullResolution;
		}
		catch (Exception e) {
			// NullPointerExceptions might occur in parallel scenarios.
			return null;
		}
	}

	/**
	 * Tell the view if it should automatically display in full resolution.
	 *
	 * @param fullResolutionFlag
	 *            if true, view shows automatically in full resolution.
	 */
	public final void allowFullResolution(final boolean fullResolutionFlag) {
		mFullResolutionFlag = fullResolutionFlag;
	}

	/**
	 * Show the current view in full resolution.
	 *
	 * @param async
	 *            A flag indicating if the bitmap creation should happen in a separate thread.
	 */
	public final void showFullResolutionSnapshot(final boolean async) {
		if (async && hasOverlay()) {
			// Do not trigger full resolution thread if there is an overlay.
			return;
		}
		if (async && !mFullResolutionFlag) {
			// Do not trigger full resolution thread if flag is configured for manual handling of full resolution.
			return;
		}

		Thread fullResolutionThread = new Thread() {
			@Override
			public final void run() {
				if (mPartialBitmapFullResolution == null) {
					try {
						mPartialBitmapFullResolution = createFullResolutionBitmap();
					}
					catch (OutOfMemoryError e) {
						Log.e(Application.TAG, "Out of memory while creating full resolution bitmap", e);
						mPartialBitmapFullResolution = null;
					}

					if (mPartialBitmapFullResolution == null) {
						return;
					}
				}

				final Bitmap partialBitmapWithBrightness =
						changeBitmapContrastBrightness(mPartialBitmapFullResolution, mContrast, mBrightness);

				if (isInterrupted()) {
					// Do not display the result if the thread has been interrupted.
					mPartialBitmapFullResolution = null;
				}
				else {
					// Make a straight display of this bitmap without any matrix transformation.
					// Will be reset by regular view as soon as the screen is touched again.
					post(new Runnable() {
						@Override
						public void run() {
							if (mPartialBitmapFullResolution != null) {
								setImageBitmap(partialBitmapWithBrightness);
								setImageMatrix(null);
							}
						}

					});
				}

				if (async) {
					// start next thread in queue
					synchronized (mFullResolutionThreads) {
						mFullResolutionThreads.remove(Thread.currentThread());
						if (mFullResolutionThreads.size() > 0) {
							mFullResolutionThreads.get(0).start();
						}
					}
				}
			}
		};

		if (async) {
			synchronized (mFullResolutionThreads) {
				if (mFullResolutionThreads.size() > 1) {
					// at most two threads in list
					mFullResolutionThreads.remove(1);
				}
				mFullResolutionThreads.add(fullResolutionThread);
				if (mFullResolutionThreads.size() == 1) {
					// only start if no thread is running
					fullResolutionThread.start();
				}
			}
		}
		else {
			try {
				fullResolutionThread.start();
				fullResolutionThread.join();
			}
			catch (InterruptedException e) {
				// do nothing
			}
		}

	}

	/**
	 * Interrupt the full resolution shapshot creation, if in process.
	 */
	private void interruptFullResolutionThread() {
		synchronized (mFullResolutionThreads) {
			if (mFullResolutionThreads.size() > 0) {
				mFullResolutionThreads.get(0).interrupt();
				if (mFullResolutionThreads.size() > 1) {
					mFullResolutionThreads.remove(1);
				}
			}
		}
		mPartialBitmapFullResolution = null;
	}

	/**
	 * Show normal resolution again after having the full resolution snapshot.
	 */
	public final void showNormalResolution() {
		interruptFullResolutionThread();
		mPartialBitmapFullResolution = null;

		if (mRequiresBrightnessContrastUpdate) {
			refresh(HIGH);
		}
		else {
			setImageBitmap(mCanvasBitmap);
			setMatrix();
		}
	}

	@Override
	protected final void setMatrix() {
		if (mPartialBitmapFullResolution != null) {
			setImageMatrix(null);
		}
		else {
			super.setMatrix();
		}
	}

	/**
	 * Override requestLayout to show normal resolution.
	 */
	@Override
	public final void requestLayout() {
		if (mBitmap != null && (getWidth() != mLastWidth || getHeight() != mLastHeight)) {
			// if view size changed, then calculate full resolution image again
			showNormalResolution();
			showFullResolutionSnapshot(true);
		}
		super.requestLayout();
		mLastHeight = getHeight();
		mLastWidth = getWidth();
	}

	/**
	 * Store the comment in the image.
	 *
	 * @param comment
	 *            the comment to be stored.
	 */
	public final void storeComment(final String comment) {
		if (mInitialized && mMetadata != null) {
			mMetadata.setComment(comment);
			mEyePhoto.storeImageMetadata(mMetadata);

			PreferenceUtil.incrementCounter(R.string.key_statistics_countcomment);
		}
	}

	/**
	 * Remove cached full bitmap from memory.
	 */
	public final void cleanFullBitmap() {
		mBitmapFull = null;
		if (mRetainFragment != null) {
			mRetainFragment.setBitmapFullResolution(null);
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
	 * Set the reference that allows GUI updates.
	 *
	 * @param updater
	 *            The GUI Element updater
	 */
	public final void setGuiElementUpdater(final GuiElementUpdater updater) {
		mGuiElementUpdater = updater;
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
		private Bitmap mRetainbitmapSmall;

		private Bitmap getBitmapSmall() {
			return mRetainbitmapSmall;
		}

		private void setBitmapSmall(final Bitmap bitmapSmall) {
			this.mRetainbitmapSmall = bitmapSmall;
		}

		/**
		 * The full resolution bitmap.
		 */
		private Bitmap mRetainBitmapFullResolution;

		private Bitmap getBitmapFullResolution() {
			return mRetainBitmapFullResolution;
		}

		private void setBitmapFullResolution(final Bitmap bitmapFullResolution) {
			this.mRetainBitmapFullResolution = bitmapFullResolution;
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

	/**
	 * Enumeration giving the resolution with which the picture is displayed.
	 */
	public enum Resolution {
		/**
		 * The three resolution values. LOW corresponds to thumbnail resolution, while FULL applies to full resolution
		 * of the current part of the picture.
		 */
		LOW, HIGH, FULL;
	}

}
