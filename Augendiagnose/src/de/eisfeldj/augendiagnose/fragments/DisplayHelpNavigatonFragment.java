package de.eisfeldj.augendiagnose.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.activities.DisplayHtmlActivity;

/**
 * Fragment to display the navigation list of help screens.
 */
public class DisplayHelpNavigatonFragment extends ListFragment {

	/**
	 * The array of HTML navigation Strings.
	 */
	private static final int[] HTML_NAVIGATION_RESOURCES = { R.string.html_navigation_overview,
			R.string.html_navigation_settings,
			R.string.html_navigation_organize_photos, R.string.html_navigation_display_photos };

	/**
	 * The array of HTML details to be shown (ordered in same sequence as navigation pane).
	 */
	private static final int[] HTML_RESOURCES = { R.string.html_overview, R.string.html_settings,
			R.string.html_organize_photos, R.string.html_display_photos };

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
		String[] folderNames = new String[HTML_NAVIGATION_RESOURCES.length];

		for (int i = 0; i < HTML_NAVIGATION_RESOURCES.length; i++) {
			folderNames[i] = getString(HTML_NAVIGATION_RESOURCES[i]);
		}

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
			int resource = HTML_RESOURCES[position];

			((DisplayHtmlActivity) getActivity()).displayDetails(resource);
		}
	}

}
