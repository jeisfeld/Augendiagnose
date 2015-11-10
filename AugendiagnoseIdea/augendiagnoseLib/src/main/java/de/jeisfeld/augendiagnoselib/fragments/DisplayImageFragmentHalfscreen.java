package de.jeisfeld.augendiagnoselib.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.util.SystemUtil;

/**
 * Variant of DisplayImageFragment that is intended for a half screen.
 *
 * @author Joerg
 */
public class DisplayImageFragmentHalfscreen extends DisplayImageFragment {

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
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
	protected final int getShowUtilitiesLimitLevel() {
		return UTILITIES_SHOW_ALWAYS;
	}

}
