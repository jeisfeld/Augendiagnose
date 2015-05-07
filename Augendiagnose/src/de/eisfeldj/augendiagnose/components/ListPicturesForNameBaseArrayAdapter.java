package de.eisfeldj.augendiagnose.components;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.util.EyePhotoPair;

/**
 * Array adapter class to display an eye photo pair in a list.
 */
public abstract class ListPicturesForNameBaseArrayAdapter extends ArrayAdapter<EyePhotoPair> {
	/**
	 * The cache size.
	 */
	private static final int CACHE_SIZE = 25;

	/**
	 * Keep up to 25 rows in memory before reusing views.
	 */
	private CacheRange cacheRange = new CacheRange(CACHE_SIZE);

	// PUBLIC_FIELDS:START
	/**
	 * A reference to the activity.
	 */
	protected final Activity activity;

	/**
	 * The list of eye photo pairs.
	 */
	protected EyePhotoPair[] eyePhotoPairs;

	// PUBLIC_FIELDS:END

	/**
	 * Constructor for the adapter.
	 *
	 * @param activity The activity using the adapter.
	 * @param eyePhotoPairs The array of eye photo pairs to be displayed.
	 */
	public ListPicturesForNameBaseArrayAdapter(final Activity activity, final EyePhotoPair[] eyePhotoPairs) {
		super(activity, R.layout.text_view_initializing, eyePhotoPairs);
		this.activity = activity;
		this.eyePhotoPairs = eyePhotoPairs;
	}

	/**
	 * Default adapter to be used by the framework.
	 *
	 * @param context The Context the view is running in.
	 */
	public ListPicturesForNameBaseArrayAdapter(final Context context) {
		super(context, R.layout.adapter_list_pictures_for_name);
		this.activity = (Activity) context;
	}

	/**
	 * Abstract method do return the layout to be used.
	 *
	 * @return The layout to be used.
	 */
	protected abstract int getLayout();

	/**
	 * Abstract method to prepare the image views for selection of pictures.
	 *
	 * @param view
	 *            The image view to be prepared.
	 */
	protected abstract void prepareViewForSelection(EyeImageView view);

	/*
	 * Fill the display of the view (date and pictures) Details on selection are handled within the
	 * ImageSelectionAndDisplayHandler class
	 */
	// OVERRIDABLE
	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View rowView;
		// Reuse views if they are already created and in the cached range
		if (convertView != null && cacheRange.isInRange(position)) {
			rowView = convertView;
		}
		else {
			rowView = LayoutInflater.from(activity).inflate(getLayout(), parent, false);
			cacheRange.putIntoRange(position);
		}

		final TextView textView = (TextView) rowView.findViewById(R.id.textPictureDate);
		textView.setText(eyePhotoPairs[position].getDateDisplayString());

		// Fill pictures in separate thread, for performance reasons
		final EyeImageView imageListRight = (EyeImageView) rowView.findViewById(R.id.imageListRight);
		if (!imageListRight.isInitialized()) {
			// Prevent duplicate initialization in case of multiple parallel calls - will happen in dialog
			imageListRight.setInitialized();
			imageListRight.setEyePhoto(activity, eyePhotoPairs[position].getRightEye(), new Runnable() {
				@Override
				public void run() {
					prepareViewForSelection(imageListRight);
				}
			});
		}
		final EyeImageView imageListLeft = (EyeImageView) rowView.findViewById(R.id.imageListLeft);
		if (!imageListLeft.isInitialized()) {
			imageListLeft.setInitialized();
			imageListLeft.setEyePhoto(activity, eyePhotoPairs[position].getLeftEye(), new Runnable() {
				@Override
				public void run() {
					prepareViewForSelection(imageListLeft);
				}
			});
		}

		return rowView;
	}

	/*
	 * After cacheRange.length entries, the views are recycled, i.e. row cacheange.length is stored in the same view as
	 * row 0.
	 */
	@Override
	public final int getItemViewType(final int position) {
		return position % cacheRange.length;
	}

	@Override
	public final int getViewTypeCount() {
		int count = getCount();
		return count < cacheRange.length ? count : cacheRange.length;
	}

	/**
	 * This is the range of positions for which the images are stored.
	 */
	public static class CacheRange {
		/**
		 * Length of the cache.
		 */
		private int length;
		/**
		 * Start position of the cache. Moves to ensure that the current pointer is always within the cache.
		 */
		private int start;

		/**
		 * Initialize the cache with a given length.
		 *
		 * @param length
		 *            The length of the cache.
		 */
		public CacheRange(final int length) {
			this.start = 0;
			this.length = length;
		}

		/**
		 * Check if a given number is within the range of the cache.
		 *
		 * @param n
		 *            The number to be checked.
		 * @return True if the number is in the cache.
		 */
		public final boolean isInRange(final int n) {
			return (start <= n) && (n < start + length);
		}

		/**
		 * Push a given number into the cache. The start position of the cache is adapted accordingly.
		 *
		 * @param n
		 *            The number to be pushed.
		 */
		public final void putIntoRange(final int n) {
			if (n < start) {
				start = n;
			}
			else if (n >= start + length) {
				start = n - 1 - length;
			}
		}

	}

}
