package de.eisfeldj.augendiagnose.components.colorpicker;

import android.graphics.Color;
import de.eisfeldj.augendiagnose.util.SystemUtil;

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
	public static final int[] COLOR_PICKER_COLORS = { //
			Color.WHITE, Color.LTGRAY, Color.GRAY, Color.DKGRAY, Color.BLACK, // grey scales
					0xFFFF9FCF, Color.MAGENTA, 0xFF7F007F, Color.RED, 0xFF5F0000, // magenta and red
					0xFF9FCFFF, Color.CYAN, 0xFF007F7F, Color.BLUE, 0xFF00006F, // cyan and blue
					0xFFCFFF9F, Color.YELLOW, 0xFF7F7F00, Color.GREEN, 0xFF003F00, // yellow and green
					0xFFFFDF9F, 0xFFFF7F00, 0xFF4F2F0F, 0x7FFFFFFF, 0x7F000000 // orange and alpha
	};
	/**
	 * Number of columns shown in the color picker dialog.
	 */
	public static final int COLOR_PICKER_COLUMNS = 5;
	/**
	 * Size of symbols in the color picker dialog.
	 */
	public static final int COLOR_PICKER_SIZE = SystemUtil.isTablet() ? ColorPickerDialog.SIZE_LARGE
			: ColorPickerDialog.SIZE_SMALL;

}
