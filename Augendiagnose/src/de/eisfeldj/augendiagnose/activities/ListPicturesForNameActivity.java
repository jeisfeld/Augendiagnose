package de.eisfeldj.augendiagnose.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.components.ListPicturesForNameArrayAdapter;
import de.eisfeldj.augendiagnose.util.ImageSelectionAndDisplayHandler;

/**
 * Activity to display the pictures in an eye photo folder (in pairs) Either pictures from this folder can be displayed
 * directly, or another folder can be selected for a second picture.
 */
public class ListPicturesForNameActivity extends ListPicturesForNameBaseActivity {
	private static final String STRING_EXTRA_NAME = "de.eisfeldj.augendiagnose.NAME";
	private static final String STRING_EXTRA_PARENTFOLDER = "de.eisfeldj.augendiagnose.PARENTFOLDER";
	private static Button buttonAdditionalPictures;

	/**
	 * Static helper method to start the activity, passing the path of the parent folder and the name of the current
	 * folder.
	 * 
	 * @param activity
	 * @param parentFolder
	 * @param name
	 */
	public static void startActivity(Context context, String parentFolder, String name) {
		Intent intent = new Intent(context, ListPicturesForNameActivity.class);
		intent.putExtra(STRING_EXTRA_PARENTFOLDER, parentFolder);
		intent.putExtra(STRING_EXTRA_NAME, name);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		buttonAdditionalPictures = (Button) findViewById(R.id.buttonSelectAdditionalPicture);

		listview.setAdapter(new ListPicturesForNameArrayAdapter(this, eyePhotoPairs));

		// Initialize the handler which manages the clicks
		ImageSelectionAndDisplayHandler.getInstance().setActivity(this);
	}

	/**
	 * Inflate options menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.only_help, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Handle menu actions
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_help:
			DisplayHtmlActivity.startActivity(this, R.string.html_display_photos);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected int getContentView() {
		return R.layout.activity_list_pictures_for_name;
	}

	/**
	 * Display the button "additional pictures" after one photo is selected
	 */
	public void activateButtonAdditionalPictures() {
		buttonAdditionalPictures.setVisibility(View.VISIBLE);
		listview.invalidate();
	}

	/**
	 * Undisplay the button "additional pictures" if photo selection has been removed.
	 */
	public void deactivateButtonAdditionalPictures() {
		buttonAdditionalPictures.setVisibility(View.GONE);
		listview.invalidate();
	}

	/**
	 * onClick action for Button "additional pictures"
	 * 
	 * @param view
	 */
	public void selectDifferentPictureActivity(View view) {
		ListFoldersForDisplaySecondActivity.startActivity(this, parentFolder);
	}
}
