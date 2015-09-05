package de.jeisfeld.augendiagnoselib;

import de.jeisfeld.augendiagnoselib.Application.AuthorizationLevel;

/**
 * Utility interface for Settings which are application specific.
 */
public abstract class ApplicationSettings {

	/**
	 * Find out if the user is authorized to use all functionality of the app.
	 *
	 * @return The authorization level of the user.
	 */
	protected abstract AuthorizationLevel getAuthorizationLevel();
}
