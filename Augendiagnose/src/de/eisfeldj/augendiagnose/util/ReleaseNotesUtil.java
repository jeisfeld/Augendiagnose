package de.eisfeldj.augendiagnose.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;

/**
 * Helper class to show standard dialogs.
 */
public final class ReleaseNotesUtil {
	/**
	 * Parameter to pass the message to the DialogFragment (of all types).
	 */
	private static final String PARAM_MESSAGE = "message";

	/**
	 * Hide default constructor.
	 */
	private ReleaseNotesUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Create the release notes.
	 *
	 * @param context
	 *            the application context.
	 * @param firstStart
	 *            indicates that the app is started for the first time
	 * @param fromVersion
	 *            first version from which to show release notes
	 * @param toVersion
	 *            last version to which to show release notes
	 * @return the release notes as HTML string.
	 */
	public static String getReleaseNotesHtml(final Context context, final boolean firstStart, final int fromVersion,
			final int toVersion) {
		StringBuffer message = new StringBuffer();
		if (firstStart) {
			message.append(context.getString(R.string.releasenotes_first_usage));
		}
		message.append(context.getString(R.string.releasenotes_current_remark));
		message.append("<h3>");
		message.append(context.getString(R.string.releasenotes_changes));
		message.append("</h3>");
		String[] names = context.getResources().getStringArray(R.array.releasenotes_version_names);
		String[] notes = context.getResources().getStringArray(R.array.releasenotes_version_notes);
		for (int i = toVersion; i >= fromVersion; i--) {
			message.append("<h5>");
			message.append(context.getString(R.string.releasenotes_release));
			message.append(" ");
			message.append(names[i - 1]);
			message.append("</h5><p>");
			message.append(notes[i - 1]);
			message.append("</p>");
		}
		return message.toString();
	}

	/**
	 * Display release notes.
	 *
	 * @param activity
	 *            the triggering activity
	 * @param firstStart
	 *            indicates that the app is started for the first time
	 * @param fromVersion
	 *            first version from which to show release notes
	 * @param toVersion
	 *            last version to which to show release notes
	 */
	public static void displayReleaseNotes(final Activity activity, final boolean firstStart, final int fromVersion,
			final int toVersion) {
		String message = getReleaseNotesHtml(activity, firstStart, fromVersion, toVersion);
		DisplayReleaseNotesFragment fragment = new DisplayReleaseNotesFragment();
		Bundle bundle = new Bundle();
		bundle.putString(PARAM_MESSAGE, message);
		fragment.setArguments(bundle);
		fragment.show(activity.getFragmentManager(), DisplayReleaseNotesFragment.class.toString());
	}

	/**
	 * Fragment to display an error and stop the activity - return to parent activity.
	 */
	public static class DisplayReleaseNotesFragment extends DialogFragment {
		@Override
		public final Dialog onCreateDialog(final Bundle savedInstanceState) {
			String message = getArguments().getString(PARAM_MESSAGE);

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.releasenotes_title) //
					.setIcon(R.drawable.ic_title_info) //
					.setMessage(Html.fromHtml(message)) //
					.setNegativeButton(R.string.button_show_later, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int id) {
							dialog.dismiss();
						}
					}).setPositiveButton(R.string.button_dont_show, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int id) {
							int version = Application.getVersion();
							PreferenceUtil.setSharedPreferenceString(R.string.key_internal_stored_version,
									Integer.toString(version));
							dialog.dismiss();
						}
					});

			return builder.create();
		}

		@Override
		public final void onStart() {
			super.onStart();
			// Make links clickable
			((TextView) getDialog().findViewById(android.R.id.message))
					.setMovementMethod(LinkMovementMethod.getInstance());
		}
	}

}
