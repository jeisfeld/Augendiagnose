package de.eisfeldj.augendiagnose.components;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.activities.DisplayTwoActivity;
import de.eisfeldj.augendiagnose.fragments.ListPicturesForNameFragment;
import de.eisfeldj.augendiagnose.util.EyePhotoPair;
import de.eisfeldj.augendiagnose.util.ImageSelectionAndDisplayHandler;

/**
 * Array adapter class to display an eye photo pair in a list (initial display).
 */
public class ListPicturesForNameArrayAdapter extends ListPicturesForNameBaseArrayAdapter {
	/**
	 * A map linking the TextViews containing the date to their position.
	 */
	private Map<TextView, Integer> positionMap = new HashMap<TextView, Integer>();

	/**
	 * The fragment using the adapter.
	 */
	private ListPicturesForNameFragment fragment;

	/**
	 * Constructor for the adapter.
	 *
	 * @param activity
	 *            The activity using the adapter.
	 * @param fragment
	 *            The fragment using the adapter.
	 * @param eyePhotoPairs
	 *            The array of eye photo pairs to be displayed.
	 */
	public ListPicturesForNameArrayAdapter(final Activity activity, final ListPicturesForNameFragment fragment,
			final EyePhotoPair[] eyePhotoPairs) {
		super(activity, eyePhotoPairs);
		this.fragment = fragment;
	}

	/**
	 * Default adapter to be used by the framework.
	 *
	 * @param context
	 *            The Context the view is running in.
	 */
	public ListPicturesForNameArrayAdapter(final Context context) {
		super(context);
	}

	@Override
	protected final int getLayout() {
		return R.layout.adapter_list_pictures_for_name;
	}

	@Override
	protected final void prepareViewForSelection(final EyeImageView view) {
		ImageSelectionAndDisplayHandler.getInstance().prepareViewForFirstSelection(view);
	}

	/*
	 * When clicking on date, display the two pictures of that date.
	 */
	@Override
	public final View getView(final int position, final View convertView, final ViewGroup parent) {
		View rowView = super.getView(position, convertView, parent);

		final TextView textView = (TextView) rowView.findViewById(R.id.textPictureDate);
		textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				ImageSelectionAndDisplayHandler.getInstance().cleanSelectedViews();
				DisplayTwoActivity.startActivity(ListPicturesForNameArrayAdapter.this.activity, eyePhotoPairs[position]
						.getRightEye().getAbsolutePath(), eyePhotoPairs[position].getLeftEye().getAbsolutePath());

			}
		});

		positionMap.put(textView, position);

		textView.setOnCreateContextMenuListener(fragment);

		return rowView;
	}

	/**
	 * Retrieve the row corresponding to a TextView displaying the date.
	 *
	 * @param view
	 *            The TextView displaying the date.
	 * @return The row
	 */
	public final int getRow(final TextView view) {
		return positionMap.get(view);
	}

}
