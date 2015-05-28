package de.eisfeldj.augendiagnosefx.fxelements;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.BorderPane;
import de.eisfeldj.augendiagnosefx.util.DialogUtil;
import de.eisfeldj.augendiagnosefx.util.DialogUtil.ProgressDialog;
import de.eisfeldj.augendiagnosefx.util.EyePhoto;
import de.eisfeldj.augendiagnosefx.util.ImageUtil.Resolution;
import de.eisfeldj.augendiagnosefx.util.JpegMetadata;
import de.eisfeldj.augendiagnosefx.util.ResourceConstants;

/**
 * Pane containing an image that can be resized.
 */
public class SizableImageView extends ScrollPane {
	/**
	 * The zoom factor to be applied for each zoom event.
	 *
	 * <p>(480th root of 2 means that 12 wheel turns of 40 will result in size factor 2.)
	 */
	private static final double ZOOM_FACTOR = 1.0014450997779993488675056142818;

	/**
	 * The x/y values representing the center of the image.
	 */
	private static final float IMAGE_CENTER = 0.5f;

	/**
	 * The zoom factor.
	 */
	private final DoubleProperty zoomProperty = new SimpleDoubleProperty(1000);

	/**
	 * The mouse X position.
	 */
	private final DoubleProperty mouseXProperty = new SimpleDoubleProperty();

	/**
	 * The mouse Y position.
	 */
	private final DoubleProperty mouseYProperty = new SimpleDoubleProperty();

	/**
	 * Flag indicating if the view is initialized (and image is loaded).
	 */
	private boolean initialized = false;

	public final boolean isInitialized() {
		return initialized;
	}

	/**
	 * The displayed ImageView.
	 */
	private ImageView imageView;

	protected final ImageView getImageView() {
		return imageView;
	}

	/**
	 * The displayed eye photo.
	 */
	private EyePhoto eyePhoto;

	protected final EyePhoto getEyePhoto() {
		return eyePhoto;
	}

	/**
	 * X Location of the view center on the image.
	 */
	private double centerX;

	/**
	 * Y Location of the view center on the image.
	 */
	private double centerY;

	/**
	 * Constructor without initialization of image.
	 */
	public SizableImageView() {
		imageView = new ImageView();
		imageView.setPreserveRatio(true);
		setContent(new BorderPane(imageView));

		setPannable(true);
		setFitToHeight(true);
		setFitToWidth(true);

		setHbarPolicy(ScrollBarPolicy.NEVER);
		setVbarPolicy(ScrollBarPolicy.NEVER);

		setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				mouseXProperty.set(event.getX());
				mouseYProperty.set(event.getY());
			}
		});

		addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
			@Override
			public void handle(final ScrollEvent event) {
				// Original size of the image.
				double sourceWidth = zoomProperty.get() * imageView.getImage().getWidth();
				double sourceHeight = zoomProperty.get() * imageView.getImage().getHeight();

				multiplyZoomProperty(Math.pow(ZOOM_FACTOR, event.getDeltaY()));

				// Old values of the scrollbars.
				double oldHvalue = getHvalue();
				double oldVvalue = getVvalue();

				// Image pixels outside the visible area which need to be scrolled.
				double preScrollXFactor = Math.max(0, sourceWidth - getWidth());
				double preScrollYFactor = Math.max(0, sourceHeight - getHeight());

				// Relative position of the mouse in the image.
				double mouseXPosition = (mouseXProperty.get() + preScrollXFactor * oldHvalue) / sourceWidth;
				double mouseYPosition = (mouseYProperty.get() + preScrollYFactor * oldVvalue) / sourceHeight;

				// Target size of the image.
				double targetWidth = zoomProperty.get() * imageView.getImage().getWidth();
				double targetHeight = zoomProperty.get() * imageView.getImage().getHeight();

				// Image pixels outside the visible area which need to be scrolled.
				double postScrollXFactor = Math.max(0, targetWidth - getWidth());
				double postScrollYFactor = Math.max(0, targetHeight - getHeight());

				// Correction applied to compensate the vertical scrolling done by ScrollPane
				double verticalCorrection = (postScrollYFactor / sourceHeight) * event.getDeltaY();

				// New scrollbar positions keeping the mouse position.
				double newHvalue = postScrollXFactor > 0 // STORE_PROPERTY
						? ((mouseXPosition * targetWidth) - mouseXProperty.get()) / postScrollXFactor
						: oldHvalue;
				double newVvalue = postScrollYFactor > 0 // STORE_PROPERTY
						? ((mouseYPosition * targetHeight) - mouseYProperty.get() + verticalCorrection)
								/ postScrollYFactor
						: oldVvalue;

				imageView.setFitWidth(targetWidth);
				imageView.setFitHeight(targetHeight);
				// Layout needs to be done now so that default scrollbar position is applied.
				layout();
				setHvalue(newHvalue);
				setVvalue(newVvalue);
			}
		});

		addEventFilter(ZoomEvent.ANY, new EventHandler<ZoomEvent>() {
			@Override
			public void handle(final ZoomEvent event) {
				multiplyZoomProperty(event.getZoomFactor());

				ImageView image = (ImageView) getContent();
				image.setFitWidth(zoomProperty.get() * image.getImage().getWidth());
				image.setFitHeight(zoomProperty.get() * image.getImage().getHeight());
			}
		});
	}

	/**
	 * Set the eye photo displayed by this class.
	 *
	 * @param eyePhoto
	 *            The eye photo.
	 */
	public final void setEyePhoto(final EyePhoto eyePhoto) {
		initialized = false;
		this.eyePhoto = eyePhoto;

		Image image = eyePhoto.getImage(Resolution.NORMAL);

		if (image.getProgress() == 1) {
			// image is already loaded from the start.
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					displayImage(image);
				}
			});
			return;
		}
		else {
			ProgressDialog dialog =
					DialogUtil
							.displayProgressDialog(ResourceConstants.MESSAGE_PROGRESS_LOADING_PHOTO,
									eyePhoto.getFilename());

			image.progressProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
						final Number newValue) {
					dialog.setProgress(newValue.doubleValue());

					if (newValue.doubleValue() == 1) {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								displayImage(image);
								dialog.close();
							}
						});
					}
				}
			});
		}

		// Size the image only after this pane is sized
		heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
					final Number newValue) {
				synchronized (imageView) {
					// Initialization after window is sized and image is loaded.
					if (imageView.getImage() != null && !initialized) {
						doInitialScaling();
					}
				}
				heightProperty().removeListener(this);
			}
		});
	}

	/**
	 * Display the image after it is loaded.
	 *
	 * @param image
	 *            The fully loaded image.
	 */
	// OVERRIDABLE
	protected void displayImage(final Image image) {
		imageView.setImage(image);
		synchronized (imageView) {
			// Initialization after window is sized and image is loaded.
			if (getHeight() > 0 && !initialized) {
				doInitialScaling();
			}
		}
	}

	/**
	 * Do the initial scaling of the image.
	 */
	private void doInitialScaling() {
		JpegMetadata metadata = eyePhoto.getImageMetadata();
		if (metadata != null && metadata.hasViewPosition()) {
			zoomProperty.set(getDefaultScaleFactor() * metadata.zoomFactor);
		}
		else if (metadata != null && metadata.hasOverlayPosition()) {
			zoomProperty.set(Math.min(getWidth(), getHeight())
					/ Math.max(imageView.getImage().getWidth(), imageView.getImage().getHeight())
					/ metadata.overlayScaleFactor);
		}
		else {
			zoomProperty.set(getDefaultScaleFactor());
		}

		imageView.setFitWidth(zoomProperty.get() * imageView.getImage().getWidth());
		imageView.setFitHeight(zoomProperty.get() * imageView.getImage().getHeight());
		layout();

		if (metadata != null && (metadata.hasViewPosition() || metadata.hasOverlayPosition())) {
			float xCenter;
			float yCenter;

			if (metadata.hasViewPosition()) {
				xCenter = metadata.xPosition;
				yCenter = metadata.yPosition;
			}
			else {
				xCenter = metadata.xCenter;
				yCenter = metadata.yCenter;
			}

			ScrollPosition scrollPosition =
					convertMetadataPositionToScrollPosition(new MetadataPosition(xCenter, yCenter));

			setHvalue(scrollPosition.hValue);
			setVvalue(scrollPosition.vValue);
		}

		initialized = true;
	}

	/**
	 * Store image position for later retrieval. Can be used to keep view center if the view size changes.
	 */
	public final void storePosition() {
		if (imageView == null || imageView.getImage() == null) {
			return;
		}
		// Size of the image.
		double imageWidth = zoomProperty.get() * imageView.getImage().getWidth();
		double imageHeight = zoomProperty.get() * imageView.getImage().getHeight();
		// Image pixels outside the visible area which need to be scrolled.
		double scrollXFactor = Math.max(0, imageWidth - getWidth());
		double scrollYFactor = Math.max(0, imageHeight - getHeight());

		// Calculate position of pane center in the image
		centerX = scrollXFactor > 0
				? (getWidth() / 2 + scrollXFactor * getHvalue()) / imageWidth
				: 0.5; // MAGIC_NUMBER
		centerY = scrollYFactor > 0
				? (getHeight() / 2 + scrollYFactor * getVvalue()) / imageHeight
				: 0.5; // MAGIC_NUMBER
	}

	/**
	 * Retrieve image position from the position stored with storePosition().
	 */
	public final void retrievePosition() {
		if (imageView == null || imageView.getImage() == null) {
			return;
		}
		// Size of the image.
		double imageWidth = zoomProperty.get() * imageView.getImage().getWidth();
		double imageHeight = zoomProperty.get() * imageView.getImage().getHeight();
		// Image pixels outside the visible area which need to be scrolled.
		double scrollXFactor = Math.max(0, imageWidth - getWidth());
		double scrollYFactor = Math.max(0, imageHeight - getHeight());

		// Move scroll position to put center back.
		if (scrollXFactor > 0) {
			setHvalue((centerX * imageWidth - getWidth() / 2) / scrollXFactor);
		}
		if (scrollYFactor > 0) {
			setVvalue((centerY * imageHeight - getHeight() / 2) / scrollYFactor);
		}
	}

	/**
	 * Multiply the zoom property by the given factor.
	 *
	 * @param factor
	 *            The factor.
	 */
	protected final void multiplyZoomProperty(final double factor) {
		zoomProperty.set(zoomProperty.get() * factor);
	}

	/**
	 * Retrieve the default scale factor of the image.
	 *
	 * @return The default scale factor that fits the image into the view.
	 */
	private double getDefaultScaleFactor() {
		return Math.min(getWidth() / imageView.getImage().getWidth(),
				getHeight() / imageView.getImage().getHeight());
	}

	/**
	 * Helper method to retrieve the position of the image within the view.
	 *
	 * @return the position within the image
	 */
	public final MetadataPosition getPosition() {
		MetadataPosition metadataPosition =
				convertScrollPositionToMetadataPosition(new ScrollPosition(getHvalue(), getVvalue()));

		metadataPosition.zoom = (float) (zoomProperty.get() / getDefaultScaleFactor());

		return metadataPosition;
	}

	/**
	 * Convert coordinates like stored in metadata to coordinates like used in the ScrollPane.
	 *
	 * @param metadataPosition
	 *            The coordinates like stored in metadata.
	 * @return The coordinates like used in ScrollPane.
	 */
	private ScrollPosition convertMetadataPositionToScrollPosition(final MetadataPosition metadataPosition) {
		// Size of the image.
		double imageWidth = zoomProperty.get() * imageView.getImage().getWidth();
		double imageHeight = zoomProperty.get() * imageView.getImage().getHeight();

		// Image pixels outside the visible area which need to be scrolled.
		double scrollXFactor = Math.max(0, imageWidth - getWidth());
		double scrollYFactor = Math.max(0, imageHeight - getHeight());

		double hValue = scrollXFactor > 0
				? (metadataPosition.xCenter * imageWidth - getWidth() / 2) / scrollXFactor
				: 1;
		double vValue = scrollYFactor > 0
				? (metadataPosition.yCenter * imageHeight - getHeight() / 2) / scrollYFactor
				: 1;

		return new ScrollPosition(hValue, vValue);
	}

	/**
	 * Convert coordinates like used in the ScrollPane to coordinates like stored in metadataused in the ScrollPane.
	 *
	 * @param scrollPosition
	 *            The coordinates like used in ScrollPane.
	 * @return The coordinates like stored in metadata.
	 */
	private MetadataPosition convertScrollPositionToMetadataPosition(final ScrollPosition scrollPosition) {
		// Size of the image.
		double imageWidth = zoomProperty.get() * imageView.getImage().getWidth();
		double imageHeight = zoomProperty.get() * imageView.getImage().getHeight();

		// Image pixels outside the visible area which need to be scrolled.
		double scrollXFactor = Math.max(0, imageWidth - getWidth());
		double scrollYFactor = Math.max(0, imageHeight - getHeight());

		double xCenter = scrollXFactor > 0
				? (scrollXFactor * scrollPosition.hValue + getWidth() / 2) / imageWidth
				: IMAGE_CENTER;
		double yCenter = scrollYFactor > 0
				? (scrollYFactor * scrollPosition.vValue + getHeight() / 2) / imageHeight
				: IMAGE_CENTER;

		return new MetadataPosition((float) xCenter, (float) yCenter);
	}

	/**
	 * Class holding zoom and position as stored in the metadata.
	 */
	public static class MetadataPosition {
		// PUBLIC_FIELDS:START
		// JAVADOC:OFF
		public float xCenter;
		public float yCenter;
		public float zoom;

		// JAVADOC:ON
		// PUBLIC_FIELDS:END

		/**
		 * Initialize a MetadataPosition with coordinate values.
		 *
		 * @param xCenter
		 *            The x position of the center
		 * @param yCenter
		 *            The y position of the center
		 */
		public MetadataPosition(final float xCenter, final float yCenter) {
			this.xCenter = xCenter;
			this.yCenter = yCenter;
		}

	}

	/**
	 * Class holding zoom and position as used in the scrollPane.
	 */
	public static class ScrollPosition {
		// PUBLIC_FIELDS:START
		// JAVADOC:OFF
		public double hValue;
		public double vValue;

		// JAVADOC:ON
		// PUBLIC_FIELDS:END

		/**
		 * Initialize a ScrollPosition with scrollbar values.
		 *
		 * @param hValue
		 *            The horizontal value
		 * @param vValue
		 *            The vertical value
		 */
		public ScrollPosition(final double hValue, final double vValue) {
			this.hValue = hValue;
			this.vValue = vValue;
		}
	}

}
