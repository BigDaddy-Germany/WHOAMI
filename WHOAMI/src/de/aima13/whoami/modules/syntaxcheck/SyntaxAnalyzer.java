package de.aima13.whoami.modules.syntaxcheck;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.GlobalData;
import de.aima13.whoami.Whoami;
import de.aima13.whoami.modules.syntaxcheck.languages.LanguageSetting;
import de.aima13.whoami.support.Utilities;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.reflections.Reflections;

import de.aima13.whoami.modules.syntaxcheck.AntlrLauncher.CHECK_RESULT;
import org.stringtemplate.v4.ST;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Modul zum Analysieren des auf dem System gefundenen Codes auf syntaktische Korrektheit
 *
 * @author Marco Dörfler
 */
public class SyntaxAnalyzer implements Analyzable {
	private static final String TEMPLATE_LOCATION = "/data/antlrStats.html";
	private final String CSV_PREFIX = "syntaxcheck";
	private final String REPORT_TITLE = "Syntaxcheck";

	/*
	 * Es soll nur eine Stichprobe von Dateien pro Sprache geparst werden, um zu vermeiden,
	 * dass die Laufzeit durch ANTLR zu hoch wird
	 * Durch späteren Zufall soll eine Auswahl getroffen werden,
	 * welche später eine repräsentative Sicht auf die Qualität des Codings zu bieten
	 */
	private final int MAX_FILES_PER_LANGUAGE = 50;
	private boolean sampleOnly = false; // Wurde nur eine Stichprobe genommen?
	private int suicidal = 0; // Für Entscheidung über Suizidgefährdung in Bericht
	private final String[] FORBIDDEN_CONTAINS = {"jdk", "jre", "adt", "tex"};

	private Map<LanguageSetting, List<Path>> languageFilesMap;
	private List<String> moduleFilter;
	private Map<LanguageSetting, Map<CHECK_RESULT, Integer>> syntaxCheckResults;

	/**
	 * Im Konstruktor werden alle Settings geladen und instanziiert
	 */
	public SyntaxAnalyzer() {
		this.languageFilesMap = new HashMap<>();

		Reflections reflections = new Reflections("de.aima13.whoami.modules.syntaxcheck" +
				".languages" +
				".settings");

		Set<Class<? extends LanguageSetting>> settingClasses = reflections.getSubTypesOf
				(LanguageSetting.class);

		for (Class<? extends LanguageSetting> settingClass : settingClasses) {
			try {
				// Sprache wird mit leerer Dateiliste instanziiert
				this.languageFilesMap.put(settingClass.newInstance(), new ArrayList<Path>());
			} catch (InstantiationException | IllegalAccessException e) {
				// Code kann nicht analysiert werden
			}
		}

	}

	/**
	 * Filter bei Bedarf durch Iteration über die unterstützen Sprachen generieren
	 * und danach zurückgeben
	 *
	 * @return Liste der Filter
	 */
	@Override
	public List<String> getFilter() {
		// Filtereinstellungen werden nur bei Bedarf generiert, danach sollten sich diese
		// allterdings nicht mehr ändern, können also gespeichert werden
		if (this.moduleFilter == null) {
			this.moduleFilter = new ArrayList<>();

			// Da nur eine Endung pro Sprache unterstützt wird, reicht eine einfache Iteration
			// und Erstellen eines Patterns pro Endung
			for (LanguageSetting setting : this.languageFilesMap.keySet()) {
				this.moduleFilter.add("**." + setting.FILE_EXTENSION);
			}
		}

		return this.moduleFilter;
	}

	/**
	 * Erhaltene Dateien müssen den Programmiersprachen zugeordnet werden
	 *
	 * @param files Liste der gefundenen Dateien
	 */
	@Override
	public void setFileInputs(List<Path> files) {
		// Erstelle (nur für Zuordnung) Map: Extension -> List of Files
		Map<String, List<Path>> extensionFilesMap = new HashMap<>();
		for (Map.Entry<LanguageSetting, List<Path>> languageFilesEntry : this.languageFilesMap
				.entrySet()) {
			extensionFilesMap.put(languageFilesEntry.getKey().FILE_EXTENSION,
					languageFilesEntry.getValue());
		}

		// Iteriere über Dateien
		for (Path file : files) {
			// Auf verbotene Substrings prüfen
			boolean containsForbidden = false;
			for (String substr : this.FORBIDDEN_CONTAINS) {
				if (file.toAbsolutePath().toString().toLowerCase().contains(substr)) {
					containsForbidden = true;
					break;
				}
			}
			if (containsForbidden) {
				continue;
			}


			// Versuche Liste der Dateien zu erreichen und füge Datei ein
			List<Path> fileList = extensionFilesMap.get(Utilities.getFileExtenstion(file
					.toString()));

			// Wenn entsprechende Liste gefunden wurde, sortiere die Datei ein.
			if (fileList != null) {
				fileList.add(file);
			}
		}
	}

	@Override
	public String getHtml() {
		// Template laden
		ST template = new ST(Utilities.getResourceAsString(TEMPLATE_LOCATION), '$', '$');

		// Variable für die Unterscheidung "Wurde überhaupt Coding geprüft?" bereitstellen
		boolean noCoding = true;

		// Ergebnisse der Syntaxchecks setzen
		for (Map.Entry<LanguageSetting, Map<CHECK_RESULT, Integer>> languageResult :
				this.syntaxCheckResults.entrySet()) {
			// Setzen der Variablen für das Template

			// Korrekte und Inkorrekte Dateien können direkt ausgelesen werden
			int correct = languageResult.getValue().get(CHECK_RESULT.CORRECT);
			int error = languageResult.getValue().get(CHECK_RESULT.SYNTAX_ERROR);
			// Die Gesamtzahl ist die Summe dieser beiden Zahlen. Nicht geparste Dateien werden
			// ignoriert
			int sum = correct + error;

			// Eine Analyse wurde nur vorgenommen, wenn die Gesamtzahl ungleich 0 ist
			boolean analyzed = sum != 0;
			// Ein perfektes Resultat liegt vor, wenn es keine Fehler gab
			boolean perfectResult = error == 0;
			// Ein gutes Resultat liegt vor, wenn es zwar Fehler gab,
			// aber mindestens 60% der Dateien korrekt sind
			boolean goodResult = (error != 0 && correct >= 0.6 * sum);
			// Ein schlechtes Resultat liegt vor, wenn weder ein gutes noch ein perfektes vorliegen
			boolean badResult = !(perfectResult || goodResult);

			// Der Name der Variable entspricht der Dateiendung
			String varName = languageResult.getKey().FILE_EXTENSION;
			template.addAggr(varName + ".{analyzed, countAll, countCorrect, countError, " +
					"perfectResult, goodResult, badResult}", analyzed, sum, correct, error,
					perfectResult, goodResult, badResult);

			// Sollte hier coding analysiert worden sein, kann noCoding auf false gesetzt werden
			if (analyzed) {
				noCoding = false;
			}
		}

		template.add("noCoding", noCoding);
		template.add("sampleOnly", this.sampleOnly);
		template.add("maxFilesToAnalyze", MAX_FILES_PER_LANGUAGE);

		// Über Selbstmordgefährdung entscheiden
		template.add("suicidal", this.suicidal > 0);

		return template.render();
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
	 * Kalkulieren der CSV-Ausgabe. Jedes Feld soll wie volgt aussehen: SPRACHE-RESULT -> Anzahl
	 * @return Die Map der CSV-Einträge
	 */
	@Override
	public SortedMap<String, String> getCsvContent() {
		SortedMap<String, String> csvContent = new TreeMap<>();

		// Sprachen durchgehen und CSV-Einträge ausgeben (nur für erfolgreiche Ergebnise)
		for (Map.Entry<LanguageSetting, Map<CHECK_RESULT, Integer>> syntaxCheckResult : this
				.syntaxCheckResults.entrySet()) {
			// Ergebnisse durchgehen
			for (Map.Entry<CHECK_RESULT, Integer> checkResultEntry : syntaxCheckResult.getValue()
					.entrySet()) {
				// Wenn das Ergebnis nicht "Parsen nicht möglich" ist, Eintrag einfügen
				if (checkResultEntry.getKey() != CHECK_RESULT.CANT_PARSE) {
					// Prefix ist die Sprache
					csvContent.put(syntaxCheckResult.getKey().LANGUAGE_NAME + "-" +
							checkResultEntry.getKey().toString(), checkResultEntry.getValue().toString());
				}
			}
		}

		return csvContent;
	}

	/**
	 * Iterieren über alle gefundenen Sprachen und die dazugehörigen Dateien. Bei jedem Eintrag
	 * wird ein Syntax-Check durchgeführt und die Ergebnisse gespeichert. Es entsteht eine
	 * Taelle, welche Auskunft darüber gibt, wie viele Dateien mit welchem Result geprüft wurden
	 */
	@Override
	public void run() {
		// ResultMap initialisieren
		this.syntaxCheckResults = new HashMap<>();

		// Alle Programmiersprachen durchgehen
		for (Map.Entry<LanguageSetting, List<Path>> languageFilesEntry : this
				.languageFilesMap.entrySet()) {

			// ResultSet für diese Sprache initialisieren
			Map<CHECK_RESULT, Integer> checkResults = new HashMap<>();
			// Für jedes existente Result den Wert 0 initialisieren
			for (CHECK_RESULT result : CHECK_RESULT.values()) {
				checkResults.put(result, 0);
			}

			// Liste der Dateien in möglichst gleichgroßen Sprüngen so durchgehen,
			// dass die maximale Anzahl an Dateien nicht überschritten wird
			Path[] files = languageFilesEntry.getValue().toArray(new Path[languageFilesEntry
					.getValue().size()]);

			if (files.length > 0) {

				// wenn zu viele Dateien vorhanden sind, soll nur eine Stichprobe durchgeführt
				// werden
				int jump;
				if (files.length > MAX_FILES_PER_LANGUAGE) {
					jump = files.length / MAX_FILES_PER_LANGUAGE;
					this.sampleOnly = true;
				} else {
					jump = 1;
				}

				for (int currentIndex = 0; currentIndex / jump < MAX_FILES_PER_LANGUAGE; currentIndex += jump) {
					// Timeboxing und ArrayIndex prüfen
					if (Whoami.getTimeProgress() > 99 || currentIndex >= files.length) {
						break;
					}

					Path file = files[currentIndex];
					CHECK_RESULT checkResult = this.checkSyntax(languageFilesEntry.getKey(), file);
					// Entsprechende Summe der Results um eins erhöhen
					checkResults.put(checkResult, checkResults.get(checkResult) + 1);

					// Je nach Resultat Selbstmordgefährdung ändern
					int deltaSuicidal = 0;
					if (checkResult == CHECK_RESULT.CORRECT) {
						deltaSuicidal = -4;
					} else if (checkResult == CHECK_RESULT.SYNTAX_ERROR) {
						deltaSuicidal = 4;
					}

					GlobalData.getInstance().changeScore("Selbstmordgefährdung", deltaSuicidal);
					this.suicidal += deltaSuicidal;
				}
			}

			// Ergebnisse für diese Sprache speichern
			this.syntaxCheckResults.put(languageFilesEntry.getKey(), checkResults);
		}
	}

	/**
	 * Diese Methode prüft die syntaktische Korrektheit einer Datei nach der übergebenen Sprache.
	 * Dies über die CommandLine in einer neuen JVM um den Memory Leak durch den übermäßig großen
	 * ANTLR DFA Cache zu unterdrücken
	 *
	 * @param languageSetting Die Sprache, auf die geprüft werden soll
	 * @param file Die Datei, die geprüft werden soll
	 * @return ENUM, welches entscheidet, wie der Status der Datei ist
	 */
	private CHECK_RESULT checkSyntax(LanguageSetting languageSetting, Path file) {
		// Zusammenbauen des Commands
		// Teil eins: Ort der Java-Installation
		String command = System.getProperty("java.home") + File.separator + "bin" + File
				.separator + "java.exe ";
		// Teil zwei: Classpath
		command += "-cp \"" + System.getProperty("java.class.path") + "\" ";

		// Teil drei: Name der Klasse
		command += AntlrLauncher.class.getName() + " ";

		// Teil vier: Argumente
		command += languageSetting.getClass().getSimpleName() + " ";
		command += "\"" + file.toString() + "\"";


		Runtime runtime = Runtime.getRuntime();

		try {
			System.out.println("Start " + file);
			Process process = runtime.exec(command);
			process.waitFor();
			System.out.println("End " + file);
			return CHECK_RESULT.getCheckResultFromReturnCode(process.exitValue());
		} catch (IOException | InterruptedException e) {
			return CHECK_RESULT.CANT_PARSE;
		}
	}
}
