package de.aima13.whoami.modules.gamesmod;

import de.aima13.whoami.Whoami;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static de.aima13.whoami.support.Utilities.loadDataFromJson;

/**
 * Der GameCollector verarbeitet EXEs und die Steam-Bibliothek und fügt diese zur Spieleliste hinzu
 *
 * @author Niko Berkmann
 */
class GameCollector {

	private GameDatabaseEntry[] gameDatabase;

	/**
	 * Initialisiert einen GameCollector und lädt dabei die Spieledatenbank aus den Ressourcen
	 */
	GameCollector() {
		gameDatabase = loadDataFromJson("/data/Games_Database.json", GameDatabaseEntry[].class);
	}

	private class GameDatabaseEntry {
		String file;
		String name;
	}

	/**
	 * Verarbeitet gefundene Steam-Bibliothek und veranlasst deren Scan
	 *
	 * @param steamExe Pfad zur Steam-Programmdatei
	 */
	void processSteamLibrary(Path steamExe) {
		//SteamApps-Verzeichnis extrahieren
		try {
			Games.steamAppsPath = steamExe.getParent().resolve("SteamApps");
		} catch (Exception e) {
		} //Fehler resultieren in später behandeltem Initalwert

		if (Games.steamAppsPath != null) {
			Games.logthis("Aktive Steam-Installation gefunden in "
					+ Games.steamAppsPath.toAbsolutePath().toString());
			Path commonFolder = Games.steamAppsPath.resolve("common");

			try (DirectoryStream<Path> gameFolderStream = Files.newDirectoryStream(commonFolder)) {
				addSteamGames(gameFolderStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Games.logthis("Steam-Installation scheint inaktiv.");
		}
	}

	/**
	 * Liest Zeitattribute des Dateisystems aus und fügt das Spiel zur Sammelliste hinzu
	 *
	 * @param gameName Name des Spieles
	 * @param gamePath Pfad zu Ordner oder ausführbarer Datei
	 * @throws IOException (Pfad ungültig oder Zugriffsfehler)
	 */
	private void addGame(String gameName, Path gamePath) throws IOException {
		BasicFileAttributes attributes;
		Date create;
		Date modify;

		attributes = Files.readAttributes(gamePath, BasicFileAttributes.class);

		create = new Date(attributes.creationTime().to(TimeUnit.MILLISECONDS));
		modify = new Date(attributes.lastModifiedTime().to(TimeUnit.MILLISECONDS));

		Games.gameList.addUnique(new GameEntry(gameName, create, modify));
	}

	/**
	 * Interpretiert alle Ordner als Spieleverzeichnis und fügt sie zur Liste hinzu
	 *
	 * @param gameFolderStream Stream des Verzeichnis Steam/SteamApps/common
	 */
	private void addSteamGames(DirectoryStream<Path> gameFolderStream) {
		String gameName;

		for (Path gameFolderPath : gameFolderStream) {
			try {
				if (Files.isDirectory(gameFolderPath)) {
					gameName = gameFolderPath.getFileName().toString();

					if (!gameName.contains("SDK") && !gameName.toLowerCase().contains("openvr")) {
						addGame(gameName, gameFolderPath);
					}
				}
			} catch (Exception e) {
			} //Zugriffsprobleme bewusst ignorieren

			if (Whoami.getTimeProgress() >= 99) {
				//Ausstieg wegen Timeboxing
				Games.cancelledByTimeLimit = true;
				return;
			}
		}
	}

	/**
	 * Verarbeitet gefundene ausführbare Datei und fügt sie zur Spieleliste hinzu,
	 * falls sie in der Datenbasis bekannter Spiele gefunden wird
	 *
	 * @param gameExecutable Pfad zur Datei
	 */
	void processExecutable(Path gameExecutable) {
		String filename = gameExecutable.getFileName().toString().toLowerCase();

		for (GameDatabaseEntry entry : gameDatabase) {
			if (filename.equals(entry.file)) {
				try {
					addGame(entry.name, gameExecutable);
				} catch (IOException e) {
				} //Zugriffsprobleme bewusst ignorieren
				break;
			}
		}
	}
}
