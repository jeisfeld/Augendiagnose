package de.eisfeldj.augendiagnose.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.util.ImageSelectionAndDisplayHandler;

/**
 * Activity to display the list of subfolders of the eye photo folder with the goal to display them after selection.
 */
public class ListFoldersForDisplayActivity extends ListFoldersBaseActivity {

	/**
	 * Static helper method to start the activity, passing the path of the folder
	 * 
	 * @param context
	 * @param foldername
	 */
	public static void startActivity(Context context, String foldername) {
		Intent intent = new Intent(context, ListFoldersForDisplayActivity.class);
		intent.putExtra(STRING_EXTRA_FOLDER, foldername);
		context.startActivity(intent);
	}

	/**
	 * Inflate options menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_only_help, menu);
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
	protected void setOnItemLongClickListener() {
		getListView().setOnItemLongClickListener(new RenameOnLongClickListener());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		((RenameOnLongClickListener) getListView().getOnItemLongClickListener()).closeDialog();
		ImageSelectionAndDisplayHandler.clean();
	}

	protected void setOnItemClickListener() {
		getListView().setOnItemClickListener(new ShowContentsOnClickListener());
	}

	/**
	 * Item click listener showing the pictures of the selected folder (in eye photo pairs) for selection of one or two
	 * pictures.
	 */
	private class ShowContentsOnClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			ListFoldersForDisplayActivity activity = ListFoldersForDisplayActivity.this;
			ListPicturesForNameActivity.startActivity(activity, activity.parentFolder.getAbsolutePath(),
					((TextView) view).getText().toString());
		}
	}
}
