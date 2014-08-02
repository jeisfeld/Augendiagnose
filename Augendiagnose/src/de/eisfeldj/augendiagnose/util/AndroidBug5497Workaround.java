package de.eisfeldj.augendiagnose.util;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

/**
 * Utility class that ensures that the activity is resized when the keyboard appears, in full screen mode.
 *
 * android:windowSoftInputMode="adjustResize" does not work in full screen mode.
 *
 * Thanks to Joseph Johnson for this class.
 *
 * For more information, see https://code.google.com/p/android/issues/detail?id=5497 To use this class, simply invoke
 * assistActivity() on an Activity that already has its content view set.
 */
public final class AndroidBug5497Workaround {

	/**
	 * Assumed minimum portion that the keyboard fills vertically.
	 */
	private static final float MIN_KEYBOARD_SIZE = .1f;

	/**
	 * Method to be called to apply the workaround to the activity. Should be called at the end of onCreate().
	 *
	 * @param activity
	 *            the activity which uses the workaround.
	 */
	@SuppressWarnings("unused")
	public static void assistActivity(final Activity activity) {
		new AndroidBug5497Workaround(activity);
	}

	// JAVADOC:OFF
	private View mChildOfContent;
	private int usableHeightPrevious;
	private FrameLayout.LayoutParams frameLayoutParams;
	private ActivityWithExplicitLayoutTrigger activityWithLayoutTrigger = null;

	// JAVADOC:ON

	/**
	 * Constructor, adding a listener to change the global layout if required.
	 *
	 * @param activity
	 *            the activity which uses the workaround.
	 */
	private AndroidBug5497Workaround(final Activity activity) {
		FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
		mChildOfContent = content.getChildAt(0);
		mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				possiblyResizeChildOfContent();
			}
		});
		frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();

		if (activity instanceof ActivityWithExplicitLayoutTrigger) {
			activityWithLayoutTrigger = (ActivityWithExplicitLayoutTrigger) activity;
		}
	}

	/**
	 * Resize the view, as the keyboard may have appeared or vanished.
	 */
	private void possiblyResizeChildOfContent() {
		int usableHeightNow = computeUsableHeight();
		if (usableHeightNow != usableHeightPrevious) {
			int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
			int heightDifference = usableHeightSansKeyboard - usableHeightNow;
			if (heightDifference > (usableHeightSansKeyboard * MIN_KEYBOARD_SIZE)) {
				// keyboard probably just became visible
				frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
			}
			else {
				// keyboard probably just became hidden
				frameLayoutParams.height = usableHeightSansKeyboard;
			}
			mChildOfContent.requestLayout();
			usableHeightPrevious = usableHeightNow;

			if (activityWithLayoutTrigger != null) {
				activityWithLayoutTrigger.requestLayout();
			}
		}
	}

	/**
	 * Calculate the height that can be used for the view above keyboard.
	 *
	 * @return the usable height.
	 */
	private int computeUsableHeight() {
		Rect r = new Rect();
		mChildOfContent.getWindowVisibleDisplayFrame(r);
		return (r.bottom - r.top);
	}

	/**
	 * Callback for activities to request layout.
	 */
	public interface ActivityWithExplicitLayoutTrigger {
		/**
		 * Callback method to do request layout.
		 */
		void requestLayout();
	}
}
