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
	private static final String WEB_TYPE = "web";
	private static final String NORMAL_CODE = "native";
	private final FileExtension[] fileExtensions;
	private final String[] FORBIDDEN_CONTAINS = {"jdk", "jre", "adt", "tex", "anaconda",
			"javadocx", "texlive", "smartgit", "adt-bundle", "javafx_samples"};

	/**
	 * Rückgabe der Sprache, die am häufigsten Verwendet wird
	 * @return Der Name der Sprache als String
	 */
	private String getTopLanguage() {
		String topLanguage = null;
		int counter = 0;
		for (Map.Entry<FileExtension, Integer> statisticsEntry : this.statisticResults.entrySet()) {
			if (statisticsEntry.getValue() > counter) {
				topLanguage = statisticsEntry.getKey().lang;
				counter = statisticsEntry.getValue();
			}
		}
		return topLanguage;
	}

	/**
	 * Inhaltstyp der json Datei als innere Klasse
	 *
	 * @author Marco Dörfler
	 */
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
		// Intitialisieren der Resultate
		for (FileExtension fileExtension : this.fileExtensions) {
			this.statisticResults.put(fileExtension, 0);
		}
	}

	/**
	 * Konstruiert die Filtereinstellungen aus der Liste der unterstützten Dateiendungen
	 *
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
	 *
	 * @param files Liste der gefundenen Dateien
	 * @throws Exception
	 */
	@Override
	public void setFileInputs(List<Path> files) throws Exception {
		this.fileInput = files;
	}


	/**
	 * Erzeugt den Modul-spezifischen HTML Code
	 * @return Der HTML Code für den Report
	 */
	@Override
	public String getHtml() {
		// Template laden
		ST template = new ST(Utilities.getResourceAsString(TEMPLATE_LOCATION), '$', '$');

		String topLanguage = this.getTopLanguage();
		template.add("topLanguage", topLanguage);
		// Wenn keine Programmierdateien auf dem Rechner gefunden wurden,
		// soll isProgrammer false sein
		template.add("isProgrammer", topLanguage != null);


		if (this.moreWebCoding()) {
			template.add("moreWebLanguage", true);
			template.add("moreNativeLanguage", false);
		} else {
			template.add("moreNativeLanguage", true);
			template.add("moreWebLanguage", false);
		}
		for (Map.Entry<FileExtension, Integer> statisticsEntry : this.statisticResults.entrySet()) {
			template.addAggr("programm.{extension, counter}", statisticsEntry.getKey().lang,
					statisticsEntry.getValue().toString());
		}
		return template.render();
	}

	/**
	 * Entscheidet darüber, ob der Benutzer eher nativ oder web-basiert programmiert
	 * @return True, wenn der Benutzer mehr im Web programmiert
	 */
	private boolean moreWebCoding() {
		int webFiles = 0;
		int nativeFiles = 0;

		for (Map.Entry<FileExtension, Integer> statisticsEntry : this.statisticResults.entrySet()) {
			if (statisticsEntry.getKey().type.equals(WEB_TYPE)) {
				webFiles++;
			} else {
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

	@Override
	public String[] getCsvHeaders() {
		List<String> csvHeaders = new ArrayList<>();

		for (FileExtension fileExtension : this.fileExtensions) {
			csvHeaders.add(fileExtension.lang);
		}
		return csvHeaders.toArray(new String[csvHeaders.size()]);
	}


	/**
	 * Iteriere über die Statistiken und füge sie in die Ergebnis-Map für die CSV Datei ein
	 *
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
					// Auf verbotene Substrings im Pfad prüfen
					boolean containsForbidden = false;
					for (String substr : this.FORBIDDEN_CONTAINS) {
						if (file.toAbsolutePath().toString().toLowerCase().contains(substr)) {
							containsForbidden = true;
							break;
						}
					}
					if (!containsForbidden) {
						this.statisticResults.put(fileExtension, this.statisticResults.get
								(fileExtension) + 1);
					}
				}
			}
		}
	}
}
