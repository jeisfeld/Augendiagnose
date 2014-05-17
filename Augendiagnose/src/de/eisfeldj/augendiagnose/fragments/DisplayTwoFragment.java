package de.eisfeldj.augendiagnose.fragments;

//require support library because nested fragments are natively supported only from API version 17.
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.eisfeldj.augendiagnose.R;

/**
 * Activity to display two pictures on full screen (screen split in two halves)
 */
public class DisplayTwoFragment extends Fragment {
	private static final String STRING_FILE1 = "de.eisfeldj.augendiagnose.FILE1";
	private static final String STRING_FILE2 = "de.eisfeldj.augendiagnose.FILE2";

	private static final String FRAGMENT_IMAGE1_TAG = "FRAGMENT_IMAGE1_TAG";
	private static final String FRAGMENT_IMAGE2_TAG = "FRAGMENT_IMAGE2_TAG";

	private String file1, file2;
	private DisplayOneFragment fragmentImage1, fragmentImage2;

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

		fragmentImage1 = (DisplayOneFragment) getChildFragmentManager().findFragmentByTag(FRAGMENT_IMAGE1_TAG);
		if (fragmentImage1 == null) {
			fragmentImage1 = createFragment();
			fragmentImage1.setParameters(file1, 1);

			getChildFragmentManager().beginTransaction().add(R.id.fragment_image1, fragmentImage1, FRAGMENT_IMAGE1_TAG)
					.commit();
		}

		fragmentImage2 = (DisplayOneFragment) getChildFragmentManager().findFragmentByTag(FRAGMENT_IMAGE2_TAG);
		if (fragmentImage2 == null) {
			fragmentImage2 = createFragment();
			fragmentImage2.setParameters(file2, 2);

			getChildFragmentManager().beginTransaction().add(R.id.fragment_image2, fragmentImage2, FRAGMENT_IMAGE2_TAG)
					.commit();
		}

		getChildFragmentManager().executePendingTransactions();
	}

	/**
	 * Initialize images - to be called after the views have restored instance state
	 */
	public void initializeImages() {
		fragmentImage1.initializeImages();
		fragmentImage2.initializeImages();
	}

	/**
	 * Factory method to return the fragment
	 * 
	 * @return
	 */
	protected DisplayOneFragment createFragment() {
		return new DisplayOneOverlayFragment();
	}

}
