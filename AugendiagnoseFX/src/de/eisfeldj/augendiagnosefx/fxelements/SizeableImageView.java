package de.eisfeldj.augendiagnosefx.fxelements;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;

/**
 * Pane containing an image that can be resized.
 */
public class SizeableImageView extends ScrollPane {
	/**
	 * The zoom factor to be applied for each zoom event.
	 */
	private static final double ZOOM_FACTOR = 1.05;

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
	 * Constructor without initialization of image.
	 */
	public SizeableImageView() {
		this(new ImageView());
	}

	/**
	 * Constructor, initializing with an image view.
	 *
	 * @param imageView
	 *            The ImageView to be displayed.
	 */
	public SizeableImageView(final ImageView imageView) {
		setContent(imageView);

		setPannable(true);
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
				ImageView image = (ImageView) getContent();

				// Original size of the image.
				double sourceWidth = zoomProperty.get() * image.getImage().getWidth();
				double sourceHeight = zoomProperty.get() * image.getImage().getHeight();

				if (event.getDeltaY() > 0) {
					zoomProperty.set(zoomProperty.get() * ZOOM_FACTOR);
				}
				else if (event.getDeltaY() < 0) {
					zoomProperty.set(zoomProperty.get() / ZOOM_FACTOR);
				}

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
				double targetWidth = zoomProperty.get() * image.getImage().getWidth();
				double targetHeight = zoomProperty.get() * image.getImage().getHeight();

				// Image pixels outside the visible area which need to be scrolled.
				double postScrollXFactor = Math.max(0, targetWidth - getWidth());
				double postScrollYFactor = Math.max(0, targetHeight - getHeight());

				// Correction applied to compensate the vertical scrolling done by ScrollPane
				double verticalCorrection = preScrollYFactor / sourceHeight * event.getDeltaY();

				// New scrollbar positions keeping the mouse position.
				double newHvalue = ((mouseXPosition * targetWidth) - mouseXProperty.get()) / postScrollXFactor;
				double newVvalue = ((mouseYPosition * targetHeight) - mouseYProperty.get() + verticalCorrection) / postScrollYFactor;

				image.setFitWidth(targetWidth);
				image.setFitHeight(targetHeight);

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
	 * Set the image view displayed by this class.
	 *
	 * @param imageView
	 *            The ImageView.
	 */
	public final void setImageView(final ImageView imageView) {
		setContent(imageView);
		zoomProperty.set(Math.min(imageView.getFitWidth() / imageView.getImage().getWidth(), imageView.getFitHeight()
				/ imageView.getImage().getHeight()));
	}
}
