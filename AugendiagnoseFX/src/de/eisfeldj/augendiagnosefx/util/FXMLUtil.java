package de.eisfeldj.augendiagnosefx.util;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import de.eisfeldj.augendiagnosefx.Main.MainController;

/**
 * Utility class for reading FXML files.
 */
public final class FXMLUtil {

	/**
	 * The controller of the main application.
	 */
	private static MainController controller = null;

	/**
	 * Private constructor to prevent instantiation.
	 */
	private FXMLUtil() {
		// do nothing
	}

	/**
	 * Utility method to expand an FXML file, including internationalization.
	 *
	 * @param fxmlFile
	 *            The name of the FXML file.
	 * @return The root.
	 * @throws IOException
	 */
	public static Parent getRootFromFxml(final String fxmlFile) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setResources(ResourceUtil.STRINGS_BUNDLE);
		Parent root = fxmlLoader.load(ClassLoader.getSystemResource("fxml/" + fxmlFile).openStream());
		root.getStylesheets().add(ClassLoader.getSystemResource("css/application.css").toExternalForm());

		if (fxmlLoader.getController() instanceof MainController) {
			FXMLUtil.controller = (MainController) fxmlLoader.getController();
		}

		return root;
	}

	/**
	 * Utility method to expand and display an FXML file in the body.
	 *
	 * @param fxmlFile
	 *            The name of the FXML file.
	 * @throws IOException
	 */
	public static void displayBody(final String fxmlFile) throws IOException {
		Parent root = getRootFromFxml(fxmlFile);
		controller.getBody().getChildren().add(root);
	}

	/**
	 * Utility method to expand and display an FXML file in the body as subpage.
	 *
	 * @param fxmlFile
	 *            The name of the FXML file.
	 * @throws IOException
	 */
	public static void displaySubpage(final String fxmlFile) throws IOException {
		final Parent root = getRootFromFxml(fxmlFile);
		controller.getBody().getChildren().add(root);

		Image btnImage = ResourceUtil.getImage("close.png");
		Button closeButton = new Button("", new ImageView(btnImage));
		closeButton.getStyleClass().add("imageButton");
		closeButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				remove(root);
				remove(closeButton);
			}
		});

		controller.getMenuButtons().getChildren().addAll(closeButton);
	}

	/**
	 * Utility method to remove a node. In particular, a full pane can be removed.
	 *
	 * @param node
	 *            The node to be removed.
	 */
	public static void remove(final Node node) {
		((Pane) node.getParent()).getChildren().remove(node);
	}

	/**
	 * Utility method to expand and display the menu bar.
	 *
	 * @param fxmlFile
	 *            The name of the FXML file.
	 * @throws IOException
	 */
	public static void displayMenu(final String fxmlFile) throws IOException {
		MenuBar root = (MenuBar) getRootFromFxml(fxmlFile);
		controller.getMenuBar().getMenus().clear();
		controller.getMenuBar().getMenus().addAll(root.getMenus());
	}

}
