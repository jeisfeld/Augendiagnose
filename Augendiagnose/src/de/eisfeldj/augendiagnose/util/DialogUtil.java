package de.eisfeldj.augendiagnose.util;

import java.io.Serializable;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.util.DialogUtil.ConfirmDialogFragment.ConfirmDialogListener;
import de.eisfeldj.augendiagnose.util.DialogUtil.DisplayMessageDialogFragment.MessageDialogListener;

/**
 * Helper class to show standard dialogs.
 */
public abstract class DialogUtil {
	/**
	 * Parameter to pass the title to the DialogFragment.
	 */
	private static final String PARAM_TITLE = "title";
	/**
	 * Parameter to pass the message to the DialogFragment (of all types).
	 */
	private static final String PARAM_MESSAGE = "message";
	/**
	 * Parameter to pass the text resource for the confirmation button to the ConfirmDialogFragment.
	 */
	private static final String PARAM_BUTTON_RESOURCE = "buttonResource";
	/**
	 * Parameter to pass the callback listener to the ConfirmDialogFragment.
	 */
	private static final String PARAM_LISTENER = "listener";

	/**
	 * Display an information message and go back to the current activity.
	 *
	 * @param activity
	 *            the current activity
	 * @param listener
	 *            an optional listener waiting for the dialog response.
	 * @param resource
	 *            the error message
	 * @param args
	 *            arguments for the error message
	 */
	public static void displayInfo(final Activity activity, final MessageDialogListener listener, final int resource,
			final Object... args) {
		DialogFragment fragment = new DisplayMessageDialogFragment();
		String message = String.format(activity.getString(resource), args);
		Bundle bundle = new Bundle();
		bundle.putString(PARAM_MESSAGE, message);
		bundle.putString(PARAM_TITLE, activity.getString(R.string.title_dialog_info));
		if (listener != null) {
			bundle.putSerializable(PARAM_LISTENER, listener);
		}
		fragment.setArguments(bundle);
		fragment.show(activity.getFragmentManager(), fragment.getClass().toString());
	}

	/**
	 * Display an error and either go back to the current activity or finish the current activity.
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
			fragment = new DisplayMessageDialogFragment();
		}
		String message = String.format(activity.getString(resource), args);
		Log.w(Application.TAG, "Dialog message: " + message);
		Bundle bundle = new Bundle();
		bundle.putString(PARAM_MESSAGE, message);
		bundle.putString(PARAM_TITLE, activity.getString(R.string.title_dialog_error));
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
	 * @param buttonResource
	 *            the display on the positive button
	 * @param messageResource
	 *            the confirmation message
	 * @param args
	 *            arguments for the confirmation message
	 */
	public static void displayConfirmationMessage(final Activity activity,
			final ConfirmDialogListener listener, final int buttonResource,
			final int messageResource, final Object... args) {
		ConfirmDialogFragment fragment = new ConfirmDialogFragment();
		String message = String.format(activity.getString(messageResource), args);
		Bundle bundle = new Bundle();
		bundle.putString(PARAM_MESSAGE, message);
		bundle.putInt(PARAM_BUTTON_RESOURCE, buttonResource);
		bundle.putSerializable(PARAM_LISTENER, listener);
		fragment.setArguments(bundle);
		fragment.show(activity.getFragmentManager(), fragment.getClass().toString());
	}

	/**
	 * Fragment to display an error and go back to the current activity.
	 */
	public static class DisplayMessageDialogFragment extends DialogFragment {
		/**
		 * The activity that creates an instance of this dialog listFoldersFragment must implement this interface in
		 * order to receive event callbacks. Each method passes the DialogFragment in case the host needs to query it.
		 */
		public interface MessageDialogListener extends Serializable {
			/**
			 * Callback method for ok click from the dialog.
			 *
			 * @param dialog
			 *            the confirmation dialog fragment.
			 */
			void onDialogClick(final DialogFragment dialog);
		}

		@Override
		public final Dialog onCreateDialog(final Bundle savedInstanceState) {
			String message = getArguments().getString(PARAM_MESSAGE);
			String title = getArguments().getString(PARAM_TITLE);
			final MessageDialogListener listener = (MessageDialogListener) getArguments().getSerializable(
					PARAM_LISTENER);

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(title) //
					.setMessage(message) //
					.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int id) {
							if (listener != null) {
								listener.onDialogClick(DisplayMessageDialogFragment.this);
							}
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
			String message = getArguments().getString(PARAM_MESSAGE);

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
	 * Fragment to display a confirmation message.
	 */
	public static class ConfirmDialogFragment extends DialogFragment {
		/**
		 * The activity that creates an instance of this dialog listFoldersFragment must implement this interface in
		 * order to receive event callbacks. Each method passes the DialogFragment in case the host needs to query it.
		 */
		public interface ConfirmDialogListener extends Serializable {
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
			String message = getArguments().getString(PARAM_MESSAGE);
			int confirmButtonResource = getArguments().getInt(PARAM_BUTTON_RESOURCE);
			final ConfirmDialogListener listener = (ConfirmDialogListener) getArguments().getSerializable(
					PARAM_LISTENER);

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.title_dialog_confirmation) //
					.setMessage(message) //
					.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int id) {
							// Send the positive button event back to the host activity
							listener.onDialogNegativeClick(ConfirmDialogFragment.this);
						}
					}) //
					.setPositiveButton(confirmButtonResource, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int id) {
							// Send the negative button event back to the host activity
							listener.onDialogPositiveClick(ConfirmDialogFragment.this);
						}
					});
			return builder.create();
		}
	}

}
