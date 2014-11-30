package de.aima13.whoami;

import de.aima13.whoami.support.Utilities;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Diese Klasse kümmert sich um den Suchlauf nach Dateien und die Einsortierung der
 * Ergebnisse in die einzelnen Module
 *
 * @author Marco Dörfler
 */
public class FileSearcher {

	// Dieser Pfad wird als root dir zum Suchen genutzt, wenn ungleich null

	private static final String[] DEBUG_TEST_DIR = null
			//		{"hier kann dein Pfad stehen"}
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
		 */
		public Map<Analyzable, List<Path>> getResults() {
			return this.resultMap;
		}


		/**
		 * Methode der Superklasse - Was wird gemacht, wenn eine Datei besucht wird
		 * Entscheide, welches Modul die Datei benötigt und im Falle eines Matches mit Modul
		 * zusammen speichern
		 *
		 * @param file  Besuchte Datei oder Ordner
		 * @param attrs Attribute der Datei/des Ordners
		 * @return Konstante von FileVisitResult - Wie soll weitergemacht werden?
		 *
		 * @throws IOException Fehler beim Lesen von Dateien
		 */
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			// Kontrolle, ob Datei gebraucht wird
			if (file != null) {
				// Kommentierung des Pfades unabhängig davon, ob er "interessant" für ein Modul
				// ist
				commentCurrentPath(file);
				// Durchsuche alle Module und entscheide, ob Datei gebraucht wird
				for (Map.Entry<Analyzable, PathMatcher> matcherEntry : this.matcherMap.entrySet()) {
					if (matcherEntry.getValue().matches(file)) {

						// Zuweisung durchführen
						this.resultMap.get(matcherEntry.getKey()).add(file);

						// Besondere Kommentierung im Falle eines Fundes
						// Die letzte Zahl hoch schrauben, um weniger Ausgaben zu erhalten
						int randomNumber = Utilities.getRandomIntBetween(1, 4000);
						switch (randomNumber) {
							case 2:
								GuiManager.updateProgress("Nachher sehe ich mir " + file
										.getFileName() + " mal genauer an.");
								break;

							case 3:
								GuiManager.updateProgress("Memo an mich: " + file.getFileName() +
										" genauer ansehen.");
								break;

							case 4:
								GuiManager.updateProgress(file.getFileName() + " scheint wohl " +
										"von Interesse zu sein...");
								break;

							case 5:
								GuiManager.updateProgress("Schnüffele in " + file.getFileName());
								break;
						}
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
		 *
		 * @param file Die besuchte Datei
		 * @param exc  Die Exception, welche beim Besuchen aufgetreten ist
		 * @return Flag, wie das Programm weiter vorgehen soll
		 *
		 * @throws IOException ein weiterer Fehler ist aufgetreten
		 */
		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			return FileVisitResult.SKIP_SUBTREE;
		}

		/**
		 * Der Papierkorb hat hat $Recycle.Bin im Pfad und soll komplett übersprungen werden
		 *
		 * @param dir   Der Ordner, dessen Besuch bevorsteht
		 * @param attrs Die Attribute des Ordners
		 * @return Flag, wie das Programm weiter vorgehen soll
		 *
		 * @throws IOException ein Fehler ist aufgetreten
		 */
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			if (dir.toString().toLowerCase().contains("$recycle.bin")) {
				return FileVisitResult.SKIP_SUBTREE;
			}
			return FileVisitResult.CONTINUE;
		}


		// Die FireFox-Meldung sollte nicht zu oft kommen
		static int countFirefox = 0;

		private void commentCurrentPath(Path file) {
			if (file.toString().endsWith("places.sqlite")) {
				countFirefox++;
				switch (countFirefox) {
					case 1:
						GuiManager.updateProgress("Yummy! " + FileUtils.byteCountToDisplaySize
								(file.toFile().length()) + " FireFox Daten!");
						break;

					case 2:
						GuiManager.updateProgress("Ahh was haben wir denn hier? Nochmal " +
								FileUtils.byteCountToDisplaySize(file.toFile().length()) + " " +
								"FireFox Daten!");
						break;
				}
			}
		}
	}


	/**
	 * Startet die Suche nach Dateien
	 *
	 * @param analyzables Liste der Module aus der Hauptklasse
	 */
	public static void startSearch(List<Analyzable> analyzables) {
		// Sammelt alle Ergebnisse über alle gestarteten FileWalks
		GuiManager.updateProgress("File Patterns werden geladen...");
		FileFinder fileFinder = new FileFinder(getMatchers(analyzables));

		// Starten des Finders ausgelagert, da dies mit Debug-Option komplexer ist
		startFinder(fileFinder);

		// Alle Ergebnisse wurden in dieser Instanz gespeichert
		Map<Analyzable, List<Path>> results = fileFinder.getResults();

		// Durch Module iterieren und Ergebnisse zurweisen
		GuiManager.updateProgress("Datenpakete werden geschnürt...");
		for (Map.Entry<Analyzable, List<Path>> resultEntry : results.entrySet()) {
			try {
				resultEntry.getKey().setFileInputs(new ArrayList<>(resultEntry.getValue()));
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
	}


	/**
	 * Zusammenstellen der Filter und Erstellen eines Glob-Patterns
	 *
	 * @param analyzables Liste der Module aus der Hauptklasse
	 * @return Zusammengesetztes Glob-Pattern
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
		GuiManager.updateProgress("Alle Matcher hochgefahren!");

		return matcherMap;
	}


	/**
	 * Durchsucht alle gewünschten Ordner/Laufwerke und speichert die Ergebnisse
	 * direkt im übergebenen FileFinder. Daher keine Rückgabe
	 *
	 * @param fileFinder die Instanz des FileFinders, welche genutzt werden soll
	 */
	private static void startFinder(FileFinder fileFinder) {

		/*
		 * Wenn debugdir gesetzt ist, nur dort suchen
		 * Ansonsten auf allen verfügbaren Laufwerken
		 */
		if (DEBUG_TEST_DIR == null) {
			// Alle verfügbaren Laufwerke iterieren und Suche starten
			File[] roots = File.listRoots();
			for (File root : roots) {
				GuiManager.updateProgress(root.toString() + " wird durchsucht...");
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
			GuiManager.updateProgress("Durchsuche die DebugOption Ordner");
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
