package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.GlobalData;
import de.aima13.whoami.support.DataSourceManager;
import de.aima13.whoami.support.Utilities;
import org.stringtemplate.v4.ST;

import java.nio.file.Path;
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
	private static final String TEMPLATE_LOCATION = "/data/webStats.html";
	private List<Path> browserDatabases = new ArrayList<Path>();
	private TreeMap<String, Integer> results = new TreeMap<String, Integer>();
	private SortedMap<String, String> csvOutput = new TreeMap<String, String>();
	private String htmlOutput = "";
	private boolean outputPrepared = false;
	private String favouriteBrowser;
	private long currentMaxHistory=0;
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
		if (!outputPrepared) {
			prepareOutput();
		}
		return htmlOutput;
	}

	@Override
	public String getReportTitle() {
		return "Deine Internetaktivitäten";
	}

	@Override
	public String getCsvPrefix() {
		return "Web";
	}

	@Override
	public String[] getCsvHeaders() {
		return new String[0];
	}


	@Override
	public SortedMap<String, String> getCsvContent() {
		if (!outputPrepared) {
			prepareOutput();
		}
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
			ResultSet mostVisited = null;
			try {
				mostVisited = analyzeBrowserHistory(db);
				if (mostVisited != null){
					while (mostVisited.next()) {
						int visitCount = -1;
						String urlName = "";
						visitCount = mostVisited.getInt("visit_count");
						urlName = mostVisited.getString("hosts");
						if (urlName != null && !urlName.equals("") && visitCount > 0) {
							if (db.toString().contains("Firefox")) {
								//Firefox Korrektur da Bsp.
								// ed.miehnnam-wbhd.nalpsgnuselrov. -> vorlesungsplan.dhbw-mannheim.de
								urlName = new StringBuffer(urlName).reverse().substring(1).toString();
							}
							checkScoreInfluence(urlName);
							if (results.containsKey(urlName) && visitCount > 0) {
								results.put(urlName, visitCount + results.get(urlName));
							} else {
								results.put(urlName, visitCount);
							}
						}
					}
				}
			} catch (SQLException e) {
				// kann nicht auf Spalten zugreifen oder Ergebnis leer
			} finally {
				//ResultSet,Statement close
				if (mostVisited != null) {
					try {
						mostVisited.close();
						mostVisited.getStatement().close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void checkScoreInfluence(String urlName) {
		if (urlName.contains("facebook")){
			GlobalData.getInstance().changeScore("Selbstmordgefährdung",-10);
		}
		if (urlName.contains("9gag")){
			GlobalData.getInstance().changeScore("Faulenzerfaktor",10);
		}
	}

	/**
	 * Bereitet nach dem run() die Ergebnisse auf. Einmal im Form von HTML und einmal als TreeMap
	 * für die CSV Datei.
	 */
	private void prepareOutput() {
		boolean resultExists = false;
		ST template = new ST(Utilities.getResourceAsString(TEMPLATE_LOCATION), '$', '$');

		for (int i = 0; i < 5; i++) {
			try {
				Map.Entry<String, Integer> highestEntry = Utilities.getHighestEntry(results);
				String key = highestEntry.getKey();
				String value = highestEntry.getValue().toString();

				if (key.contains("facebook")){
					template.add("facebook", true);
				}
				if (key.contains("stackoverflow")){
					template.add("stackoverflow", true);
				}
				if (key.contains("bild")){
					template.add("bild", true);
				}
				template.addAggr("webseite.{url, counter}", key, value);
				//lege in CSV Map ab
				csvOutput.put("MostVisitedWebsitePlaceNo" + (i + 1), key);
				results.remove(highestEntry.getKey());
				resultExists = true;
			} catch (NoSuchElementException e) {
				// kein Element gefunden
			}
		}
		template.add("favouriteBrowser",favouriteBrowser);
		template.add("hasData",resultExists);

		outputPrepared = true;
		htmlOutput = template.render();
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
		long dbSize = sqliteDb.toFile().length();
		boolean browserFavUpdate = false;
		if (dbSize > currentMaxHistory){
			currentMaxHistory = dbSize;
			browserFavUpdate = true;
		}
		DataSourceManager dbManager = null;
		ResultSet mostVisited = null;
		try {
			dbManager = new DataSourceManager(sqliteDb);
			if (sqliteDb.toString().contains("Firefox")) {
				if (browserFavUpdate){
					this.favouriteBrowser = "Firefox";
				}
				mostVisited = dbManager.querySqlStatement("SELECT SUM(moz_places.visit_count) " +
						"visit_count, " +
						"moz_places.rev_host hosts " +
						"FROM moz_places " +
						"GROUP BY rev_host " +
						"ORDER BY visit_count DESC " +
						"LIMIT 5;");
			} else if (sqliteDb.toString().contains("Chrome")) {
				if (browserFavUpdate){
					this.favouriteBrowser = "Chrome";
				}
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
			// Deadlock auf DB
		}
		return mostVisited;
	}
}



