package de.jeisfeld.augendiagnoselib.fragments;

import java.io.File;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.SystemUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhotoPair;
import de.jeisfeld.augendiagnoselib.util.imagefile.ImageUtil;

/**
 * Base listFoldersFragment to display the pictures in an eye photo folder (in pairs) Abstract class - child classes
 * determine the detailed actions.
 */
public abstract class ListPicturesForNameBaseFragment extends Fragment {
	/**
	 * The resource key of the name.
	 */
	private static final String STRING_NAME = "de.jeisfeld.augendiagnoselib.NAME";
	/**
	 * The resource key of the parent folder.
	 */
	private static final String STRING_PARENTFOLDER = "de.jeisfeld.augendiagnoselib.PARENTFOLDER";

	/**
	 * The parent folder.
	 */
	private String parentFolder;
	/**
	 * The name for which the eye photos should be displayed.
	 */
	private String name;

	/**
	 * Flag indicating that the fragment should be dismissed because there are no pictures.
	 */
	private boolean dismiss = false;

	/**
	 * The list view showing the pictures.
	 */
	private ListView listview;

	/**
	 * The array of eye photo pairs.
	 */
	private EyePhotoPair[] eyePhotoPairs;

	/**
	 * Initialize the listFoldersFragment with parentFolder and name.
	 *
	 * @param initialParentFolder
	 *            The parent folder
	 * @param initialName
	 *            the name
	 */
	public final void setParameters(final String initialParentFolder, final String initialName) {
		Bundle args = new Bundle();
		args.putString(STRING_PARENTFOLDER, initialParentFolder);
		args.putString(STRING_NAME, initialName);

		setArguments(args);
	}

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();

		name = args.getString(STRING_NAME);
		parentFolder = args.getString(STRING_PARENTFOLDER);
	}

	// OVERRIDABLE
	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		TextView headerNameView = (TextView) getView().findViewById(R.id.textTitleName);
		headerNameView.setText(name);

		createAndStoreEyePhotoList(true);

		listview = (ListView) getView().findViewById(R.id.listViewForName);

		// prevent highlighting
		listview.setCacheColorHint(Color.TRANSPARENT);
		listview.setSelector(new StateListDrawable());
	}

	/**
	 * Create the list of eye photo pairs for display. Photos are arranged in pairs (right-left) by date.
	 *
	 * @param folder
	 *            the folder where the photos are located.
	 * @return The list of eye photo pairs.
	 */
	private EyePhotoPair[] createEyePhotoList(final File folder) {
		Map<Date, EyePhotoPair> eyePhotoMap = new TreeMap<Date, EyePhotoPair>();

		File[] files = folder.listFiles(new ImageUtil.ImageFileFilter());

		if (files == null) {
			return new EyePhotoPair[0];
		}

		for (File f : files) {
			EyePhoto eyePhoto = new EyePhoto(f);

			if (eyePhoto.isFormatted()) {
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
			else {
				DialogUtil.displayError(getActivity(), R.string.message_dialog_unformatted_file, false,
						f.getAbsolutePath());
			}
		}

		// Remove incomplete pairs - need duplication to avoid ConcurrentModificationException
		Map<Date, EyePhotoPair> eyePhotoMap2 = new TreeMap<Date, EyePhotoPair>(new Comparator<Date>() {
			@Override
			public int compare(final Date lhs, final Date rhs) {
				return rhs.compareTo(lhs);
			}
		});

		for (Date date : eyePhotoMap.keySet()) {
			if (eyePhotoMap.get(date).isComplete()) {
				eyePhotoMap2.put(date, eyePhotoMap.get(date));
			}
		}

		return eyePhotoMap2.values().toArray(new EyePhotoPair[eyePhotoMap2.size()]);
	}

	/**
	 * Create the list of eye photo pairs and store them.
	 *
	 * @param dismissIfEmpty
	 *            Dismiss fragment in case of no photos?
	 *
	 * @return true if there are still eye photos remaining.
	 */
	protected final boolean createAndStoreEyePhotoList(final boolean dismissIfEmpty) {
		eyePhotoPairs = createEyePhotoList(new File(parentFolder, name));

		if (eyePhotoPairs == null || eyePhotoPairs.length == 0) {
			DialogUtil.displayError(getActivity(), R.string.message_dialog_no_photos_for_name, !SystemUtil.isTablet(),
					name);
			if (dismissIfEmpty) {
				dismiss = true;
			}
			return false;
		}
		return true;
	}

	public final String getParentFolder() {
		return parentFolder;
	}

	public final String getName() {
		return name;
	}

	protected final boolean isDismiss() {
		return dismiss;
	}

	protected final ListView getListView() {
		return listview;
	}

	protected final EyePhotoPair[] getEyePhotoPairs() {
		return eyePhotoPairs;
	}

}
