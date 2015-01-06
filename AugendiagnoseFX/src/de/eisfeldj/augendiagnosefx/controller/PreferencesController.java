package de.eisfeldj.augendiagnosefx.controller;

import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_FOLDER_PHOTOS;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import de.eisfeldj.augendiagnosefx.Application;
import de.eisfeldj.augendiagnosefx.util.PreferenceUtil;

/**
 * Controller for the Preferences page.
 */
public class PreferencesController extends DialogController implements Initializable {
	/**
	 * The main pane.
	 */
	@FXML
	private GridPane settingsPane;

	/**
	 * The photos folder when starting the activity.
	 */
	private String oldPhotosFolder;

	/**
	 * Text field for the eye photos folder.
	 */
	@FXML
	private TextField textFolderPhotos;

	@Override
	public final Parent getRoot() {
		return settingsPane;
	}

	@Override
	public final void initialize(final URL location, final ResourceBundle resources) {
		oldPhotosFolder = PreferenceUtil.getPreferenceString(KEY_FOLDER_PHOTOS);
		textFolderPhotos.setText(oldPhotosFolder);
	}

	/**
	 * Action handler for cancel button.
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void cancel(final ActionEvent event) {
		close();
	}

	/**
	 * Action handler for submit button.
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void submit(final ActionEvent event) {
		String newPhotosFolder = textFolderPhotos.getText();
		if (newPhotosFolder != null && !newPhotosFolder.equals(oldPhotosFolder)) {
			PreferenceUtil.setPreference(KEY_FOLDER_PHOTOS, textFolderPhotos.getText());
			Application.refreshMainPage();
		}
		close();
	}

}
