package de.eisfeldj.augendiagnose.components.colorpicker;

import android.graphics.Color;
import de.eisfeldj.augendiagnose.Application;

/**
 * Constants used for the color picker by its clients.
 */
public final class ColorPickerConstants {

	/**
	 * Default constructor is overridden to prevent instantiation.
	 */
	private ColorPickerConstants() {
		// Prevent instantiation
	}

	/**
	 * List of colors to be used for the color picker dialog.
	 */
	public static final int[] COLOR_PICKER_COLORS = { Color.BLACK, Color.DKGRAY, Color.GRAY, Color.LTGRAY, Color.WHITE,
			Color.MAGENTA,
			0xFF7F0000, Color.RED, 0xFFFF7F00, Color.YELLOW, 0xFF7F7F00, 0xFF007F00, Color.GREEN, Color.CYAN,
			Color.BLUE, 0xFF00007F };
	/**
	 * Number of columns shown in the color picker dialog.
	 */
	public static final int COLOR_PICKER_COLUMNS = 4;
	/**
	 * Size of symbols in the color picker dialog.
	 */
	public static final int COLOR_PICKER_SIZE = Application.isTablet() ? ColorPickerDialog.SIZE_LARGE
			: ColorPickerDialog.SIZE_SMALL;

}