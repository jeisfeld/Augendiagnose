package de.eisfeldj.augendiagnose.util;

import android.app.Activity;
import android.content.Context;
import de.eisfeldj.augendiagnose.R;

/**
 * Array adapter class to display an eye photo pair in a list (display for selection of second picture)
 */
public class ListPicturesForSecondNameArrayAdapter extends ListPicturesForNameBaseArrayAdapter {

	public ListPicturesForSecondNameArrayAdapter(Activity activity, EyePhotoPair[] eyePhotoPairs) {
		super(activity, eyePhotoPairs);
	}

	public ListPicturesForSecondNameArrayAdapter(Context context) {
		super(context);
	}

	@Override
	protected int getLayout() {
		return R.layout.adapter_list_pictures_for_second_name;
	}

	@Override
	protected void prepareViewForSelection(EyeImageView view) {
		ImageSelectionAndDisplayHandler.getInstance().prepareViewForSecondSelection(view);
	}

}
