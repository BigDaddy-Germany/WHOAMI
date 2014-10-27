package de.aima13.whoami.support;

import org.sqlite.JDBC;

import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marvin on 18.10.2014.
 */
public class DataSourceManager {

	private Connection dbConnection = null;
	private static List<Connection> openConnections = new ArrayList<Connection>();
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
		dbConnection = getAlreadyOpenConnection(sqliteDatabase.toString());
		if(dbConnection == null){
			dbConnection = DriverManager.getConnection
					("jdbc:sqlite:" + sqliteDatabase.toString());
		}


		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				closeConnection();
			}
		});

	}

	/**
	 * Zur Abfrage, ob Verbindung noch offen ist bzw von null verschieden.
	 * @return Boolean, der angibt ob die Verbindung zustande gekommen ist, bzw. noch offen ist.
	 */
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
	public synchronized ResultSet querySqlStatement(String statement) throws SQLException {
		Statement s = dbConnection.createStatement();
		ResultSet rs = s.executeQuery(statement);
		return rs;
	}
	private Connection getAlreadyOpenConnection(String dbUrl){
		for (Connection c : openConnections){
			try {
				if (c.getMetaData().getURL().equals(dbUrl)){
					return c;
				}
			} catch (SQLException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Wrapper zum schließen und freigeben der Verbindung!
	 */
	public void closeConnection() {
		try {
			if (dbConnection != null && !dbConnection.isClosed() ) {
				openConnections.remove(dbConnection);
				dbConnection.close();
			}
		} catch (SQLException e) {

		}
	}

}
