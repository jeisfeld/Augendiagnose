package de.eisfeldj.augendiagnose.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

/**
 * Activity to display the list of subfolders of the eye photo folder as dialog with the goal to select a second picture
 * for display.
 */
public class ListFoldersForDisplaySecondActivity extends ListFoldersBaseActivity {
	private static final String STRING_EXTRA_FILEPATH = "de.eisfeldj.augendiagnose.FILEPATH";

	/**
	 * Static helper method to start the activity, passing the path of the folder
	 * 
	 * @param context
	 * @param foldername
	 */
	public static void startActivity(Context context, String foldername) {
		Intent intent = new Intent(context, ListFoldersForDisplaySecondActivity.class);
		intent.putExtra(STRING_EXTRA_FOLDER, foldername);
		context.startActivity(intent);
	}

	/**
	 * Static helper method to extract the name of the selected file from the activity response
	 * 
	 * @param resultCode
	 * @param data
	 *            The activity response
	 * @return
	 */
	public static String getResult(int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Bundle res = data.getExtras();
			return res.getString(STRING_EXTRA_FILEPATH);
		}
		else {
			return "";
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	protected void setOnItemClickListener() {
		getListView().setOnItemClickListener(new ShowContentsOnClickListener());
	}

	/**
	 * Item click listener showing the pictures of the selected folder (in eye photo pairs) for selection of a picture.
	 */
	private class ShowContentsOnClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			ListFoldersForDisplaySecondActivity activity = ListFoldersForDisplaySecondActivity.this;
			ListPicturesForSecondNameActivity.startActivity(activity, activity.parentFolder.getAbsolutePath(),
					((TextView) view).getText().toString());
		}
	}

	/**
	 * When getting the response from the picture selection, return the name of the selected picture and finish the
	 * activity.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ListPicturesForSecondNameActivity.REQUEST_CODE:
			Bundle resultData = new Bundle();
			resultData.putString(STRING_EXTRA_FILEPATH, ListPicturesForSecondNameActivity.getResult(resultCode, data));
			Intent intent = new Intent();
			intent.putExtras(resultData);
			setResult(RESULT_OK, intent);
			finish();
			break;
		}
	}
}
