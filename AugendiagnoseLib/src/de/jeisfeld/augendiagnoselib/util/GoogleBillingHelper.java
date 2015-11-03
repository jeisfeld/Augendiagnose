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
import de.jeisfeld.augendiagnoselib.R;

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
	private static final String[] PRODUCT_IDS = Application.getAppContext().getResources().getStringArray(R.array.googlebilling_ids);

	/**
	 * The product ids which set premium status.
	 */
	private static final String[] PREMIUM_IDS = Application.getAppContext().getResources().getStringArray(R.array.googlebilling_premium_ids);

	/**
	 * The product ids which are subscriptions.
	 */
	private static final String[] SUBSCRIPTION_IDS =
			Application.getAppContext().getResources().getStringArray(R.array.googlebilling_subscription_ids);

	/**
	 * An instance of GoogleBillingHelper.
	 */
	private static GoogleBillingHelper mInstance;

	/**
	 * Helper class for Google Billing.
	 */
	private IabHelper mIabHelper;

	/**
	 * The activity starting Google Billing.
	 */
	private Activity mActivity;

	/**
	 * An onInventoryFinishedListener called after inventory has been retrieved.
	 */
	private OnInventoryFinishedListener mOnInventoryFinishedListener;

	/**
	 * An onPurchaseSuccessListener called after a purchase has been completed.
	 */
	private OnPurchaseSuccessListener mOnPurchaseSuccessListener;

	/**
	 * The list of purchases.
	 */
	private List<PurchasedSku> mPurchases = null;

	/**
	 * The list of non-purchased products.
	 */
	private List<SkuDetails> mNonPurchases = null;

	/**
	 * Flag indicating if there is a purchase setting premium status.
	 */
	private boolean mIsPremium = false;

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
			if (mInstance != null) {
				if (mInstance.mActivity == activity) {
					return;
				}
				else {
					dispose();
				}
			}
			mInstance = new GoogleBillingHelper();
			mInstance.mActivity = activity;
			mInstance.mOnInventoryFinishedListener = listener;
		}
		mInstance.initialize();
	}

	/**
	 * Initialize the GoogleBillingHelper.
	 */
	private void initialize() {
		String base64EncodedPublicKey = Application.getResourceString(R.string.private_license_key);

		// compute your public key and store it in base64EncodedPublicKey
		mIabHelper = new IabHelper(mActivity, base64EncodedPublicKey);
		mIabHelper.enableDebugLogging(false, TAG);
		mIabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			@Override
			public void onIabSetupFinished(final IabResult result) {
				if (result.isSuccess()) {
					Log.d(TAG, "Finished IAB setup");
					mIabHelper.queryInventoryAsync(true, Arrays.asList(PRODUCT_IDS), mGotInventoryListener);
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
		if (mInstance == null || mInstance.mIabHelper == null) {
			throw new NullPointerException(
					"Tried to launch purchase flow without having GoogleBillingHelper initialized");
		}
		mInstance.mOnPurchaseSuccessListener = listener;

		if (Arrays.asList(SUBSCRIPTION_IDS).contains(productId)) {
			Log.d(TAG, "Starting subscription purchase flow for " + productId);
			mInstance.mIabHelper.launchSubscriptionPurchaseFlow(mInstance.mActivity, productId, REQUEST_CODE,
					mInstance.mPurchaseFinishedListener);
		}
		else {
			Log.d(TAG, "Starting product purchase flow for " + productId);
			mInstance.mIabHelper.launchPurchaseFlow(mInstance.mActivity, productId, REQUEST_CODE,
					mInstance.mPurchaseFinishedListener);
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
			if (mInstance != null && mInstance.mIabHelper != null) {
				mInstance.mIabHelper.handleActivityResult(requestCode, resultCode, data);
			}
		}
	}

	/**
	 * Clean up GoogleBillingHelper instance.
	 */
	public static void dispose() {
		Log.d(TAG, "Disposing GoogleBillingHelper");
		synchronized (GoogleBillingHelper.class) {
			if (mInstance != null) {
				if (mInstance.mIabHelper != null) {
					mInstance.mIabHelper.dispose();
				}
				mInstance = null;
			}
		}
	}

	/**
	 * The onInventoryFinishedListener started after the inventory is loaded.
	 */
	private IabHelper.QueryInventoryFinishedListener mGotInventoryListener =
			new IabHelper.QueryInventoryFinishedListener() {
				@Override
				public void onQueryInventoryFinished(final IabResult result, final Inventory inventory) {
					Log.d(TAG, "Query inventory finished - " + inventory);

					// Have we been disposed of in the meantime? If so, quit.
					if (mIabHelper == null) {
						return;
					}

					// Is it a failure?
					if (result.isFailure()) {
						Log.e(TAG, "Failed to query inventory: " + result);
						return;
					}

					Log.d(TAG, "Query inventory was successful.");

					synchronized (GoogleBillingHelper.class) {
						mPurchases = new ArrayList<PurchasedSku>();
						mNonPurchases = new ArrayList<SkuDetails>();
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
									mNonPurchases.add(skuDetails);
								}
								else {
									Log.d(TAG, "Found purchase: " + purchase);
									mPurchases.add(new PurchasedSku(skuDetails, purchase));
									if (Arrays.asList(PREMIUM_IDS).contains(purchase.getSku())) {
										mIsPremium = true;
									}
								}
							}
						}

					}

					if (mOnInventoryFinishedListener != null) {
						mOnInventoryFinishedListener.handleProducts(mPurchases, mNonPurchases, mIsPremium);
					}
				}
			};

	/**
	 * Callback for when a purchase is finished.
	 */
	private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener =
			new IabHelper.OnIabPurchaseFinishedListener() {
				@Override
				public void onIabPurchaseFinished(final IabResult result, final Purchase purchase) {
					Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

					if (result.isFailure()) {
						Log.e(TAG, "Error purchasing: " + result);
						return;
					}

					Log.d(TAG, "Purchase successful.");

					if (mOnPurchaseSuccessListener != null) {
						boolean isPremiumProduct = Arrays.asList(PREMIUM_IDS).contains(purchase.getSku());
						mOnPurchaseSuccessListener.handlePurchase(purchase, isPremiumProduct && !mIsPremium);
						mIsPremium = mIsPremium || isPremiumProduct;
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
