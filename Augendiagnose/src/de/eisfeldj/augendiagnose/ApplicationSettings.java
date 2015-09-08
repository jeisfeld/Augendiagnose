package de.eisfeldj.augendiagnose;

import android.app.Activity;
import android.content.Intent;
import de.eisfeldj.augendiagnose.activities.MainActivity;
import de.jeisfeld.augendiagnoselib.Application.AuthorizationLevel;
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
		AuthorizationLevel level = super.getAuthorizationLevel();

		if (de.jeisfeld.augendiagnoselib.Application.getVersion() <= 44) { // MAGIC_NUMBER
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
		triggeringActivity.startActivity(intent);
	}
}
