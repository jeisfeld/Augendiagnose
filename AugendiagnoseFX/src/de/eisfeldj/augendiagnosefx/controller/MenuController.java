package de.eisfeldj.augendiagnosefx.controller;

import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_SHOW_COMMENT_PANE;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_SHOW_OVERLAY_PANE;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_SHOW_SPLIT_WINDOW;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import de.eisfeldj.augendiagnosefx.Application;
import de.eisfeldj.augendiagnosefx.util.DialogUtil;
import de.eisfeldj.augendiagnosefx.util.DialogUtil.ConfirmDialogListener;
import de.eisfeldj.augendiagnosefx.util.Logger;
import de.eisfeldj.augendiagnosefx.util.PreferenceUtil;
import de.eisfeldj.augendiagnosefx.util.ResourceConstants;
import de.eisfeldj.augendiagnosefx.util.ResourceUtil;
import de.eisfeldj.augendiagnosefx.util.SystemUtil;
import de.eisfeldj.augendiagnosefx.util.VersioningUtil;

/**
 * BaseController class for the menu.
 */
public class MenuController extends BaseController implements Initializable {
	/**
	 * The main menu bar.
	 */
	@FXML
	private MenuBar mMenuBar;

	/**
	 * The Menu entry "Close".
	 */
	@FXML
	private MenuItem mMenuClose;

	/**
	 * The Menu entry "Overlay Pane".
	 */
	@FXML
	private CheckMenuItem mMenuOverlayPane;

	public final CheckMenuItem getMenuOverlayPane() {
		return mMenuOverlayPane;
	}

	/**
	 * The Menu entry "Comment Pane".
	 */
	@FXML
	private CheckMenuItem mMenuCommentPane;

	public final CheckMenuItem getMenuCommentPane() {
		return mMenuCommentPane;
	}

	/**
	 * The Menu entry "Split window".
	 */
	@FXML
	private CheckMenuItem mMenuSplitWindow;

	@Override
	public final Parent getRoot() {
		return mMenuBar;
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
		Application.exitAfterConfirmation();
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
		PreferenceUtil.setPreference(KEY_SHOW_COMMENT_PANE, mMenuCommentPane.isSelected());
		for (DisplayImageController controller : getControllers(DisplayImageController.class)) {
			controller.showCommentPane(mMenuCommentPane.isSelected());
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
		PreferenceUtil.setPreference(KEY_SHOW_OVERLAY_PANE, mMenuOverlayPane.isSelected());
		for (DisplayImageController controller : getControllers(DisplayImageController.class)) {
			controller.showOverlayPane(mMenuOverlayPane.isSelected());
		}
	}

	/**
	 * Handler for menu entry "Split window".
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void toggleSplitWindow(final ActionEvent event) {
		if (!mMenuSplitWindow.isSelected() && MainController.hasDirtyBaseController()) {
			ConfirmDialogListener listener = new ConfirmDialogListener() {
				@Override
				public void onDialogPositiveClick() {
					PreferenceUtil.setPreference(KEY_SHOW_SPLIT_WINDOW, mMenuSplitWindow.isSelected());

					if (MainController.getInstance().hasClosablePage()) {
						MainController.getInstance().setSplitPane(mMenuSplitWindow.isSelected());
					}
				}

				@Override
				public void onDialogNegativeClick() {
					// revert
					mMenuSplitWindow.setSelected(!mMenuSplitWindow.isSelected());
				}
			};
			DialogUtil.displayConfirmationMessage(listener, ResourceConstants.BUTTON_OK,
					ResourceConstants.MESSAGE_CONFIRM_EXIT_UNSAVED);
		}
		else {
			PreferenceUtil.setPreference(KEY_SHOW_SPLIT_WINDOW, mMenuSplitWindow.isSelected());
			if (MainController.getInstance().hasClosablePage()) {
				MainController.getInstance().setSplitPane(mMenuSplitWindow.isSelected());
			}
		}
	}

	/**
	 * Handler for menu entry "Online Help".
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void showHelp(final ActionEvent event) {
		Application.getApplicationHostServices().showDocument(
				ResourceUtil.getString(ResourceConstants.DOCUMENTATION_URL));
	}

	/**
	 * Handler for menu entry "Check for updates".
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void checkUpdates(final ActionEvent event) {
		VersioningUtil.checkForNewerVersion(true);
	}

	/**
	 * Handler for menu entry "Uninstall".
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void uninstall(final ActionEvent event) {
		SystemUtil.uninstallApplication();
	}

	/**
	 * Handler for menu entry "About".
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void displayAboutMessage(final ActionEvent event) {
		DialogUtil.displayInfo(ResourceConstants.MESSAGE_INFO_APP_ABOUT,
				VersioningUtil.CURRENT_VERSION.getVersionString());
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
		mMenuClose.setDisable(!enabled);
		mMenuClose.setOnAction(eventHandler);
	}

	@Override
	public final void initialize(final URL location, final ResourceBundle resources) {
		mMenuCommentPane.setSelected(PreferenceUtil.getPreferenceBoolean(KEY_SHOW_COMMENT_PANE));
		mMenuOverlayPane.setSelected(PreferenceUtil.getPreferenceBoolean(KEY_SHOW_OVERLAY_PANE));
		mMenuSplitWindow.setSelected(PreferenceUtil.getPreferenceBoolean(KEY_SHOW_SPLIT_WINDOW));
	}

}
