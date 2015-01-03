package de.eisfeldj.augendiagnosefx.fxelements;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

/**
 * A custom pane holding an image view.
 */
public class ImageViewPane extends Region {
	/**
	 * The contained ImageView, stored in a property.
	 */
	private ObjectProperty<ImageView> imageViewProperty = new SimpleObjectProperty<ImageView>();

	/**
	 * Getter for the contained ImageView property.
	 *
	 * @return The imageView property.
	 */
	public final ObjectProperty<ImageView> imageViewProperty() {
		return imageViewProperty;
	}

	/**
	 * Get the ImageView of the pane.
	 *
	 * @return The ImageView.
	 */
	public final ImageView getImageView() {
		return imageViewProperty.get();
	}

	/**
	 * Set the imageView of the pane.
	 *
	 * @param imageView
	 *            The imageView to be set.
	 */
	public final void setImageView(final ImageView imageView) {
		this.imageViewProperty.set(imageView);
	}

	/**
	 * Creating an ImageViewPane without imageView.
	 */
	public ImageViewPane() {
		this(new ImageView());
	}

	@Override
	protected final void layoutChildren() {
		ImageView imageView = imageViewProperty.get();
		if (imageView != null) {
			imageView.setFitWidth(getWidth());
			imageView.setFitHeight(getHeight());
			layoutInArea(imageView, 0, 0, getWidth(), getHeight(), 0, HPos.CENTER, VPos.CENTER);
		}
		super.layoutChildren();
	}

	/**
	 * Create an ImageViewPane containing an imageView.
	 *
	 * @param imageView
	 *            The imageView contained in the pane.
	 */
	public ImageViewPane(final ImageView imageView) {
		imageViewProperty.addListener(new ChangeListener<ImageView>() {
			@Override
			public void changed(final ObservableValue<? extends ImageView> arg0, final ImageView oldIV,
					final ImageView newIV) {
				if (oldIV != null) {
					getChildren().remove(oldIV);
				}
				if (newIV != null) {
					getChildren().add(newIV);
				}
			}
		});
		this.imageViewProperty.set(imageView);
	}
}