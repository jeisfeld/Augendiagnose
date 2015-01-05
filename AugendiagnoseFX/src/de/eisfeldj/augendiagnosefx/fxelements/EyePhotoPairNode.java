package de.eisfeldj.augendiagnosefx.fxelements;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import de.eisfeldj.augendiagnosefx.Application;
import de.eisfeldj.augendiagnosefx.controller.DisplayImageController;
import de.eisfeldj.augendiagnosefx.util.EyePhoto;
import de.eisfeldj.augendiagnosefx.util.EyePhotoPair;
import de.eisfeldj.augendiagnosefx.util.FXMLUtil;

/**
 * Special GridPane for displaying a pair of eye photos.
 */
public class EyePhotoPairNode extends GridPane {
	/**
	 * The width of the date node.
	 */
	private static final int DATE_WIDTH = 70;

	/**
	 * The standard size of gaps.
	 */
	private static final int STANDARD_GAP = 10;

	/**
	 * Height of the left image.
	 */
	private double heightLeft = 0;
	/**
	 * Height of the right image.
	 */
	private double heightRight = 0;

	/**
	 * Constructor given a pair of eye photos.
	 *
	 * @param pair
	 *            The eye photo pair.
	 */
	public EyePhotoPairNode(final EyePhotoPair pair) {
		setHgap(STANDARD_GAP);
		add(new Label(pair.getDateDisplayString("dd.MM.yyyy")), 0, 0);
		getColumnConstraints().add(0, new ColumnConstraints(DATE_WIDTH));
		GridPane imagePane = new GridPane();
		imagePane.setHgap(STANDARD_GAP);
		add(imagePane, 1, 0);
		ColumnConstraints totalImageConstraints = new ColumnConstraints();
		totalImageConstraints.setHgrow(Priority.SOMETIMES);
		getColumnConstraints().add(1, totalImageConstraints);

		imagePane.add(getImageViewPane(pair.getRightEye()), 0, 0);
		imagePane.add(getImageViewPane(pair.getLeftEye()), 1, 0);
		ColumnConstraints imageConstraints = new ColumnConstraints();
		imageConstraints.setPercentWidth(50); // MAGIC_NUMBER
		imageConstraints.setHalignment(HPos.CENTER);
		imagePane.getColumnConstraints().add(0, imageConstraints);
		imagePane.getColumnConstraints().add(1, imageConstraints);
	}

	/**
	 * Get the image view for a thumbnail.
	 *
	 * @param eyePhoto
	 *            The eye photo to be displayed.
	 * @return The image view.
	 */
	private ImageViewPane getImageViewPane(final EyePhoto eyePhoto) {
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

		ImageViewPane imageViewPane = new ImageViewPane(imageView);
		imageViewPane.setPrefWidth(0);

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

		return imageViewPane;
	}

}
