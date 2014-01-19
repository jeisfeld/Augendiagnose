package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.util.DialogUtil;

/**
 * Main activity of the application
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SettingsActivity.setDefaultSharedPreferences();
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
		if(launchIntent != null) {
			startActivity(launchIntent);
		}
		else {
			DialogUtil.displayError(this, R.string.message_dialog_eyefi_not_installed);
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
		OrganizeNewPhotosActivity.startActivity(this,
				Application.getSharedPreferenceString(R.string.key_folder_eyefi),
				Application.getSharedPreferenceString(R.string.key_folder_photos), rightEyeLast);
	}

	
	/**
	 * onClick action used only for test purposes
	 * @param view
	 */
	public void doTest(View view) {
		DisplayOneActivityOverlay.startActivity(this, "/storage/emulated/0/Augenfotos/Schraml Sybille/Schraml Sybille 2013-08-02 re.jpg");
	}
	
}
