package de.eisfeldj.augendiagnosefx.display;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

/**
 * Controller for the "Display Photos" page.
 */
public class DisplayPhotosController implements Initializable {
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
		List<String> valuesNames = new ArrayList<String>();
		List<GridPane> valuesPhotos = new ArrayList<GridPane>();

		for (int i = 0; i < 50; i++) {
			valuesNames.add("Item " + i);
		}

		for (int i = 0; i < 10; i++) {
			GridPane pane = new GridPane();
			pane.add(new Label("text"), 0, 0);
			pane.add(new Label("" + i), 1, 0);
			pane.setGridLinesVisible(true);
			valuesPhotos.add(pane);
		}

		listNames.setItems(FXCollections.observableList(valuesNames));
		listPhotos.setItems(FXCollections.observableList(valuesPhotos));
	}

}
