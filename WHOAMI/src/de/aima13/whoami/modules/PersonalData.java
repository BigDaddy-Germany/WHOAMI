package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.Whoami;
import de.aima13.whoami.support.DataSourceManager;
import de.aima13.whoami.support.Utilities;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Persönliche Daten Modul zum erfassen von Adresse,Name,Email-Adresse
 */
public class PersonalData implements Analyzable {

	private static final String MY_NAME="Persönliche Daten";
	private static final String SELECT_FIRST_NAME="SELECT value,SUM(timesUsed)" +
			" FROM moz_formhistory WHERE LOWER(fieldname) LIKE '%vorname%' OR LOWER(fieldname) LIKE" +
			" '%firstname%' GROUP BY value ORDER BY SUM(timesUsed) DESC";
	private static final String SELECT_LAST_NAME="SELECT value,SUM(timesUsed) FROM moz_formhistory" +
			" WHERE LOWER(fieldname) LIKE '%nachname%' OR LOWER(fieldname) LIKE '%lastname%'" +
			" GROUP BY value ORDER BY SUM(timesUsed) DESC";
	private static final String SELECT_EMAIL="SELECT LOWER(value) AS value,SUM(timesUsed)," +
			" FROM moz_formhistory WHERE LOWER(fieldname) LIKE '%mail%' AND value" +
			" LIKE '%@%' GROUP BY LOWER(value) ORDER BY SUM(timesUsed) DESC";
	private static final String SELECT_PLACE="SELECT value,SUM(timesUsed)" +
			"FROM moz_formhistory WHERE LOWER(fieldname) LIKE '%ort%' " +
			"OR LOWER(fieldname) LIKE '%city%' GROUP BY value ORDER BY SUM(timesUsed) DESC";

	private static final String SELECT_STREET="SELECT value,SUM(timesUsed)" +
			" FROM moz_formhistory WHERE LOWER(fieldname) LIKE '%strasse%' OR" +
			" LOWER(fieldname) LIKE '%street%' GROUP BY value ORDER BY SUM(timesUsed) DESC";
	private static final String SELECT_PHONE_NUMBER="SELECT value,SUM(timesUsed) FROM moz_formhistory" +
			" WHERE LOWER(fieldname) LIKE '%phone%'  OR LOWER(fieldname) LIKE '%telefon%'GROUP BY" +
			" value ORDER BY SUM(timesUsed) DESC";
	private static final String SELECT_BANK_ACCOUNT="SELECT value," +
			"SUM(timesUsed) FROM moz_formhistory " +
			"WHERE LOWER(fieldname) LIKE '%iban%' GROUP BY value ORDER BY SUM(timesUsed) DESC";
	private static final String[] SQL_STATEMENT_SELECTION={SELECT_FIRST_NAME,SELECT_LAST_NAME,
			SELECT_EMAIL,SELECT_PLACE,SELECT_STREET, SELECT_PHONE_NUMBER,SELECT_BANK_ACCOUNT};


	//dieses Modul soll nichts zurückgeben es liefert lediglich Werte an die GLobalDataKlasse
	private String myHtml=null;
	private TreeMap<String,String> myCsv = null;
	private List<Path> myDbs;
	private List<Path> myPersonalDataPath;
	public PersonalData() {

	}

	@Override
	public List<String> getFilter() {
		List<String> filter = new ArrayList<>();
		//formhistory.sql gehört zu Firefox
		filter.add("**Firefox**formhistory.sqlite");
		//@ToDo nach passender Sqlite-Db von Chrome suche

		//@ToDo nach Lebensläufen auf Festplatte suchen



		return filter;
	}

	@Override
	public void setFileInputs(List<Path> files) throws Exception {
		if(files==null){
			throw new IllegalArgumentException("This Module doesn't work without Input");
		}
		myPersonalDataPath=files;
	}

	@Override
	public String getHtml() {
		return myHtml;
	}

	@Override
	public String getReportTitle()
	{
		return MY_NAME;
	}

	@Override
	public String getCsvPrefix() {
		String CsvPrefix=MY_NAME.replaceAll("\\s+","");

		return CsvPrefix;
	}

	@Override
	public SortedMap<String, String> getCsvContent() {

		return myCsv;
	}


	@Override
	public void run() {

		if(Whoami.getTimeProgress()<100){
			this.dbExtraction();
		}

		if(Whoami.getTimeProgress()<100){
			this.analyzeCV();

		}
		if(Whoami.getTimeProgress()<100){
			this.analyseFfForms();
		}
		if(Whoami.getTimeProgress()<100){
			//doStuff
		}
	}

	private void analyseFfForms(){



		if(myDbs.size()>0){

			//herausfinden welche DBs der gefunden zu FF gehören
			List<Path> ffDbs=new LinkedList<Path>();
			for(Path curr: myDbs){
				String path=curr.toString().toLowerCase();
				if(path.contains("firefox") && path.contains("formhistory")){
					ffDbs.add(curr);
				}
			}
			if(ffDbs.size()>0) {

				for (Path curr : ffDbs) {
					int i=0;
					for(String statement: SQL_STATEMENT_SELECTION){

						String result = executeSqlAndReturnSingleStringFf(curr,statement);
						if(!result.equals("")){
							myHtml+=result+"\n";
						}
						
					}
				}
			}

		}
	}
	/**
	 *Diese Methode dient der einfachen Abfrage von Daten aus der FireFox formHistory und liefert
	 * @param formHistoryPath Der Pfad zu einer
	 * @param sqlStatement SQL-Befehl der an übermitellt wird um die Datenbank auszulesen
	 * @return Das oberste Ergebniss der Abfrage oder ein leerer String falls es zu Fehlern kamm
	 */
		private String executeSqlAndReturnSingleStringFf(Path formHistoryPath,
		                                                 String sqlStatement ){
			String result="";
			boolean error=false;
			DataSourceManager dbManager=null;
			try {
				dbManager = new DataSourceManager(formHistoryPath);
			}catch(Exception e){
				//merken dass es Probleme gab aber ansonsten nix machen
				error=true;
			}
			if(!error && dbManager!=null){
				try{
					ResultSet resultSet=dbManager.querySqlStatement(sqlStatement+" LIMIT 1");
					//resultSet.beforeFirst();
					//resultSet.next();
					result =resultSet.getString("value");
				}catch(Exception e){
					//kann eigentlich nur crashen wenn es absolut keinen Eintrag gibt
					error=true;
				}
			}
			if(error){
				result="";
			}
			return  result;

		}
	/*
	*Methode die versucht aus eventuell gefundenen Lebensläufen, Daten zu extrahieren
	*
	*/
	private void analyzeCV(){
		//@ToDo ausimplementieren
		if(myPersonalDataPath.size()>0){

		}
	}
	/**
	 * Filtert aus allen Daten die für dieses Modul die SQlite DBs heraus da diese anders
	 * behandelt werden als "normale" Dateien.
	 */
	//@Todo korrigierte Methode auslagern und aufrufen
	private void dbExtraction(){
		//sqlite daten rausspeichern
		//@Todo Fehler wie er in Food auftrat behandeln wenn mehr als 2 sqlite dbs gefunden werden
		myDbs = new ArrayList<Path>();
		int foundDbs = 0;

		try {
			for (Path curr : myPersonalDataPath) {
				if (curr != null) {
					String path;
					try {
						path = curr.toString();//getCanonicalPath();
					} catch (Exception e) {
						e.printStackTrace();
						path = "";
					}

					if (path.contains(".sqlite")) {
						myDbs.add( curr);
						foundDbs++;
					} else if (path.contains("History")) {
						myDbs.add(curr);
						foundDbs++;
					}

					if (foundDbs > 1) {
						break;
					}
				}
			}
		}catch(Exception e){e.printStackTrace();}

		//Db-Files aus myFoodFiles Liste löschen
		for(int i=0; i<foundDbs; i++) {
			try {

				myPersonalDataPath.remove(myDbs.get(i));
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	}

