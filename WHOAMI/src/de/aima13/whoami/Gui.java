package de.aima13.whoami;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author Marvin Klose
 */
public class Gui extends Application {
	private static boolean eulaConfirmed = false;
	@Override
	public void start(final Stage primaryStage) throws Exception{
		primaryStage.initStyle(StageStyle.UTILITY);
		final FXMLLoader loader = new FXMLLoader();
		Parent root;
		InputStream is = new FileInputStream("res/start.fxml");
		root = (Parent) loader.load(is);
		primaryStage.setTitle("Endnutzervereinbarung");
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
		primaryStage.setResizable(false);
		primaryStage.setOnHiding(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent windowEvent) {
				eulaConfirmed = ((GuiController)loader.getController()).isEulaAccepted();
			}
		});

	}

	public static void showEulaGui() {
		launch(new String[]{});
	}
	public static boolean eulaConfirmed(){
		return eulaConfirmed;
	}
}
