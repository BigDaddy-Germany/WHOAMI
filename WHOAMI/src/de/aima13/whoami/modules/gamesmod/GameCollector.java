package de.aima13.whoami.modules.gamesmod;

import de.aima13.whoami.Whoami;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Der GameCollector verarbeitet EXEs und die Steam-Bibliothek und fügt diese zur Spieleliste hinzu
 *
 * @author Niko Berkmann
 */
class GameCollector {

	/**
	 * Verarbeitet gefundene Steam-Bibliothek und veranlasst deren Scan
	 *
	 * @param steamExe Pfad zur Steam-Programmdatei
	 */
	static void processSteamLibrary(Path steamExe) {
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
	 * Interpretiert alle Ordner als Spieleverzeichnis und fügt sie zur Liste hinzu
	 *
	 * @param gameFolderStream Stream des Verzeichnis Steam/SteamApps/common
	 */
	private static void addSteamGames(DirectoryStream<Path> gameFolderStream) {
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

					Games.gameList.addUnique(new GameEntry(gameName, create, modify));
				}
			} catch (Exception e) {
			} //Bei Problemen mit einzelnen Ordnern -> komplett überspringen, bewusst ignorieren

			if (Whoami.getTimeProgress() >= 99) {
				//Ausstieg wegen Timeboxing
				Games.cancelledByTimeLimit = true;
				return;
			}
		}
	}


}
