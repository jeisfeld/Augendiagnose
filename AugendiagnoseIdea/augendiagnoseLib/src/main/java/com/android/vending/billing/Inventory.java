package com.android.vending.billing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a block of information about in-app items. An Inventory is returned by such methods as
 * {@link IabHelper#queryInventory}.
 */
public class Inventory {

	// JAVADOC:OFF

	private Map<String, SkuDetails> mSkuMap = new HashMap<String, SkuDetails>();
	private Map<String, Purchase> mPurchaseMap = new HashMap<String, Purchase>();

	Inventory() {
	}

	// JAVADOC:ON

	/**
	 * Returns the listing details for an in-app product.
	 *
	 * @param sku
	 *            the product
	 * @return The listing details for that product.
	 */
	public final SkuDetails getSkuDetails(final String sku) {
		return mSkuMap.get(sku);
	}

	/**
	 * Returns purchase information for a given product, or null if there is no purchase.
	 *
	 * @param sku
	 *            The product.
	 * @return The purchase information for that product.
	 */
	public final Purchase getPurchase(final String sku) {
		return mPurchaseMap.get(sku);
	}

	/**
	 * Returns whether or not there exists a purchase of the given product.
	 *
	 * @param sku
	 *            The product.
	 * @return true if there exists a purchase of this product.
	 */
	public final boolean hasPurchase(final String sku) {
		return mPurchaseMap.containsKey(sku);
	}

	/**
	 * Return whether or not details about the given product are available.
	 *
	 * @param sku
	 *            The product.
	 * @return True if details are available.
	 */
	public final boolean hasDetails(final String sku) {
		return mSkuMap.containsKey(sku);
	}

	/**
	 * Erase a purchase (locally) from the inventory, given its product ID. This just modifies the Inventory object
	 * locally and has no effect on the server! This is useful when you have an existing Inventory object which you know
	 * to be up to date, and you have just consumed an item successfully, which means that erasing its purchase data
	 * from the Inventory you already have is quicker than querying for a new Inventory.
	 *
	 * @param sku
	 *            The purchase to be erased.
	 */
	public final void erasePurchase(final String sku) {
		if (mPurchaseMap.containsKey(sku)) {
			mPurchaseMap.remove(sku);
		}
	}

	/**
	 * Returns a list of all owned product IDs.
	 *
	 * @return A list of all owned product IDs.
	 */
	protected final List<String> getAllOwnedSkus() {
		return new ArrayList<String>(mPurchaseMap.keySet());
	}

	/**
	 * Returns a list of all owned product IDs of a given type.
	 *
	 * @param itemType
	 *            The item type
	 * @return The list of all owned product IDs of this type.
	 */
	protected final List<String> getAllOwnedSkus(final String itemType) {
		List<String> result = new ArrayList<String>();
		for (Purchase p : mPurchaseMap.values()) {
			if (p.getItemType().equals(itemType)) {
				result.add(p.getSku());
			}
		}
		return result;
	}

	/**
	 * Returns a list of all purchases.
	 *
	 * @return The list of all purchases.
	 */
	protected final List<Purchase> getAllPurchases() {
		return new ArrayList<Purchase>(mPurchaseMap.values());
	}

	protected final void addSkuDetails(final SkuDetails d) {
		mSkuMap.put(d.getSku(), d);
	}

	protected final void addPurchase(final Purchase p) {
		mPurchaseMap.put(p.getSku(), p);
	}
}
