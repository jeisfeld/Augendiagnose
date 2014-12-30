package de.eisfeldj.augendiagnosefx;

import javafx.scene.Parent;

/**
 * Generic controller interface.
 */
public interface Controller {
	/**
	 * Getter method for the root element managed by the controller.
	 *
	 * @return the root element.
	 */
	Parent getRoot();
}
