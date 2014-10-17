package de.aima13.whoami;

import java.util.SortedMap;

/**
 * Created by D060469 on 16.10.14.
 */
public class GlobalData implements Representable {

	/**
	 * @todo Global Data muss Thrad-safe sein
	 */

	// Speicherung der Instanz für Singleton Klasse
	private static GlobalData instance;

	@Override
	public String getHtml() {

		return null;
	}

	@Override
	public SortedMap<String, String> getCsvContent() {

		return null;
	}


	// Konstruktor privat, Klasse ist Singleton
	private GlobalData() {

	}

	// getInstance für Singleton-Klasse
	public GlobalData getInstance() {
		if (instance == null) {
			instance = new GlobalData();
		}
		return instance;
	}

	public void proposeData(String key, String value) {

	}

	public void changeScore(String key, int value) {

	}
}
