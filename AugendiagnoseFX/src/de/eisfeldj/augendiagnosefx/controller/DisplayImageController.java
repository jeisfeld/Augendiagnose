package de.eisfeldj.augendiagnosefx.controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

/**
 * Controller for the "Display Image" page.
 */
public class DisplayImageController implements Controller {

	/**
	 * The main pane holding the image.
	 */
	@FXML
	private GridPane displayImage;

	@Override
	public final Parent getRoot() {
		return displayImage;
	}

	/**
	 * Add the image to the pane.
	 *
	 * @param imageView
	 *            The imageView to be added.
	 */
	public final void setImageView(final ImageView imageView) {
		displayImage.add(imageView, 0, 0);
	}

}
