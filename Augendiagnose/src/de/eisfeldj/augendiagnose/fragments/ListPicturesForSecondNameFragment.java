package de.eisfeldj.augendiagnose.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.components.ListPicturesForSecondNameArrayAdapter;

/**
 * Fragment to display the pictures in an eye photo folder (in pairs) This is for the selection of a second picture for
 * display.
 */
public class ListPicturesForSecondNameFragment extends ListPicturesForNameBaseFragment {
	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_list_pictures_for_second_name, container, false);
	}

	@Override
	public final void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (isDismiss()) {
			return;
		}

		getListView().setAdapter(new ListPicturesForSecondNameArrayAdapter(getActivity(), getEyePhotoPairs()));
	}

}
