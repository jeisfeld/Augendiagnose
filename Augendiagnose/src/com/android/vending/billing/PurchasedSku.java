package com.android.vending.billing;

/**
 * Container for a Purchase with Sku Details.
 */
public class PurchasedSku {

	/**
	 * The purchase.
	 */
	private Purchase purchase;

	public final Purchase getPurchase() {
		return purchase;
	}

	/**
	 * The SKU details of the purchase.
	 */
	private SkuDetails skuDetails;

	public final SkuDetails getSkuDetails() {
		return skuDetails;
	}

	/**
	 * Constructor, passing the SKU details and the purchase.
	 *
	 * @param skuDetails
	 *            The SKU details
	 * @param purchase
	 *            The purchase.
	 */
	public PurchasedSku(final SkuDetails skuDetails, final Purchase purchase) {
		this.skuDetails = skuDetails;
		this.purchase = purchase;
	}
}
