package de.eisfeldj.augendiagnose.fragments;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import de.eisfeldj.augendiagnose.activities.ListPicturesForSecondNameActivity;

/**
 * Fragment to display the list of subfolders of the eye photo folder as dialog with the goal to select a second picture
 * for display.
 */
public class ListFoldersForDisplaySecondFragment extends ListFoldersBaseFragment {
	@Override
	protected void setOnItemClickListener() {
		getListView().setOnItemClickListener(new ShowContentsOnClickListener());
	}

	/**
	 * Item click listener showing the pictures of the selected folder (in eye photo pairs) for selection of a picture.
	 */
	private class ShowContentsOnClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			ListFoldersForDisplaySecondFragment fragment = ListFoldersForDisplaySecondFragment.this;
			ListPicturesForSecondNameActivity.startActivity(fragment.getActivity(),
					fragment.parentFolder.getAbsolutePath(), ((TextView) view).getText().toString());
		}
	}

}
