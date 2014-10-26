package de.aima13.whoami;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.regex.Pattern;

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
		GuiManager.guiController = loader.getController();
		primaryStage.setTitle("Endnutzervereinbarung");
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
		primaryStage.setResizable(false);

		final FXMLLoader secondLoader = new FXMLLoader();
		final Stage secondaryStage = new Stage();
		secondaryStage.initStyle(StageStyle.UTILITY);
		InputStream isSecond = new FileInputStream("res/progressScreen.fxml");
		Parent root2 = (Parent) secondLoader.load(isSecond);
		GuiManager.progressController = secondLoader.getController();
		GuiManager.progressStage = secondaryStage;
		secondaryStage.setScene(new Scene(root2));

		primaryStage.setOnHiding(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent windowEvent) {
				eulaConfirmed = ((GuiController)loader.getController()).isEulaAccepted();
				Thread t =new Thread(new Runnable() {
					@Override
					public void run() {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								secondaryStage.show();
							}
						});
					}
				});
				t.start();
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
