package de.aima13.whoami;

import java.util.SortedMap;

/**
 * Created by D060469 on 16.10.14.
 * Alle Klassen, die im Bericht dargestellt werden können, implementieren dieses Interface
 */
public interface Representable {

	/**
	 * Rückgabe des beigesteuerten Teils des Berichtes
	 * @return Teil des Berichtes im HTML-Format
	 */
	public String getHtml();

	/**
	 * Rückgabe des beigesteuerten Teils der CSV-Datei
	 * @return Teil der CSV als Key-Value Paare (SortedMap)
	 */
	public SortedMap<String, String> getCsvContent();
}
