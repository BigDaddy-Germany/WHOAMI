package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.support.DataSourceManager;

import java.io.File;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.nio.file.Path;

/**
 * Created by Marvin on 16.10.14.
 *
 * @author Marvin Klose
 * @version 1.0
 */
public class TopFive implements Analyzable {
	private List<Path> browserDatabases = new ArrayList<Path>();
	private SortedMap<String, Integer> results = new TreeMap<String, Integer>();
	private SortedMap<String, String> csvOutput = new TreeMap<String, String>();
	private String htmlOutput = "";


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
		myFileList.add("**Firefox**places.sqlite");

		//* hier weil History Datein gibt es zu viele und Chrome kann mehrere Benutzer verwalten
		myFileList.add("**Google/Chrome**History");

		return myFileList;
	}

	@Override
	public void setFileInputs(List<Path> files) throws IllegalArgumentException {
		if (files != null && !files.isEmpty()) {
			browserDatabases = files;
		}
		else {
			throw new IllegalArgumentException("No sqlite Database specified");
		}
	}

	/**
	 * @return String der das Ergebnis der 5 am meisten besuchtesten Webseiten in einer
	 * HTML Tabelle darstellt.
	 *
	 * @author Marvin Klose
	 */
	@Override
	public String getHtml() {
		return htmlOutput;
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
		return csvOutput;
	}

	/**
	 * run() implementiert den eigentlichen Analyseablauf. Dies gilt für alle gefunden Datenbanken
	 * die zu einem der jeweiligen unterstützen Browser(Firefox,Chrome) gehören. Die Ergebnisse
	 * werden vereinigt. Da bereits zwischen den Spaltennamen Gleichheit herscht.
	 */
	@Override
	public void run() {
		for (Path db : browserDatabases) {
			try {
				ResultSet mostVisted = analyzeBrowserHistory(db);
				while (mostVisted.next()) {
					int visitCount = -1;
					String urlName = "";

					visitCount = mostVisted.getInt("visit_count");
					urlName = mostVisted.getString("hosts");
					if (urlName != null && !urlName.equals("") && visitCount > 0) {
						if (db.toString().contains("Firefox")) {
							//Firefox Korrektur da Bsp.
							// ed.miehnnam-wbhd.nalpsgnuselrov. -> vorlesungsplan.dhbw-mannheim.de
							urlName = new StringBuffer(urlName).reverse().substring(1).toString();
						}

						if (results.containsKey(urlName) && visitCount > 0) {
							results.put(urlName, visitCount + results.get(urlName));
						}
						else {
							results.put(urlName, visitCount);
						}
					}
				}
				mostVisted.getStatement().getConnection().close();
			} catch (SQLException e) {
				// kann nicht auf Spalten zugreifen oder Ergebnis leer
			}
		}
		prepareOutput();
	}

	/**
	 * Bereitet nach dem run() die Ergebnisse auf. Einmal im Form von HTML und einmal als TreeMap
	 * für die CSV Datei.
	 */
	private void prepareOutput() {
		boolean resultExists = false;
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("<table>" +
				" <tr> " +
				"<th>Webseite</th> " +
				"<th>Aufrufe</th> " +
				"</tr>");
		for (int i = 0; i < 5; i++) {
			try {
				Map.Entry<String, Integer> highestEntry = getHighestEntry();
				String key = highestEntry.getKey();
				String value = highestEntry.getValue().toString();
				//Formatiere genau eine Zeile
				stringBuffer.append(String.format("<tr>  " +
								"<td>%s</td> " +
								"<td>%s</td>  " +
								"</tr>",
						key, value));
				//lege in CSV Map ab
				csvOutput.put("MostVisitedWebsitePlaceNo" + (i + 1), key);
				results.remove(highestEntry.getKey());
				resultExists = true;
			} catch (NoSuchElementException e) {
				// kein Element gefunden
			}
		}
		if (resultExists) {
			htmlOutput = stringBuffer.append("</table>").toString();
		}
		else {
			htmlOutput = "<b>Leider lieferte das Modul der TOP5 Webseiten keinerlei Ergebnisse! " +
					"Du scheinst deine Spuren gut zu verwischen!</b>";
		}
	}

	/**
	 * Methode baut Verbindung zur jeweiligen Datenbank auf und schickt danach die Query ab.
	 * Die Ergebnisse sind auf 5 limitiert um an Performance zu gewinnnen. Am Ende wird die
	 * Verbindung getrennt und die Datenbank wieder freigegeben.
	 *
	 * @param sqliteDb File zur sqlite Datenbank.
	 * @return Ergebnisse der jeweiligen Abfrage f&uuml;r Firefox oder Chrome oder null.
	 */
	private ResultSet analyzeBrowserHistory(Path sqliteDb) {
		DataSourceManager dbManager = null;
		ResultSet mostVisited = null;
		try {
			dbManager = new DataSourceManager(sqliteDb);
			if (sqliteDb.toString().contains("Firefox")) {
				mostVisited = dbManager.querySqlStatement("SELECT SUM(moz_places.visit_count) " +
						"visit_count, " +
						"moz_places.rev_host hosts " +
						"FROM moz_places " +
						"GROUP BY rev_host " +
						"ORDER BY visit_count DESC " +
						"LIMIT 5;");
			}
			else if (sqliteDb.toString().contains("Chrome")) {
				mostVisited = dbManager.querySqlStatement(
						"SELECT SUM(visit_count) visit_count ,substr(B.url, 0 ,instr(B.url," +
								"'/')) hosts FROM (SELECT visit_count," +
								"substr(substr(url, " +
								"instr(url,'//'))," +
								"3) url FROM urls) AS B " +
								"GROUP BY hosts " +
								"ORDER BY visit_count DESC " +
								"LIMIT 5;"
				);
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return mostVisited;
	}
}



