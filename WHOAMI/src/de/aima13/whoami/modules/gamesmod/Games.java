package de.aima13.whoami.modules.gamesmod;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.GlobalData;
import de.aima13.whoami.Whoami;
import de.aima13.whoami.support.Utilities;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;

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
	 * Spielemodul fragt nach einer Liste von Executables und benötigt den Pfad eines
	 * gegebenenfalls vorhandenen "SteamApps"-Ordners
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

		int count = gameList.size();
		GamesComments gamesComments = Utilities.loadDataFromJson
				("/data/Games_Comments.json", GamesComments.class);


		//Anzahl Spiele mit Kommentar dazu und GamingScore
		int score = 0;
		int scoreLevel = -1;
		final int scoreMaxLevel = gamesComments.gameThresholds.size() - 1;

		GameThreshold threshold;
		do { //Passendes Spiele-Level ermitteln
			threshold = gamesComments.gameThresholds.get(++scoreLevel);
		} while ((gameList.size() > threshold.limit)
				&& (scoreLevel < scoreMaxLevel));

		float band = 100 / (scoreMaxLevel + 1);
		score = (int) (band * scoreLevel);
		if (scoreLevel > 0) {
			int upper = threshold.limit;
			int lower = gamesComments.gameThresholds.get(scoreLevel - 1).limit;
			int bonus = (int) (band * (gameList.size() - lower) / (upper - lower));
			score += bonus;
		}
		GlobalData.getInstance().changeScore("GamingScore", -50 + score); //-50 to adjust to 0-100

		html.append("Es wurden " + gameList.size() + " Spiele gefunden. "
				+ threshold.comment + " ");

		//Steam kommentieren
		if (steamAppsPath != null) {
			html.append(gamesComments.steamFound);
		} else if (count > gamesComments.minGamesForDistributorRecommendation) {
			html.append(gamesComments.distributorRecommendation);
		}
		html.append(" ");

		//Datumsangaben der Executables kommentieren
		if (gameList.size() > 0) {
			html.append("Spielst du eigentlich noch " + resultFirstCreatedGame.name + "? " +
					"" + gamesComments.firstCreated + " Wie läuft es denn so mit " +
					"" + resultLastModifiedGame.name + "? " + gamesComments.lastModified + " Als letztes " +
					"wurde anscheinend " + resultLastCreatedGame.name + " installiert. " +
					"" + gamesComments.lastCreated);
		}
		return html.toString();
	}

	@Override
	public String getReportTitle() {
		return null;
	}

	@Override
	public String getCsvPrefix() {
		return null;
	}

	@Override
	public SortedMap<String, String> getCsvContent() {
		return null;
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

		//Dem Ergebnis dienliche Abschlussoperationen auch bei Timeboxing-Abbruch durchführen
		if (gameList.size() > 0) {
			gameList.sortByLatestCreated();
			resultFirstCreatedGame = gameList.get(gameList.size() - 1);
			resultLastCreatedGame = gameList.get(0);
			gameList.sortByLatestModified();
			resultLastModifiedGame = gameList.get(0);
		}
	}

	/**
	 * :TODO: Hilfsmethode, die es im Release auszumustern gilt
	 */
	static void logthis(String msg) {
		System.out.println(msg);
	}
}
