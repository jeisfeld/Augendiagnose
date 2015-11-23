package com.android.vending.billing;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an in-app billing purchase.
 */
public class Purchase {

	// JAVADOC:OFF

	private final String mItemType; // ITEM_TYPE_INAPP or ITEM_TYPE_SUBS
	private String mOrderId;
	private String mPackageName;
	private String mSku;
	private long mPurchaseTime;
	private int mPurchaseState;
	private String mDeveloperPayload;
	private String mToken;
	private final String mOriginalJson;
	private String mSignature;

	public Purchase(final String itemType, final String jsonPurchaseInfo, final String signature) throws JSONException {
		mItemType = itemType;
		mOriginalJson = jsonPurchaseInfo;
		JSONObject o = new JSONObject(mOriginalJson);
		mOrderId = o.optString("orderId");
		mPackageName = o.optString("packageName");
		mSku = o.optString("productId");
		mPurchaseTime = o.optLong("purchaseTime");
		mPurchaseState = o.optInt("purchaseState");
		mDeveloperPayload = o.optString("developerPayload");
		mToken = o.optString("token", o.optString("purchaseToken"));
		mSignature = signature;
	}

	public final String getItemType() {
		return mItemType;
	}

	public final String getOrderId() {
		return mOrderId;
	}

	public final String getPackageName() {
		return mPackageName;
	}

	public final String getSku() {
		return mSku;
	}

	public final long getPurchaseTime() {
		return mPurchaseTime;
	}

	public final int getPurchaseState() {
		return mPurchaseState;
	}

	public final String getDeveloperPayload() {
		return mDeveloperPayload;
	}

	public final String getToken() {
		return mToken;
	}

	public final String getOriginalJson() {
		return mOriginalJson;
	}

	public final String getSignature() {
		return mSignature;
	}

	@NonNull
	@Override
	public final String toString() {
		return "PurchaseInfo(type:" + mItemType + "):" + mOriginalJson;
	}
}
