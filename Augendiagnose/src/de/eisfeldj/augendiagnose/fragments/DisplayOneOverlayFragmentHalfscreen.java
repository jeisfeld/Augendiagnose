package de.eisfeldj.augendiagnose.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;

/**
 * Variant of DisplayOneOverlayFragment that is intended for a half screen
 * 
 * @author Joerg
 */
public class DisplayOneOverlayFragmentHalfscreen extends DisplayOneOverlayFragment {

	/**
	 * Inflate View
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_display_one_overlay_halfscreen, container, false);
	}

	/**
	 * Update data from view
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (!Application.isTablet()) {
			// hide extra bars
			getView().findViewById(R.id.seekBarBrightnessLayout).setVisibility(View.GONE);
			getView().findViewById(R.id.seekBarContrastLayout).setVisibility(View.GONE);
			getView().findViewById(R.id.buttonOverlayLayout).setVisibility(View.GONE);
		}

	}

}
