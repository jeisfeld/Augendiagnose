package de.eisfeldj.augendiagnose.fragments;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.util.DialogUtil;
import de.eisfeldj.augendiagnose.util.EyePhoto;
import de.eisfeldj.augendiagnose.util.EyePhotoPair;
import de.eisfeldj.augendiagnose.util.ImageUtil;

/**
 * Base fragment to display the pictures in an eye photo folder (in pairs) Abstract class - child classes determine the
 * detailed actions.
 */
public abstract class ListPicturesForNameBaseFragment extends Fragment {
	private static final String STRING_NAME = "de.eisfeldj.augendiagnose.NAME";
	private static final String STRING_PARENTFOLDER = "de.eisfeldj.augendiagnose.PARENTFOLDER";

	public String parentFolder;
	protected String name;
	protected boolean dismiss = false;

	protected ListView listview;
	protected EyePhotoPair[] eyePhotoPairs;

	/**
	 * Initialize the fragment with parentFolder and name
	 * 
	 * @param parentFolder
	 * @param name
	 * @return
	 */
	public void setParameters(String parentFolder, String name) {
		Bundle args = new Bundle();
		args.putString(STRING_PARENTFOLDER, parentFolder);
		args.putString(STRING_NAME, name);

		setArguments(args);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();

		name = args.getString(STRING_NAME);
		parentFolder = args.getString(STRING_PARENTFOLDER);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		TextView headerNameView = (TextView) getView().findViewById(R.id.textTitleName);
		headerNameView.setText(name);

		eyePhotoPairs = createEyePhotoList(new File(parentFolder, name));

		if (eyePhotoPairs == null || eyePhotoPairs.length == 0) {
			DialogUtil.displayErrorAndReturn(getActivity(), R.string.message_dialog_no_photos_for_name, name);
			dismiss = true;
			return;
		}

		listview = (ListView) getView().findViewById(R.id.listViewForName);

		// prevent highlighting
		listview.setCacheColorHint(Color.TRANSPARENT);
		listview.setSelector(new StateListDrawable());
	}

	/**
	 * Create the list of eye photo pairs for display. Photos are arranged in pairs (right-left) by date.
	 * 
	 * @param the
	 *            folder where the photos are located.
	 * @return
	 */
	protected EyePhotoPair[] createEyePhotoList(File folder) {
		Map<Date, EyePhotoPair> eyePhotoMap = new TreeMap<Date, EyePhotoPair>();

		File[] files = folder.listFiles(new ImageUtil.ImageFileFilter());

		if (files == null) {
			return new EyePhotoPair[0];
		}

		for (File f : files) {
			EyePhoto eyePhoto = new EyePhoto(f);

			if (!eyePhoto.isFormatted()) {
				DialogUtil.displayError(getActivity(), R.string.message_dialog_unformatted_file, f.getAbsolutePath());
			}
			else {
				Date date = eyePhoto.getDate();

				if (eyePhotoMap.containsKey(date)) {
					EyePhotoPair eyePhotoPair = eyePhotoMap.get(date);
					eyePhotoPair.setEyePhoto(eyePhoto);
				}
				else {
					EyePhotoPair eyePhotoPair = new EyePhotoPair();
					eyePhotoPair.setEyePhoto(eyePhoto);
					eyePhotoMap.put(date, eyePhotoPair);
				}
			}

		}

		// Remove incomplete pairs - need duplication to avoid ConcurrentModificationException
		Map<Date, EyePhotoPair> eyePhotoMap2 = new TreeMap<Date, EyePhotoPair>();
		for (Date date : eyePhotoMap.keySet()) {
			if (eyePhotoMap.get(date).isComplete()) {
				eyePhotoMap2.put(date, eyePhotoMap.get(date));
			}
		}

		EyePhotoPair[] eyePhotoPairs = eyePhotoMap2.values().toArray(new EyePhotoPair[0]);

		return eyePhotoPairs;
	}

}
