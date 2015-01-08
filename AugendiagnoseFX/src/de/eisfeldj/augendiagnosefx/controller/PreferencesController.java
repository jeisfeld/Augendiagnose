package de.eisfeldj.augendiagnosefx.controller;

import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_FOLDER_PHOTOS;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_MAX_BITMAP_SIZE;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import de.eisfeldj.augendiagnosefx.Application;
import de.eisfeldj.augendiagnosefx.util.Logger;
import de.eisfeldj.augendiagnosefx.util.PreferenceUtil;

/**
 * BaseController for the Preferences page.
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

	/**
	 * Choice box for max bitmap size.
	 */
	@FXML
	private ChoiceBox<Integer> choiceMaxBitmapSize;

	@Override
	public final Parent getRoot() {
		return settingsPane;
	}

	@Override
	public final void initialize(final URL location, final ResourceBundle resources) {
		oldPhotosFolder = PreferenceUtil.getPreferenceString(KEY_FOLDER_PHOTOS);
		textFolderPhotos.setText(oldPhotosFolder);

		choiceMaxBitmapSize.setValue(PreferenceUtil.getPreferenceInt(KEY_MAX_BITMAP_SIZE));
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
		PreferenceUtil.setPreference(KEY_MAX_BITMAP_SIZE, choiceMaxBitmapSize.getValue());
		close();
	}

	/**
	 * Action handler for select directory button.
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	public final void selectDirectory(final ActionEvent event) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setInitialDirectory(new File(PreferenceUtil.getPreferenceString(KEY_FOLDER_PHOTOS)));
		File selectedFolder = directoryChooser.showDialog(getStage());
		if (selectedFolder != null) {
			String selectedFolderString;
			try {
				selectedFolderString = selectedFolder.getCanonicalPath();
			}
			catch (IOException e) {
				Logger.warning("Could not get canonical path for " + selectedFolder.getAbsolutePath());
				selectedFolderString = selectedFolder.getAbsolutePath();
			}
			textFolderPhotos.setText(selectedFolderString);
		}
	}

}
