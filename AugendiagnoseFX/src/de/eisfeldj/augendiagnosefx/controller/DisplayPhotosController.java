package de.eisfeldj.augendiagnosefx.controller;

import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_FOLDER_PHOTOS;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_LAST_NAME;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import de.eisfeldj.augendiagnosefx.fxelements.EyePhotoPairNode;
import de.eisfeldj.augendiagnosefx.util.DialogUtil;
import de.eisfeldj.augendiagnosefx.util.DialogUtil.ProgressDialog;
import de.eisfeldj.augendiagnosefx.util.EyePhoto;
import de.eisfeldj.augendiagnosefx.util.EyePhotoPair;
import de.eisfeldj.augendiagnosefx.util.Logger;
import de.eisfeldj.augendiagnosefx.util.PreferenceUtil;
import de.eisfeldj.augendiagnosefx.util.ResourceConstants;

/**
 * Controller for the "Display Photos" page.
 */
public class DisplayPhotosController implements Initializable, Controller {
	/**
	 * The list of folder names which should be shown on top of the list.
	 */
	protected static final List<String> FOLDERS_TOP = Arrays.asList(new String[] { "IRISTOPOGRAPHIE" });

	/**
	 * The full "Display Photos" pane.
	 */
	@FXML
	private Pane displayMain;

	/**
	 * The list of names.
	 */
	@FXML
	private ListView<String> listNames;

	/**
	 * The list of names.
	 */
	@FXML
	private ListView<GridPane> listPhotos;

	@Override
	public final void initialize(final URL location, final ResourceBundle resources) {
		List<String> valuesNames = getFolderNames(new File(PreferenceUtil.getPreferenceString(KEY_FOLDER_PHOTOS)));
		listNames.setItems(FXCollections.observableList(valuesNames));

		String lastName = PreferenceUtil.getPreferenceString(KEY_LAST_NAME);
		if (lastName != null && valuesNames.contains(lastName)) {
			showPicturesForName(lastName);
			int selectedIndex = valuesNames.indexOf(lastName);
			listNames.getSelectionModel().select(selectedIndex);
			listNames.scrollTo(selectedIndex);
		}
	}

	@Override
	public final Parent getRoot() {
		return displayMain;
	}

	/**
	 * Handler for Click on name on list. Displays the eye photo pairs for that name.
	 *
	 * @param event
	 *            The action event.
	 * @throws IOException
	 */
	@FXML
	protected final void handleNameClick(final MouseEvent event) throws IOException {
		String name = listNames.getSelectionModel().getSelectedItem();
		showPicturesForName(name);
		PreferenceUtil.setPreference(KEY_LAST_NAME, name);
	}

	/**
	 * Display the pictures for a given name.
	 *
	 * @param name
	 *            The name.
	 */
	private void showPicturesForName(final String name) {
		File nameFolder = new File(PreferenceUtil.getPreferenceString(KEY_FOLDER_PHOTOS), name);

		ProgressDialog dialog = DialogUtil.displayProgressDialog(ResourceConstants.MESSAGE_DIALOG_LOADING_PHOTOS, name);

		Thread thread = new Thread() {
			@Override
			public void run() {
				EyePhotoPair[] eyePhotos = createEyePhotoList(nameFolder);

				List<GridPane> valuesPhotos = new ArrayList<GridPane>();

				for (int i = 0; i < eyePhotos.length; i++) {
					valuesPhotos.add(new EyePhotoPairNode(eyePhotos[i]));
				}

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						listPhotos.setItems(FXCollections.observableList(valuesPhotos));
						dialog.close();
					}
				});
			}
		};
		thread.start();
	}

	// METHODS CLONED FROM ANDROID

	/**
	 * Get the list of subfolders, using getFileNameForSorting() for ordering.
	 *
	 * @param parentFolder
	 *            The parent folder.
	 * @return The list of subfolders.
	 */
	public static final List<String> getFolderNames(final File parentFolder) {
		File[] folders = parentFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File pathname) {
				return pathname.isDirectory();
			}
		});

		List<String> folderNames = new ArrayList<String>();
		if (folders == null) {
			return folderNames;
		}

		Arrays.sort(folders, new Comparator<File>() {
			@Override
			public int compare(final File f1, final File f2) {
				return getFilenameForSorting(f1).compareTo(getFilenameForSorting(f2));
			}
		});
		for (File f : folders) {
			folderNames.add(f.getName());
		}
		return folderNames;
	}

	/**
	 * Helper method to return the name of the file for sorting.
	 *
	 * @param f
	 *            The file
	 * @return The name for Sorting
	 */
	private static String getFilenameForSorting(final File f) {
		String name = f.getName().toUpperCase(Locale.getDefault());
		if (FOLDERS_TOP.contains(name)) {
			return "1" + name;
		}
		else {
			return "2" + name;
		}
	}

	/**
	 * Create the list of eye photo pairs for display. Photos are arranged in pairs (right-left) by date.
	 *
	 * @param folder
	 *            the folder where the photos are located.
	 * @return The list of eye photo pairs.
	 */
	private EyePhotoPair[] createEyePhotoList(final File folder) {
		Map<Date, EyePhotoPair> eyePhotoMap = new TreeMap<Date, EyePhotoPair>();

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
			EyePhoto eyePhoto = new EyePhoto(f);

			if (!eyePhoto.isFormatted()) {
				Logger.error("Eye photo is not formatted correctly: " + f.getAbsolutePath());
			}
			else {
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

		}

		// Remove incomplete pairs - need duplication to avoid ConcurrentModificationException
		Map<Date, EyePhotoPair> eyePhotoMap2 = new TreeMap<Date, EyePhotoPair>();
		for (Date date : eyePhotoMap.keySet()) {
			if (eyePhotoMap.get(date).isComplete()) {
				eyePhotoMap2.put(date, eyePhotoMap.get(date));
			}
		}

		return eyePhotoMap2.values().toArray(new EyePhotoPair[eyePhotoMap2.size()]);
	}

}
