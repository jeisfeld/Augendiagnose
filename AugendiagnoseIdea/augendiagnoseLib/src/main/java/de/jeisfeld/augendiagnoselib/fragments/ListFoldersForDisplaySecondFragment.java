package de.jeisfeld.augendiagnoselib.fragments;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import androidx.annotation.NonNull;
import de.jeisfeld.augendiagnoselib.activities.ListPicturesForSecondNameActivity;

/**
 * Fragment to display the list of subfolders of the eye photo folder as dialog with the goal to select a second picture
 * for display.
 */
public class ListFoldersForDisplaySecondFragment extends ListFoldersBaseFragment {
	@Override
	protected final void setOnItemClickListener() {
		mListView.setOnItemClickListener(new ShowContentsOnClickListener());
	}

	/**
	 * Item click listener showing the pictures of the selected folder (in eye photo pairs) for selection of a picture.
	 */
	private class ShowContentsOnClickListener implements OnItemClickListener {
		@Override
		public final void onItemClick(final AdapterView<?> parent, @NonNull final View view, final int position, final long id) {
			ListFoldersForDisplaySecondFragment fragment = ListFoldersForDisplaySecondFragment.this;
			ListPicturesForSecondNameActivity.startActivity(fragment.getActivity(),
					fragment.mParentFolder.getAbsolutePath(), ((TextView) view).getText().toString());
		}
	}

}
