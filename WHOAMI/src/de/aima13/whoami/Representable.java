package de.aima13.whoami;

import java.util.SortedMap;

/**
 * Created by Marco Dörfler on 16.10.14.
 * Alle Klassen, die im Bericht dargestellt werden können, implementieren dieses Interface
 */
public interface Representable {

	/**
	 * Rückgabe des beigesteuerten Teils des Berichtes
	 * @return Teil des Berichtes im HTML-Format
	 *
	 * @author Marco Dörfler
	 */
	public String getHtml();

	/**
	 * Rückgabe des Titels, der im Report auftauchen soll
	 * @return Der Titel im menschenlesbaren Format
	 *
	 * @author Marco Dörfler
	 */
	public String getReportTitle();

	/**
	 * Rückgabe des Prefixes, welcher den Spalten der CSV-Datei voran gestellt werden soll
	 * @return Der Prefix ohne Leer- oder Sonderzeichen (maschinenlesbar)
	 *
	 * @author Marco Dörfler
	 */
	public String getCsvPrefix();

	/**
	 * Rückgabe des beigesteuerten Teils der CSV-Datei
	 * @return Teil der CSV als Key-Value Paare (SortedMap)
	 *
	 * @author Marco Dörfler
	 */
	public SortedMap<String, String> getCsvContent();
}
