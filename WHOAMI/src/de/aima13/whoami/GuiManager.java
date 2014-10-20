package de.aima13.whoami;

/**
 * Created by D060469 on 16.10.14.
 */
public class GuiManager {
	final static String GUI_PREFIX = "................";
	public static void startGui() {
		System.out.println(GUI_PREFIX + "GUI STARTED");
	}

	public static boolean confirmAgb() {
		System.out.println(GUI_PREFIX + "GUI AGB");
		return true;
	}

	public static void showGoodBye() {
		System.out.println(GUI_PREFIX + "GUI GOOD BYE");
	}

	public static void showProgress() {
		System.out.println(GUI_PREFIX + "GUI PROGRESS STARTED");
	}

	public static void updateProgress(String status) {
		System.out.println(GUI_PREFIX + "GUI UPDATED: " + status);
	}

	public static void showReport(String reportHtml) {
		System.out.println(GUI_PREFIX + "GUI REPORT");
	}
}
