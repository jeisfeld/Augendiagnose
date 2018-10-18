package de.jeisfeld.augendiagnoselib;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Locale;

import de.jeisfeld.augendiagnoselib.activities.SettingsActivity;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Utility class to retrieve base application resources.
 */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS",
		justification = "Intentionally using same name as superclass")
public class Application extends android.app.Application {
	/**
	 * The private constants of this app.
	 */
	private static ApplicationSettings mApplicationSettings;
	/**
	 * An instance of the application context.
	 */
	private static Context mContext;

	/**
	 * The default tag for logging.
	 */
	public static final String TAG = "Augendiagnose.JE";

	/**
	 * The system default locale.
	 */
	private static final Locale DEFAULT_LOCALE = Locale.getDefault();

	@Nullable
	private static ApplicationSettings getApplicationSettings() {
		return mApplicationSettings;
	}

	// OVERRIDABLE
	@Override
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
			justification = "Make some context visible statically (no matter which one)")
	public void onCreate() {
		super.onCreate();
		Application.mContext = this.getApplicationContext();

		SettingsActivity.setDefaultSharedPreferences(getApplicationContext());

		setLanguage();
		setExceptionHandler();

		// Initialize special classes
		try {
			Application.mApplicationSettings =
					(ApplicationSettings) Class.forName(getResourceString(R.string.class_application_settings))
							.getDeclaredMethod("getInstance", new Class<?>[0])
							.invoke(null);
		}
		catch (Exception e) {
			Log.e(TAG, "Error in getting PrivateConstants and ApplicationSettings", e);
		}

		// Set statistics
		int initialVersion = PreferenceUtil.getSharedPreferenceInt(R.string.key_statistics_initialversion, -1);
		if (initialVersion == -1) {
			PreferenceUtil.setSharedPreferenceInt(R.string.key_statistics_initialversion, getVersion());
		}

		long firstStartTime = PreferenceUtil.getSharedPreferenceLong(R.string.key_statistics_firststarttime, -1);
		if (firstStartTime == -1) {
			PreferenceUtil.setSharedPreferenceLong(R.string.key_statistics_firststarttime, System.currentTimeMillis());
		}

		PreferenceUtil.incrementCounter(R.string.key_statistics_countstarts);
		PreferenceUtil.sendStatistics();
	}

	/**
	 * Define custom ExceptionHandler which takes action on OutOfMemoryError.
	 */
	private void setExceptionHandler() {
		final UncaughtExceptionHandler defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

		UncaughtExceptionHandler customExceptionHandler =
				new UncaughtExceptionHandler() {
					@Override
					public void uncaughtException(final Thread thread, final Throwable ex) {
						if (ex instanceof OutOfMemoryError) {
							// Store info about OutOfMemoryError
							PreferenceUtil.setSharedPreferenceBoolean(R.string.key_internal_outofmemoryerror, true);
						}

						// re-throw critical exception further to the os
						defaultExceptionHandler.uncaughtException(thread, ex);
					}
				};

		Thread.setDefaultUncaughtExceptionHandler(customExceptionHandler);
	}

	/**
	 * Retrieve the application context.
	 *
	 * @return The (statically stored) application context
	 */
	public static Context getAppContext() {
		return Application.mContext;
	}

	/**
	 * Check if the application has an authorized user key.
	 *
	 * @return true if the application has an authorized user key.
	 */
	public static AuthorizationLevel getAuthorizationLevel() {
		return getApplicationSettings().getAuthorizationLevel();
	}

	/**
	 * start the application.
	 *
	 * @param triggeringActivity the triggering activity.
	 */
	public static void startApplication(final Activity triggeringActivity) {
		getApplicationSettings().startApplication(triggeringActivity);
	}

	/**
	 * Get a resource string.
	 *
	 * @param resourceId the id of the resource.
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
			pInfo = getAppContext().getPackageManager().getPackageInfo(getAppContext().getPackageName(), 0);
			return pInfo.versionCode;
		}
		catch (NameNotFoundException e) {
			Log.e(TAG, "Did not find application version", e);
			return 0;
		}
	}

	/**
	 * Retrieve the version String of the app.
	 *
	 * @return the app version String.
	 */
	public static String getVersionString() {
		PackageInfo pInfo;
		try {
			pInfo = getAppContext().getPackageManager().getPackageInfo(getAppContext().getPackageName(), 0);
			return pInfo.versionName;
		}
		catch (NameNotFoundException e) {
			Log.e(TAG, "Did not find application version", e);
			return null;
		}
	}

	/**
	 * Set the language.
	 */
	public static void setLanguage() {
		String languageString = PreferenceUtil.getSharedPreferenceString(R.string.key_language);
		if (languageString == null || languageString.length() == 0) {
			languageString = "0";
			PreferenceUtil.setSharedPreferenceString(R.string.key_language, "0");
		}

		int languageSetting = Integer.parseInt(languageString);
		if (languageSetting == 0 && VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
			return;
		}

		switch (languageSetting) {
		case 0:
			setLocale(DEFAULT_LOCALE);
			break;
		case 1:
			setLocale(Locale.ENGLISH);
			break;
		case 2:
			setLocale(Locale.GERMAN);
			break;
		case 3: // MAGIC_NUMBER
			setLocale(new Locale("es"));
			break;
		case 4: // MAGIC_NUMBER
			setLocale(new Locale("pt"));
			break;
		default:
		}
	}

	/**
	 * Set the locale.
	 *
	 * @param locale The locale to be set.
	 */
	private static void setLocale(final Locale locale) {
		if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
			setLocale17(locale);
		}
		else {
			setLocale16(locale);
		}
	}

	/**
	 * Set the locale for Android version above Jelly Bean.
	 *
	 * @param locale The locale to be set.
	 */
	@RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR1)
	private static void setLocale17(final Locale locale) {
		Resources res = getAppContext().getResources();
		Configuration conf = res.getConfiguration();
		conf.setLocale(locale);
		res.updateConfiguration(conf, res.getDisplayMetrics());
	}

	/**
	 * Set the locale for Android version below Jelly Bean.
	 *
	 * @param locale The locale to be set.
	 */
	@SuppressWarnings("deprecation")
	private static void setLocale16(final Locale locale) {
		Resources res = getAppContext().getResources();
		Configuration conf = res.getConfiguration();
		conf.locale = locale;
		res.updateConfiguration(conf, res.getDisplayMetrics());
	}

	/**
	 * The level of authorization of the class.
	 */
	public enum AuthorizationLevel {
		/**
		 * Only trial access.
		 */
		TRIAL_ACCESS,
		/**
		 * Full usage, but with ads.
		 */
		FULL_ACCESS_WITH_ADS,
		/**
		 * Full usage.
		 */
		FULL_ACCESS,
		/**
		 * No access.
		 */
		NO_ACCESS
	}
}
