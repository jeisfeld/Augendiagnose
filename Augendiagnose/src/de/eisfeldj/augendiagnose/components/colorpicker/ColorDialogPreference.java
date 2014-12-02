package de.eisfeldj.augendiagnose.components.colorpicker;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.graphics.Color;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.components.colorpicker.ColorPickerSwatch.OnColorSelectedListener;

/**
 * The ColorDialogPreference class is responsible for selecting a color as shared preference.
 */
public class ColorDialogPreference extends DialogPreference implements OnColorSelectedListener {
	/**
	 * The selected color.
	 */
	private int selectedColor = 0;

	/**
	 * ColorDialogPreference constructor.
	 *
	 * @param context
	 *            of this class.
	 * @param attrs
	 *            custom xml attributes.
	 */
	public ColorDialogPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected final void onDialogClosed(final boolean positiveResult) {
		// Persist the selectedColor
		if (positiveResult && selectedColor != 0) {
			persistInt(selectedColor);
		}

		super.onDialogClosed(positiveResult);
	}

	@Override
	protected final void onPrepareDialogBuilder(final Builder builder) {
		super.onPrepareDialogBuilder(builder);
		int storedColor = getSharedPreferences().getInt(getContext().getString(R.string.key_overlay_color), Color.RED);

		ColorPickerPalette palette = new ColorPickerPalette(getContext());
		palette.init(ColorPickerConstants.COLOR_PICKER_SIZE, ColorPickerConstants.COLOR_PICKER_COLUMNS, this);
		palette.drawPalette(ColorPickerConstants.COLOR_PICKER_COLORS, storedColor);

		builder.setView(palette);
		builder.setPositiveButton(null, null);
	}

	@Override
	public final void onColorSelected(final int color) {
		selectedColor = color;
		onDialogClosed(true);
		getDialog().dismiss();
	}
}
