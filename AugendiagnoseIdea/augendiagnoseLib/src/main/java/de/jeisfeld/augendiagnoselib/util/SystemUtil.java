package de.jeisfeld.augendiagnoselib.util;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.util.Locale;

import de.jeisfeld.augendiagnoselib.Application;

/**
 * Utility class for getting system information.
 */
public final class SystemUtil {
	/**
	 * Hide default constructor.
	 */
	private SystemUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get information if Android version is Kitkat (4.4).
	 *
	 * @return true if Kitkat.
	 */
	public static boolean isKitkat() {
		return Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT;
	}

	/**
	 * Get information if Android version is Lollipop (5.0) or higher.
	 *
	 * @return true if Lollipop or higher.
	 */
	public static boolean isAndroid5() {
		return isAtLeastVersion(Build.VERSION_CODES.LOLLIPOP);
	}

	/**
	 * Check if Android version is at least the given version.
	 *
	 * @param version The version
	 * @return true if Android version is at least the given version
	 */
	public static boolean isAtLeastVersion(final int version) {
		return Build.VERSION.SDK_INT >= version;
	}

	/**
	 * Determine if the device has a camera activity.
	 *
	 * @return true if the device has a camera activity.
	 */
	public static boolean hasCameraActivity() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		return takePictureIntent.resolveActivity(Application.getAppContext().getPackageManager()) != null;
	}

	/**
	 * Determine if the device has a camera.
	 *
	 * @return true if the device has a camera.
	 */
	public static boolean hasCamera() {
		PackageManager pm = Application.getAppContext().getPackageManager();

		return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

	/**
	 * Determine if the device has a flashlight.
	 *
	 * @return true if the device has a flashlight.
	 */
	public static boolean hasFlashlight() {
		PackageManager pm = Application.getAppContext().getPackageManager();

		return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
	}

	/**
	 * Determine if the device supports a manual sensor.
	 *
	 * @return true if the device supports a manual sensor.
	 */
	@SuppressLint("InlinedApi")
	public static boolean hasManualSensor() {
		PackageManager pm = Application.getAppContext().getPackageManager();

		return isAndroid5() && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_CAPABILITY_MANUAL_SENSOR);
	}

	/**
	 * Determine if an app is installed.
	 *
	 * @param appPackage the app package name.
	 * @return true if the app is installed.
	 */
	public static boolean isAppInstalled(final String appPackage) {
		Intent appIntent = Application.getAppContext().getPackageManager().getLaunchIntentForPackage(appPackage);
		return appIntent != null;
	}

	/**
	 * Determine if the screen is shown in landscape mode (i.e. width &gt; height)
	 *
	 * @return true if the app runs in landscape mode
	 */
	public static boolean isLandscape() {
		// use screen width as criterion rather than getRotation
		WindowManager wm = (WindowManager) Application.getAppContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		return width > height;
	}

	/**
	 * Determine if the device is a tablet (i.e. it has a large screen).
	 *
	 * @return true if the app is running on a tablet.
	 */
	public static boolean isTablet() {
		return (Application.getAppContext().getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	/**
	 * Retrieve the default display.
	 *
	 * @return the default display.
	 */
	private static Display getDefaultDisplay() {
		WindowManager wm = (WindowManager) Application.getAppContext().getSystemService(Context.WINDOW_SERVICE);
		return wm.getDefaultDisplay();
	}

	/**
	 * Retrieve the display size in pixels (max of x and y value).
	 *
	 * @return the display size.
	 */
	public static int getDisplaySize() {
		Point p = new Point();
		getDefaultDisplay().getSize(p);
		return Math.max(p.x, p.y);
	}

	/**
	 * Retrieve the display size in inches (min of x and y value).
	 *
	 * @return the display size.
	 */
	public static double getPhysicalDisplaySize() {
		DisplayMetrics dm = new DisplayMetrics();
		getDefaultDisplay().getMetrics(dm);
		Point p = new Point();
		getDefaultDisplay().getSize(p);
		double x = p.x / dm.xdpi;
		double y = p.y / dm.ydpi;
		return Math.max(x, y);
	}

	/**
	 * Get ISO 3166-1 alpha-2 country code for this device (or null if not available).
	 *
	 * @return country code or null
	 */
	@Nullable
	public static String getUserCountry() {
		Context context = Application.getAppContext();
		String locale = null;

		final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		final String simCountry = tm.getSimCountryIso();
		if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
			locale = simCountry.toUpperCase(Locale.getDefault());
		}
		else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
			String networkCountry = tm.getNetworkCountryIso();
			if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
				locale = networkCountry.toLowerCase(Locale.getDefault());
			}
		}

		if (locale == null || locale.length() != 2) {
			locale = context.getResources().getConfiguration().locale.getCountry();
		}

		return locale;
	}

	/**
	 * Get the large memory class of the device.
	 *
	 * @return the memory class - the maximal available memory for the app (in MB).
	 */
	public static int getLargeMemoryClass() {
		ActivityManager manager =
				(ActivityManager) Application.getAppContext().getSystemService(Context.ACTIVITY_SERVICE);

		return manager.getLargeMemoryClass();
	}
}
