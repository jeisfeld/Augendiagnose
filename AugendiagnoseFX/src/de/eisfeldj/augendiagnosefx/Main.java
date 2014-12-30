package de.eisfeldj.augendiagnosefx;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import de.eisfeldj.augendiagnosefx.util.FXMLUtil;
import de.eisfeldj.augendiagnosefx.util.ResourceUtil;

/**
 * Main class for starting the application.
 */
public class Main extends Application {

	/**
	 * The primary scene.
	 */
	private static Scene scene;

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

		MainController mainController = (MainController) FXMLUtil.getRootFromFxml("Main.fxml");
		scene = new Scene(mainController.getRoot());
		primaryStage.setScene(scene);
		primaryStage.show();

		FXMLUtil.displayMenu("Menu.fxml");
		mainController.getBody().getChildren().add(FXMLUtil.getRootFromFxml("StartPage.fxml").getRoot());
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
	 * The controller of the main window.
	 */
	public static class MainController implements Initializable, Controller {
		/**
		 * The root of the main page.
		 */
		@FXML
		private VBox mainPane;

		@Override
		public final Parent getRoot() {
			return mainPane;
		}

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
		 * The close button in the menu bar.
		 */
		@FXML
		private Button closeButton;

		@Override
		public final void initialize(final URL location, final ResourceBundle resources) {
			closeButton.setGraphic(new ImageView(ResourceUtil.getImage("close.png")));
		}

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

		/**
		 * Getter for the close button.
		 *
		 * @return the close button.
		 */
		public final Button getCloseButton() {
			return closeButton;
		}
	}

	/**
	 * The controller of the start body.
	 */
	public static class StartController implements Controller {
		/**
		 * The root pane of the start page.
		 */
		@FXML
		private GridPane start;

		@Override
		public final Parent getRoot() {
			return start;
		}

		/**
		 * Handler for button "Organize new photos".
		 *
		 * @param event
		 *            The action event.
		 * @throws IOException
		 */
		@FXML
		protected final void handleButtonOrganize(final ActionEvent event) {
			FXMLUtil.displaySubpage("OrganizePhotos.fxml");
		}

		/**
		 * Handler for button "Display Photos".
		 *
		 * @param event
		 *            The action event.
		 * @throws IOException
		 */
		@FXML
		protected final void handleButtonDisplay(final ActionEvent event) {
			FXMLUtil.displaySubpage("DisplayPhotos.fxml");
		}

	}
}
