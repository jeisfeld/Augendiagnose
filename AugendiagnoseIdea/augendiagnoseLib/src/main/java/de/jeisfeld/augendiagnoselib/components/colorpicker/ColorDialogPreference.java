package de.jeisfeld.augendiagnoselib.components.colorpicker;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.graphics.Color;
import android.preference.DialogPreference;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.ScaleXSpan;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.annotation.NonNull;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.components.colorpicker.ColorPickerSwatch.OnColorSelectedListener;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;

/**
 * The ColorDialogPreference class is responsible for selecting a color as shared preference.
 */
public class ColorDialogPreference extends DialogPreference implements OnColorSelectedListener {
	/**
	 * The selected color.
	 */
	private int mSelectedColor = 0;

	/**
	 * ColorDialogPreference constructor.
	 *
	 * @param context of this class.
	 * @param attrs   custom xml attributes.
	 */
	public ColorDialogPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		updateSummary();
	}

	@Override
	protected final void onDialogClosed(final boolean positiveResult) {
		// Persist the selectedColor
		if (positiveResult && mSelectedColor != 0) {
			persistInt(mSelectedColor);
		}

		updateSummary();

		super.onDialogClosed(positiveResult);
	}

	/**
	 * Update the summary of the preference.
	 */
	private void updateSummary() {
		SpannableString summary = new SpannableString(getContext().getString(R.string.button_select_color));
		int overlayColor = PreferenceUtil.getSharedPreferenceInt(R.string.key_overlay_color, Color.RED);
		summary.setSpan(new ForegroundColorSpan(overlayColor), 0, summary.length(), 0);
		summary.setSpan(new RelativeSizeSpan(1.2f), 0, summary.length(), 0); // MAGIC_NUMBER
		summary.setSpan(new ScaleXSpan(5), 0, summary.length(), 0); // MAGIC_NUMBER
		setSummary(summary);
	}

	@Override
	protected final void onPrepareDialogBuilder(@NonNull final Builder builder) {
		super.onPrepareDialogBuilder(builder);
		int storedColor = getSharedPreferences().getInt(getContext().getString(R.string.key_overlay_color), Color.RED);

		ColorPickerPalette palette = new ColorPickerPalette(getContext());
		palette.init(ColorPickerConstants.COLOR_PICKER_SIZE, ColorPickerConstants.COLOR_PICKER_COLUMNS, this);
		palette.drawPalette(ColorPickerConstants.COLOR_PICKER_COLORS, storedColor);
		palette.setGravity(Gravity.CENTER);

		builder.setView(palette);
		builder.setPositiveButton(null, null);
	}

	@Override
	public final void onColorSelected(final int color) {
		mSelectedColor = color;
		onDialogClosed(true);
		getDialog().dismiss();
	}
}
