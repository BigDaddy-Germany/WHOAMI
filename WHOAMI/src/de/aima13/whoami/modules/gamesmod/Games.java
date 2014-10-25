package de.aima13.whoami.modules.gamesmod;

import de.aima13.whoami.Analyzable;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * Spielemodul sucht installierte Spiele, kommentiert diese und liefert Zocker-Score
 *
 * @author Niko Berkmann
 */
public class Games implements Analyzable {


	private List<Path> exePaths;

	private String steamNickname = null;
	private Path steamAppsPath = null;

	private GameList gameList;

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
		return null;
	}

	@Override
	public String getReportTitle() {
		return null;
	}

	@Override
	public String csvPrefix() {
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
				processSteamLibrary(current);
			}
		}

		if (gameList.size() > 0) {
			gameList.sortByLatestCreated();
			logthis("Ah, du hast dir endlich mal " + gameList.get(0).name + " installiert? " +
					"Wurde auch Zeit...");
			gameList.sortByLatestModified();
			logthis("Wie läuft's eigentlich mit " + gameList.get(0).name + "?");
		}
	}

	/**
	 * Verarbeitet gefundene Steam-Bibliothek und veranlasst deren Scan
	 *
	 * @param steamExe Pfad zur Steam-Programmdatei
	 */
	private void processSteamLibrary(Path steamExe) {
		//SteamApps-Verzeichnis extrahieren
		try {
			steamAppsPath = steamExe.getParent().resolve("SteamApps");
		} catch (Exception e) {
		} //Fehler resultieren in später behandeltem Initalwert

		if (steamAppsPath != null) {
			logthis("Aktive Steam-Installation gefunden in "
					+ steamAppsPath.toAbsolutePath().toString());
			Path commonFolder = steamAppsPath.resolve("common");

			try (DirectoryStream<Path> gameFolderStream = Files.newDirectoryStream(commonFolder)) {
				addSteamGames(gameFolderStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			logthis("Steam-Installation scheint inaktiv.");
		}
	}

	/**
	 * Interpretiert alle Ordner als Spieleverzeichnis und fügt sie zur Liste hinzu
	 *
	 * @param gameFolderStream Stream des Verzeichnis Steam/SteamApps/common
	 */
	private void addSteamGames(DirectoryStream<Path> gameFolderStream) {
		String gameName;
		BasicFileAttributes attributes;
		Date create;
		Date modify;

		for (Path gameFolderPath : gameFolderStream) {
			try {
				if (Files.isDirectory(gameFolderPath)) {
					attributes = Files.readAttributes(gameFolderPath, BasicFileAttributes.class);
					create = new Date(attributes.creationTime().to(TimeUnit.MILLISECONDS));
					modify = new Date(attributes.lastModifiedTime().to(TimeUnit.MILLISECONDS));

					gameName = gameFolderPath.getFileName().toString();

					gameList.addUnique(new GameEntry(gameName, create, modify));
				}
			} catch (Exception e) {
			} //Bei Problemen mit einzelnen Ordnern -> komplett überspringen, bewusst ignorieren
		}
	}

	/**
	 * :TODO: Hilfsmethode, die es im Release auszumustern gilt
	 */
	private void logthis(String msg) {
		System.out.println(msg);
	}
}
