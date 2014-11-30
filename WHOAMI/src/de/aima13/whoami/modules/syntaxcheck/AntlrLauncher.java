package de.aima13.whoami.modules.syntaxcheck;

import de.aima13.whoami.modules.syntaxcheck.languages.LanguageSetting;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Diese Klasse erlaubt den Aufruf des ANTLR Parsings innerhalb einer neuen JVM. Dies stellt
 * sicher, dass durch ANTLR kein DFA-Caching vorgenommen werden kann
 *
 * @author Marco Dörfler
 */
public class AntlrLauncher {

	/**
	 * Speicherung des CheckResults soll aus Schönheitsgründen in Enum erfolgen und nicht mit
	 * reinen Zahlen. Dadurch ist es später sehr schön erweiterbar
	 */
	public static enum CHECK_RESULT {
		CORRECT(0), CANT_PARSE(1), SYNTAX_ERROR(2);

		/*
		 * Für die Konvertierung zwischen CheckResult und den Codes brauchen wir die einzelnen
		 * Variablen und die untenstehenden Methoden
		 */
		private int returnCode;
		private static Map<Integer, CHECK_RESULT> returnValueToCheckResult = new HashMap<>();

		/*
		 * Da wir das öfter brauchen werden, speichern wir uns die Zuordnung int->checkResult in
		 * einer Map. Die Zuordnung checkResult->int speichert sich jedes checkResult selbst
		 */
		static {
			for (CHECK_RESULT checkResult : CHECK_RESULT.values()) {
				returnValueToCheckResult.put(checkResult.getReturnCode(), checkResult);
			}
		}

		/**
		 * Speichern des returnCodes in privater Variable
		 *
		 * @param returnCode der oben zugewiesene ReturnCode
		 */
		private CHECK_RESULT(int returnCode) {
			this.returnCode = returnCode;
		}

		/**
		 * Konvertierung von CheckResult zu ReturnCode (int)
		 *
		 * @return Der zugeordnete ReturnCode
		 */
		public int getReturnCode() {
			return this.returnCode;
		}

		/**
		 * Konvertierung von ReturnCode (int) zu CheckResult
		 *
		 * @param returnCode Der entsprechende ReturnCode
		 * @return Das dazugehörige CheckResult
		 */
		public static CHECK_RESULT getCheckResultFromReturnCode(int returnCode) {
			CHECK_RESULT checkResult = returnValueToCheckResult.get(returnCode);
			if (checkResult != null) {
				return checkResult;
			} else {
				return CHECK_RESULT.CANT_PARSE;
			}
		}
	}


	/**
	 * Erlaubt den Aufruf von der CommandLine
	 *
	 * @param args Commandline-Argumente. 0 = Name des Settings der Sprache, 1 = Datei
	 */
	public static void main(String[] args) {
		// Wenn Anzahl der Argumente nicht stimmt, beende den Task
		if (args.length < 2) {
			System.exit(1);
		}

		// Vorhandene weitere Argumente sollten Dateien darstellen
		List<Path> files = new ArrayList<>();
		for (int i = 1; i < args.length; i++) {
			files.add(Paths.get(args[i]));
		}

		// Versuche das SprachSetting zu laden
		LanguageSetting languageSetting;
		try {
			languageSetting = (LanguageSetting) Class.forName("de.aima13.whoami.modules" +
					".syntaxcheck.languages" +
					".settings." +
					args[0]).newInstance();


			// Wenn wir hier gelandet sind, sollte alles okay sein und wir können Parsen
			// Auch das Ergebnis des Parsens geben wir als ReturnCode zurück
			for (Path file : files) {
				// Das Ergebnis (ENUM) des Parsens wird auf den Outputstream geschrieben und
				// später ausgelesen
				System.out.println(checkSyntax(languageSetting, file));
			}
			// Sind wir nach dem Methodenafuruf wieder hier gelandet, ist alles gut gegangen und
			// wir können erfolgreich beenden (Returncode 0)
			System.exit(0);

		} catch (Exception e) {
			// Auch hier können wir einfach alle Exceptions abfangen, da der Grund des Fehlers
			// nichts zur Sache tut - wir können die Datei jetzt nicht parsen.
			System.exit(1);
		}
	}


	/**
	 * Diese Methode prüft die syntaktische Korrektheit einer Datei nach der übergebenen Sprache
	 *
	 * @param languageSetting Die Sprache, auf die geprüft werden soll
	 * @param file            Die Datei, die geprüft werden soll
	 * @return ENUM, welches entscheidet, wie der Status der Datei ist
	 */
	private static CHECK_RESULT checkSyntax(LanguageSetting languageSetting, Path file) {
		if (!Files.exists(file)) {
			return CHECK_RESULT.CANT_PARSE;
		}
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

		} catch (Exception e) {
			// Pokemon Exception Handling - Catch them all!
			//
			// Wenn wir was nicht parsen könne, können wir es nicht parsen
			// Hier landen wir nur, wenn etwas schief gegangen ist. Was genau ist eigentlich
			// uninteressant. Wichtig ist: Wir können die Datei nicht parsen. Warum auch immer.
			return CHECK_RESULT.CANT_PARSE;
		}
	}
}
