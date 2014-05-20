package de.eisfeldj.augendiagnose.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;

/**
 * Variant of DisplayImageFragment that is intended for a half screen
 * 
 * @author Joerg
 */
public class DisplayImageFragmentHalfscreen extends DisplayImageFragment {

	/**
	 * Inflate View
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (Application.isLandscape()) {
			return inflater.inflate(R.layout.fragment_display_image_portrait, container, false);
		}
		else {
			return inflater.inflate(R.layout.fragment_display_image_landscape, container, false);
		}
	}

	/**
	 * Return the level from which on the utilities are shown. 1 means: don't show. 2 means: show only on full screen. 3
	 * means: show always.
	 * 
	 * @return
	 */
	@Override
	protected int getShowUtilitiesLimitLevel() {
		return 3;
	}

}
