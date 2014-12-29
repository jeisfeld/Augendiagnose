package de.eisfeldj.augendiagnosefx;

import java.io.IOException;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import de.eisfeldj.augendiagnosefx.util.FXMLUtil;
import de.eisfeldj.augendiagnosefx.util.Logger;
import de.eisfeldj.augendiagnosefx.util.ResourceUtil;

/**
 * Main class for starting the application.
 */
public class Main extends Application {
	/**
	 * Main method to start the application.
	 *
	 * @param args
	 *            The command line arguments.
	 */
	public static void main(final String[] args) {
		launch(args);
	}

	@Override
	public final void start(final Stage primaryStage) throws IOException, IllegalAccessException {
		primaryStage.setTitle(ResourceUtil.getString("app_name"));

		Parent root = FXMLUtil.getRootFromFxml("Main.fxml");
		primaryStage.setScene(new Scene(root));
		primaryStage.show();

		FXMLUtil.displayMenu("Menu.fxml");
		FXMLUtil.displayBody("StartPage.fxml");
	}

	/**
	 * The controller of the main window.
	 */
	public static class MainController {
		/**
		 * The pane containing the body.
		 */
		@FXML
		private StackPane body;

		/**
		 * The pane containing the menu bar.
		 */
		@FXML
		private MenuBar menuBar;

		/**
		 * The pane containing menu buttons.
		 */
		@FXML
		private HBox menuButtons;

		/**
		 * Getter for the body pane.
		 *
		 * @return The body pane.
		 */
		public final StackPane getBody() {
			return body;
		}

		/**
		 * Getter for the menu pane.
		 *
		 * @return The menu pane.
		 */
		public final MenuBar getMenuBar() {
			return menuBar;
		}

		/**
		 * Getter for the menu buttons.
		 *
		 * @return The menu buttons.
		 */
		public final HBox getMenuButtons() {
			return menuButtons;
		}
	}

	/**
	 * The controller of the start body.
	 */
	public static class StartController {
		/**
		 * Handler for button "Organize new photos".
		 *
		 * @param event
		 *            The action event.
		 * @throws IOException
		 */
		@FXML
		protected final void handleButtonOrganize(final ActionEvent event) throws IOException {
			FXMLUtil.displaySubpage("DisplayPhotos.fxml");
		}

		/**
		 * Handler for button "Display Photos".
		 *
		 * @param event
		 *            The action event.
		 */
		@FXML
		protected final void handleButtonDisplay(final ActionEvent event) {
			// TODO
			Logger.info("Pressed Display Button.");
		}
	}
}
