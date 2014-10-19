package de.aima13.whoami.support;

import java.io.File;
import java.sql.*;

/**
 * Created by Marvin on 18.10.2014.
 */
public class DataSourceManager {

	private Connection dbConnection = null;

	/**
	 * Konstruktor erzeugt eine Verbindung zur Datenbank und lädt den JDBC Treiber dafür.
	 * Ebenso gibt es einen Hook, dass wenn die Runtime beendet wird, auf die Connection getrennt
	 * wird. Falls jemand vergisst die Verbindung zu schließen.
	 *
	 * @param sqliteDatabase Dateipfad zur benötigten sqlite Datenbank.
	 */
	public DataSourceManager(File sqliteDatabase) throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		dbConnection = DriverManager.getConnection
					("jdbc:sqlite:" + sqliteDatabase.getAbsolutePath());

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					if (!dbConnection.isClosed() && dbConnection != null) {
						dbConnection.close();
					}
				} catch (SQLException e) {

				}
			}
		});

	}
	public boolean isConnected(){
		try {
			return  !dbConnection.isClosed();
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
	public ResultSet querySqlStatement(String statement) throws SQLException {
		Statement s = dbConnection.createStatement();
		ResultSet rs = s.executeQuery(statement);
		return rs;
	}

	/**
	 * Wrapper zum schließen und freigeben der Verbindung!
	 */
	public void closeConnection() {
		try {
			if (!dbConnection.isClosed() && dbConnection != null) {
				dbConnection.close();
			}
		} catch (SQLException e) {

		}
	}

}
