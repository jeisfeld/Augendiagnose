package de.eisfeldj.augendiagnose.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.activities.ListPicturesForNameActivity;
import de.eisfeldj.augendiagnose.util.DialogUtil;
import de.eisfeldj.augendiagnose.util.DialogUtil.ConfirmDeleteDialogFragment.ConfirmDeleteDialogListener;
import de.eisfeldj.augendiagnose.util.ImageSelectionAndDisplayHandler;

/**
 * Activity to display the list of subfolders of the eye photo folder with the goal to display them after selection.
 */
public class ListFoldersForDisplayFragment extends ListFoldersBaseFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
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
			ListFoldersForDisplayFragment fragment = ListFoldersForDisplayFragment.this;
			ListPicturesForNameActivity.startActivity(fragment.getActivity(), fragment.parentFolder.getAbsolutePath(),
					((TextView) view).getText().toString());
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
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

			DialogUtil.displayDeleteConfirmationMessage(getActivity(), listener,
					R.string.message_dialog_confirm_delete_folder, name);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

}
