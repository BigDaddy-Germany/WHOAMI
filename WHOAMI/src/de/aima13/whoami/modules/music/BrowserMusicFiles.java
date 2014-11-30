package de.aima13.whoami.modules.music;

import de.aima13.whoami.GuiManager;
import de.aima13.whoami.support.DataSourceManager;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Inga on 24.11.2014.
 */
public class BrowserMusicFiles {

	///////////////////////////////////////////
	///// Analysiere Browserverlauf //////////
	/////////////////////////////////////////

	/**
	 * Durchsucht den Browser-Verlauf auf bekannte Musikportale (MY_SEARCH_DELIEVERY_URLS)
	 *
	 * @param searchUrl "final static String[] MY_SEARCH_DELIEVERY_URLS" wird übergeben
	 * @return void
	 * @exception java.sql.SQLException
	 */

	private static final String[] MY_SEARCH_DELIEVERY_URLS = {"youtube.com", "myvideo.de", "dailymotion.com",
			"soundcloud.com", "deezer.com", "spotify.com", "play.google.com/store/music"};
	ResultSet mostVisited = null; //Liste der gefundenen URLs
	ArrayList<String> urls = new ArrayList<>();

	public String readBrowser(List<Path> browserFiles) {
		String onlService = "";
		GuiManager.updateProgress("Was nutzt du wohl online zum Musikhören?");
		for (Path db : browserFiles) {
			try {
				mostVisited = dbExtraction(db, MY_SEARCH_DELIEVERY_URLS);
				if (mostVisited != null) {
					while (mostVisited.next()) {
						String urlName = "";
						urlName = mostVisited.getString("host");
						if (urlName != null && !urlName.equals("")) {
							if (!(urls.contains(urlName))) {
								urls.add(urlName);
							}
						}
					}
				}
			} catch (SQLException e) {
				//Ergebnis ist leer
			} finally {
				//Schließe ResultSet imd Statement
				if (mostVisited != null) {
					try {
						mostVisited.close();
						mostVisited.getStatement().close();
					} catch (SQLException e) {
						//Keine DB
					}
				}
			}
		}

		// Füge den String onlServices als Aufzählung zusammen
		for (int i = 0; i < urls.size(); i++) {
			for (int j = 0; j < MY_SEARCH_DELIEVERY_URLS.length; j++) {
				if (urls.get(i).contains(MY_SEARCH_DELIEVERY_URLS[j]) && !(onlService.contains(MY_SEARCH_DELIEVERY_URLS[j]))) {
					if (onlService.isEmpty()) {
						onlService += MY_SEARCH_DELIEVERY_URLS[j]; // erster Dienst
					} else {
						onlService += ", " + MY_SEARCH_DELIEVERY_URLS[j]; // weitere Dienste werden mit Komma
						// angehangen
					}
				}
			}
		}
		return onlService;
	}

	/**
	 * Durchsucht den Browser-Verlauf auf bekannte Musikportale (MY_SEARCH_DELIEVERY_URLS)
	 *
	 * @param sqliteDb
	 * @param searchUrl "final static String[] MY_SEARCH_DELIEVERY_URLS" wird übergeben
	 * @return mostVisited Ergebnisliste aller gefundener URLs/Hosts
	 */
	private ResultSet dbExtraction(Path sqliteDb, String searchUrl[]) {
		DataSourceManager dbManager;
		try {
			dbManager = new DataSourceManager(sqliteDb);

			//Kontruktion des SQL-Statements für Firefox
			if (sqliteDb.toString().contains("Firefox")) {
				String sqlStatement = "SELECT host " +
						"FROM moz_hosts " +
						"WHERE host LIKE '" + searchUrl[0] + "'";
				for (int i = 1; i < searchUrl.length; i++) {
					sqlStatement += " OR host LIKE '" + searchUrl[i] + "'";
				}
				mostVisited = dbManager.querySqlStatement(sqlStatement);
			}

			//Kontruktion des SQL-Statements für Chrome
			else if (sqliteDb.toString().contains("Chrome")) {
				String sqlStatement = "SELECT url AS host " +
						"FROM urls " +
						"WHERE host LIKE '%" + searchUrl[0] + "%'";
				for (int i = 1; i < searchUrl.length; i++) {
					sqlStatement += " OR host LIKE '%" + searchUrl[i] + "%'";
				}
				mostVisited = dbManager.querySqlStatement(sqlStatement);
			}
		} catch (SQLException | NullPointerException e) {
			// Deadlock auf DB | es kommt null auf die DB-Abfrage zurück
		}
		return mostVisited;
	}

}


