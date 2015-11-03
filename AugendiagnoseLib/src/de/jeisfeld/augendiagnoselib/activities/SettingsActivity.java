package de.jeisfeld.augendiagnoselib.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.Application.AuthorizationLevel;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.components.DirectorySelectionPreference;
import de.jeisfeld.augendiagnoselib.components.PinchImageView;
import de.jeisfeld.augendiagnoselib.fragments.SettingsFragment;
import de.jeisfeld.augendiagnoselib.util.GoogleBillingHelper;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.SystemUtil;

/**
 * Activity to display the settings page.
 */
public class SettingsActivity extends PreferenceActivity {
	/**
	 * The fragment tag.
	 */
	private static final String FRAGMENT_TAG = "FRAGMENT_TAG";

	/**
	 * The resource key for the type of preferences to be displayed.
	 */
	private static final String STRING_EXTRA_PREF_TYPE = "de.jeisfeld.augendiagnoselib.PREF_TYPE";

	/**
	 * The path resources for which external storage prefix should be replaced.
	 */
	private static final int[] PATH_RESOURCES = { R.string.key_folder_input, R.string.key_folder_photos };

	/**
	 * Utility method to start the activity.
	 *
	 * @param context
	 *            The context in which the activity is started.
	 * @param prefType
	 *            The type of preferences to be displayed.
	 */
	public static final void startActivity(final Context context, final String prefType) {
		Intent intent = new Intent(context, SettingsActivity.class);
		if (prefType != null) {
			intent.putExtra(STRING_EXTRA_PREF_TYPE, prefType);
		}
		context.startActivity(intent);
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String prefType = getIntent().getStringExtra(STRING_EXTRA_PREF_TYPE);

		if (prefType != null) {
			SettingsFragment fragment = (SettingsFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
			if (fragment == null) {
				fragment = new SettingsFragment();
				fragment.setParameters(prefType);

				getFragmentManager().beginTransaction().replace(android.R.id.content, fragment, FRAGMENT_TAG).commit();
				getFragmentManager().executePendingTransactions();

				if (savedInstanceState == null) {
					PreferenceUtil.incrementCounter(R.string.key_statistics_countsettings);
				}
			}
		}

		if (savedInstanceState == null) {
			PreferenceUtil.incrementCounter(R.string.key_statistics_countsettings);
		}

		String[] activitiesWithHomeEnablement = getResources().getStringArray(R.array.activities_with_home_enablement);
		if (getActionBar() != null) {
			getActionBar().setDisplayHomeAsUpEnabled(Arrays.asList(activitiesWithHomeEnablement).contains(getClass().getName()));
		}
	}

	@Override
	public final void onBuildHeaders(final List<Header> target) {
		List<Header> baseHeaders = new ArrayList<Header>();

		String prefType = getIntent().getStringExtra(STRING_EXTRA_PREF_TYPE);
		if (prefType == null) {
			// Load resource only into preliminary list, in order to allow manipulation.
			loadHeadersFromResource(R.xml.pref_header, baseHeaders);

			for (int i = 0; i < baseHeaders.size(); i++) {
				Header header = baseHeaders.get(i);
				String listPrefType = header.fragmentArguments == null ? "" : header.fragmentArguments.getString(SettingsFragment.STRING_PREF_TYPE);

				if (listPrefType.equals(getString(R.string.key_dummy_screen_input_settings))
						&& getString(R.string.pref_title_folder_input).length() == 0) {
					// ignore basic settings if there is no need to enter input folder
					continue;
				}

				if (listPrefType.equals(getString(R.string.key_dummy_screen_premium_settings))
						&& Application.getAuthorizationLevel() != AuthorizationLevel.FULL_ACCESS
						&& getString(R.string.pref_title_folder_input).length() == 0) {
					// if not authorized and if input folder not required, put premium settings at top position.
					target.add(0, header);
					continue;
				}

				target.add(header);

			}

		}

	}

	@Override
	protected final boolean isValidFragment(final String fragmentName) {
		return fragmentName.startsWith("de.jeisfeld.augendiagnoselib.fragments");
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_default, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_help) {
			DisplayHtmlActivity.startActivity(this, R.string.html_settings);
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Set the default shared preferences (after first installation) Regarding paths, choose external folder as base
	 * folder. For Bluestacks, chose bluestacks shared folder as base folder.
	 *
	 * @param context
	 *            The Context in which the preferences should be set.
	 */
	@SuppressLint("SdCardPath")
	public static final void setDefaultSharedPreferences(final Context context) {
		PreferenceManager.setDefaultValues(Application.getAppContext(), R.xml.prefs_input, true);
		PreferenceManager.setDefaultValues(Application.getAppContext(), R.xml.prefs_display, true);
		PreferenceManager.setDefaultValues(Application.getAppContext(), R.xml.prefs_storage, true);
		PreferenceManager.setDefaultValues(Application.getAppContext(), R.xml.prefs_camera, true);
		PreferenceManager.setDefaultValues(Application.getAppContext(), R.xml.prefs_premium, true);

		if (PreferenceUtil.getSharedPreferenceString(R.string.key_folder_input).equals(
				context.getString(R.string.pref_dummy_folder_input))) {
			// On first startup, make default setting dependent on status of Eye-Fi.
			if (SystemUtil.isAppInstalled("fi.eye.android")) {
				// If Eye-Fi is available, use Eye-Fi default folder
				PreferenceUtil.setSharedPreferenceString(R.string.key_folder_input,
						context.getString(R.string.pref_default_folder_input_eyefi));
			}
			else {
				// Otherwise, use normal default folder.
				PreferenceUtil.setSharedPreferenceString(R.string.key_folder_input,
						context.getString(R.string.pref_default_folder_input));
			}
		}

		for (int id : PATH_RESOURCES) {
			String path = PreferenceUtil.getSharedPreferenceString(id);

			String mappedPath = DirectorySelectionPreference.replaceSpecialFolderTags(path);

			if (!path.equals(mappedPath)) {
				PreferenceUtil.setSharedPreferenceString(id, mappedPath);
			}
		}

		// Setting for full resolution setting and for max bitmap size - dependent on available memory.
		PreferenceUtil.setDefaultResolutionSettings();

		// Setting for camera API - dependent on OS version.
		PreferenceUtil.setDefaultCameraSettings();

		// Do the initialization of hint settings
		if (!PreferenceUtil.getSharedPreferenceBoolean(R.string.key_internal_initialized_hints)) {
			boolean showTips = Boolean.parseBoolean(context.getString(R.string.pref_default_show_tips));
			PreferenceUtil.setAllHints(!showTips);
			PreferenceUtil.setSharedPreferenceBoolean(R.string.key_internal_initialized_hints, true);
		}

		// Inform PinchImageView about maxBitmapSize
		PinchImageView.setMaxBitmapSize(PreferenceUtil.getSharedPreferenceIntString(R.string.key_max_bitmap_size, 0));
	}

	@Override
	public final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Google Billing is started by Fragment, but on Activity level!
		GoogleBillingHelper.handleActivityResult(requestCode, resultCode, data);
	}
}
