package de.eisfeldj.augendiagnosefx.controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import de.eisfeldj.augendiagnosefx.fxelements.SizableImageView;
import de.eisfeldj.augendiagnosefx.util.imagefile.EyePhoto;

/**
 * BaseController for the "Display Image on Full window" page.
 */
public class DisplayImageFullController extends BaseController {
	/**
	 * The scroll pane holding the image.
	 */
	@FXML
	private SizableImageView displayImageView;

	@Override
	public final Parent getRoot() {
		return displayImageView;
	}

	/**
	 * Setter for the eye photo. Initializes the view.
	 *
	 * @param eyePhoto
	 *            The eye photo.
	 */
	public final void setImage(final EyePhoto eyePhoto, final Image image) {
		displayImageView.setImage(eyePhoto, image);
	}

}
