package de.eisfeldj.augendiagnose;

import android.app.Activity;
import android.content.Intent;
import de.eisfeldj.augendiagnose.activities.MainActivity;
import de.jeisfeld.augendiagnoselib.Application.AuthorizationLevel;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.util.AdMarvelUtil;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Utility class to hold private constants.
 */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS",
		justification = "Intentionally using same name as superclass")
public final class ApplicationSettings extends de.jeisfeld.augendiagnoselib.ApplicationSettings {
	/**
	 * An instance of this class.
	 */
	private static volatile ApplicationSettings mInstance;

	/**
	 * Keep constructor private.
	 */
	private ApplicationSettings() {
	}

	/**
	 * Get an instance of this class.
	 *
	 * @return An instance of this class.
	 */
	public static ApplicationSettings getInstance() {
		if (mInstance == null) {
			mInstance = new ApplicationSettings();
		}
		return mInstance;
	}

	@Override
	protected AuthorizationLevel getAuthorizationLevel() {
		AuthorizationLevel level = super.getAuthorizationLevel();

		if (PreferenceUtil.getSharedPreferenceString(R.string.key_user_key).startsWith(AdMarvelUtil.FORCE_AD_USER)) {
			return AuthorizationLevel.FULL_ACCESS_WITH_ADS;
		}

		int initialVersion = PreferenceUtil.getSharedPreferenceInt(R.string.key_statistics_initialversion, 45); // MAGIC_NUMBER
		if (initialVersion <= 44) { // MAGIC_NUMBER
			// Special handling for "old" users that may keep full functionality.
			return level == AuthorizationLevel.FULL_ACCESS ? AuthorizationLevel.FULL_ACCESS : AuthorizationLevel.FULL_ACCESS_WITH_ADS;
		}
		else {
			return level;
		}
	}

	@Override
	public void startApplication(final Activity triggeringActivity) {
		Intent intent = new Intent(triggeringActivity, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		triggeringActivity.startActivity(intent);
	}
}
