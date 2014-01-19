package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import de.eisfeldj.augendiagnose.R;

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
		setContentView(R.layout.activity_display_html);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		int resource = getIntent().getIntExtra(STRING_EXTRA_RESOURCE, -1);

		WebView webView = (WebView) findViewById(R.id.webViewDisplayHtml);

		webView.loadData(getString(resource), "text/html", "UTF-8");
	}

}
