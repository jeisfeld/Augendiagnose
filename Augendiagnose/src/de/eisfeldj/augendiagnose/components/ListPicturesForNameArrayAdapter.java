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
 * Array adapter class to display an eye photo pair in a list (initial display)
 */
public class ListPicturesForNameArrayAdapter extends ListPicturesForNameBaseArrayAdapter {

	private Map<TextView, Integer> positionMap = new HashMap<TextView, Integer>();
	private ListPicturesForNameFragment fragment;

	public ListPicturesForNameArrayAdapter(Activity activity, ListPicturesForNameFragment fragment,
			EyePhotoPair[] eyePhotoPairs) {
		super(activity, eyePhotoPairs);
		this.fragment = fragment;
	}

	public ListPicturesForNameArrayAdapter(Context context) {
		super(context);
	}

	@Override
	protected int getLayout() {
		return R.layout.adapter_list_pictures_for_name;
	}

	@Override
	protected void prepareViewForSelection(EyeImageView view) {
		ImageSelectionAndDisplayHandler.getInstance().prepareViewForFirstSelection(view);
	}

	/**
	 * When clicking on date, display the two pictures of that date
	 */
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View rowView = super.getView(position, convertView, parent);

		final TextView textView = (TextView) rowView.findViewById(R.id.textPictureDate);
		textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ImageSelectionAndDisplayHandler.getInstance().cleanSelectedView();
				DisplayTwoActivity.startActivity(ListPicturesForNameArrayAdapter.this.activity, eyePhotoPairs[position]
						.getRightEye().getAbsolutePath(), eyePhotoPairs[position].getLeftEye().getAbsolutePath());

			}
		});

		positionMap.put(textView, position);

		textView.setOnCreateContextMenuListener(fragment);

		return rowView;
	}

	/**
	 * Retrieve the row corresponding to a TextView displaying the date
	 * 
	 * @param view
	 * @return
	 */
	public int getRow(TextView view) {
		return positionMap.get(view);
	}

}
