package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

		public GameEntry(String name, Date installed, Date modified){
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
			return this.add(game);
		}

		/**
		 * Sortiert Spieleliste nach "Installationsdatum"
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

	private LinkedList<File> exeFiles;

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
	 * @param files Suchergebnisse
	 */
	@Override
	public void setFileInputs(List<File> files) {
		exeFiles = new LinkedList<>();
		for (File currentFile : files) {
			if (currentFile.isFile() && currentFile.getAbsolutePath().toLowerCase().endsWith(".exe")) {
				exeFiles.add(currentFile);
			} else {
				throw new RuntimeException("Input passt nicht zu Filter: "
						+ currentFile.getAbsolutePath());
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
		for (File current : exeFiles) {
			if (current.getName().toLowerCase().equals("steam.exe")) {
				processSteamLibrary(current);
			}
		}

		gameList.sortByLatestCreated();
		logthis("Ah, du hast dir endlich mal " + gameList.get(0).name + " installiert? Wurde auch " +
				"Zeit...");
		gameList.sortByLatestModified();
		logthis("Wie läuft's eigentlich mit "+gameList.get(0).name+"?");
	}

	private void processSteamLibrary(File steamExe) {
		//SteamApps-Verzeichnis extrahieren
		try {
			steamAppsPath = Paths.get(steamExe.getParentFile().getAbsolutePath()).resolve("SteamApps");
		} catch (Exception e) {
		} //Fehler resultieren in später behandeltem Initalwert

		if (steamAppsPath != null) {
			logthis("Aktive Steam-Installation gefunden in "
					+ steamAppsPath.toFile().getAbsolutePath());
			Path commonFolder = steamAppsPath.resolve("common");
			try (DirectoryStream<Path> gameFolderStream = Files.newDirectoryStream(commonFolder)) {
				for (Path gameFolderPath : gameFolderStream) {
					File gameFolder = gameFolderPath.toFile();
					if (gameFolder.isDirectory()) {
						BasicFileAttributes folderAttributes = Files.readAttributes
								(gameFolderPath, BasicFileAttributes.class);
						Date createDate = new Date(folderAttributes.creationTime()
								.to(TimeUnit.MILLISECONDS));
						Date modifyDate = new Date(gameFolder.lastModified());
						gameList.add(new GameEntry(gameFolder.getName(), createDate, modifyDate));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void logthis(String msg){
		System.out.println(msg);
	}
}
