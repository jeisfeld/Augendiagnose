package de.eisfeldj.augendiagnose.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.eisfeldj.augendiagnose.R;

/**
 * Array adapter class to display an eye photo pair in a list.
 */
public abstract class ListPicturesForNameBaseArrayAdapter extends ArrayAdapter<EyePhotoPair> {
	protected final Context context;

	/**
	 * Keep up to 25 rows in memory before reusing views.
	 */
	private CacheRange cacheRange = new CacheRange(25);

	protected EyePhotoPair[] eyePhotoPairs;

	public ListPicturesForNameBaseArrayAdapter(Context context, EyePhotoPair[] eyePhotoPairs) {
		super(context, R.layout.text_view_initializing, eyePhotoPairs);
		this.context = context;
		this.eyePhotoPairs = eyePhotoPairs;
	}

	public ListPicturesForNameBaseArrayAdapter(Context context) {
		super(context, R.layout.adapter_list_pictures_for_name);
		this.context = context;
	}

	/**
	 * Abstract method do return the layout to be used.
	 * 
	 * @return
	 */
	protected abstract int getLayout();

	/**
	 * Abstract method to prepare the image views for selection of pictures
	 * 
	 * @param view
	 */
	protected abstract void prepareViewForSelection(EyeImageView view);

	/**
	 * Fill the display of the view (date and pictures) Details on selection are handled within the
	 * ImageSelectionAndDisplayHandler class
	 */
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View rowView;
		// Reuse views if they are already created and in the cached range
		if (convertView != null && cacheRange.isInRange(position)) {
			rowView = convertView;
		}
		else {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(getLayout(), parent, false);
			cacheRange.putIntoRange(position);
		}

		final TextView textView = (TextView) rowView.findViewById(R.id.textPictureDate);
		textView.setText(eyePhotoPairs[position].getDateDisplayString("dd.MM.yyyy"));

		// Fill pictures in separate thread, for performance reasons
		final EyeImageView imageListRight = (EyeImageView) rowView.findViewById(R.id.imageListRight);
		if (!imageListRight.isInitialized()) {
			// Prevent duplicate initialization in case of multiple parallel calls - will happen in dialog
			imageListRight.setInitialized();
			imageListRight.post(new Runnable() {
				@Override
				public void run() {
					imageListRight.setEyePhoto(eyePhotoPairs[position].getRightEye());
					prepareViewForSelection(imageListRight);
				}
			});
		}
		final EyeImageView imageListLeft = (EyeImageView) rowView.findViewById(R.id.imageListLeft);
		if (!imageListLeft.isInitialized()) {
			imageListLeft.setInitialized();
			imageListLeft.post(new Runnable() {
				@Override
				public void run() {
					imageListLeft.setEyePhoto(eyePhotoPairs[position].getLeftEye());
					prepareViewForSelection(imageListLeft);
				}
			});
		}

		return rowView;
	}

	/**
	 * After cacheRange.length entries, the views are recycled, i.e. row cacherange.length is stored in the same view as
	 * row 0.
	 */
	@Override
	public int getItemViewType(int position) {
		return position % cacheRange.length;
	}

	@Override
	public int getViewTypeCount() {
		int count = getCount();
		return count < cacheRange.length ? count : cacheRange.length;
	}

	/**
	 * This is the range of positions for which the images are stored.
	 */
	public class CacheRange {
		private int length;
		private int start;

		public CacheRange(int length) {
			this.start = 0;
			this.length = length;
		}

		public boolean isInRange(int n) {
			return (start <= n) && (n < start + length);
		}

		public void putIntoRange(int n) {
			if (n < start) {
				start = n;
			}
			else if (n >= start + length) {
				start = n - 1 - length;
			}
		}

	}

}
