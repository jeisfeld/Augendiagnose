package de.eisfeldj.augendiagnosefx.fxelements;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import de.eisfeldj.augendiagnosefx.Application;
import de.eisfeldj.augendiagnosefx.controller.Controller;
import de.eisfeldj.augendiagnosefx.controller.DisplayImageController;
import de.eisfeldj.augendiagnosefx.util.EyePhoto;
import de.eisfeldj.augendiagnosefx.util.EyePhotoPair;
import de.eisfeldj.augendiagnosefx.util.FXMLUtil;

/**
 * Special GridPane for displaying a pair of eye photos.
 */
public class EyePhotoPairNode extends GridPane implements Controller {
	/**
	 * Height of the left image.
	 */
	private double heightLeft = 0;
	/**
	 * Height of the right image.
	 */
	private double heightRight = 0;

	/**
	 * The label for the date.
	 */
	@FXML
	private Label labelDate;

	/**
	 * The image view of the right eye.
	 */
	@FXML
	private ImageViewPane imageViewRight;

	/**
	 * The image view of the left eye.
	 */
	@FXML
	private ImageViewPane imageViewLeft;

	@Override
	public final Parent getRoot() {
		return this;
	}

	/**
	 * Constructor given a pair of eye photos.
	 *
	 * @param pair
	 *            The eye photo pair.
	 */
	public EyePhotoPairNode(final EyePhotoPair pair) {
		FXMLUtil.loadFromFxml(this, "EyePhotoPairNode.fxml");

		labelDate.setText(pair.getDateDisplayString("dd.MM.yyyy"));

		imageViewRight.setImageView(getImageView(pair.getRightEye()));
		imageViewLeft.setImageView(getImageView(pair.getLeftEye()));
	}

	/**
	 * Get the image view for a thumbnail.
	 *
	 * @param eyePhoto
	 *            The eye photo to be displayed.
	 * @return The image view.
	 */
	private ImageView getImageView(final EyePhoto eyePhoto) {
		ImageView imageView = new ImageView(eyePhoto.getImage());
		imageView.setPreserveRatio(true);
		imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				String fxmlName = Application.getScene().getWidth() > Application.getScene().getHeight()
						? "DisplayImageWide.fxml"
						: "DisplayImageNarrow.fxml";
				DisplayImageController controller =
						(DisplayImageController) FXMLUtil.displaySubpage(fxmlName);
				controller.setEyePhoto(eyePhoto);
			}
		});

		// Ensure that height is adapted to image width.
		imageView.fitHeightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
					final Number newValue) {
				switch (eyePhoto.getRightLeft()) {
				case RIGHT:
					heightRight = newValue.doubleValue();
					break;
				case LEFT:
					heightLeft = newValue.doubleValue();
					break;
				default:
				}
				setPrefHeight(Math.max(heightLeft, heightRight));
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						requestLayout();
					}
				});
			}
		});
		return imageView;
	}

}
