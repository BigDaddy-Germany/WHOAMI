package de.aima13.whoami.modules.coding;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.Whoami;
import de.aima13.whoami.modules.coding.languages.LanguageSetting;
import de.aima13.whoami.support.Utilities;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.reflections.Reflections;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Modul zum Analysieren des auf dem System gefundenen Codes
 *
 * Created by Marco Dörfler on 28.10.14.
 */
public class CodeAnalyzer implements Analyzable {
	private final String CSV_PREFIX = "coding";
	private final String REPORT_TITLE = "Code-Analyse";

	private Map<LanguageSetting, List<Path>> languageFilesMap;
	private List<String> moduleFilter;
	private Map<LanguageSetting, Map<CHECK_RESULT, Integer>> syntaxCheckResults;

	private static enum CHECK_RESULT {
		CANT_PARSE, CORRECT, SYNTAX_ERROR
	}

	/**
	 * Im Konstruktor werden alle Settings geladen und instanziiert
	 *
	 * @author Marco Dörfler
	 */
	public CodeAnalyzer() {
		this.languageFilesMap = new HashMap<>();

		Reflections reflections = new Reflections("de.aima13.whoami.modules.coding.languages" +
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
	 *
	 * @author Marco Dörfler
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
	 *
	 * @author Marco Dörfler
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

			// Alle Dateien der Sprache parsen
			for (Path file : languageFilesEntry.getValue()) {
				// Timeboxing prüfen
				if (Whoami.getTimeProgress() > 99) {
					return;
				}

				CHECK_RESULT checkResult = this.checkSyntax(languageFilesEntry.getKey(), file);
				// Entsprechende Summe der Results um eins erhöhen
				checkResults.put(checkResult, checkResults.get(checkResult) + 1);
			}

			// Ergebnisse für diese Sprache speichern
			this.syntaxCheckResults.put(languageFilesEntry.getKey(), checkResults);
		}

		int i = 0;
	}

	/**
	 * Diese Methode iteriert über alle gefundenen Sprachen und checkt deren syntaktische
	 * Korrektheit.
	 *
	 * @param languageSetting Die Sprache, auf die geprüft werden soll
	 * @param file Die Datei, die geprüft werden soll
	 * @return ENUM, welches entscheidet, wie der Status der Datei ist
	 *
	 * @author Marco Dörfler
	 */
	private CHECK_RESULT checkSyntax(LanguageSetting languageSetting, Path file) {
		try {
			// ANTLRInputStrem erzeugen
			ANTLRInputStream inputStream = new ANTLRInputStream(Files.newInputStream(file));

			// Constructor des Lexers auslesen
			// Umweg über Array nötig, da genauer Typ des Lexers nicht bekannt (nur Oberklasse)
			Constructor<Lexer>[] lexerConstructors = (Constructor<Lexer>[]) languageSetting.LEXER.getConstructors();
			Lexer lexer = lexerConstructors[0].newInstance(inputStream);

			// Constructor des Parsers auslesen
			// Umweg über Array nötig, da genauer Typ des Lexers nicht bekannt (nur Oberklasse)
			CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
			Constructor<Parser>[] parserConstructors = (Constructor<Parser>[]) languageSetting.PARSER.getConstructors();

			// Parser Instanziieren
			Parser parser = parserConstructors[0].newInstance(commonTokenStream);

			// Methode des Startsymbols auslesen
			Method startMethod = languageSetting.PARSER.getMethod(languageSetting.START_SYMBOL);

			// Antlr gibt bei Fehlern etwas auf dem Errorstream aus. Das soll unterdrückt werden,
			// indem der Errorstream neu gesetzt wird (dieser tut nichts). Vorher sollte der
			// aktuelle Errorstream gespeichert werden, sodass alles resettet werden kann
			PrintStream standardErrorStream = System.err;
			System.setErr(new PrintStream(new OutputStream() {
				@Override
				public void write(int b) throws IOException {
				}
			}));
			// Methode aus der Instanz des Parsers heraus ausführen
			startMethod.invoke(parser);
			// Errorstream resetten
			System.setErr(standardErrorStream);

			// Entscheidung nach Syntaxfehlern
			if (parser.getNumberOfSyntaxErrors() == 0) {
				return CHECK_RESULT.CORRECT;
			} else {
				return CHECK_RESULT.SYNTAX_ERROR;
			}

		} catch (IOException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
			// Hier landen wir nur, wenn etwas schief gegangen ist. Was genau ist eigentlich
			// uninteressant. Wichtig ist: Wir können die Datei nicht parsen. Warum auch immer.
			return CHECK_RESULT.CANT_PARSE;
		}
	}
}
