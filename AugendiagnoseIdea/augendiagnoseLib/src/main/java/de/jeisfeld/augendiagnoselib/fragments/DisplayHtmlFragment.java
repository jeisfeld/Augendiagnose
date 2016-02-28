package de.jeisfeld.augendiagnoselib.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.util.ReleaseNotesUtil;

/**
 * A fragment to display an HTML page (used for help screens).
 */
public class DisplayHtmlFragment extends Fragment {
	/**
	 * The style tag to be inserted into the HTML.
	 */
	private static final String STYLE =
			"<style type=\"text/css\">"
					+ "body{color: #ffffff;} "
					+ "img {width: 24px; height: 24px; vertical-align: middle;} "
					+ "img.frameless {width: 18px; height: 18px;} "
					+ "a {color: #7fffff;} "
					+ "li {margin-top: 6px; }"
					+ "table p, li p {padding-top: 0px; padding-bottom: 0px; margin-top: 0px; margin-bottom: 6px; }"
					+ "table p:last-child, table ul:last-child {margin-bottom: 2px; }"
					+ "</style>";

	/**
	 * The resource key for the resource to be displayed (for storage in the bundle).
	 */
	private static final String STRING_RESOURCE = "de.jeisfeld.augendiagnoselib.RESOURCE";

	/**
	 * The resource id of the HTML String to be displayed.
	 */
	private int mResource;

	/**
	 * Initialize the listFoldersFragment with the resource.
	 *
	 * @param initialResource The resource id of the HTML to be displayed.
	 */
	public final void setParameters(final int initialResource) {
		Bundle args = new Bundle();
		args.putInt(STRING_RESOURCE, initialResource);

		setArguments(args);
	}

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mResource = getArguments().getInt(STRING_RESOURCE, -1);
	}

	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
								   final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_display_html, container, false);
	}

	@Override
	public final void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (getView() == null) {
			return;
		}

		WebView webView = (WebView) getView().findViewById(R.id.webViewDisplayHtml);
		webView.setBackgroundColor(0x00000000);

		setOpenLinksInExternalBrowser(webView, null);

		String html = getString(mResource);
		if (mResource == R.string.html_release_notes_base) {
			int indexBody = html.indexOf("</body>");
			String releaseNotes =
					ReleaseNotesUtil.getReleaseNotesHtml(getActivity(), false, 1, Application.getVersion());
			html = html.substring(0, indexBody) + releaseNotes + html.substring(indexBody);
		}

		// add style
		int index = html.indexOf("</head>");
		html = html.substring(0, index) + STYLE + html.substring(index);

		webView.loadDataWithBaseURL("file:///android_res/drawable/", html, "text/html", "utf-8", "");
	}

	/**
	 * Enable a WebView to open links in the external browser.
	 *
	 * @param webView  The webView.
	 * @param callback A callback that can be used to trigger application code from links.
	 */
	public static void setOpenLinksInExternalBrowser(@NonNull final WebView webView, @Nullable final WebViewLinkCallback callback) {
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(@NonNull final WebView view, @Nullable final String url) {
				if (url == null) {
					return false;
				}
				else if (url.startsWith("http://") || url.startsWith("https://")) {
					view.getContext().startActivity(
							new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
					return true;
				}
				else if (url.startsWith("appcode://") && callback != null) {
					String action = url.substring("appcode://".length());
					callback.handleLinkAction(action);
					return true;
				}
				else {
					return false;
				}
			}
		});
	}

	/**
	 * Callback that allows to trigger code from links in WebView.
	 */
	public interface WebViewLinkCallback {
		/**
		 * Callback handler for a WebView link action.
		 *
		 * @param linkAction The action in the link.
		 */
		void handleLinkAction(String linkAction);
	}

}
