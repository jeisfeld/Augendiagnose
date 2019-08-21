package com.android.vending.billing;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import de.jeisfeld.augendiagnoselib.R;

/**
 * Represents an in-app product's listing details.
 */
public class SkuDetails {

	// JAVADOC:OFF

	private final String mItemType;
	private String mSku;
	private String mType;
	private String mPrice;
	private String mTitle;
	private String mDescription;
	private final String mJson;

	public SkuDetails(final String jsonSkuDetails) throws JSONException {
		this(IabHelper.ITEM_TYPE_INAPP, jsonSkuDetails);
	}

	public SkuDetails(final String itemType, final String jsonSkuDetails) throws JSONException {
		mItemType = itemType;
		mJson = jsonSkuDetails;
		JSONObject o = new JSONObject(mJson);
		mSku = o.optString("productId");
		mType = o.optString("type");
		mPrice = o.optString("price");
		mTitle = o.optString("title");
		mDescription = o.optString("description");
	}

	public final String getItemType() {
		return mItemType;
	}

	public final String getSku() {
		return mSku;
	}

	public final String getType() {
		return mType;
	}

	public final String getPrice() {
		return mPrice;
	}

	public final String getTitle() {
		return mTitle;
	}

	public final String getDisplayTitle(@NonNull final Context context) {
		if (mSku.startsWith("android.test")) {
			return mSku;
		}
		String applicationNamePostfix = " (" + context.getString(R.string.app_name) + ")";
		if (mTitle.endsWith(applicationNamePostfix)) {
			return mTitle.substring(0, mTitle.length() - applicationNamePostfix.length());
		}
		else {
			return mTitle;
		}
	}

	public final String getDescription() {
		return mDescription;
	}

	@NonNull
	@Override
	public final String toString() {
		return "SkuDetails:" + mJson;
	}
}
