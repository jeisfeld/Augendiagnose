package de.jeisfeld.miniris;

import android.app.Activity;
import android.content.Intent;
import de.jeisfeld.augendiagnoselib.activities.ListFoldersForDisplayActivity;
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
	public void startApplication(final Activity triggeringActivity) {
		Intent intent = new Intent(triggeringActivity, ListFoldersForDisplayActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		triggeringActivity.startActivity(intent);
	}

}
