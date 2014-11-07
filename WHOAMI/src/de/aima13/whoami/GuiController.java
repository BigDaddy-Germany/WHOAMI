package de.aima13.whoami;

import de.aima13.whoami.support.Utilities;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="cancelButton"
	private Button cancelButton; // Value injected by FXMLLoader

	@FXML // fx:id="wantExecution"
	private CheckBox wantExecution; // Value injected by FXMLLoader

	@FXML // fx:id="agbArea"
	private TextArea agbArea; // Value injected by FXMLLoader

	@FXML // fx:id="okButton"
	private Button okButton; // Value injected by FXMLLoader

	@FXML // fx:id="aggreedOnEula"
	private CheckBox aggreedOnEula; // Value injected by FXMLLoader

	// 5335 ist der maximale Wert den die ScrollBar hat
	private final double endScrollPosition = 5335.0;
	private boolean eulaAccepted = false;
	private boolean scrolledOnceDown = false;

	/**
	 * Called to initialize a controller after its root element has been
	 * completely processed.
	 *
	 * @param location  The location used to resolve relative paths for the root object, or
	 *                  <tt>null</tt> if the location is not known.
	 * @param resources The resources used to localize the root object, or <tt>null</tt> if
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		agbArea.setText(Utilities.getResourceAsString("/EULA.txt"));
	}

	@FXML
	private void clickedOnCancel(ActionEvent event) {
		eulaAccepted = false;
		cancelButton.getScene().getWindow().hide();
	}

	public void updateOkButton() {
		if (wantExecution.isSelected() && aggreedOnEula.isSelected() && (
				isScrolledDown(agbArea.getScrollTop()) || scrolledOnceDown)) {
			okButton.setDisable(false);
			scrolledOnceDown = true;
		} else {
			okButton.setDisable(true);
		}
	}

	@FXML
	private void clickedOnEula(ActionEvent actionEvent) {
		updateOkButton();
	}

	@FXML
	private void clickedOnWantToExecute(ActionEvent actionEvent) {
		updateOkButton();
	}

	@FXML
	private void clickedOnOk(ActionEvent actionEvent) {
		eulaAccepted = true;
		okButton.getScene().getWindow().hide();
	}

	private boolean isScrolledDown(double position) {
		return position == endScrollPosition;
	}

	public boolean isEulaAccepted() {
		return eulaAccepted;
	}
}
