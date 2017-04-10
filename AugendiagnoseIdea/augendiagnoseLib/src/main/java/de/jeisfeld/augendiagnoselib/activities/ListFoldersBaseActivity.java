package de.jeisfeld.augendiagnoselib.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.fragments.ListFoldersBaseFragment;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;

/**
 * Base activity to display the list of subfolders of a folder Abstract class - child classes determine the detailed
 * actions. The folders should contain eye photos (following the name policy).
 */
public abstract class ListFoldersBaseActivity extends StandardActivity {
	/**
	 * The parent folder.
	 */
	@Nullable
	private String mParentFolder;
	/**
	 * The fragment displaying the list of folders.
	 */
	private ListFoldersBaseFragment mListFoldersFragment;

	// OVERRIDABLE
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mParentFolder = PreferenceUtil.getSharedPreferenceString(R.string.key_folder_photos);
	}

	/**
	 * Populate the ListFoldersBaseFragment with parameters.
	 *
	 * @param fragment The fragment to be populated.
	 */
	protected final void setFragmentParameters(@NonNull final ListFoldersBaseFragment fragment) {
		fragment.setParameters(mParentFolder);
	}

	/**
	 * Display the ListFoldersBaseFragment in the main listFoldersFragment container.
	 *
	 * @param fragment The fragment to be displayed.
	 * @param tag      The tag name of the fragment.
	 */
	protected final void displayOnFullScreen(final ListFoldersBaseFragment fragment, final String tag) {
		getFragmentManager().beginTransaction().add(R.id.fragment_container, fragment, tag).commit();
		getFragmentManager().executePendingTransactions();
	}

	@Override
	protected final int getHelpResource() {
		boolean helpOverviewInListFolders = Boolean.parseBoolean(getString(R.string.help_overview_in_list_folders));
		if (helpOverviewInListFolders) {
			return R.string.html_overview;
		}
		else {
			return R.string.html_display_photos;
		}
	}

	// Setters and getters

	@Nullable
	protected final String getParentFolder() {
		return mParentFolder;
	}

	public final ListFoldersBaseFragment getListFoldersFragment() {
		return mListFoldersFragment;
	}

	protected final void setListFoldersFragment(final ListFoldersBaseFragment listFoldersFragment) {
		this.mListFoldersFragment = listFoldersFragment;
	}

}
