package de.jeisfeld.augendiagnoselib.util;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders.EventBuilder;
import com.google.android.gms.analytics.HitBuilders.ScreenViewBuilder;
import com.google.android.gms.analytics.HitBuilders.TimingBuilder;
import com.google.android.gms.analytics.Tracker;

import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;

/**
 * Utility class for sending Google Analytics Events.
 */
public final class TrackingUtil {
	/**
	 * The tracker for Google Analytics (on instance level).
	 */
	private static Tracker mTracker;

	/**
	 * Hide default constructor.
	 */
	private TrackingUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Send a screen opening event.
	 *
	 * @param object The activity or fragment showing the screen.
	 */
	public static void sendScreen(final Object object) {
		getDefaultTracker();
		mTracker.setScreenName(object.getClass().getSimpleName());
		mTracker.send(new ScreenViewBuilder().build());
	}

	/**
	 * Start a new session.
	 */
	public static void startSession() {
		mTracker.send(new ScreenViewBuilder().setNewSession().build());
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
		getDefaultTracker();
		EventBuilder eventBuilder = new EventBuilder();
		eventBuilder.setCategory(category.toString());
		if (action != null) {
			eventBuilder.setAction(action);
		}
		if (label == null) {
			eventBuilder.setLabel(action);
		}
		else {
			eventBuilder.setLabel(action + " - " + label);
		}
		if (value != null) {
			eventBuilder.setValue(value);
		}
		mTracker.send(eventBuilder.build());
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
		getDefaultTracker();
		TimingBuilder timingBuilder = new TimingBuilder();
		timingBuilder.setCategory(category.toString());
		timingBuilder.setVariable(variable);
		if (label == null) {
			timingBuilder.setLabel(variable);
		}
		else {
			timingBuilder.setLabel(variable + " - " + label);
		}
		timingBuilder.setValue(duration);
		mTracker.send(timingBuilder.build());
	}

	/**
	 * Gets the default {@link Tracker} for this {@link Application}.
	 *
	 * @return tracker
	 */
	private static synchronized Tracker getDefaultTracker() {
		if (mTracker == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(Application.getAppContext());
			// To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
			mTracker = analytics.newTracker(R.xml.global_tracker);
			mTracker.enableExceptionReporting(true);
		}
		return mTracker;
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
