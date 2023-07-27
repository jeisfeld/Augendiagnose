package de.jeisfeld.augendiagnoselib;

import android.Manifest;
import android.Manifest.permission;
import android.app.Activity;
import android.os.Build.VERSION_CODES;

import java.util.concurrent.TimeUnit;

import de.jeisfeld.augendiagnoselib.Application.AuthorizationLevel;
import de.jeisfeld.augendiagnoselib.util.EncryptionUtil;
import de.jeisfeld.augendiagnoselib.util.EncryptionUtil.KeyValidationResult;
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
		boolean hasPremiumSubscription = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_internal_has_premium_subscription);
		boolean hasUnlockerApp = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_internal_has_unlocker_app);
		KeyValidationResult userKeyValidationResult = EncryptionUtil.validateUserKey(userKey);
		boolean isAuthorizedUser =
				hasPremiumPack || hasPremiumSubscription || hasUnlockerApp || userKeyValidationResult == KeyValidationResult.SUCCESS;

		if (isAuthorizedUser) {
			return AuthorizationLevel.FULL_ACCESS;
		}
		long firstStartTime = PreferenceUtil.getSharedPreferenceLong(R.string.key_statistics_firststarttime, -1);
		int trialPeriod = userKeyValidationResult == KeyValidationResult.PROLONG_TRIAL ? 180 : 14; // MAGIC_NUMBER

		return System.currentTimeMillis() < firstStartTime + TimeUnit.DAYS.toMillis(trialPeriod)
				? AuthorizationLevel.TRIAL_ACCESS : AuthorizationLevel.NO_ACCESS;
	}

	/**
	 * Get the required permissions.
	 *
	 * @return The required permissions for the app.
	 */
	// OVERRIDABLE
	protected String[] getRequiredPermissions() {
		if (SystemUtil.isAtLeastVersion(VERSION_CODES.TIRAMISU)) {
			return new String[]{permission.READ_MEDIA_IMAGES};
		}
		else if (SystemUtil.isAtLeastVersion(VERSION_CODES.R)) {
			return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
		}
		else {
			return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
		}
	}

	/**
	 * Start the application.
	 *
	 * @param triggeringActivity the triggering activity.
	 */
	public abstract void startApplication(Activity triggeringActivity);
}
