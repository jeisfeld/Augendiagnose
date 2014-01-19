package de.eisfeldj.augendiagnose.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.activities.DisplayTwoActivity;

/**
 * Array adapter class to display an eye photo pair in a list (initial display)
 */
public class ListPicturesForNameArrayAdapter extends ListPicturesForNameBaseArrayAdapter {

	public ListPicturesForNameArrayAdapter(Context context, EyePhotoPair[] eyePhotoPairs) {
		super(context, eyePhotoPairs);
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
				DisplayTwoActivity.startActivity(ListPicturesForNameArrayAdapter.this.context, eyePhotoPairs[position]
						.getRightEye().getAbsolutePath(), eyePhotoPairs[position].getLeftEye().getAbsolutePath());

			}
		});
		return rowView;
	}

}
