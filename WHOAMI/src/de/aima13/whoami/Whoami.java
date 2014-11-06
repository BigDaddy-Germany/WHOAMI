package de.aima13.whoami;

import java.util.ArrayList;
import java.util.List;

/**
 * Hauptklasse mit Main-Methode
 *
 * @author Marco Dörfler
 */
public class Whoami {
	private static final int ANALYZE_TIME= 1000; // Analysezeit in Sekunden
	public static final int PERCENT_FOR_FILE_SEARCHER = 75; // Wie viel Prozent für den
	private static long startTime;

	/**
	 * Standard Main-Methode
	 * @param args Commandline Argumente
	 */
	public static void main(String[] args) {
		startTime = System.currentTimeMillis();

		List<Analyzable> moduleList = new ArrayList<>();                // Liste der Module
		List<Representable> representableList = new ArrayList<>();      // Liste der Representables

		// Gui starten und AGB zur Bestätigung anzeigen
		GuiManager.startGui();
		if (!GuiManager.confirmAgb()) {
			// Beenden des Programms, falls der User die AGB ablehnt
			GuiManager.showGoodBye();
			System.exit(0);
		}

		// Fortschrittsanzeige einnblenden und immer wieder updaten
		GuiManager.showProgress();

		GuiManager.updateProgress("Lade und initialisiere Module...");

		// Module laden
		moduleList = ModuleManager.getModuleList();

		GuiManager.updateProgress("Scanne Dateisystem...");
		FileSearcher.startSearch(moduleList);

		// Instanz der Singletonklasse GlobalData holen
		GlobalData globalData = GlobalData.getInstance();

		GuiManager.updateProgress("Analysiere gefundene Dateien...");
		SlaveDriver.startModules(moduleList);

		// Stelle persönliche Daten an den Anfang der Liste
		representableList = new ArrayList<>();
		representableList.add(globalData);
		representableList.addAll(moduleList);

		// Starte Speichervorgang

		// CSV
		CsvCreator.saveCsv(representableList);

		// PDF
		ReportCreator reportCreator = new ReportCreator(representableList);
		reportCreator.savePdf();

		// Anzeigen des Berichtes
		GuiManager.showReport(reportCreator.getHtml());
	}

	/**
	 * Information über die bisherige und restliche Laufzeit des Programms
	 * @return Ganzzahliger Prozentwert zwischen 0 und 100 (100: Zeit ist um)
	 */
	public static int getTimeProgress() {
		float elapsedTime = (float) ((System.currentTimeMillis() - startTime) / 1000);
		int timeProgress = (int) (elapsedTime / ANALYZE_TIME * 100);

		if (timeProgress < 100) {
			return timeProgress;
		} else {
			return 100;
		}
	}

	/**
	 * Kalkulieren der noch verbleibenden Analysezeit
	 * @return Die Anzahl der Millisekunden, welche noch übrig sind
	 */
	public static long getRemainingMillis() {
		long elapsedTime = System.currentTimeMillis() - startTime;
		return ANALYZE_TIME * 1000 - elapsedTime;
	}
}
