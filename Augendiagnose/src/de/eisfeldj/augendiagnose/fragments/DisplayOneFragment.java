package de.eisfeldj.augendiagnose.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.components.PinchImageView;

/**
 * Activity to display one photo on full screen
 * 
 * @author Joerg
 */
public class DisplayOneFragment extends Fragment {
	protected static final String STRING_TYPE = "de.eisfeldj.augendiagnose.TYPE";
	protected static final String STRING_FILE = "de.eisfeldj.augendiagnose.FILE";
	protected static final String STRING_FILERESOURCE = "de.eisfeldj.augendiagnose.FILERESOURCE";
	protected static final int TYPE_FILENAME = 1;
	protected static final int TYPE_FILERESOURCE = 2;

	protected int type;
	protected int fileResource;
	protected String file;
	protected PinchImageView imageView;

	/**
	 * Initialize the fragment with the file name
	 * 
	 * @param text
	 * @return
	 */
	public void setParameters(String file) {
		Bundle args = new Bundle();
		args.putString(STRING_FILE, file);
		args.putInt(STRING_TYPE, TYPE_FILENAME);

		setArguments(args);
	}

	/**
	 * Initialize the fragment with the file resource
	 * 
	 * @param text
	 * @return
	 */
	public void setParameters(int fileResource) {
		Bundle args = new Bundle();
		args.putInt(STRING_FILERESOURCE, fileResource);
		args.putInt(STRING_TYPE, TYPE_FILERESOURCE);

		setArguments(args);
	}

	/**
	 * Retrieve parameters
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		type = getArguments().getInt(STRING_TYPE, -1);
		file = getArguments().getString(STRING_FILE);
		fileResource = getArguments().getInt(STRING_FILERESOURCE, -1);
	}

	/**
	 * Inflate View
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_display_one, container, false);
	}

	/**
	 * Update data from view
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		imageView = (PinchImageView) getView().findViewById(R.id.mainImage);
	}

	/**
	 * Initialize images - to be called after the views have restored instance state
	 */
	public void initializeImages() {
		if (type == TYPE_FILERESOURCE) {
			imageView.setImage(fileResource, getActivity(), 1);
		}
		else {
			imageView.setImage(file, getActivity(), 1);
		}
	}
}
