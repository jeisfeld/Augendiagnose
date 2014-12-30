package de.eisfeldj.augendiagnosefx.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import de.eisfeldj.augendiagnosefx.util.ResourceUtil;

/**
 * The controller of the main window.
 */
public class MainController implements Initializable, Controller {
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