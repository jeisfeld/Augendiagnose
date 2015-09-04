package de.jeisfeld.augendiagnoselib.activities;

import java.util.Arrays;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;

/**
 * Base activity being the subclass of most application activities. Handles the help menu.
 */
public abstract class BaseActivity extends AdMarvelActivity {

	// OVERRIDABLE
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Application.setLanguage();
		DialogUtil.checkOutOfMemoryError(this);

		String[] activitiesWithHomeEnablement = getResources().getStringArray(R.array.activities_with_home_enablement);
		getActionBar().setDisplayHomeAsUpEnabled(Arrays.asList(activitiesWithHomeEnablement).contains(getClass().getName()));
	}

	/*
	 * Inflate options menu.
	 */
	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		String[] activitiesWithSettings = getResources().getStringArray(R.array.activities_with_settings);
		boolean allowsSettings = Arrays.asList(activitiesWithSettings).contains(getClass().getName());

		getMenuInflater().inflate(allowsSettings ? R.menu.menu_settings_help : R.menu.menu_only_help, menu);

		if (getHelpResource() == 0 || getString(getHelpResource()).length() == 0) {
			// Hide help icon if there is no help text
			menu.findItem(R.id.action_help).setVisible(false);
		}

		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * Handle menu actions.
	 */
	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_help) {
			DisplayHtmlActivity.startActivity(this, getHelpResource());
			return true;
		}
		else if (itemId == R.id.action_settings) {
			SettingsActivity.startActivity(this);
			return true;
		}
		else {
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
