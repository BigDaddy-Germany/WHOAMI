package de.aima13.whoami.support;

import org.sqlite.JDBC;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.Map;
import java.util.TreeMap;

/**
 * DataSourceManager der die Verbindungen zu den SQLite Datenbank handelt.
 *
 * @author Marvin Klose
 * @version 2.0
 */


public class DataSourceManager {

	// Connections werden einmalig je Path angelegt, da mehrere Module sich für die Browser
	// Datenbanken interessieren geht der Zugriff durch die Zuordnung schneller
	private static Map<Path, Connection> openConnections = new TreeMap<Path, Connection>();
	
	private Connection dbConnection = null;


	/**
	 * Konstruktor erzeugt eine Verbindung zur Datenbank und lädt den JDBC Treiber dafür.
	 * Ebenso gibt es einen Hook, dass wenn die Runtime beendet wird, auf die Connection getrennt
	 * wird. Falls jemand vergisst die Verbindung zu schließen.
	 *
	 * @param sqliteDatabase Dateipfad zur benötigten sqlite Datenbank.
	 */
	public DataSourceManager(Path sqliteDatabase) throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");

		System.out.println("Found " + JDBC.class.getName() + "!");

		dbConnection = getAlreadyOpenConnection(sqliteDatabase);
		if (dbConnection == null) {
			if (sqliteDatabase.toString().contains("Chrome")) {
				dbConnection = getConnectionFromShadowCopy(sqliteDatabase);
			} else {
				dbConnection = DriverManager.getConnection
						("jdbc:sqlite:" + sqliteDatabase.toString());
			}
		}
	}

	/**
	 * Sind alle Module fertig alle Resourcen wieder freigeben.
	 */
	public static void closeRemainingOpenConnections() {
		for (Map.Entry<Path, Connection> entry : openConnections.entrySet()) {
			try {
				if (entry.getValue() != null && !entry.getValue().isClosed()) {
					entry.getValue().close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
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
	private Connection getConnectionFromShadowCopy(Path source) {
		File chromeCopy = null;
		try {
			chromeCopy = File.createTempFile("chrome", ".sqlite", null);
			chromeCopy.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			Files.copy(source, chromeCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Connection fakedConnection = null;
		try {
			Class.forName("org.sqlite.JDBC");
			fakedConnection = DriverManager.getConnection
					("jdbc:sqlite:" + chromeCopy.toString());
			openConnections.put(source, fakedConnection);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return fakedConnection;
	}

	/**
	 * Zur Abfrage, ob Verbindung noch offen ist bzw von null verschieden.
	 *
	 * @return Boolean, der angibt ob die Verbindung zustande gekommen ist, bzw. noch offen ist.
	 */
	public boolean isConnected() {
		try {
			return !dbConnection.isClosed();
		} catch (SQLException e) {
			return false;
		}
	}

	/**
	 * @param statement Query als String die zur Abfrage dienen soll.
	 * @return ResultSet, dass aus den Ergebnissen der Abfrage besteht.
	 *
	 * @throws SQLException Fehler beim Ausführen des SQL Befehls.
	 */
	public synchronized ResultSet querySqlStatement(String statement) throws SQLException {
		Statement s = dbConnection.createStatement();
		ResultSet rs = s.executeQuery(statement);
		return rs;
	}

	private Connection getAlreadyOpenConnection(Path lookUpPath) {
		if (openConnections.containsKey(lookUpPath)) {
			return openConnections.get(lookUpPath);
		}
		return null;
	}

}
