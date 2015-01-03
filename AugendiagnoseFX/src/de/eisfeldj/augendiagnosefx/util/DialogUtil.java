package de.eisfeldj.augendiagnosefx.util;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import de.eisfeldj.augendiagnosefx.Application;
import de.eisfeldj.augendiagnosefx.controller.DialogController;

/**
 * Helper class to show standard dialogs.
 */
public abstract class DialogUtil {
	/**
	 * Display an error and go back to the current activity.
	 *
	 * @param resource
	 *            the error message resource
	 * @param args
	 *            arguments for the error message
	 */
	public static void displayError(final String resource, final Object... args) {
		String message = String.format(ResourceUtil.getString(resource), args);
		Logger.warning("Dialog message: " + message);

		DialogController controller;
		try {
			controller = (DialogController) FXMLUtil.getRootFromFxml("Dialog.fxml");
		}
		catch (IOException e) {
			Logger.error("Failed to load FXML file for dialog");
			return;
		}

		controller.setHeading(ResourceUtil.getString("title_dialog_error"));
		controller.setMessage(message);

		Scene scene = new Scene(controller.getRoot());
		Stage dialog = new Stage();
		// dialog.initStyle(StageStyle.UTILITY);
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(Application.getStage());
		dialog.setScene(scene);

		controller.getBtnBack().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				dialog.close();
			}
		});

		dialog.show();
	}

	// /**
	// * Display an error just as toast.
	// *
	// * @param context
	// * the current activity or context
	// * @param resource
	// * the error message
	// * @param args
	// * arguments for the error message
	// */
	// public static void displayErrorAsToast(final Context context, final int resource, final Object... args) {
	// String message = String.format(context.getString(resource), args);
	// Logger.warning("Toast message: " + message);
	// Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	// }
	//
	// /**
	// * Display a confirmation message asking for cancel or ok.
	// *
	// * @param activity
	// * the current activity
	// * @param listener
	// * The listener waiting for the response
	// * @param buttonResource
	// * the display on the positive button
	// * @param messageResource
	// * the confirmation message
	// * @param args
	// * arguments for the confirmation message
	// */
	// public static void displayConfirmationMessage(final Activity activity,
	// final ConfirmDialogListener listener, final int buttonResource,
	// final int messageResource, final Object... args) {
	// ConfirmDialogFragment fragment = new ConfirmDialogFragment();
	// String message = String.format(activity.getString(messageResource), args);
	// Bundle bundle = new Bundle();
	// bundle.putString(PARAM_MESSAGE, message);
	// bundle.putInt(PARAM_BUTTON_RESOURCE, buttonResource);
	// bundle.putSerializable(PARAM_LISTENER, listener);
	// fragment.setArguments(bundle);
	// fragment.show(activity.getFragmentManager(), DisplayErrorDialogFragment.class.toString());
	// }
	//
	// /**
	// * Display an error and stop the activity - return to parent activity.
	// *
	// * @param activity
	// * the current activity
	// * @param message
	// * the error message
	// */
	// public static void displayReleaseNotes(final Activity activity, final String message) {
	// DisplayReleaseNotesFragment fragment = new DisplayReleaseNotesFragment();
	// Bundle bundle = new Bundle();
	// bundle.putString(PARAM_MESSAGE, message);
	// fragment.setArguments(bundle);
	// fragment.show(activity.getFragmentManager(), DisplayReleaseNotesFragment.class.toString());
	// }
	//
	// /**
	// * Display release notes.
	// *
	// * @param activity
	// * the triggering activity
	// * @param firstStart
	// * indicates that the app is started for the first time
	// * @param fromVersion
	// * first version from which to show release notes
	// * @param toVersion
	// * last version to which to show release notes
	// */
	// public static void displayReleaseNotes(final Activity activity, final boolean firstStart, final int fromVersion,
	// final int toVersion) {
	// StringBuffer message = new StringBuffer();
	// if (firstStart) {
	// message.append(activity.getString(R.string.releasenotes_first_usage));
	// }
	// message.append(activity.getString(R.string.releasenotes_current_remark));
	// message.append("<h3>");
	// message.append(activity.getString(R.string.releasenotes_changes));
	// message.append("</h3>");
	// String[] names = activity.getResources().getStringArray(R.array.releasenotes_version_names);
	// String[] notes = activity.getResources().getStringArray(R.array.releasenotes_version_notes);
	// for (int i = toVersion; i >= fromVersion; i--) {
	// message.append("<h5>");
	// message.append(activity.getString(R.string.releasenotes_release));
	// message.append(" ");
	// message.append(names[i - 1]);
	// message.append("</h5><p>");
	// message.append(notes[i - 1]);
	// message.append("</p>");
	// }
	//
	// DisplayReleaseNotesFragment fragment = new DisplayReleaseNotesFragment();
	// Bundle bundle = new Bundle();
	// bundle.putString(PARAM_MESSAGE, message.toString());
	// fragment.setArguments(bundle);
	// fragment.show(activity.getFragmentManager(), DisplayReleaseNotesFragment.class.toString());
	// }

}
