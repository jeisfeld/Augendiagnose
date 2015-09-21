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
	private GridPane mDialogPane;

	/**
	 * The heading of the Dialog box.
	 */
	@FXML
	private Label mDialogHeading;

	/**
	 * The message of the dialog box.
	 */
	@FXML
	private Text mDialogMessage;

	/**
	 * The "back" button on the dialog.
	 */
	@FXML
	private Button mBtnBack;

	/**
	 * The "ok" button on the dialog.
	 */
	@FXML
	private Button mBtnOk;

	/**
	 * The progressBar bar.
	 */
	@FXML
	private ProgressBar mProgressBar;

	/**
	 * The "cancel" button on the dialog.
	 */
	@FXML
	private Button mBtnCancel;

	@Override
	public final Parent getRoot() {
		return mDialogPane;
	}

	/**
	 * Set the heading of the dialog.
	 *
	 * @param text
	 *            The heading text.
	 */
	public final void setHeading(final String text) {
		mDialogHeading.setText(text);
	}

	/**
	 * Set the message of the dialog.
	 *
	 * @param text
	 *            The message text.
	 */
	public final void setMessage(final String text) {
		mDialogMessage.setText(text);
	}

	/**
	 * Get the "back" button.
	 *
	 * @return The "back" button.
	 */
	public final Button getBtnBack() {
		return mBtnBack;
	}

	/**
	 * Get the "ok" button.
	 *
	 * @return The "ok" button.
	 */
	public final Button getBtnOk() {
		return mBtnOk;
	}

	/**
	 * Get the "cancel" button.
	 *
	 * @return The "cancel" button.
	 */
	public final Button getBtnCancel() {
		return mBtnCancel;
	}

	/**
	 * Set the progressBar on the progressBar bar.
	 *
	 * @param progress
	 *            The progressBar value.
	 */
	public final void setProgress(final double progress) {
		this.mProgressBar.setProgress(progress);
	}
}
