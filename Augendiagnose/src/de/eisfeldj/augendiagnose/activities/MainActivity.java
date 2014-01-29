package de.eisfeldj.augendiagnose.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.util.DialogUtil;
import de.eisfeldj.augendiagnose.util.MediaStoreUtil;

/**
 * Main activity of the application
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SettingsActivity.setDefaultSharedPreferences();

		Intent intent = getIntent();
		if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction()) && intent.getType() != null) {
			// Application was started from other application by passing a list of images - open
			// OrganizeNewPhotosActivity.
			if (intent.getType().startsWith("image/")) {
				ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
				if (imageUris != null) {
					String[] fileNames = new String[imageUris.size()];
					for (int i = 0; i < imageUris.size(); i++) {
						fileNames[i] = MediaStoreUtil.getRealPathFromURI(imageUris.get(i));
						if(fileNames[i]==null) {
							fileNames[i] = imageUris.get(i).getPath();
						}
					}
					boolean rightEyeLast = Application.getSharedPreferenceBoolean(R.string.key_eye_sequence_choice);
					OrganizeNewPhotosActivity.startActivity(this, fileNames,
							Application.getSharedPreferenceString(R.string.key_folder_photos), rightEyeLast);
				}
			}
		}
	}

	/**
	 * Inflate options menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Open the settings screen
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			SettingsActivity.startActivity(this);
			break;
		case R.id.action_help:
			DisplayHtmlActivity.startActivity(this, R.string.html_overview);
			break;
		}
		return true;
	}

	/**
	 * onClick action for Button to open the Eye-Fi app
	 * 
	 * @param view
	 */
	public void openEyeFiApp(View view) {
		Intent launchIntent = getPackageManager().getLaunchIntentForPackage("fi.eye.android");
		if (launchIntent != null) {
			startActivity(launchIntent);
		}
		else {
			launchIntent = new Intent(Intent.ACTION_VIEW);
			launchIntent.setData(Uri.parse("market://details?id=fi.eye.android"));
			try {
				startActivity(launchIntent);
			}
			catch (Exception e) {
				DialogUtil.displayError(this, R.string.message_dialog_eyefi_not_installed);
			}
		}
	}

	/**
	 * onClick action for Button to display eye photos
	 * 
	 * @param view
	 */
	public void listFoldersForDisplayActivity(View view) {
		ListFoldersForDisplayActivity.startActivity(this,
				Application.getSharedPreferenceString(R.string.key_folder_photos));
	}

	/**
	 * onClick action for Button to organize new eye photos
	 * 
	 * @param view
	 */
	public void organizeNewFoldersActivity(View view) {
		boolean rightEyeLast = Application.getSharedPreferenceBoolean(R.string.key_eye_sequence_choice);
		OrganizeNewPhotosActivity.startActivity(this, Application.getSharedPreferenceString(R.string.key_folder_input),
				Application.getSharedPreferenceString(R.string.key_folder_photos), rightEyeLast);
	}

	/**
	 * onClick action used only for test purposes
	 * 
	 * @param view
	 */
	public void doTest(View view) {
		DisplayOneActivityOverlay.startActivity(this,
				"/storage/emulated/0/Augenfotos/Schraml Sybille/Schraml Sybille 2013-08-02 re.jpg");
	}

}
