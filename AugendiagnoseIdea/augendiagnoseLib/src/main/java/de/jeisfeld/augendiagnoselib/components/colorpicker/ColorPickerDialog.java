package de.jeisfeld.augendiagnoselib.components.colorpicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.components.colorpicker.ColorPickerSwatch.OnColorSelectedListener;

/**
 * A dialog which takes in as input an array of colors and creates a palette allowing the user to select a specific
 * color swatch, which invokes a listener.
 */
public class ColorPickerDialog extends DialogFragment implements OnColorSelectedListener {

	/**
	 * Size parameter indicating a large size of swatches.
	 */
	public static final int SIZE_LARGE = 1;
	/**
	 * Size parameter indicating a small size of swatches.
	 */
	public static final int SIZE_SMALL = 2;

	/**
	 * The resource key for the title resource id.
	 */
	private static final String KEY_TITLE_ID = "title_id";
	/**
	 * The resource key for the list of colors.
	 */
	private static final String KEY_COLORS = "colors";
	/**
	 * The resource key for the selected color.
	 */
	private static final String KEY_SELECTED_COLOR = "selected_color";
	/**
	 * The resource key for the number of columns.
	 */
	private static final String KEY_COLUMNS = "columns";
	/**
	 * The resource key for the size.
	 */
	private static final String KEY_SIZE = "size";

	/**
	 * The resource id of the dialog title.
	 */
	private int mTitleResId = R.string.color_picker_default_title;

	/**
	 * The list of displayed colors.
	 */
	private int[] mColors = null;

	/**
	 * The selected color.
	 */
	private int mSelectedColor;

	/**
	 * The number of columns.
	 */
	private int mColumns;

	/**
	 * The size of the swatches. (SIZE_LARGE or SIZE_SMALL)
	 */
	private int mSize;

	/**
	 * The color palette.
	 */
	private ColorPickerPalette mPalette;

	/**
	 * The listener called when a color is selected.
	 */
	private OnColorSelectedListener mListener;

	/**
	 * Create a new ColorPickerDialog.
	 *
	 * @param titleResId    The resource if of the title.
	 * @param colors        The list of colors to be displayed.
	 * @param selectedColor The selected color.
	 * @param columns       The number of columns.
	 * @param size          The size of the displayed swatches.
	 * @return The dialog.
	 */
	public static ColorPickerDialog newInstance(final int titleResId, final int[] colors, final int selectedColor,
												final int columns, final int size) {
		ColorPickerDialog ret = new ColorPickerDialog();
		ret.initialize(titleResId, colors, selectedColor, columns, size);
		return ret;
	}

	/**
	 * Initialize the dialog.
	 *
	 * @param titleResId    The resource if of the title.
	 * @param colors        The list of colors to be displayed.
	 * @param selectedColor The selected color.
	 * @param columns       The number of columns.
	 * @param size          The size of the displayed swatches.
	 */
	private void initialize(final int titleResId, final int[] colors, final int selectedColor, final int columns,
							final int size) {
		setArguments(titleResId, columns, size);
		setColors(colors, selectedColor);
	}

	/**
	 * Set the arguments to be passed to the dialog.
	 *
	 * @param titleResId The resource id of the title.
	 * @param columns    The number of columns.
	 * @param size       The size of the displayed swatches.
	 */
	private void setArguments(final int titleResId, final int columns, final int size) {
		Bundle bundle = new Bundle();
		bundle.putInt(KEY_TITLE_ID, titleResId);
		bundle.putInt(KEY_COLUMNS, columns);
		bundle.putInt(KEY_SIZE, size);
		setArguments(bundle);
	}

	public final void setOnColorSelectedListener(final OnColorSelectedListener listener) {
		mListener = listener;
	}

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			mTitleResId = getArguments().getInt(KEY_TITLE_ID);
			mColumns = getArguments().getInt(KEY_COLUMNS);
			mSize = getArguments().getInt(KEY_SIZE);
		}

		if (savedInstanceState != null) {
			mColors = savedInstanceState.getIntArray(KEY_COLORS);
			mSelectedColor = (Integer) savedInstanceState.getSerializable(KEY_SELECTED_COLOR);
		}
	}

	@Override
	public final Dialog onCreateDialog(final Bundle savedInstanceState) {
		final Activity activity = getActivity();

		View view =
				LayoutInflater.from(getActivity()).inflate(R.layout.color_picker_dialog, new LinearLayout(activity));
		mPalette = (ColorPickerPalette) view.findViewById(R.id.color_picker);
		mPalette.init(mSize, mColumns, this);

		if (mColors != null) {
			showPaletteView();
		}

		return new AlertDialog.Builder(activity)
				.setTitle(mTitleResId)
				.setView(view)
				.create();
	}

	@Override
	public final void onColorSelected(final int color) {
		if (mListener != null) {
			mListener.onColorSelected(color);
		}

		if (getTargetFragment() instanceof OnColorSelectedListener) {
			final OnColorSelectedListener listener =
					(OnColorSelectedListener) getTargetFragment();
			listener.onColorSelected(color);
		}

		if (color != mSelectedColor) {
			mSelectedColor = color;
			// Redraw palette to show checkmark on newly selected color before dismissing.
			mPalette.drawPalette(mColors, mSelectedColor);
		}

		dismiss();
	}

	/**
	 * Show the palette.
	 */
	private void showPaletteView() {
		if (mPalette != null) {
			refreshPalette();
			mPalette.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Set the list of colors and the selected color.
	 *
	 * @param colors        The list of colors to be displayed.
	 * @param selectedColor The selected color.
	 */
	private void setColors(final int[] colors, final int selectedColor) {
		if (mColors != colors || mSelectedColor != selectedColor) {
			mColors = colors;
			mSelectedColor = selectedColor;
			refreshPalette();
		}
	}

	/**
	 * Refresh the color palette.
	 */
	private void refreshPalette() {
		if (mPalette != null && mColors != null) {
			mPalette.drawPalette(mColors, mSelectedColor);
		}
	}

	public final int[] getColors() {
		return mColors;
	}

	public final int getSelectedColor() {
		return mSelectedColor;
	}

	@Override
	public final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putIntArray(KEY_COLORS, mColors);
		outState.putSerializable(KEY_SELECTED_COLOR, mSelectedColor);
	}
}
