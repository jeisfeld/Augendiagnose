package de.jeisfeld.augendiagnoselib.activities;

import java.io.File;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.fragments.ListFoldersBaseFragment;
import de.jeisfeld.augendiagnoselib.fragments.ListFoldersForDisplayFragment;
import de.jeisfeld.augendiagnoselib.fragments.ListPicturesForNameFragment;
import de.jeisfeld.augendiagnoselib.fragments.ListPicturesForNameFragment.ListPicturesForNameFragmentHolder;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.ImageSelectionAndDisplayHandler;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.SystemUtil;

/**
 * Activity to display the list of subfolders of the eye photo folder with the goal to display them after selection.
 */
public class ListFoldersForDisplayActivity extends ListFoldersBaseActivity implements ListPicturesForNameFragmentHolder {
	/**
	 * The fragment tag.
	 */
	private static final String FRAGMENT_TAG = "FRAGMENT_TAG";
	/**
	 * The fragment tag for the list pictures fragment.
	 */
	private static final String FRAGMENT_LISTPICTURES_TAG = "FRAGMENT_LISTPICTURES_TAG";

	/**
	 * The ListPicturesForNameFragment used to display pictures on a tablet.
	 */
	private ListPicturesForNameFragment listPicturesFragment;

	/**
	 * Static helper method to start the activity, passing the path of the folder.
	 *
	 * @param context
	 *            The context in which the activity is started.
	 */
	public static final void startActivity(final Context context) {
		Intent intent = new Intent(context, ListFoldersForDisplayActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (isCreationFailed()) {
			return;
		}

		if (SystemUtil.isTablet()) {
			setContentView(R.layout.activity_fragments_list_detail);
		}
		else {
			setContentView(R.layout.activity_fragments_single);
		}

		setListFoldersFragment((ListFoldersBaseFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG));

		if (getListFoldersFragment() == null) {
			setListFoldersFragment(new ListFoldersForDisplayFragment());
			setFragmentParameters(getListFoldersFragment());

			if (SystemUtil.isTablet()) {
				getFragmentManager().beginTransaction().add(R.id.fragment_list, getListFoldersFragment(), FRAGMENT_TAG)
						.commit();

				String defaultName = PreferenceUtil.getSharedPreferenceString(R.string.key_internal_last_name);
				if (defaultName.length() > 0 && new File(getParentFolder(), defaultName).exists()) {
					listPicturesForName(defaultName);
				}

				getFragmentManager().executePendingTransactions();

				// In tablet view, different title is more appropriate
				setTitle(getString(R.string.title_activity_list_pictures_for_name));

				DialogUtil.displayTip(this, R.string.message_tip_displaypicturestablet, R.string.key_tip_displaynames);
			}
			else {
				displayOnFullScreen(getListFoldersFragment(), FRAGMENT_TAG);

				DialogUtil.displayTip(this, R.string.message_tip_displaynames, R.string.key_tip_displaynames);
			}
		}

		if (savedInstanceState == null) {
			PreferenceUtil.incrementCounter(R.string.key_statistics_countlistnames);
		}

		if (SystemUtil.isTablet()) {
			// Associate image display to this activity
			ImageSelectionAndDisplayHandler.getInstance().setActivity(this);
		}

		requestBannerAdIfEligible();
	}

	@Override
	protected final void onDestroy() {
		super.onDestroy();
		ImageSelectionAndDisplayHandler.clean();
	}

	/**
	 * Display the list of pictures for a selected name.
	 *
	 * @param name
	 *            The name for which pictures should be shown.
	 */
	public final void listPicturesForName(final String name) {
		if (SystemUtil.isTablet()) {
			listPicturesFragment =
					(ListPicturesForNameFragment) getFragmentManager().findFragmentByTag(FRAGMENT_LISTPICTURES_TAG);
			if (listPicturesFragment != null && name.equals(listPicturesFragment.getName())) {
				// Do nothing if the given name is already opened.
				return;
			}

			listPicturesFragment = new ListPicturesForNameFragment();
			listPicturesFragment.setParameters(getParentFolder(), name);

			FragmentTransaction transaction = getFragmentManager().beginTransaction().replace(R.id.fragment_detail,
					listPicturesFragment, FRAGMENT_LISTPICTURES_TAG);
			if (findViewById(R.id.listViewForName) != null) {
				// if right pane is filled, then add it to the back stack
				transaction.addToBackStack(null);
			}
			transaction.commit();

			getFragmentManager().executePendingTransactions();
		}
		else {
			ListPicturesForNameActivity.startActivity(this, getParentFolder(), name);
		}

		// Store the name so that it may be opened automatically next time
		PreferenceUtil.setSharedPreferenceString(R.string.key_internal_last_name, name);
		PreferenceUtil.setSharedPreferenceBoolean(R.string.key_internal_organized_new_photo, false);
	}

	/**
	 * Remove the back stack on the "ListPicturesForName" pane and clean the pane.
	 */
	public final void popBackStack() {
		if (SystemUtil.isTablet()) {
			getFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

			Fragment fragment = getFragmentManager().findFragmentByTag(FRAGMENT_LISTPICTURES_TAG);
			if (fragment != null) {
				getFragmentManager().beginTransaction().remove(fragment).commit();
				getFragmentManager().executePendingTransactions();
			}
		}
	}

	// implementation of ListPicturesForNameFragmentHolder

	@Override
	public final ListPicturesForNameFragment getListPicturesForNameFragment() {
		return listPicturesFragment;
	}

	@Override
	public final void setListPicturesForNameFragment(final ListPicturesForNameFragment fragment) {
		this.listPicturesFragment = fragment;
	}

}
