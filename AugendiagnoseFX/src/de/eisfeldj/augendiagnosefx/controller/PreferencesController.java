package de.eisfeldj.augendiagnosefx.controller;

import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_FOLDER_PHOTOS;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_MAX_BITMAP_SIZE;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_OVERLAY_COLOR;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_THUMBNAIL_SIZE;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_SORT_BY_LAST_NAME;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_UPDATE_AUTOMATICALLY;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
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
	 * The "sort by last name" value when starting the activity.
	 */
	private boolean oldSortByLastName;

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

	/**
	 * Choice box for max bitmap size.
	 */
	@FXML
	private ChoiceBox<Integer> choiceThumbnailSize;

	/**
	 * Checkbox for "sort by last name" flag.
	 */
	@FXML
	private CheckBox checkBoxSortByLastName;

	/**
	 * Checkbox for "update automatically" flag.
	 */
	@FXML
	private CheckBox checkBoxUpdateAutomatically;

	/**
	 * Color picker for the default overlay color.
	 */
	@FXML
	private ColorPicker colorPicker;

	@Override
	public final Parent getRoot() {
		return settingsPane;
	}

	@Override
	public final void initialize(final URL location, final ResourceBundle resources) {
		oldPhotosFolder = PreferenceUtil.getPreferenceString(KEY_FOLDER_PHOTOS);
		textFolderPhotos.setText(oldPhotosFolder);

		choiceMaxBitmapSize.setValue(PreferenceUtil.getPreferenceInt(KEY_MAX_BITMAP_SIZE));
		choiceThumbnailSize.setValue(PreferenceUtil.getPreferenceInt(KEY_THUMBNAIL_SIZE));
		colorPicker.setValue(PreferenceUtil.getPreferenceColor(KEY_OVERLAY_COLOR));

		oldSortByLastName = PreferenceUtil.getPreferenceBoolean(KEY_SORT_BY_LAST_NAME);
		checkBoxSortByLastName.setSelected(oldSortByLastName);
		checkBoxUpdateAutomatically.setSelected(PreferenceUtil.getPreferenceBoolean(KEY_UPDATE_AUTOMATICALLY));
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
		// Check if main page needs to be refreshed, before updating values.
		boolean requireRefreshMainPage = requireRefreshMainPage(); // STORE_PROPERTY

		PreferenceUtil.setPreference(KEY_FOLDER_PHOTOS, textFolderPhotos.getText());
		PreferenceUtil.setPreference(KEY_MAX_BITMAP_SIZE, choiceMaxBitmapSize.getValue());
		PreferenceUtil.setPreference(KEY_THUMBNAIL_SIZE, choiceThumbnailSize.getValue());
		PreferenceUtil.setPreference(KEY_OVERLAY_COLOR, colorPicker.getValue());
		PreferenceUtil.setPreference(KEY_SORT_BY_LAST_NAME, checkBoxSortByLastName.isSelected());
		PreferenceUtil.setPreference(KEY_UPDATE_AUTOMATICALLY, checkBoxUpdateAutomatically.isSelected());

		if (requireRefreshMainPage) {
			Application.refreshMainPage();
		}

		close();
	}

	/**
	 * Check if the main page needs to be refreshed.
	 *
	 * @return true if the main page needs to be refreshed.
	 */
	private boolean requireRefreshMainPage() {
		String newPhotosFolder = textFolderPhotos.getText();
		boolean changedPhotosFolder = newPhotosFolder != null && !newPhotosFolder.equals(oldPhotosFolder);

		boolean newSortByLastName = checkBoxSortByLastName.isSelected();
		boolean changedSortByLastName = newSortByLastName != oldSortByLastName;

		return changedPhotosFolder || changedSortByLastName;
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
