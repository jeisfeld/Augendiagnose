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
import de.eisfeldj.augendiagnosefx.util.JpegMetadata;
import de.eisfeldj.augendiagnosefx.util.ResourceConstants;

/**
 * Pane containing an image that can be resized.
 */
public class SizableImageView extends ScrollPane {
	/**
	 * The zoom factor to be applied for each zoom event.
	 *
	 * (480th root of 2 means that 12 wheel turns of 40 will result in size factor 2.)
	 */
	private static final double ZOOM_FACTOR = 1.0014450997779993488675056142818;

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
	 * Flag indicating if the view is already initialized (and image is scaled).
	 */
	private boolean isInitialized = false;

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

				zoomProperty.set(zoomProperty.get() * Math.pow(ZOOM_FACTOR, event.getDeltaY()));

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
				double newHvalue = postScrollXFactor > 0
						? ((mouseXPosition * targetWidth) - mouseXProperty.get()) / postScrollXFactor
						: oldHvalue;
				double newVvalue = postScrollYFactor > 0
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
				zoomProperty.set(zoomProperty.get() * event.getZoomFactor());

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
		this.eyePhoto = eyePhoto;

		ProgressDialog dialog =
				DialogUtil
						.displayProgressDialog(ResourceConstants.MESSAGE_DIALOG_LOADING_PHOTO, eyePhoto.getFilename());

		Thread thread = new Thread() {
			@Override
			public void run() {
				Image image = eyePhoto.getImage(false);

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						imageView.setImage(image);
						synchronized (imageView) {
							// Initialization after window is sized and image is loaded.
							if (getHeight() > 0 && !isInitialized) {
								doInitialScaling();
							}
						}
						dialog.close();
					}
				});
			}
		};
		thread.start();

		// Size the image only after this pane is sized
		heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
					final Number newValue) {
				synchronized (imageView) {
					// Initialization after window is sized and image is loaded.
					if (imageView.getImage() != null && !isInitialized) {
						doInitialScaling();
					}
				}
				heightProperty().removeListener(this);
			}
		});
	}

	/**
	 * Do the initial scaling of the image.
	 */
	private void doInitialScaling() {
		JpegMetadata metadata = eyePhoto.getImageMetadata();
		if (metadata != null && metadata.hasOverlayPosition()) {
			zoomProperty.set(Math.min(getWidth(), getHeight())
					/ Math.max(imageView.getImage().getWidth(), imageView.getImage().getHeight())
					/ metadata.overlayScaleFactor);
		}
		else {
			zoomProperty.set(Math.min(getWidth() / imageView.getImage().getWidth(),
					getHeight() / imageView.getImage().getHeight()));
		}

		imageView.setFitWidth(zoomProperty.get() * imageView.getImage().getWidth());
		imageView.setFitHeight(zoomProperty.get() * imageView.getImage().getHeight());
		layout();

		if (metadata != null && metadata.hasOverlayPosition()) {
			// Size of the image.
			double imageWidth = zoomProperty.get() * imageView.getImage().getWidth();
			double imageHeight = zoomProperty.get() * imageView.getImage().getHeight();

			// Image pixels outside the visible area which need to be scrolled.
			double scrollXFactor = Math.max(0, imageWidth - getWidth());
			double scrollYFactor = Math.max(0, imageHeight - getHeight());

			// The initial scrollbar positions
			double hValue = scrollXFactor > 0
					? (metadata.xCenter * imageWidth - getWidth() / 2) / scrollXFactor
					: 1;
			double vValue = scrollYFactor > 0
					? (metadata.yCenter * imageHeight - getHeight() / 2) / scrollYFactor
					: 1;

			setHvalue(hValue);
			setVvalue(vValue);
		}
		isInitialized = true;
	}

	/**
	 * Store image position for later retrieval.
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
				? getWidth() / 2 + scrollXFactor * getHvalue()
				: imageWidth / 2;
		centerY = scrollYFactor > 0
				? getHeight() / 2 + scrollYFactor * getVvalue()
				: imageHeight / 2;
	}

	/**
	 * Retrieve image position.
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
			setHvalue((centerX - getWidth() / 2) / scrollXFactor);
		}
		if (scrollYFactor > 0) {
			setVvalue((centerY - getHeight() / 2) / scrollYFactor);
		}
	}

}
