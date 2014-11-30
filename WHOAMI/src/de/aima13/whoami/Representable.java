package de.aima13.whoami;

import java.util.SortedMap;

/**
 * Alle Klassen, die im Bericht dargestellt werden können, implementieren dieses Interface
 *
 * @author Marco Dörfler
 */
public interface Representable {

	/**
	 * Rückgabe des beigesteuerten Teils des Berichtes
	 *
	 * @return Teil des Berichtes im HTML-Format
	 */
	public String getHtml();

	/**
	 * Rückgabe des Titels, der im Report auftauchen soll
	 *
	 * @return Der Titel im menschenlesbaren Format
	 */
	public String getReportTitle();

	/**
	 * Rückgabe des Prefixes, welcher den Spalten der CSV-Datei voran gestellt werden soll
	 *
	 * @return Der Prefix ohne Leer- oder Sonderzeichen (maschinenlesbar)
	 */
	public String getCsvPrefix();

	/**
	 * Funktion zur Anmeldung des Moduls. Hier muss es die Header später zurückgegebenen
	 * CSV-Spalten angeben. Nicht angegebene Spalten werden später ignoriert. Zu viel angegebene
	 * Spalten werden mit einem Platzhalter gefüllt.
	 *
	 * @return Array der Namen der Header-Spalten
	 */
	public String[] getCsvHeaders();

	/**
	 * Rückgabe des beigesteuerten Teils der CSV-Datei
	 *
	 * @return Teil der CSV als Key-Value Paare (SortedMap)
	 */
	public SortedMap<String, String> getCsvContent();
}
