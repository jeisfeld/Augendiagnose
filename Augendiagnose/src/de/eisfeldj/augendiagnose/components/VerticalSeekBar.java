package de.eisfeldj.augendiagnose.components;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

/**
 * Implementation of an easy vertical SeekBar
 */
public class VerticalSeekBar extends SeekBar {

	private OnSeekBarChangeListener mOnSeekBarChangeListener;

	public VerticalSeekBar(Context context) {
		super(context);
	}

	public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public VerticalSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/*
	 * (non-Javadoc) ${see_to_overridden}
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(h, w, oldh, oldw);
	}

	/*
	 * (non-Javadoc) ${see_to_overridden}
	 */
	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}

	/*
	 * (non-Javadoc) ${see_to_overridden}
	 */
	@Override
	protected void onDraw(Canvas c) {
		c.rotate(-90);
		c.translate(-getHeight(), 0);

		super.onDraw(c);
	}

	/*
	 * (non-Javadoc) ${see_to_overridden}
	 */
	@Override
	public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
		mOnSeekBarChangeListener = l;
		super.setOnSeekBarChangeListener(l);
	}

	/*
	 * (non-Javadoc) ${see_to_overridden}
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled()) {
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
			mOnSeekBarChangeListener.onStartTrackingTouch(this);
			break;

		case MotionEvent.ACTION_MOVE:
			setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
			break;

		case MotionEvent.ACTION_UP:
			setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
			mOnSeekBarChangeListener.onStopTrackingTouch(this);
			break;

		case MotionEvent.ACTION_CANCEL:
			mOnSeekBarChangeListener.onStopTrackingTouch(this);
			break;

		default:
			break;
		}

		return true;
	}

	/*
	 * (non-Javadoc) ${see_to_overridden}
	 */
	@Override
	public void setProgress(int progress) {
		super.setProgress(progress);
		onSizeChanged(getWidth(), getHeight(), 0, 0);
	}
}
