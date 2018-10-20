package de.jeisfeld.augendiagnoselib.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.util.Log;

import de.jeisfeld.augendiagnoselib.Application;

/**
 * Base class of activities within the app.
 */
public abstract class BasePreferenceActivity extends PreferenceActivity {
	@Override
	protected final void attachBaseContext(final Context newBase) {
		super.attachBaseContext(Application.createContextWrapperForLocale(newBase));
	}

	// OVERRIDABLE
	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Update title
		try {
			ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
			setTitle(activityInfo.labelRes);
		}
		catch (NameNotFoundException ex) {
			Log.e(Application.TAG, "Error while getting activity info. " + ex.getMessage(), ex);
		}
	}
}
