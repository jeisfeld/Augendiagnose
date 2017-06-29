package de.jeisfeld.augendiagnoselib.activities;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;

import com.android.vending.billing.Purchase;
import com.android.vending.billing.PurchasedSku;
import com.android.vending.billing.SkuDetails;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.Application.AuthorizationLevel;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.DialogUtil.ConfirmDialogFragment.ConfirmDialogListener;
import de.jeisfeld.augendiagnoselib.util.DialogUtil.DisplayMessageDialogFragment.MessageDialogListener;
import de.jeisfeld.augendiagnoselib.util.EncryptionUtil;
import de.jeisfeld.augendiagnoselib.util.GoogleBillingHelper;
import de.jeisfeld.augendiagnoselib.util.GoogleBillingHelper.OnInventoryFinishedListener;
import de.jeisfeld.augendiagnoselib.util.GoogleBillingHelper.OnPurchaseSuccessListener;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.ReleaseNotesUtil;
import de.jeisfeld.augendiagnoselib.util.TrackingUtil;
import de.jeisfeld.augendiagnoselib.util.TrackingUtil.Category;

/**
 * Base activity being the subclass of most application activities. Handles the help menu, and handles startup activities related to authorization.
 */
public abstract class StandardActivity extends AdMarvelActivity {
	/**
	 * The request code for the unlocker app.
	 */
	private static final int REQUEST_CODE_UNLOCKER = 100;
	/**
	 * The request code for the rating on Google Play.
	 */
	private static final int REQUEST_CODE_RATING = 101;
	/**
	 * The request code used to query for permission.
	 */
	protected static final int REQUEST_CODE_PERMISSION = 6;
	/**
	 * The resource key for the authorizaton with the unlocker app.
	 */
	private static final String STRING_EXTRA_REQUEST_KEY = "de.jeisfeld.augendiagnoseunlocker.REQUEST_KEY";
	/**
	 * The resource key for the response from the unlocker app.
	 */
	private static final String STRING_RESULT_RESPONSE_KEY = "de.jeisfeld.augendiagnoseunlocker.RESPONSE_KEY";

	/**
	 * The random string used for authorization versus unlocker app.
	 */
	@Nullable
	private String mRandomAuthorizationString = null;

	/**
	 * Flag indicating if the creation of the activity is failed.
	 */
	private boolean mIsCreationFailed = false;

	protected final boolean isCreationFailed() {
		return mIsCreationFailed;
	}

	// OVERRIDABLE
	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Application.setLanguage();
		DialogUtil.checkOutOfMemoryError(this);
		test();

		// Check permissions for Android 6
		final String[] missingPermissions = checkRequiredPermissions();

		if (missingPermissions.length > 0) {
			// prevent NonSerializableException when changing orientation while showing confirmation dialog
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

			DialogUtil.displayConfirmationMessage(this, new ConfirmDialogListener() {
				/**
				 * The serial version UID.
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void onDialogPositiveClick(final DialogFragment dialog) {
					ActivityCompat.requestPermissions(StandardActivity.this, missingPermissions, REQUEST_CODE_PERMISSION);
				}

				@Override
				public void onDialogNegativeClick(final DialogFragment dialog) {
					finish();
				}
			}, R.string.button_continue, getPermissionInfoResource());
		}

		if (Intent.ACTION_MAIN.equals(getIntent().getAction())) {
			// Check authorization.
			if (Application.getAuthorizationLevel() == AuthorizationLevel.NO_ACCESS) {
				mIsCreationFailed = true;

				// Try authorization via unlocker app.
				checkUnlockerApp();
				return;
			}
			else {
				if (savedInstanceState == null) {
					testOnce();

					// Initial tip is triggered first, so that it is hidden behind release notes.
					DialogUtil.displayTip(this, R.string.message_tip_firstuse, R.string.key_tip_firstuse);

					// When starting from launcher, check if started the first time in this version. If yes, display release
					// notes.
					int storedVersion = PreferenceUtil.getSharedPreferenceIntString(R.string.key_internal_stored_version, null);
					int currentVersion = Application.getVersion();

					if (storedVersion < currentVersion) {
						ReleaseNotesUtil.displayReleaseNotes(this, storedVersion == 0, storedVersion + 1, currentVersion);
					}

					// Check unlocker app.
					checkUnlockerApp();

					// Check in-app purchases
					GoogleBillingHelper.initialize(this, new OnInventoryFinishedListener() {

						@Override
						public void handleProducts(final List<PurchasedSku> purchases, final List<SkuDetails> availableProducts,
												   final boolean isPremium) {
							PreferenceUtil.setSharedPreferenceBoolean(R.string.key_internal_has_premium_pack, isPremium);
							if (isPremium) {
								invalidateOptionsMenu();
							}
							GoogleBillingHelper.dispose();
						}
					});

				}
			}
		}

		String[] activitiesWithHomeEnablement = getResources().getStringArray(R.array.activities_with_home_enablement);
		if (getActionBar() != null) {
			getActionBar().setDisplayHomeAsUpEnabled(Arrays.asList(activitiesWithHomeEnablement).contains(getClass().getName()));
		}
	}

	// OVERRIDABLE
	@Override
	protected void onResume() {
		super.onResume();
		TrackingUtil.sendScreen(this);
	}

	/*
	 * Inflate options menu.
	 */
	@Override
	public final boolean onCreateOptionsMenu(@NonNull final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_default, menu);

		String[] activitiesWithActionSettings = getResources().getStringArray(R.array.activities_with_action_settings);
		boolean hasActionSettings = Arrays.asList(activitiesWithActionSettings).contains(getClass().getName());
		if (hasActionSettings) {
			menu.findItem(R.id.action_settings).setVisible(true);
		}

		String[] activitiesWithActionCamera = getResources().getStringArray(R.array.activities_with_action_camera);
		boolean hasActionCamera = Arrays.asList(activitiesWithActionCamera).contains(getClass().getName());
		if (hasActionCamera) {
			menu.findItem(R.id.action_camera).setVisible(true);
		}

		String[] activitiesWithActionPurchase = getResources().getStringArray(R.array.activities_with_action_purchase);
		boolean hasActionPurchase = Arrays.asList(activitiesWithActionPurchase).contains(getClass().getName());
		if (hasActionPurchase) {
			if (Application.getAuthorizationLevel() != AuthorizationLevel.FULL_ACCESS) {
				menu.findItem(R.id.action_purchase).setVisible(true);
			}
			else if (PreferenceUtil.getSharedPreferenceBoolean(R.string.key_show_rating_icon)) {
				menu.findItem(R.id.action_rating).setVisible(true);
			}
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
	public final boolean onOptionsItemSelected(@NonNull final MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_help) {
			DisplayHtmlActivity.startActivity(this, getHelpResource());
			return true;
		}
		else if (itemId == R.id.action_settings) {
			SettingsActivity.startActivity(this, null);
			return true;
		}
		else if (itemId == R.id.action_camera) {
			CameraActivity.startActivity(this, null);
			finish();
			return true;
		}
		else if (itemId == R.id.action_purchase) {
			DialogUtil.displayToast(this, R.string.message_dialog_triggering_purchase);
			triggerDefaultPurchase();
			return true;
		}
		else if (itemId == R.id.action_rating) {
			triggerRating();
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * After all other authorization options have failed, try to authorize via premium pack.
	 */
	private void checkPremiumPackAfterAuthorizationFailure() {
		if (mIsCreationFailed) {
			GoogleBillingHelper.initialize(this, new OnInventoryFinishedListener() {

				@Override
				public void handleProducts(final List<PurchasedSku> purchases, final List<SkuDetails> availableProducts,
										   final boolean isPremium) {
					PreferenceUtil.setSharedPreferenceBoolean(R.string.key_internal_has_premium_pack, isPremium);
					GoogleBillingHelper.dispose();

					if (isPremium) {
						finish();
						startActivity(getIntent());
					}
					else {
						DialogUtil.displayAuthorizationError(StandardActivity.this, R.string.message_dialog_trial_time);
					}
				}
			});
		}
	}

	/**
	 * Check authorization via unlocker app.
	 */
	private void checkUnlockerApp() {
		Intent intent = getPackageManager().getLaunchIntentForPackage("de.jeisfeld.augendiagnoseunlocker");
		if (intent == null) {
			updateUnlockerAppStatus(false);
			checkPremiumPackAfterAuthorizationFailure();
		}
		else {
			SecureRandom random = new SecureRandom();
			mRandomAuthorizationString = new BigInteger(130, random).toString(32); // MAGIC_NUMBER
			intent.putExtra(STRING_EXTRA_REQUEST_KEY, mRandomAuthorizationString);
			intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

			startActivityForResult(intent, REQUEST_CODE_UNLOCKER);
		}
	}

	/**
	 * Update the status of the unlocker app. Set to "true" if found. Set to false only after some failed retries.
	 *
	 * @param isCheckSuccessful flag indicating if the verification with unlocker app was successful.
	 */
	private void updateUnlockerAppStatus(final boolean isCheckSuccessful) {
		if (isCheckSuccessful) {
			PreferenceUtil.setSharedPreferenceBoolean(R.string.key_internal_has_unlocker_app, true);
			PreferenceUtil.setSharedPreferenceInt(R.string.key_internal_unlocker_app_retries, 0);
		}
		else {
			int retries = PreferenceUtil.incrementCounter(R.string.key_internal_unlocker_app_retries);
			if (retries > 10) { // MAGIC_NUMBER
				PreferenceUtil.setSharedPreferenceBoolean(R.string.key_internal_has_unlocker_app, false);
			}
		}
	}

	/**
	 * Trigger the purchase of the default premium pack.
	 */
	private void triggerDefaultPurchase() {
		// First dispose, in order to be sure to be able to instantiate the helper.
		GoogleBillingHelper.dispose();
		GoogleBillingHelper.initialize(this, new OnInventoryFinishedListener() {
			@Override
			public void handleProducts(final List<PurchasedSku> purchases, final List<SkuDetails> availableProducts, final boolean isPremium) {
				GoogleBillingHelper.launchPurchaseFlow(GoogleBillingHelper.PRIMARY_ID, new OnPurchaseSuccessListener() {
					@Override
					public void handlePurchase(final Purchase purchase, final boolean addedPremiumProduct) {
						if (addedPremiumProduct) {
							PreferenceUtil.setSharedPreferenceBoolean(R.string.key_internal_has_premium_pack, true);
						}
						GoogleBillingHelper.dispose();
						int messageResource = addedPremiumProduct
								? R.string.message_dialog_purchase_thanks_premium : R.string.message_dialog_purchase_thanks;

						MessageDialogListener listener = new MessageDialogListener() {
							private static final long serialVersionUID = 1L;

							@Override
							public void onDialogClick(final DialogFragment dialog) {
								finish();
								Application.startApplication(StandardActivity.this);
							}

							@Override
							public void onDialogCancel(final DialogFragment dialog) {
								finish();
								Application.startApplication(StandardActivity.this);
							}
						};

						DialogUtil.displayInfo(StandardActivity.this, listener, messageResource);
					}

					@Override
					public void handleFailure() {
						GoogleBillingHelper.dispose();
					}
				});
			}
		});
	}

	/**
	 * Trigger the app rating on Google Play.
	 */
	private void triggerRating() {
		TrackingUtil.sendEvent(Category.EVENT_USER, "Rating", "Pressed icon");
		DialogUtil.displayConfirmationMessage(this, new ConfirmDialogListener() {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void onDialogPositiveClick(final DialogFragment dialog) {
				TrackingUtil.sendEvent(Category.EVENT_USER, "Rating", "Go to rating");
				startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())), REQUEST_CODE_RATING);
			}

			@Override
			public void onDialogNegativeClick(final DialogFragment dialog) {
				queryRemoveRatingIcon();
			}
		}, R.string.button_rate, R.string.message_dialog_confirm_rate_app);
	}

	/**
	 * Query if rating icon should be removed.
	 */
	private void queryRemoveRatingIcon() {
		DialogUtil.displayConfirmationMessage(StandardActivity.this, new ConfirmDialogListener() {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void onDialogPositiveClick(final DialogFragment dialog) {
				PreferenceUtil.setSharedPreferenceBoolean(R.string.key_show_rating_icon, false);
				StandardActivity.this.invalidateOptionsMenu();
			}

			@Override
			public void onDialogNegativeClick(final DialogFragment dialog) {
				// do nothing
			}
		}, R.string.button_remove, R.string.message_dialog_confirm_rate_icon);
	}

	// OVERRIDABLE
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
		if (requestCode == REQUEST_CODE_UNLOCKER && resultCode == RESULT_OK && data != null) {
			String responseKey = data.getStringExtra(STRING_RESULT_RESPONSE_KEY);
			String expectedResponseKey = EncryptionUtil.createHash(mRandomAuthorizationString + getString(R.string.private_unlock_key));

			if (expectedResponseKey != null && expectedResponseKey.equals(responseKey)) {
				updateUnlockerAppStatus(true);

				if (mIsCreationFailed) {
					finish();
					startActivity(getIntent());
				}
			}
			else {
				updateUnlockerAppStatus(false);
				checkPremiumPackAfterAuthorizationFailure();
			}
		}
		else if (requestCode == REQUEST_CODE_RATING) {
			queryRemoveRatingIcon();
		}
		else {
			GoogleBillingHelper.handleActivityResult(requestCode, resultCode, data);

			updateUnlockerAppStatus(false);
			checkPremiumPackAfterAuthorizationFailure();
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	/**
	 * Get the array of required permissions.
	 *
	 * @return The array of required permissions.
	 */
	// OVERRIDABLE
	protected String[] getRequiredPermissions() {
		return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
	}

	/**
	 * Get the message displayed when asking for permission.
	 *
	 * @return The resource id of the message.
	 */
	// OVERRIDABLE
	protected int getPermissionInfoResource() {
		return R.string.message_dialog_confirm_need_read_permission;
	}

	/**
	 * Check which required permissions are missing.
	 *
	 * @return The list of missing required permissions.
	 */
	private String[] checkRequiredPermissions() {
		List<String> missingPermissions = new ArrayList<>();
		for (String permission : getRequiredPermissions()) {
			if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
				missingPermissions.add(permission);
			}
		}
		return missingPermissions.toArray(new String[missingPermissions.size()]);
	}

	// OVERRIDABLE
	@Override
	public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
		if (requestCode == REQUEST_CODE_PERMISSION) {
			// If request is cancelled, the result arrays are empty.
			if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
				finish();
			}
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
	private void testOnce() {
	}

	/**
	 * Utility method - here it is possible to place code to be tested on each activity start.
	 */
	private void test() {
	}

}
