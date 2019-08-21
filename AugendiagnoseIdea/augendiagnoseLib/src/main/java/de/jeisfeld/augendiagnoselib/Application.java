package de.jeisfeld.augendiagnoselib;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.LocaleList;
import android.util.Log;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Locale;

import androidx.annotation.Nullable;
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
	 * The default locale.
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
		Application.mContext = this;
		Application.mContext = createContextWrapperForLocale(getApplicationContext());

		SettingsActivity.setDefaultSharedPreferences(getAppContext());
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
	 * Get the configured application locale.
	 *
	 * @return The configured application locale.
	 */
	private static Locale getApplicationLocale() {
		String languageString = PreferenceUtil.getSharedPreferenceString(R.string.key_language);
		if (languageString == null || languageString.length() == 0) {
			languageString = "0";
			PreferenceUtil.setSharedPreferenceString(R.string.key_language, "0");
		}

		int languageSetting = Integer.parseInt(languageString);
		switch (languageSetting) {
		case 0:
			return DEFAULT_LOCALE;
		case 1:
			return Locale.ENGLISH;
		case 2:
			return Locale.GERMAN;
		case 3: // MAGIC_NUMBER
			return new Locale("es");
		case 4: // MAGIC_NUMBER
			return new Locale("pt");
		case 5: // MAGIC_NUMBER
			return Locale.FRENCH;
		case 6: // MAGIC_NUMBER
			return new Locale("pl");
		default:
			return DEFAULT_LOCALE;
		}
	}

	/**
	 * Create a ContextWrapper, wrappint the context with a specific locale.
	 *
	 * @param context The original context.
	 * @return The context wrapper.
	 */
	public static ContextWrapper createContextWrapperForLocale(final Context context) {
		Resources res = context.getResources();
		Configuration configuration = res.getConfiguration();
		Locale newLocale = getApplicationLocale();
		Context newContext = context;

		if (VERSION.SDK_INT >= VERSION_CODES.N) {
			configuration.setLocale(newLocale);

			LocaleList localeList = new LocaleList(newLocale);
			LocaleList.setDefault(localeList);
			configuration.setLocales(localeList);

			newContext = context.createConfigurationContext(configuration);

		}
		else if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
			configuration.setLocale(newLocale);
			newContext = context.createConfigurationContext(configuration);

		}
		else {
			configuration.locale = newLocale;
			res.updateConfiguration(configuration, res.getDisplayMetrics());
		}
		return new ContextWrapper(newContext);
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
		 * Full usage.
		 */
		FULL_ACCESS,
		/**
		 * No access.
		 */
		NO_ACCESS
	}
}
