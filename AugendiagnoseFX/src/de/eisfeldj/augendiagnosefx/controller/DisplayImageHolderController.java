package de.eisfeldj.augendiagnosefx.controller;

import java.net.URL;
import java.util.ResourceBundle;

import de.eisfeldj.augendiagnosefx.Application;
import de.eisfeldj.augendiagnosefx.util.FxmlConstants;
import de.eisfeldj.augendiagnosefx.util.FxmlUtil;
import de.eisfeldj.augendiagnosefx.util.imagefile.EyePhoto;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

/**
 * A holder class for DisplayImageController - enables switching the instance keeping the mController.
 */
public class DisplayImageHolderController extends BaseController implements Initializable {
	/**
	 * The controller which is currently operated.
	 */
	private DisplayImageController mController;
	/**
	 * Flag indicating if the current mController is narrow.
	 */
	private boolean mIsNarrow;
	/**
	 * The pane holding the main view.
	 */
	@FXML
	private StackPane mBody;

	@Override
	public final Parent getRoot() {
		return mBody;
	}

	@Override
	public final boolean isDirty() {
		return mController.isDirty();
	}


	@Override
	public final void initialize(final URL location, final ResourceBundle resources) {
		mIsNarrow = requiresNarrow();
		String fxmlFile = mIsNarrow ? FxmlConstants.FXML_DISPLAY_IMAGE_NARROW : FxmlConstants.FXML_DISPLAY_IMAGE_WIDE;
		mController = (DisplayImageController) FxmlUtil.getRootFromFxml(fxmlFile);
		mBody.getChildren().add(mController.getRoot());
	}

	@Override
	public final void close() {
		mController.close();
		super.close();
	}

	/**
	 * Checks if narrow display is required.
	 *
	 * @return true if narrow display is required.
	 */
	public static final boolean requiresNarrow() {
		return MainController.getInstance().isSplitPane()
				|| Application.getScene().getWidth() <= Application.getScene().getHeight();
	}

	/**
	 * Setter for the eye photo. Initializes the view.
	 *
	 * @param eyePhoto
	 *            The eye photo.
	 */
	public final void setEyePhoto(final EyePhoto eyePhoto) {
		mController.setEyePhoto(eyePhoto);
	}

	@Override
	public final void refreshOnResize() {
		boolean requiresNarrow = requiresNarrow();
		if (requiresNarrow != mIsNarrow) {
			mIsNarrow = requiresNarrow;
			DisplayImageController oldController = mController;
			String fxmlFile = mIsNarrow ? FxmlConstants.FXML_DISPLAY_IMAGE_NARROW : FxmlConstants.FXML_DISPLAY_IMAGE_WIDE;
			mController = (DisplayImageController) FxmlUtil.getRootFromFxml(fxmlFile);
			mController.cloneContents(oldController);
			mBody.getChildren().clear();
			mBody.getChildren().add(mController.getRoot());
		}
	}

}
