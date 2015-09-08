package de.jeisfeld.augendiagnoselib.activities;

import java.util.Arrays;
import java.util.List;

import com.android.vending.billing.PurchasedSku;
import com.android.vending.billing.SkuDetails;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.Application.AuthorizationLevel;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.GoogleBillingHelper;
import de.jeisfeld.augendiagnoselib.util.GoogleBillingHelper.OnInventoryFinishedListener;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.ReleaseNotesUtil;

/**
 * Base activity being the subclass of most application activities. Handles the help menu.
 */
public abstract class BaseActivity extends AdMarvelActivity {
	/**
	 * Flag indicating if the creation of the activity is failed.
	 */
	private boolean isCreationFailed = false;

	protected final boolean isCreationFailed() {
		return isCreationFailed;
	}

	// OVERRIDABLE
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Application.setLanguage();
		DialogUtil.checkOutOfMemoryError(this);

		if (Intent.ACTION_MAIN.equals(getIntent().getAction())) {
			if (Application.getAuthorizationLevel() == AuthorizationLevel.NO_ACCESS) {
				final Intent starterIntent = getIntent();

				// Check in-app purchases
				GoogleBillingHelper.initialize(this, new OnInventoryFinishedListener() {

					@Override
					public void handleProducts(final List<PurchasedSku> purchases, final List<SkuDetails> availableProducts,
							final boolean isPremium) {
						PreferenceUtil.setSharedPreferenceBoolean(R.string.key_internal_has_premium_pack, isPremium);
						GoogleBillingHelper.dispose();

						if (isPremium) {
							startActivity(starterIntent);
							finish();
						}
						else {
							DialogUtil.displayAuthorizationError(BaseActivity.this, R.string.message_dialog_trial_time);
						}
					}
				});
				isCreationFailed = true;
				return;
			}
			else {
				if (savedInstanceState == null) {
					boolean firstStart = false;

					// Initial tip is triggered first, so that it is hidden behind release notes.
					DialogUtil.displayTip(this, R.string.message_tip_firstuse, R.string.key_tip_firstuse);

					// When starting from launcher, check if started the first time in this version. If yes, display release
					// notes.
					String storedVersionString = PreferenceUtil.getSharedPreferenceString(R.string.key_internal_stored_version);
					if (storedVersionString == null || storedVersionString.length() == 0) {
						storedVersionString = "0";
						firstStart = true;
					}
					int storedVersion = Integer.parseInt(storedVersionString);
					int currentVersion = Application.getVersion();

					if (storedVersion < currentVersion) {
						ReleaseNotesUtil.displayReleaseNotes(this, firstStart, storedVersion + 1, currentVersion);
					}

					// Check in-app purchases
					GoogleBillingHelper.initialize(this, new OnInventoryFinishedListener() {

						@Override
						public void handleProducts(final List<PurchasedSku> purchases, final List<SkuDetails> availableProducts,
								final boolean isPremium) {
							PreferenceUtil.setSharedPreferenceBoolean(R.string.key_internal_has_premium_pack, isPremium);
							GoogleBillingHelper.dispose();
						}
					});

					test();
				}
			}
		}

		String[] activitiesWithHomeEnablement = getResources().getStringArray(R.array.activities_with_home_enablement);
		if (getActionBar() != null) {
			getActionBar().setDisplayHomeAsUpEnabled(Arrays.asList(activitiesWithHomeEnablement).contains(getClass().getName()));
		}
	}

	/*
	 * Inflate options menu.
	 */
	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		String[] activitiesWithActionSettings = getResources().getStringArray(R.array.activities_with_action_settings);
		boolean hasActionSettings = Arrays.asList(activitiesWithActionSettings).contains(getClass().getName());
		String[] activitiesWithActionCamera = getResources().getStringArray(R.array.activities_with_action_camera);
		boolean hasActionCamera = Arrays.asList(activitiesWithActionCamera).contains(getClass().getName());

		getMenuInflater().inflate(R.menu.menu_default, menu);

		if (hasActionSettings) {
			menu.findItem(R.id.action_settings).setVisible(true);
		}
		if (hasActionCamera) {
			menu.findItem(R.id.action_camera).setVisible(true);
		}

		if (getHelpResource() == 0 || getString(getHelpResource()).length() == 0) {
			// Hide help icon if there is no help text
			menu.findItem(R.id.action_help).setVisible(false);
		}

		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * Handle menu actions.
	 */
	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_help) {
			DisplayHtmlActivity.startActivity(this, getHelpResource());
			return true;
		}
		else if (itemId == R.id.action_settings) {
			SettingsActivity.startActivity(this);
			return true;
		}
		else if (itemId == R.id.action_camera) {
			CameraActivity.startActivity(this);
			finish();
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Factory method to retrieve the resource id of the help page to be shown.
	 *
	 * @return the resource id of the help page.
	 */
	protected abstract int getHelpResource();

	/**
	 * Utility method - here it is possible to place code to be tested on startup.
	 */
	private void test() {
	}
}
