package de.aima13.whoami;

import java.util.*;

/**
 * Hauptklasse mit Main-Methode
 *
 * @author Marco Dörfler
 */
public class Whoami implements Runnable {
	private static final int ANALYZE_TIME = 1000; // Analysezeit in Sekunden
	public static final int PERCENT_FOR_FILE_SEARCHER = 75; // Wie viel Prozent für den
	private static long startTime;
	private Map<Representable, String[]> csvHeaderMap = new HashMap<>();

	/**
	 * Standard Main-Methode
	 * @param args Commandline Argumente
	 */
	public static void main(String[] args) {
		startTime = System.currentTimeMillis();
			// Gui starten und AGB zur Bestätigung anzeigen
		GuiManager.startGui();
		if (!GuiManager.confirmAgb()) {
			// Beenden des Programms, falls der User die AGB ablehnt
			System.exit(0);
		}
		// hier gehts dann aus dem Application Thread weiter ins Run unten
	}

	@Override
	public void run() {
		System.out.println("\n\nIch runne mal die Module");
		List<Analyzable> moduleList = new ArrayList<>();                // Liste der Module
		List<Representable> representableList = new ArrayList<>();      // Liste der Representables

		GuiManager.updateProgress("Lade und initialisiere Module...");

		// Module laden
		moduleList = ModuleManager.getModuleList();


		GuiManager.updateProgress("Scanne Dateisystem...");
		FileSearcher.startSearch(moduleList);

		// Instanz der Singletonklasse GlobalData holen
		GlobalData globalData = GlobalData.getInstance();


		// Die Liste der Representables ist alles, was im Bericht auftaucht. Das sind alle Module
		// und die GlobalData Klasse
		// Stelle persönliche Daten an den Anfang der Liste
		representableList.add(globalData);
		representableList.addAll(moduleList);


		/*
		CSV Header der Module werden abgefragt, bevor die Module gestartet werden,
		sodass sie die CSV-Einträge nicht von den gefundenen Dateien abhängig machen können. Dies
		sorgt für eine einheitliche CSV-Datei und bewirkt, dass neue Zeilen später einfachher
		angehängt werden können.
		*/
		List<String> csvHeaderList = new ArrayList<>();
		for (Representable representable : representableList) {
			this.csvHeaderMap.put(representable, representable.getCsvHeaders());
		}


		GuiManager.updateProgress("Analysiere gefundene Dateien...");
		SlaveDriver.startModules(moduleList);

		// Starte Speichervorgang
		/*
		Zur besseren Zuordnung von Bericht und Tabelleneintrag sollten beide mit einer ID
		versehen werden. Diese wird jetzt generiert
		 */
		String scanId = Integer.toHexString((int) (startTime / 1000));

		// CSV
		CsvCreator.saveCsv(this.csvHeaderMap, scanId);

		// PDF
		ReportCreator reportCreator = new ReportCreator(representableList, scanId);
		reportCreator.savePdf();
		
		GuiManager.updateProgress("Bin fertig :)");


		// Anzeigen des Berichtes
		GuiManager.closeProgressAndShowReport(reportCreator.getHtml());
	}

	/**
	 * Kalkulieren der noch verbleibenden Analysezeit
	 * @return Die Anzahl der Millisekunden, welche noch übrig sind
	 */
	public static long getRemainingMillis() {
		long elapsedTime = System.currentTimeMillis() - startTime;
		return ANALYZE_TIME * 1000 - elapsedTime;
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

}
