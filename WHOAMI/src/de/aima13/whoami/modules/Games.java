package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.GuiManager;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Spielemodul sucht installierte Spiele, kommentiert diese und liefert Zocker-Score
 *
 * @author Niko Berkmann
 */
public class Games implements Analyzable {

	/**
	 * Datenstruktur "Spiel"
	 */
	private static class GameEntry {
		public String name;
		public Date installed;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Date getInstalled() {
			return installed;
		}

		public void setInstalled(Date installed) {
			this.installed = installed;
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
		 * Sortiert Spieleliste nach Installationsdatum
		 */
		public void sortByLatestInstall() {
			Collections.sort(this, new Comparator<GameEntry>() {
				@Override
				public int compare(GameEntry o1, GameEntry o2) {
					return o2.getInstalled().compareTo(o1.getInstalled());
				}
			});
		}
	}

	private LinkedList<File> exeFiles;

	private String steamNickname = null;
	private File steamAppsFolder = null;

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
			if (currentFile.isFile() && currentFile.getAbsolutePath().endsWith(".exe")) {
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
		for (File current : exeFiles) {
			if (current.getName().toLowerCase() == "steam.exe") {
			}
		}
	}

	private void processSteamLibrary(File steamExe) {
		//SteamApps-Verzeichnis extrahieren
		try {
			steamAppsFolder = steamExe.getCanonicalFile().getParentFile();
		} catch (IOException e) {
			GuiManager.updateProgress("Fehlerhaftes Steam-Verzeichnis entdeckt.");
		}


		if (steamAppsFolder == null) {
			GuiManager.updateProgress("Keine Steam-Installation gefunden.");
		} else {
			GuiManager.updateProgress("Aktive Steam-Installation gefunden in "
					                  + steamAppsFolder.getAbsolutePath());
		}
	}
}
