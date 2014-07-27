package de.eisfeldj.augendiagnose.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import de.eisfeldj.augendiagnose.R;

public class DisplayHtmlFragment extends Fragment {

	private static final String STYLE = "<style type=\"text/css\">body{color: #fff}</style>";
	private static final String STRING_RESOURCE = "de.eisfeldj.augendiagnose.RESOURCE";

	private int resource;

	/**
	 * Initialize the fragment with the resource
	 *
	 * @param text
	 * @return
	 */
	public void setParameters(int resource) {
		Bundle args = new Bundle();
		args.putInt(STRING_RESOURCE, resource);

		setArguments(args);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		resource = getArguments().getInt(STRING_RESOURCE, -1);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_display_html, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		WebView webView = (WebView) getView().findViewById(R.id.webViewDisplayHtml);
		webView.setBackgroundColor(0x00000000);
		String html = getString(resource);
		int index = html.indexOf("</head>");
		html = html.substring(0, index) + STYLE + html.substring(index);
		webView.loadData(html, "text/html; charset=UTF-8", "utf-8");
	}

}
