package de.eisfeldj.augendiagnosefx.controller;

import java.util.ArrayList;
import java.util.List;

import de.eisfeldj.augendiagnosefx.util.FXMLUtil;
import javafx.scene.Parent;

/**
 * Generic controller class.
 */
public abstract class Controller {
	/**
	 * The list of all controllers.
	 */
	private static List<Controller> controllerRegistry = new ArrayList<Controller>();

	/**
	 * Constructor of controllers. Adds the controller to the registry.
	 */
	public Controller() {
		controllerRegistry.add(this);
	}

	/**
	 * Getter method for the root element managed by the controller.
	 *
	 * @return the root element.
	 */
	public abstract Parent getRoot();

	/**
	 * Close a controller, removing it from the registry.
	 */
	// OVERRIDABLE
	public void close() {
		FXMLUtil.remove(this.getRoot());
		controllerRegistry.remove(this);
	}

}
