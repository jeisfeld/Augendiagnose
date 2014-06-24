package de.eisfeldj.augendiagnose;

import de.eisfeldj.augendiagnose.util.EncryptionUtil;
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
 * Utility class to retrieve base application resources
 */
public class Application extends android.app.Application {
	private static Context context;
	public static final String TAG = "Application";

	public void onCreate() {
		super.onCreate();
		Application.context = getApplicationContext();
	}

	/**
	 * Retrieve the application context
	 * 
	 * @return
	 */
	public static Context getAppContext() {
		return Application.context;
	}

	/**
	 * Retrieve the default display
	 * 
	 * @return
	 */
	public static Display getDefaultDisplay() {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		return wm.getDefaultDisplay();
	}

	/**
	 * Retrieve the max display size in pixels
	 * 
	 * @return
	 */
	public static int getDisplaySize() {
		Point p = new Point();
		getDefaultDisplay().getSize(p);
		return Math.max(p.x, p.y);
	}

	/**
	 * Retrieve the default shared preferences of the application
	 * 
	 * @return
	 */
	public static SharedPreferences getSharedPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	/**
	 * Retrieve a String shared preference
	 * 
	 * @param preferenceId
	 * @return
	 */
	public static String getSharedPreferenceString(int preferenceId) {
		return getSharedPreferences().getString(context.getString(preferenceId), "");
	}

	/**
	 * Set a String shared preference
	 * 
	 * @param preferenceId
	 * @param s
	 */
	public static void setSharedPreferenceString(int preferenceId, String s) {
		Editor editor = getSharedPreferences().edit();
		editor.putString(context.getString(preferenceId), s);
		editor.commit();
	}

	/**
	 * Retrieve a boolean shared preference
	 * 
	 * @param preferenceId
	 * @return
	 */
	public static boolean getSharedPreferenceBoolean(int preferenceId) {
		return getSharedPreferences().getBoolean(context.getString(preferenceId), false);
	}

	/**
	 * Set a Boolean shared preference
	 * 
	 * @param preferenceId
	 * @param b
	 */
	public static void setSharedPreferenceBoolean(int preferenceId, boolean b) {
		Editor editor = getSharedPreferences().edit();
		editor.putBoolean(context.getString(preferenceId), b);
		editor.commit();
	}

	/**
	 * Retrieve an integer shared preference
	 * 
	 * @param preferenceId
	 * @return
	 */
	public static int getSharedPreferenceInt(int preferenceId, int defaultValue) {
		return getSharedPreferences().getInt(context.getString(preferenceId), defaultValue);
	}

	/**
	 * Set an integer shared preference
	 * 
	 * @param preferenceId
	 * @param i
	 */
	public static void setSharedPreferenceInt(int preferenceId, int i) {
		Editor editor = getSharedPreferences().edit();
		editor.putInt(context.getString(preferenceId), i);
		editor.commit();
	}

	/**
	 * Check if the application has an authorized user key
	 * 
	 * @return
	 */
	public static boolean isAuthorized() {
		String userKey = getSharedPreferenceString(R.string.key_user_key);
		return EncryptionUtil.validateUserKey(userKey);
	}

	/**
	 * Get a resource string
	 * 
	 * @param resource
	 * @return
	 */
	public static String getResourceString(int resourceId) {
		return getAppContext().getResources().getString(resourceId);
	}

	/**
	 * Retrieve the version number of the app
	 * 
	 * @return
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
	 * @param context
	 *            The calling context.
	 */
	public static boolean isTablet() {
		return (getAppContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	/**
	 * Determine if the screen is shown in landscape mode (i.e. width > height)
	 * 
	 * @param context
	 *            The calling context.
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
	 * Determine if Eye-Fi is installed
	 * 
	 * @return
	 */
	public static boolean isEyeFiInstalled() {
		Intent eyeFiIntent = getAppContext().getPackageManager().getLaunchIntentForPackage("fi.eye.android");
		return eyeFiIntent != null;
	}
}
