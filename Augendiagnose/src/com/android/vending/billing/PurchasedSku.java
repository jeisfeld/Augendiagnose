package com.android.vending.billing;

/**
 * Container for a Purchase with Sku Details.
 */
public class PurchasedSku {

	private Purchase purchase;

	public Purchase getPurchase() {
		return purchase;
	}

	private SkuDetails skuDetails;

	public SkuDetails getSkuDetails() {
		return skuDetails;
	}

	public PurchasedSku(SkuDetails skuDetails, Purchase purchase) {
		this.skuDetails = skuDetails;
		this.purchase = purchase;
	}
}
