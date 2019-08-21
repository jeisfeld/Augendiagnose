package de.jeisfeld.augendiagnoselib.components;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.util.ImageSelectionAndDisplayHandler;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhotoPair;

/**
 * Array adapter class to display an eye photo pair in a list (display for selection of second picture).
 */
public class ListPicturesForSecondNameArrayAdapter extends ListPicturesForNameBaseArrayAdapter {

	/**
	 * Constructor for the adapter.
	 *
	 * @param activity      The activity using the adapter.
	 * @param eyePhotoPairs The array of eye photo pairs to be displayed.
	 */
	public ListPicturesForSecondNameArrayAdapter(final Activity activity, @NonNull final EyePhotoPair[] eyePhotoPairs) {
		super(activity, eyePhotoPairs);
	}

	/**
	 * Default adapter to be used by the framework.
	 *
	 * @param context The Context the view is running in.
	 */
	public ListPicturesForSecondNameArrayAdapter(final Context context) {
		super(context);
	}

	@Override
	protected final int getLayout() {
		return R.layout.adapter_list_pictures_for_second_name;
	}

	@Override
	protected final void prepareViewForSelection(@NonNull final EyeImageView view) {
		ImageSelectionAndDisplayHandler.getInstance().prepareViewForSecondSelection(view);
	}

}
