package de.eisfeldj.augendiagnosefx;

import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_WINDOW_MAXIMIZED;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_WINDOW_SIZE_X;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_WINDOW_SIZE_Y;

import java.io.IOException;

import javafx.application.HostServices;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import de.eisfeldj.augendiagnosefx.controller.MainController;
import de.eisfeldj.augendiagnosefx.util.FxmlUtil;
import de.eisfeldj.augendiagnosefx.util.Logger;
import de.eisfeldj.augendiagnosefx.util.PreferenceUtil;
import de.eisfeldj.augendiagnosefx.util.ResourceUtil;
import de.eisfeldj.augendiagnosefx.util.VersioningUtil;
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

		MainController mainController = (MainController) FxmlUtil.getRootFromFxml("Main.fxml");
		scene =
				new Scene(mainController.getRoot(), PreferenceUtil.getPreferenceDouble(KEY_WINDOW_SIZE_X),
						PreferenceUtil.getPreferenceDouble(KEY_WINDOW_SIZE_Y));

		// Store window size on close.
		primaryStage.setOnCloseRequest(
				new EventHandler<WindowEvent>() {
					@Override
					public void handle(final WindowEvent event) {
						PreferenceUtil.setPreference(KEY_WINDOW_MAXIMIZED, primaryStage.isMaximized());
						if (!primaryStage.isMaximized()) {
							PreferenceUtil.setPreference(KEY_WINDOW_SIZE_X, scene.getWidth());
							PreferenceUtil.setPreference(KEY_WINDOW_SIZE_Y, scene.getHeight());
						}
					}
				});

		primaryStage.setScene(scene);
		primaryStage.setMaximized(PreferenceUtil.getPreferenceBoolean(KEY_WINDOW_MAXIMIZED));
		primaryStage.show();

		FxmlUtil.displaySubpage("DisplayPhotos.fxml");

		hostServices = getHostServices();

		VersioningUtil.checkForNewerVersion(false);
	}

	/**
	 * Redisplay the main page.
	 */
	public static final void refreshMainPage() {
		FxmlUtil.removeAllSubpages();
		FxmlUtil.displaySubpage("DisplayPhotos.fxml");
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
