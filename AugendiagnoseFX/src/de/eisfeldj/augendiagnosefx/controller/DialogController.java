package de.eisfeldj.augendiagnosefx.controller;

import javafx.stage.Stage;

/**
 * BaseController of a dialog with own stage.
 */
public abstract class DialogController extends BaseController {
	/**
	 * The stage containing the dialog.
	 */
	private Stage mStage;

	/**
	 * Set the stage.
	 *
	 * @param stage The stage.
	 */
	public final void setStage(final Stage stage) {
		this.mStage = stage;
	}

	/**
	 * Get the stage.
	 *
	 * @return The stage.
	 */
	public final Stage getStage() {
		return mStage;
	}

	@Override
	public final void close() {
		super.close();
		mStage.close();
	}

	/**
	 * Show the dialog window.
	 */
	public final void show() {
		mStage.show();
	}
}
