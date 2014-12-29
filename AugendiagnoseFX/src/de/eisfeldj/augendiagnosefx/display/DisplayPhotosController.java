package de.eisfeldj.augendiagnosefx.display;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import de.eisfeldj.augendiagnosefx.util.FXMLUtil;

/**
 * Controller for the "Display Photos" page.
 */
public class DisplayPhotosController {
	/**
	 * The full "Display Photos" pane.
	 */
	@FXML
	private Pane displayMain;

	/**
	 * Action method for button "Close".
	 *
	 * @param event
	 *            The action event.
	 * @throws IOException
	 */
	@FXML
	protected final void close(final ActionEvent event) throws IOException {
		FXMLUtil.remove(displayMain);
	}
}
