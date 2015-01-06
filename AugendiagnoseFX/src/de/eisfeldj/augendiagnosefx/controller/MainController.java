package de.eisfeldj.augendiagnosefx.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
public class MainController extends Controller implements Initializable {
	/**
	 * The root of the main page.
	 */
	@FXML
	private VBox mainPane;

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

	/**
	 * The list of subpages.
	 */
	private List<Controller> subPageRegistry = new ArrayList<Controller>();

	@Override
	public final Parent getRoot() {
		return mainPane;
	}

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

	/**
	 * Add a subpage.
	 *
	 * @param controller
	 *            The controller of the subpage.
	 */
	public final void addSubPage(final Controller controller) {
		getBody().getChildren().add(controller.getRoot());
		subPageRegistry.add(controller);
	}

	/**
	 * Remove a subpage.
	 *
	 * @param controller
	 *            The controller of the subpage.
	 */
	public final void removeSubPage(final Controller controller) {
		controller.close();
		subPageRegistry.remove(controller);
	}

	/**
	 * Remove all subpages.
	 */
	public final void removeAllSubPages() {
		for (Controller controller : subPageRegistry) {
			controller.close();
		}
		subPageRegistry.clear();
	}
}