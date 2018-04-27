package de.jeisfeld.augendiagnoselib.components;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.activities.DisplayOneActivity;
import de.jeisfeld.augendiagnoselib.activities.DisplayTwoActivity;
import de.jeisfeld.augendiagnoselib.fragments.ListPicturesForNameFragment;
import de.jeisfeld.augendiagnoselib.util.ImageSelectionAndDisplayHandler;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhotoPair;

/**
 * Array adapter class to display an eye photo pair in a list (initial display).
 */
public class ListPicturesForNameArrayAdapter extends ListPicturesForNameBaseArrayAdapter {
	/**
	 * A map linking the TextViews containing the date to their position.
	 */
	private final Map<TextView, Integer> mPositionMap = new HashMap<>();

	/**
	 * The fragment using the adapter.
	 */
	private ListPicturesForNameFragment mFragment;

	/**
	 * Constructor for the adapter.
	 *
	 * @param activity      The activity using the adapter.
	 * @param fragment      The fragment using the adapter.
	 * @param eyePhotoPairs The array of eye photo pairs to be displayed.
	 */
	public ListPicturesForNameArrayAdapter(final Activity activity, final ListPicturesForNameFragment fragment,
										   final EyePhotoPair[] eyePhotoPairs) {
		super(activity, eyePhotoPairs);
		this.mFragment = fragment;
	}

	/**
	 * Default adapter to be used by the framework.
	 *
	 * @param context The Context the view is running in.
	 */
	public ListPicturesForNameArrayAdapter(final Context context) {
		super(context);
	}

	@Override
	protected final int getLayout() {
		return R.layout.adapter_list_pictures_for_name;
	}

	@Override
	protected final void prepareViewForSelection(@NonNull final EyeImageView view) {
		ImageSelectionAndDisplayHandler.getInstance().prepareViewForFirstSelection(view);
	}

	/*
	 * When clicking on date, display the two pictures of that date.
	 */
	@Override
	public final View getView(final int position, final View convertView, final ViewGroup parent) {
		View rowView = super.getView(position, convertView, parent);

		final TextView textView = rowView.findViewById(R.id.textPictureDate);
		textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				ImageSelectionAndDisplayHandler.getInstance().cleanSelectedViews();
				if (mEyePhotoPairs[position].isComplete()) {
					DisplayTwoActivity
							.startActivity(ListPicturesForNameArrayAdapter.this.mActivity, mEyePhotoPairs[position]
									.getRightEye().getAbsolutePath(), mEyePhotoPairs[position].getLeftEye()
									.getAbsolutePath(), true);
				}
				else if (mEyePhotoPairs[position].getRightEye() != null) {
					DisplayOneActivity.startActivity(ListPicturesForNameArrayAdapter.this.mActivity,
							mEyePhotoPairs[position].getRightEye().getAbsolutePath());
				}
				else if (mEyePhotoPairs[position].getLeftEye() != null) {
					DisplayOneActivity.startActivity(ListPicturesForNameArrayAdapter.this.mActivity,
							mEyePhotoPairs[position].getLeftEye().getAbsolutePath());
				}
			}
		});

		mPositionMap.put(textView, position);

		textView.setOnCreateContextMenuListener(mFragment);

		return rowView;
	}

	/**
	 * Retrieve the row corresponding to a TextView displaying the date.
	 *
	 * @param view The TextView displaying the date.
	 * @return The row
	 */
	public final int getRow(final TextView view) {
		return mPositionMap.get(view);
	}

}
