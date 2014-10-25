package de.aima13.whoami;

/**
 * Created by D060469 on 16.10.14.
 */
public class GuiManager {
	public static ProgressController pgController;
	final static String GUI_PREFIX = "................";
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
		System.out.println(GUI_PREFIX + "GUI PROGRESS STARTED");
	}

	public static void updateProgress(String status) {
		pgController.addComment(status);
	}

	public static void showReport(String reportHtml) {
		System.out.println(GUI_PREFIX + "GUI REPORT");
	}
	public static void closeAnalysisProgess(){
		pgController.finishedScanningClose();
	}
}
