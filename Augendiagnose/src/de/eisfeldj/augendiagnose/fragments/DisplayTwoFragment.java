package de.eisfeldj.augendiagnose.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.components.PinchImageView;

/**
 * Activity to display two pictures on full screen (screen split in two halves)
 */
public class DisplayTwoFragment extends Fragment {
	private static final String STRING_FILE1 = "de.eisfeldj.augendiagnose.FILE1";
	private static final String STRING_FILE2 = "de.eisfeldj.augendiagnose.FILE2";

	private String file1, file2;
	private PinchImageView imageView1, imageView2;

	/**
	 * Initialize the fragment with the file names
	 * 
	 * @param text
	 * @return
	 */
	public void setParameters(String file1, String file2) {
		Bundle args = new Bundle();
		args.putString(STRING_FILE1, file1);
		args.putString(STRING_FILE2, file2);

		setArguments(args);
	}

	/**
	 * Retrieve parameters
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		file1 = getArguments().getString(STRING_FILE1);
		file2 = getArguments().getString(STRING_FILE2);
	}

	/**
	 * Inflate View
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_display_two, container, false);
	}

	/**
	 * Update data from view
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		imageView1 = (PinchImageView) getView().findViewById(R.id.mainImage1);
		imageView2 = (PinchImageView) getView().findViewById(R.id.mainImage2);
	}

	/**
	 * Initialize images - to be called after the views have restored instance state
	 */
	public void initializeImages() {
		imageView1.setImage(file1, getActivity(), 1);
		imageView2.setImage(file2, getActivity(), 2);
	}

}
