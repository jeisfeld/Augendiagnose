package de.eisfeldj.augendiagnosefx.controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import de.eisfeldj.augendiagnosefx.fxelements.SizeableImageView;

/**
 * Controller for the "Display Image" page.
 */
public class DisplayImageController implements Controller {
	/**
	 * The main pane holding the image.
	 */
	@FXML
	private GridPane displayImage;

	/**
	 * The scroll pane holding the image.
	 */
	@FXML
	private SizeableImageView displayImageView;

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
		displayImageView.setImageView(imageView);
	}

}
