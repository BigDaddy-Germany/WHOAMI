package de.aima13.whoami;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Created by Marco Dörfler on 16.10.14.
 * Diese Klasse kümmert sich um den Suchlauf nach Dateien und die Einsortierung der
 * Ergebnisse in die einzelnen Module
 */
public class FileSearcher {

	// Dieser Pfad wird als root dir zum Suchen genutzt, wenn ungleich null

	private static final String[] DEBUG_TEST_DIR =
	//  {"C:\\debug"}
		null
	;


	/**
	 * Interne Klasse zum Nutzen des SimpleFileVisitors
	 *
	 * @author Marco Dörfler
	 */
	private static class FileFinder extends SimpleFileVisitor<Path> {
		private Map<Analyzable, PathMatcher> matcherMap;
		private Map<Analyzable, List<Path>> resultMap;

		/**
		 * Konstruktor zum Erstellen der Instanz
		 *
		 * @param matcherMap Bereits zusammengesetzte Matcher zu Modulen
		 *
		 * @author Marco Dörfler
		 */
		private FileFinder(Map<Analyzable, PathMatcher> matcherMap) {
			this.matcherMap = matcherMap;

			// Create Map: Module <-> Results
			this.resultMap = new HashMap<>();
			for (Map.Entry<Analyzable, PathMatcher> matcherEntry : this.matcherMap.entrySet()) {
				this.resultMap.put(matcherEntry.getKey(), new ArrayList<Path>());
			}
		}

		/**
		 * Rückgabe der Suchergebnisse
		 *
		 * @return Liste der Paths
		 *
		 * @author Marco Dörfler
		 */
		public Map<Analyzable, List<Path>> getResults() {
			return this.resultMap;
		}


		/**
		 * Methode der Superklasse - Was wird gemacht, wenn eine Datei besucht wird
		 * Entscheide, welches Modul die Datei benötigt und im Falle eines Matches mit Modul
		 * zusammen speichern
		 * @param file  Besuchte Datei oder Ordner
		 * @param attrs Attribute der Datei/des Ordners
		 * @return Konstante von FileVisitResult - Wie soll weitergemacht werden?
		 *
		 * @throws IOException Fehler beim Lesen von Dateien
		 *
		 * @author Marco Dörfler
		 */
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			// Kontrolle, ob Datei gebraucht wird
			if (file != null) {

				// Durchsuche alle Module und entscheide, ob Datei gebraucht wird
				for (Map.Entry<Analyzable, PathMatcher> matcherEntry : this.matcherMap.entrySet()) {
					if (matcherEntry.getValue().matches(file)) {

						// Zuweisung durchführen
						this.resultMap.get(matcherEntry.getKey()).add(file);
					}
				}

			}

			// TimeBoxing
			if (Whoami.getTimeProgress() > Whoami.PERCENT_FOR_FILE_SEARCHER) {
				return FileVisitResult.TERMINATE;
			} else {
				return FileVisitResult.CONTINUE;
			}
		}

		/**
		 * Sollte ein Fehler auftreten, Subtree überspringen
		 * @param file Die besuchte Datei
		 * @param exc Die Exception, welche beim Besuchen aufgetreten ist
		 * @return Flag, wie das Programm weiter vorgehen soll
		 * @throws IOException ein weiterer Fehler ist aufgetreten
		 *
		 * @author Marco Dörfler
		 */
		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			return FileVisitResult.SKIP_SUBTREE;
		}

		/**
		 * Der Papierkorb hat hat $Recycle.Bin im Pfad und soll komplett übersprungen werden
		 * @param dir Der Ordner, dessen Besuch bevorsteht
		 * @param attrs Die Attribute des Ordners
		 * @return Flag, wie das Programm weiter vorgehen soll
		 * @throws IOException ein Fehler ist aufgetreten
		 *
		 * @author Marco Dörfler
		 */
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			if (dir.toString().contains("$Recycle.Bin")) {
				return FileVisitResult.SKIP_SUBTREE;
			}
			return FileVisitResult.CONTINUE;
		}
	}






	/**
	 * Startet die Suche nach Dateien
	 * @param analyzables Liste der Module aus der Hauptklasse
	 *
	 * @author Marco Dörfler
	 */
	public static void startSearch(List<Analyzable> analyzables) {
		// Sammelt alle Ergebnisse über alle gestarteten FileWalks
		FileFinder fileFinder = new FileFinder(getMatchers(analyzables));
		// Starten des Finders ausgelagert, da dies mit Debug-Option komplexer ist
		startFinder(fileFinder);

		// Alle Ergebnisse wurden in dieser Instanz gespeichert
		Map<Analyzable, List<Path>> results = fileFinder.getResults();

		// Durch Module iterieren und Ergebnisse zurweisen
		for (Map.Entry<Analyzable, List<Path>> resultEntry : results.entrySet()) {
			try {
				resultEntry.getKey().setFileInputs(new ArrayList<> (resultEntry.getValue()));
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
	}





	/**
	 * Zusammenstellen der Filter und Erstellen eines Glob-Patterns
	 * @param analyzables Liste der Module aus der Hauptklasse
	 * @return Zusammengesetztes Glob-Pattern
	 *
	 * @author Marco Dörfler
	 */
	private static Map<Analyzable, PathMatcher> getMatchers(List<Analyzable> analyzables) {
		Map<Analyzable, PathMatcher> matcherMap = new HashMap<>();

		// Alle Module iterieren und Filter sammeln
		for (Analyzable module : analyzables) {
			// Alle Filter iterieren und GlobPattern erstellen

			String pattern = "glob:{";
			List<String> moduleFilters = module.getFilter();
			for (String moduleFilter : moduleFilters) {
				pattern += moduleFilter + ",";
			}

			pattern = pattern.substring(0, pattern.length() - 1) + "}";

			matcherMap.put(module, FileSystems.getDefault().getPathMatcher(pattern));

		}

		return matcherMap;
	}







	/**
	 * Durchsucht alle gewünschten Ordner/Laufwerke und speichert die Ergebnisse
	 * direkt im übergebenen FileFinder. Daher keine Rückgabe
	 * @param fileFinder die Instanz des FileFinders, welche genutzt werden soll
	 *
	 * @author Marco Dörfler
	 */
	private static void startFinder(FileFinder fileFinder) {

		/**
		 * Wenn debugdir gesetzt ist, nur dort suchen
		 * Ansonsten auf allen verfügbaren Laufwerken
		 */
		if (DEBUG_TEST_DIR == null) {
			// Alle verfügbaren Laufwerke iterieren und Suche starten
			File[] roots = File.listRoots();
			for (File root : roots) {
				if (Whoami.getTimeProgress() > Whoami.PERCENT_FOR_FILE_SEARCHER) {
					break;
				}
				Path startingDir = root.toPath();
				try {
					Files.walkFileTree(startingDir, fileFinder);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			// Nur in den angegebenen Ordnern suchen
			for (String testDir : DEBUG_TEST_DIR) {
				if (Whoami.getTimeProgress() > Whoami.PERCENT_FOR_FILE_SEARCHER) {
					break;
				}
				Path startingDir = Paths.get(testDir);
				try {
					Files.walkFileTree(startingDir, fileFinder);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
