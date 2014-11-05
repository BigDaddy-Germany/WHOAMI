package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.support.Utilities;
import org.stringtemplate.v4.ST;

import java.nio.file.Path;
import java.util.*;

/**
 * Modul zum Analysieren der Häufigkeit der verschiedenen Programmiersprachen,
 * die der Nutzer verwendet
 *
 * @author Marco Dörfler
 */
public class CodeStatistics implements Analyzable {

	private static final String TEMPLATE_LOCATION = "/data/programmStats.html";
	private static final String WEB_TYPE ="web";
	private static final String NORMAL_CODE = "native";
	private final FileExtension[] fileExtensions;
	// Inhaltstyp der JSon Datei
	private class FileExtension {
		public String ext;
		public String lang;
		public String type;
	}

	private final String REPORT_TITLE = "Coding Statistiken";
	private final String CSV_PREFIX = "codestatistics";

	private List<Path> fileInput;
	private Map<FileExtension, Integer> statisticResults;

	/**
	 * Im Konstruktor wird die JSon Datei der Dateiendungen eingelesen und gespeichert (wird
	 * schon bei der Ausgabe der Filter benötigt. Des weiteren wird die Map der Ergebnisse
	 * initialisiert
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
	 */
	@Override
	public void setFileInputs(List<Path> files) throws Exception {
		this.fileInput = files;
	}

	@Override
	public String getHtml() {
		// Template laden
		ST template = new ST(Utilities.getResourceAsString(TEMPLATE_LOCATION), '$', '$');
		if(moreWebCoding()){
			template.add("moreWebLanguage",true);
		}else{
			template.add("moreNativeLanguage",true);
		}
		for (Map.Entry<FileExtension, Integer> statisticsEntry : this.statisticResults.entrySet()) {
			template.addAggr("programm.{extension, counter}",statisticsEntry.getKey().lang,
					statisticsEntry.getValue().toString());
		}
		return template.render();
	}

	private boolean moreWebCoding() {
		int webFiles=0; int nativeFiles=0;
		for (Map.Entry<FileExtension, Integer> statisticsEntry : this.statisticResults.entrySet()) {
			if(statisticsEntry.getKey().type.equals(WEB_TYPE)){
				webFiles++;
			}else{
				nativeFiles++;
			}
		}
		return webFiles > nativeFiles;
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
