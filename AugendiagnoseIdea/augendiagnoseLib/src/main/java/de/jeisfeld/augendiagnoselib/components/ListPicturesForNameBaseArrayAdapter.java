package de.jeisfeld.augendiagnoselib.components;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhotoPair;

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
	private final CacheRange mCacheRange = new CacheRange(CACHE_SIZE);

	// PUBLIC_FIELDS:START
	/**
	 * A reference to the activity.
	 */
	protected final Activity mActivity;

	/**
	 * The list of eye photo pairs.
	 */
	protected EyePhotoPair[] mEyePhotoPairs;

	// PUBLIC_FIELDS:END

	/**
	 * Constructor for the adapter.
	 *
	 * @param activity      The activity using the adapter.
	 * @param eyePhotoPairs The array of eye photo pairs to be displayed.
	 */
	public ListPicturesForNameBaseArrayAdapter(final Activity activity, @NonNull final EyePhotoPair[] eyePhotoPairs) {
		super(activity, R.layout.text_view_initializing, eyePhotoPairs);
		this.mActivity = activity;
		this.mEyePhotoPairs = eyePhotoPairs;
	}

	/**
	 * Default adapter to be used by the framework.
	 *
	 * @param context The Context the view is running in.
	 */
	public ListPicturesForNameBaseArrayAdapter(final Context context) {
		super(context, R.layout.adapter_list_pictures_for_name);
		this.mActivity = (Activity) context;
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
	 * @param view The image view to be prepared.
	 */
	protected abstract void prepareViewForSelection(EyeImageView view);

	/*
	 * Fill the display of the view (date and pictures) Details on selection are handled within the
	 * ImageSelectionAndDisplayHandler class
	 */
	// OVERRIDABLE
	@Nullable
	@Override
	public View getView(final int position, @Nullable final View convertView, final ViewGroup parent) {
		View rowView;

		// Reuse views if they are already created and in the cached range
		if (convertView != null && mCacheRange.isInRange(position)) {
			rowView = convertView;
		}
		else {
			rowView = LayoutInflater.from(mActivity).inflate(getLayout(), parent, false);
			mCacheRange.putIntoRange(position);
		}

		final TextView textView = rowView.findViewById(R.id.textPictureDate);
		textView.setText(mEyePhotoPairs[position].getDateDisplayString("dd.MM.yyyy"));

		// Fill pictures in separate thread, for performance reasons
		final EyeImageView imageListRight = rowView.findViewById(R.id.imageListRight);
		if (!imageListRight.isInitialized() && mEyePhotoPairs[position].getRightEye() != null) {
			// Prevent duplicate initialization in case of multiple parallel calls - will happen in dialog
			imageListRight.setInitialized();
			imageListRight.setEyePhoto(mActivity, mEyePhotoPairs[position].getRightEye(), new Runnable() {
				@Override
				public void run() {
					prepareViewForSelection(imageListRight);
				}
			});
		}
		final EyeImageView imageListLeft = rowView.findViewById(R.id.imageListLeft);
		if (!imageListLeft.isInitialized() && mEyePhotoPairs[position].getLeftEye() != null) {
			imageListLeft.setInitialized();
			imageListLeft.setEyePhoto(mActivity, mEyePhotoPairs[position].getLeftEye(), new Runnable() {
				@Override
				public void run() {
					prepareViewForSelection(imageListLeft);
				}
			});
		}

		return rowView;
	}

	/*
	 * After cacheRange.length entries, the views are recycled, i.e. row cacheRange.length is stored in the same view as
	 * row 0.
	 */
	@Override
	public final int getItemViewType(final int position) {
		return position % mCacheRange.mLength;
	}

	@Override
	public final int getViewTypeCount() {
		int count = getCount();
		if (count == 0) {
			return 1;
		}
		else if (count < mCacheRange.mLength) {
			return count;
		}
		else {
			return mCacheRange.mLength;
		}
	}

	/**
	 * This is the range of positions for which the images are stored.
	 */
	private static final class CacheRange {
		/**
		 * Length of the cache.
		 */
		private final int mLength;
		/**
		 * Start position of the cache. Moves to ensure that the current pointer is always within the cache.
		 */
		private int mStart;

		/**
		 * Initialize the cache with a given length.
		 *
		 * @param length The length of the cache.
		 */
		private CacheRange(final int length) {
			this.mStart = 0;
			this.mLength = length;
		}

		/**
		 * Check if a given number is within the range of the cache.
		 *
		 * @param n The number to be checked.
		 * @return True if the number is in the cache.
		 */
		private boolean isInRange(final int n) {
			return (mStart <= n) && (n < mStart + mLength);
		}

		@NonNull
		@Override
		public String toString() {
			return "[" + mStart + "," + (mStart + mLength - 1) + "]";
		}

		/**
		 * Push a given number into the cache. The start position of the cache is adapted accordingly.
		 *
		 * @param n The number to be pushed.
		 */
		private void putIntoRange(final int n) {
			if (n < mStart) {
				mStart = n;
			}
			else if (n >= mStart + mLength) {
				mStart = n + 1 - mLength;
			}
		}

	}

}
