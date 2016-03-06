package de.jeisfeld.augendiagnoselib.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.SystemUtil;

/**
 * Variant of DisplayImageFragment that is intended for a half screen.
 *
 * @author Joerg
 */
public class DisplayImageFragmentHalfscreen extends DisplayImageFragment {

	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
								   final Bundle savedInstanceState) {
		if (SystemUtil.isLandscape()) {
			setLandscape(false);
			return inflater.inflate(R.layout.fragment_display_image_portrait, container, false);
		}
		else {
			setLandscape(true);
			return inflater.inflate(R.layout.fragment_display_image_landscape, container, false);
		}
	}

	@Override
	protected final UtilitiyStatus getDefaultShowUtilitiesValue() {
		UtilitiyStatus level = UtilitiyStatus.fromResourceValue(
				PreferenceUtil.getSharedPreferenceInt(R.string.key_internal_show_utilities_halfscreen, -1));

		if (level == null) {
			// call this method only if no value is set
			level = SystemUtil.isTablet() ? UtilitiyStatus.SHOW_EVERYTHING : UtilitiyStatus.SHOW_NOTHING;
		}

		return level;
	}

	@Override
	protected final void updateDefaultShowUtilities(final UtilitiyStatus utilityStatus) {
		PreferenceUtil.setSharedPreferenceInt(R.string.key_internal_show_utilities_halfscreen, utilityStatus.getNumericValue());
	}

	@Override
	protected final boolean alwaysShowOverlayBar() {
		return SystemUtil.isTablet() || getOverlayStatus() == OverlayStatus.GUIDE_IRIS || getOverlayStatus() == OverlayStatus.GUIDE_PUPIL;
	}

	@Override
	protected final boolean allowAllBars() {
		return SystemUtil.isTablet();
	}

}
