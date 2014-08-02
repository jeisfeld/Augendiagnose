package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.fragments.DisplayHtmlFragment;

/**
 * Activity to display an HTML page. Used for display of help pages.
 */
public class DisplayHtmlActivity extends Activity {

	/**
	 * The resource key for the resource to be displayed.
	 */
	private static final String STRING_EXTRA_RESOURCE = "de.eisfeldj.augendiagnose.RESOURCE";

	/**
	 * The fragment tag.
	 */
	private static final String FRAGMENT_TAG = "FRAGMENT_TAG";

	/**
	 * Static helper method to start the activity, passing the resource holding the HTML as string.
	 *
	 * @param context
	 *            The context in which the activity is started.
	 * @param resource
	 *            The resource to be displayed.
	 */
	public static void startActivity(final Context context, final int resource) {
		Intent intent = new Intent(context, DisplayHtmlActivity.class);
		intent.putExtra(STRING_EXTRA_RESOURCE, resource);
		context.startActivity(intent);
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		int resource = getIntent().getIntExtra(STRING_EXTRA_RESOURCE, -1);

		setContentView(R.layout.activity_fragments_single);

		if (getFragmentManager().findFragmentByTag(FRAGMENT_TAG) == null) {
			DisplayHtmlFragment fragment = new DisplayHtmlFragment();
			fragment.setParameters(resource);

			getFragmentManager().beginTransaction().add(R.id.fragment_container, fragment, FRAGMENT_TAG).commit();
			getFragmentManager().executePendingTransactions();
		}
	}

}
