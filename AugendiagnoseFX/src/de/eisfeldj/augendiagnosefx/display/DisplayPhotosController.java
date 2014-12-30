package de.eisfeldj.augendiagnosefx.display;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import de.eisfeldj.augendiagnosefx.Main;

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
	private static final File EYE_PHOTOS_FOLDER = new File("D:/Jörg/Bilder/SchnuSy/Augenfotos/");

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
		List<GridPane> valuesPhotos = new ArrayList<GridPane>();

		for (int i = 0; i < 2; i++) {
			GridPane pane = new GridPane();
			pane.setHgap(STANDARD_GAP);
			pane.add(new Label("date " + 1), 0, 0);
			pane.getColumnConstraints().add(0, new ColumnConstraints(DATE_WIDTH));

			GridPane imagePane = new GridPane();
			imagePane.setHgap(STANDARD_GAP);
			pane.add(imagePane, 1, 0);
			ColumnConstraints totalImageConstraints = new ColumnConstraints();
			totalImageConstraints.setHgrow(Priority.SOMETIMES);
			pane.getColumnConstraints().add(1, totalImageConstraints);

			File imageRightFile =
					new File("D:/Jörg/Bilder/SchnuSy/Augenfotos/Schraml Sybille/Schraml Sybille 2013-08-01 rechts.jpg");
			File imageLeftFile =
					new File("D:/Jörg/Bilder/SchnuSy/Augenfotos/Schraml Sybille/Schraml Sybille 2013-08-01 links.jpg");

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

			valuesPhotos.add(pane);
		}

		listNames.setItems(FXCollections.observableList(valuesNames));
		listPhotos.setItems(FXCollections.observableList(valuesPhotos));
	}

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

}
