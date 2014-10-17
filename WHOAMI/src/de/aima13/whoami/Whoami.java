package de.aima13.whoami;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by D060469 on 16.10.14.
 */
public class Whoami {
	public static void main(String[] args) {
		GuiManager guiManager = new GuiManager();                       // Steuerung der GUI
		List<Analyzable> moduleList = new ArrayList<>();                // Liste der Module
		List<Representable> representableList = new ArrayList<>();      // Liste der Representables

		// Gui starten und AGB zur Bestätigung anzeigen
		guiManager.startGui();
		if (!guiManager.confirmAgb()) {
			// Beenden des Programms, falls der User die AGB ablehnt
			GuiManager.showGoodBye();
			System.exit(0);
		}

		// Fortschrittsanzeige einnblenden und immer wieder updaten
		guiManager.showProgress();

		guiManager.updateProgress("Lade und initialisiere Module...");
		/**
		 * @todo Errorhandling
		 */
		try {
			moduleList = ModuleManager.getModuleList();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		guiManager.updateProgress("Scanne Dateisystem...");
		FileSearcher.startSearch(moduleList);

		// Instanz der Singletonklasse GlobalData holen
		GlobalData globalData = GlobalData.getInstance();

		guiManager.updateProgress("Analysiere gefundene Dateien...");
		SlaveDriver.startModules(moduleList);

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
