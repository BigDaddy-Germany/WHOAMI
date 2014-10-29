package de.aima13.whoami.modules.gamesmod;

import de.aima13.whoami.Analyzable;
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

	class GamesComments {
		List<GameThreshold> gameThresholds;
		String steamFound;
		int minGamesForDistributorRecommendation;
		String distributorRecommendation;
		String firstCreated;
		String lastModified;
		String lastCreated;
	}

	class GameThreshold {
		int limit;
		String comment;
	}

	@Override
	public String getHtml() {
		StringBuilder html = new StringBuilder();

		int count = gameList.size();
		GamesComments gamesComments = Utilities.loadDataFromJson
				("/data/Games_Comments.json", GamesComments.class);


		//Anzahl Spiele mit Kommentar dazu
		int i = 0;
		GameThreshold threshold;
		do {
			threshold = gamesComments.gameThresholds.get(i++);
		} while (gameList.size() > threshold.limit);

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
		for (Path current : exePaths) {
			//Haben wir die Steam-Executable gefunden?
			if (current.getFileName().toString().toLowerCase().equals("steam.exe")) {
				GameCollector.processSteamLibrary(current);
			}

			if (Whoami.getTimeProgress() >= 99) {
				//Ausstieg wegen Timeboxing
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
