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
 * A custom pane holding an image view, so that the image size adapts to the pane size.
 */
public class ImageViewPane extends Region {
	/**
	 * The contained ImageView, stored in a property.
	 */
	private ObjectProperty<ImageView> mImageViewProperty = new SimpleObjectProperty<>();

	/**
	 * Getter for the contained ImageView property.
	 *
	 * @return The imageView property.
	 */
	public final ObjectProperty<ImageView> imageViewProperty() {
		return mImageViewProperty;
	}

	/**
	 * Get the ImageView of the pane.
	 *
	 * @return The ImageView.
	 */
	public final ImageView getImageView() {
		return mImageViewProperty.get();
	}

	/**
	 * Set the imageView of the pane.
	 *
	 * @param imageView
	 *            The imageView to be set.
	 */
	public final void setImageView(final ImageView imageView) {
		this.mImageViewProperty.set(imageView);
	}

	/**
	 * Creating an ImageViewPane without imageView.
	 */
	public ImageViewPane() {
		mImageViewProperty.addListener(new ChangeListener<ImageView>() {
			@Override
			public void changed(final ObservableValue<? extends ImageView> arg0, final ImageView oldImageView,
					final ImageView newImageView) {

				if (oldImageView != null) {
					getChildren().remove(oldImageView);
				}
				if (newImageView != null) {
					getChildren().add(newImageView);
				}
			}
		});
	}

	@Override
	protected final void layoutChildren() {
		ImageView imageView = mImageViewProperty.get();
		if (imageView != null) {
			imageView.setFitWidth(getWidth());
			double newHeight = getWidth() * imageView.getImage().getHeight() / imageView.getImage().getWidth();
			imageView.setFitHeight(newHeight);
			layoutInArea(imageView, 0, 0, getWidth(), newHeight, 0, HPos.CENTER, VPos.CENTER);
		}
		super.layoutChildren();
	}

}
