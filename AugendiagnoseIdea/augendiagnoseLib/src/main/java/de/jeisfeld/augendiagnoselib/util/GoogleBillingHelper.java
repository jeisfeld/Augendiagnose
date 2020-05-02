package de.jeisfeld.augendiagnoselib.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponseCode;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.Purchase.PurchaseState;
import com.android.billingclient.api.Purchase.PurchasesResult;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;

/**
 * Utility class to support in-ad purchases via Google Billing.
 */
public final class GoogleBillingHelper implements PurchasesUpdatedListener {
	/**
	 * The logging tag for this class.
	 */
	private static final String TAG = "JE.Aug.GoogleBilling";

	/**
	 * The product ids to be offered.
	 */
	private static final List<String> INAPP_PRODUCT_IDS =
			Arrays.asList(Application.getAppContext().getResources().getStringArray(R.array.googlebilling_inapp_ids));

	/**
	 * The product ids which are subscriptions.
	 */
	private static final List<String> SUBSCRIPTION_IDS =
			Arrays.asList(Application.getAppContext().getResources().getStringArray(R.array.googlebilling_subscription_ids));

	/**
	 * An instance of GoogleBillingHelper.
	 */
	@Nullable
	private static GoogleBillingHelper mInstance;

	/**
	 * The context.
	 */
	private Context mContext;

	/**
	 * An onPurchaseSuccessListener called after a purchase has been completed.
	 */
	private OnPurchaseSuccessListener mOnPurchaseSuccessListener;

	/**
	 * Flag indicating if there is a purchase setting premium status.
	 */
	private boolean mIsPremium = false;

	/**
	 * The Billing client connection.
	 */
	private BillingClient mBillingClient;

	/**
	 * Flag indicating if billing client is connected.
	 */
	private boolean mIsConnected = false;
	/**
	 * Found in app SKUs.
	 */
	private List<SkuPurchase> mInAppSkus = null;
	/**
	 * Found subscription SKUs.
	 */
	private List<SkuPurchase> mSubscriptionSkus = null;

	/**
	 * Hide default constructor.
	 */
	private GoogleBillingHelper() {
		// hide default constructor.
	}

	/**
	 * Get an instance of Google Billing Helper.
	 *
	 * @param context The context.
	 * @return an instance of GoogleBillingHelper.
	 */
	public static GoogleBillingHelper getInstance(final Context context) {
		synchronized (GoogleBillingHelper.class) {
			if (mInstance == null) {
				mInstance = new GoogleBillingHelper();
				mInstance.mContext = context;
				mInstance.mBillingClient = BillingClient.newBuilder(context).setListener(mInstance).enablePendingPurchases().build();
			}
		}
		return mInstance;
	}

	/**
	 * Get information if there is a premium pack for the app. If there is no connection, then establish connection before.
	 *
	 * @param listener The listener called when the information is available.
	 */
	public void hasPremiumPack(final OnPurchaseQueryCompletedListener listener) {
		if (mIsConnected) {
			int premiumPackState = doHasPremiumPack();
			listener.onHasPremiumPack(premiumPackState == PurchaseState.PURCHASED, premiumPackState == PurchaseState.PENDING);
		}
		else {
			mBillingClient.startConnection(new BillingClientStateListener() {
				@Override
				public void onBillingSetupFinished(final BillingResult billingResult) {
					if (billingResult.getResponseCode() == BillingResponseCode.OK) {
						Log.d(TAG, "Google Billing Connection established.");
						mIsConnected = true;
						int premiumPackState = doHasPremiumPack();
						listener.onHasPremiumPack(premiumPackState == PurchaseState.PURCHASED, premiumPackState == PurchaseState.PENDING);
					}
					else {
						Log.i(TAG, "Google Billing Connection failed - " + billingResult.getDebugMessage());
						mIsConnected = false;
					}
				}

				@Override
				public void onBillingServiceDisconnected() {
					Log.d(TAG, "Google Billing Connection lost.");
					mIsConnected = false;
				}
			});
		}
	}

	/**
	 * Get information if there is a premium pack for the app.
	 *
	 * @return The purchase state if existing, otherwise -1
	 */
	private int doHasPremiumPack() {
		PurchasesResult purchasesResult = mBillingClient.queryPurchases(SkuType.INAPP);
		int result = -1;
		if (purchasesResult.getPurchasesList() != null) {
			for (Purchase purchase : purchasesResult.getPurchasesList()) {
				result = PurchaseState.PENDING;
				if (INAPP_PRODUCT_IDS.contains(purchase.getSku())) {
					if (purchase.getPurchaseState() == PurchaseState.PURCHASED) {
						doAcknowledgePurchaseIfRequired(purchase);
						return PurchaseState.PURCHASED;
					}
				}
			}
		}
		purchasesResult = mBillingClient.queryPurchases(SkuType.SUBS);
		if (purchasesResult.getPurchasesList() != null) {
			for (Purchase purchase : purchasesResult.getPurchasesList()) {
				result = PurchaseState.PENDING;
				if (SUBSCRIPTION_IDS.contains(purchase.getSku())) {
					if (purchase.getPurchaseState() == PurchaseState.PURCHASED) {
						doAcknowledgePurchaseIfRequired(purchase);
						return PurchaseState.PURCHASED;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Query SKU details. If no connection available, establish connection.
	 *
	 * @param listener The listener called when the query is finished.
	 */
	public void querySkuDetails(final OnInventoryFinishedListener listener) {
		if (mIsConnected) {
			doQuerySkuDetails(listener);
		}
		else {
			mBillingClient.startConnection(new BillingClientStateListener() {
				@Override
				public void onBillingSetupFinished(final BillingResult billingResult) {
					if (billingResult.getResponseCode() == BillingResponseCode.OK) {
						Log.d(TAG, "Google Billing Connection established.");
						mIsConnected = true;
						doQuerySkuDetails(listener);
					}
					else {
						Log.i(TAG, "Google Billing Connection failed - " + billingResult.getDebugMessage());
						mIsConnected = false;
					}
				}

				@Override
				public void onBillingServiceDisconnected() {
					Log.d(TAG, "Google Billing Connection lost.");
					mIsConnected = false;
				}
			});
		}
	}

	/**
	 * Query SKU details.
	 *
	 * @param listener The listener called when the query is finished.
	 */
	private void doQuerySkuDetails(final OnInventoryFinishedListener listener) {
		synchronized (this) {
			mInAppSkus = null;
			mSubscriptionSkus = null;
		}
		mIsPremium = false;
		mBillingClient.querySkuDetailsAsync(
				SkuDetailsParams.newBuilder().setSkusList(INAPP_PRODUCT_IDS).setType(SkuType.INAPP).build(),
				new SkuDetailsResponseListener() {
					@Override
					public void onSkuDetailsResponse(final BillingResult result, final List<SkuDetails> list) {
						GoogleBillingHelper.this.onSkuDetailsResponse(result, list, false, listener);
					}
				});
		mBillingClient.querySkuDetailsAsync(
				SkuDetailsParams.newBuilder().setSkusList(SUBSCRIPTION_IDS).setType(SkuType.SUBS).build(),
				new SkuDetailsResponseListener() {
					@Override
					public void onSkuDetailsResponse(final BillingResult result, final List<SkuDetails> list) {
						GoogleBillingHelper.this.onSkuDetailsResponse(result, list, true, listener);
					}
				});
	}

	/**
	 * Launch the purchase flow for a product. If there is no connection, then establish connection before.
	 *
	 * @param activity   The triggering activity.
	 * @param skuDetails The details of the product to be purchased.
	 * @param listener   a listener called after the purchase has been completed.
	 */
	public void launchPurchaseFlow(final Activity activity, final SkuDetails skuDetails, final OnPurchaseSuccessListener listener) {
		if (mIsConnected) {
			doLaunchPurchaseFlow(activity, skuDetails, listener);
		}
		else {
			mBillingClient.startConnection(new BillingClientStateListener() {
				@Override
				public void onBillingSetupFinished(final BillingResult billingResult) {
					if (billingResult.getResponseCode() == BillingResponseCode.OK) {
						Log.d(TAG, "Google Billing Connection established.");
						mIsConnected = true;
						doLaunchPurchaseFlow(activity, skuDetails, listener);
					}
					else {
						Log.i(TAG, "Google Billing Connection failed - " + billingResult.getDebugMessage());
						mIsConnected = false;
					}
				}

				@Override
				public void onBillingServiceDisconnected() {
					Log.d(TAG, "Google Billing Connection lost.");
					mIsConnected = false;
				}
			});
		}
	}

	/**
	 * Launch the purchase flow for a product.
	 *
	 * @param activity   The triggering activity.
	 * @param skuDetails The details of the product to be purchased.
	 * @param listener   a listener called after the purchase has been completed.
	 */
	private void doLaunchPurchaseFlow(final Activity activity, final SkuDetails skuDetails, final OnPurchaseSuccessListener listener) {
		mOnPurchaseSuccessListener = listener;

		Log.d(TAG, "Starting purchase flow for " + skuDetails.getSku());
		mBillingClient.launchBillingFlow(activity, BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build());
	}

	@Override
	public void onPurchasesUpdated(final BillingResult billingResult, final @Nullable List<Purchase> purchaseList) {
		Log.i(TAG, "Purchase finished: " + billingResult.getResponseCode());

		if (billingResult.getResponseCode() != BillingResponseCode.OK) {
			Log.e(TAG, "Error purchasing: " + billingResult.getDebugMessage());
			if (mOnPurchaseSuccessListener != null) {
				mOnPurchaseSuccessListener.handleFailure();
			}
			return;
		}

		Log.i(TAG, "Purchase successful.");

		if (mOnPurchaseSuccessListener != null && purchaseList != null) {
			boolean hasPurchase = false;
			boolean isPurchased = false;
			for (Purchase purchase : purchaseList) {
				hasPurchase = true;
				if (purchase.getPurchaseState() == PurchaseState.PURCHASED) {
					Log.i(TAG, "Purchase " + purchase.getSku() + " finished");
					isPurchased = true;
					doAcknowledgePurchaseIfRequired(purchase);
				}
			}
			if (hasPurchase) {
				mOnPurchaseSuccessListener.handlePurchase(!mIsPremium, !isPurchased);
				if (isPurchased) {
					mIsPremium = true;
				}
			}
		}
	}

	/**
	 * Acknowledge a purchase.
	 *
	 * @param purchase The purchase.
	 */
	private void doAcknowledgePurchaseIfRequired(final Purchase purchase) {
		if (!purchase.isAcknowledged()) {
			Log.d(TAG, "Acknowledging purchase " + purchase.getSku());
			mBillingClient.acknowledgePurchase(
					AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build(),
					new AcknowledgePurchaseResponseListener() {
						@Override
						public void onAcknowledgePurchaseResponse(final BillingResult billingResult) {
							Log.d(TAG, "Acknowledgement result: " + billingResult.getResponseCode());
						}
					});
		}
	}

	/**
	 * Handle result of inventory query.
	 *
	 * @param billingResult  The result flag.
	 * @param skuDetailsList The retrieved SKU details.
	 * @param isSubscription Flag indicating if this was subscription query.
	 * @param listener       The listener called when inventory calls are finished.
	 */
	private void onSkuDetailsResponse(final BillingResult billingResult, final List<SkuDetails> skuDetailsList, final boolean isSubscription,
									  final OnInventoryFinishedListener listener) {
		Log.d(TAG, "Query inventory finished - " + billingResult.getResponseCode() + " - " + isSubscription);

		if (billingResult.getResponseCode() != BillingResponseCode.OK) {
			Log.e(TAG, "Failed to query inventory: " + billingResult.getDebugMessage());
			return;
		}

		Log.d(TAG, "Query inventory was successful.");

		Map<String, Purchase> purchaseMap = new HashMap<>();
		List<SkuPurchase> skuPurchases = new ArrayList<>();

		PurchasesResult purchasesResult = mBillingClient.queryPurchases(isSubscription ? SkuType.SUBS : SkuType.INAPP);
		if (purchasesResult.getResponseCode() != BillingResponseCode.OK) {
			Log.e(TAG, "Failed to query purchases: " + purchasesResult.getBillingResult().getDebugMessage());
			return;
		}
		for (Purchase purchase : purchasesResult.getPurchasesList()) {
			if (purchase.getPackageName().equals(mContext.getPackageName())) {
				purchaseMap.put(purchase.getSku(), purchase);
				if (purchase.getPurchaseState() == PurchaseState.PURCHASED) {
					doAcknowledgePurchaseIfRequired(purchase);
					mIsPremium = true;
				}
			}
		}

		for (SkuDetails skuDetails : skuDetailsList) {
			if (purchaseMap.containsKey(skuDetails.getSku())) {
				skuPurchases.add(new SkuPurchase(skuDetails, purchaseMap.get(skuDetails.getSku())));
			}
			else {
				skuPurchases.add(new SkuPurchase(skuDetails));
			}
		}

		synchronized (this) {
			if (isSubscription) {
				mSubscriptionSkus = skuPurchases;
			}
			else {
				mInAppSkus = skuPurchases;
			}

			if (listener != null && mSubscriptionSkus != null && mInAppSkus != null) {
				listener.handleProducts(mInAppSkus, mSubscriptionSkus);
			}
		}
	}

	/**
	 * Container for a Purchase with Sku Details.
	 */
	public static final class SkuPurchase {
		/**
		 * The purchase.
		 */
		private final Purchase mPurchase;

		/**
		 * Get the purchase.
		 *
		 * @return the purchase.
		 */
		public Purchase getPurchase() {
			return mPurchase;
		}

		/**
		 * The SKU details of the purchase.
		 */
		private final SkuDetails mSkuDetails;

		/**
		 * Get the SKU details of the purchase.
		 *
		 * @return The SKU details of the purchase.
		 */
		public SkuDetails getSkuDetails() {
			return mSkuDetails;
		}

		/**
		 * Constructor, passing the SKU details and the purchase.
		 *
		 * @param skuDetails The SKU details
		 * @param purchase   The purchase.
		 */
		private SkuPurchase(final SkuDetails skuDetails, final Purchase purchase) {
			this.mSkuDetails = skuDetails;
			this.mPurchase = purchase;
		}

		/**
		 * Constructor, passing the SKU details for non-purchase.
		 *
		 * @param skuDetails The SKU details
		 */
		private SkuPurchase(final SkuDetails skuDetails) {
			this.mSkuDetails = skuDetails;
			this.mPurchase = null;
		}

		/**
		 * Get information if the SKU is purchased.
		 *
		 * @return true if purchased.
		 */
		public boolean isPurchased() {
			return mPurchase != null && mPurchase.getPurchaseState() == PurchaseState.PURCHASED;
		}

		/**
		 * Get information if the SKU is pending.
		 *
		 * @return true if pending.
		 */
		public boolean isPending() {
			return mPurchase != null && mPurchase.getPurchaseState() != PurchaseState.PURCHASED;
		}
	}

	/**
	 * Listener called when query for purchases is completed.
	 */
	public interface OnPurchaseQueryCompletedListener {
		/**
		 * Callback called if premium pack status is available.
		 *
		 * @param hasPremiumPack true if premium pack is available.
		 * @param isPending trie if there is a pending purchase.
		 */
		void onHasPremiumPack(boolean hasPremiumPack, boolean isPending);
	}

	/**
	 * Listener to be called after inventory has been retrieved.
	 */
	public interface OnInventoryFinishedListener {
		/**
		 * Handler called after inventory has been retrieved.
		 *
		 * @param inAppSkus        The in-app SKUs.
		 * @param subscriptionSkus The subscription SKUs.
		 */
		void handleProducts(List<SkuPurchase> inAppSkus, List<SkuPurchase> subscriptionSkus);
	}

	/**
	 * Listener to be called after a purchase has been successfully completed.
	 */
	public interface OnPurchaseSuccessListener {
		/**
		 * Handler called after a purchase has been successfully completed.
		 *
		 * @param addedPremiumProduct Flag indicating if there was a premium upgrade.
		 * @param isPending           Flag indicating if the purchase is in pending state.
		 */
		void handlePurchase(boolean addedPremiumProduct, boolean isPending);

		/**
		 * Handler called after the failure of a purchase.
		 */
		void handleFailure();
	}
}
