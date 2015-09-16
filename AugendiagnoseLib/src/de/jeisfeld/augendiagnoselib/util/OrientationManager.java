package de.jeisfeld.augendiagnoselib.util;

import android.content.Context;
import android.content.res.Configuration;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;
import de.jeisfeld.augendiagnoselib.Application;

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
	private ScreenOrientation screenOrientation;
	/**
	 * The listener reacting on changes of screen orientation.
	 */
	private OrientationListener listener;

	/**
	 * Constructor for the orientation manager.
	 *
	 * @param context
	 *            The context/activity starting the orientation manager.
	 * @param rate
	 *            The rate at which the sensor should be checked.
	 * @param listener
	 *            The listener called in case of orientation change.
	 */
	public OrientationManager(final Context context, final int rate, final OrientationListener listener) {
		super(context, rate);
		setListener(listener);
	}

	public OrientationManager(final Context context, final int rate) {
		super(context, rate);
	}

	public OrientationManager(final Context context) {
		super(context);
	}

	@Override
	public final void onOrientationChanged(final int orientation) {
		if (orientation == -1) {
			return;
		}

		int adjustedOrientation = orientation;
		if (getDeviceDefaultOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
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
		if (newScreenOrientation != screenOrientation) {
			screenOrientation = newScreenOrientation;
			if (listener != null) {
				listener.onOrientationChange(screenOrientation);
			}
		}
	}

	public final void setListener(final OrientationListener listener) {
		this.listener = listener;
	}

	public final ScreenOrientation getScreenOrientation() {
		return screenOrientation;
	}

	/**
	 * Get the default orientation of the device.
	 *
	 * @return The default orientation of the device.
	 */
	public static int getDeviceDefaultOrientation() {
		WindowManager windowManager = (WindowManager) Application.getAppContext().getSystemService(Context.WINDOW_SERVICE);
		Configuration config = Application.getAppContext().getResources().getConfiguration();
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
		 * @param screenOrientation
		 *            The new screen orientation.
		 */
		void onOrientationChange(ScreenOrientation screenOrientation);
	}
}
