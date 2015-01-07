package de.eisfeldj.augendiagnose;

import de.eisfeldj.augendiagnose.util.EncryptionUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * Utility class to retrieve base application resources.
 */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS",
		justification = "Intentionally using same name as superclass")
public class Application extends android.app.Application {
	/**
	 * A utility field to store a context statically.
	 */
	private static Context context;
	/**
	 * The default tag for logging.
	 */
	public static final String TAG = "Application";

	@Override
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
			justification = "Make some context visible statically (no matter which one)")
	public final void onCreate() {
		super.onCreate();
		Application.context = getApplicationContext();
	}

	/**
	 * Retrieve the application context.
	 *
	 * @return The (statically stored) application context
	 */
	public static Context getAppContext() {
		return Application.context;
	}

	/**
	 * Retrieve the default display.
	 *
	 * @return the default display.
	 */
	private static Display getDefaultDisplay() {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
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
	 * Retrieve the default shared preferences of the application.
	 *
	 * @return the default shared preferences.
	 */
	public static SharedPreferences getSharedPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	/**
	 * Retrieve a String shared preference.
	 *
	 * @param preferenceId
	 *            the id of the shared preference.
	 * @return the corresponding preference value.
	 */
	public static String getSharedPreferenceString(final int preferenceId) {
		return getSharedPreferences().getString(context.getString(preferenceId), "");
	}

	/**
	 * Set a String shared preference.
	 *
	 * @param preferenceId
	 *            the id of the shared preference.
	 * @param s
	 *            the target value of the preference.
	 */
	public static void setSharedPreferenceString(final int preferenceId, final String s) {
		Editor editor = getSharedPreferences().edit();
		editor.putString(context.getString(preferenceId), s);
		editor.commit();
	}

	/**
	 * Retrieve a boolean shared preference.
	 *
	 * @param preferenceId
	 *            the id of the shared preference.
	 * @return the corresponding preference value.
	 */
	public static boolean getSharedPreferenceBoolean(final int preferenceId) {
		return getSharedPreferences().getBoolean(context.getString(preferenceId), false);
	}

	/**
	 * Set a Boolean shared preference.
	 *
	 * @param preferenceId
	 *            the id of the shared preference.
	 * @param b
	 *            the target value of the preference.
	 */
	public static void setSharedPreferenceBoolean(final int preferenceId, final boolean b) {
		Editor editor = getSharedPreferences().edit();
		editor.putBoolean(context.getString(preferenceId), b);
		editor.commit();
	}

	/**
	 * Retrieve an integer shared preference.
	 *
	 * @param preferenceId
	 *            the id of the shared preference.
	 * @param defaultValue
	 *            the default value of the shared preference.
	 * @return the corresponding preference value.
	 */
	public static int getSharedPreferenceInt(final int preferenceId, final int defaultValue) {
		return getSharedPreferences().getInt(context.getString(preferenceId), defaultValue);
	}

	/**
	 * Set an integer shared preference.
	 *
	 * @param preferenceId
	 *            the id of the shared preference.
	 * @param i
	 *            the target value of the preference.
	 */
	public static void setSharedPreferenceInt(final int preferenceId, final int i) {
		Editor editor = getSharedPreferences().edit();
		editor.putInt(context.getString(preferenceId), i);
		editor.commit();
	}

	/**
	 * Check if the application has an authorized user key.
	 *
	 * @return true if the application has an authorized user key.
	 */
	public static boolean isAuthorized() {
		String userKey = getSharedPreferenceString(R.string.key_user_key);
		return EncryptionUtil.validateUserKey(userKey);
	}

	/**
	 * Get a resource string.
	 *
	 * @param resourceId
	 *            the id of the resource.
	 * @return the value of the String resource.
	 */
	public static String getResourceString(final int resourceId) {
		return getAppContext().getResources().getString(resourceId);
	}

	/**
	 * Retrieve the version number of the app.
	 *
	 * @return the app version.
	 */
	public static int getVersion() {
		PackageInfo pInfo;
		try {
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pInfo.versionCode;
		}
		catch (NameNotFoundException e) {
			Log.e(TAG, "Did not find application version", e);
			return 0;
		}
	}

	/**
	 * Determine if the device is a tablet (i.e. it has a large screen).
	 *
	 * @return true if the app is running on a tablet.
	 */
	public static boolean isTablet() {
		return (getAppContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
		>= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	/**
	 * Determine if the screen is shown in landscape mode (i.e. width > height)
	 *
	 * @return true if the app runs in landscape mode
	 */
	public static boolean isLandscape() {
		// use screen width as criterion rather than getRotation
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		return width > height;
	}

	/**
	 * Determine if Eye-Fi is installed.
	 *
	 * @return true if Eye-Fi is installed.
	 */
	public static boolean isEyeFiInstalled() {
		Intent eyeFiIntent = getAppContext().getPackageManager().getLaunchIntentForPackage("fi.eye.android");
		return eyeFiIntent != null;
	}
}
