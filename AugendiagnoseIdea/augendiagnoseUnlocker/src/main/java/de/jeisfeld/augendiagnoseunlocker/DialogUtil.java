package de.jeisfeld.augendiagnoseunlocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Helper class to show standard dialogs.
 */
public final class DialogUtil {
	/**
	 * Parameter to pass the title to the DialogFragment.
	 */
	private static final String PARAM_TITLE = "title";
	/**
	 * Parameter to pass the message to the DialogFragment (of all types).
	 */
	private static final String PARAM_MESSAGE = "message";

	/**
	 * Hide default constructor.
	 */
	private DialogUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Display an error.
	 *
	 * @param activity the current activity
	 * @param resource the error message
	 * @param args     arguments for the error message
	 */
	public static void displayError(@NonNull final Activity activity, final int resource, final Object... args) {
		String message = String.format(activity.getString(resource), args);
		Bundle bundle = new Bundle();
		bundle.putCharSequence(PARAM_MESSAGE, message);
		bundle.putString(PARAM_TITLE, activity.getString(R.string.title_dialog_error));

		DialogFragment fragment = new DisplayMessageDialogFragment();
		fragment.setArguments(bundle);
		fragment.show(activity.getFragmentManager(), fragment.getClass().toString());
	}

	/**
	 * Fragment to display an error and go back to the current activity.
	 */
	public static class DisplayMessageDialogFragment extends DialogFragment {
		@Override
		public final Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
			CharSequence message = getArguments().getCharSequence(PARAM_MESSAGE);
			String title = getArguments().getString(PARAM_TITLE);

			// Listeners cannot retain functionality when automatically recreated.
			// Therefore, dialogs with listeners must be re-created by the activity on orientation change.
			boolean preventRecreation = false;
			if (savedInstanceState != null) {
				preventRecreation = savedInstanceState.getBoolean("preventRecreation");
			}
			if (preventRecreation) {
				dismiss();
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(title)
					.setMessage(message)
					.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(@NonNull final DialogInterface dialog, final int id) {
							dialog.dismiss();
						}
					});
			return builder.create();
		}
	}

}
