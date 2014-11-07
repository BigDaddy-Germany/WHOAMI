package de.aima13.whoami;

/**
 * GuiManager zur Verwaltung von z.B. Nachrichten, die während des Scans und der Analyse
 * ausgegeben werden.
 *
 * @author Marco Dörfler
 * @author Marvin Klose
 */
public class GuiManager {
	public static ProgressController pgController;
	final static String GUI_PREFIX = "................";
	private static String resultingHtml;
	private static final boolean showGui = false;

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

	public synchronized static void updateProgress(String status) {
		pgController.addComment(status);
	}

	public static void showReport(String reportHtml) {
		resultingHtml = reportHtml;
		pgController.finishedScanningClose();
		System.out.println(GUI_PREFIX + "GUI REPORT");
	}

	private static void closeAnalysisProgess() {
		System.out.println(GUI_PREFIX + "GUI ANALYSIS READY");
	}

	public static String getResultingHtml() {
		return resultingHtml;
	}

	public static void closeProgressAndShowReport(String html) {
		closeAnalysisProgess();
		showReport(html);
	}
}
