package de.jeisfeld.augendiagnoselib.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.activities.SelectTwoPicturesActivity;
import de.jeisfeld.augendiagnoselib.util.TwoImageSelectionHandler;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto;

/**
 * Array adapter class to display a list of eye photos in order to select two of them.
 */
public class SelectTwoPicturesArrayAdapter extends ArrayAdapter<EyePhoto> {
	/**
	 * The activity holding this adapter.
	 */
	private final SelectTwoPicturesActivity mActivity;

	/**
	 * The array of eye photos displayed.
	 */
	private EyePhoto[] mEyePhotos;

	/**
	 * Constructor for the adapter.
	 *
	 * @param activity  The activity using the adapter.
	 * @param eyePhotos The array of eye photos to be displayed.
	 */
	public SelectTwoPicturesArrayAdapter(final SelectTwoPicturesActivity activity, final EyePhoto[] eyePhotos) {
		super(activity, R.layout.text_view_initializing, eyePhotos);
		this.mActivity = activity;
		this.mEyePhotos = eyePhotos;
	}

	/**
	 * Default adapter to be used by the framework.
	 *
	 * @param context The Context the view is running in.
	 */
	public SelectTwoPicturesArrayAdapter(final Context context) {
		super(context, R.layout.text_view_initializing);
		this.mActivity = (SelectTwoPicturesActivity) context;
	}

	/*
	 * Fill the display of the view (date and pictures) Details on selection are handled within the
	 * TwoImageSelectionHandler class.
	 */
	@Override
	public final View getView(final int position, final View convertView, final ViewGroup parent) {
		final EyeImageView eyeImageView;
		if (convertView != null && convertView instanceof EyeImageView) {
			eyeImageView = (EyeImageView) convertView;
			eyeImageView.cleanEyePhoto();
		}
		else {
			eyeImageView = (EyeImageView) LayoutInflater.from(mActivity).inflate(R.layout.adapter_select_two_pictures,
					parent, false);
		}

		eyeImageView.setEyePhoto(mActivity, mEyePhotos[position], new Runnable() {
			@Override
			public void run() {
				TwoImageSelectionHandler.getInstance().highlightIfSelected(eyeImageView);
			}
		});

		TwoImageSelectionHandler.getInstance().prepareViewForSelection(eyeImageView,
				mActivity.isStartedWithInputFolder());

		return eyeImageView;
	}

}
