package de.eisfeldj.augendiagnosefx.controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import de.eisfeldj.augendiagnosefx.Application;
import de.eisfeldj.augendiagnosefx.fxelements.SizeableImageView;
import de.eisfeldj.augendiagnosefx.util.EyePhoto;

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

	/**
	 * The displayed eye photo.
	 */
	private EyePhoto eyePhoto;

	@Override
	public final Parent getRoot() {
		return displayImage;
	}

	public final EyePhoto getEyePhoto() {
		return eyePhoto;
	}

	/**
	 * Setter for the eye photo. Initializes the view.
	 *
	 * @param eyePhoto
	 *            The eye photo.
	 */
	public final void setEyePhoto(final EyePhoto eyePhoto) {
		this.eyePhoto = eyePhoto;

		displayImageView.setImageView(
				eyePhoto.getImageView(Application.getScene().getWidth() - 2, Application.getScene().getHeight() - 27)); // MAGIC_NUMBER

	}

}
