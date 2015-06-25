package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewManager;

import com.admarvel.android.ads.AdMarvelView;

import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.util.AdMarvelUtil;

/**
 * An activity showing AdMarvel ads.
 */
public abstract class AdMarvelActivity extends Activity {
	/**
	 * The view holding the ad.
	 */
	private AdMarvelView adMarvelView;

	// OVERRIDABLE
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResId());
		adMarvelView = (AdMarvelView) findViewById(R.id.admarvel);
	}

	/**
	 * Display a banner ad.
	 */
	protected final void requestBannerAdIfEligible() {
		AdMarvelUtil.requestBannerAdIfEligible(this);
	}

	/**
	 * The resource id of the layout.
	 *
	 * @return The resource id of the layout.
	 */
	protected abstract int getLayoutResId();

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

	@Override
	protected final void onStart() {
		super.onStart();
		if (adMarvelView != null) {
			adMarvelView.start(this);
		}
	}

	@Override
	protected final void onResume() {
		super.onResume();
		if (adMarvelView != null) {
			adMarvelView.resume(this);
		}
	}

	@Override
	protected final void onPause() {
		super.onPause();
		if (adMarvelView != null) {
			adMarvelView.pause(this);
		}
	}

	@Override
	public final void onStop() {
		super.onStop();
		if (adMarvelView != null) {
			adMarvelView.stop(this);
		}
	}

	@Override
	protected final void onDestroy() {
		super.onDestroy();
		if (adMarvelView != null) {
			adMarvelView.destroy();
		}
	}

}
