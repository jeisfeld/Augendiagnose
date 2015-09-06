package de.jeisfeld.augendiagnoselib.fragments;

import android.app.ListFragment;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.activities.DisplayHtmlActivity;

/**
 * Fragment to display the navigation list of help screens.
 */
public class DisplayHelpNavigatonFragment extends ListFragment {

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
	protected final void createList() {
		TypedArray htmlNavigationResoucces =
				getActivity().getResources().obtainTypedArray(R.array.html_navigation_resources);

		String[] folderNames = new String[htmlNavigationResoucces.length()];

		for (int i = 0; i < htmlNavigationResoucces.length(); i++) {
			folderNames[i] = htmlNavigationResoucces.getString(i);
		}
		htmlNavigationResoucces.recycle();

		ArrayAdapter<String> directoryListAdapter =
				new ArrayAdapter<String>(getActivity(), R.layout.adapter_list_names, folderNames);
		setListAdapter(directoryListAdapter);
	}

	/**
	 * Item click listener displaying the help content.
	 */
	private class ShowHelpOnClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
			TypedArray htmlResources =
					Application.getAppContext().getResources().obtainTypedArray(R.array.html_resources);
			int resource = htmlResources.getResourceId(position, 0);
			htmlResources.recycle();

			((DisplayHtmlActivity) getActivity()).displayDetails(resource);
		}
	}

}
