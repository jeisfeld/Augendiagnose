package de.eisfeldj.augendiagnose.activities;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.util.DialogUtil;
import de.eisfeldj.augendiagnose.util.EyePhoto;
import de.eisfeldj.augendiagnose.util.EyePhotoPair;

/**
 * Base activity to display the pictures in an eye photo folder (in pairs)
 * Abstract class - child classes determine the detailed actions.
 */
public abstract class ListPicturesForNameBaseActivity extends Activity {
	private static final String STRING_EXTRA_NAME = "de.eisfeldj.augendiagnose.NAME";
	private static final String STRING_EXTRA_PARENTFOLDER = "de.eisfeldj.augendiagnose.PARENTFOLDER";

	protected String parentFolder;

	protected ListView listview;
	protected EyePhotoPair[] eyePhotoPairs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getContentView());

		String name = getIntent().getStringExtra(STRING_EXTRA_NAME);
		parentFolder = getIntent().getStringExtra(STRING_EXTRA_PARENTFOLDER);

		TextView headerNameView = (TextView) findViewById(R.id.textTitleName);
		headerNameView.setText(name);

		eyePhotoPairs = createEyePhotoList(new File(parentFolder, name));

		listview = (ListView) findViewById(R.id.listViewForName);
		
		// prevent highlighting
		listview.setCacheColorHint(Color.TRANSPARENT);
		listview.setSelector(new StateListDrawable());
	}
	
	/**
	 * Abstract method to be overriden - should return the view to be used for display.
	 */
	protected abstract int getContentView();

	/**
	 * Create the list of eye photo pairs for display. Photos are arranged in pairs (right-left) by date.
	 * @param the folder where the photos are located.
	 * @return
	 */
	private EyePhotoPair[] createEyePhotoList(File folder) {
		Map<Date, EyePhotoPair> eyePhotoMap = new TreeMap<Date, EyePhotoPair>();

		File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().toUpperCase(Locale.getDefault()).endsWith(".JPG");
			}
		});

		for (File f : files) {
			EyePhoto eyePhoto = new EyePhoto(f);

			if (!eyePhoto.isFormatted()) {
				DialogUtil.displayError(this, R.string.message_dialog_unformatted_file, f.getAbsolutePath());
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

		EyePhotoPair[] eyePhotoPairs = eyePhotoMap.values().toArray(new EyePhotoPair[0]);

		return eyePhotoPairs;
	}

}
