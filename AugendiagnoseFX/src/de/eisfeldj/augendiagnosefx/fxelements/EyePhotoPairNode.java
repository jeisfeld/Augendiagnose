package de.eisfeldj.augendiagnosefx.fxelements;

import de.eisfeldj.augendiagnosefx.controller.BaseController;
import de.eisfeldj.augendiagnosefx.controller.Controller;
import de.eisfeldj.augendiagnosefx.controller.DisplayImageHolderController;
import de.eisfeldj.augendiagnosefx.controller.MainController;
import de.eisfeldj.augendiagnosefx.util.FxmlConstants;
import de.eisfeldj.augendiagnosefx.util.FxmlUtil;
import de.eisfeldj.augendiagnosefx.util.PreferenceUtil;
import de.eisfeldj.augendiagnosefx.util.imagefile.EyePhoto;
import de.eisfeldj.augendiagnosefx.util.imagefile.EyePhotoPair;
import de.eisfeldj.augendiagnosefx.util.imagefile.ImageUtil.Resolution;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

/**
 * Special GridPane for displaying a pair of eye photos.
 */
public class EyePhotoPairNode extends GridPane implements Controller {
	/**
	 * The parent controller.
	 */
	private BaseController mParentController;

	/**
	 * Height of the left image.
	 */
	private double mHeightLeft = 0;
	/**
	 * Height of the right image.
	 */
	private double mHeightRight = 0;

	/**
	 * The label for the date.
	 */
	@FXML
	private Label mLabelDate;

	/**
	 * The image view of the right eye.
	 */
	@FXML
	private ImageViewPane mImageViewRight;

	/**
	 * The image view of the left eye.
	 */
	@FXML
	private ImageViewPane mImageViewLeft;

	@Override
	public final Parent getRoot() {
		return this;
	}

	/**
	 * A boolean property indicating if images are loaded.
	 */
	private BooleanProperty mImagesLoadedProperty = new SimpleBooleanProperty(false);

	public final BooleanProperty getImagesLoadedProperty() {
		return mImagesLoadedProperty;
	}

	/**
	 * Constructor given a pair of eye photos.
	 *
	 * @param pair
	 *            The eye photo pair.
	 * @param initialParentController
	 *            The parent controller.
	 */
	@SuppressFBWarnings(value = "UR_UNINIT_READ", justification = "Is initialized via fxml")
	public EyePhotoPairNode(final EyePhotoPair pair, final BaseController initialParentController) {
		mParentController = initialParentController;

		FxmlUtil.loadFromFxml(this, FxmlConstants.FXML_EYE_PHOTO_PAIR_NODE);

		mLabelDate.setText(pair.getDateDisplayString());

		mImageViewRight.setImageView(getImageView(pair.getRightEye()));
		mImageViewLeft.setImageView(getImageView(pair.getLeftEye()));

		mLabelDate.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				if (MainController.getInstance().isSplitPane()) {
					return;
				}
				MainController.getInstance().setSplitPane(null);
				DisplayImageHolderController controllerRight =
						(DisplayImageHolderController) FxmlUtil.displaySubpage(FxmlConstants.FXML_DISPLAY_IMAGE_HOLDER, 0, true);
				controllerRight.setEyePhoto(pair.getRightEye());
				DisplayImageHolderController controllerLeft =
						(DisplayImageHolderController) FxmlUtil.displaySubpage(FxmlConstants.FXML_DISPLAY_IMAGE_HOLDER, 1, false);
				controllerLeft.setEyePhoto(pair.getLeftEye());
			}
		});
	}

	/**
	 * Get the image view for a thumbnail.
	 *
	 * @param eyePhoto
	 *            The eye photo to be displayed.
	 * @return The image view.
	 */
	private ImageView getImageView(final EyePhoto eyePhoto) {
		Image image = eyePhoto.getImage(Resolution.THUMB);
		image.progressProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
					final Number newValue) {
				if (newValue.doubleValue() == 1) {
					checkIfImagesLoaded();
				}
			}
		});

		ImageView imageView = new ImageView(image);
		imageView.setPreserveRatio(true);
		imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				if (PreferenceUtil.getPreferenceBoolean(PreferenceUtil.KEY_SHOW_SPLIT_WINDOW)
						&& !MainController.getInstance().isSplitPane()) {
					MainController.getInstance().setSplitPane(FxmlConstants.FXML_DISPLAY_PHOTOS);
				}

				DisplayImageHolderController controller = (DisplayImageHolderController) FxmlUtil
						.displaySubpage(FxmlConstants.FXML_DISPLAY_IMAGE_HOLDER, mParentController.getPaneIndex(), true);
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
					mHeightRight = newValue.doubleValue();
					break;
				case LEFT:
					mHeightLeft = newValue.doubleValue();
					break;
				default:
				}
				setPrefHeight(Math.max(mHeightLeft, mHeightRight));
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						requestParentLayout();
					}
				});
			}
		});
		return imageView;
	}

	/**
	 * Check if the images are loaded.
	 *
	 * @return true if the images are loaded.
	 */
	private boolean checkIfImagesLoaded() {
		if (mImagesLoadedProperty.get()) {
			return true;
		}

		Image imageRight = mImageViewRight.getImageView().getImage();
		Image imageLeft = mImageViewRight.getImageView().getImage();

		boolean loaded = imageRight.getProgress() == 1 && imageLeft.getProgress() == 1;
		if (loaded) {
			mImagesLoadedProperty.set(true);
		}
		return loaded;
	}

}
