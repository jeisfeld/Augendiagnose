package de.jeisfeld.augendiagnoselib.components;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * A broadcast receiver being informed about SD card mounts.
 */
public class HeadsetPlugReceiver extends BroadcastReceiver {
	/**
	 * The mHandler reacting on headset plug events.
	 */
	private HeadsetPlugHandler mHandler;

	@Override
	public final void onReceive(final Context context, final Intent intent) {
		String action = intent.getAction();

		if (action.equals(Intent.ACTION_HEADSET_PLUG) && mHandler != null) {
			int headSetState = intent.getIntExtra("state", -1);

			switch (headSetState) {
			case 0:
				mHandler.handleHeadsetPlug(false);
				break;
			case 1:
				mHandler.handleHeadsetPlug(true);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Register this receiver.
	 *
	 * @param context The context
	 * @param handler A handler for headset plug events
	 */
	public void register(final Context context, final HeadsetPlugHandler handler) {
		mHandler = handler;
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
		context.registerReceiver(this, intentFilter);
	}

	/**
	 * Unregister this receiver.
	 *
	 * @param context The context
	 */
	public void unregister(final Context context) {
		context.unregisterReceiver(this);
	}

	/**
	 * Handler to react on headset plugging/unplugging.
	 */
	public interface HeadsetPlugHandler {
		/**
		 * Method to be called if headset is plugged or unplugged.
		 *
		 * @param plugged true if headset is plugged.
		 */
		void handleHeadsetPlug(boolean plugged);
	}

}
