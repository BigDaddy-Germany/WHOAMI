package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.support.DataSourceManager;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Marvin on 16.10.14.
 *
 * @author Marvin Klose
 * @version 1.0
 */
public class TopFive implements Analyzable {
	private List<File> browserDatabases = new ArrayList<File>();
	private SortedMap<String, Integer> results = new TreeMap<String, Integer>();

	public TopFive() {

	}

	/**
	 * Methode spezifiziert die sqlite Datenbank die für uns von Interesse sind.
	 *
	 * @return Liste von Datenbankfiles die bekannterweise zu Firefox oder Chrome gehören.
	 */
	@Override
	public List<String> getFilter() {
		List myFileList = new ArrayList<String>();
		//places.sql gehört zu Firefox
		myFileList.add("places.sqlite");
		//* hier weil History Datein gibt es zu viele und Chrome kann mehrere Benutzer verwalten
		myFileList.add("Google\\Chrome\\User Data\\*\\History");
		return myFileList;
	}

	@Override
	public void setFileInputs(List<File> files) throws Exception {
		browserDatabases = files;
	}

	/**
	 * @return String der das Ergebnis der 5 am meisten besuchtesten Webseiten in einer
	 * HTML Tabelle darstellt.
	 *
	 * @author Marvin Klose
	 */
	@Override
	public String getHtml() {
		boolean resultExists = false;
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("<table\n" +
				"  <tr>" +
				"    <th>Webseite</th>" +
				"    <th>Aufrufe</th>" +
				"  </tr>");
		for (int i = 0; i < 5; i++) {
			try {
				Map.Entry<String, Integer> highestEntry = getHighestEntry();
				//Formatiere genau eine Zeile
				stringBuffer.append(String.format("<tr>" +
								"<td>%s</td>" +
								"<td>%s</td>" +
								"</tr>",
						highestEntry.getKey(), highestEntry.getValue().toString()));

				results.remove(highestEntry.getKey());
				resultExists = true;
			} catch (NoSuchElementException e) {

			}
		}
		if (resultExists) {
			return stringBuffer.append("</table>").toString();

		}
		else {
			return "<b>Leider lieferte das Modul der TOP5 Webseiten keinerlei Ergebnisse! Du " +
					"scheinst deine Spuren gut zu verwischen!</b>";
		}
	}

	/**
	 * Methode iteriert über die Ergebnisse in der TreeMap. Und liefert den maximalen Wert der
	 * TreeMap.
	 *
	 * @return Ergebnis ist der Eintrag der die meisten Klicks im Browser bekommen hat.
	 *
	 * @throws NoSuchElementException Sollte kein Element gefunden werden gibt auch kein Entry
	 *                                der am höchsten ist.
	 */
	private Map.Entry<String, Integer> getHighestEntry() throws NoSuchElementException {
		int maxValue = -1;
		Map.Entry<String, Integer> highestEntry = null;
		for (Map.Entry<String, Integer> entry : results.entrySet()) {
			if (entry.getValue() > maxValue) {
				highestEntry = entry;
				maxValue = highestEntry.getValue();
			}
		}
		if (null == highestEntry) {
			throw new NoSuchElementException("Highest Value was not found");
		}
		else {
			return highestEntry;
		}

	}

	@Override
	public SortedMap<String, String> getCsvContent() {

		return null;
	}

	/**
	 * run() implementiert den eigentlichen Analyseablauf. Dies gilt für alle gefunden Datenbanken
	 * die zu einem der jeweiligen unterstützen Browser(Firefox,Chrome) gehören. Die Ergebnisse
	 * werden vereinigt. Da bereits zwischen den Spaltennamen Gleichheit herscht.
	 */
	@Override
	public void run() {
		for (File db : browserDatabases) {
			try {
				ResultSet mostVisted = analyzeBrowserHistory(db);
				while (mostVisted.next() && mostVisted != null) {

					int visitCount = -1;
					visitCount = mostVisted.getInt("visit_count");
					String urlName = mostVisted.getString("url");
					if (results.containsKey(urlName) && results.get(urlName) < visitCount) {
						results.put(urlName, visitCount);
					}
					else {
						results.put(urlName, visitCount);
					}
				}
			} catch (SQLException e) {
			}
		}
	}

	/**
	 * Methode baut Verbindung zur jeweiligen Datenbank auf und schickt danach die Query ab.
	 * Die Ergebnisse sind auf 5 limitiert um an Performance zu gewinnnen. Am Ende wird die
	 * Verbindung getrennt und die Datenbank wieder freigegeben.
	 *
	 * @param sqliteDb File zur sqlite Datenbank.
	 * @return Ergebnisse der jeweiligen Abfrage für Firefox oder Chrome oder null.
	 *
	 * @throws SQLException Fehler bei der Ausführung von SQL Statements.
	 */
	private ResultSet analyzeBrowserHistory(File sqliteDb) throws SQLException {
		DataSourceManager dbManager = new DataSourceManager(sqliteDb);
		if (dbManager != null) {
			if (sqliteDb.getAbsolutePath().contains("Mozilla")) {
				return dbManager.querySqlStatement("SELECT moz_places.visit_count, " +
						"moz_places.url " +
						"FROM moz_places " +
						"ORDER by visit_count DESC " +
						"LIMIT 5;");
			}
			else if (sqliteDb.getAbsolutePath().contains("Chrome")) {
				return dbManager.querySqlStatement("SELECT urls.visit_count, urls.url, " +
						"FROM urls" +
						"ORDER by visit_count DESC " +
						"LIMIT 5;");
			}
		}
		dbManager.closeConnection();
		return null;
	}
}



