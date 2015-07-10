package de.eisfeldj.augendiagnose.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.admarvel.android.ads.AdMarvelUtils;
import com.admarvel.android.ads.AdMarvelUtils.ErrorReason;
import com.admarvel.android.ads.AdMarvelUtils.SDKAdNetwork;
import com.admarvel.android.ads.AdMarvelView;
import com.admarvel.android.ads.AdMarvelView.AdMarvelViewListener;

import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.activities.AdMarvelActivity;

/**
 * Utility class for handling AdMarvel ads.
 */
public final class AdMarvelUtil {
	/**
	 * The AdMarvel partnerId.
	 */
	public static final String PARTNER_ID = "50a68fd597efa11b";

	/**
	 * SiteId for BNR (320x50).
	 */
	public static final String SITE_BNR = "130360";
	/**
	 * SiteId for MREC (300x250).
	 */
	public static final String SITE_MREC = "130361";
	/**
	 * SiteId for LDR (728x90).
	 */
	public static final String SITE_LDR = "130362";

	/**
	 * The countries where ads are displayed.
	 */
	public static final String[] AD_COUNTRIES = { "DE", "US" };

	/**
	 * Hide default constructor.
	 */
	private AdMarvelUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Check if the app is eligible for displaying an ad.
	 *
	 * @return true if eligible.
	 */
	public static boolean isEligibleForAd() {
		int checksSinceLastClick = PreferenceUtil.incrementCounter(R.string.key_admarvel_checkssincelastclick);
		if (Application.isAuthorized()) {
			return false;
		}
		String country = SystemUtil.getUserCountry();
		if (country == null || !Arrays.asList(AD_COUNTRIES).contains(country)) {
			return false;
		}

		return checksSinceLastClick > PreferenceUtil.getSharedPreferenceInt(R.string.key_admarvel_countclicks, 0);
	}

	/**
	 * Initialize the AdMarvel SDK. Currently not used.
	 *
	 * @param activity
	 *            The triggering activity.
	 */
	public static void initializeSdk(final Activity activity) {
		Map<SDKAdNetwork, String> publisherIds = new HashMap<SDKAdNetwork, String>();
		// publisherIds.put(SDKAdNetwork.ADMARVEL, "<App IDs for that AdNetwork>");
		AdMarvelUtils.initialize(activity, publisherIds);
	}

	/**
	 * Request a banner ad.
	 *
	 * @param activity
	 *            The triggering activity.
	 */
	public static void requestBannerAd(final AdMarvelActivity activity) {
		try {
			Map<String, Object> targetParams = new HashMap<String, Object>();
			targetParams.put("KEYWORDS", "games");
			targetParams.put("APP_VERSION", "1.0.0"); // version of your app
			AdMarvelView adMarvelView = activity.getAdMarvelView();
			adMarvelView.setListener(new AdMarvelListener(activity));
			adMarvelView.requestNewAd(targetParams, PARTNER_ID, SystemUtil.isTablet() ? SITE_LDR : SITE_BNR);
		}
		catch (Exception e) {
			Log.e(Application.TAG, "Error when requesting banner ad.", e);
		}
	}

	/**
	 * Request a banner ad after checking eligibility.
	 *
	 * @param activity
	 *            The triggering activity.
	 */
	public static void requestBannerAdIfEligible(final AdMarvelActivity activity) {
		if (AdMarvelUtil.isEligibleForAd()) {
			AdMarvelUtil.requestBannerAd(activity);
		}
		else {
			AdMarvelView adMarvelView = activity.getAdMarvelView();
			if (adMarvelView != null) {
				adMarvelView.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * Listener for tracking AdMarvel ad actions.
	 */
	private static class AdMarvelListener implements AdMarvelViewListener {
		/**
		 * Constructor handing over the activity.
		 *
		 * @param activity
		 *            The triggering activity.
		 */
		public AdMarvelListener(final Activity activity) {
		}

		@Override
		public void onClickAd(final AdMarvelView arg0, final String arg1) {
			PreferenceUtil.incrementCounter(R.string.key_admarvel_countclicks);
			PreferenceUtil.setSharedPreferenceInt(R.string.key_admarvel_checkssincelastclick, 0);
		}

		@Override
		public void onClose() {
			// do nothing.
		}

		@Override
		public void onExpand() {
			// do nothing.
		}

		@Override
		public void onFailedToReceiveAd(final AdMarvelView arg0, final int arg1, final ErrorReason arg2) {
			// do nothing.
		}

		@Override
		public void onReceiveAd(final AdMarvelView arg0) {
			PreferenceUtil.incrementCounter(R.string.key_admarvel_countdisplays);
		}

		@Override
		public void onRequestAd(final AdMarvelView arg0) {
			// do nothing.
		}

	}
}
