package de.eisfeldj.augendiagnose.components;

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
	protected final Context context;

	protected EyePhoto[] eyePhotos;

	public SelectTwoPicturesArrayAdapter(Context context, EyePhoto[] eyePhotos) {
		super(context, R.layout.text_view_initializing, eyePhotos);
		this.context = context;
		this.eyePhotos = eyePhotos;
	}

	public SelectTwoPicturesArrayAdapter(Context context) {
		super(context, R.layout.adapter_list_pictures_for_name);
		this.context = context;
	}

	/**
	 * Fill the display of the view (date and pictures) Details on selection are handled within the
	 * ImageSelectionAndDisplayHandler class
	 */
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final EyeImageView eyeImageView = (EyeImageView) LayoutInflater.from(context).inflate(
				R.layout.adapter_select_two_pictures, parent, false);

		eyeImageView.post(new Runnable() {
			@Override
			public void run() {
				eyeImageView.setEyePhoto(eyePhotos[position]);
				TwoImageSelectionHandler.getInstance().highlightIfSelected(eyeImageView);
			}
		});

		return eyeImageView;
	}

}
