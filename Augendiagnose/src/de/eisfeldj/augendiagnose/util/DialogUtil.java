package de.eisfeldj.augendiagnose.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
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
		Bundle bundle = new Bundle();
		bundle.putString("message", String.format(activity.getString(resource), args));
		fragment.setArguments(bundle);
		fragment.show(activity.getFragmentManager(), DisplayErrorDialogFragment.class.toString());
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
		Bundle bundle = new Bundle();
		bundle.putString("message", String.format(activity.getString(resource), args));
		fragment.setArguments(bundle);
		fragment.show(activity.getFragmentManager(), DisplayErrorDialogAndReturnFragment.class.toString());
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

}
