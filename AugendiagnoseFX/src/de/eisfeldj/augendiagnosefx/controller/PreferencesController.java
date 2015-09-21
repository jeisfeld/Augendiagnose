package de.eisfeldj.augendiagnosefx.controller;

import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_FOLDER_PHOTOS;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_LANGUAGE;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_MAX_BITMAP_SIZE;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_OVERLAY_COLOR;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_SORT_BY_LAST_NAME;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_THUMBNAIL_SIZE;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_UPDATE_AUTOMATICALLY;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
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
import de.eisfeldj.augendiagnosefx.util.ResourceConstants;
import de.eisfeldj.augendiagnosefx.util.ResourceUtil;
import de.eisfeldj.augendiagnosefx.util.SystemUtil;

/**
 * BaseController for the Preferences page.
 */
public class PreferencesController extends DialogController implements Initializable {
	/**
	 * A map of language ids to language Strings.
	 */
	public static final Map<Integer, String> LANGUAGE_MAP = new HashMap<Integer, String>();

	/**
	 * A map of language Strings to language ids.
	 */
	private static final Map<String, Integer> LANGUAGE_MAP_BACK = new HashMap<String, Integer>();

	/**
	 * The main pane.
	 */
	@FXML
	private GridPane mSettingsPane;

	/**
	 * The photos folder when starting the activity.
	 */
	private String mOldPhotosFolder;

	/**
	 * The "sort by last name" value when starting the activity.
	 */
	private boolean mOldSortByLastName;

	/**
	 * The "language" value when starting the activity.
	 */
	private int mOldLanguage;

	/**
	 * Text field for the eye photos folder.
	 */
	@FXML
	private TextField mTextFolderPhotos;

	/**
	 * Choice box for max bitmap size.
	 */
	@FXML
	private ChoiceBox<Integer> mChoiceMaxBitmapSize;

	/**
	 * Choice box for max bitmap size.
	 */
	@FXML
	private ChoiceBox<Integer> mChoiceThumbnailSize;

	/**
	 * Checkbox for "sort by last name" flag.
	 */
	@FXML
	private CheckBox mCheckBoxSortByLastName;

	/**
	 * Checkbox for "update automatically" flag.
	 */
	@FXML
	private CheckBox mCheckBoxUpdateAutomatically;

	/**
	 * Color picker for the default overlay color.
	 */
	@FXML
	private ColorPicker mColorPicker;

	/**
	 * Choice box for language.
	 */
	@FXML
	private ChoiceBox<String> mChoiceLanguage;

	@Override
	public final Parent getRoot() {
		return mSettingsPane;
	}

	@Override
	public final void initialize(final URL location, final ResourceBundle resources) {
		LANGUAGE_MAP.put(0, ResourceUtil.getString(ResourceConstants.PREF_VALUE_LANGUAGE_DEFAULT));
		LANGUAGE_MAP.put(1, "English");
		LANGUAGE_MAP.put(2, "Deutsch");
		LANGUAGE_MAP.put(3, "Español"); // MAGIC_NUMBER

		LANGUAGE_MAP.forEach((key, value) -> LANGUAGE_MAP_BACK.put(value, key));

		mOldPhotosFolder = PreferenceUtil.getPreferenceString(KEY_FOLDER_PHOTOS);
		mTextFolderPhotos.setText(mOldPhotosFolder);

		mChoiceMaxBitmapSize.setValue(PreferenceUtil.getPreferenceInt(KEY_MAX_BITMAP_SIZE));
		mChoiceThumbnailSize.setValue(PreferenceUtil.getPreferenceInt(KEY_THUMBNAIL_SIZE));
		mColorPicker.setValue(PreferenceUtil.getPreferenceColor(KEY_OVERLAY_COLOR));

		mOldSortByLastName = PreferenceUtil.getPreferenceBoolean(KEY_SORT_BY_LAST_NAME);
		mCheckBoxSortByLastName.setSelected(mOldSortByLastName);
		mCheckBoxUpdateAutomatically.setSelected(PreferenceUtil.getPreferenceBoolean(KEY_UPDATE_AUTOMATICALLY));

		// Fill language choice box from LANGUAGE_MAP
		LANGUAGE_MAP.forEach((key, value) -> mChoiceLanguage.getItems().add(key, value));
		mOldLanguage = PreferenceUtil.getPreferenceInt(KEY_LANGUAGE);
		mChoiceLanguage.setValue(languageIdToString(mOldLanguage));
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
		PreferenceUtil.setPreference(KEY_FOLDER_PHOTOS, mTextFolderPhotos.getText());
		PreferenceUtil.setPreference(KEY_MAX_BITMAP_SIZE, mChoiceMaxBitmapSize.getValue());
		PreferenceUtil.setPreference(KEY_THUMBNAIL_SIZE, mChoiceThumbnailSize.getValue());
		PreferenceUtil.setPreference(KEY_OVERLAY_COLOR, mColorPicker.getValue());
		PreferenceUtil.setPreference(KEY_SORT_BY_LAST_NAME, mCheckBoxSortByLastName.isSelected());
		PreferenceUtil.setPreference(KEY_UPDATE_AUTOMATICALLY, mCheckBoxUpdateAutomatically.isSelected());
		PreferenceUtil.setPreference(KEY_LANGUAGE, languageStringToId(mChoiceLanguage.getValue()));

		if (requiresRestartApplication()) {
			SystemUtil.restartApplication();
			Application.exitAfterConfirmation();
		}
		if (requireRefreshMainPage()) {
			Application.refreshMainPage();
			close();
		}
		else {
			close();
		}
	}

	/**
	 * Check if the application needs to be restarted.
	 *
	 * @return true if the application needs to be restarted.
	 */
	private boolean requiresRestartApplication() {
		return mOldLanguage != PreferenceUtil.getPreferenceInt(KEY_LANGUAGE);
	}

	/**
	 * Check if the main page needs to be refreshed.
	 *
	 * @return true if the main page needs to be refreshed.
	 */
	private boolean requireRefreshMainPage() {
		String newPhotosFolder = PreferenceUtil.getPreferenceString(KEY_FOLDER_PHOTOS);
		boolean changedPhotosFolder = newPhotosFolder != null && !newPhotosFolder.equals(mOldPhotosFolder);

		boolean newSortByLastName = PreferenceUtil.getPreferenceBoolean(KEY_SORT_BY_LAST_NAME);
		boolean changedSortByLastName = newSortByLastName != mOldSortByLastName;

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
			mTextFolderPhotos.setText(selectedFolderString);
		}
	}

	/**
	 * Determine the language string from the language id.
	 *
	 * @param id
	 *            The language id
	 * @return The language string
	 */
	public static String languageIdToString(final int id) {
		return LANGUAGE_MAP.get(id);
	}

	/**
	 * Determine the language id from the language string.
	 *
	 * @param s
	 *            The language string
	 * @return The language id
	 */
	public static int languageStringToId(final String s) {
		return LANGUAGE_MAP_BACK.get(s);
	}

}
