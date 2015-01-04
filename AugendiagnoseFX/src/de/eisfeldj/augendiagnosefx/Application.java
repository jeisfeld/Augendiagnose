package de.eisfeldj.augendiagnosefx;

import java.io.IOException;

import javafx.scene.Scene;
import javafx.stage.Stage;
import de.eisfeldj.augendiagnosefx.controller.MainController;
import de.eisfeldj.augendiagnosefx.util.FXMLUtil;
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

		MainController mainController = (MainController) FXMLUtil.getRootFromFxml("Main.fxml");
		scene = new Scene(mainController.getRoot());
		primaryStage.setScene(scene);
		primaryStage.show();

		FXMLUtil.displayMenu("Menu.fxml");
		mainController.getBody().getChildren().add(FXMLUtil.getRootFromFxml("DisplayPhotos.fxml").getRoot());
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
