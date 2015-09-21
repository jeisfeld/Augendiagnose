package de.eisfeldj.augendiagnosefx.controller;

import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_OVERLAY_COLOR;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_SHOW_COMMENT_PANE;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_SHOW_OVERLAY_PANE;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.BUTTON_EDIT_COMMENT;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.BUTTON_SAVE_COMMENT;
import static de.eisfeldj.augendiagnosefx.util.imagefile.ImageUtil.Resolution.FULL;
import static de.eisfeldj.augendiagnosefx.util.imagefile.ImageUtil.Resolution.NORMAL;
import static de.eisfeldj.augendiagnosefx.util.imagefile.ImageUtil.Resolution.THUMB;

import java.net.URL;
import java.util.ResourceBundle;

import de.eisfeldj.augendiagnosefx.fxelements.OverlayImageView;
import de.eisfeldj.augendiagnosefx.fxelements.SizableImageView.MetadataPosition;
import de.eisfeldj.augendiagnosefx.util.FxmlConstants;
import de.eisfeldj.augendiagnosefx.util.FxmlUtil;
import de.eisfeldj.augendiagnosefx.util.PreferenceUtil;
import de.eisfeldj.augendiagnosefx.util.ResourceUtil;
import de.eisfeldj.augendiagnosefx.util.imagefile.EyePhoto;
import de.eisfeldj.augendiagnosefx.util.imagefile.ImageUtil.Resolution;
import de.eisfeldj.augendiagnosefx.util.imagefile.JpegMetadata;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
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

/**
 * BaseController for the "Display Image" page.
 */
public class DisplayImageController extends BaseController implements Initializable {
	/**
	 * The main pane holding the image.
	 */
	@FXML
	private GridPane mDisplayImage;

	/**
	 * The scroll pane holding the image.
	 */
	@FXML
	private OverlayImageView mDisplayImageView;

	/**
	 * The pane used for displaying and editing the comment.
	 */
	@FXML
	private Pane mCommentPane;

	/**
	 * The constraints of the comment pane.
	 */
	@FXML
	private ConstraintsBase mCommentConstraints;

	/**
	 * The pane used for toggling the overlay.
	 */
	@FXML
	private Pane mOverlayPane;

	/**
	 * The constraints of the overlay pane.
	 */
	@FXML
	private ConstraintsBase mOverlayConstraints;

	/**
	 * The text field for the image comment.
	 */
	@FXML
	private TextArea mTxtImageComment;

	/**
	 * The Button for editing/saving the image comment.
	 */
	@FXML
	private ToggleButton mBtnEditComment;

	/**
	 * The Button for adding the circle overlay.
	 */
	@FXML
	private ToggleButton mBtnOverlayCircle;

	/**
	 * The button for displaying the view in full resolution.
	 *
	 * <p>This is a ToggleButton, as it is incompatible with overlays.
	 */
	@FXML
	private ToggleButton mClarityButton;

	/**
	 * The button for displaying the image on full screen.
	 */
	@FXML
	private Button mFullScreenButton;

	/**
	 * The slider for brightness.
	 */
	@FXML
	private Slider mSliderBrightness;

	/**
	 * The slider for contrast.
	 */
	@FXML
	private Slider mSliderContrast;

	/**
	 * The Buttons for overlays.
	 */
	// JAVADOC:OFF
	@FXML
	private ToggleButton mBtnOverlay1;
	@FXML
	private ToggleButton mBtnOverlay2;
	@FXML
	private ToggleButton mBtnOverlay3;
	@FXML
	private ToggleButton mBtnOverlay4;
	@FXML
	private ToggleButton mBtnOverlay5;
	@FXML
	private ToggleButton mBtnOverlay6;
	@FXML
	private ToggleButton mBtnOverlay7;
	// JAVADOC:ON

	/**
	 * The Button for selecting the overlay color.
	 */
	@FXML
	private ColorPicker mColorPicker;

	/**
	 * The displayed eye photo.
	 */
	private EyePhoto mEyePhoto;

	/**
	 * Flag storing if the view is already initialized. (However, the image may be loaded later asynchronously.)
	 */
	private boolean mIsInitialized = false;

	/**
	 * Temporary storage for the comment while editing.
	 */
	private String mOldComment;

	/**
	 * Slider indicating if the current state displays only a thumbnail.
	 */
	private Resolution mCurrentResolution = NORMAL;

	/**
	 * Storage for the current overlay type.
	 */
	private Integer mCurrentOverlayType = null;

	/**
	 * Update the stored current resolution, redisplay if the resolution changed, and update the clarityButton if
	 * appicable.
	 *
	 * @param newResolution
	 *            The current resolution.
	 */
	private void updateResolution(final Resolution newResolution) {
		if (newResolution != mCurrentResolution) {
			mCurrentResolution = newResolution;
			if (newResolution != FULL) {
				mClarityButton.setSelected(false);
			}
			mDisplayImageView.redisplay(newResolution);
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
		mColorPicker.setValue(PreferenceUtil.getPreferenceColor(KEY_OVERLAY_COLOR));
		mColorPicker.getStyleClass().add("button");

		mSliderBrightness.setMin(-1);
		mSliderBrightness.setValue(0);
		mSliderBrightness.setMax(1);
		mSliderContrast.setMin(-1);
		mSliderContrast.setValue(0);
		mSliderContrast.setMax(1);

		mIsInitialized = true;
	}

	/**
	 * Initialize the sliders for contrast and brightness.
	 */
	private void initializeSliders() {
		mSliderBrightness.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
					final Number newValue) {
				mDisplayImageView.setBrightness(newValue.floatValue(), mCurrentResolution);
			}
		});
		mSliderBrightness.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				updateResolution(THUMB);
			}
		});
		mSliderBrightness.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				updateResolution(NORMAL);
			}
		});

		// Inititlize slider for contrast.
		mSliderContrast.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
					final Number newValue) {
				mDisplayImageView.setContrast(newValue.floatValue(), mCurrentResolution);
			}
		});
		mSliderContrast.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				updateResolution(THUMB);
			}
		});
		mSliderContrast.setOnMouseReleased(new EventHandler<MouseEvent>() {
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
		return mDisplayImage;
	}

	/**
	 * Action method for button "Edit Comment".
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void btnEditCommentPressed(final ActionEvent event) {
		if (mBtnEditComment.isSelected()) {
			// make comment editable
			mOldComment = mTxtImageComment.getText();
			mTxtImageComment.setEditable(true);
			mTxtImageComment.requestFocus();

			mBtnEditComment.setText(ResourceUtil.getString(BUTTON_SAVE_COMMENT));
			setDirty(true);
		}
		else {
			mTxtImageComment.setEditable(false);
			String newComment = mTxtImageComment.getText();

			// Save only if comment changed.
			if (!(newComment == null && mOldComment == null) && !(newComment != null && newComment.equals(mOldComment))) {
				JpegMetadata metadata = mEyePhoto.getImageMetadata();
				metadata.mComment = newComment;
				mEyePhoto.storeImageMetadata(metadata);
			}

			mBtnEditComment.setText(ResourceUtil.getString(BUTTON_EDIT_COMMENT));
			setDirty(false);
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
			case "mBtnOverlayCircle":
				overlayType = 0;
				break;
			default:
				String indexStr = btnId.substring("mBtnOverlay".length());
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
		if (mClarityButton.isSelected()) {
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
		showOverlay(mCurrentOverlayType);
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
			JpegMetadata metadata = mEyePhoto.getImageMetadata();
			if (metadata != null) {
				metadata.mBrightness = (float) mSliderBrightness.getValue();
				metadata.mContrast = OverlayImageView.seekbarContrastToStoredContrast((float) mSliderContrast.getValue());

				mEyePhoto.storeImageMetadata(metadata);
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
		mSliderBrightness.setValue(0);
		mSliderContrast.setValue(0);
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
			JpegMetadata metadata = mEyePhoto.getImageMetadata();
			if (metadata != null) {
				MetadataPosition position = mDisplayImageView.getPosition();
				metadata.mXPosition = position.mXCenter;
				metadata.mYPosition = position.mYCenter;
				metadata.mZoomFactor = position.mZoom;

				mEyePhoto.storeImageMetadata(metadata);
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
			JpegMetadata metadata = mEyePhoto.getImageMetadata();
			if (metadata != null) {
				metadata.mXPosition = null;
				metadata.mYPosition = null;
				metadata.mZoomFactor = null;

				mEyePhoto.storeImageMetadata(metadata);
				// re-set eyePhoto in order to do initial scaling again.
				mDisplayImageView.setEyePhoto(mEyePhoto);
			}
		}
	}

	/**
	 * Action method for displaying the image on the full window.
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void displayInFullWindow(final ActionEvent event) {
		DisplayImageFullController controller =
				(DisplayImageFullController) FxmlUtil.displaySubpage(FxmlConstants.FXML_DISPLAY_IMAGE_FULL,
						MainController.getInstance().isSplitPane() ? 2 : 0, true);

		if (isInitialized()) {
			JpegMetadata metadata = mEyePhoto.getImageMetadata();
			if (metadata != null) {
				MetadataPosition position = mDisplayImageView.getPosition();
				metadata.mXPosition = position.mXCenter;
				metadata.mYPosition = position.mYCenter;
				metadata.mZoomFactor = position.mZoom;

				controller.setImage(metadata, mDisplayImageView.getImageView().getImage());
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
		this.mEyePhoto = eyePhoto;
		JpegMetadata metadata = eyePhoto.getImageMetadata();

		if (metadata.hasBrightnessContrast()) {
			mSliderBrightness.setValue(metadata.mBrightness);
			mSliderContrast.setValue(OverlayImageView.storedContrastToSeekbarContrast(metadata.mContrast));
			mDisplayImageView.initializeBrightnessContrast(metadata.mBrightness, metadata.mContrast);
		}
		// Only now the listeners should be initialized, as image is not yet loaded and listeners should not
		// react on initial slider setup.
		initializeSliders();

		mDisplayImageView.setEyePhoto(eyePhoto);

		enableOverlayButtons(metadata.hasOverlayPosition());

		mTxtImageComment.setText(metadata.mComment);
	}

	/**
	 * Display a specific overlay on the eye photo.
	 *
	 * @param overlayType
	 *            The overlay type to be displayed.
	 */
	public final void showOverlay(final Integer overlayType) {
		mCurrentOverlayType = overlayType;
		if (overlayType != null) {
			mCurrentResolution = NORMAL;
		}
		mDisplayImageView.displayOverlay(overlayType, mColorPicker.getValue(), mCurrentResolution);
	}

	/**
	 * Show or hide the comment pane.
	 *
	 * @param visible
	 *            Indicator if the pane should be visible.
	 */
	public final void showCommentPane(final boolean visible) {
		mDisplayImageView.storePosition();
		mCommentPane.setVisible(visible);
		mCommentPane.setManaged(visible);
		if (mCommentConstraints instanceof ColumnConstraints) {
			((ColumnConstraints) mCommentConstraints).setPercentWidth(visible ? 20 : 0); // MAGIC_NUMBER
		}
		if (mCommentConstraints instanceof RowConstraints) {
			((RowConstraints) mCommentConstraints).setPercentHeight(visible ? 20 : 0); // MAGIC_NUMBER
		}
		mDisplayImage.layout();
		mDisplayImageView.retrievePosition();
	}

	/**
	 * Show or hide the overlay pane.
	 *
	 * @param visible
	 *            Indicator if the pane should be visible.
	 */
	public final void showOverlayPane(final boolean visible) {
		mDisplayImageView.storePosition();
		mOverlayPane.setVisible(visible);
		mOverlayPane.setManaged(visible);
		if (mOverlayConstraints instanceof ColumnConstraints) {
			((ColumnConstraints) mOverlayConstraints).setMinWidth(visible ? 75 : 0); // MAGIC_NUMBER
		}
		mDisplayImage.layout();
		mDisplayImageView.retrievePosition();
	}

	/**
	 * Enable or disable the overlay buttons.
	 *
	 * @param enabled
	 *            Indicator if the overlay buttons should be enabled.
	 */
	private void enableOverlayButtons(final boolean enabled) {
		mBtnOverlayCircle.setDisable(!enabled);
		mBtnOverlay1.setDisable(!enabled);
		mBtnOverlay2.setDisable(!enabled);
		mBtnOverlay3.setDisable(!enabled);
		mBtnOverlay4.setDisable(!enabled);
		mBtnOverlay5.setDisable(!enabled);
		mBtnOverlay6.setDisable(!enabled);
		mBtnOverlay7.setDisable(!enabled);
		mColorPicker.setDisable(!enabled);
	}

	/**
	 * Give information if the image is already loaded and the view is initialized.
	 *
	 * @return true if the image is loaded and the view is initialized.
	 */
	private boolean isInitialized() {
		return mIsInitialized && mDisplayImageView.isInitialized();
	}

}
