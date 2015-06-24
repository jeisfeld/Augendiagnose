package de.eisfeldj.augendiagnosefx.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import de.eisfeldj.augendiagnosefx.util.DialogUtil;
import de.eisfeldj.augendiagnosefx.util.DialogUtil.ConfirmDialogListener;
import de.eisfeldj.augendiagnosefx.util.FxmlConstants;
import de.eisfeldj.augendiagnosefx.util.FxmlUtil;
import de.eisfeldj.augendiagnosefx.util.Logger;
import de.eisfeldj.augendiagnosefx.util.PreferenceUtil;
import de.eisfeldj.augendiagnosefx.util.ResourceConstants;

/**
 * The controller of the main window.
 */
public class MainController extends BaseController implements Initializable {
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
	 * The save icon.
	 */
	@FXML
	private ImageView imageSave;

	/**
	 * The panes (one or two) containing the body.
	 */
	private StackPane[] bodies = new StackPane[0];

	/**
	 * Number of subpages that cannot be closed.
	 */
	private int unclosablePages = 0;

	/**
	 * The list of subpages.
	 */
	private List<BaseController> subPageRegistry = new ArrayList<BaseController>();

	/**
	 * A list storing the handlers for closing windows.
	 */
	private List<EventHandler<ActionEvent>> closeHandlerList = new ArrayList<EventHandler<ActionEvent>>();

	/**
	 * Indicator if two panes are shown.
	 */
	private boolean isSplitPane = false;

	/**
	 * Get information if two panes are shown.
	 *
	 * @return True if two panes are shown.
	 */
	public final boolean isSplitPane() {
		return getInstance().isSplitPane;
	}

	/**
	 * Set either single pane or split pane.
	 *
	 * @param newIsSplitPane
	 *            If true, then split pane is set, otherwise single pane is set.
	 */
	public final void setSplitPane(final boolean newIsSplitPane) {
		isSplitPane = newIsSplitPane;

		if (isSplitPane()) {
			StackPane body1 = new StackPane();
			StackPane body2 = new StackPane();

			SplitPane splitPane = new SplitPane();
			splitPane.setOrientation(Orientation.HORIZONTAL);
			splitPane.getItems().add(body1);
			splitPane.getItems().add(body2);

			bodies = new StackPane[] { body1, body2 };

			// retain old body as left pane.
			body1.getChildren().addAll(body.getChildren());
			FxmlUtil.displaySubpage(FxmlConstants.FXML_DISPLAY_PHOTOS, 1, false);

			body.getChildren().clear();
			body.getChildren().add(splitPane);

		}
		else {
			body.getChildren().clear();

			// retain old left pane.
			removeSubPage(subPageRegistry.get(1));
			body.getChildren().addAll(bodies[0].getChildren());

			bodies = new StackPane[] { body };
		}
	}

	@Override
	public final Parent getRoot() {
		return mainPane;
	}

	@Override
	public final void initialize(final URL location, final ResourceBundle resources) {
		bodies = new StackPane[] { body };
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
			throw new RuntimeException(e);
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
	 * @param paneIndex
	 *            The pane where to add the subpage.
	 * @param isClosable
	 *            Indicator if this is a closable page.
	 */
	public final void addSubPage(final BaseController controller, final int paneIndex, final boolean isClosable) {
		bodies[paneIndex].getChildren().add(controller.getRoot());
		int position;
		if (isClosable) {
			position = subPageRegistry.size();
			subPageRegistry.add(controller);
		}
		else {
			position = unclosablePages;
			subPageRegistry.add(position, controller);
			unclosablePages++;
		}

		// Enable the close menu.
		enableClose(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				if (controller.isDirty()) {
					ConfirmDialogListener listener = new ConfirmDialogListener() {
						@Override
						public void onDialogPositiveClick() {
							controller.setDirty(false);
							FxmlUtil.removeSubpage(controller);
						}

						@Override
						public void onDialogNegativeClick() {
							// do nothing.
						}
					};
					DialogUtil.displayConfirmationMessage(listener, ResourceConstants.BUTTON_OK,
							ResourceConstants.MESSAGE_CONFIRM_EXIT_UNSAVED);
				}
				else {
					FxmlUtil.removeSubpage(controller);
				}
			}
		}, position);
	}

	/**
	 * Remove a subpage.
	 *
	 * @param controller
	 *            The controller of the subpage.
	 */
	public final void removeSubPage(final BaseController controller) {
		controller.close();
		int index = subPageRegistry.indexOf(controller);
		if (index >= 0 && index < unclosablePages) {
			unclosablePages--;
		}
		subPageRegistry.remove(controller);
		disableClose(index);

		if (PreferenceUtil.getPreferenceBoolean(PreferenceUtil.KEY_SHOW_SPLIT_WINDOW)
				&& MainController.getInstance().isSplitPane() && !hasClosablePage()) {
			MainController.getInstance().setSplitPane(false);
		}

	}

	/**
	 * Remove all subpages.
	 */
	public final void removeAllSubPages() {
		for (BaseController controller : subPageRegistry) {
			controller.close();
		}
		subPageRegistry.clear();
		unclosablePages = 0;
		disableAllClose();
	}

	/**
	 * Enable the close menu item.
	 *
	 * @param eventHandler
	 *            The event handler to be called when closing.
	 * @param position
	 *            The position in the stack. (-1 means end)
	 */
	private void enableClose(final EventHandler<ActionEvent> eventHandler, final int position) {
		if (position >= 0) {
			closeHandlerList.add(position, eventHandler);
		}
		else {
			closeHandlerList.add(eventHandler);
		}
		if (hasClosablePage()) {
			MenuController.getInstance().setMenuClose(true, eventHandler);

			closeButton.setVisible(true);
			closeButton.setOnAction(closeHandlerList.get(closeHandlerList.size() - 1));
		}
	}

	/**
	 * Disable one level of the close menu item.
	 *
	 * @param position
	 *            The position in the stack. (-1 means end)
	 */
	private void disableClose(final int position) {
		if (position >= 0) {
			closeHandlerList.remove(position);
		}
		else {
			closeHandlerList.remove(closeHandlerList.size() - 1);
		}
		if (hasClosablePage()) {
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
	 * Check if there is a page that can be closed.
	 *
	 * @return true if there is a page that can be closed.
	 */
	public final boolean hasClosablePage() {
		return closeHandlerList.size() > unclosablePages;
	}

	/**
	 * Disable all levels of the close menu icon.
	 */
	private void disableAllClose() {
		closeHandlerList.clear();
		MenuController.getInstance().setMenuClose(false, null);
		closeButton.setVisible(false);
	}

	/**
	 * Show the save icon.
	 */
	public static void showSaveIcon() {
		getInstance().imageSave.setVisible(true);
	}

	/**
	 * Hide the save icon.
	 */
	public static void hideSaveIcon() {
		getInstance().imageSave.setVisible(false);
	}

	/**
	 * Find out if there is any dirty page that requires saving.
	 *
	 * @return true if there is a dirty page.
	 */
	public static boolean hasDirtyBaseController() {
		for (BaseController controller : getInstance().subPageRegistry) {
			if (controller.isDirty()) {
				return true;
			}
		}
		return false;
	}

}
