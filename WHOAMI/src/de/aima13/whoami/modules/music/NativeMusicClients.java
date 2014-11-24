package de.aima13.whoami.modules.music;

import de.aima13.whoami.GuiManager;

import java.nio.file.Path;
import java.util.List;

/**
 * Created by Inga on 24.11.2014.
 */
public class NativeMusicClients {

	///////////////////////////////////////////
	///// Analysiere Musikprogramme //////////
	/////////////////////////////////////////

	/**
	 * Überprüft welche Musikprogramme gefunden wurden und speichert diese global als Liste in der
	 * Variable cltProgram
	 * @return String cltProgram Aufzählung der gefundenen Exe-Dateien aus der White-List in
	 * MY_SEARCH_DELIVERY_NAMES
	 * @param exeFiles
	 */
	public String checkNativeClients(List<Path> exeFiles) {
		String cltProgram = "";
		final String[] MY_SEARCH_DELIVERY_EXES = {"Deezer.exe", "spotify.exe",
				"Amazon Music.exe", "SWYH.exe", "iTunes.exe", "napster.exe", "simfy.exe"};
		final String[] MY_SEARCH_DELIVERY_NAMES = {"Deezer", "Spotify", "Amazon Music",
				"Stream What You Hear", "iTunes", "napster", "simfy"};

		GuiManager.updateProgress("Wie du Musik hörst prüfen wir auch...");
		for (Path currentExe : exeFiles) {
			for(int i = 0; i < MY_SEARCH_DELIVERY_EXES.length; i++) {
				if (currentExe.toString().endsWith(MY_SEARCH_DELIVERY_EXES[i])) {
					if(cltProgram.equals("")){
						cltProgram = MY_SEARCH_DELIVERY_EXES[i];
					}
					else if(!(cltProgram.contains(MY_SEARCH_DELIVERY_NAMES[i]))){
						cltProgram += ", " + MY_SEARCH_DELIVERY_NAMES[i];
					}
				}
			}
		}
	return cltProgram;
	}

}
