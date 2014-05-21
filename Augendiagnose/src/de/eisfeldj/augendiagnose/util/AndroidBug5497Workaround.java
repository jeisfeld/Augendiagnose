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
 */
public class AndroidBug5497Workaround {
	// For more information, see https://code.google.com/p/android/issues/detail?id=5497
	// To use this class, simply invoke assistActivity() on an Activity that already has its content view set.

	public static void assistActivity(Activity activity) {
		new AndroidBug5497Workaround(activity);
	}

	private View mChildOfContent;
	private int usableHeightPrevious;
	private FrameLayout.LayoutParams frameLayoutParams;
	private ActivityWithExplicitLayoutTrigger activityWithLayoutTrigger = null;

	private AndroidBug5497Workaround(Activity activity) {
		FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
		mChildOfContent = content.getChildAt(0);
		mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			public void onGlobalLayout() {
				possiblyResizeChildOfContent();
			}
		});
		frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();

		if (activity instanceof ActivityWithExplicitLayoutTrigger) {
			activityWithLayoutTrigger = (ActivityWithExplicitLayoutTrigger) activity;
		}
	}

	private void possiblyResizeChildOfContent() {
		int usableHeightNow = computeUsableHeight();
		if (usableHeightNow != usableHeightPrevious) {
			int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
			int heightDifference = usableHeightSansKeyboard - usableHeightNow;
			if (heightDifference > (usableHeightSansKeyboard / 8)) {
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

	private int computeUsableHeight() {
		Rect r = new Rect();
		mChildOfContent.getWindowVisibleDisplayFrame(r);
		return (r.bottom - r.top);
	}

	/**
	 * Callback for activities to request layout
	 */
	public interface ActivityWithExplicitLayoutTrigger {
		public void requestLayout();
	}
}
