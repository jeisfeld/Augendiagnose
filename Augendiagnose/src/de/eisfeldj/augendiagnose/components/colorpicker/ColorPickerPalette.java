package de.eisfeldj.augendiagnose.components.colorpicker;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.components.colorpicker.ColorPickerSwatch.OnColorSelectedListener;

/**
 * A color picker custom view which creates an grid of color squares. The number of squares per row (and the padding
 * between the squares) is determined by the user.
 */
public class ColorPickerPalette extends TableLayout {

	/**
	 * The listener called after a color has been selected.
	 */
	private OnColorSelectedListener mOnColorSelectedListener;
	/**
	 * The description of the non-selected swatches.
	 */
	private String mDescription;
	/**
	 * The description of the selected swatches.
	 */
	private String mDescriptionSelected;
	/**
	 * The size of the swatches.
	 */
	private int mSwatchLength;
	/**
	 * The size of the margin around the swatches.
	 */
	private int mMarginSize;
	/**
	 * The number of columns.
	 */
	private int mNumColumns;

	/**
	 * Constructor passing attributes.
	 *
	 * @param context
	 *            The context.
	 * @param attrs
	 *            The attributes.
	 */
	public ColorPickerPalette(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Standard constructor.
	 *
	 * @param context
	 *            The context.
	 */
	public ColorPickerPalette(final Context context) {
		super(context);
	}

	/**
	 * Initialize the size, columns, and listener. Size should be a pre-defined size (SIZE_LARGE or SIZE_SMALL) from
	 * ColorPickerDialogFragment.
	 *
	 * @param size
	 *            The size of the palette (SIZE_LARGE or SIZE_SMALL)
	 * @param columns
	 *            The number of columns
	 * @param listener
	 *            The listener to be called when a color is selected.
	 */
	public final void init(final int size, final int columns, final OnColorSelectedListener listener) {
		mNumColumns = columns;
		Resources res = getResources();
		if (size == ColorPickerDialog.SIZE_LARGE) {
			mSwatchLength = res.getDimensionPixelSize(R.dimen.color_swatch_large);
			mMarginSize = res.getDimensionPixelSize(R.dimen.color_swatch_margins_large);
		}
		else {
			mSwatchLength = res.getDimensionPixelSize(R.dimen.color_swatch_small);
			mMarginSize = res.getDimensionPixelSize(R.dimen.color_swatch_margins_small);
		}
		mOnColorSelectedListener = listener;

		mDescription = res.getString(R.string.color_swatch_description);
		mDescriptionSelected = res.getString(R.string.color_swatch_description_selected);
	}

	/**
	 * Helper method to create a row of the table of colors.
	 *
	 * @return The row.
	 */
	private TableRow createTableRow() {
		TableRow row = new TableRow(getContext());
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		row.setLayoutParams(params);
		return row;
	}

	/**
	 * Adds swatches to table in a serpentine format.
	 *
	 * @param colors
	 *            The colors to be added.
	 * @param selectedColor
	 *            The preseleted color.
	 */
	public final void drawPalette(final int[] colors, final int selectedColor) {
		if (colors == null) {
			return;
		}

		this.removeAllViews();
		int tableElements = 0;
		int rowElements = 0;
		int rowNumber = 0;

		// Fills the table with swatches based on the array of colors.
		TableRow row = createTableRow();
		for (int color : colors) {
			tableElements++;

			View colorSwatch = createColorSwatch(color, selectedColor);
			setSwatchDescription(rowNumber, tableElements, rowElements, color == selectedColor,
					colorSwatch);
			addSwatchToRow(row, colorSwatch);

			rowElements++;
			if (rowElements == mNumColumns) {
				addView(row);
				row = createTableRow();
				rowElements = 0;
				rowNumber++;
			}
		}

		// Create blank views to fill the row if the last row has not been filled.
		if (rowElements > 0) {
			while (rowElements != mNumColumns) {
				addSwatchToRow(row, createBlankSpace());
				rowElements++;
			}
			addView(row);
		}
	}

	/**
	 * Appends a swatch to the end of the row.
	 *
	 * @param row
	 *            The row.
	 * @param swatch
	 *            The swatch to be added.
	 */
	private static void addSwatchToRow(final TableRow row, final View swatch) {
		row.addView(swatch);
	}

	/**
	 * Add a content description to the specified swatch view. Because the colors get added in a snaking form, every
	 * other row will need to compensate for the fact that the colors are added in an opposite direction from their
	 * left->right/top->bottom order, which is how the system will arrange them for accessibility purposes.
	 *
	 * @param rowNumber
	 *            The row number.
	 * @param index
	 *            The index of the view.
	 * @param rowElements
	 *            The number of elements in the row.
	 * @param selected
	 *            Flag indicating if the swatch is selected.
	 * @param swatch
	 *            The swatch to be affected.
	 */
	private void setSwatchDescription(final int rowNumber, final int index, final int rowElements,
			final boolean selected,
			final View swatch) {
		int accessibilityIndex;
		if (rowNumber % 2 == 0) {
			// We're in a regular-ordered row
			accessibilityIndex = index;
		}
		else {
			// We're in a backwards-ordered row.
			int rowMax = (rowNumber + 1) * mNumColumns;
			accessibilityIndex = rowMax - rowElements;
		}

		String description;
		if (selected) {
			description = String.format(mDescriptionSelected, accessibilityIndex);
		}
		else {
			description = String.format(mDescription, accessibilityIndex);
		}
		swatch.setContentDescription(description);
	}

	/**
	 * Creates a blank space to fill the row.
	 *
	 * @return the view with the blank space.
	 */
	private ImageView createBlankSpace() {
		ImageView view = new ImageView(getContext());
		TableRow.LayoutParams params = new TableRow.LayoutParams(mSwatchLength, mSwatchLength);
		params.setMargins(mMarginSize, mMarginSize, mMarginSize, mMarginSize);
		view.setLayoutParams(params);
		return view;
	}

	/**
	 * Creates a color swatch.
	 *
	 * @param color
	 *            The color of the swatch.
	 * @param selectedColor
	 *            The selected color.
	 * @return the created color swatch.
	 */
	private ColorPickerSwatch createColorSwatch(final int color, final int selectedColor) {
		ColorPickerSwatch view = new ColorPickerSwatch(getContext(), color,
				color == selectedColor, mOnColorSelectedListener);
		TableRow.LayoutParams params = new TableRow.LayoutParams(mSwatchLength, mSwatchLength);
		params.setMargins(mMarginSize, mMarginSize, mMarginSize, mMarginSize);
		view.setLayoutParams(params);
		return view;
	}
}
