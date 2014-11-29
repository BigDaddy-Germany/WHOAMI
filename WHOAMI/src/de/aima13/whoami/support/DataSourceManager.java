package de.aima13.whoami.support;

import org.apache.commons.io.filefilter.FileFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.SortedMap;
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
	private static SortedMap<String, Connection> openConnections = new ConcurrentSkipListMap<String, Connection>();
	private Connection dbConnection = null;

	/**
	* Statischer Klassenkonstruktor der dafür sorgt, dass die von der SQLite Library ansonsten
	* geladene dll nur nur einmal erzeugt wird.
	*/
	static {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			// kann nicht auftreten, weil durch Maven die Library sicher dabei ist
		}
	}

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
	}

	/**
	 * Sind alle Module fertig alle Resourcen wieder freigeben.
	 * Und die bestehenden Verbindungen wieder komplett gelöscht werden.
	 */
	public static void closeRemainingOpenConnections() {
		for (Connection c : openConnections.values()) {
			try {
				if (c != null && !c.isClosed()) {
					c.close();
				}
			} catch (SQLException e) {
			}
		}
		openConnections.clear();
		openConnections = null;
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
		Connection fakedConnection = null;
		if (!openConnections.containsKey(source.toString())) {
			String copiedBrowser = copyToTemp(source);
			if(copiedBrowser !=null) {
				fakedConnection = DriverManager.getConnection("jdbc:sqlite:" + copiedBrowser);
				openConnections.put(source.toString(), fakedConnection);
			}
		} else {
			fakedConnection = openConnections.get(source.toString());
		}
		return fakedConnection;
	}

	/**
	 * Die Methode erzeugt eine neue temporäre Datei und kopiert dann in diese die bestehende Datei.
	 * @param source Pfad der kopiert werden soll.
	 * @return Pfad als String wie neu erzeugte Datei heißt
	 */
	private synchronized String copyToTemp(Path source) {
		try {
			File browserCopy = File.createTempFile("db" + java.util.UUID.randomUUID().toString(),
					".sqlite");
			Utilities.deleteTempFileOnExit(browserCopy.getAbsolutePath());
			Files.copy(source, browserCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);
			return browserCopy.toString();
		} catch (IOException e) {
			e.printStackTrace();
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
		ResultSet rs = s.executeQuery(statement);
		return rs;
	}

}
