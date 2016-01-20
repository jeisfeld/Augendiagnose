package de.jeisfeld.augendiagnoseunlocker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * Main activity of the application.
 */
public class DisplayMessageActivity extends Activity {
	/**
	 * The package name of the eye diagnosis app.
	 */
	private static final String PACKAGE_EYE_DIAGNOSIS = "de.eisfeldj.augendiagnose";
	/**
	 * The package name of the Miniris app.
	 */
	private static final String PACKAGE_MINIRIS = "de.jeisfeld.miniris";

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (isMainAppInstalled()) {
			displayUnlimitedUseMessage();
		}
		else {
			displayMissingMainAppMessage();
		}
	}

	/**
	 * Display the message that the main app is required.
	 */
	private void displayMissingMainAppMessage() {
		setContentView(R.layout.dialog_missing_main_app);

		findViewById(R.id.buttonEyeDiagnosis).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				openGooglePlay(PACKAGE_EYE_DIAGNOSIS);
				finish();
			}
		});

		findViewById(R.id.buttonMiniris).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				openGooglePlay(PACKAGE_MINIRIS);
				finish();
			}
		});
	}

	/**
	 * Display the message that the apps can be used unlimited now.
	 */
	private void displayUnlimitedUseMessage() {
		setContentView(R.layout.dialog_unlimited_use);

		findViewById(R.id.buttonOk).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				setEnabled(DisplayMessageActivity.this, false);
				finish();
			}
		});
	}

	/**
	 * Enable or disable this activity.
	 *
	 * @param context The calling context.
	 * @param enabled true for enabling, false for disabling.
	 */
	private static void setEnabled(final Context context, final boolean enabled) {
		ComponentName componentName = new ComponentName(context, DisplayMessageActivity.class);
		context.getPackageManager().setComponentEnabledSetting(componentName,
				enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);
	}

	/**
	 * Get information if this activity is enabled.
	 *
	 * @param context The calling context.
	 * @return true if enabled.
	 */
	protected static boolean isEnabled(final Context context) {
		ComponentName componentName = new ComponentName(context, DisplayMessageActivity.class);
		return context.getPackageManager().getComponentEnabledSetting(componentName) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
	}

	/**
	 * Open Google Play to display a certain app.
	 *
	 * @param appPackage The package of the app to be displayed.
	 */
	private void openGooglePlay(final String appPackage) {
		Intent googlePlayIntent = new Intent(Intent.ACTION_VIEW);
		googlePlayIntent.setData(Uri.parse("market://details?id=" + appPackage));
		try {
			startActivity(googlePlayIntent);
		}
		catch (Exception e) {
			DialogUtil.displayError(this, R.string.message_dialog_failed_to_open_google_play);
		}
	}


	/**
	 * Check if the main app Eye Diagnosis or Miniris is installed.
	 *
	 * @return true if installed.
	 */
	private boolean isMainAppInstalled() {
		return isAppInstalled(PACKAGE_EYE_DIAGNOSIS) || isAppInstalled(PACKAGE_MINIRIS);
	}

	/**
	 * Determine if an app is installed.
	 *
	 * @param appPackage the app package name.
	 * @return true if the app is installed.
	 */
	private boolean isAppInstalled(final String appPackage) {
		Intent appIntent = getPackageManager().getLaunchIntentForPackage(appPackage);
		return appIntent != null;
	}

}
