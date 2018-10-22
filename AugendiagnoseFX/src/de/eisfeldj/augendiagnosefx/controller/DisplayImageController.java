package de.eisfeldj.augendiagnosefx.controller;

import java.net.URL;
import java.util.ResourceBundle;

import de.eisfeldj.augendiagnosefx.fxelements.OverlayImageView;
import de.eisfeldj.augendiagnosefx.fxelements.SizableImageView.MetadataPosition;
import de.eisfeldj.augendiagnosefx.util.DateUtil;
import de.eisfeldj.augendiagnosefx.util.FxmlConstants;
import de.eisfeldj.augendiagnosefx.util.FxmlUtil;
import de.eisfeldj.augendiagnosefx.util.PreferenceUtil;
import de.eisfeldj.augendiagnosefx.util.ResourceConstants;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.ConstraintsBase;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;

import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_INDEXED_OVERLAY_TYPE;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_OVERLAY_COLOR;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_SHOW_COMMENT_PANE;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_SHOW_OVERLAY_PANE;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.BUTTON_EDIT_COMMENT;
import static de.eisfeldj.augendiagnosefx.util.ResourceConstants.BUTTON_SAVE_COMMENT;
import static de.eisfeldj.augendiagnosefx.util.imagefile.ImageUtil.Resolution.FULL;
import static de.eisfeldj.augendiagnosefx.util.imagefile.ImageUtil.Resolution.NORMAL;
import static de.eisfeldj.augendiagnosefx.util.imagefile.ImageUtil.Resolution.THUMB;

/**
 * BaseController for the "Display Image" page.
 */
public class DisplayImageController extends BaseController implements Initializable {
	/**
	 * The number of available overlay buttons (excluding circle). Must be at most OVERLAY_COUNT.
	 */
	public static final int OVERLAY_BUTTON_COUNT = 9;

	/**
	 * The number of available overlays (excluding circle).
	 */
	public static final int OVERLAY_COUNT = 9;

	/**
	 * The names of the overlays.
	 */
	private static final String[] OVERLAY_NAMES = new String[OVERLAY_COUNT];

	/**
	 * The button texts of the overlays.
	 */
	private static final String[] OVERLAY_BUTTON_STRINGS = new String[OVERLAY_BUTTON_COUNT];

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
	 * The label showing the image name.
	 */
	@FXML
	private Label mNameLabel;

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
	 * The slider for saturation.
	 */
	@FXML
	private Slider mSliderSaturation;

	/**
	 * The slider for color temperature.
	 */
	@FXML
	private Slider mSliderColorTemperature;

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
	@FXML
	private ToggleButton mBtnOverlay8;
	@FXML
	private ToggleButton mBtnOverlay9;

	private ToggleButton[] mOverlayButtons;

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

	static {
		for (int i = 0; i < OVERLAY_COUNT; i++) {
			OVERLAY_NAMES[i] = ResourceUtil.getString("overlay_" + (i + 1) + "_name");
		}
		for (int i = 0; i < OVERLAY_BUTTON_COUNT; i++) {
			OVERLAY_BUTTON_STRINGS[i] = ResourceUtil.getString("button_overlay_" + (i + 1));
		}
	}

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
			MenuController.getInstance().setCommentPaneMenuEnablement(true);
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
		mSliderSaturation.setMin(-1);
		mSliderSaturation.setValue(0);
		mSliderSaturation.setMax(1);
		mSliderColorTemperature.setMin(-1);
		mSliderColorTemperature.setValue(0);
		mSliderColorTemperature.setMax(1);

		mOverlayButtons = new ToggleButton[] {mBtnOverlayCircle, mBtnOverlay1, mBtnOverlay2, mBtnOverlay3, mBtnOverlay4, mBtnOverlay5, mBtnOverlay6,
				mBtnOverlay7, mBtnOverlay8, mBtnOverlay9};
		createOverlayButtonContextMenus();

		mIsInitialized = true;
	}

	/**
	 * Create the context menus of the overlay buttons.
	 */
	private void createOverlayButtonContextMenus() {
		for (int i = 1; i < mOverlayButtons.length; i++) {
			mOverlayButtons[i].setContextMenu(createOverlayButtonContextMenu(i, false));
		}
	}

	/**
	 * Create the context menu of an overlay button.
	 *
	 * @param position
	 *            The position of the button.
	 * @param forNewButton
	 *            Flag indicating the popup is displayed for a new button. Then it is enforced to select a new overlay.
	 * @return the context menu.
	 */
	private ContextMenu createOverlayButtonContextMenu(final int position, final boolean forNewButton) {
		ToggleButton button = mOverlayButtons[position];
		ContextMenu menu = new ContextMenu();

		if (position == getHighestOverlayButtonIndex()) {
			if (position > 1) {
				MenuItem menuItemRemove = new MenuItem();
				menuItemRemove.setText(ResourceUtil.getString(ResourceConstants.MENU_REMOVE_OVERLAY_BUTTON));

				menuItemRemove.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(final ActionEvent event) {
						mOverlayButtons[position].setSelected(false);
						handleBtnOverlayPressed(mOverlayButtons[position]);
						PreferenceUtil.setIndexedPreference(KEY_INDEXED_OVERLAY_TYPE, position, -1);
						mOverlayButtons[position].setVisible(false);
						mOverlayButtons[position].setManaged(false);
						createOverlayButtonContextMenus();
					}
				});
				menu.getItems().add(menuItemRemove);
			}

			if (position < OVERLAY_BUTTON_COUNT) {
				MenuItem menuItemAdd = new MenuItem();
				menuItemAdd.setText(ResourceUtil.getString(ResourceConstants.MENU_ADD_OVERLAY_BUTTON));

				menuItemAdd.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(final ActionEvent event) {
						ContextMenu contextMenu = createOverlayButtonContextMenu(position + 1, true);
						contextMenu.show(button, menu.getAnchorX(), menu.getAnchorY());
					}
				});
				menu.getItems().add(menuItemAdd);
			}
			menu.getItems().add(new SeparatorMenuItem());
		}

		MenuItem[] secondaryMenuItems = new MenuItem[OVERLAY_BUTTON_COUNT];

		for (int i = 1; i <= OVERLAY_COUNT; i++) {
			final int index = i;

			final Integer oldButtonPosition = getButtonForOverlayWithIndex(i);
			MenuItem menuItem = null;
			if (oldButtonPosition == null) {
				menuItem = new MenuItem();
				menuItem.setText(OVERLAY_NAMES[i - 1]);
				menu.getItems().add(menuItem);
			}
			else {
				if (!forNewButton) {
					menuItem = new MenuItem();
					menuItem.setText(OVERLAY_BUTTON_STRINGS[oldButtonPosition - 1] + " " + OVERLAY_NAMES[i - 1]);
					if (oldButtonPosition == position) {
						menuItem.getStyleClass().add("bold");
					}
					secondaryMenuItems[oldButtonPosition - 1] = menuItem;
				}
			}

			if (menuItem != null) {
				menuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(final ActionEvent event) {
						if (oldButtonPosition != null && oldButtonPosition != position) {
							// If the same overlay is already used, switch overlays
							int currentOverlay = PreferenceUtil.getIndexedPreferenceInt(KEY_INDEXED_OVERLAY_TYPE, position, -1);
							PreferenceUtil.setIndexedPreference(KEY_INDEXED_OVERLAY_TYPE, oldButtonPosition, currentOverlay);
						}
						PreferenceUtil.setIndexedPreference(KEY_INDEXED_OVERLAY_TYPE, position, index);
						mOverlayButtons[position].setSelected(true);
						mOverlayButtons[position].setVisible(true);
						mOverlayButtons[position].setManaged(true);
						handleBtnOverlayPressed(mOverlayButtons[position]);
						createOverlayButtonContextMenus();
					}
				});
			}
		}

		for (MenuItem menuItem : secondaryMenuItems) {
			if (menuItem != null) {
				menu.getItems().add(menuItem);
			}
		}

		return menu;
	}

	/**
	 * Get the index of the highest active overlay button.
	 *
	 * @return The index of the highest active overlay button.
	 */
	public static int getHighestOverlayButtonIndex() {
		int maxIndex = -1;
		for (int i = 0; i <= OVERLAY_BUTTON_COUNT; i++) {
			if (PreferenceUtil.getIndexedPreferenceInt(KEY_INDEXED_OVERLAY_TYPE, i, -1) >= 0) {
				maxIndex = i;
			}
		}
		return maxIndex;
	}

	/**
	 * Check if there is already a button configured for a certain overlay type.
	 *
	 * @param index
	 *            The index of the overlay type.
	 * @return The button index configured for this overlay type.
	 */
	private static Integer getButtonForOverlayWithIndex(final int index) {
		if (index < 0) {
			return null;
		}
		for (int i = 0; i <= OVERLAY_BUTTON_COUNT; i++) {
			if (PreferenceUtil.getIndexedPreferenceInt(KEY_INDEXED_OVERLAY_TYPE, i, -1) == index) {
				return i;
			}
		}
		return null;
	}

	/**
	 * Initialize the sliders for contrast, brightness, saturation and color temperature.
	 */
	private void initializeSliders() {
		// Initialize slider for brightness.
		mSliderBrightness.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
					final Number newValue) {
				mDisplayImageView.setColorSettings(newValue.floatValue(), null, null, null, mCurrentResolution);
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

		// Inititalize slider for contrast.
		mSliderContrast.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
					final Number newValue) {
				mDisplayImageView.setColorSettings(null, newValue.floatValue(), null, null, mCurrentResolution);
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

		// Initialize slider for saturation.
		mSliderSaturation.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
					final Number newValue) {
				mDisplayImageView.setColorSettings(null, null, newValue.floatValue(), null, mCurrentResolution);
			}
		});
		mSliderSaturation.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				updateResolution(THUMB);
			}
		});
		mSliderSaturation.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				updateResolution(NORMAL);
			}
		});

		// Inititalize slider for color temperature.
		mSliderColorTemperature.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
					final Number newValue) {
				mDisplayImageView.setColorSettings(null, null, null, newValue.floatValue(), mCurrentResolution);
			}
		});
		mSliderColorTemperature.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				updateResolution(THUMB);
			}
		});
		mSliderColorTemperature.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				updateResolution(NORMAL);
			}
		});

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
				metadata.setComment(newComment);
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
		handleBtnOverlayPressed((ToggleButton) event.getSource());
	}

	/**
	 * Perform the display of overlay after toggleButton is pressed.
	 *
	 * @param button
	 *            The button.
	 */
	private void handleBtnOverlayPressed(final ToggleButton button) {
		if (button.isSelected()) {
			String btnId = button.getId();
			int buttonPosition;

			switch (btnId) {
			case "mBtnOverlayCircle":
				buttonPosition = 0;
				break;
			default:
				String indexStr = btnId.substring("mBtnOverlay".length());
				buttonPosition = Integer.parseInt(indexStr);
			}

			int overlayType = PreferenceUtil.getIndexedPreferenceInt(KEY_INDEXED_OVERLAY_TYPE, buttonPosition, -1);

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
	public final void storeColorSettings(final ActionEvent event) {
		if (isInitialized()) {
			JpegMetadata metadata = mEyePhoto.getImageMetadata();
			if (metadata != null) {
				metadata.setBrightness((float) mSliderBrightness.getValue());
				metadata.setContrast(OverlayImageView.seekbarContrastToStoredContrast((float) mSliderContrast.getValue()));
				metadata.setSaturation(OverlayImageView.seekbarSaturationToStoredSaturation((float) mSliderSaturation.getValue()));
				metadata.setColorTemperature((float) mSliderColorTemperature.getValue());

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
	public final void resetColorSettings(final ActionEvent event) {
		mSliderBrightness.setValue(0);
		mSliderContrast.setValue(0);
		mSliderSaturation.setValue(0);
		mSliderColorTemperature.setValue(0);
		storeColorSettings(event);
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
				metadata.setXPosition(position.mXCenter);
				metadata.setYPosition(position.mYCenter);
				metadata.setZoomFactor(position.mZoom);

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
				metadata.setXPosition((Float) null);
				metadata.setYPosition((Float) null);
				metadata.setZoomFactor((Float) null);

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
				(DisplayImageFullController) FxmlUtil.displaySubpage(FxmlConstants.FXML_DISPLAY_IMAGE_FULL, -1, true);

		if (isInitialized()) {
			JpegMetadata metadata = mEyePhoto.getImageMetadata();
			if (metadata != null) {
				MetadataPosition position = mDisplayImageView.getPosition();
				metadata.setXPosition(position.mXCenter);
				metadata.setYPosition(position.mYCenter);
				metadata.setZoomFactor(position.mZoom);

				controller.setImage(metadata, mDisplayImageView.getImageView().getImage());
			}
		}

	}

	/**
	 * Action method for displaying the original saturation and color temperature of the image.
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void showOriginalSaturation(final MouseEvent event) {
		mDisplayImageView.setColorSettings(null, null, 0f, 0f, mCurrentResolution);
	}

	/**
	 * Action method for displaying the original colors of the image, disabling the slidebar values.
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void showOriginalColors(final MouseEvent event) {
		mDisplayImageView.setColorSettings(0f, 0f, 0f, 0f, mCurrentResolution);
	}

	/**
	 * Action method for re-applying the slidebar settings of the image.
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void showSlidebarColors(final MouseEvent event) {
		mDisplayImageView.setColorSettings((float) mSliderBrightness.getValue(), (float) mSliderContrast.getValue(),
				(float) mSliderSaturation.getValue(), (float) mSliderColorTemperature.getValue(), mCurrentResolution);
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
			mSliderBrightness.setValue(metadata.getBrightness());
			mSliderContrast.setValue(OverlayImageView.storedContrastToSeekbarContrast(metadata.getContrast()));
			mSliderSaturation.setValue(OverlayImageView.storedSaturationToSeekbarSaturation(
					metadata.getSaturation() == null ? 1f : metadata.getSaturation()));
			mSliderColorTemperature.setValue(metadata.getColorTemperature() == null ? 0f : metadata.getColorTemperature());

			mDisplayImageView.initializeColorSettings(metadata.getBrightness(), metadata.getContrast(),
					metadata.getSaturation(), metadata.getColorTemperature());
		}
		// Only now the listeners should be initialized, as image is not yet loaded and listeners should not
		// react on initial slider setup.
		initializeSliders();

		mDisplayImageView.setEyePhoto(eyePhoto);

		enableOverlayButtons(metadata.hasOverlayPosition());

		mNameLabel.setText(eyePhoto.getPersonName() + " - " + DateUtil.format(eyePhoto.getDate()) + " - " + eyePhoto.getRightLeft().getTitleSuffix());

		mTxtImageComment.setText(metadata.getComment());
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
		for (int i = 0; i < mOverlayButtons.length; i++) {
			mOverlayButtons[i].setDisable(!enabled);
			boolean isVisible = PreferenceUtil.getIndexedPreferenceInt(KEY_INDEXED_OVERLAY_TYPE, i, -1) >= 0;
			mOverlayButtons[i].setVisible(isVisible);
			mOverlayButtons[i].setManaged(isVisible);
		}
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

	/**
	 * Clone the contents from another instance.
	 *
	 * @param controller The other instance.
	 */
	protected void cloneContents(final DisplayImageController controller) {
		this.mEyePhoto = controller.mEyePhoto;
		JpegMetadata metadata = controller.mEyePhoto.getImageMetadata();
		if (metadata != null) {
			MetadataPosition position = controller.mDisplayImageView.getPosition();
			metadata.setXPosition(position.mXCenter);
			metadata.setYPosition(position.mYCenter);
			metadata.setZoomFactor(position.mZoom);
			enableOverlayButtons(metadata.hasOverlayPosition());
		}

		mDisplayImageView.cloneContents(controller.mDisplayImageView);

		mSliderBrightness.setValue(controller.mSliderBrightness.getValue());
		mSliderContrast.setValue(controller.mSliderContrast.getValue());
		mSliderSaturation.setValue(controller.mSliderSaturation.getValue());
		mSliderColorTemperature.setValue(controller.mSliderColorTemperature.getValue());

		mColorPicker.setValue(controller.mColorPicker.getValue());
		for (int i = 0; i < Math.min(mOverlayButtons.length, controller.mOverlayButtons.length); i++) {
			mOverlayButtons[i].setSelected(controller.mOverlayButtons[i].isSelected());
		}
		mCurrentOverlayType = controller.mCurrentOverlayType;
		mCurrentResolution = controller.mCurrentResolution;
		updateResolution(THUMB);

		mTxtImageComment.setText(controller.mTxtImageComment.getText());
		mTxtImageComment.setEditable(controller.mTxtImageComment.isEditable());
		mNameLabel.setText(controller.mNameLabel.getText());
		mBtnEditComment.setSelected(controller.mBtnEditComment.isSelected());
		mClarityButton.setSelected(controller.mClarityButton.isSelected());
		setDirty(controller.isDirty());

		mDisplayImageView.setImage(metadata, controller.mDisplayImageView.getImageView().getImage());
		initializeSliders();
	}

}
