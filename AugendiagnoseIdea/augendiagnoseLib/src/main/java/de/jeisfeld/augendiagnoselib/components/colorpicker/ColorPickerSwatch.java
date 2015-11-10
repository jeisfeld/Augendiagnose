package de.jeisfeld.augendiagnoselib.components.colorpicker;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
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
	 * @param color The color to be set.
	 */
	private void setColor(final int color) {
		Drawable[] colorDrawable = new Drawable[] {getContext().getResources().getDrawable(R.drawable.color_picker_swatch, null)};
		mSwatchImage.setImageDrawable(new ColorStateDrawable(colorDrawable, color));
	}

	/**
	 * Set the checked flag of the swatch.
	 *
	 * @param checked The falue of the flag.
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
	 * Standard constructor to be implemented for all views.
	 *
	 * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
	 * @see android.view.View#View(Context)
	 */
	public ColorPickerSwatch(final Context context) {
		this(context, null, 0);
	}

	/**
	 * Standard constructor to be implemented for all views.
	 *
	 * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
	 * @param attrs The attributes of the XML tag that is inflating the view.
	 * @see android.view.View#View(Context, AttributeSet)
	 */
	public ColorPickerSwatch(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Standard constructor to be implemented for all views.
	 *
	 * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
	 * @param attrs The attributes of the XML tag that is inflating the view.
	 * @param defStyle An attribute in the current theme that contains a reference to a style resource that supplies default
	 *            values for the view. Can be 0 to not look for defaults.
	 * @see android.view.View#View(Context, AttributeSet, int)
	 */
	public ColorPickerSwatch(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * Constructor initializing the swatch.
	 *
	 * @param context The application context.
	 * @param color The color of the swatch.
	 * @param checked Flag indicating if the swatch is flagged.
	 * @param listener A listener called if the swatch is selected.
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
		 * @param color the selected color.
		 */
		void onColorSelected(int color);
	}
}
