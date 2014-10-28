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
	private static final String SELECT_FIRST_NAME="SELECT value,SUM(timesUsed),firstUsed,lastUsed" +
			" FROM moz_formhistory WHERE LOWER(fieldname) LIKE '%vorname%' OR LOWER(fieldname) LIKE" +
			" '%firstname%' GROUP BY value ORDER BY SUM(timesUsed) DESC";
	private static final String SELECT_LAST_NAME="";
	private static final String SELECT_EMAIL="SELECT LOWER(value),SUM(timesUsed),firstUsed," +
			"lastUsed FROM moz_formhistory WHERE LOWER(fieldname) LIKE '%mail%' AND value" +
			" LIKE '%@%' GROUP BY LOWER(value) ORDER BY SUM(timesUsed) DESC";
	private static final String SELECT_PLACE="SELECT value,SUM(timesUsed),firstUsed,lastUsed " +
			"FROM moz_formhistory WHERE LOWER(fieldname) LIKE '%ort%' " +
			"OR LOWER(fieldname) LIKE '%city%' GROUP BY value ORDER BY SUM(timesUsed) DESC";

	private static final String SELECT_STREET="SELECT value,SUM(timesUsed),firstUsed," +
			"lastUsed FROM moz_formhistory WHERE LOWER(fieldname) LIKE '%strasse%' OR" +
			" LOWER(fieldname) LIKE '%street%' GROUP BY value ORDER BY SUM(timesUsed) DESC";
	private static final String SELECT_PHONE_NUMBER="";
	private static final String SELECT_BANK_ACCOUNT="";




	private String myHtml="";
	private TreeMap<String,String> myCsv = new TreeMap<>();
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
			//this.analy
		}
		if(Whoami.getTimeProgress()<100){
			//doStuff
		}
	}


	private void getEmailFromFF(Path formHistoryPath){
		//@todo test this code

		boolean error=false;
		DataSourceManager dbManager=null;
		try {
			dbManager = new DataSourceManager(formHistoryPath);
		}catch(SQLException e){
			//merken dass es Probleme gab aber ansonsten nix machen
			error=true;
		}
		if(!error && dbManager!=null){
			String email="";
			try{
				ResultSet emailResults=dbManager.querySqlStatement(SELECT_EMAIL+" LIMIT 1");
				emailResults.beforeFirst();
				emailResults.next();
				email =emailResults.getString("value");
			}catch(Exception e){
				//kann eigentlich nur crashen wenn es absolut keinen Eintrag gibt
				error=true;
			}
		}

	}



	/*
	*Methode die versucht aus eventuell gefundenen Lebensläufen, Daten zu extrahieren
	*
	*/
	private void analyzeCV(){
		//@ToDo ausimplementieren
	}
	/**
	 * Filtert aus allen Daten die für dieses Modul die SQlite DBs heraus da diese anders
	 * behandelt werden als "normale" Dateien.
	 */
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

