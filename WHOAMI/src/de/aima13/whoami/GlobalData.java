package de.aima13.whoami;

import java.util.SortedMap;

/**
 * Created by D060469 on 16.10.14.
 */
public class GlobalData implements Representable {

	/**
	 * @todo Global Data muss Thread-safe sein
	 */

	/**
	 * Instanz der Singleton-Klasse
	 */
	private static GlobalData instance;

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


	/**
	 * Privater Konstruktor, da Singleton
	 */
	private GlobalData() {

	}

	/**
	 * Erlangen der Singletoninstanz der Klasse
	 * @return Instanz der Singleton Klasse
	 */
	public static GlobalData getInstance() {
		if (instance == null) {
			instance = new GlobalData();
		}
		return instance;
	}

	/**
	 * Vorschlagen von persönlichen Daten
	 * @param key Key des Datensatzes (z.B. "Name")
	 * @param value Wert des Datensatzes (z.B. der Name des Nutzers)
	 */
	public void proposeData(String key, String value) {

	}

	/**
	 * Verändern eines globalen Scores
	 * @param key Key des Scores
	 * @param value Wert, um welchen erhöht oder erniedrigt werden soll
	 */
	public void changeScore(String key, int value) {

	}
}
