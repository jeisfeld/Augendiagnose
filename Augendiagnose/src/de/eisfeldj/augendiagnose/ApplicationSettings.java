package de.eisfeldj.augendiagnose;

import java.util.concurrent.TimeUnit;

import de.jeisfeld.augendiagnoselib.Application.AuthorizationLevel;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.util.EncryptionUtil;
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
	private static volatile ApplicationSettings instance;

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
		if (instance == null) {
			instance = new ApplicationSettings();
		}
		return instance;
	}

	@Override
	protected AuthorizationLevel getAuthorizationLevel() {
		if (de.jeisfeld.augendiagnoselib.Application.getVersion() <= 44) { // MAGIC_NUMBER
			String userKey = PreferenceUtil.getSharedPreferenceString(R.string.key_user_key);
			return EncryptionUtil.validateUserKey(userKey) ? AuthorizationLevel.FULL_ACCESS : AuthorizationLevel.FULL_ACCESS_WITH_ADS;
		}
		else {
			long firstStartTime = PreferenceUtil.getSharedPreferenceLong(R.string.key_statistics_firststarttime, -1);
			return System.currentTimeMillis() < firstStartTime + TimeUnit.DAYS.toMillis(1)
					? AuthorizationLevel.FULL_ACCESS : AuthorizationLevel.TRIAL_ACCESS;
		}
	}
}
