package de.eisfeldj.augendiagnosefx;

import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_WINDOW_MAXIMIZED;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_WINDOW_SIZE_X;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_WINDOW_SIZE_Y;

import java.io.IOException;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import de.eisfeldj.augendiagnosefx.controller.MainController;
import de.eisfeldj.augendiagnosefx.util.DialogUtil;
import de.eisfeldj.augendiagnosefx.util.DialogUtil.ConfirmDialogListener;
import de.eisfeldj.augendiagnosefx.util.FxmlConstants;
import de.eisfeldj.augendiagnosefx.util.FxmlUtil;
import de.eisfeldj.augendiagnosefx.util.Logger;
import de.eisfeldj.augendiagnosefx.util.PreferenceUtil;
import de.eisfeldj.augendiagnosefx.util.ResourceConstants;
import de.eisfeldj.augendiagnosefx.util.ResourceUtil;
import de.eisfeldj.augendiagnosefx.util.VersioningUtil;
import de.eisfeldj.augendiagnosefx.util.imagefile.JpegSynchronizationUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Application class for starting the application.
 */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS",
		justification = "Intentionally using same name as superclass")
public class Application extends javafx.application.Application {
	/**
	 * The name of the application.
	 */
	public static final String APPLICATION_NAME = "Augendiagnose";

	/**
	 * The primary scene.
	 */
	private static Scene scene;

	/**
	 * The primary stage.
	 */
	private static Stage stage;

	/**
	 * The application host services.
	 */
	private static HostServices hostServices;

	/**
	 * Application method to start the application.
	 *
	 * @param args
	 *            The command line arguments.
	 */
	public static void main(final String[] args) {
		// launch the application.
		Logger.info("Launching application.");
		launch(args);
	}

	@Override
	@SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
			justification = "Intentionally write the stage statically")
	public final void start(final Stage primaryStage) throws IOException, IllegalAccessException {
		Application.stage = primaryStage;
		primaryStage.setTitle(ResourceUtil.getString("app_name"));

		MainController mainController = (MainController) FxmlUtil.getRootFromFxml(FxmlConstants.FXML_MAIN);

		scene =
				new Scene(mainController.getRoot(), PreferenceUtil.getPreferenceDouble(KEY_WINDOW_SIZE_X),
						PreferenceUtil.getPreferenceDouble(KEY_WINDOW_SIZE_Y));

		// Store window size on close.
		primaryStage.setOnCloseRequest(
				new EventHandler<WindowEvent>() {
					@Override
					public void handle(final WindowEvent event) {
						// do not close window.
						event.consume();
						exitAfterConfirmation();
					}
				});

		primaryStage.setScene(scene);
		primaryStage.setMaximized(PreferenceUtil.getPreferenceBoolean(KEY_WINDOW_MAXIMIZED));
		primaryStage.show();

		FxmlUtil.displaySubpage(FxmlConstants.FXML_DISPLAY_PHOTOS, 0, false);

		hostServices = getHostServices();

		VersioningUtil.checkForNewerVersion(false);
	}

	/**
	 * Exit the application after asking for confirmation if there are unsaved data.
	 */
	public static void exitAfterConfirmation() {
		if (JpegSynchronizationUtil.hasRunningSaveRequests()) {
			DialogUtil.displayInfo(ResourceConstants.MESSAGE_INFO_SAVING_PHOTO);
			return;
		}

		if (MainController.hasDirtyBaseController()) {
			ConfirmDialogListener listener = new ConfirmDialogListener() {
				@Override
				public void onDialogPositiveClick() {
					storeWindowDimensions();
					Platform.exit();
				}

				@Override
				public void onDialogNegativeClick() {
					// do nothing.
				}
			};
			DialogUtil.displayConfirmationMessage(listener, ResourceConstants.BUTTON_OK,
					ResourceConstants.MESSAGE_CONFIRM_EXIT_UNSAVED);
		}
		else {
			storeWindowDimensions();
			Platform.exit();
		}
	}

	/**
	 * Store the dimensions of the application window.
	 */
	private static void storeWindowDimensions() {
		PreferenceUtil.setPreference(KEY_WINDOW_MAXIMIZED, stage.isMaximized());
		if (!stage.isMaximized()) {
			PreferenceUtil.setPreference(KEY_WINDOW_SIZE_X, scene.getWidth());
			PreferenceUtil.setPreference(KEY_WINDOW_SIZE_Y, scene.getHeight());
		}
	}

	/**
	 * Redisplay the main page.
	 */
	public static final void refreshMainPage() {
		FxmlUtil.removeAllSubpages();
		FxmlUtil.displaySubpage(FxmlConstants.FXML_DISPLAY_PHOTOS, 0, false);
	}

	/**
	 * Getter for the primary scene.
	 *
	 * @return The primary scene.
	 */
	public static Scene getScene() {
		return scene;
	}

	/**
	 * Getter for the primary stage.
	 *
	 * @return The primary stage.
	 */
	public static Stage getStage() {
		return stage;
	}

	/**
	 * Get the host services.
	 *
	 * @return The host services.
	 */
	public static HostServices getApplicationHostServices() {
		return hostServices;
	}

}
