package de.eisfeldj.augendiagnosefx.controller;

import java.util.ArrayList;
import java.util.List;

import de.eisfeldj.augendiagnosefx.util.FXMLUtil;

/**
 * Generic controller class.
 */
public abstract class BaseController implements Controller {
	/**
	 * The list of all controllers.
	 */
	private static List<BaseController> controllerRegistry = new ArrayList<BaseController>();

	/**
	 * Constructor of controllers. Adds the controller to the registry.
	 */
	public BaseController() {
		controllerRegistry.add(this);
	}

	/**
	 * Close a controller, removing it from the registry.
	 */
	// OVERRIDABLE
	public void close() {
		FXMLUtil.remove(this.getRoot());
		controllerRegistry.remove(this);
	}

	/**
	 * Get all controllers of a given type.
	 *
	 * @param <C>
	 *            the controller class.
	 * @param controllerClass
	 *            The type of controller.
	 * @return The list of controllers.
	 */
	@SuppressWarnings("unchecked")
	public static <C extends BaseController> List<C> getControllers(final Class<C> controllerClass) {
		List<C> result = new ArrayList<C>();

		for (BaseController controller : controllerRegistry) {
			if (controllerClass.isAssignableFrom(controller.getClass())) {
				result.add((C) controller);
			}
		}

		return result;
	}

	/**
	 * Get the controller of a given type.
	 *
	 * @param <C>
	 *            the controller class.
	 * @param controllerClass
	 *            The type of controller.
	 * @return The controller
	 * @throws TooManyControllersException
	 *             There is more than one such controller.
	 * @throws MissingControllerException
	 *             There is no such controller.
	 */
	public static <C extends BaseController> C getController(final Class<C> controllerClass)
			throws TooManyControllersException,
			MissingControllerException {
		List<C> controllers = getControllers(controllerClass);

		if (controllers.size() > 1) {
			throw new TooManyControllersException();
		}
		if (controllers.size() == 0) {
			throw new MissingControllerException();
		}
		return controllers.get(0);
	}

	/**
	 * Expected controller is missing.
	 */
	public static class MissingControllerException extends Exception {
		/**
		 * The serial version id.
		 */
		private static final long serialVersionUID = 1L;
	}

	/**
	 * There are two controllers of the same type, but there shouldn't.
	 */
	public static class TooManyControllersException extends Exception {
		/**
		 * The serial version id.
		 */
		private static final long serialVersionUID = 1L;
	}

}
