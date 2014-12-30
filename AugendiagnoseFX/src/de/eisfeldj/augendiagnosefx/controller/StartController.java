package de.eisfeldj.augendiagnosefx.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import de.eisfeldj.augendiagnosefx.util.FXMLUtil;

/**
 * The controller of the start body.
 */
public class StartController implements Controller {
	/**
	 * The root pane of the start page.
	 */
	@FXML
	private GridPane start;

	@Override
	public final Parent getRoot() {
		return start;
	}

	/**
	 * Handler for button "Organize new photos".
	 *
	 * @param event
	 *            The action event.
	 * @throws IOException
	 */
	@FXML
	protected final void handleButtonOrganize(final ActionEvent event) {
		FXMLUtil.displaySubpage("OrganizePhotos.fxml");
	}

	/**
	 * Handler for button "Display Photos".
	 *
	 * @param event
	 *            The action event.
	 * @throws IOException
	 */
	@FXML
	protected final void handleButtonDisplay(final ActionEvent event) {
		FXMLUtil.displaySubpage("DisplayPhotos.fxml");
	}

}