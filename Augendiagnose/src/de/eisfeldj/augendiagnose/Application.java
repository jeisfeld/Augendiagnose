package de.eisfeldj.augendiagnose;

import android.util.Log;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Utility class to retrieve base application resources.
 */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS",
		justification = "Intentionally using same name as superclass")
public class Application extends de.jeisfeld.augendiagnoselib.Application {
	@Override
	public final void onCreate() {
		super.onCreate();

		try {
			PrivateConstants privateConstants =
					(PrivateConstants) Class.forName("de.eisfeldj.augendiagnose.PrivateConstants")
							.getDeclaredMethod("getInstance", new Class<?>[0])
							.invoke(null, new Object[0]);
			setPrivateConstants(privateConstants);
		}
		catch (Exception e) {
			Log.e(TAG, "Error in getting PrivateConstants", e);
		}
	}

}
