package com.android.vending.billing;

/**
 * Container for a Purchase with Sku Details.
 */
public class PurchasedSku {

	/**
	 * The purchase.
	 */
	private final Purchase mPurchase;

	public final Purchase getPurchase() {
		return mPurchase;
	}

	/**
	 * The SKU details of the purchase.
	 */
	private final SkuDetails mSkuDetails;

	public final SkuDetails getSkuDetails() {
		return mSkuDetails;
	}

	/**
	 * Constructor, passing the SKU details and the purchase.
	 *
	 * @param skuDetails The SKU details
	 * @param purchase   The purchase.
	 */
	public PurchasedSku(final SkuDetails skuDetails, final Purchase purchase) {
		this.mSkuDetails = skuDetails;
		this.mPurchase = purchase;
	}
}
