package de.eisfeldj.augendiagnosefx.fxelements;

import de.eisfeldj.augendiagnosefx.util.DialogUtil;
import de.eisfeldj.augendiagnosefx.util.DialogUtil.ProgressDialog;
import de.eisfeldj.augendiagnosefx.util.ResourceConstants;
import de.eisfeldj.augendiagnosefx.util.imagefile.EyePhoto;
import de.eisfeldj.augendiagnosefx.util.imagefile.ImageUtil.Resolution;
import de.eisfeldj.augendiagnosefx.util.imagefile.JpegMetadata;
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
	private final DoubleProperty mZoomProperty = new SimpleDoubleProperty(1000);

	/**
	 * The mouse X position.
	 */
	private final DoubleProperty mMouseXProperty = new SimpleDoubleProperty();

	/**
	 * The mouse Y position.
	 */
	private final DoubleProperty mMouseYProperty = new SimpleDoubleProperty();

	/**
	 * Flag indicating if the view is initialized (and image is loaded).
	 */
	private boolean mIsInitialized = false;

	public final boolean isInitialized() {
		return mIsInitialized;
	}

	/**
	 * The displayed ImageView.
	 */
	private ImageView mImageView;

	public final ImageView getImageView() {
		return mImageView;
	}

	/**
	 * The displayed eye photo.
	 */
	private EyePhoto mEyePhoto;

	protected final EyePhoto getEyePhoto() {
		return mEyePhoto;
	}

	/**
	 * X Location of the view center on the image.
	 */
	private double mCenterX;

	/**
	 * Y Location of the view center on the image.
	 */
	private double mCenterY;

	/**
	 * Constructor without initialization of image.
	 */
	public SizableImageView() {
		mImageView = new ImageView();
		mImageView.setPreserveRatio(true);
		setContent(new BorderPane(mImageView));

		setPannable(true);
		setFitToHeight(true);
		setFitToWidth(true);

		setHbarPolicy(ScrollBarPolicy.NEVER);
		setVbarPolicy(ScrollBarPolicy.NEVER);

		setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				mMouseXProperty.set(event.getX());
				mMouseYProperty.set(event.getY());
			}
		});

		addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
			@Override
			public void handle(final ScrollEvent event) {
				// Original size of the image.
				double sourceWidth = mZoomProperty.get() * mImageView.getImage().getWidth();
				double sourceHeight = mZoomProperty.get() * mImageView.getImage().getHeight();

				multiplyZoomProperty(Math.pow(ZOOM_FACTOR, event.getDeltaY()));

				// Old values of the scrollbars.
				double oldHvalue = getHvalue();
				double oldVvalue = getVvalue();

				// Image pixels outside the visible area which need to be scrolled.
				double preScrollXFactor = Math.max(0, sourceWidth - getWidth());
				double preScrollYFactor = Math.max(0, sourceHeight - getHeight());

				// Relative position of the mouse in the image.
				double mouseXPosition = (mMouseXProperty.get() + preScrollXFactor * oldHvalue) / sourceWidth;
				double mouseYPosition = (mMouseYProperty.get() + preScrollYFactor * oldVvalue) / sourceHeight;

				// Target size of the image.
				double targetWidth = mZoomProperty.get() * mImageView.getImage().getWidth();
				double targetHeight = mZoomProperty.get() * mImageView.getImage().getHeight();

				// Image pixels outside the visible area which need to be scrolled.
				double postScrollXFactor = Math.max(0, targetWidth - getWidth());
				double postScrollYFactor = Math.max(0, targetHeight - getHeight());

				// Correction applied to compensate the vertical scrolling done by ScrollPane
				double verticalCorrection = (postScrollYFactor / sourceHeight) * event.getDeltaY();

				// New scrollbar positions keeping the mouse position.
				double newHvalue = postScrollXFactor > 0 // STORE_PROPERTY
						? ((mouseXPosition * targetWidth) - mMouseXProperty.get()) / postScrollXFactor
						: oldHvalue;
				double newVvalue = postScrollYFactor > 0 // STORE_PROPERTY
						? ((mouseYPosition * targetHeight) - mMouseYProperty.get() + verticalCorrection)
								/ postScrollYFactor
						: oldVvalue;

				mImageView.setFitWidth(targetWidth);
				mImageView.setFitHeight(targetHeight);
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
				image.setFitWidth(mZoomProperty.get() * image.getImage().getWidth());
				image.setFitHeight(mZoomProperty.get() * image.getImage().getHeight());
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
		mIsInitialized = false;
		this.mEyePhoto = eyePhoto;

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
				synchronized (mImageView) {
					// Initialization after window is sized and image is loaded.
					if (mImageView.getImage() != null && !mIsInitialized) {
						doInitialScaling(eyePhoto.getImageMetadata());
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
		mImageView.setImage(image);
		synchronized (mImageView) {
			// Initialization after window is sized and image is loaded.
			if (getHeight() > 0 && !mIsInitialized) {
				doInitialScaling(mEyePhoto.getImageMetadata());
			}
		}
	}

	/**
	 * Display a pre-loaded image generated from an eye photo.
	 *
	 * @param metadata
	 *            The metadata to be used for scaling.
	 * @param image
	 *            The pre-loaded image.
	 */
	public final void setImage(final JpegMetadata metadata, final Image image) {
		mImageView.setImage(image);
		synchronized (mImageView) {
			// Initialization after window is sized and image is loaded.
			if (getHeight() > 0 && !mIsInitialized) {
				doInitialScaling(metadata);
			}
		}

		// Size the image only after this pane is sized
		heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
					final Number newValue) {
				synchronized (mImageView) {
					// Initialization after window is sized and image is loaded.
					if (mImageView.getImage() != null && !mIsInitialized) {
						synchronized (mImageView) {
							// Initialization after window is sized and image is loaded.
							if (getHeight() > 0 && !mIsInitialized) {
								doInitialScaling(metadata);
							}
						}
					}
				}
				heightProperty().removeListener(this);
			}
		});

	}

	/**
	 * Do the initial scaling of the image.
	 *
	 * @param metadata
	 *            The metadata by which to do the scaling.
	 */
	private void doInitialScaling(final JpegMetadata metadata) {
		if (metadata != null && metadata.hasViewPosition()) {
			mZoomProperty.set(getDefaultScaleFactor() * metadata.getZoomFactor());
		}
		else if (metadata != null && metadata.hasOverlayPosition()) {
			mZoomProperty.set(Math.min(getWidth(), getHeight())
					/ Math.max(mImageView.getImage().getWidth(), mImageView.getImage().getHeight())
					/ metadata.getOverlayScaleFactor());
		}
		else {
			mZoomProperty.set(getDefaultScaleFactor());
		}

		mImageView.setFitWidth(mZoomProperty.get() * mImageView.getImage().getWidth());
		mImageView.setFitHeight(mZoomProperty.get() * mImageView.getImage().getHeight());
		layout();

		if (metadata != null && (metadata.hasViewPosition() || metadata.hasOverlayPosition())) {
			float xCenter;
			float yCenter;

			if (metadata.hasViewPosition()) {
				xCenter = metadata.getXPosition();
				yCenter = metadata.getYPosition();
			}
			else {
				xCenter = metadata.getXCenter();
				yCenter = metadata.getYCenter();
			}

			ScrollPosition scrollPosition =
					convertMetadataPositionToScrollPosition(new MetadataPosition(xCenter, yCenter));

			setHvalue(scrollPosition.mHValue);
			setVvalue(scrollPosition.mVValue);
		}

		mIsInitialized = true;
	}

	/**
	 * Store image position for later retrieval. Can be used to keep view center if the view size changes.
	 */
	public final void storePosition() {
		if (mImageView == null || mImageView.getImage() == null) {
			return;
		}
		// Size of the image.
		double imageWidth = mZoomProperty.get() * mImageView.getImage().getWidth();
		double imageHeight = mZoomProperty.get() * mImageView.getImage().getHeight();
		// Image pixels outside the visible area which need to be scrolled.
		double scrollXFactor = Math.max(0, imageWidth - getWidth());
		double scrollYFactor = Math.max(0, imageHeight - getHeight());

		// Calculate position of pane center in the image
		mCenterX = scrollXFactor > 0
				? (getWidth() / 2 + scrollXFactor * getHvalue()) / imageWidth
				: 0.5; // MAGIC_NUMBER
		mCenterY = scrollYFactor > 0
				? (getHeight() / 2 + scrollYFactor * getVvalue()) / imageHeight
				: 0.5; // MAGIC_NUMBER
	}

	/**
	 * Retrieve image position from the position stored with storePosition().
	 */
	public final void retrievePosition() {
		if (mImageView == null || mImageView.getImage() == null) {
			return;
		}
		// Size of the image.
		double imageWidth = mZoomProperty.get() * mImageView.getImage().getWidth();
		double imageHeight = mZoomProperty.get() * mImageView.getImage().getHeight();
		// Image pixels outside the visible area which need to be scrolled.
		double scrollXFactor = Math.max(0, imageWidth - getWidth());
		double scrollYFactor = Math.max(0, imageHeight - getHeight());

		// Move scroll position to put center back.
		if (scrollXFactor > 0) {
			setHvalue((mCenterX * imageWidth - getWidth() / 2) / scrollXFactor);
		}
		if (scrollYFactor > 0) {
			setVvalue((mCenterY * imageHeight - getHeight() / 2) / scrollYFactor);
		}
	}

	/**
	 * Multiply the zoom property by the given factor.
	 *
	 * @param factor
	 *            The factor.
	 */
	protected final void multiplyZoomProperty(final double factor) {
		mZoomProperty.set(mZoomProperty.get() * factor);
	}

	/**
	 * Retrieve the default scale factor of the image.
	 *
	 * @return The default scale factor that fits the image into the view.
	 */
	private double getDefaultScaleFactor() {
		return Math.min(getWidth() / mImageView.getImage().getWidth(),
				getHeight() / mImageView.getImage().getHeight());
	}

	/**
	 * Helper method to retrieve the position of the image within the view.
	 *
	 * @return the position within the image
	 */
	public final MetadataPosition getPosition() {
		MetadataPosition metadataPosition =
				convertScrollPositionToMetadataPosition(new ScrollPosition(getHvalue(), getVvalue()));

		metadataPosition.mZoom = (float) (mZoomProperty.get() / getDefaultScaleFactor());

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
		double imageWidth = mZoomProperty.get() * mImageView.getImage().getWidth();
		double imageHeight = mZoomProperty.get() * mImageView.getImage().getHeight();

		// Image pixels outside the visible area which need to be scrolled.
		double scrollXFactor = Math.max(0, imageWidth - getWidth());
		double scrollYFactor = Math.max(0, imageHeight - getHeight());

		double hValue = scrollXFactor > 0
				? (metadataPosition.mXCenter * imageWidth - getWidth() / 2) / scrollXFactor
				: 1;
		double vValue = scrollYFactor > 0
				? (metadataPosition.mYCenter * imageHeight - getHeight() / 2) / scrollYFactor
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
		double imageWidth = mZoomProperty.get() * mImageView.getImage().getWidth();
		double imageHeight = mZoomProperty.get() * mImageView.getImage().getHeight();

		// Image pixels outside the visible area which need to be scrolled.
		double scrollXFactor = Math.max(0, imageWidth - getWidth());
		double scrollYFactor = Math.max(0, imageHeight - getHeight());

		double xCenter = scrollXFactor > 0
				? (scrollXFactor * scrollPosition.mHValue + getWidth() / 2) / imageWidth
				: IMAGE_CENTER;
		double yCenter = scrollYFactor > 0
				? (scrollYFactor * scrollPosition.mVValue + getHeight() / 2) / imageHeight
				: IMAGE_CENTER;

		return new MetadataPosition((float) xCenter, (float) yCenter);
	}

	/**
	 * Class holding zoom and position as stored in the metadata.
	 */
	public static class MetadataPosition {
		// PUBLIC_FIELDS:START
		// JAVADOC:OFF
		public float mXCenter;
		public float mYCenter;
		public float mZoom;

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
			this.mXCenter = xCenter;
			this.mYCenter = yCenter;
		}

	}

	/**
	 * Class holding zoom and position as used in the scrollPane.
	 */
	public static class ScrollPosition {
		// PUBLIC_FIELDS:START
		// JAVADOC:OFF
		public double mHValue;
		public double mVValue;

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
			this.mHValue = hValue;
			this.mVValue = vValue;
		}
	}

}
