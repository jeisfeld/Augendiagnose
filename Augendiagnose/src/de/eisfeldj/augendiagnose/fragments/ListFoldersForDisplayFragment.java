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
import de.eisfeldj.augendiagnose.activities.ListFoldersForDisplayActivity;
import de.eisfeldj.augendiagnose.util.DialogUtil;
import de.eisfeldj.augendiagnose.util.DialogUtil.ConfirmDialogFragment.ConfirmDialogListener;
import de.eisfeldj.augendiagnose.util.ImageSelectionAndDisplayHandler;

/**
 * Fragment to display the list of subfolders of the eye photo folder with the goal to display them after selection.
 */
public class ListFoldersForDisplayFragment extends ListFoldersBaseFragment {

	@Override
	public final void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(listView);
	}

	@Override
	public final void onDestroyView() {
		super.onDestroyView();
		ImageSelectionAndDisplayHandler.clean();
	}

	@Override
	protected final void setOnItemClickListener() {
		listView.setOnItemClickListener(new ShowContentsOnClickListener());
	}

	@Override
	public final void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.context_name_list, menu);
	}

	@Override
	public final boolean onContextItemSelected(final MenuItem item) {
		if (item.getGroupId() == R.id.menugroup_name_list) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			final CharSequence name = ((TextView) info.targetView).getText();

			switch (item.getItemId()) {
			case R.id.action_change_name:
				showChangeNameDialog(name, name);
				return true;
			case R.id.action_delete_images:
				ConfirmDialogListener listener = new ConfirmDialogListener() {
					private static final long serialVersionUID = -90397353402300863L;

					@Override
					public void onDialogPositiveClick(final DialogFragment dialog) {
						deleteFolder(name.toString());
					}

					@Override
					public void onDialogNegativeClick(final DialogFragment dialog) {
						// Do nothing
					}
				};

				DialogUtil.displayConfirmationMessage(getActivity(), listener, R.string.button_delete,
						R.string.message_dialog_confirm_delete_folder, name);
				return true;
			default:
				return super.onContextItemSelected(item);
			}
		}
		else {
			return super.onContextItemSelected(item);
		}
	}

	/**
	 * Item click listener showing the pictures of the selected folder (in eye photo pairs) for selection of one or two
	 * pictures.
	 */
	private class ShowContentsOnClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
			((ListFoldersForDisplayActivity) getActivity()).listPicturesForName(((TextView) view).getText().toString());
		}
	}
}
