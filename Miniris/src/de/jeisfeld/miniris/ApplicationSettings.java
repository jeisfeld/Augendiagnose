package de.jeisfeld.miniris;

import android.app.Activity;
import de.jeisfeld.augendiagnoselib.activities.CameraActivity;
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
	public void startApplication(final Activity triggeringActivity) {
		CameraActivity.startActivity(triggeringActivity);
	}

}
