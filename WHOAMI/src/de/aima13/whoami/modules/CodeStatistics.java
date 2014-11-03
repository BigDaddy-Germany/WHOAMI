package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.support.Utilities;

import java.nio.file.Path;
import java.util.*;

/**
 * Modul zum Analysieren der Häufigkeit der verschiedenen Programmiersprachen,
 * die der Nutzer verwendet
 *
 * Created by Marco Dörfler on 03.11.14.
 */
public class CodeStatistics implements Analyzable {

	private final FileExtension[] fileExtensions;
	// Inhaltstyp der JSon Datei
	private class FileExtension {
		public String ext;
		public String lang;
	}

	private final String REPORT_TITLE = "Coding Statistiken";
	private final String CSV_PREFIX = "codestatistics";

	private List<Path> fileInput;
	private Map<FileExtension, Integer> statisticResults;

	/**
	 * Im Konstruktor wird die JSon Datei der Dateiendungen eingelesen und gespeichert (wird
	 * schon bei der Ausgabe der Filter benötigt. Des weiteren wird die Map der Ergebnisse
	 * initialisiert
	 *
	 * @author Marco Dörfler
	 */
	public CodeStatistics() {
		this.fileExtensions = Utilities.loadDataFromJson("/data/CodeStatistics_FileExtensions" +
				".json", FileExtension[].class);

		this.statisticResults = new HashMap<>();
		for (FileExtension fileExtension : this.fileExtensions) {
			this.statisticResults.put(fileExtension, 0);
		}
	}

	/**
	 * Konstruiert die Filtereinstellungen aus der Liste der unterstützten Dateiendungen
	 * @return Die erstellte Liste der Filter
	 *
	 * @author Marco Dörfler
	 */
	@Override
	public List<String> getFilter() {
		List<String> filter = new ArrayList<>();

		for (FileExtension fileExtension : this.fileExtensions) {
			filter.add("**." + fileExtension.ext);
		}

		return filter;
	}

	/**
	 * Dateien werden einfach gespeichert und später genutzt
	 * @param files Liste der gefundenen Dateien
	 * @throws Exception
	 *
	 * @author Marco Dörfler
	 */
	@Override
	public void setFileInputs(List<Path> files) throws Exception {
		this.fileInput = files;
	}

	@Override
	public String getHtml() {
		return null;
	}

	@Override
	public String getReportTitle() {
		return REPORT_TITLE;
	}

	@Override
	public String getCsvPrefix() {
		return CSV_PREFIX;
	}


	/**
	 * Iteriere über die Statistiken und füge sie in die Ergebnis-Map für die CSV Datei ein
	 * @return Die fertige CSV-Datei
	 *
	 * @author Marco Dörfler
	 */
	@Override
	public SortedMap<String, String> getCsvContent() {
		SortedMap<String, String> csvContent = new TreeMap<>();

		for (Map.Entry<FileExtension, Integer> statisticsEntry : this.statisticResults.entrySet()) {
			csvContent.put(statisticsEntry.getKey().lang, statisticsEntry.getValue().toString());
		}

		return csvContent;
	}

	/**
	 * Iteriert über die Dateien und sortiert diese nach den verschiedenen Endungen
	 *
	 * @author Marco Dörfler
	 */
	@Override
	public void run() {
		// Über Dateien iterieren
		for (Path file : this.fileInput) {
			// Über Sprache entscheiden
			for (FileExtension fileExtension : this.fileExtensions) {
				// Wenn die Dateiendung passt, Wert um eins erhöhen
				if (file.toString().endsWith(fileExtension.ext)) {
					this.statisticResults.put(fileExtension, this.statisticResults.get
							(fileExtension) + 1);
				}
			}
		}
	}
}
