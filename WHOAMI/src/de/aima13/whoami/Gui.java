package de.aima13.whoami;

import com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDLoader;
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
		final GuiController guiController = loader.getController();
		primaryStage.setTitle("Endnutzervereinbarung");
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
		primaryStage.setResizable(false);

		final FXMLLoader secondLoader = new FXMLLoader();
		final Stage secondaryStage = new Stage();
		secondaryStage.initStyle(StageStyle.UNDECORATED);
		InputStream isSecond = new FileInputStream("res/progressScreen.fxml");
		Parent root2 = (Parent) secondLoader.load(isSecond);
		secondaryStage.setScene(new Scene(root2));
		GuiManager.pgController = secondLoader.getController();

		Platform.setImplicitExit(false);

		secondaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				event.consume();
			}
		});
		primaryStage.setOnHiding(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent windowEvent) {
				eulaConfirmed = guiController.isEulaAccepted();
				if(eulaConfirmed){
					Thread t = new Thread(new Whoami());
					t.start();
					secondaryStage.show();
				}
			}
		});
		secondaryStage.setOnHiding(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent windowEvent) {
				//show pdf Stage
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
