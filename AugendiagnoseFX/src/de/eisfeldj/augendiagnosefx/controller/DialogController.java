package de.eisfeldj.augendiagnosefx.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

/**
 * The controller of a dialog window.
 */
public class DialogController implements Controller {
	/**
	 * The root of the main page.
	 */
	@FXML
	private GridPane dialogPane;

	/**
	 * The heading of the Dialog box.
	 */
	@FXML
	private Label heading;

	/**
	 * The message of the dialog box.
	 */
	@FXML
	private Text message;

	/**
	 * The "back" button on the dialog.
	 */
	@FXML
	private Button btnBack;

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
		heading.setText(text);
	}

	/**
	 * Set the message of the dialog.
	 *
	 * @param text
	 *            The message text.
	 */
	public final void setMessage(final String text) {
		message.setText(text);
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
	 * Handler for button "Organize new photos".
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	protected final void onButtonBack(final ActionEvent event) {

	}

}