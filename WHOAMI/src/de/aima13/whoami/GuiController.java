package de.aima13.whoami;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Marvin Klose
 */
public class GuiController implements Initializable {
	private TextArea agbArea;
	private CheckBox aggreedOnAGB;
	private CheckBox wantExecution;
	private Button okButton;
	private Button cancelButton;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
	}
}

