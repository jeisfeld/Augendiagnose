package de.jeisfeld.augendiagnoselib.activities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import de.jeisfeld.augendiagnoselib.Application;

/**
 * Base class of activities within the app.
 */
public abstract class BaseActivity extends Activity {
	@Override
	protected final void attachBaseContext(final Context newBase) {
		super.attachBaseContext(Application.createContextWrapperForLocale(newBase));
	}

	// OVERRIDABLE
	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Update title - required for custom locale on Android N
		try {
			ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
			if (activityInfo.labelRes != 0) {
				setTitle(activityInfo.labelRes);
			}
		}
		catch (Exception ex) {
			Log.e(Application.TAG, "Error while getting activity info. " + ex.getMessage(), ex);
		}
	}
}
