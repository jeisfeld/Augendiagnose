package de.eisfeldj.augendiagnosefx.controller;

import java.net.URL;
import java.util.ResourceBundle;

import de.eisfeldj.augendiagnosefx.util.FxmlConstants;
import de.eisfeldj.augendiagnosefx.util.FxmlUtil;
import de.eisfeldj.augendiagnosefx.util.imagefile.EyePhotoPair;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;

/**
 * A holder class for DisplayImageController - enables switching the instance keeping the mController.
 */
public class DisplayImagePairController extends BaseController implements Initializable {
	/**
	 * The controller of the right eye.
	 */
	private DisplayImageController mControllerRight;
	/**
	 * The controller of the left eye.
	 */
	private DisplayImageController mControllerLeft;
	/**
	 * The pane holding the main view.
	 */
	@FXML
	private SplitPane mBody;

	@Override
	public final Parent getRoot() {
		return mBody;
	}

	@Override
	public final boolean isDirty() {
		return mControllerRight.isDirty() || mControllerLeft.isDirty();
	}

	@Override
	public final void initialize(final URL location, final ResourceBundle resources) {
	}

	@Override
	public final void close() {
		mControllerRight.close();
		mControllerLeft.close();
		super.close();
	}

	/**
	 * Setter for the eye photo pair. Initializes the view.
	 *
	 * @param eyePhotoPair The eye photo pair.
	 */
	public final void setEyePhotos(final EyePhotoPair eyePhotoPair) {
		mControllerRight = (DisplayImageController) FxmlUtil.getRootFromFxml(FxmlConstants.FXML_DISPLAY_IMAGE_NARROW);
		mControllerLeft = (DisplayImageController) FxmlUtil.getRootFromFxml(FxmlConstants.FXML_DISPLAY_IMAGE_NARROW);
		mControllerRight.setEyePhoto(eyePhotoPair.getRightEye());
		mControllerLeft.setEyePhoto(eyePhotoPair.getLeftEye());

		mBody.getItems().add(mControllerRight.getRoot());
		mBody.getItems().add(mControllerLeft.getRoot());
	}
}
