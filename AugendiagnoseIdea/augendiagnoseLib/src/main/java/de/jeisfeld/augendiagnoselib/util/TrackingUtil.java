package de.jeisfeld.augendiagnoselib.util;


import android.app.Activity;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import de.jeisfeld.augendiagnoselib.Application;

/**
 * Utility class for sending Google Analytics Events.
 */
public final class TrackingUtil {
	/**
	 * The tracker for Google Analytics (on instance level).
	 */
	private static FirebaseAnalytics mFirebaseAnalytics;

	/**
	 * Hide default constructor.
	 */
	private TrackingUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Send a screen opening event.
	 *
	 * @param activity The activity showing the screen.
	 */
	public static void sendScreen(final Activity activity) {
		getDefaultFirebaseAnalytics();

		mFirebaseAnalytics.setCurrentScreen(activity, null, null);

		Bundle params = new Bundle();
		params.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "screen");
		params.putString(FirebaseAnalytics.Param.ITEM_NAME, activity.getClass().getSimpleName());
		mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, params);
	}

	/**
	 * Send a specific event.
	 *
	 * @param category The event category.
	 * @param action   The action.
	 * @param label    The label.
	 * @param value    An associated value.
	 */
	public static void sendEvent(final Category category, final String action, final String label, final Long value) {
		getDefaultFirebaseAnalytics();

		Bundle params = new Bundle();
		params.putString("category", category.toString());
		params.putString("action", action);
		if (label == null) {
			params.putString("label", action);
		}
		else {
			params.putString("label", action + " - " + label);
		}
		if (value != null) {
			params.putLong("value", value);
		}
		mFirebaseAnalytics.logEvent(action, params);
	}

	/**
	 * Send a specific event.
	 *
	 * @param category The event category.
	 * @param action   The action.
	 * @param label    The label.
	 */
	public static void sendEvent(final Category category, final String action, final String label) {
		sendEvent(category, action, label, null);
	}

	/**
	 * Send timing information.
	 *
	 * @param category The event category.
	 * @param variable The variable.
	 * @param label    The label.
	 * @param duration The duration.
	 */
	public static void sendTiming(final Category category, final String variable, final String label, final long duration) {
		getDefaultFirebaseAnalytics();

		Bundle params = new Bundle();
		params.putString("category", category.toString());
		params.putString("variable", variable);
		if (label == null) {
			params.putString("label", variable);
		}
		else {
			params.putString("label", variable + " - " + label);
		}
		params.putLong("value", duration);
		mFirebaseAnalytics.logEvent("Timing", params);
	}

	/**
	 * Send exception information.
	 *
	 * @param message A small message.
	 * @param e       The exception.
	 */
	public static void sendException(final String message, final Throwable e) {
		getDefaultFirebaseAnalytics();

		Bundle params = new Bundle();
		params.putString("category", "Exception");
		params.putString("message1", message);
		if (e != null) {
			params.putString("message2", e.getMessage());
			params.putString("class", e.getClass().toString());
			if (e.getStackTrace() != null) {
				params.putString("stacktrace0", e.getStackTrace()[0].toString());
			}
		}
		mFirebaseAnalytics.logEvent("Exception", params);
	}

	/**
	 * Gets the default {@link FirebaseAnalytics} for this {@link Application}.
	 *
	 * @return tracker
	 */
	private static synchronized FirebaseAnalytics getDefaultFirebaseAnalytics() {
		if (mFirebaseAnalytics == null) {
			mFirebaseAnalytics = FirebaseAnalytics.getInstance(Application.getAppContext());
		}
		return mFirebaseAnalytics;
	}

	/**
	 * Categories of app events.
	 */
	public enum Category {
		/**
		 * Activities of the user.
		 */
		EVENT_USER,
		/**
		 * Background events of the app.
		 */
		EVENT_BACKGROUND,
		/**
		 * Usage times of the app.
		 */
		TIME_USAGE,
		/**
		 * Background times of the app.
		 */
		TIME_BACKGROUND,
		/**
		 * Image counters.
		 */
		COUNTER_STATISTICS
	}
}
