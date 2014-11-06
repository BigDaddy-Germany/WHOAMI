package de.aima13.whoami.modules.gamesmod;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.GlobalData;
import de.aima13.whoami.Whoami;
import de.aima13.whoami.support.Utilities;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Spielemodul sucht installierte Spiele, kommentiert diese und liefert Zocker-Score
 * Dies ist die Hauptklasse, welche sich um die Durchführung und Ausgabe kümmert.
 *
 * @author Niko Berkmann
 */
public class Games implements Analyzable {

	private List<Path> exePaths;
	static Path steamAppsPath = null;
	static GameList gameList;
	private GameEntry resultFirstCreatedGame;
	private GameEntry resultLastCreatedGame;
	private GameEntry resultLastModifiedGame;
	static boolean cancelledByTimeLimit = false;
	private GamesComments gamesComments;
	private GameThreshold resultThreshold;

	/**
	 * Datenstruktur für die Kommentar-Ressourcen (Container)
	 */
	private class GamesComments {
		List<GameThreshold> gameThresholds;
		String steamFound;
		int minGamesForDistributorRecommendation;
		String distributorRecommendation;
		String firstCreated;
		String lastModified;
		String lastCreated;
	}

	/**
	 * Datenstruktur für die Kommentar-Ressourcen (Bewertung der Spieleanzahl)
	 */
	private class GameThreshold {
		int limit;
		String comment;
	}

	/**
	 * Spielemodul benötigt eine Liste von Executables
	 *
	 * @return Filterliste
	 */
	@Override
	public List<String> getFilter() {
		List<String> filter = new LinkedList<String>();
		filter.add("**.exe");
		return filter;
	}

	/**
	 * Nimmt Suchergebnisse entgegen und legt Executables getrennt ab
	 *
	 * @param paths Suchergebnisse
	 */
	@Override
	public void setFileInputs(List<Path> paths) {
		//Eingabedateien gleich in eigene Liste kopieren
		exePaths = paths; //:TODO: Bestätigung holen, dass das MEINE LISTE GANZ ALLEIN ist
		for (Path currentPath : exePaths) {
			if (!currentPath.toAbsolutePath().toString().toLowerCase().endsWith(".exe")) {
				throw new RuntimeException("Input passt nicht zu Filter: "
						+ currentPath.toAbsolutePath().toString());
			}
		}
	}

	@Override
	public String getHtml() {
		StringBuilder html = new StringBuilder();

		html.append("Es wurden " + gameList.size() + " Spiele gefunden. "
				+ resultThreshold.comment + " ");

		//Steam kommentieren
		if (steamAppsPath != null) {
			html.append(gamesComments.steamFound);
		} else if (gameList.size() > gamesComments.minGamesForDistributorRecommendation) {
			html.append(gamesComments.distributorRecommendation);
		}
		html.append(" ");

		//Datumsangaben der Executables kommentieren
		if (gameList.size() > 0) {
			if (resultFirstCreatedGame != null) {
				html.append("Spielst du eigentlich noch " + resultFirstCreatedGame.name + "? "
						+ gamesComments.firstCreated + " ");
			}
			if (resultLastModifiedGame != null) {
				html.append("Wie läuft es denn so mit " + resultLastModifiedGame.name + "? "
						+ gamesComments.lastModified + " ");
			}
			if (resultLastCreatedGame != null) {
				html.append("Als letztes wurde anscheinend " + resultLastCreatedGame.name
						+ " installiert. " + gamesComments.lastCreated + " ");
			}
		}

		//Liste weiterer Spiele
		if (gameList.size() >= 5) {
			//erst ab 5, damit abzüglich der eventuellen Duplikate mind. 3 übrig bleiben
			html.append("<table>");
			html.append("<tr><th colspan=\"2\">Auswahl weiterer gefundener Spiele:</th></tr>");
			html.append("<tr><th>Spiel</th><th>Installiert</th></tr>");
			int listed = 0;
			for (GameEntry entry : gameList) {
				if (entry != resultFirstCreatedGame
						&& entry != resultLastCreatedGame
						&& entry != resultLastModifiedGame) {
					html.append("<tr>"
							+ "<td>" + entry.name + "</td>"
							+ "<td>" + new SimpleDateFormat("dd. MM. yyyy").format(
							entry.created) + "</td>"
							+ "</tr>");
					if (++listed >= 10) {
						break; //maximal 10 weitere Spiele anzeigen
					}
				}
			}
			html.append("</table>");
		}
		return html.toString();
	}

	@Override
	public String getReportTitle() {
		return "Freizeitgestaltung";
	}

	@Override
	public String getCsvPrefix() {
		return "Spiele";
	}

	@Override
	public SortedMap<String, String> getCsvContent() {
		TreeMap<String, String> csvContent = new TreeMap();

		csvContent.put("Anzahl", Integer.toString(gameList.size()));
		csvContent.put("ÄltesteInstallation",
				resultFirstCreatedGame == null ? "-" : resultFirstCreatedGame.name);
		csvContent.put("LetzteInstallation",
				resultLastCreatedGame == null ? "-" : resultLastCreatedGame.name);
		csvContent.put("LetztesUpdate",
				resultLastModifiedGame == null ? "-" : resultLastModifiedGame.name);

		return csvContent;
	}

	@Override
	public void run() {
		gameList = new GameList();
		GameCollector collector = new GameCollector();

		for (Path current : exePaths) {
			//Je nach Fund Steam-Bibliothek oder einzelne Programmdatei verarbeiten
			if (current.getFileName().toString().toLowerCase().equals("steam.exe")) {
				collector.processSteamLibrary(current);
			} else {
				collector.processExecutable(current);
			}

			//Timeboxing-Kontrolle
			if (Whoami.getTimeProgress() >= 99) {
				cancelledByTimeLimit = true;
				break;
			}
		}
		//Dem Ergebnis dienliche Abschlussoperationen auch bei Timeboxing-Abbruch durchführen:

		/**
		 * Berechnung des Gaming-Scores:
		 * Durch obere Grenzen definierte Level von 0 bis X werden in einer stückweise linearen
		 * Funktion auf den Bereich 0-100 abgebildet (über 100 falls max. Level überschritten)
		 *
		 * Beispiel: Fünf Level in JSON-Ressource konfiguriert
		 * LVL0 bis    0 Spiele: Score 0
		 * LVL1 bis   10 Spiele: Score 1 - 25
		 * LVL2 bis   50 Spiele: Score     25 - 50
		 * LVL3 bis  150 Spiele: Score          50 - 75
		 * LVL4 bis 1000 Spiele: Score               75 - 100
		 *     über 1000 Spiele: Score                    100 - infinity
		 */
		gamesComments = Utilities.loadDataFromJson
				("/data/Games_Comments.json", GamesComments.class);
		int score;
		int scoreLevel = -1;
		int scoreMaxLevel = gamesComments.gameThresholds.size() - 1;

		do { //Passendes Spiele-Level ermitteln
			resultThreshold = gamesComments.gameThresholds.get(++scoreLevel);
		} while ((gameList.size() > resultThreshold.limit)
				&& (scoreLevel < scoreMaxLevel));

		if (scoreLevel > 0) { //Zugehörige Punkte berechnen
			float band = 100 / scoreMaxLevel;
			score = (int) (band * (scoreLevel - 1));
			int upper = resultThreshold.limit;
			int lower = gamesComments.gameThresholds.get(scoreLevel - 1).limit;
			int bonus = (int) (band * (gameList.size() - lower) / (upper - lower));
			score += bonus;
		} else {
			score = 0;
		}
		GlobalData.getInstance().changeScore("GamingScore", score - 50); //Scoreänderung -50 bis +50


		//Installationsdaten behandeln
		if (gameList.size() > 0) {
			//Zuletzt & zuerst installierte sowie modifizierte Spiele ermitteln
			gameList.sortByLatestModified();
			resultLastModifiedGame = gameList.get(0);
			gameList.sortByLatestCreated();
			resultFirstCreatedGame = gameList.get(gameList.size() - 1);
			resultLastCreatedGame = gameList.get(0);

			//Duplikate löschen, damit nicht mehrere Aussagen zum selben Spiel getroffen werden
			if (resultLastCreatedGame == resultFirstCreatedGame) {
				resultFirstCreatedGame = null;
			}
			if (resultLastModifiedGame == resultLastCreatedGame) {
				resultLastCreatedGame = null;
			}
		}
	}

	/**
	 * :TODO: Hilfsmethode, die es im Release auszumustern gilt
	 */
	static void logthis(String msg) {
		System.out.println(msg);
	}
}
