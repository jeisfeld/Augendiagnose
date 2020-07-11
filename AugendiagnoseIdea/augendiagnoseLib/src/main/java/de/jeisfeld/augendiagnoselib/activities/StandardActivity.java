package de.jeisfeld.augendiagnoselib.activities;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.Application.AuthorizationLevel;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.DialogUtil.ConfirmDialogFragment.ConfirmDialogListener;
import de.jeisfeld.augendiagnoselib.util.DialogUtil.DisplayMessageDialogFragment.MessageDialogListener;
import de.jeisfeld.augendiagnoselib.util.EncryptionUtil;
import de.jeisfeld.augendiagnoselib.util.GoogleBillingHelper;
import de.jeisfeld.augendiagnoselib.util.GoogleBillingHelper.OnPurchaseQueryCompletedListener;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.ReleaseNotesUtil;
import de.jeisfeld.augendiagnoselib.util.SystemUtil;
import de.jeisfeld.augendiagnoselib.util.TrackingUtil;
import de.jeisfeld.augendiagnoselib.util.TrackingUtil.Category;
import de.jeisfeld.augendiagnoselib.util.imagefile.FileUtil;

/**
 * Base activity being the subclass of most application activities. Handles the help menu, and handles startup activities related to authorization.
 */
public abstract class StandardActivity extends BaseActivity {
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
	 * The requestCode with which the storage access framework is triggered for eye photo folder.
	 */
	private static final int REQUEST_CODE_STORAGE_ACCESS_PHOTOS = 5;
	/**
	 * The resource key for the authorizaton with the unlocker app.
	 */
	private static final String STRING_EXTRA_REQUEST_KEY = "de.jeisfeld.augendiagnoseunlocker.REQUEST_KEY";
	/**
	 * The resource key for the response from the unlocker app.
	 */
	private static final String STRING_RESULT_RESPONSE_KEY = "de.jeisfeld.augendiagnoseunlocker.RESPONSE_KEY";
	/**
	 * The resource key for skipping startup hints.
	 */
	private static final String STRING_EXTRA_SKIP_STARTUP_HINTS = "de.jeisfeld.augendiagnoselib.SKIP_STARTUP_HINTS";

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

	/**
	 * The expected eye photo folder folder.
	 */
	private String mExpectedFolderPhotos = null;

	// OVERRIDABLE
	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DialogUtil.checkOutOfMemoryError(this);
		test();

		checkPermissions();

		if (Intent.ACTION_MAIN.equals(getIntent().getAction())) {
			// Check authorization.
			if (Application.getAuthorizationLevel() == AuthorizationLevel.NO_ACCESS) {
				mIsCreationFailed = true;

				// Try authorization via unlocker app.
				checkUnlockerApp();
				return;
			}

			if (savedInstanceState == null) {
				testOnce();

				if (!getIntent().getBooleanExtra(STRING_EXTRA_SKIP_STARTUP_HINTS, false)) {
					// Initial tip is triggered first, so that it is hidden behind release notes.
					DialogUtil.displayTip(this, R.string.message_tip_firstuse, R.string.key_tip_firstuse);

					// When starting from launcher, check if started the first time in this version. If yes, display release
					// notes.
					int storedVersion = PreferenceUtil.getSharedPreferenceIntString(R.string.key_internal_stored_version, null);
					int currentVersion = Application.getVersion();

					if (storedVersion < currentVersion) {
						ReleaseNotesUtil.displayReleaseNotes(this, storedVersion == 0, storedVersion + 1, currentVersion);
					}
				}

				// Check unlocker app.
				checkUnlockerApp();

				// Check in-app purchases
				GoogleBillingHelper.getInstance(this).hasPremiumPack(new OnPurchaseQueryCompletedListener() {
					@Override
					public void onHasPremiumPack(final boolean hasPremiumPack, final boolean isPending) {
						PreferenceUtil.setSharedPreferenceBoolean(R.string.key_internal_has_premium_pack, hasPremiumPack);
						if (hasPremiumPack) {
							invalidateOptionsMenu();
						}
					}
				});
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
			SettingsActivity.startActivity(this, R.string.key_dummy_screen_premium_settings);
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
	 * Check if the app has all required permissions.
	 */
	//OVERRIDABLE
	protected void checkPermissions() {
		// Check permissions for Android 6
		final String[] missingPermissions = checkMissingPermissions();

		if (missingPermissions.length > 0) {
			// prevent NonSerializableException when changing orientation while showing confirmation dialog
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

			DialogUtil.displayInfo(this, new MessageDialogListener() {
				/**
				 * The serial version UID.
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void onDialogClick(final DialogFragment dialog) {
					ActivityCompat.requestPermissions(StandardActivity.this, missingPermissions, REQUEST_CODE_PERMISSION);
				}

				@Override
				public void onDialogCancel(final DialogFragment dialog) {
					finish();
				}
			}, getPermissionInfoResource());
		}

		if (SystemUtil.isAtLeastVersion(VERSION_CODES.Q)
				&& PreferenceUtil.getSharedPreferenceUri(R.string.key_internal_uri_extsdcard_photos) == null) {
			int initialVersion = PreferenceUtil.getSharedPreferenceInt(de.jeisfeld.augendiagnoselib.R.string.key_statistics_initialversion, -1);
			int dialogResource = R.string.message_dialog_select_photos_folder_saf_new;
			String dialogParameter = getString(R.string.pref_default_folder_name_photos);
			if (initialVersion < Application.getVersion()) {
				mExpectedFolderPhotos = PreferenceUtil.getSharedPreferenceString(de.jeisfeld.augendiagnoselib.R.string.key_folder_photos);
				dialogResource = R.string.message_dialog_select_photos_folder_saf;
				dialogParameter = mExpectedFolderPhotos;
			}
			DialogUtil.displayInfo(this, new MessageDialogListener() {
				/**
				 * The serial version UID.
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void onDialogClick(final DialogFragment dialog) {
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
					intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI,
							Uri.parse("content://com.android.externalstorage.documents/tree/primary%3A"
									+ getString(R.string.pref_default_folder_name_photos) + "/document/primary%3A"
									+ getString(R.string.pref_default_folder_name_photos)));
					startActivityForResult(intent, REQUEST_CODE_STORAGE_ACCESS_PHOTOS);
				}

				@Override
				public void onDialogCancel(final DialogFragment dialog) {
					finish();
				}
			}, dialogResource, dialogParameter);
		}
	}

	/**
	 * After all other authorization options have failed, try to authorize via premium pack.
	 */
	private void checkPremiumPackAfterAuthorizationFailure() {
		if (mIsCreationFailed) {
			GoogleBillingHelper.getInstance(this).hasPremiumPack(new OnPurchaseQueryCompletedListener() {
				@Override
				public void onHasPremiumPack(final boolean hasPremiumPack, final boolean isPending) {
					PreferenceUtil.setSharedPreferenceBoolean(R.string.key_internal_has_premium_pack, hasPremiumPack);
					if (hasPremiumPack) {
						restartActivity();
					}
					else if (isPending) {
						DialogUtil.displayAuthorizationError(StandardActivity.this, R.string.message_dialog_trial_pending);
					}
					else {
						DialogUtil.displayAuthorizationError(StandardActivity.this, R.string.message_dialog_trial_time);
					}
				}
			});
		}
	}

	/**
	 * Restart the activity.
	 */
	protected void restartActivity() {
		finish();
		Intent intent = getIntent();
		intent.putExtra(STRING_EXTRA_SKIP_STARTUP_HINTS, true);
		startActivity(getIntent());
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
					restartActivity();
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
		else if (requestCode == REQUEST_CODE_STORAGE_ACCESS_PHOTOS) {
			if (resultCode == RESULT_OK && data != null) {
				if (VERSION.SDK_INT >= VERSION_CODES.Q) {
					handleSelectedPhotoFolderUri(data);
				}
			}
			else {
				finish();
			}
		}
		else {
			updateUnlockerAppStatus(false);
			checkPremiumPackAfterAuthorizationFailure();
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@RequiresApi(api = VERSION_CODES.Q)
	private void handleSelectedPhotoFolderUri(final Intent data) {
		Uri treeUri = data.getData();
		String path = FileUtil.getFullPathFromTreeUri(treeUri);

		if (treeUri == null || path == null) {
			restartActivity();
			return;
		}

		if (mExpectedFolderPhotos != null && !mExpectedFolderPhotos.equals(path)) {
			DialogUtil.displayInfo(this, new MessageDialogListener() {
				/**
				 * The serial version uid.
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void onDialogClick(final DialogFragment dialog) {
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
					startActivityForResult(intent, REQUEST_CODE_STORAGE_ACCESS_PHOTOS);
				}

				@Override
				public void onDialogCancel(final DialogFragment dialog) {
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
					startActivityForResult(intent, REQUEST_CODE_STORAGE_ACCESS_PHOTOS);
				}
			}, R.string.message_dialog_changed_photos_folder, mExpectedFolderPhotos, path);
		}
		else {
			String defaultFolderName = getString(R.string.pref_default_folder_name_photos);
			if (!defaultFolderName.equals(new File(path).getName())) {
				path = new File(path, defaultFolderName).getAbsolutePath();
				DocumentFile documentFile = DocumentFile.fromTreeUri(this, treeUri);
				if (documentFile != null && documentFile.findFile(defaultFolderName) == null) {
					documentFile.createDirectory(defaultFolderName);
				}
			}
			PreferenceUtil.setSharedPreferenceUri(R.string.key_internal_uri_extsdcard_photos, treeUri);
			PreferenceUtil.setSharedPreferenceString(R.string.key_folder_photos, path);
			getContentResolver().takePersistableUriPermission(treeUri, data.getFlags()
					& (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
			mExpectedFolderPhotos = null;
			restartActivity();
		}

	}


	/**
	 * Get the array of required permissions.
	 *
	 * @return The array of required permissions.
	 */
	// OVERRIDABLE
	protected String[] getRequiredPermissions() {
		return Application.getRequiredPermissions();
	}

	/**
	 * Get the message displayed when asking for permission.
	 *
	 * @return The resource id of the message.
	 */
	// OVERRIDABLE
	protected int getPermissionInfoResource() {
		return R.string.message_dialog_confirm_need_file_permission;
	}

	/**
	 * Check which required permissions are missing.
	 *
	 * @return The list of missing required permissions.
	 */
	private String[] checkMissingPermissions() {
		List<String> missingPermissions = new ArrayList<>();
		for (String permission : getRequiredPermissions()) {
			if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
				missingPermissions.add(permission);
			}
		}
		return missingPermissions.toArray(new String[0]);
	}

	// OVERRIDABLE
	@Override
	public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
		if (requestCode == REQUEST_CODE_PERMISSION) {
			// If request is cancelled, the result arrays are empty.
			if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
				DialogUtil.displayError(this, R.string.message_dialog_confirm_missing_permission, true);
			}
			else {
				SettingsActivity.setDefaultSharedPreferences(this);
				restartActivity();
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
