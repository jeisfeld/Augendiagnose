package de.jeisfeld.augendiagnoselib.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponseCode;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsParams.Product;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;

import static com.android.billingclient.api.BillingClient.ProductType.INAPP;
import static com.android.billingclient.api.BillingClient.ProductType.SUBS;
import static com.android.billingclient.api.Purchase.PurchaseState.PENDING;
import static com.android.billingclient.api.Purchase.PurchaseState.PURCHASED;

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
	 * The in-app products to be offered.
	 */
	private static final List<Product> INAPP_PRODUCTS = new ArrayList<>();
	/**
	 * The subscription products to be offered.
	 */
	private static final List<Product> SUBS_PRODUCTS = new ArrayList<>();

	/**
	 * An instance of GoogleBillingHelper.
	 */
	@Nullable
	private static GoogleBillingHelper mInstance;

	static {
		for (String productId : INAPP_PRODUCT_IDS) {
			INAPP_PRODUCTS.add(Product.newBuilder().setProductType(INAPP).setProductId(productId).build());
		}
		for (String productId : SUBSCRIPTION_IDS) {
			SUBS_PRODUCTS.add(Product.newBuilder().setProductType(SUBS).setProductId(productId).build());
		}
	}

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
	 * Found in app Products.
	 */
	private List<ProductPurchase> mInAppProducts = null;
	/**
	 * Found subscription Products.
	 */
	private List<ProductPurchase> mSubscriptionProducts = null;

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
			doHasPremiumPack(listener);
		}
		else {
			mBillingClient.startConnection(new BillingClientStateListener() {
				@Override
				public void onBillingSetupFinished(@NonNull final BillingResult billingResult) {
					if (billingResult.getResponseCode() == BillingResponseCode.OK) {
						Log.d(TAG, "Google Billing Connection established.");
						mIsConnected = true;
						doHasPremiumPack(listener);
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
	 * @param listener The listener called when the information is available.
	 */
	private void doHasPremiumPack(final OnPurchaseQueryCompletedListener listener) {
		mBillingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(INAPP).build(),
				(billingResultInApp, listInApp) ->
						mBillingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(INAPP).build(),
								(billingResultSubs, listSubs) -> {
									int result = -1;
									for (Purchase purchase : listInApp) {
										result = PENDING;
										if (purchase.getPurchaseState() == PURCHASED) {
											for (String product : purchase.getProducts()) {
												if (INAPP_PRODUCT_IDS.contains(product)) {
													doAcknowledgePurchaseIfRequired(purchase);
													result = PURCHASED;
												}
											}
										}
									}
									if (result != PURCHASED) {
										for (Purchase purchase : listSubs) {
											result = PENDING;
											if (purchase.getPurchaseState() == PURCHASED) {
												for (String product : purchase.getProducts()) {
													if (INAPP_PRODUCT_IDS.contains(product)) {
														doAcknowledgePurchaseIfRequired(purchase);
														result = PURCHASED;
													}
												}
											}
										}
									}

									listener.onHasPremiumPack(result == PURCHASED, result == PENDING);
								}));

	}

	/**
	 * Query Product details. If no connection available, establish connection.
	 *
	 * @param listener The listener called when the query is finished.
	 */
	public void queryProductDetails(final OnInventoryFinishedListener listener) {
		if (mIsConnected) {
			doQueryProductDetails(listener);
		}
		else {
			mBillingClient.startConnection(new BillingClientStateListener() {
				@Override
				public void onBillingSetupFinished(@NonNull final BillingResult billingResult) {
					if (billingResult.getResponseCode() == BillingResponseCode.OK) {
						Log.d(TAG, "Google Billing Connection established.");
						mIsConnected = true;
						doQueryProductDetails(listener);
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
	 * Query Product details.
	 *
	 * @param listener The listener called when the query is finished.
	 */
	private void doQueryProductDetails(final OnInventoryFinishedListener listener) {
		synchronized (this) {
			mInAppProducts = null;
			mSubscriptionProducts = null;
		}
		mIsPremium = false;
		mBillingClient.queryProductDetailsAsync(
				QueryProductDetailsParams.newBuilder().setProductList(INAPP_PRODUCTS).build(),
				(result, list) -> GoogleBillingHelper.this.onProductDetailsResponse(result, list, false, listener));
		mBillingClient.queryProductDetailsAsync(
				QueryProductDetailsParams.newBuilder().setProductList(SUBS_PRODUCTS).build(),
				(result, list) -> GoogleBillingHelper.this.onProductDetailsResponse(result, list, true, listener));
	}

	/**
	 * Launch the purchase flow for a product. If there is no connection, then establish connection before.
	 *
	 * @param activity       The triggering activity.
	 * @param productDetails The details of the product to be purchased.
	 * @param listener       a listener called after the purchase has been completed.
	 */
	public void launchPurchaseFlow(final Activity activity, final ProductDetails productDetails, final OnPurchaseSuccessListener listener) {
		if (mIsConnected) {
			doLaunchPurchaseFlow(activity, productDetails, listener);
		}
		else {
			mBillingClient.startConnection(new BillingClientStateListener() {
				@Override
				public void onBillingSetupFinished(@NonNull final BillingResult billingResult) {
					if (billingResult.getResponseCode() == BillingResponseCode.OK) {
						Log.d(TAG, "Google Billing Connection established.");
						mIsConnected = true;
						doLaunchPurchaseFlow(activity, productDetails, listener);
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
	 * @param activity       The triggering activity.
	 * @param productDetails The details of the product to be purchased.
	 * @param listener       a listener called after the purchase has been completed.
	 */
	private void doLaunchPurchaseFlow(final Activity activity, final ProductDetails productDetails, final OnPurchaseSuccessListener listener) {
		mOnPurchaseSuccessListener = listener;

		List<ProductDetailsParams> params = new ArrayList<>();
		if (SUBS.equals(productDetails.getProductType())) {
			String offerToken = productDetails.getSubscriptionOfferDetails().get(0).getOfferToken();
			params.add(ProductDetailsParams.newBuilder().setProductDetails(productDetails).setOfferToken(offerToken).build());
		}
		else {
			params.add(ProductDetailsParams.newBuilder().setProductDetails(productDetails).build());
		}

		Log.d(TAG, "Starting purchase flow for " + productDetails.getName());
		mBillingClient.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(params).build());
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
				if (purchase.getPurchaseState() == PURCHASED) {
					Log.i(TAG, "Purchase " + purchase.getProducts() + " finished");
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
			Log.d(TAG, "Acknowledging purchase " + purchase.getProducts());
			mBillingClient.acknowledgePurchase(
					AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build(),
					billingResult -> Log.d(TAG, "Acknowledgement result: " + billingResult.getResponseCode()));
		}
	}

	/**
	 * Handle result of inventory query.
	 *
	 * @param billingResult      The result flag.
	 * @param productDetailsList The retrieved Product details.
	 * @param isSubscription     Flag indicating if this was subscription query.
	 * @param listener           The listener called when inventory calls are finished.
	 */
	private void onProductDetailsResponse(final BillingResult billingResult, final List<ProductDetails> productDetailsList, final boolean isSubscription,
										  final OnInventoryFinishedListener listener) {
		Log.d(TAG, "Query inventory finished - " + billingResult.getResponseCode() + " - " + isSubscription);

		if (billingResult.getResponseCode() != BillingResponseCode.OK) {
			Log.e(TAG, "Failed to query inventory: " + billingResult.getDebugMessage());
			return;
		}

		Log.d(TAG, "Query inventory was successful.");

		Map<String, Purchase> purchaseMap = new HashMap<>();
		List<ProductPurchase> productPurchases = new ArrayList<>();

		mBillingClient.queryPurchasesAsync(
				QueryPurchasesParams.newBuilder().setProductType(isSubscription ? SUBS : INAPP).build(), new PurchasesResponseListener() {
					@Override
					public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
						if (billingResult.getResponseCode() != BillingResponseCode.OK) {
							Log.e(TAG, "Failed to query purchases: " + billingResult.getDebugMessage());
							return;
						}
						for (Purchase purchase : list) {
							if (purchase.getPackageName().equals(mContext.getPackageName())) {
								for (String product : purchase.getProducts()) {
									purchaseMap.put(product, purchase);
								}
								if (purchase.getPurchaseState() == PURCHASED && list.size() > 0) {
									doAcknowledgePurchaseIfRequired(purchase);
									mIsPremium = true;
								}
							}
						}
						for (ProductDetails productDetails : productDetailsList) {
							if (purchaseMap.containsKey(productDetails.getProductId())) {
								productPurchases.add(new ProductPurchase(productDetails, purchaseMap.get(productDetails.getProductId())));
							}
							else {
								productPurchases.add(new ProductPurchase(productDetails));
							}
						}

						synchronized (this) {
							if (isSubscription) {
								mSubscriptionProducts = productPurchases;
							}
							else {
								mInAppProducts = productPurchases;
							}

							if (listener != null && mSubscriptionProducts != null && mInAppProducts != null) {
								listener.handleProducts(mInAppProducts, mSubscriptionProducts);
							}
						}
					}
				});
	}

	/**
	 * Container for a Purchase with Product Details.
	 */
	public static final class ProductPurchase {
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
		 * The Product details of the purchase.
		 */
		private final ProductDetails mProductDetails;

		/**
		 * Get the Product details of the purchase.
		 *
		 * @return The Product details of the purchase.
		 */
		public ProductDetails getProductDetails() {
			return mProductDetails;
		}

		/**
		 * Constructor, passing the Product details and the purchase.
		 *
		 * @param productDetails The Product details
		 * @param purchase   The purchase.
		 */
		private ProductPurchase(final ProductDetails productDetails, final Purchase purchase) {
			this.mProductDetails = productDetails;
			this.mPurchase = purchase;
		}

		/**
		 * Constructor, passing the Product details for non-purchase.
		 *
		 * @param productDetails The Product details
		 */
		private ProductPurchase(final ProductDetails productDetails) {
			this.mProductDetails = productDetails;
			this.mPurchase = null;
		}

		/**
		 * Get information if the Product is purchased.
		 *
		 * @return true if purchased.
		 */
		public boolean isPurchased() {
			return mPurchase != null && mPurchase.getPurchaseState() == PURCHASED;
		}

		/**
		 * Get information if the Product is pending.
		 *
		 * @return true if pending.
		 */
		public boolean isPending() {
			return mPurchase != null && mPurchase.getPurchaseState() != PURCHASED;
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
		 * @param isPending      trie if there is a pending purchase.
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
		 * @param inAppProducts        The in-app Products.
		 * @param subscriptionProducts The subscription Products.
		 */
		void handleProducts(List<ProductPurchase> inAppProducts, List<ProductPurchase> subscriptionProducts);
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
