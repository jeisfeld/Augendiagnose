package de.jeisfeld.augendiagnoselib;

import java.util.concurrent.TimeUnit;

import android.app.Activity;
import de.jeisfeld.augendiagnoselib.Application.AuthorizationLevel;
import de.jeisfeld.augendiagnoselib.util.EncryptionUtil;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.SystemUtil;

/**
 * Utility interface for Settings which are application specific.
 */
public abstract class ApplicationSettings {

	/**
	 * Find out if the user is authorized to use all functionality of the app.
	 *
	 * @return The authorization level of the user.
	 */
	// OVERRIDABLE
	protected AuthorizationLevel getAuthorizationLevel() {
		String userKey = PreferenceUtil.getSharedPreferenceString(R.string.key_user_key);
		boolean hasPremiumPack = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_internal_has_premium_pack);
		boolean isAuthorizedUser = hasPremiumPack || EncryptionUtil.validateUserKey(userKey) || SystemUtil.isJeDevice();

		if (isAuthorizedUser) {
			return AuthorizationLevel.FULL_ACCESS;
		}

		long firstStartTime = PreferenceUtil.getSharedPreferenceLong(R.string.key_statistics_firststarttime, -1);
		return System.currentTimeMillis() < firstStartTime + TimeUnit.DAYS.toMillis(1)
				? AuthorizationLevel.TRIAL_ACCESS : AuthorizationLevel.NO_ACCESS;
	}

	/**
	 * Start the application.
	 *
	 * @param triggeringActivity
	 *            the triggering activity.
	 */
	public abstract void startApplication(final Activity triggeringActivity);
}
