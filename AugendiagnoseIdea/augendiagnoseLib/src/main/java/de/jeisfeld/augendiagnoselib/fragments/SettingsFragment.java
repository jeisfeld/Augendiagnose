package de.jeisfeld.augendiagnoselib.fragments;

import java.io.File;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.android.vending.billing.Purchase;
import com.android.vending.billing.PurchasedSku;
import com.android.vending.billing.SkuDetails;
import com.immersion.hapticmediasdk.utils.Log;

import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.Application.AuthorizationLevel;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.components.DirectorySelectionPreference;
import de.jeisfeld.augendiagnoselib.components.DirectorySelectionPreference.OnDialogClosedListener;
import de.jeisfeld.augendiagnoselib.components.OverlayPinchImageView;
import de.jeisfeld.augendiagnoselib.components.PinchImageView;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.DialogUtil.DisplayMessageDialogFragment.MessageDialogListener;
import de.jeisfeld.augendiagnoselib.util.GoogleBillingHelper;
import de.jeisfeld.augendiagnoselib.util.GoogleBillingHelper.OnInventoryFinishedListener;
import de.jeisfeld.augendiagnoselib.util.GoogleBillingHelper.OnPurchaseSuccessListener;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.SystemUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.FileUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.JpegSynchronizationUtil;

/**
 * Fragment for displaying the settings.
 */
public class SettingsFragment extends PreferenceFragment {
	/**
	 * The requestCode for starting the permisson request.
	 */
	public static final int REQUEST_CODE_PERMISSION = 5;

	/**
	 * The requestCode with which the storage access framework is triggered for photo folder.
	 */
	private static final int REQUEST_CODE_STORAGE_ACCESS_PHOTOS = 3;

	/**
	 * The requestCode with which the storage access framework is triggered for input folder.
	 */
	private static final int REQUEST_CODE_STORAGE_ACCESS_INPUT = 4;

	/**
	 * The resource key of the flag indicating the type of settings to be shown.
	 * This key is used as well in pref_header.xml.
	 */
	public static final String STRING_PREF_TYPE = "prefType";

	/**
	 * A prefix put before the productId to define the according preference key.
	 */
	private static final String SKU_KEY_PREFIX = "sku_";

	/**
	 * A listener handling the change of preferences.
	 */
	private final CustomOnPreferenceChangeListener mOnPreferenceChangeListener = new CustomOnPreferenceChangeListener();

	/**
	 * Field holding the value of the input folder preference, in order to detect a real change.
	 */
	@Nullable
	private String mFolderInput;
	/**
	 * Field holding the value of the photo folder preference, in order to detect a real change.
	 */
	@Nullable
	private String mFolderPhotos;

	/**
	 * The type of fragment to be shown.
	 */
	@Nullable
	private String mType = null;

	/**
	 * Field for temporarily storing the folder used for Storage Access Framework.
	 */
	@Nullable
	private File mCurrentFolder;

	/**
	 * Initialize the fragment with onlyPacks flag.
	 *
	 * @param prefType The type of preferences to be shown.
	 */
	public final void setParameters(final String prefType) {
		Bundle args = new Bundle();
		args.putString(STRING_PREF_TYPE, prefType);
		setArguments(args);
	}

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() == null) {
			Log.e(Application.TAG, "Illegal call of SettingsFragment without parameters");
			return;
		}

		mType = getArguments().getString(STRING_PREF_TYPE);
		if (mType.equals(getActivity().getString(R.string.key_dummy_screen_input_settings))) {
			addPreferencesFromResource(R.xml.prefs_input);

			mFolderInput = PreferenceUtil.getSharedPreferenceString(R.string.key_folder_input);
			bindPreferenceSummaryToValue(R.string.key_folder_input);
		}
		else if (mType.equals(getActivity().getString(R.string.key_folder_input))) {
			// Special fragment for displaying only input folder.
			addPreferencesFromResource(R.xml.prefs_input);

			mFolderInput = PreferenceUtil.getSharedPreferenceString(R.string.key_folder_input);
			bindPreferenceSummaryToValue(R.string.key_folder_input);

			PreferenceScreen screen = getPreferenceScreen();

			for (int i = 0; i < screen.getPreferenceCount(); i++) {
				Preference pref = screen.getPreference(i);
				if (pref.getKey().equals(getActivity().getString(R.string.key_folder_input))) {
					DirectorySelectionPreference preference = (DirectorySelectionPreference) pref;

					preference.setOnDialogClosedListener(new OnDialogClosedListener() {
						@Override
						public void onDialogClosed() {
							getActivity().finish();
						}
					});

					preference.showDialog();
				}
				else {
					screen.removePreference(pref);
				}
			}
		}
		else if (mType.equals(getActivity().getString(R.string.key_dummy_screen_display_settings))) {
			addPreferencesFromResource(R.xml.prefs_display);

			bindPreferenceSummaryToValue(R.string.key_language);

			addHintButtonListener(R.string.key_dummy_show_hints, false);
			addHintButtonListener(R.string.key_dummy_hide_hints, true);
		}
		else if (mType.equals(getActivity().getString(R.string.key_dummy_screen_storage_settings))) {
			addPreferencesFromResource(R.xml.prefs_storage);

			mFolderPhotos = PreferenceUtil.getSharedPreferenceString(R.string.key_folder_photos);
			bindPreferenceSummaryToValue(R.string.key_folder_photos);
			bindPreferenceSummaryToValue(R.string.key_max_bitmap_size);
			bindPreferenceSummaryToValue(R.string.key_store_option);
			bindPreferenceSummaryToValue(R.string.key_full_resolution);
		}
		else if (mType.equals(getActivity().getString(R.string.key_dummy_screen_camera_settings))) {
			addPreferencesFromResource(R.xml.prefs_camera);
			bindPreferenceSummaryToValue(R.string.key_camera_api_version);
			bindPreferenceSummaryToValue(R.string.key_camera_screen_position);

			if (getString(R.string.pref_title_folder_input).length() > 0) {
				// Eye photo sequence is in input settings and can be hidden here.
				getPreferenceScreen().removePreference(findPreference(getString(R.string.key_eye_sequence_choice)));
			}
			if (!SystemUtil.isAndroid5()) {
				getPreferenceScreen().removePreference(findPreference(getString(R.string.key_camera_api_version)));
			}
			if (!SystemUtil.hasFlashlight()) {
				getPreferenceScreen().removePreference(findPreference(getString(R.string.key_enable_flash)));
			}
		}
		else if (mType.equals(getActivity().getString(R.string.key_dummy_screen_overlay_settings))) {
			PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(getActivity());
			setPreferenceScreen(preferenceScreen);

			String[] buttonStrings = getResources().getStringArray(R.array.overlay_button_strings);

			for (int btnIndex = 1; btnIndex < DisplayImageFragment.OVERLAY_BUTTON_COUNT; btnIndex++) {
				ListPreference preference = new ListPreference(getActivity());
				preference.setTitle(String.format(getString(R.string.pref_title_overlay_button), buttonStrings[btnIndex]));

				updateOverlayPreferenceEntries(btnIndex, preference);

				preference.setKey(PreferenceUtil.getIndexedPreferenceKey(R.string.key_indexed_overlaytype, btnIndex));
				bindPreferenceSummaryToValue(preference);

				preferenceScreen.addPreference(preference);
			}
		}
		else if (mType.equals(getActivity().getString(R.string.key_dummy_screen_premium_settings))) {
			addPreferencesFromResource(R.xml.prefs_premium);

			addDeveloperContactButtonListener();
			addUnlockerAppButtonListener();

			int permission = ContextCompat.checkSelfPermission(getActivity(), "com.android.vending.BILLING");

			if (permission == PackageManager.PERMISSION_GRANTED) {
				initializeGoogleBilling();
			}
			else {
				ActivityCompat.requestPermissions(getActivity(), new String[] {"com.android.vending.BILLING"}, REQUEST_CODE_PERMISSION);
			}
		}

	}

	/**
	 * Initialize Google Billing (after having permission).
	 */
	public final void initializeGoogleBilling() {
		GoogleBillingHelper.initialize(getActivity(), mOnInventoryFinishedListener);
	}

	/**
	 * Add the listener for a "hints" button.
	 *
	 * @param preferenceId        The id of the button.
	 * @param hintPreferenceValue The value to be set to all the hints preferences.
	 */
	private void addHintButtonListener(final int preferenceId, final boolean hintPreferenceValue) {
		Preference showPreference = findPreference(getString(preferenceId));
		showPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(final Preference preference) {
				PreferenceUtil.setAllHints(hintPreferenceValue);
				DialogUtil.displayInfo(getActivity(), null,
						hintPreferenceValue ? R.string.message_dialog_no_hints_will_be_shown : R.string.message_dialog_hints_will_be_shown);
				return true;
			}
		});
	}

	/**
	 * Add the listener for unlocker app.
	 */
	private void addUnlockerAppButtonListener() {
		Preference unlockPreference = findPreference(getString(R.string.key_dummy_unlocker_app));
		unlockPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(final Preference preference) {
				Intent googlePlayIntent = new Intent(Intent.ACTION_VIEW);
				googlePlayIntent.setData(Uri.parse("market://details?id=de.jeisfeld.augendiagnoseunlocker"));
				try {
					startActivity(googlePlayIntent);
				}
				catch (Exception e) {
					DialogUtil.displayError(getActivity(), R.string.message_dialog_failed_to_open_google_play, false);
				}
				return true;
			}
		});
		unlockPreference.setEnabled(!SystemUtil.isAppInstalled("de.jeisfeld.augendiagnoseunlocker"));
	}

	/**
	 * Add the listener for developer contact.
	 */
	private void addDeveloperContactButtonListener() {
		Preference contactPreference = findPreference(getString(R.string.key_dummy_contact_developer));
		contactPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(final Preference preference) {
				Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
						"mailto", getString(R.string.menu_email_contact_developer), null));
				intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.menu_subject_contact_developer));

				startActivity(intent);
				return true;
			}
		});
	}

	/**
	 * Set the choice entries for all overlay button preferences.
	 */
	private void updateOverlayPreferenceEntries() {
		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
			ListPreference preference = (ListPreference) getPreferenceScreen().getPreference(i);
			Integer buttonIndex = PreferenceUtil.getIndexFromPreferenceKey(preference.getKey());
			if (buttonIndex != null) {
				updateOverlayPreferenceEntries(buttonIndex, preference);
			}
		}
	}

	/**
	 * Set the choice entries for an overlay button preference.
	 *
	 * @param buttonIndex The index of the button.
	 * @param preference  The preference.
	 */
	private void updateOverlayPreferenceEntries(final int buttonIndex, @NonNull final ListPreference preference) {
		int highestOverlayButtonIndex = DisplayImageFragment.getHighestOverlayButtonIndex();
		if (buttonIndex > highestOverlayButtonIndex + 1) {
			preference.setEnabled(false);
			return;
		}
		else {
			preference.setEnabled(true);
		}

		String[] buttonStrings = getResources().getStringArray(R.array.overlay_button_strings);
		String[] overlayNames = getResources().getStringArray(R.array.overlay_names);
		boolean showUnused = buttonIndex >= highestOverlayButtonIndex;
		int numberOfEntries = showUnused ? OverlayPinchImageView.OVERLAY_COUNT - 1 : OverlayPinchImageView.OVERLAY_COUNT - 2;

		CharSequence[] entries = new CharSequence[numberOfEntries];
		CharSequence[] entryValues = new CharSequence[numberOfEntries];

		for (int i = 0; i < OverlayPinchImageView.OVERLAY_COUNT - 2; i++) {
			int overlayIndex = i + 1;
			entryValues[i] = Integer.toString(overlayIndex);
			Integer currentButton = DisplayImageFragment.buttonForOverlayWithIndex(overlayIndex);

			SpannableString entry;
			if (currentButton == null) {
				entry = new SpannableString(overlayNames[overlayIndex]);
			}
			else {
				entry = new SpannableString(buttonStrings[currentButton] + " " + overlayNames[overlayIndex]);
				if (currentButton == buttonIndex) {
					entry.setSpan(new StyleSpan(Typeface.BOLD), 0, entry.length(), 0);
				}
				else {
					entry.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, entry.length(), 0);
				}
			}
			entries[i] = entry;
		}
		if (showUnused) {
			SpannableString entry = new SpannableString(getString(R.string.pref_value_overlay_button_empty));
			entry.setSpan(new StyleSpan(Typeface.ITALIC), 0, entry.length(), 0);
			entries[OverlayPinchImageView.OVERLAY_COUNT - 2] = entry;
			entryValues[OverlayPinchImageView.OVERLAY_COUNT - 2] = "-1";
		}

		preference.setEntries(entries);
		preference.setEntryValues(entryValues);
	}

	/**
	 * Binds a preference's summary to its value. More specifically, when the preference's value is changed, its summary
	 * (line of text below the preference title) is updated to reflect the value. The summary is also immediately
	 * updated upon calling this method. The exact display format is dependent on the type of preference.
	 *
	 * @param preference The preference to be bound.
	 */
	private void bindPreferenceSummaryToValue(@Nullable final Preference preference) {
		if (preference != null) {
			// Set the listener to watch for value changes.
			preference.setOnPreferenceChangeListener(mOnPreferenceChangeListener);

			// Trigger the listener immediately to set the summary
			mOnPreferenceChangeListener.setSummary(preference, PreferenceManager
					.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
		}
	}

	/**
	 * Helper method for easier call of {@link #bindPreferenceSummaryToValue(android.preference.Preference)}.
	 *
	 * @param preferenceKey The key of the preference.
	 */
	private void bindPreferenceSummaryToValue(final int preferenceKey) {
		bindPreferenceSummaryToValue(findPreference(getString(preferenceKey)));
	}

	/**
	 * A listener handling the response after reading the in-add purchase inventory.
	 */
	private final OnInventoryFinishedListener mOnInventoryFinishedListener = new OnInventoryFinishedListener() {
		@Override
		public void handleProducts(@NonNull final List<PurchasedSku> purchases,
								   @NonNull final List<SkuDetails> availableProducts, final boolean isPremium) {
			// List inventory items.
			for (PurchasedSku purchase : purchases) {
				Preference purchasePreference = new Preference(getActivity());
				String title = String.format(getString(R.string.button_purchased_item), purchase.getSkuDetails().getDisplayTitle(getActivity()));
				purchasePreference.setTitle(title);
				purchasePreference.setSummary(purchase.getSkuDetails().getDescription());
				purchasePreference.setEnabled(false);

				getPreferenceScreen().addPreference(purchasePreference);
			}
			for (SkuDetails skuDetails : availableProducts) {
				Preference skuPreference = new Preference(getActivity());
				skuPreference.setTitle(skuDetails.getDisplayTitle(getActivity()));
				skuPreference.setKey(SKU_KEY_PREFIX + skuDetails.getSku());
				skuPreference.setSummary(skuDetails.getDescription());
				skuPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(@NonNull final Preference preference) {
						String productId = preference.getKey().substring(SKU_KEY_PREFIX.length());
						GoogleBillingHelper.launchPurchaseFlow(productId, mOnPurchaseSuccessListener);
						return false;
					}
				});
				getPreferenceScreen().addPreference(skuPreference);
			}
		}
	};

	/**
	 * A listener handling the response after purchasing a product.
	 */
	private final OnPurchaseSuccessListener mOnPurchaseSuccessListener = new OnPurchaseSuccessListener() {
		@Override
		public void handlePurchase(final Purchase purchase, final boolean addedPremiumProduct) {
			if (addedPremiumProduct) {
				PreferenceUtil.setSharedPreferenceBoolean(R.string.key_internal_has_premium_pack, true);
			}
			int messageResource = addedPremiumProduct ? R.string.message_dialog_purchase_thanks_premium : R.string.message_dialog_purchase_thanks;

			MessageDialogListener listener = new MessageDialogListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void onDialogClick(final DialogFragment dialog) {
					getActivity().finish();
					Application.startApplication(getActivity());
				}

				@Override
				public void onDialogCancel(final DialogFragment dialog) {
					getActivity().finish();
					Application.startApplication(getActivity());
				}
			};

			DialogUtil.displayInfo(getActivity(), listener, messageResource);
		}

		@Override
		public void handleFailure() {
			// Do nothing.
		}
	};

	@Override
	public final void onActivityResult(final int requestCode, final int resultCode, @NonNull final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (SystemUtil.isAndroid5()) {
			onActivityResultLollipop(requestCode, resultCode, data);
		}
	}

	/**
	 * After triggering the Storage Access Framework, ensure that folder is really writable. Set preferences
	 * accordingly.
	 *
	 * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify who
	 *                    this result came from.
	 * @param resultCode  The integer result code returned by the child activity through its setResult().
	 * @param data        An Intent, which can return result data to the caller (various data can be attached to Intent
	 *                    "extras").
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void onActivityResultLollipop(final int requestCode, final int resultCode, @NonNull final Intent data) {
		int preferenceKeyUri;
		int preferenceKeyFolder;
		String oldFolder;
		if (requestCode == REQUEST_CODE_STORAGE_ACCESS_PHOTOS) {
			preferenceKeyUri = R.string.key_internal_uri_extsdcard_photos;
			preferenceKeyFolder = R.string.key_folder_photos;
			oldFolder = mFolderPhotos;
		}
		else if (requestCode == REQUEST_CODE_STORAGE_ACCESS_INPUT) {
			preferenceKeyUri = R.string.key_internal_uri_extsdcard_input;
			preferenceKeyFolder = R.string.key_folder_input;
			oldFolder = mFolderInput;
		}
		else {
			return;
		}

		Uri oldUri = PreferenceUtil.getSharedPreferenceUri(preferenceKeyUri);
		if (oldUri == null) {
			// Backward compatibility to the version where only the base key existed
			oldUri = PreferenceUtil.getSharedPreferenceUri(R.string.key_internal_uri_extsdcard_photos);
		}
		Uri treeUri = null;

		if (resultCode == Activity.RESULT_OK) {
			// Get Uri from Storage Access Framework.
			treeUri = data.getData();
			// Persist URI - this is required for verification of writability.
			PreferenceUtil.setSharedPreferenceUri(preferenceKeyUri, treeUri);
		}

		// If not confirmed SAF, or if still not writable, then revert settings.
		if (resultCode != Activity.RESULT_OK || !FileUtil.isWritableNormalOrSaf(mCurrentFolder)) {
			DialogUtil.displayError(getActivity(), R.string.message_dialog_cannot_write_to_folder_saf, false,
					mCurrentFolder);

			PreferenceUtil.setSharedPreferenceString(preferenceKeyFolder, oldFolder);
			findPreference(getString(preferenceKeyFolder)).setSummary(oldFolder);

			mCurrentFolder = null;
			PreferenceUtil.setSharedPreferenceUri(preferenceKeyUri, oldUri);
			return;
		}

		// After confirmation, update stored value of folder.
		if (preferenceKeyFolder == R.string.key_folder_photos) {
			mFolderPhotos = mCurrentFolder.getAbsolutePath();
		}
		if (preferenceKeyFolder == R.string.key_folder_input) {
			mFolderInput = mCurrentFolder.getAbsolutePath();
		}
		PreferenceUtil.setSharedPreferenceString(preferenceKeyFolder, mCurrentFolder.getAbsolutePath());
		findPreference(getString(preferenceKeyFolder)).setSummary(mCurrentFolder.getAbsolutePath());

		// Persist access permissions.
		final int takeFlags = data.getFlags()
				& (Intent.FLAG_GRANT_READ_URI_PERMISSION
				| Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		// noinspection ResourceType
		getActivity().getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
	}

	@Override
	public final void onDestroy() {
		super.onDestroy();
		if (mType != null && mType.equals(getActivity().getString(R.string.key_dummy_screen_premium_settings))) {
			GoogleBillingHelper.dispose();
		}
	}

	/**
	 * A preference value change listener that updates the preference's summary to reflect its new value.
	 */
	private class CustomOnPreferenceChangeListener implements OnPreferenceChangeListener {
		@Override
		public boolean onPreferenceChange(@NonNull final Preference preference, @NonNull final Object value) {
			String stringValue = value.toString();

			// For maxBitmapSize, inform PinchImageView
			if (preference.getKey().equals(preference.getContext().getString(R.string.key_max_bitmap_size))) {
				PinchImageView.setMaxBitmapSize(Integer.parseInt(stringValue));
			}

			// For folder choices, if not writable on Android 5, then trigger Storage Access Framework.
			else if (preference.getKey().equals(preference.getContext().getString(R.string.key_folder_photos))) {
				if (mFolderPhotos == null || !mFolderPhotos.equals(value)) {
					mCurrentFolder = new File(stringValue);
					if (checkFolder(mCurrentFolder, REQUEST_CODE_STORAGE_ACCESS_PHOTOS)) {
						mFolderPhotos = mCurrentFolder.getAbsolutePath();
					}
					else {
						// Do not accept change.
						return false;
					}
				}
			}
			else if (preference.getKey().equals(preference.getContext().getString(R.string.key_folder_input))) {
				if (mFolderInput == null || !mFolderInput.equals(value)) {
					mCurrentFolder = new File(stringValue);
					if (checkFolder(mCurrentFolder, REQUEST_CODE_STORAGE_ACCESS_INPUT)) {
						mFolderInput = mCurrentFolder.getAbsolutePath();
					}
					else {
						// Do not accept change.
						return false;
					}
				}
			}

			// Apply change of language
			else if (preference.getKey().equals(preference.getContext().getString(R.string.key_language))) {
				String oldLanguageString = PreferenceUtil.getSharedPreferenceString(R.string.key_language);
				if (oldLanguageString == null || !oldLanguageString.equals(value)) {
					Application.setLanguage();
					PreferenceUtil.setSharedPreferenceString(R.string.key_language, stringValue);

					// Workaround to get rid of all kinds of cashing
					if (!JpegSynchronizationUtil.isSaving()) {
						Application.startApplication(getActivity());
						System.exit(0);
					}

				}
			}

			else if (preference.getKey().startsWith(getString(R.string.key_indexed_overlaytype))) {
				int overlayIndex = Integer.parseInt(stringValue);
				Integer buttonPosition = PreferenceUtil.getIndexFromPreferenceKey(preference.getKey());
				if (buttonPosition == null) {
					buttonPosition = 0;
				}
				Integer oldButtonPosition = DisplayImageFragment.buttonForOverlayWithIndex(overlayIndex);
				int oldOverlayIndex =
						PreferenceUtil.getIndexedSharedPreferenceIntString(R.string.key_indexed_overlaytype, buttonPosition, -1);

				if (oldOverlayIndex != overlayIndex) {
					if (Application.getAuthorizationLevel() == AuthorizationLevel.TRIAL_ACCESS
							&& overlayIndex >= Integer.parseInt(Application.getResourceString(R.string.overlay_trial_count))) {
						DialogUtil.displayAuthorizationError(getActivity(), R.string.message_dialog_trial_overlays);
						return false;
					}

					if (oldButtonPosition != null && !oldButtonPosition.equals(buttonPosition)) {
						// If the same overlay is already used, switch overlays
						PreferenceUtil.setIndexedSharedPreferenceIntString(R.string.key_indexed_overlaytype, oldButtonPosition,
								oldOverlayIndex);

						ListPreference oldButtonPreference = (ListPreference) findPreference(
								PreferenceUtil.getIndexedPreferenceKey(R.string.key_indexed_overlaytype, oldButtonPosition));
						updateSummaryForOverlayPreference(oldButtonPreference);
					}

					PreferenceUtil.setIndexedSharedPreferenceString(R.string.key_indexed_overlaytype, buttonPosition, stringValue);
					updateOverlayPreferenceEntries();
				}
			}

			setSummary(preference, stringValue);

			return true;
		}

		/**
		 * Update the summary for a preference.
		 *
		 * @param preference The preference.
		 * @param value      the new value of the preference.
		 */
		private void setSummary(@NonNull final Preference preference, final String value) {
			if (preference.getClass().equals(ListPreference.class)) {
				// For list preferences (except customized ones), look up the correct display value in the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(value);

				if (preference.getKey().startsWith(getString(R.string.key_indexed_overlaytype))) {
					updateSummaryForOverlayPreference(listPreference);
				}
				else if (index < 0) {
					preference.setSummary(null);
				}
				else {
					String entryValue = listPreference.getEntries()[index].toString();

					preference.setSummary(entryValue);
				}
			}
			else {
				// For all other preferences, set the summary to the value's simple string representation.
				preference.setSummary(value);
			}
		}

		/**
		 * Set the summary for an overlay preference.
		 *
		 * @param preference The overlay preference.
		 */
		private void updateSummaryForOverlayPreference(@Nullable final ListPreference preference) {
			if (preference != null && preference.getKey().startsWith(getString(R.string.key_indexed_overlaytype))) {
				Integer buttonIndex = PreferenceUtil.getIndexFromPreferenceKey(preference.getKey());
				if (buttonIndex == null) {
					buttonIndex = 0;
				}
				int overlayIndex = PreferenceUtil.getIndexedSharedPreferenceIntString(R.string.key_indexed_overlaytype, buttonIndex, -1);
				if (overlayIndex > 0) {
					String overlayName = getResources().getStringArray(R.array.overlay_names)[overlayIndex];
					preference.setSummary(overlayName);
				}
				else {
					preference.setSummary(R.string.pref_value_overlay_button_empty);
				}
			}
		}

		/**
		 * Check the folder for writeability. If not, then on Android 5 retrieve Uri for extsdcard via Storage
		 * Access Framework.
		 *
		 * @param folder The folder to be checked.
		 * @param code   The request code of the type of folder check.
		 * @return true if the check was successful or if SAF has been triggered.
		 */
		private boolean checkFolder(@NonNull final File folder, final int code) {
			if (SystemUtil.isAndroid5() && FileUtil.isOnExtSdCard(folder)) {
				if (!folder.exists() || !folder.isDirectory()) {
					return false;
				}

				// On Android 5, trigger storage access framework.
				if (!FileUtil.isWritableNormalOrSaf(folder)) {
					// Ensure via listener that storage access framework is called only after information
					// message.
					MessageDialogListener listener = new MessageDialogListener() {
						/**
						 * Default serial version id.
						 */
						private static final long serialVersionUID = 1L;

						@Override
						public void onDialogClick(final DialogFragment dialog) {
							triggerStorageAccessFramework(code);
						}

						@Override
						public void onDialogCancel(final DialogFragment dialog) {
							// do nothing.
						}
					};

					DialogUtil.displayInfo(getActivity(), listener, R.string.message_dialog_select_extsdcard);
					return false;
				}
				// Only accept after SAF stuff is done.
				return true;
			}
			else if (SystemUtil.isKitkat() && FileUtil.isOnExtSdCard(folder)) {
				// Assume that Kitkat workaround works
				return true;
			}
			else if (FileUtil.isWritable(new File(folder, "DummyFile"))) {
				return true;
			}
			else {
				DialogUtil.displayError(getActivity(), R.string.message_dialog_cannot_write_to_folder, false,
						mCurrentFolder);

				mCurrentFolder = null;
				return false;
			}
		}

		/**
		 * Trigger the storage access framework to access the base folder of the ext sd card.
		 *
		 * @param code The request code to be used.
		 */
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		private void triggerStorageAccessFramework(final int code) {
			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
			startActivityForResult(intent, code);
		}

	}
}
