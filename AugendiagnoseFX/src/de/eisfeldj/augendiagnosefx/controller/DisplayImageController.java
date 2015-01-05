package de.eisfeldj.augendiagnosefx.controller;

import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.BUTTON_EDIT_COMMENT;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.BUTTON_SAVE_COMMENT;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import de.eisfeldj.augendiagnosefx.fxelements.SizeableImageView;
import de.eisfeldj.augendiagnosefx.util.EyePhoto;
import de.eisfeldj.augendiagnosefx.util.JpegMetadata;
import de.eisfeldj.augendiagnosefx.util.ResourceUtil;

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
	 * The displayed eye photo.
	 */
	private EyePhoto eyePhoto;

	/**
	 * Temporary storage for the comment while editing.
	 */
	private String oldComment;

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

		displayImageView.setImageView(eyePhoto.getImageView());

		txtImageComment.setText(eyePhoto.getImageMetadata().comment);

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
			txtImageComment.setMinHeight(displayImage.getHeight() / 4); // MAGIC_NUMBER
			txtImageComment.setMinWidth(displayImage.getWidth() / 4); // MAGIC_NUMBER

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
			txtImageComment.setMinHeight(0);
			txtImageComment.setMinWidth(100); // MAGIC_NUMBER

			btnEditComment.setText(ResourceUtil.getString(BUTTON_EDIT_COMMENT));
		}
	}

}
