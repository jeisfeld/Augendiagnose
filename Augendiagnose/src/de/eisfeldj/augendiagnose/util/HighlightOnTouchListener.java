package de.eisfeldj.augendiagnose.util;

import android.view.MotionEvent;
import android.view.View;

/**
 * Listener to highlight a view only while touching.
 */
public class HighlightOnTouchListener implements View.OnTouchListener {
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			v.setBackgroundColor(v.getContext().getResources().getColor(android.R.color.holo_blue_light));
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			v.setBackgroundColor(v.getContext().getResources().getColor(android.R.color.transparent));
			break;
		}
		return false;
	}

}
