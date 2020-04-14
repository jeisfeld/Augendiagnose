package de.jeisfeld.augendiagnoselib.util;

import android.content.Context;
import android.content.res.Configuration;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Helper class that allows to listen on orientation changes of the device.
 */
public class OrientationManager extends OrientationEventListener {

	/**
	 * Enumeration for the possible screen orientations.
	 */
	public enum ScreenOrientation {
		/**
		 * Reversed landscape.
		 */
		REVERSED_LANDSCAPE,
		/**
		 * Landscape.
		 */
		LANDSCAPE,
		/**
		 * Portrait.
		 */
		PORTRAIT,
		/**
		 * Reversed portrait.
		 */
		REVERSED_PORTRAIT
	}

	/**
	 * The current screen orientation.
	 */
	private ScreenOrientation mScreenOrientation;
	/**
	 * The listener reacting on changes of screen orientation.
	 */
	private OrientationListener mListener;
	/**
	 * The context in which this is created.
	 */
	private Context mContext;

	/**
	 * Constructor for the orientation manager.
	 *
	 * @param context The context/activity starting the orientation manager.
	 * @param rate The rate at which the sensor should be checked.
	 * @param listener The listener called in case of orientation change.
	 */
	public OrientationManager(final Context context, final int rate, final OrientationListener listener) {
		super(context, rate);
		mListener = listener;
		mContext = context;
	}

	/**
	 * Constructor for the orientation manager.
	 *
	 * @param context The context/activity starting the orientation manager.
	 * @param rate    The rate at which the sensor should be checked.
	 */
	public OrientationManager(final Context context, final int rate) {
		super(context, rate);
	}

	/**
	 * Constructor for the orientation manager.
	 *
	 * @param context The context/activity starting the orientation manager.
	 */
	public OrientationManager(final Context context) {
		super(context);
	}

	@Override
	public final void onOrientationChanged(final int orientation) {
		if (orientation == -1) {
			return;
		}
		int adjustedOrientation = orientation;
		if (getDeviceDefaultOrientation(mContext) == Configuration.ORIENTATION_LANDSCAPE) {
			adjustedOrientation = (orientation + 270) % 360; // MAGIC_NUMBER
		}
		ScreenOrientation newScreenOrientation;
		if (adjustedOrientation >= 45 && adjustedOrientation < 135) { // MAGIC_NUMBER
			newScreenOrientation = ScreenOrientation.REVERSED_LANDSCAPE;
		}
		else if (adjustedOrientation >= 135 && adjustedOrientation < 225) { // MAGIC_NUMBER
			newScreenOrientation = ScreenOrientation.REVERSED_PORTRAIT;
		}
		else if (adjustedOrientation >= 225 && adjustedOrientation < 315) { // MAGIC_NUMBER
			newScreenOrientation = ScreenOrientation.LANDSCAPE;
		}
		else {
			newScreenOrientation = ScreenOrientation.PORTRAIT;
		}
		if (newScreenOrientation != mScreenOrientation) {
			mScreenOrientation = newScreenOrientation;
			if (mListener != null) {
				mListener.onOrientationChange(mScreenOrientation);
			}
		}
	}

	/**
	 * Get the screen orientation.
	 *
	 * @return The screen orientation.
	 */
	public final ScreenOrientation getScreenOrientation() {
		return mScreenOrientation;
	}

	/**
	 * Get the default orientation of the device.
	 *
	 * @param context the context.
	 * @return The default orientation of the device.
	 */
	private static int getDeviceDefaultOrientation(final Context context) {
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Configuration config = context.getResources().getConfiguration();
		int rotation = windowManager.getDefaultDisplay().getRotation();

		if ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)
				&& config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			return Configuration.ORIENTATION_LANDSCAPE;
		}
		else if ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270)
				&& config.orientation == Configuration.ORIENTATION_PORTRAIT) {
			return Configuration.ORIENTATION_LANDSCAPE;
		}
		else {
			return Configuration.ORIENTATION_PORTRAIT;
		}
	}

	/**
	 * Interface for a listener waiting for orientation changes.
	 */
	public interface OrientationListener {
		/**
		 * Callback method for orientation change.
		 *
		 * @param screenOrientation The new screen orientation.
		 */
		void onOrientationChange(ScreenOrientation screenOrientation);
	}
}
