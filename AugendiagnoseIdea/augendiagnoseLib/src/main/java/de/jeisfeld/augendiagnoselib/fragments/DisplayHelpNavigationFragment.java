package de.jeisfeld.augendiagnoselib.fragments;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

import androidx.fragment.app.ListFragment;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.activities.DisplayHtmlActivity;

/**
 * Fragment to display the navigation list of help screens.
 */
public class DisplayHelpNavigationFragment extends ListFragment {

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		createList();
	}

	@Override
	public final void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setOnItemClickListener(new ShowHelpOnClickListener());
	}

	/**
	 * Fill the contents of the navigation page.
	 */
	private void createList() {
		TypedArray htmlNavigationResources =
				getActivity().getResources().obtainTypedArray(R.array.html_navigation_resources);

		String[] folderNames = new String[htmlNavigationResources.length()];

		for (int i = 0; i < htmlNavigationResources.length(); i++) {
			folderNames[i] = htmlNavigationResources.getString(i);
		}
		htmlNavigationResources.recycle();

		ArrayAdapter<String> directoryListAdapter =
				new ArrayAdapter<>(getActivity(), R.layout.adapter_list_names, folderNames);
		setListAdapter(directoryListAdapter);
	}

	/**
	 * Item click listener displaying the help content.
	 */
	private class ShowHelpOnClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
			TypedArray htmlResources =
					getActivity().getResources().obtainTypedArray(R.array.html_resources);
			int resource = htmlResources.getResourceId(position, 0);
			htmlResources.recycle();

			((DisplayHtmlActivity) getActivity()).displayDetails(resource);
		}
	}

}
