package de.eisfeldj.augendiagnose.components;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.util.EyePhoto;
import de.eisfeldj.augendiagnose.util.TwoImageSelectionHandler;

/**
 * Array adapter class to display an eye photo pair in a list.
 */
public class SelectTwoPicturesArrayAdapter extends ArrayAdapter<EyePhoto> {
	/**
	 * The activity holding this adapter.
	 */
	private final Activity activity;

	/**
	 * The array of eye photos displayed.
	 */
	private EyePhoto[] eyePhotos;

	/**
	 * Constructor for the adapter.
	 *
	 * @param activity
	 *            The activity using the adapter.
	 * @param eyePhotos
	 *            The array of eye photos to be displayed.
	 */
	public SelectTwoPicturesArrayAdapter(final Activity activity, final EyePhoto[] eyePhotos) {
		super(activity, R.layout.text_view_initializing, eyePhotos);
		this.activity = activity;
		this.eyePhotos = eyePhotos;
	}

	/**
	 * Default adapter to be used by the framework.
	 *
	 * @param context
	 *            The Context the view is running in.
	 */
	public SelectTwoPicturesArrayAdapter(final Context context) {
		super(context, R.layout.adapter_list_pictures_for_name);
		this.activity = (Activity) context;
	}

	/*
	 * Fill the display of the view (date and pictures) Details on selection are handled within the
	 * ImageSelectionAndDisplayHandler class.
	 */
	@Override
	public final View getView(final int position, final View convertView, final ViewGroup parent) {
		final EyeImageView eyeImageView;
		if (convertView != null && convertView instanceof EyeImageView) {
			eyeImageView = (EyeImageView) convertView;
			eyeImageView.cleanEyePhoto();
		}
		else {
			eyeImageView = (EyeImageView) LayoutInflater.from(activity).inflate(R.layout.adapter_select_two_pictures,
					parent, false);
		}

		eyeImageView.setEyePhoto(activity, eyePhotos[position], new Runnable() {
			@Override
			public void run() {
				TwoImageSelectionHandler.getInstance().highlightIfSelected(eyeImageView);
			}
		});

		return eyeImageView;
	}

}
