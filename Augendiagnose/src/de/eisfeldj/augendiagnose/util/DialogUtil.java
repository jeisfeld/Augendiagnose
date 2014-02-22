package de.eisfeldj.augendiagnose.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;

/**
 * Helper class to show standard dialogs
 */
public abstract class DialogUtil {
	/**
	 * Display an error and go back to the current activity
	 * 
	 * @param activity
	 *            the current activity
	 * @param resource
	 *            the error message
	 * @param args
	 *            arguments for the error message
	 */
	public static void displayError(final Activity activity, int resource, Object... args) {
		DisplayErrorDialogFragment fragment = new DisplayErrorDialogFragment();
		String message = String.format(activity.getString(resource), args);
		Log.w(Application.TAG, "Dialog message: " + message);
		Bundle bundle = new Bundle();
		bundle.putString("message", message);
		fragment.setArguments(bundle);
		fragment.show(activity.getFragmentManager(), DisplayErrorDialogFragment.class.toString());
	}

	/**
	 * Display an error just as toast
	 * 
	 * @param activity
	 *            the current activity
	 * @param resource
	 *            the error message
	 * @param args
	 *            arguments for the error message
	 */
	public static void displayErrorAsToast(final Context context, int resource, Object... args) {
		String message = String.format(context.getString(resource), args);
		Log.w(Application.TAG, "Toast message: " + message);
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}

	/**
	 * Display an error and stop the activity - return to parent activity.
	 * 
	 * @param activity
	 *            the current activity
	 * @param resource
	 *            the error message
	 * @param args
	 *            arguments for the error message
	 */
	public static void displayErrorAndReturn(final Activity activity, int resource, Object... args) {
		DisplayErrorDialogAndReturnFragment fragment = new DisplayErrorDialogAndReturnFragment();
		String message = String.format(activity.getString(resource), args);
		Log.w(Application.TAG, "Dialog message: " + message);
		Bundle bundle = new Bundle();
		bundle.putString("message", message);
		fragment.setArguments(bundle);
		fragment.show(activity.getFragmentManager(), DisplayErrorDialogAndReturnFragment.class.toString());
	}

	/**
	 * Display an error and stop the activity - return to parent activity.
	 * 
	 * @param activity
	 *            the current activity
	 * @param resource
	 *            the error message
	 * @param args
	 *            arguments for the error message
	 */
	public static void displayReleaseNotes(final Activity activity, String message) {
		DisplayReleaseNotesFragment fragment = new DisplayReleaseNotesFragment();
		Bundle bundle = new Bundle();
		bundle.putString("message", message);
		fragment.setArguments(bundle);
		fragment.show(activity.getFragmentManager(), DisplayReleaseNotesFragment.class.toString());
	}

	/**
	 * Display release notes
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
	public static void displayReleaseNotes(final Activity activity, boolean firstStart, int fromVersion, int toVersion) {
		StringBuffer message = new StringBuffer();
		if(firstStart) {
			message.append(activity.getString(R.string.releasenotes_first_usage));
		}
		message.append(activity.getString(R.string.releasenotes_current_remark));
		message.append("<h3>");
		message.append(activity.getString(R.string.releasenotes_changes));
		message.append("</h3>");
		String[] names = activity.getResources().getStringArray(R.array.releasenotes_version_names);
		String[] notes = activity.getResources().getStringArray(R.array.releasenotes_version_notes);
		for (int i = toVersion; i >= fromVersion; i--) {
			message.append("<h5>");
			message.append(activity.getString(R.string.releasenotes_release));
			message.append(" ");
			message.append(names[i - 1]);
			message.append("</h5><p>");
			message.append(notes[i - 1]);
			message.append("</p>");
		}

		DisplayReleaseNotesFragment fragment = new DisplayReleaseNotesFragment();
		Bundle bundle = new Bundle();
		bundle.putString("message", message.toString());
		fragment.setArguments(bundle);
		fragment.show(activity.getFragmentManager(), DisplayReleaseNotesFragment.class.toString());
	}

	/**
	 * Fragment to display an error and go back to the current activity
	 */
	public static class DisplayErrorDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			String message = getArguments().getString("message");

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.title_dialog_error) //
					.setMessage(message) //
					.setPositiveButton(R.string.button_back, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			return builder.create();
		}
	}

	/**
	 * Fragment to display an error and stop the activity - return to parent activity.
	 */
	public static class DisplayErrorDialogAndReturnFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			String message = getArguments().getString("message");

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.title_dialog_error) //
					.setMessage(message) //
					.setPositiveButton(R.string.button_back, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							getActivity().finish();
						}
					}).setOnKeyListener(new DialogInterface.OnKeyListener() {
						@Override
						public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
							if (keyCode == KeyEvent.KEYCODE_BACK) {
								getActivity().finish();
								return true;
							}
							return false;
						}
					});
			return builder.create();
		}
	}

	/**
	 * Fragment to display an error and stop the activity - return to parent activity.
	 */
	public static class DisplayReleaseNotesFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			String message = getArguments().getString("message");

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.releasenotes_title) //
					.setMessage(Html.fromHtml(message)) //
					.setNegativeButton(R.string.button_show_later, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					}).setPositiveButton(R.string.button_dont_show, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							int version = Application.getVersion();
							Application.setSharedPreferenceString(R.string.key_internal_stored_version,
									Integer.toString(version));
							dialog.dismiss();
						}
					});
			return builder.create();
		}
	}

}
