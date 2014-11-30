package de.aima13.whoami.support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * DataSourceManager der die Verbindungen zu den SQLite Datenbank handelt.
 *
 * @author Marvin Klose
 * @version 3.0
 */


public class DataSourceManager {

	// Connections werden einmalig je Path angelegt, da mehrere Module sich für die Browser
	// Datenbanken interessieren geht der Zugriff durch die Zuordnung schneller
	private static ConcurrentSkipListMap<String, Connection> openConnections = new
			ConcurrentSkipListMap<String, Connection>();
	private Connection dbConnection = null;

	/**
	 * Konstruktor setzt für die lokale Instanz die Verbindung zu einer Datenbank.
	 * Diese Datenbank wird lokal in den Temp Ordner kopiert und nach dem Beenden der JVM gelöscht.
	 *
	 * @param sqliteDatabase Dateipfad zur benötigten sqlite Datenbank.
	 */
	public DataSourceManager(Path sqliteDatabase) throws SQLException {
		dbConnection = openConnections.get(sqliteDatabase.toString());
		if (dbConnection == null) {
			dbConnection = getConnectionFromShadowCopy(sqliteDatabase);
		}
		if (dbConnection == null) {
			throw new SQLException();
		}
	}

	/**
	 * Sind alle Module fertig alle Resourcen wieder freigeben.
	 * Und die bestehenden Verbindungen wieder komplett gelöscht werden.
	 */
	public static void closeRemainingOpenConnections() {
		for (Connection c : openConnections.values()) {
			try {
				if (c != null) {
					c.close();
				}
			} catch (SQLException e) {
				// Verbindung hat Probleme
			}
		}
		openConnections.clear();
	}

	/**
	 * Google Chrome sperrt, wenn es läuft komplett seine Datenbank,
	 * folglich gibt es dann bei Querys 'Database locked Exceptions'. Durch die in dieser Methode
	 * angelegte Schattenkopie ist dieses Problem ausgehebelt, weil die Verbdinung zur Kopie in
	 * den temporären Files besteht.
	 *
	 * @param source Google Chrome sqlite Pfad
	 * @return Connection Verbindung zur kopierten SQLite Datenbank.
	 */
	private synchronized Connection getConnectionFromShadowCopy(Path source) throws SQLException {
		if (!openConnections.containsKey(source.toString())) {
			String copiedBrowser = copyToTemp(source);
			if (copiedBrowser != null) {
				try {
					Class.forName("org.sqlite.JDBC");
					Connection fakedConnection = DriverManager.getConnection("jdbc:sqlite:" +
							copiedBrowser);
					Connection old = openConnections.put(source.toString(), fakedConnection);
					if (old != null) {
						openConnections.put(source.toString(), old);
						fakedConnection.close();
					}
				} catch (ClassNotFoundException e) {
					// wird beim Build eingebaut tritt also nicht auf
				}
				return openConnections.get(source.toString());
			}
			// Darf nicht vorkommen! Wenn doch dann Exception
			throw new SQLException();
		} else {
			return openConnections.get(source.toString());
		}
	}

	/**
	 * Die Methode erzeugt eine neue temporäre Datei und kopiert dann in diese die bestehende Datei.
	 *
	 * @param source Pfad der kopiert werden soll.
	 * @return Pfad als String wie neu erzeugte Datei heißt
	 */
	private synchronized String copyToTemp(Path source) {
		Path browserCopy = null;
		try {
			if (!openConnections.containsKey(source.toString())) {
				browserCopy = Files.createTempFile("db" + java.util.UUID.randomUUID().toString(),
						".sqlite");
				Files.copy(source, browserCopy, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (browserCopy != null) {
				Utilities.deleteTempFileOnExit(browserCopy.toFile().getAbsolutePath());
				return browserCopy.toString();
			}
		}
		return null;
	}


	/**
	 * @param statement Query als String die zur Abfrage dienen soll.
	 * @return ResultSet, dass aus den Ergebnissen der Abfrage besteht.
	 *
	 * @throws SQLException Fehler beim Ausführen des SQL Befehls.
	 */
	public synchronized ResultSet querySqlStatement(String statement) throws SQLException {
		Statement s = dbConnection.createStatement();
		s.closeOnCompletion();
		return s.executeQuery(statement);
	}

}
