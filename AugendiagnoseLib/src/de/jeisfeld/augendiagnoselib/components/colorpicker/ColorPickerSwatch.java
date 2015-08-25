package de.jeisfeld.augendiagnoselib.components.colorpicker;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import de.jeisfeld.augendiagnoselib.R;

/**
 * Creates a circular swatch of a specified color. Adds a checkmark if marked as checked.
 */
public class ColorPickerSwatch extends FrameLayout implements View.OnClickListener {
	/**
	 * The color of the swatch.
	 */
	private int mColor;

	/**
	 * View for the image.
	 */
	private ImageView mSwatchImage;

	/**
	 * View for the checkmark.
	 */
	private ImageView mCheckmarkImage;

	/**
	 * Listener for selection the swatch.
	 */
	private OnColorSelectedListener mOnColorSelectedListener;

	/**
	 * Set the color of the swatch.
	 *
	 * @param color
	 *            The color to be set.
	 */
	private void setColor(final int color) {
		Drawable[] colorDrawable = new Drawable[]
		{ getContext().getResources().getDrawable(R.drawable.color_picker_swatch) };
		mSwatchImage.setImageDrawable(new ColorStateDrawable(colorDrawable, color));
	}

	/**
	 * Set the checked flag of the swatch.
	 *
	 * @param checked
	 *            The falue of the flag.
	 */
	private void setChecked(final boolean checked) {
		if (checked) {
			mCheckmarkImage.setVisibility(View.VISIBLE);
		}
		else {
			mCheckmarkImage.setVisibility(View.GONE);
		}
	}

	@Override
	public final void onClick(final View v) {
		if (mOnColorSelectedListener != null) {
			mOnColorSelectedListener.onColorSelected(mColor);
		}
	}

	/**
	 * Constructor initializing the swatch.
	 *
	 * @param context
	 *            The application context.
	 * @param color
	 *            The color of the swatch.
	 * @param checked
	 *            Flag indicating if the swatch is flagged.
	 * @param listener
	 *            A listener called if the swatch is selected.
	 */
	public ColorPickerSwatch(final Context context, final int color, final boolean checked,
			final OnColorSelectedListener listener) {
		super(context);
		mColor = color;
		mOnColorSelectedListener = listener;

		LayoutInflater.from(context).inflate(R.layout.color_picker_swatch, this);
		mSwatchImage = (ImageView) findViewById(R.id.color_picker_swatch);
		mCheckmarkImage = (ImageView) findViewById(R.id.color_picker_checkmark);
		setColor(color);
		setChecked(checked);
		setOnClickListener(this);
	}

	/**
	 * Interface for a callback when a color square is selected.
	 */
	public interface OnColorSelectedListener {

		/**
		 * Called when a specific color square has been selected.
		 *
		 * @param color
		 *            the selected color.
		 */
		void onColorSelected(int color);
	}
}
