package de.eisfeldj.augendiagnosefx;

import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_WINDOW_MAXIMIZED;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_WINDOW_SIZE_X;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_WINDOW_SIZE_Y;

import java.io.IOException;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import de.eisfeldj.augendiagnosefx.controller.MainController;
import de.eisfeldj.augendiagnosefx.util.FXMLUtil;
import de.eisfeldj.augendiagnosefx.util.Logger;
import de.eisfeldj.augendiagnosefx.util.PreferenceUtil;
import de.eisfeldj.augendiagnosefx.util.ResourceUtil;

/**
 * Application class for starting the application.
 */
public class Application extends javafx.application.Application {

	/**
	 * The primary scene.
	 */
	private static Scene scene;

	/**
	 * The primary stage.
	 */
	private static Stage stage;

	/**
	 * The main controller.
	 */
	private static MainController mainController;

	/**
	 * Application method to start the application.
	 *
	 * @param args
	 *            The command line arguments.
	 */
	public static void main(final String[] args) {
		launch(args);
	}

	@Override
	public final void start(final Stage primaryStage) throws IOException, IllegalAccessException {
		stage = primaryStage;
		primaryStage.setTitle(ResourceUtil.getString("app_name"));

		mainController = (MainController) FXMLUtil.getRootFromFxml("Main.fxml");
		scene =
				new Scene(mainController.getRoot(), PreferenceUtil.getPreferenceDouble(KEY_WINDOW_SIZE_X),
						PreferenceUtil.getPreferenceDouble(KEY_WINDOW_SIZE_Y));

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

		FXMLUtil.displayMenu("Menu.fxml");
		mainController.getBody().getChildren().add(FXMLUtil.getRootFromFxml("DisplayPhotos.fxml").getRoot());
	}

	/**
	 * Redisplay the main page.
	 */
	public static final void refreshMainPage() {
		FXMLUtil.removeAllSubpages();
		try {
			mainController.getBody().getChildren().add(FXMLUtil.getRootFromFxml("DisplayPhotos.fxml").getRoot());
		}
		catch (IOException e) {
			Logger.error("Failed to load FXML file for display", e);
		}
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

}
