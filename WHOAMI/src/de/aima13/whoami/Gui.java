package de.aima13.whoami;


import de.aima13.whoami.support.DataSourceManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Marvin Klose
 */
public class Gui extends Application {
	private static boolean eulaConfirmed = false;
	private GuiController guiController;
	private WebEngine webEngine;

	/**
	 * @return Ob die EULA akzeptiert wurde oder nicht.
	 */
	public static boolean eulaConfirmed() {
		return eulaConfirmed;
	}

	/**
	 * Methode startet den FX Application Thread
	 */
	public static void showEulaGui() {
		try {
			launch();
		} catch (Exception e) {
			//Hm dann ist wohl was beim start() kaputt
			System.exit(0);
		}
	}

	/**
	 * Diese Methode wird vom launch aufgerufen und bildet somit den Start des FX Application
	 * Threads. Zuerst werden die 3. Stages konstruiert und initialisiert. Die 2. und 3. sind
	 * final weil sie aus anonymen Listenern aufgerufen werden.
	 *
	 * @param primaryStage Wird vom launch() automatisch gefüllt. Ist die Stage die als erstes
	 *                     live geht.
	 * @throws Exception JavaFX Exceptions oder IO Exceptions
	 */
	@Override
	public void start(final Stage primaryStage) throws Exception {
		Stage first = initFirstStage(primaryStage);
		final Stage second = initSecondStage();
		final Stage third = initThirdStage();

		first.setOnHiding(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent windowEvent) {
				eulaConfirmed = guiController.isEulaAccepted();
				if (!eulaConfirmed) {
					System.exit(0);
				}
				Thread t = new Thread(new Whoami());
				t.start();
				second.show();

			}
		});
		second.setOnHiding(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent windowEvent) {
				Platform.setImplicitExit(true);
				webEngine.loadContent(GuiManager.getResultingHtml());
				third.show();
				third.centerOnScreen();
				DataSourceManager.closeRemainingOpenConnections();
			}
		});
	}

	/**
	 * Erster Teil des Programms mit der EULA und den jeweiligen Checkboxen.
	 * Erst wenn die Stage aus dem Fokus verschwindet läuft WHOAMI im Thread los.
	 *
	 * @param primaryStage Eingangsparameter der start Methode die vom FX Thread kommt
	 * @return Stage    Erster Startbildschrim
	 *
	 * @throws IOException Wenn FXML Datei nicht gefunden wurde.
	 */
	private Stage initFirstStage(final Stage primaryStage) throws IOException {
		primaryStage.initStyle(StageStyle.TRANSPARENT);
		final FXMLLoader loader = new FXMLLoader();
		Parent root;
		InputStream is = Whoami.class.getResourceAsStream("/gui/start.fxml");
		root = (Parent) loader.load(is);
		guiController = loader.getController();
		primaryStage.setTitle("Endnutzervereinbarung [WHOAMI-Scanner]");
		Scene scene = new Scene(root);
		scene.getStylesheets().addAll(Whoami.class.getResource("/gui/window.css")
				.toExternalForm());
		primaryStage.getIcons().add(new Image(Whoami.class.getResourceAsStream("/gui/Symbol.png")));
		primaryStage.setScene(scene);
		scene.setFill(null);
		primaryStage.show();
		primaryStage.setResizable(false);
		return primaryStage;
	}

	/**
	 * Stage besteht im wesentlichen aus einer TextArea auf der die Kommentare anzeigt werden,
	 * die von den Modulen oder sonst wem kommen. Die ProgressBar läuft unten munter von links
	 * nach Rechts und wieder zurück.
	 *
	 * @return Stage 2. Screen der den Fortschritt anzeigt.
	 *
	 * @throws IOException Wenn die passende FXML Datei nicht gefunden wird.
	 */
	private Stage initSecondStage() throws IOException {
		final FXMLLoader secondLoader = new FXMLLoader();
		final Stage secondaryStage = new Stage();
		secondaryStage.initStyle(StageStyle.TRANSPARENT);
		secondaryStage.setTitle("Analyse... [WHOAMI-Scanner]");
		InputStream isSecond = Whoami.class.getResourceAsStream("/gui/progressScreen.fxml");
		Parent root2 = (Parent) secondLoader.load(isSecond);
		Scene scene = new Scene(root2);
		scene.getStylesheets().addAll(Whoami.class.getResource("/gui/window.css")
				.toExternalForm());
		secondaryStage.setScene(scene);
		scene.setFill(null);
		GuiManager.pgController = secondLoader.getController();
		secondaryStage.getIcons().add(new Image(Whoami.class.getResourceAsStream("/gui/Symbol.png")));
		Platform.setImplicitExit(false);

		secondaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				event.consume();
			}
		});
		return secondaryStage;
	}

	/**
	 * Fenster mit dem Titel dein Ergebnis. In dieser Stage wird das Ergebnis präsentiert. Der
	 * Stagestyle ist nicht mehr der reduzierte, sondern es ist wieder alles in vollem Umfang
	 * einsehbar. Die FXML beschreibt den Screen, der lediglich aus einer WebView besteht.
	 * Die Engine wird lokal gespeichert. Der CSS Style wird ebenfalls wiederverwendet.
	 *
	 * @return Stage Dritte und letzte Stage
	 */
	private Stage initThirdStage() throws IOException {
		final FXMLLoader thirdLoader = new FXMLLoader();
		final Stage thirdStage = new Stage();
		thirdStage.setTitle("Dein Ergebnis");

		InputStream isThird = Whoami.class.getResourceAsStream("/gui/reportScreen.fxml");
		Parent rootReport = (Parent) thirdLoader.load(isThird);
		webEngine = ((WebView) rootReport.lookup("#webView")).getEngine();
		Scene scene = new Scene(rootReport);
		scene.getStylesheets().addAll(Whoami.class.getResource("/gui/window.css")
				.toExternalForm());
		thirdStage.getIcons().add(new Image(Whoami.class.getResourceAsStream("/gui/Symbol.png")));
		thirdStage.setScene(scene);
		return thirdStage;
	}
}
