package de.aima13.whoami;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

public class ProgressController implements Initializable{

	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="runningProgressBar"
	private ProgressBar runningProgressBar; // Value injected by FXMLLoader

	@FXML // fx:id="mainTextArea"
	private TextArea mainTextArea; // Value injected by FXMLLoader

	@FXML
	void ignoreUser(ActionEvent event) {
		event.consume();
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		mainTextArea.appendText("Achtung Ärmel hochkrempeln und Abfahrt...");
	}
}