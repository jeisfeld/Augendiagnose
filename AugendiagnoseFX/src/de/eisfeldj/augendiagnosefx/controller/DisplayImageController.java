package de.eisfeldj.augendiagnosefx.controller;

import static de.eisfeldj.augendiagnosefx.util.ImageUtil.Resolution.FULL;
import static de.eisfeldj.augendiagnosefx.util.ImageUtil.Resolution.NORMAL;
import static de.eisfeldj.augendiagnosefx.util.ImageUtil.Resolution.THUMB;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_OVERLAY_COLOR;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_SHOW_COMMENT_PANE;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_SHOW_OVERLAY_PANE;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.BUTTON_EDIT_COMMENT;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.BUTTON_SAVE_COMMENT;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.ConstraintsBase;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import de.eisfeldj.augendiagnosefx.fxelements.OverlayImageView;
import de.eisfeldj.augendiagnosefx.fxelements.SizableImageView.MetadataPosition;
import de.eisfeldj.augendiagnosefx.util.EyePhoto;
import de.eisfeldj.augendiagnosefx.util.ImageUtil.Resolution;
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
	private OverlayImageView displayImageView;

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
	private Pane overlayPane;

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
	 * The button for displaying the view in full resolution.
	 *
	 * <p>
	 * This is a ToggleButton, as it is incompatible with overlays.
	 */
	@FXML
	private ToggleButton clarityButton;

	/**
	 * The slider for brightness.
	 */
	@FXML
	private Slider sliderBrightness;

	/**
	 * The slider for contrast.
	 */
	@FXML
	private Slider sliderContrast;

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
	@FXML
	private ToggleButton btnOverlay7;
	// JAVADOC:ON

	/**
	 * The Button for selecting the overlay color.
	 */
	@FXML
	private ColorPicker colorPicker;

	/**
	 * The displayed eye photo.
	 */
	private EyePhoto eyePhoto;

	/**
	 * Flag storing if the view is already initialized. (However, the image may be loaded later asynchronously.)
	 */
	private boolean initialized = false;

	/**
	 * Temporary storage for the comment while editing.
	 */
	private String oldComment;

	/**
	 * Slider indicating if the current state displays only a thumbnail.
	 */
	private Resolution currentResolution = NORMAL;

	/**
	 * Storage for the current overlay type.
	 */
	private Integer currentOverlayType = null;

	/**
	 * Update the stored current resolution, redisplay if the resolution changed, and update the clarityButton if
	 * appicable.
	 *
	 * @param newResolution
	 *            The current resolution.
	 */
	private void updateResolution(final Resolution newResolution) {
		if (newResolution != currentResolution) {
			currentResolution = newResolution;
			if (newResolution != FULL) {
				clarityButton.setSelected(false);
			}
			displayImageView.redisplay(newResolution);
		}
	}

	@Override
	public final void initialize(final URL location, final ResourceBundle resources) {
		try {
			MenuController.getInstance().getMenuCommentPane().setDisable(false);
			MenuController.getInstance().getMenuOverlayPane().setDisable(false);
		}
		catch (RuntimeException e) {
			// Catching exception so that JavaFX preview works.
		}
		showCommentPane(PreferenceUtil.getPreferenceBoolean(KEY_SHOW_COMMENT_PANE));
		showOverlayPane(PreferenceUtil.getPreferenceBoolean(KEY_SHOW_OVERLAY_PANE));
		colorPicker.setValue(PreferenceUtil.getPreferenceColor(KEY_OVERLAY_COLOR));
		colorPicker.getStyleClass().add("button");

		sliderBrightness.setMin(-1);
		sliderBrightness.setValue(0);
		sliderBrightness.setMax(1);
		sliderContrast.setMin(-1);
		sliderContrast.setValue(0);
		sliderContrast.setMax(1);

		initialized = true;
	}

	/**
	 * Initialize the sliders for contrast and brightness.
	 */
	private void initializeSliders() {
		sliderBrightness.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
					final Number newValue) {
				displayImageView.setBrightness(newValue.floatValue(), currentResolution);
			}
		});
		sliderBrightness.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				updateResolution(THUMB);
			}
		});
		sliderBrightness.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				updateResolution(NORMAL);
			}
		});

		// Inititlize slider for contrast.
		sliderContrast.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
					final Number newValue) {
				displayImageView.setContrast(newValue.floatValue(), currentResolution);
			}
		});
		sliderContrast.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				updateResolution(THUMB);
			}
		});
		sliderContrast.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				updateResolution(NORMAL);
			}
		});
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
			if (!(newComment == null && oldComment == null) && !(newComment != null && newComment.equals(oldComment))) {
				JpegMetadata metadata = eyePhoto.getImageMetadata();
				metadata.comment = newComment;
				eyePhoto.storeImageMetadata(metadata);
			}

			btnEditComment.setText(ResourceUtil.getString(BUTTON_EDIT_COMMENT));
		}
	}

	/**
	 * Action method for button "Overlay x".
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

			updateResolution(NORMAL);
			showOverlay(overlayType);
		}
		else {
			showOverlay(null);
		}
	}

	/**
	 * Action method for clarity button.
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void btnClarityPressed(final ActionEvent event) {
		if (clarityButton.isSelected()) {
			showOverlay(null);
			updateResolution(FULL);
		}
		else {
			updateResolution(NORMAL);
		}
	}

	/**
	 * Action method for color picker.
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void onColorChanged(final ActionEvent event) {
		showOverlay(currentOverlayType);
	}

	/**
	 * Action method for storing brightness and contrast.
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void storeBrightnessContrast(final ActionEvent event) {
		if (isInitialized()) {
			JpegMetadata metadata = eyePhoto.getImageMetadata();
			if (metadata != null) {
				metadata.brightness = (float) sliderBrightness.getValue();
				metadata.contrast = OverlayImageView.seekbarContrastToStoredContrast((float) sliderContrast.getValue());

				eyePhoto.storeImageMetadata(metadata);
			}
		}
	}

	/**
	 * Action method for resetting brightness and contrast.
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void resetBrightnessContrast(final ActionEvent event) {
		sliderBrightness.setValue(0);
		sliderContrast.setValue(0);
		storeBrightnessContrast(event);
	}

	/**
	 * Action method for storing the view position.
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void storeViewPosition(final ActionEvent event) {
		if (isInitialized()) {
			JpegMetadata metadata = eyePhoto.getImageMetadata();
			if (metadata != null) {
				MetadataPosition position = displayImageView.getPosition();
				metadata.xPosition = position.xCenter;
				metadata.yPosition = position.yCenter;
				metadata.zoomFactor = position.zoom;

				eyePhoto.storeImageMetadata(metadata);
			}
		}
	}

	/**
	 * Action method for resetting the view position.
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void resetViewPosition(final ActionEvent event) {
		if (isInitialized()) {
			JpegMetadata metadata = eyePhoto.getImageMetadata();
			if (metadata != null) {
				metadata.xPosition = null;
				metadata.yPosition = null;
				metadata.zoomFactor = null;

				eyePhoto.storeImageMetadata(metadata);
				// re-set eyePhoto in order to do initial scaling again.
				displayImageView.setEyePhoto(eyePhoto);
			}
		}
	}

	/**
	 * Setter for the eye photo. Initializes the view.
	 *
	 * @param eyePhoto
	 *            The eye photo.
	 */
	public final void setEyePhoto(final EyePhoto eyePhoto) {
		this.eyePhoto = eyePhoto;
		JpegMetadata metadata = eyePhoto.getImageMetadata();

		if (metadata.hasBrightnessContrast()) {
			sliderBrightness.setValue(metadata.brightness);
			sliderContrast.setValue(OverlayImageView.storedContrastToSeekbarContrast(metadata.contrast));
			displayImageView.initializeBrightnessContrast(metadata.brightness, metadata.contrast);
		}
		// Only now the listeners should be initialized, as image is not yet loaded and listeners should not
		// react on initial slider setup.
		initializeSliders();

		displayImageView.setEyePhoto(eyePhoto);

		enableOverlayButtons(metadata.hasOverlayPosition());

		txtImageComment.setText(metadata.comment);
	}

	/**
	 * Display a specific overlay on the eye photo.
	 *
	 * @param overlayType
	 *            The overlay type to be displayed.
	 */
	public final void showOverlay(final Integer overlayType) {
		currentOverlayType = overlayType;
		if (overlayType != null) {
			currentResolution = NORMAL;
		}
		displayImageView.displayOverlay(overlayType, colorPicker.getValue(), currentResolution);
	}

	/**
	 * Show or hide the comment pane.
	 *
	 * @param visible
	 *            Indicator if the pane should be visible.
	 */
	public final void showCommentPane(final boolean visible) {
		displayImageView.storePosition();
		commentPane.setVisible(visible);
		commentPane.setManaged(visible);
		if (commentConstraints instanceof ColumnConstraints) {
			((ColumnConstraints) commentConstraints).setPercentWidth(visible ? 20 : 0); // MAGIC_NUMBER
		}
		if (commentConstraints instanceof RowConstraints) {
			((RowConstraints) commentConstraints).setPercentHeight(visible ? 20 : 0); // MAGIC_NUMBER
		}
		displayImage.layout();
		displayImageView.retrievePosition();
	}

	/**
	 * Show or hide the overlay pane.
	 *
	 * @param visible
	 *            Indicator if the pane should be visible.
	 */
	public final void showOverlayPane(final boolean visible) {
		displayImageView.storePosition();
		overlayPane.setVisible(visible);
		overlayPane.setManaged(visible);
		if (overlayConstraints instanceof ColumnConstraints) {
			((ColumnConstraints) overlayConstraints).setMinWidth(visible ? 75 : 0); // MAGIC_NUMBER
		}
		displayImage.layout();
		displayImageView.retrievePosition();
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
		btnOverlay7.setDisable(!enabled);
		colorPicker.setDisable(!enabled);
	}

	/**
	 * Give information if the image is already loaded and the view is initialized.
	 *
	 * @return true if the image is loaded and the view is initialized.
	 */
	private boolean isInitialized() {
		return initialized && displayImageView.isInitialized();
	}

}
