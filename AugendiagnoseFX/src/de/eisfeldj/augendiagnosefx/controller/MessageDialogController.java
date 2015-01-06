package de.eisfeldj.augendiagnosefx.controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

/**
 * The controller of a dialog window.
 */
public class MessageDialogController extends DialogController {
	/**
	 * The root of the main page.
	 */
	@FXML
	private GridPane dialogPane;

	/**
	 * The heading of the Dialog box.
	 */
	@FXML
	private Label dialogHeading;

	/**
	 * The message of the dialog box.
	 */
	@FXML
	private Text dialogMessage;

	/**
	 * The "back" button on the dialog.
	 */
	@FXML
	private Button btnBack;

	/**
	 * The "ok" button on the dialog.
	 */
	@FXML
	private Button btnOk;

	/**
	 * The progress bar.
	 */
	@FXML
	private ProgressBar progress;

	/**
	 * The "cancel" button on the dialog.
	 */
	@FXML
	private Button btnCancel;

	@Override
	public final Parent getRoot() {
		return dialogPane;
	}

	/**
	 * Set the heading of the dialog.
	 *
	 * @param text
	 *            The heading text.
	 */
	public final void setHeading(final String text) {
		dialogHeading.setText(text);
	}

	/**
	 * Set the message of the dialog.
	 *
	 * @param text
	 *            The message text.
	 */
	public final void setMessage(final String text) {
		dialogMessage.setText(text);
	}

	/**
	 * Get the "back" button.
	 *
	 * @return The "back" button.
	 */
	public final Button getBtnBack() {
		return btnBack;
	}

	/**
	 * Get the "ok" button.
	 *
	 * @return The "ok" button.
	 */
	public final Button getBtnOk() {
		return btnOk;
	}

	/**
	 * Get the "cancel" button.
	 *
	 * @return The "cancel" button.
	 */
	public final Button getBtnCancel() {
		return btnCancel;
	}
}