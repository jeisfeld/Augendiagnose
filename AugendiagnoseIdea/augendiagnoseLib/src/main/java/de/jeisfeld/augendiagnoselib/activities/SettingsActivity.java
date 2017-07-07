package de.jeisfeld.augendiagnoselib.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.Application.AuthorizationLevel;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.components.DirectorySelectionPreference;
import de.jeisfeld.augendiagnoselib.components.PinchImageView;
import de.jeisfeld.augendiagnoselib.fragments.SettingsFragment;
import de.jeisfeld.augendiagnoselib.util.GoogleBillingHelper;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.SystemUtil;
import de.jeisfeld.augendiagnoselib.util.TrackingUtil;
import de.jeisfeld.augendiagnoselib.util.TrackingUtil.Category;

/**
 * Activity to display the settings page.
 */
public class SettingsActivity extends BasePreferenceActivity {
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
	private static final int[] PATH_RESOURCES = {R.string.key_folder_input, R.string.key_folder_photos};

	/**
	 * The settings fragment.
	 */
	private SettingsFragment mFragment;

	/**
	 * Utility method to start the activity.
	 *
	 * @param context  The context in which the activity is started.
	 * @param prefType The type of preferences to be displayed.
	 */
	public static void startActivity(@NonNull final Context context, @Nullable final Integer prefType) {
		Intent intent = new Intent(context, SettingsActivity.class);
		if (prefType != null) {
			intent.putExtra(STRING_EXTRA_PREF_TYPE, prefType);
		}
		context.startActivity(intent);
	}

	@Override
	protected final void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int prefType = getIntent().getIntExtra(STRING_EXTRA_PREF_TYPE, -1);

		if (prefType != -1) {
			mFragment = (SettingsFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
			if (mFragment == null) {
				mFragment = new SettingsFragment();
				mFragment.setParameters(getString(prefType));

				getFragmentManager().beginTransaction().replace(android.R.id.content, mFragment, FRAGMENT_TAG).commit();
				getFragmentManager().executePendingTransactions();

				if (savedInstanceState == null) {
					PreferenceUtil.incrementCounter(R.string.key_statistics_countsettings);
					TrackingUtil.sendEvent(Category.EVENT_USER, "Open Settings", null);
				}
			}
		}

		if (savedInstanceState == null) {
			PreferenceUtil.incrementCounter(R.string.key_statistics_countsettings);
			TrackingUtil.sendEvent(Category.EVENT_USER, "Open Settings", null);
		}

		String[] activitiesWithHomeEnablement = getResources().getStringArray(R.array.activities_with_home_enablement);
		if (getActionBar() != null) {
			getActionBar().setDisplayHomeAsUpEnabled(Arrays.asList(activitiesWithHomeEnablement).contains(getClass().getName()));
		}
	}

	@Override
	protected final void onResume() {
		super.onResume();
		TrackingUtil.sendScreen(this);
	}

	@Override
	public final void onBuildHeaders(@NonNull final List<Header> target) {
		List<Header> baseHeaders = new ArrayList<>();

		int prefType = getIntent().getIntExtra(STRING_EXTRA_PREF_TYPE, -1);
		if (prefType == -1) {
			// Load resource only into preliminary list, in order to allow manipulation.
			loadHeadersFromResource(R.xml.pref_header, baseHeaders);

			for (int i = 0; i < baseHeaders.size(); i++) {
				Header header = baseHeaders.get(i);
				String listPrefType = header.fragmentArguments == null ? "" : header.fragmentArguments.getString(SettingsFragment.STRING_PREF_TYPE);

				if (listPrefType == null || listPrefType.equals(getString(R.string.key_dummy_screen_input_settings))
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
	protected final boolean isValidFragment(@NonNull final String fragmentName) {
		return fragmentName.startsWith("de.jeisfeld.augendiagnoselib.fragments");
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_default, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public final boolean onOptionsItemSelected(@NonNull final MenuItem item) {
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
	 * folder.
	 *
	 * @param context The Context in which the preferences should be set.
	 */
	@SuppressLint("SdCardPath")
	public static void setDefaultSharedPreferences(@NonNull final Context context) {
		PreferenceManager.setDefaultValues(Application.getAppContext(), R.xml.prefs_input, true);
		PreferenceManager.setDefaultValues(Application.getAppContext(), R.xml.prefs_display, true);
		PreferenceManager.setDefaultValues(Application.getAppContext(), R.xml.prefs_storage, true);
		PreferenceManager.setDefaultValues(Application.getAppContext(), R.xml.prefs_camera, true);
		PreferenceManager.setDefaultValues(Application.getAppContext(), R.xml.prefs_premium, true);

		if (PreferenceUtil.getSharedPreferenceString(R.string.key_folder_input).equals(
				context.getString(R.string.pref_dummy_folder_input))) {
			// On first startup, make default setting dependent on status of Eye-Fi.
			if (SystemUtil.isAppInstalled(Application.getResourceString(R.string.package_mobi))) {
				// If Eye-Fi (new) is available, use Eye-Fi default folder
				PreferenceUtil.setSharedPreferenceString(R.string.key_folder_input,
						context.getString(R.string.pref_default_folder_input_mobi));
			}
			else if (SystemUtil.isAppInstalled(Application.getResourceString(R.string.package_eyefi))) {
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

		// Settings for overlay buttons.
		PreferenceUtil.setDefaultOverlayButtonSettings();

		// Do the initialization of hint settings
		if (!PreferenceUtil.getSharedPreferenceBoolean(R.string.key_internal_initialized_hints)) {
			boolean showTips = Boolean.parseBoolean(context.getString(R.string.pref_default_show_tips));
			PreferenceUtil.setAllHints(!showTips);
			// always show tips for first use
			PreferenceUtil.setSharedPreferenceBoolean(R.string.key_tip_firstuse, false);
			// always show hint on max volume for external LED
			PreferenceUtil.setSharedPreferenceBoolean(R.string.key_tip_external_flash, false);

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

	@Override
	public final void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
		if (requestCode == SettingsFragment.REQUEST_CODE_PERMISSION) {
			// If request is cancelled, the result arrays are empty.
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				mFragment.initializeGoogleBilling();
			}
		}
	}
}
