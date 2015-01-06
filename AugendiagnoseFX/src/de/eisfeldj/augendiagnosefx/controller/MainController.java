package de.eisfeldj.augendiagnosefx.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import de.eisfeldj.augendiagnosefx.util.FXMLUtil;
import de.eisfeldj.augendiagnosefx.util.Logger;
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

	/**
	 * A list storing the handlers for closing windows.
	 */
	private List<EventHandler<ActionEvent>> closeHandlerList = new ArrayList<EventHandler<ActionEvent>>();

	@Override
	public final Parent getRoot() {
		return mainPane;
	}

	@Override
	public final void initialize(final URL location, final ResourceBundle resources) {
		closeButton.setGraphic(new ImageView(ResourceUtil.getImage("close.png")));
	}

	/**
	 * Get the main controller instance.
	 *
	 * @return The main controller instance.
	 */
	public static MainController getInstance() {
		try {
			return getController(MainController.class);
		}
		catch (TooManyControllersException | MissingControllerException e) {
			Logger.error("Could not find main controller", e);
			return null;
		}
	}

	/**
	 * Set the contents of the menu bar.
	 *
	 * @param menuBarContents
	 *            The contents of the menu bar.
	 */
	public final void setMenuBarContents(final MenuBar menuBarContents) {
		menuBar.getMenus().clear();
		menuBar.getMenus().addAll(menuBarContents.getMenus());
	}

	/**
	 * Add a subpage.
	 *
	 * @param controller
	 *            The controller of the subpage.
	 */
	public final void addSubPage(final Controller controller) {
		body.getChildren().add(controller.getRoot());
		subPageRegistry.add(controller);

		// Enable the close menu.
		enableClose(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				FXMLUtil.removeSubpage(controller);
			}
		});
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
		disableClose();
	}

	/**
	 * Remove all subpages.
	 */
	public final void removeAllSubPages() {
		for (Controller controller : subPageRegistry) {
			controller.close();
		}
		subPageRegistry.clear();
		disableAllClose();
	}

	/**
	 * Enable the close menu item.
	 *
	 * @param eventHandler
	 *            The event handler to be called when closing.
	 */
	private void enableClose(final EventHandler<ActionEvent> eventHandler) {
		closeHandlerList.add(eventHandler);

		if (closeHandlerList.size() > 1) {
			MenuController.getInstance().setMenuClose(true, eventHandler);

			closeButton.setVisible(true);
			closeButton.setOnAction(eventHandler);
		}
	}

	/**
	 * Disable one level of the close menu item.
	 */
	private void disableClose() {
		closeHandlerList.remove(closeHandlerList.size() - 1);
		if (closeHandlerList.size() > 1) {
			EventHandler<ActionEvent> newEventHandler = closeHandlerList.get(closeHandlerList.size() - 1);
			MenuController.getInstance().setMenuClose(true, newEventHandler);
			closeButton.setOnAction(newEventHandler);
		}
		else {
			MenuController.getInstance().setMenuClose(false, null);
			closeButton.setVisible(false);
		}
	}

	/**
	 * Disable all levels of the close menu icon.
	 */
	private void disableAllClose() {
		closeHandlerList.clear();
		MenuController.getInstance().setMenuClose(false, null);
		closeButton.setVisible(false);
	}

}