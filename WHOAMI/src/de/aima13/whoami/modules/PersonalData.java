package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.GlobalData;
import de.aima13.whoami.Whoami;
import de.aima13.whoami.support.DataSourceManager;
import de.aima13.whoami.support.SqlSelectSaver;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Persönliche Daten Modul zum erfassen von Adresse,Name,Email-Adresse
 */
public class PersonalData implements Analyzable {

	private static final String MY_NAME="Persönliche Daten";

	//firefox sql Statements
	private static final String SELECT_FIRST_NAME="SELECT value,SUM(timesUsed) AS hcnt" +
			" FROM moz_formhistory WHERE LOWER(fieldname) LIKE '%vorname%' OR LOWER(fieldname) LIKE" +
			" '%firstname%' GROUP BY value ORDER BY SUM(timesUsed) DESC";
	private static final String SELECT_LAST_NAME="SELECT value,SUM(timesUsed) AS hcnt FROM " +
			"moz_formhistory" +
			" WHERE LOWER(fieldname) LIKE '%nachname%' OR LOWER(fieldname) LIKE '%lastname%'" +
			" GROUP BY value ORDER BY SUM(timesUsed) DESC";
	private static final String SELECT_EMAIL="SELECT LOWER(value) AS value,SUM(timesUsed) AS hcnt" +
			" FROM moz_formhistory WHERE (LOWER(fieldname) LIKE '%mail%' OR LOWER(fieldname) LIKE" +
			" '%login%' OR LOWER(fieldname) LIKE 'id'" +
			" ) AND value" +
			" LIKE '%@%' GROUP BY LOWER(value) ORDER BY SUM(timesUsed) DESC";
	private static final String SELECT_PLACE="SELECT value,SUM(timesUsed) AS hcnt " +
			"FROM moz_formhistory WHERE LOWER(fieldname) LIKE '%ort%' " +
			"OR LOWER(fieldname) LIKE '%city%' GROUP BY value ORDER BY SUM(timesUsed) DESC";

	private static final String SELECT_STREET="SELECT value,SUM(timesUsed) AS hcnt" +
			" FROM moz_formhistory WHERE LOWER(fieldname) LIKE '%strasse%' OR" +
			" LOWER(fieldname) LIKE '%street%' GROUP BY value ORDER BY SUM(timesUsed) DESC";
	private static final String SELECT_PHONE_NUMBER="SELECT value,SUM(timesUsed) AS hcnt FROM " +
			"moz_formhistory" +
			" WHERE LOWER(fieldname) LIKE '%phone%'  OR LOWER(fieldname) LIKE '%telefon%'GROUP BY" +
			" value ORDER BY SUM(timesUsed) DESC";
	private static final String SELECT_BANK_ACCOUNT="SELECT value," +
			"SUM(timesUsed) AS hcnt FROM moz_formhistory " +
			"WHERE LOWER(fieldname) LIKE '%iban%' GROUP BY value ORDER BY SUM(timesUsed) DESC";

	//Chrome SQL Statements
	private static final String SELECT_FIRST_NAME_CHROME="SELECT value," +
			"SUM(count) AS hcnt FROM autofill WHERE name LIKE '%vorname%' OR" +
			" name LIKE '%firstname%' GROUP BY value_lower ORDER BY SUM(count) DESC";
	private static final String SELECT_LAST_NAME_CHROME="SELECT value,SUM(count) AS hcnt " +
			"FROM autofill WHERE name LIKE '%nachname%' OR name LIKE '%lastname%' " +
			"GROUP BY value_lower ORDER BY SUM(count) DESC";
	private static final String SELECT_EMAIL_CHROME="SELECT value_lower AS value,SUM(count) AS hcnt" +
			" FROM autofill WHERE (LOWER(name) LIKE '%mail%' OR LOWER(name) LIKE '%login%' OR " +
			"LOWER(name) LIKE 'id' ) AND value_lower LIKE '%@%' GROUP BY value_lower " +
			"ORDER BY SUM(count) DESC";
	private static final String SELECT_PLACE_CHROME="SELECT value,SUM(count) AS hcnt FROM autofill" +
			" WHERE LOWER(name) LIKE '%ort%' OR LOWER(name) LIKE '%city%' GROUP BY value " +
			"ORDER BY SUM(count) DESC";
	private static final String SELECT_STREET_CHROME="SELECT value," +
			"SUM(count) AS hcnt FROM autofill WHERE LOWER(name) LIKE '%strasse%' " +
			"OR LOWER(name) LIKE '%street%' GROUP BY value_lower ORDER BY SUM(count) DESC";
	private static final String SELECT_PHONE_NUMBER_CHROME="SELECT value,SUM(count) AS hcnt" +
			" FROM autofill WHERE LOWER(name) LIKE '%phone%'  OR LOWER(name) LIKE '%telefon%' " +
			"GROUP BY value_lower ORDER BY SUM(count) DESC";
	private static final String SELECT_BANK_ACCOUNT_CHROME="SELECT value,SUM(count) AS hcnt " +
			"FROM autofill WHERE LOWER(name) LIKE '%iban%' GROUP BY value ORDER BY SUM(count) DESC";

	//dieses Modul soll nichts zurückgeben es liefert lediglich Werte an die GLobalDataKlasse
	private String myHtml=null;
	private TreeMap<String,String> myCsv = null;
	private List<Path> myDbs;
	private List<Path> myPersonalDataPath;
	private LinkedList<SqlSelectSaver> myDbResults;
	public PersonalData() {

	}

	@Override
	public List<String> getFilter() {
		List<String> filter = new ArrayList<>();
		//formhistory.sql gehört zu Firefox
		filter.add("**Firefox**formhistory.sqlite");

		filter.add("**Google**Web Data");


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
			this.analyseFfForms();
		}


		if(Whoami.getTimeProgress()<100){
			//this.analyseChromeForms();
		}
		//@Todo besprechen ob das legitim ist
		//egal ob Zeit abgelaufen ist Daten ausgeben
		this.transmitBrowserForensik();
		if(Whoami.getTimeProgress()<100){


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
				myDbResults= new LinkedList<>();
				for (Path curr : ffDbs) {
					for(int i=0;i<7;i++) {
						String title="";
						String select="";
						switch (i) {
							case 0:title="Vorname";
									select=SELECT_FIRST_NAME;
									break;
							case 1: title="Nachname";
									select=SELECT_LAST_NAME;
									break;
							case 2: title="Straße";
									select=SELECT_STREET;
									break;
							case 3: title="Ort";
									select=SELECT_PLACE;
									break;
							case 4: title="Telefon";
									select=SELECT_PHONE_NUMBER;
									break;
							case 5: title="Email";
									select=SELECT_EMAIL;
									break;
							case 6: title="IBAN";
									select=SELECT_BANK_ACCOUNT;

						}
						myDbResults.addAll(executeSqlAndReturnFromFf(curr,
								title, select));

						//überprüfen ob der Wert schon enthalten ist
						/**
						boolean alreadyInList = false;
						for (SqlSelectSaver old : myDbResults) {
							if (old.title.equals(result.value) && old.value.equals(result.value)) {
								alreadyInList = true;
								old.hitCount += result.hitCount;
							}
						}
							if (!alreadyInList) {
								myDbResults.add(result);
							}
						*/

					}
				}
			}

		}
	}

	//@Todo Kommentar updaten
	/**
	 *Diese Methode dient der einfachen Abfrage von Daten aus der FireFox formHistory und liefert
	 * @param formHistoryPath Der Pfad zu einer
	 * @param
	 * @return Das oberste Ergebniss der Abfrage oder ein leerer String falls es zu Fehlern kamm
	 */
		private List<SqlSelectSaver> executeSqlAndReturnFromFf(Path formHistoryPath,
		                                                       String title, String sqlStatement) {


			LinkedList<SqlSelectSaver> sqlResults = new LinkedList<>();
			String resultStrg = "";
			int resultHitCnt = 0;
			boolean error = false;
			DataSourceManager dbManager = null;
			try {
				dbManager = new DataSourceManager(formHistoryPath);
			} catch (Exception e) {
				//merken dass es Probleme gab aber ansonsten nix machen
				error = true;
			}
			if (!error && dbManager != null) {
				ResultSet resultSet;
				try {
					resultSet = dbManager.querySqlStatement(sqlStatement);

					//resultSet.next();

				} catch (Exception e) {
					//kann eigentlich nur crashen wenn es absolut keinen Eintrag gibt
					error = true;
					resultSet=null;
				}
				if (!error && resultSet!=null) {
				/**	try {
						resultSet.beforeFirst();
					} catch (SQLException e) {
						//kann eigentlich nur crashen wenn es absolut keinen Eintrag gibt
						error = true;
					}*/
					try {
						while (!error && resultSet.next()) {
							try {
								resultStrg = resultSet.getString("value");
								resultHitCnt = resultSet.getInt("hcnt");
							}catch(Exception e){
								resultStrg="";
								resultHitCnt=0;
							}
							//keine leeren Elemente zulassen
							if(!resultStrg.equals("")) {
								sqlResults.add(new SqlSelectSaver(title, resultStrg, resultHitCnt));
							}
						}
					} catch (SQLException e) {
						//nix machen
						e = e;
					}



				}
			}
			return sqlResults;
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


	private void transmitBrowserForensik(){
		if(myDbResults!=null){
			if(myDbResults.size()>0){
				for(SqlSelectSaver curr: myDbResults){
					GlobalData.getInstance().proposeData(curr.title,curr.value,curr.hitCount);
				}
				//Speicher freigeben:
				//myDbResults=null;
			}
		}

	}

	}

