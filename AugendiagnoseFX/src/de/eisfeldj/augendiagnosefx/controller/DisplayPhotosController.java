package de.eisfeldj.augendiagnosefx.controller;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.URL;
import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import de.eisfeldj.augendiagnosefx.fxelements.EyePhotoPairNode;
import de.eisfeldj.augendiagnosefx.util.DialogUtil;
import de.eisfeldj.augendiagnosefx.util.DialogUtil.ConfirmDialogListener;
import de.eisfeldj.augendiagnosefx.util.DialogUtil.ProgressDialog;
import de.eisfeldj.augendiagnosefx.util.Logger;
import de.eisfeldj.augendiagnosefx.util.PreferenceUtil;
import de.eisfeldj.augendiagnosefx.util.ResourceConstants;
import de.eisfeldj.augendiagnosefx.util.ResourceUtil;
import de.eisfeldj.augendiagnosefx.util.imagefile.EyePhoto;
import de.eisfeldj.augendiagnosefx.util.imagefile.EyePhotoPair;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_FOLDER_PHOTOS;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_LAST_NAME;

/**
 * BaseController for the "Display Photos" page.
 */
public class DisplayPhotosController extends BaseController implements Initializable {
	/**
	 * The previous selected name.
	 */
	private String mPreviousName;

	/**
	 * The full "Display Photos" pane.
	 */
	@FXML
	private Pane mDisplayMain;

	/**
	 * The list of names.
	 */
	@FXML
	private ListView<String> mListNames;

	/**
	 * The list of names.
	 */
	@FXML
	private ListView<GridPane> mListPhotos;

	/**
	 * The field for searching names.
	 */
	@FXML
	private TextField mSearchField;

	@Override
	public final void initialize(final URL location, final ResourceBundle resources) {
		initializeNames("", true);
	}

	/**
	 * Initialize the list of names with the search string.
	 *
	 * @param searchString
	 *            A search string for the names.
	 * @param loadPhotos
	 *            indicator if photos from the preselected name should be loaded.
	 */
	private void initializeNames(final String searchString, final boolean loadPhotos) {
		List<String> valuesNames =
				getFolderNames(new File(PreferenceUtil.getPreferenceString(KEY_FOLDER_PHOTOS)), searchString);
		mListNames.setItems(FXCollections.observableList(valuesNames));

		String lastName = PreferenceUtil.getPreferenceString(KEY_LAST_NAME);
		if (lastName == null && valuesNames.size() > 0) {
			lastName = valuesNames.get(0);
		}
		if (lastName != null && valuesNames.contains(lastName)) {
			if (loadPhotos) {
				showPicturesForName(lastName);
			}
			int selectedIndex = valuesNames.indexOf(lastName);
			mListNames.getSelectionModel().select(selectedIndex);
			mListNames.scrollTo(selectedIndex);
		}
	}

	@Override
	public final Parent getRoot() {
		return mDisplayMain;
	}

	/**
	 * Handler for Click on name on list. Displays the eye photo pairs for that name.
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	private void handleNameClick(final MouseEvent event) {
		String name = mListNames.getSelectionModel().getSelectedItem();
		if (event.getButton() == MouseButton.SECONDARY) {
			createContextMenu(name).show(getRoot(), event.getScreenX(), event.getScreenY());
		}
		else {
			if (name != null && !name.equals(mPreviousName)) {
				showPicturesForName(name);
			}
			PreferenceUtil.setPreference(KEY_LAST_NAME, name);
		}
	}

	/**
	 * Handler for change of search text. Filters displayed eye photo pairs.
	 *
	 * @param event
	 *            The action event.
	 */
	@FXML
	private void handleSearchText(final KeyEvent event) {
		// Need to be in main thread to ensure that text field is already updated
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				initializeNames(mSearchField.getText(), false);
			}
		});
	}

	/**
	 * Create the context menu when clicking on a name.
	 *
	 * @param name The name.
	 * @return The context menu.
	 */
	private ContextMenu createContextMenu(final String name) {
		ContextMenu menu = new ContextMenu();

		MenuItem menuItemRemove = new MenuItem();
		menuItemRemove.setText(ResourceUtil.getString(ResourceConstants.MENU_DELETE_IMAGES));

		menuItemRemove.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				DialogUtil.displayConfirmationMessage(new ConfirmDialogListener() {

					@Override
					public void onDialogPositiveClick() {
						File folder = new File(new File(PreferenceUtil.getPreferenceString(KEY_FOLDER_PHOTOS)), name);
						File[] children = folder.listFiles();
						if (children != null) {
							for (File child : children) {
								child.delete();
							}
						}
						folder.delete();

						if (name.equals(PreferenceUtil.getPreferenceString(KEY_LAST_NAME))) {
							PreferenceUtil.removePreference(KEY_LAST_NAME);
						}
						initializeNames("", true);
					}

					@Override
					public void onDialogNegativeClick() {
						// do nothing
					}
				}, ResourceConstants.BUTTON_DELETE,
						ResourceConstants.MESSAGE_DIALOG_CONFIRM_DELETE_FOLDER, name);
			}
		});
		menu.getItems().add(menuItemRemove);

		return menu;
	}

	/**
	 * Display the pictures for a given name.
	 *
	 * @param name
	 *            The name.
	 */
	private void showPicturesForName(final String name) {
		File nameFolder = new File(PreferenceUtil.getPreferenceString(KEY_FOLDER_PHOTOS), name);

		ProgressDialog dialog =
				DialogUtil.displayProgressDialog(ResourceConstants.MESSAGE_PROGRESS_LOADING_PHOTOS, name);

		EyePhotoPair[] eyePhotos = createEyePhotoList(nameFolder);

		ObservableList<GridPane> valuesPhotos = FXCollections.observableList(new ArrayList<GridPane>());

		for (int i = 0; i < eyePhotos.length; i++) {
			EyePhotoPairNode eyePhotoPairNode = new EyePhotoPairNode(eyePhotos[i], this);
			valuesPhotos.add(eyePhotoPairNode);

			// Workaround to ensure that the scrollbar is correctly resized after the images are loaded.
			eyePhotoPairNode.getImagesLoadedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(final ObservableValue<? extends Boolean> observable,
						final Boolean oldValue,
						final Boolean newValue) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							GridPane dummy = new GridPane();
							valuesPhotos.add(dummy);
							mListPhotos.layout();
							valuesPhotos.remove(dummy);
						}
					});
				}
			});
		}

		mPreviousName = name;

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				mListPhotos.setItems(valuesPhotos);
				dialog.close();
			}
		});
	}

	/**
	 * Remove the item for one date from the list.
	 *
	 * @param node The row to be removed.
	 */
	public void removeItem(final EyePhotoPairNode node) {
		mListPhotos.getItems().remove(node);
	}

	// METHODS CLONED FROM ANDROID

	/**
	 * Get the list of subfolders, using getFileNameForSorting() for ordering.
	 *
	 * @param parentFolder
	 *            The parent folder.
	 * @param searchString
	 *            A search String for the name.
	 * @return The list of subfolders.
	 */
	public static final List<String> getFolderNames(final File parentFolder, final String searchString) {
		File[] folders = parentFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File pathname) {
				return pathname.isDirectory() && nameStartsWith(pathname.getName(), searchString);
			}
		});

		List<String> folderNames = new ArrayList<>();
		if (folders == null) {
			return folderNames;
		}

		Collator collator = Collator.getInstance();
		final Map<File, CollationKey> collationMap = new HashMap<>();
		for (File folder : folders) {
			collationMap.put(folder, collator.getCollationKey(getFilenameForSorting(folder)));
		}

		Arrays.sort(folders, new Comparator<File>() {
			@Override
			public int compare(final File f1, final File f2) {
				return collationMap.get(f1).compareTo(collationMap.get(f2));
			}
		});
		for (File f : folders) {
			folderNames.add(f.getName());
		}
		return folderNames;
	}

	/**
	 * Check if a name part starts with the given String (case insensitive).
	 *
	 * @param name
	 *            The name.
	 * @param searchString
	 *            The search string.
	 * @return True if a name part starts with the given String.
	 */
	private static boolean nameStartsWith(final String name, final String searchString) {
		String[] nameParts = name.toLowerCase().split(" ");
		String searchStringCaseIndependent = searchString.toLowerCase();
		for (int i = 0; i < nameParts.length; i++) {
			if (nameParts[i].startsWith(searchStringCaseIndependent)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Helper method to return the name of the file for sorting.
	 *
	 * @param f
	 *            The file
	 * @return The name for Sorting
	 */
	private static String getFilenameForSorting(final File f) {
		String name = f.getName();

		boolean sortByLastName = PreferenceUtil.getPreferenceBoolean(PreferenceUtil.KEY_SORT_BY_LAST_NAME);
		if (sortByLastName) {
			int index = name.lastIndexOf(' ');
			if (index >= 0) {
				String firstName = name.substring(0, index);
				String lastName = name.substring(index + 1);
				name = lastName + " " + firstName;
			}
		}

		return name;
	}

	/**
	 * Create the list of eye photo pairs for display. Photos are arranged in pairs (right-left) by date.
	 *
	 * @param folder
	 *            the folder where the photos are located.
	 * @return The list of eye photo pairs.
	 */
	private EyePhotoPair[] createEyePhotoList(final File folder) {
		Map<Date, EyePhotoPair> eyePhotoMap = new TreeMap<>(new Comparator<Date>() {
			@Override
			public int compare(final Date lhs, final Date rhs) {
				return rhs.compareTo(lhs);
			}
		});

		File[] files = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				return name.toUpperCase().endsWith(".JPG");
			}
		});

		if (files == null) {
			return new EyePhotoPair[0];
		}

		for (File f : files) {
			EyePhoto eyePhoto = EyePhoto.fromFile(f);

			if (eyePhoto.isFormatted()) {
				Date date = eyePhoto.getDate();

				if (eyePhotoMap.containsKey(date)) {
					EyePhotoPair eyePhotoPair = eyePhotoMap.get(date);
					eyePhotoPair.setEyePhoto(eyePhoto);
				}
				else {
					EyePhotoPair eyePhotoPair = new EyePhotoPair();
					eyePhotoPair.setEyePhoto(eyePhoto);
					eyePhotoMap.put(date, eyePhotoPair);
				}
			}
			else {
				Logger.error("Eye photo is not formatted correctly: " + f.getAbsolutePath());
			}

		}

		return eyePhotoMap.values().toArray(new EyePhotoPair[eyePhotoMap.size()]);
	}

}
