package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.view.View;
import android.view.ViewManager;

import com.admarvel.android.ads.AdMarvelView;

import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.util.AdMarvelUtil;
import de.eisfeldj.augendiagnose.util.PreferenceUtil;

/**
 * An activity showing AdMarvel ads.
 */
public abstract class AdMarvelActivity extends Activity {
	/**
	 * The view holding the ad.
	 */
	private AdMarvelView adMarvelView;

	/**
	 * Display a banner ad.
	 */
	public final void requestBannerAdIfEligible() {
		getAdMarvelView();
		AdMarvelUtil.requestBannerAdIfEligible(this);
	}

	/**
	 * Delete the ad view.
	 */
	protected final void deleteAdMarvelView() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					((ViewManager) adMarvelView.getParent()).removeView(adMarvelView);
				}
				catch (Exception e) {
					// do nothing.
				}
			}
		});
	}

	// OVERRIDABLE
	@Override
	protected void onStart() {
		super.onStart();
		if (adMarvelView != null && adMarvelView.getVisibility() != View.GONE) {
			if (PreferenceUtil.getSharedPreferenceBoolean(R.string.key_admarvel_iscurrentlyclicked)
					|| PreferenceUtil.getSharedPreferenceBoolean(R.string.key_remove_ads)) {
				// do not trigger again after it was once clicked.
				adMarvelView.setVisibility(View.GONE);
				adMarvelView.destroy();
				adMarvelView = null;
			}
			else {
				adMarvelView.start(this);
			}
		}
	}

	// OVERRIDABLE
	@Override
	protected void onResume() {
		super.onResume();
		if (adMarvelView != null && adMarvelView.getVisibility() != View.GONE) {
			adMarvelView.resume(this);
		}
	}

	// OVERRIDABLE
	@Override
	protected void onPause() {
		super.onPause();
		if (adMarvelView != null && adMarvelView.getVisibility() != View.GONE) {
			adMarvelView.pause(this);
		}
	}

	// OVERRIDABLE
	@Override
	public void onStop() {
		super.onStop();
		if (adMarvelView != null && adMarvelView.getVisibility() != View.GONE) {
			adMarvelView.stop(this);
		}
	}

	// OVERRIDABLE
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (adMarvelView != null && adMarvelView.getVisibility() != View.GONE) {
			adMarvelView.destroy();
		}
	}

	/**
	 * Get the AdMarvelView.
	 *
	 * @return The AdMarvelView.
	 */
	public final AdMarvelView getAdMarvelView() {
		if (adMarvelView == null) {
			adMarvelView = (AdMarvelView) findViewById(R.id.admarvel);
		}
		return adMarvelView;
	}

}
