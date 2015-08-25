package de.jeisfeld.augendiagnoselib.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.android.vending.billing.IabHelper;
import com.android.vending.billing.IabResult;
import com.android.vending.billing.Inventory;
import com.android.vending.billing.Purchase;
import com.android.vending.billing.PurchasedSku;
import com.android.vending.billing.SkuDetails;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import de.jeisfeld.augendiagnoselib.Application;

/**
 * Utility class to support in-ad purchases via Google Billing.
 */
public final class GoogleBillingHelper {
	/**
	 * The request code used for starting the payment activity - any number.
	 */
	private static final int REQUEST_CODE = 14254;

	/**
	 * The logging tag for this class.
	 */
	private static final String TAG = "GoogleBillingHelper";

	/**
	 * The product ids to be offered.
	 */
	private static final String[] PRODUCT_IDS =
			new String[] { "donation_monthly_small", "donation_big", "donation_medium",
					"donation_small" };

	/**
	 * The product ids which set premium status.
	 */
	private static final String[] PREMIUM_IDS =
			new String[] { "donation_monthly_small", "donation_big", "donation_medium" };

	/**
	 * The product ids which are subscriptions.
	 */
	private static final String[] SUBSCRIPTION_IDS =
			new String[] { "donation_monthly_small" };

	/**
	 * An instance of GoogleBillingHelper.
	 */
	private static GoogleBillingHelper instance;

	/**
	 * Helper class for Google Billing.
	 */
	private IabHelper iabHelper;

	/**
	 * The activity starting Google Billing.
	 */
	private Activity activity;

	/**
	 * An onInventoryFinishedListener called after inventory has been retrieved.
	 */
	private OnInventoryFinishedListener onInventoryFinishedListener;

	/**
	 * An onPurchaseSuccessListener called after a purchase has been completed.
	 */
	private OnPurchaseSuccessListener onPurchaseSuccessListener;

	/**
	 * The list of purchases.
	 */
	private List<PurchasedSku> purchases = null;

	/**
	 * The list of non-purchased products.
	 */
	private List<SkuDetails> nonPurchases = null;

	/**
	 * Flag indicating if there is a purchase setting premium status.
	 */
	private boolean isPremium = false;

	/**
	 * Hide default constructor.
	 */
	private GoogleBillingHelper() {
		// hide default constructor.
	}

	/**
	 * Initialize an instance of GoogleBillingHelper.
	 *
	 * @param activity
	 *            The activity triggering Google Billing.
	 * @param listener
	 *            a listener called after the inventory has been retrieved.
	 */
	public static void initialize(final Activity activity, final OnInventoryFinishedListener listener) {
		synchronized (GoogleBillingHelper.class) {
			if (instance != null) {
				if (instance.activity == activity) {
					return;
				}
				else {
					dispose();
				}
			}
			instance = new GoogleBillingHelper();
			instance.activity = activity;
			instance.onInventoryFinishedListener = listener;
		}
		instance.initialize();
	}

	/**
	 * Initialize the GoogleBillingHelper.
	 */
	private void initialize() {

		String base64EncodedPublicKey = null;
		try {
			PrivateConstants privateConstants = Application.getPrivateConstants();
			base64EncodedPublicKey = privateConstants.getLicenseKey();
		}
		catch (Exception e) {
			Log.e(TAG, "Did not find PrivateConstants", e);
			return;
		}

		// compute your public key and store it in base64EncodedPublicKey
		iabHelper = new IabHelper(activity, base64EncodedPublicKey);
		iabHelper.enableDebugLogging(false, TAG);
		iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			@Override
			public void onIabSetupFinished(final IabResult result) {
				if (result.isSuccess()) {
					Log.d(TAG, "Finished IAB setup");
					iabHelper.queryInventoryAsync(true, Arrays.asList(PRODUCT_IDS), gotInventoryListener);
				}
				else {
					Log.e(TAG, "Problem setting up In-app Billing: " + result);
				}
			}
		});
	}

	/**
	 * Launch the purchase flow for a product.
	 *
	 * @param productId
	 *            The productId.
	 * @param listener
	 *            a listener called after the purchase has been completed.
	 */
	public static void launchPurchaseFlow(final String productId, final OnPurchaseSuccessListener listener) {
		if (instance == null || instance.iabHelper == null) {
			throw new NullPointerException(
					"Tried to launch purchase flow without having GoogleBillingHelper initialized");
		}
		instance.onPurchaseSuccessListener = listener;

		if (Arrays.asList(SUBSCRIPTION_IDS).contains(productId)) {
			Log.d(TAG, "Starting subscription purchase flow for " + productId);
			instance.iabHelper.launchSubscriptionPurchaseFlow(instance.activity, productId, REQUEST_CODE,
					instance.purchaseFinishedListener);
		}
		else {
			Log.d(TAG, "Starting product purchase flow for " + productId);
			instance.iabHelper.launchPurchaseFlow(instance.activity, productId, REQUEST_CODE,
					instance.purchaseFinishedListener);
		}
	}

	/**
	 * To be called in the onActivityResult method of the activity launching the purchase flow.
	 *
	 * @param requestCode
	 *            The integer request code originally supplied to startActivityForResult(), allowing you to identify who
	 *            this result came from.
	 * @param resultCode
	 *            The integer result code returned by the child activity through its setResult().
	 * @param data
	 *            An Intent, which can return result data to the caller (various data can be attached to Intent
	 *            "extras").
	 */
	public static void handleActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (requestCode == REQUEST_CODE) {
			if (instance != null && instance.iabHelper != null) {
				instance.iabHelper.handleActivityResult(requestCode, resultCode, data);
			}
		}
	}

	/**
	 * Clean up GoogleBillingHelper instance.
	 */
	public static void dispose() {
		synchronized (GoogleBillingHelper.class) {
			if (instance != null) {
				if (instance.iabHelper != null) {
					instance.iabHelper.dispose();
				}
				instance = null;
			}
		}
	}

	/**
	 * The onInventoryFinishedListener started after the inventory is loaded.
	 */
	private IabHelper.QueryInventoryFinishedListener gotInventoryListener =
			new IabHelper.QueryInventoryFinishedListener() {
				@Override
				public void onQueryInventoryFinished(final IabResult result, final Inventory inventory) {
					Log.d(TAG, "Query inventory finished - " + inventory);

					// Have we been disposed of in the meantime? If so, quit.
					if (iabHelper == null) {
						return;
					}

					// Is it a failure?
					if (result.isFailure()) {
						Log.e(TAG, "Failed to query inventory: " + result);
						return;
					}

					Log.d(TAG, "Query inventory was successful.");

					synchronized (GoogleBillingHelper.class) {
						purchases = new ArrayList<PurchasedSku>();
						nonPurchases = new ArrayList<SkuDetails>();
					}
					for (String purchaseId : Arrays.asList(PRODUCT_IDS)) {
						Purchase purchase = inventory.getPurchase(purchaseId);
						SkuDetails skuDetails = inventory.getSkuDetails(purchaseId);

						if (purchase == null && skuDetails == null) {
							Log.w(TAG, "Did not find entry for " + purchaseId);
						}
						else {
							synchronized (GoogleBillingHelper.class) {
								if (purchase == null) {
									nonPurchases.add(skuDetails);
								}
								else {
									Log.d(TAG, "Found purchase: " + purchase);
									purchases.add(new PurchasedSku(skuDetails, purchase));
									if (Arrays.asList(PREMIUM_IDS).contains(purchase.getSku())) {
										isPremium = true;
									}
								}
							}
						}

					}

					if (onInventoryFinishedListener != null) {
						onInventoryFinishedListener.handleProducts(purchases, nonPurchases, isPremium);
					}
				}
			};

	/**
	 * Callback for when a purchase is finished.
	 */
	private IabHelper.OnIabPurchaseFinishedListener purchaseFinishedListener =
			new IabHelper.OnIabPurchaseFinishedListener() {
				@Override
				public void onIabPurchaseFinished(final IabResult result, final Purchase purchase) {
					Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

					// if we were disposed of in the meantime, quit.
					if (iabHelper == null) {
						return;
					}

					if (result.isFailure()) {
						Log.e(TAG, "Error purchasing: " + result);
						return;
					}

					Log.d(TAG, "Purchase successful.");

					if (onPurchaseSuccessListener != null) {
						boolean isPremiumProduct = Arrays.asList(PREMIUM_IDS).contains(purchase.getSku());
						onPurchaseSuccessListener.handlePurchase(purchase, isPremiumProduct && !isPremium);
						isPremium = isPremium || isPremiumProduct;
					}
				}
			};

	/**
	 * Listener to be called after inventory has been retrieved.
	 */
	public interface OnInventoryFinishedListener {
				/**
				 * Handler called after inventory has been retrieved.
				 *
				 * @param purchases
				 *            The list of bought purchases.
				 * @param availableProducts
				 *            The list of available products.
				 * @param isPremium
				 *            Flag indicating if there is a purchase setting premium status.
				 */
				void handleProducts(List<PurchasedSku> purchases, List<SkuDetails> availableProducts,
						boolean isPremium);
	}

	/**
	 * Listener to be called after a purchase has been successfully completed.
	 */
	public interface OnPurchaseSuccessListener {
				/**
				 * Handler called after a purchase has been successfully completed.
				 *
				 * @param purchase
				 *            The completed purchase.
				 * @param addedPremiumProduct
				 *            Flag indicating if there was a premium upgrade.
				 */
				void handlePurchase(Purchase purchase, boolean addedPremiumProduct);
	}
}
