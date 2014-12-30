package de.eisfeldj.augendiagnosefx.display;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
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

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import de.eisfeldj.augendiagnosefx.Main;
import de.eisfeldj.augendiagnosefx.util.EyePhoto;
import de.eisfeldj.augendiagnosefx.util.EyePhotoPair;
import de.eisfeldj.augendiagnosefx.util.Logger;

/**
 * Controller for the "Display Photos" page.
 */
public class DisplayPhotosController implements Initializable {

	/**
	 * The width of the date node.
	 */
	private static final int DATE_WIDTH = 70;

	/**
	 * The standard size of gaps.
	 */
	private static final int STANDARD_GAP = 10;

	/**
	 * The eye photos folder.
	 */
	private static final File EYE_PHOTOS_FOLDER = new File("D:/J�rg/Bilder/SchnuSy/Augenfotos/");

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
		List<String> valuesNames = getFolderNames(EYE_PHOTOS_FOLDER);
		listNames.setItems(FXCollections.observableList(valuesNames));
	}

	/**
	 * Create a node for display of an eye photo pair in the list.
	 *
	 * @param pair
	 *            the eye photo pair.
	 * @return the list.
	 */
	private GridPane createNodeForEyePhotoPair(final EyePhotoPair pair) {
		GridPane pane = new GridPane();
		pane.setHgap(STANDARD_GAP);
		pane.add(new Label(pair.getDateDisplayString("dd.MM.yyyy")), 0, 0);
		pane.getColumnConstraints().add(0, new ColumnConstraints(DATE_WIDTH));

		GridPane imagePane = new GridPane();
		imagePane.setHgap(STANDARD_GAP);
		pane.add(imagePane, 1, 0);
		ColumnConstraints totalImageConstraints = new ColumnConstraints();
		totalImageConstraints.setHgrow(Priority.SOMETIMES);
		pane.getColumnConstraints().add(1, totalImageConstraints);

		File imageRightFile = pair.getRightEye().getFile();
		File imageLeftFile = pair.getLeftEye().getFile();

		String urlRight = null;
		String urlLeft = null;
		try {
			urlRight = imageRightFile.toURI().toURL().toExternalForm();
			urlLeft = imageLeftFile.toURI().toURL().toExternalForm();
		}
		catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		double imageWidth = (Main.getScene().getWidth() - 270) / 2; // MAGIC_NUMBER

		ImageView imageRight = new ImageView(urlRight);
		imageRight.setPreserveRatio(true);
		imageRight.setFitWidth(imageWidth);
		imagePane.add(imageRight, 0, 0);

		ImageView imageLeft = new ImageView(urlLeft);
		imageLeft.setPreserveRatio(true);
		imageLeft.setFitWidth(imageWidth);
		imagePane.add(imageLeft, 1, 0);

		ColumnConstraints imageConstraints = new ColumnConstraints();
		imageConstraints.setPercentWidth(50); // MAGIC_NUMBER
		imageConstraints.setHalignment(HPos.CENTER);
		imagePane.getColumnConstraints().add(0, imageConstraints);
		imagePane.getColumnConstraints().add(1, imageConstraints);

		return pane;
	}

	/**
	 * Handler for Click on name on list.
	 *
	 * @param event
	 *            The action event.
	 * @throws IOException
	 */
	@FXML
	protected final void handleNameClick(final MouseEvent event) throws IOException {
		String name = listNames.getSelectionModel().getSelectedItem();
		File nameFolder = new File(EYE_PHOTOS_FOLDER, name);

		EyePhotoPair[] eyePhotos = createEyePhotoList(nameFolder);

		List<GridPane> valuesPhotos = new ArrayList<GridPane>();

		for (int i = 0; i < eyePhotos.length; i++) {
			valuesPhotos.add(createNodeForEyePhotoPair(eyePhotos[i]));
		}

		listPhotos.setItems(FXCollections.observableList(valuesPhotos));
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

		if (folders == null) {
			return null;
		}

		Arrays.sort(folders, new Comparator<File>() {
			@Override
			public int compare(final File f1, final File f2) {
				return getFilenameForSorting(f1).compareTo(getFilenameForSorting(f2));
			}
		});
		List<String> folderNames = new ArrayList<String>();
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