package de.aima13.whoami;

import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Created by D060469 on 16.10.14.
 */
public class GuiManager {
	final static String GUI_PREFIX = "................";
	public static GuiController guiController;
	public static ProgressController progressController;
	public static Stage progressStage;
	public static void startGui() {

		System.out.println(GUI_PREFIX + "GUI STARTED");
	}

	public static boolean confirmAgb() {
		System.out.println(GUI_PREFIX + "GUI AGB");
		Gui.showEulaGui();
		return Gui.eulaConfirmed();
	}

	public static void showGoodBye() {
		// Gui zeigt keinen GoodBye
		System.out.println(GUI_PREFIX + "GUI GOOD BYE EULA DECLINED");
	}

	public static void showProgress() {
//		Thread t =new Thread(new Runnable() {
//			@Override
//			public void run() {
//				Platform.runLater(new Runnable() {
//					@Override
//					public void run() {
//						progressStage.show();
//					}
//				});
//			}
//		});
//		t.start();
		System.out.println(GUI_PREFIX + "GUI PROGRESS STARTED");
	}

	public static void updateProgress(String status) {
		System.out.println(GUI_PREFIX + "GUI UPDATED: " + status);
	}

	public static void showReport(String reportHtml) {
		System.out.println(GUI_PREFIX + "GUI REPORT");
	}
}
