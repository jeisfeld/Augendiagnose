package de.eisfeldj.augendiagnosefx.controller;

import de.eisfeldj.augendiagnosefx.fxelements.SizableImageView;
import de.eisfeldj.augendiagnosefx.util.imagefile.EyePhoto;
import de.eisfeldj.augendiagnosefx.util.imagefile.JpegMetadata;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.image.Image;

/**
 * BaseController for the "Display Image on Full window" page.
 */
public class DisplayImageFullController extends BaseController {
	/**
	 * The scroll pane holding the image.
	 */
	@FXML
	private SizableImageView mDisplayImageView;

	@Override
	public final Parent getRoot() {
		return mDisplayImageView;
	}

	/**
	 * Initialize the view.
	 *
	 * @param metadata
	 *            The metadata used for scaling.
	 * @param image
	 *            the image to be displayed.
	 * @param eyePhoto
	 *            the eye photo to be displayed.
	 */
	public final void setImage(final JpegMetadata metadata, final Image image, final EyePhoto eyePhoto) {
		mDisplayImageView.setImage(metadata, image, eyePhoto);
	}

}
