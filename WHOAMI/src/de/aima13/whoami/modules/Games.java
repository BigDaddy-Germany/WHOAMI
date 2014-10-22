package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;
import org.apache.commons.lang3.text.WordUtils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Spielemodul sucht installierte Spiele, kommentiert diese und liefert Zocker-Score
 *
 * @author Niko Berkmann
 */
public class Games implements Analyzable {

	/**
	 * Datenstruktur "Spiel"
	 */
	private class GameEntry {
		public String name;
		public Date created;
		public Date modified;

		public GameEntry(String name, Date installed, Date modified) {
			this.name = name;
			this.created = installed;
			this.modified = modified;
		}
	}

	/**
	 * Spieleliste hält einzigartige Einträge von Spielen, ggf. nach Installationszeitpunkt sortiert
	 */
	private class GameList extends ArrayList<GameEntry> {
		/**
		 * Spiel nach Duplikatscheck hinzufügen
		 *
		 * @param game Neues Spiel
		 * @return Tatsächlich hinzugefügt?
		 */
		public boolean addUnique(GameEntry game) {
			//Falls Spieleordner in Kleinschreibung sind, Wortanfänge groß machen
			if (game.name.toLowerCase().equals(game.name)) {
				game.name = WordUtils.capitalize(game.name);
			}
			return this.add(game);
		}

		/**
		 * Sortiert Spieleliste nach Installationsdatum
		 */
		public void sortByLatestCreated() {
			Collections.sort(this, new Comparator<GameEntry>() {
				@Override
				public int compare(GameEntry o1, GameEntry o2) {
					return o2.created.compareTo(o1.created);
				}
			});
		}

		/**
		 * Sortiert Spieleliste nach Veränderungsdatum
		 */
		public void sortByLatestModified() {
			Collections.sort(this, new Comparator<GameEntry>() {
				@Override
				public int compare(GameEntry o1, GameEntry o2) {
					return o2.modified.compareTo(o1.modified);
				}
			});
		}
	}

	private LinkedList<Path> exePaths;

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
	 * Nimmt Suchergebnisse entgegen und legt Executables und SteamApps-Ordner gleich getrennt ab
	 *
	 * @param paths Suchergebnisse
	 */
	@Override
	public void setFileInputs(List<Path> paths) {
		//Eingabedateien gleich in eigene Liste kopieren
		exePaths = new LinkedList<>();
		for (Path currentPath : paths) {
			if (currentPath.toAbsolutePath().toString().toLowerCase().endsWith(".exe")) {
				exePaths.add(currentPath);
			} else {
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
