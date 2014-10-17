package de.aima13.whoami;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by D060469 on 16.10.14.
 */
public class Whoami {
	public static void main(String[] args) {
		GuiManager guiManager = new GuiManager();   // Steuerung der GUI
		List<Analyzable> moduleList;                // Liste der Module
		List<Representable> representableList;      // Liste der Representables
		GlobalData globalData;

		// Gui starten und AGB zur Bestätigung anzeigen
		guiManager.startGui();
		if (!guiManager.confirmAgb()) {
			// Beenden des Programms, falls der User die AGB ablehnt
			System.exit(0);
		}

		// Fortschrittsanzeige einnblenden und immer wieder updaten
		guiManager.showProgress();

		guiManager.updateProgess("Lade und initialisiere Module...");
		moduleList = ModuleManager.getModuleList();

		guiManager.updateProgess("Scanne Dateisystem...");
		FileSearcher.startSearch(moduleList);

		// Start der Sammlung der globalen Daten und Scores
		globalData = new GlobalData();

		guiManager.updateProgess("Analysiere gefundene Dateien...");
		try {
			SlaveDriver.startModules(moduleList);
		} catch (Exception e) {
			/**
			 * @todo Errorhandling SlaveDriver
			 */
			e.printStackTrace();
		}

		// Stelle persönliche Daten an den Anfang der Liste
		representableList = new ArrayList<>();
		representableList.add(globalData);
		representableList.addAll(moduleList);

		// Starte Speichervorgang

		// CSV
		try {
			CsvCreator.saveCsv(representableList);
		} catch (Exception e) {
			/**
			 * @todo Errorhandling Csv-Saver
			 */
			e.printStackTrace();
		}

		// PDF
		ReportCreator reportCreator = new ReportCreator(representableList);
		try {
			reportCreator.savePdf();
		} catch (Exception e) {
			/**
			 * @todo Errorhandling Report-Creator
			 */
			e.printStackTrace();
		}

		// Anzeigen des Berichtes
		GuiManager.showReport(reportCreator.getHtml());

	}
}
