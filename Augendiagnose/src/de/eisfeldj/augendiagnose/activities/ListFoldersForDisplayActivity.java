package de.eisfeldj.augendiagnose.activities;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.util.DialogUtil;
import de.eisfeldj.augendiagnose.util.ImageSelectionAndDisplayHandler;
import de.eisfeldj.augendiagnose.util.DialogUtil.ConfirmDeleteDialogFragment.ConfirmDeleteDialogListener;

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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		registerForContextMenu(getListView());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_name_list, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		CharSequence name = ((TextView) info.targetView).getText();
		final int position = info.position;

		switch (item.getItemId()) {
		case R.id.action_change_name:
			showChangeNameDialog(name, position);
			return true;
		case R.id.action_delete_images:
			ConfirmDeleteDialogListener listener = new ConfirmDeleteDialogListener() {
				private static final long serialVersionUID = -90397353402300863L;

				@Override
				public void onDialogPositiveClick(DialogFragment dialog) {
					deleteFolder(position);
				}

				@Override
				public void onDialogNegativeClick(DialogFragment dialog) {
					// Do nothing
				}
			};

			DialogUtil.displayDeleteConfirmationMessage(this, listener, R.string.message_dialog_confirm_delete_folder,
					name);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

}
