package de.eisfeldj.augendiagnosefx.controller;

import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_SHOW_COMMENT_PANE;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_SHOW_OVERLAY_PANE;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import de.eisfeldj.augendiagnosefx.util.DialogUtil;
import de.eisfeldj.augendiagnosefx.util.Logger;
import de.eisfeldj.augendiagnosefx.util.PreferenceUtil;

/**
 * BaseController class for the menu.
 */
public class MenuController extends BaseController implements Initializable {
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

	/**
	 * The Menu entry "Overlay Pane".
	 */
	@FXML
	private CheckMenuItem menuOverlayPane;

	public final CheckMenuItem getMenuOverlayPane() {
		return menuOverlayPane;
	}

	/**
	 * The Menu entry "Comment Pane".
	 */
	@FXML
	private CheckMenuItem menuCommentPane;

	public final CheckMenuItem getMenuCommentPane() {
		return menuCommentPane;
	}

	@Override
	public final Parent getRoot() {
		return menuBar;
	}

	/**
	 * Get the main controller instance.
	 *
	 * @return The main controller instance.
	 */
	public static MenuController getInstance() {
		try {
			return getController(MenuController.class);
		}
		catch (TooManyControllersException | MissingControllerException e) {
			Logger.error("Could not find menu controller", e);
			throw new RuntimeException(e);
		}
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
	 * Handler for menu entry "Preferences".
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void showPreferences(final ActionEvent event) {
		DialogUtil.displayPreferencesDialog();
	}

	/**
	 * Handler for menu entry "Comment pane".
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void toggleCommentPane(final ActionEvent event) {
		for (DisplayImageController controller : getControllers(DisplayImageController.class)) {
			controller.showCommentPane(menuCommentPane.isSelected());
			PreferenceUtil.setPreference(KEY_SHOW_COMMENT_PANE, menuCommentPane.isSelected());
		}
	}

	/**
	 * Handler for menu entry "Overlay pane".
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void toggleOverlayPane(final ActionEvent event) {
		for (DisplayImageController controller : getControllers(DisplayImageController.class)) {
			controller.showOverlayPane(menuOverlayPane.isSelected());
			PreferenceUtil.setPreference(KEY_SHOW_OVERLAY_PANE, menuCommentPane.isSelected());
		}
	}

	/**
	 * Configure the "Close" menu entry.
	 *
	 * @param enabled
	 *            The enablement status.
	 * @param eventHandler
	 *            The event handler.
	 */
	public final void setMenuClose(final boolean enabled, final EventHandler<ActionEvent> eventHandler) {
		menuClose.setDisable(!enabled);
		menuClose.setOnAction(eventHandler);
	}

	@Override
	public final void initialize(final URL location, final ResourceBundle resources) {
		menuCommentPane.setSelected(PreferenceUtil.getPreferenceBoolean(KEY_SHOW_COMMENT_PANE));
		menuOverlayPane.setSelected(PreferenceUtil.getPreferenceBoolean(KEY_SHOW_OVERLAY_PANE));
	}

}
