package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;

/**
 *
 */
public class Games implements Analyzable {
	private List<File> exeFiles;
	private File steamAppsFolder;

	/**
	 * Spielemodul fragt nach einer Liste von Executables und benötigt den Pfad eines
	 * gegebenenfalls vorhandenen "SteamApps"-Ordners
	 *
	 * @return Filterliste
	 */
	@Override
	public List<String> getFilter() {
		List<String> filter = new LinkedList<String>();
		filter.add("*.exe");
		filter.add("SteamApps");
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
			}
			else if (currentFile.isDirectory() && currentFile.getName() == "SteamApps") {
				steamAppsFolder = currentFile;
			}
			else {
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

	}
}
