package de.eisfeldj.augendiagnosefx.controller;

import javafx.stage.Stage;

/**
 * Controller of a dialog with own stage.
 */
public abstract class DialogController extends Controller {
	/**
	 * The stage containing the dialog.
	 */
	private Stage stage;

	public final void setStage(final Stage stage) {
		this.stage = stage;
	}

	public final Stage getStage() {
		return stage;
	}

	@Override
	public final void close() {
		super.close();
		stage.close();
	}

	/**
	 * Show the dialog window.
	 */
	public final void show() {
		stage.show();
	}
}
