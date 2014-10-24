package de.aima13.whoami;

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

	//JavaFX TextArea lassen sich dabei nicht gescheit handeln
	private double END_SCROLL_POSITION = 182;
	private boolean eulaAccepted = false;

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
		agbArea.setText("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.   \n" +
				"\n" +
				"Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.   \n" +
				"\n" +
				"Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi.   \n" +
				"\n" +
				"Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat.   \n" +
				"\n" +
				"Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis.   \n" +
				"\n" +
				"At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur");

	}

	@FXML
	private void clickedOnCancel(ActionEvent event) {
		eulaAccepted = false;
		cancelButton.getScene().getWindow().hide();
	}

	public void updateOkButton() {
		if (wantExecution.isSelected() && aggreedOnEula.isSelected() && isScrolledDown(agbArea.getScrollTop())) {
			okButton.setDisable(false);
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
		return position == END_SCROLL_POSITION;
	}

	public boolean isEulaAccepted() {
		return eulaAccepted;
	}
}
