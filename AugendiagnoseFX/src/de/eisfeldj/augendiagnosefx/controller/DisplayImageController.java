package de.eisfeldj.augendiagnosefx.controller;

import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_SHOW_COMMENT_PANE;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_SHOW_OVERLAY_PANE;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.BUTTON_EDIT_COMMENT;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.BUTTON_SAVE_COMMENT;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.ConstraintsBase;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import de.eisfeldj.augendiagnosefx.fxelements.SizeableImageView;
import de.eisfeldj.augendiagnosefx.util.EyePhoto;
import de.eisfeldj.augendiagnosefx.util.JpegMetadata;
import de.eisfeldj.augendiagnosefx.util.PreferenceUtil;
import de.eisfeldj.augendiagnosefx.util.ResourceUtil;

/**
 * BaseController for the "Display Image" page.
 */
public class DisplayImageController extends BaseController implements Initializable {
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
	 * The pane used for displaying and editing the comment.
	 */
	@FXML
	private Pane commentPane;

	/**
	 * The constraints of the comment pane.
	 */
	@FXML
	private ConstraintsBase commentConstraints;

	/**
	 * The pane used for toggling the overlay.
	 */
	@FXML
	private GridPane overlayPane;

	/**
	 * The constraints of the overlay pane.
	 */
	@FXML
	private ConstraintsBase overlayConstraints;

	/**
	 * The text field for the image comment.
	 */
	@FXML
	private TextArea txtImageComment;

	/**
	 * The Button for editing/saving the image comment.
	 */
	@FXML
	private ToggleButton btnEditComment;

	/**
	 * The Button for adding the circle overlay.
	 */
	@FXML
	private ToggleButton btnOverlayCircle;

	/**
	 * The Buttons for overlays.
	 */
	// JAVADOC:OFF
	@FXML
	private ToggleButton btnOverlay1;
	@FXML
	private ToggleButton btnOverlay2;
	@FXML
	private ToggleButton btnOverlay3;
	@FXML
	private ToggleButton btnOverlay4;
	@FXML
	private ToggleButton btnOverlay5;
	@FXML
	private ToggleButton btnOverlay6;
	// JAVADOC:ON

	/**
	 * The displayed eye photo.
	 */
	private EyePhoto eyePhoto;

	/**
	 * Temporary storage for the comment while editing.
	 */
	private String oldComment;

	@Override
	public final void initialize(final URL location, final ResourceBundle resources) {
		MenuController.getInstance().getMenuCommentPane().setDisable(false);
		MenuController.getInstance().getMenuOverlayPane().setDisable(false);
		showCommentPane(PreferenceUtil.getPreferenceBoolean(KEY_SHOW_COMMENT_PANE));
		showOverlayPane(PreferenceUtil.getPreferenceBoolean(KEY_SHOW_OVERLAY_PANE));
	}

	@Override
	public final void close() {
		super.close();
		if (getControllers(DisplayImageController.class).size() == 0) {
			MenuController.getInstance().getMenuCommentPane().setDisable(true);
			MenuController.getInstance().getMenuOverlayPane().setDisable(true);
		}
	}

	@Override
	public final Parent getRoot() {
		return displayImage;
	}

	/**
	 * Action method for button "Edit Comment".
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void btnEditCommentPressed(final ActionEvent event) {
		if (btnEditComment.isSelected()) {
			// make comment editable
			oldComment = txtImageComment.getText();
			txtImageComment.setEditable(true);
			txtImageComment.requestFocus();

			btnEditComment.setText(ResourceUtil.getString(BUTTON_SAVE_COMMENT));
		}
		else {
			txtImageComment.setEditable(false);
			String newComment = txtImageComment.getText();

			// Save only if comment changed.
			if (!((newComment == null && oldComment == null) || (newComment != null && newComment.equals(oldComment)))) {
				JpegMetadata metadata = eyePhoto.getImageMetadata();
				metadata.comment = newComment;
				eyePhoto.storeImageMetadata(metadata);
			}

			btnEditComment.setText(ResourceUtil.getString(BUTTON_EDIT_COMMENT));
		}
	}

	/**
	 * Action method for button "Overlay".
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void btnOverlayPressed(final ActionEvent event) {
		ToggleButton source = (ToggleButton) event.getSource();
		if (source.isSelected()) {
			String btnId = source.getId();
			Integer overlayType = null;

			switch (btnId) {
			case "btnOverlayCircle":
				overlayType = 0;
				break;
			default:
				String indexStr = btnId.substring("btnOverlay".length());
				overlayType = Integer.parseInt(indexStr);
			}

			showOverlay(overlayType);
		}
		else {
			showOverlay(null);
		}
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

		displayImageView.setEyePhoto(eyePhoto);

		enableOverlayButtons(eyePhoto.getImageMetadata().hasOverlayPosition());

		txtImageComment.setText(eyePhoto.getImageMetadata().comment);
	}

	/**
	 * Display a specific overlay on the eye photo.
	 *
	 * @param overlayType
	 *            The overlay type to be displayed.
	 */
	public final void showOverlay(final Integer overlayType) {
		displayImageView.displayOverlay(overlayType);
	}

	/**
	 * Show or hide the comment pane.
	 *
	 * @param visible
	 *            Indicator if the pane should be visible.
	 */
	public final void showCommentPane(final boolean visible) {
		commentPane.setVisible(visible);
		commentPane.setManaged(visible);
		if (commentConstraints instanceof ColumnConstraints) {
			((ColumnConstraints) commentConstraints).setPercentWidth(visible ? 20 : 0); // MAGIC_NUMBER
		}
		if (commentConstraints instanceof RowConstraints) {
			((RowConstraints) commentConstraints).setPercentHeight(visible ? 20 : 0); // MAGIC_NUMBER
		}
	}

	/**
	 * Show or hide the overlay pane.
	 *
	 * @param visible
	 *            Indicator if the pane should be visible.
	 */
	public final void showOverlayPane(final boolean visible) {
		overlayPane.setVisible(visible);
		overlayPane.setManaged(visible);
		if (overlayConstraints instanceof ColumnConstraints) {
			((ColumnConstraints) overlayConstraints).setMinWidth(visible ? 70 : 0); // MAGIC_NUMBER
		}
	}

	/**
	 * Enable or disable the overlay buttons.
	 *
	 * @param enabled
	 *            Indicator if the overlay buttons should be enabled.
	 */
	private void enableOverlayButtons(final boolean enabled) {
		btnOverlayCircle.setDisable(!enabled);
		btnOverlay1.setDisable(!enabled);
		btnOverlay2.setDisable(!enabled);
		btnOverlay3.setDisable(!enabled);
		btnOverlay4.setDisable(!enabled);
		btnOverlay5.setDisable(!enabled);
		btnOverlay6.setDisable(!enabled);
	}

}
