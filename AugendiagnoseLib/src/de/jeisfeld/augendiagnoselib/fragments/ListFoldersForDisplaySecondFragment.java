package de.jeisfeld.augendiagnoselib.fragments;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import de.jeisfeld.augendiagnoselib.activities.ListPicturesForSecondNameActivity;
import android.widget.TextView;

/**
 * Fragment to display the list of subfolders of the eye photo folder as dialog with the goal to select a second picture
 * for display.
 */
public class ListFoldersForDisplaySecondFragment extends ListFoldersBaseFragment {
	@Override
	protected final void setOnItemClickListener() {
		listView.setOnItemClickListener(new ShowContentsOnClickListener());
	}

	/**
	 * Item click listener showing the pictures of the selected folder (in eye photo pairs) for selection of a picture.
	 */
	private class ShowContentsOnClickListener implements OnItemClickListener {
		@Override
		public final void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
			ListFoldersForDisplaySecondFragment fragment = ListFoldersForDisplaySecondFragment.this;
			ListPicturesForSecondNameActivity.startActivity(fragment.getActivity(),
					fragment.parentFolder.getAbsolutePath(), ((TextView) view).getText().toString());
		}
	}

}
