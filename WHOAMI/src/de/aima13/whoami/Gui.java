package de.aima13.whoami;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author Marvin Klose
 */
public class Gui extends Application {

	@Override
	public void start(final Stage primaryStage) throws Exception{
		primaryStage.initStyle(StageStyle.UTILITY);
		FXMLLoader loader = new FXMLLoader();
		Parent root;
		InputStream is = new FileInputStream("res/start.fxml");
		root = (Parent) loader.load(is);
		primaryStage.setTitle("Endnutzervereinbarung");
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}

	public static void launchGui(String [] args) {
		launch(args);
	}
}
