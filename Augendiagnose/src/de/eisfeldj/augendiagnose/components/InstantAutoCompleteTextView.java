package de.eisfeldj.augendiagnose.components;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

/**
 * Variant of AutoCompleteTextView that shows the suggestions immediately without waiting for minimum input.
 */
public class InstantAutoCompleteTextView extends AutoCompleteTextView {

	public InstantAutoCompleteTextView(Context context) {
		super(context);
	}

	public InstantAutoCompleteTextView(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
	}

	public InstantAutoCompleteTextView(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
	}

	@Override
	public boolean enoughToFilter() {
		return true;
	}

	@Override
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
		if (focused) {
			performFiltering(getText(), 0);
		}
	}

}