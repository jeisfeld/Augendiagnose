package de.jeisfeld.augendiagnoselib.util;

import android.app.Activity;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

/**
 * Utility class that ensures that the activity is resized when the keyboard appears, in full screen mode.
 *
 * <p>android:windowSoftInputMode="adjustResize" does not work in full screen mode.
 *
 * <p>Thanks to Joseph Johnson for this class.
 *
 * <p>For more information, see https://code.google.com/p/android/issues/detail?id=5497 To use this class, simply invoke
 * assistActivity() on an Activity that already has its content view set.
 */
public final class AutoKeyboardLayoutUtility {
	/**
	 * Method to be called to apply the workaround to the activity. Should be called at the end of onCreate().
	 *
	 * @param activity the activity which uses the workaround.
	 */
	@SuppressWarnings("unused")
	public static void assistActivity(@NonNull final Activity activity) {
		new AutoKeyboardLayoutUtility(activity);
	}


	// JAVADOC:OFF
	private final View mChildOfContent;
	private int mUsableHeightPrevious;
	@NonNull
	private final FrameLayout.LayoutParams mFrameLayoutParams;
	@Nullable
	private ActivityWithExplicitLayoutTrigger mActivityWithLayoutTrigger = null;

	// JAVADOC:ON

	/**
	 * Constructor, adding a listener to change the global layout if required.
	 *
	 * @param activity the activity which uses the workaround.
	 */
	private AutoKeyboardLayoutUtility(@NonNull final Activity activity) {
		FrameLayout content = activity.findViewById(android.R.id.content);
		mChildOfContent = content.getChildAt(0);
		mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				possiblyResizeChildOfContent();
			}
		});
		mFrameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();

		if (activity instanceof ActivityWithExplicitLayoutTrigger) {
			mActivityWithLayoutTrigger = (ActivityWithExplicitLayoutTrigger) activity;
		}
	}

	/**
	 * Resize the view, as the keyboard may have appeared or vanished.
	 */
	private void possiblyResizeChildOfContent() {
		int usableHeightNow = computeUsableHeight();
		if (usableHeightNow != mUsableHeightPrevious) {
			mFrameLayoutParams.height = usableHeightNow;
			mChildOfContent.requestLayout();
			mUsableHeightPrevious = usableHeightNow;

			if (mActivityWithLayoutTrigger != null) {
				mActivityWithLayoutTrigger.requestLayout();
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
		return r.bottom - r.top;
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

	/**
	 * Callback listener that will be informed if the keyboard is added or removed.
	 */
	public interface OnKeyboardChangeListener {
		/**
		 * Callback method that will be called if the keyboard is added or removed.
		 *
		 * @param visible true if the keyboard is added, false if the keyboard is removed.
		 */
		void onKeyboardChanged(boolean visible);
	}
}
