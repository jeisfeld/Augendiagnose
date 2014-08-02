package de.eisfeldj.augendiagnose.util;

import java.io.Serializable;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.util.DialogUtil.ConfirmDeleteDialogFragment.ConfirmDeleteDialogListener;

/**
 * Helper class to show standard dialogs.
 */
public abstract class DialogUtil {
	/**
	 * Display an error and go back to the current activity.
	 *
	 * @param activity
	 *            the current activity
	 * @param resource
	 *            the error message
	 * @param finishActivity
	 *            should activity be finished after display?
	 * @param args
	 *            arguments for the error message
	 */
	public static void displayError(final Activity activity, final int resource, final boolean finishActivity,
			final Object... args) {
		DialogFragment fragment;
		if (finishActivity) {
			fragment = new DisplayErrorDialogAndReturnFragment();
		}
		else {
			fragment = new DisplayErrorDialogFragment();
		}
		String message = String.format(activity.getString(resource), args);
		Log.w(Application.TAG, "Dialog message: " + message);
		Bundle bundle = new Bundle();
		bundle.putString("message", message);
		fragment.setArguments(bundle);
		fragment.show(activity.getFragmentManager(), fragment.getClass().toString());
	}

	/**
	 * Display an error just as toast.
	 *
	 * @param context
	 *            the current activity or context
	 * @param resource
	 *            the error message
	 * @param args
	 *            arguments for the error message
	 */
	public static void displayErrorAsToast(final Context context, final int resource, final Object... args) {
		String message = String.format(context.getString(resource), args);
		Log.w(Application.TAG, "Toast message: " + message);
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}

	/**
	 * Display a confirmation message asking for cancel or ok.
	 *
	 * @param activity
	 *            the current activity
	 * @param listener
	 *            The listener waiting for the response
	 * @param resource
	 *            the confirmation message
	 * @param args
	 *            arguments for the confirmation message
	 */
	public static void displayDeleteConfirmationMessage(final Activity activity,
			final ConfirmDeleteDialogListener listener,
			final int resource, final Object... args) {
		ConfirmDeleteDialogFragment fragment = new ConfirmDeleteDialogFragment();
		String message = String.format(activity.getString(resource), args);
		Bundle bundle = new Bundle();
		bundle.putString("message", message);
		bundle.putSerializable("listener", listener);
		fragment.setArguments(bundle);
		fragment.show(activity.getFragmentManager(), DisplayErrorDialogFragment.class.toString());
	}

	/**
	 * Display an error and stop the activity - return to parent activity.
	 *
	 * @param activity
	 *            the current activity
	 * @param message
	 *            the error message
	 */
	public static void displayReleaseNotes(final Activity activity, final String message) {
		DisplayReleaseNotesFragment fragment = new DisplayReleaseNotesFragment();
		Bundle bundle = new Bundle();
		bundle.putString("message", message);
		fragment.setArguments(bundle);
		fragment.show(activity.getFragmentManager(), DisplayReleaseNotesFragment.class.toString());
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
		StringBuffer message = new StringBuffer();
		if (firstStart) {
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
	 * Fragment to display an error and go back to the current activity.
	 */
	public static class DisplayErrorDialogFragment extends DialogFragment {
		@Override
		public final Dialog onCreateDialog(final Bundle savedInstanceState) {
			String message = getArguments().getString("message");

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.title_dialog_error) //
					.setMessage(message) //
					.setPositiveButton(R.string.button_back, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int id) {
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
		public final Dialog onCreateDialog(final Bundle savedInstanceState) {
			String message = getArguments().getString("message");

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.title_dialog_error) //
					.setMessage(message) //
					.setPositiveButton(R.string.button_back, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int id) {
							getActivity().finish();
						}
					});

			return builder.create();
		}

		@Override
		public final void onDismiss(final DialogInterface dialog) {
			super.onDismiss(dialog);
			getActivity().finish();
		}
	}

	/**
	 * Fragment to display an error and stop the activity - return to parent activity.
	 */
	public static class DisplayReleaseNotesFragment extends DialogFragment {
		@Override
		public final Dialog onCreateDialog(final Bundle savedInstanceState) {
			String message = getArguments().getString("message");

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.releasenotes_title) //
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
							Application.setSharedPreferenceString(R.string.key_internal_stored_version,
									Integer.toString(version));
							dialog.dismiss();
						}
					});
			return builder.create();
		}
	}

	/**
	 * Fragment to display a confirmation message.
	 */
	public static class ConfirmDeleteDialogFragment extends DialogFragment {
		/**
		 * The activity that creates an instance of this dialog listFoldersFragment must implement this interface in
		 * order to receive event callbacks. Each method passes the DialogFragment in case the host needs to query it.
		 */
		public interface ConfirmDeleteDialogListener extends Serializable {
			/**
			 * Callback method for positive click from the confirmation dialog.
			 *
			 * @param dialog
			 *            the confirmation dialog fragment.
			 */
			void onDialogPositiveClick(final DialogFragment dialog);

			/**
			 * Callback method for negative click from the confirmation dialog.
			 *
			 * @param dialog
			 *            the confirmation dialog fragment.
			 */
			void onDialogNegativeClick(final DialogFragment dialog);
		}

		@Override
		public final Dialog onCreateDialog(final Bundle savedInstanceState) {
			String message = getArguments().getString("message");
			final ConfirmDeleteDialogListener listener = (ConfirmDeleteDialogListener) getArguments().getSerializable(
					"listener");

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.title_dialog_confirmation) //
					.setMessage(message) //
					.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int id) {
							// Send the positive button event back to the host activity
							listener.onDialogNegativeClick(ConfirmDeleteDialogFragment.this);
						}
					}) //
					.setPositiveButton(R.string.button_delete, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int id) {
							// Send the negative button event back to the host activity
							listener.onDialogPositiveClick(ConfirmDeleteDialogFragment.this);
						}
					});
			return builder.create();
		}
	}

}
