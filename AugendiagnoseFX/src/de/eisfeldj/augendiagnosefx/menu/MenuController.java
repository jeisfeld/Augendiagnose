package de.eisfeldj.augendiagnosefx.menu;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import de.eisfeldj.augendiagnosefx.Controller;

/**
 * Controller class for the menu.
 */
public class MenuController implements Controller {
	/**
	 * The main menu bar.
	 */
	@FXML
	private MenuBar menuBar;

	/**
	 * The Menu entry "Close".
	 */
	@FXML
	private MenuItem menuClose;

	@Override
	public final Parent getRoot() {
		return menuBar;
	}

	/**
	 * Handler for menu entry "Exit".
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	protected final void exitApplication(final ActionEvent event) {
		Platform.exit();
	}

	/**
	 * Enable the close menu item.
	 *
	 * @param eventHandler
	 *            The event handler to be called when closing.
	 */
	public final void enableClose(final EventHandler<ActionEvent> eventHandler) {
		menuClose.setDisable(false);
		menuClose.setOnAction(eventHandler);
	}

	/**
	 * Disable the close menu item.
	 */
	public final void disableClose() {
		menuClose.setDisable(true);
	}
}
