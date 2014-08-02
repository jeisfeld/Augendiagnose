package de.eisfeldj.augendiagnose.components;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

/**
 * Variant of AutoCompleteTextView that shows the suggestions immediately without waiting for minimum input.
 */
public class InstantAutoCompleteTextView extends AutoCompleteTextView {

	// JAVADOC:OFF
	/**
	 * Standard constructor to be implemented for all views.
	 *
	 * @see #View(Context)
	 */
	public InstantAutoCompleteTextView(final Context context) {
		super(context);
	}

	/**
	 * Standard constructor to be implemented for all views.
	 *
	 * @see #View(Context, AttributeSet)
	 */
	public InstantAutoCompleteTextView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Standard constructor to be implemented for all views.
	 *
	 * @see #View(Context, AttributeSet, int)
	 */
	public InstantAutoCompleteTextView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	// JAVADOC:ON

	@Override
	public final boolean enoughToFilter() {
		return true;
	}

	@Override
	protected final void onFocusChanged(final boolean focused, final int direction, final Rect previouslyFocusedRect) {
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
		if (focused) {
			performFiltering(getText(), 0);
		}
	}

}