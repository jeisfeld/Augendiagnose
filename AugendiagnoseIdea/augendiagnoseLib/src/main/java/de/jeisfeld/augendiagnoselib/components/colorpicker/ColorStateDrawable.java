package de.jeisfeld.augendiagnoselib.components.colorpicker;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import androidx.annotation.NonNull;

/**
 * A drawable which sets its color filter to a color specified by the user, and changes to a slightly darker color when
 * pressed or focused.
 */
public class ColorStateDrawable extends LayerDrawable {

	/**
	 * The multiplier for adjusting the pressed color.
	 */
	private static final float PRESSED_STATE_MULTIPLIER = 0.70f;

	/**
	 * Ths color of this drawable.
	 */
	private final int mColor;

	/**
	 * Constructor setting the color.
	 *
	 * @param layers The layers (required for super constructor)
	 * @param color  The color.
	 */
	public ColorStateDrawable(@NonNull final Drawable[] layers, final int color) {
		super(layers);
		mColor = color;
	}

	@Override
	protected final boolean onStateChange(@NonNull final int[] states) {
		boolean pressedOrFocused = false;
		for (int state : states) {
			if (state == android.R.attr.state_pressed || state == android.R.attr.state_focused) {
				pressedOrFocused = true;
				break;
			}
		}

		if (pressedOrFocused) {
			super.setColorFilter(getPressedColor(mColor), PorterDuff.Mode.SRC_ATOP);
		}
		else {
			super.setColorFilter(mColor, PorterDuff.Mode.SRC_ATOP);
		}

		return super.onStateChange(states);
	}

	/**
	 * Given a particular color, adjusts its value by a multiplier.
	 *
	 * @param color the color.
	 * @return the adjusted color.
	 */
	private static int getPressedColor(final int color) {
		float[] hsv = new float[3]; // MAGIC_NUMBER
		Color.colorToHSV(color, hsv);
		hsv[2] = hsv[2] * PRESSED_STATE_MULTIPLIER;
		return Color.HSVToColor(hsv);
	}

	@Override
	public final boolean isStateful() {
		return true;
	}
}
