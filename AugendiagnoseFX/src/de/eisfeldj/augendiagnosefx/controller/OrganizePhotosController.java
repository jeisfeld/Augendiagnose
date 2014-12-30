package de.eisfeldj.augendiagnosefx.controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

/**
 * Controller for the "Display Photos" page.
 */
public class OrganizePhotosController implements Controller {
	/**
	 * The full "Display Photos" pane.
	 */
	@FXML
	private Pane organizeMain;

	@Override
	public final Parent getRoot() {
		return organizeMain;
	}

}
