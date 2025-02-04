package de.eisfeldj.augendiagnosefx.util;

import java.io.IOException;
import java.net.URL;

import de.eisfeldj.augendiagnosefx.controller.BaseController;
import de.eisfeldj.augendiagnosefx.controller.Controller;
import de.eisfeldj.augendiagnosefx.controller.MainController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * Utility class for reading FXML files.
 */
public final class FxmlUtil {
	/**
	 * Private constructor to prevent instantiation.
	 */
	private FxmlUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Utility method to expand an FXML file, including internationalization.
	 *
	 * @param fxmlFile
	 *            The name of the FXML file.
	 * @return The controller handling the expanded page.
	 */
	public static Controller getRootFromFxml(final String fxmlFile) {
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setResources(ResourceUtil.STRINGS_BUNDLE);
		Parent root;
		try {
			URL file = FxmlUtil.class.getResource("/fxml/" + fxmlFile);
			root = fxmlLoader.load(file.openStream());
			root.getStylesheets().add(FxmlUtil.class.getResource("/css/application.css").toExternalForm());

			return fxmlLoader.getController();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Utility method to expand an FXML file for a custom component.
	 *
	 * @param root
	 *            The custom component.
	 * @param fxmlFile
	 *            The FXML file.
	 */
	public static void loadFromFxml(final Parent root, final String fxmlFile) {
		FXMLLoader fxmlLoader = new FXMLLoader(ClassLoader.getSystemResource("fxml/" + fxmlFile));
		fxmlLoader.setResources(ResourceUtil.STRINGS_BUNDLE);
		fxmlLoader.setController(root);
		fxmlLoader.setRoot(root);
		try {
			fxmlLoader.load();
			root.getStylesheets().add(ClassLoader.getSystemResource("css/application.css").toExternalForm());
		}
		catch (IOException e) {
			Logger.error("Failed to load FXML file " + fxmlFile, e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Utility method to expand and display an FXML file in the body as subpage.
	 *
	 * @param fxmlFile
	 *            The name of the FXML file.
	 * @param paneIndex
	 *            The pane where to display it in case of multiple panes.
	 * @param isClosable
	 *            Indicator if this is a closable page. (-1 means end)
	 * @return the controller of the subpage.
	 */
	public static BaseController displaySubpage(final String fxmlFile, final int paneIndex, final boolean isClosable) {
		BaseController controller;
		controller = (BaseController) getRootFromFxml(fxmlFile);
		MainController.getInstance().addSubPage(controller, paneIndex, isClosable);

		return controller;
	}

	/**
	 * Utility method to remove a node. In particular, a full pane can be removed.
	 *
	 * @param node
	 *            The node to be removed.
	 */
	public static void remove(final Node node) {
		if (node.getParent() != null && node.getParent() instanceof Pane) {
			((Pane) node.getParent()).getChildren().remove(node);
		}
	}

	/**
	 * Utility method to remove all panes from the stack.
	 */
	public static void removeAllSubpages() {
		MainController.getInstance().removeAllSubPages();
	}

	/**
	 * Utility method to expand and display the menu bar.
	 *
	 * @param fxmlFile
	 *            The name of the FXML file.
	 */
	public static void displayMenu(final String fxmlFile) {
		MenuBar root = (MenuBar) getRootFromFxml(fxmlFile).getRoot();
		MainController.getInstance().setMenuBarContents(root);
	}

	/**
	 * Utility method to temporarily add a visible border around a region.
	 *
	 * @param region
	 *            The region getting the border.
	 * @param color
	 *            the color of the border.
	 */
	public static void addDummyBorder(final Region region, final Color color) {
		region.setBorder(new Border(new BorderStroke(color, BorderStrokeStyle.SOLID, null, new BorderWidths(5)))); // MAGIC_NUMBER
	}

}
