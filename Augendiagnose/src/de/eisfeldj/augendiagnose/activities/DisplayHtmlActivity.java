package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.fragments.DisplayHtmlFragment;

public class DisplayHtmlActivity extends Activity {

	private static final String STRING_EXTRA_RESOURCE = "de.eisfeldj.augendiagnose.RESOURCE";

	/**
	 * Static helper method to start the activity, passing the resource holding the HTML as string
	 * 
	 * @param context
	 * @param resource
	 */
	public static void startActivity(Context context, int resource) {
		Intent intent = new Intent(context, DisplayHtmlActivity.class);
		intent.putExtra(STRING_EXTRA_RESOURCE, resource);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		int resource = getIntent().getIntExtra(STRING_EXTRA_RESOURCE, -1);

		setContentView(R.layout.activity_fragments_single);

		DisplayHtmlFragment fragment = new DisplayHtmlFragment();
		fragment.setParameters(resource);

		getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
		getFragmentManager().executePendingTransactions();
	}

}
