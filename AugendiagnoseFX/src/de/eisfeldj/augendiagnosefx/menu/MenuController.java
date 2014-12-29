package de.eisfeldj.augendiagnosefx.menu;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.MenuItem;

/**
 * Controller class for the menu.
 */
public class MenuController {

	/**
	 * The Menu entry "Close".
	 */
	@FXML
	private MenuItem menuClose;

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
	 * @param root
	 *            The pane to be closed.
	 * @param eventHandler
	 *            The event handler to be called when closing.
	 */
	public final void enableClose(final Parent root, final EventHandler<ActionEvent> eventHandler) {
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
