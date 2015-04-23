package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.util.DialogUtil;

/**
 * Base activity being the subclass of most application activities. Handles the help menu.
 */
public abstract class BaseActivity extends Activity {

	// OVERRIDABLE
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DialogUtil.checkOutOfMemoryError(this);
	}

	/*
	 * Inflate options menu.
	 */
	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_only_help, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * Handle menu actions.
	 */
	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_help:
			DisplayHtmlActivity.startActivity(this, getHelpResource());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Factory method to retrieve the resource id of the help page to be shown.
	 *
	 * @return the resource id of the help page.
	 */
	protected abstract int getHelpResource();

}
