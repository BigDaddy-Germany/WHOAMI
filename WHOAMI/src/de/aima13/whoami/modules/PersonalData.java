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
 * Persönliche Daten Modul zum erfassen von u.a. Adresse,Name,Email-Adresse
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
		//chrome dependant zur formhistory in FireFox
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
	public String[] getCsvHeaders() {
		return new String[0];
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
			this.analyseChromeForms();
		}
		this.transmitBrowserForensik();
	}

	/**
	 * Diese Methode sendet die SQL Statements an die Chrome Web Data Datenbank(Ablage des Chrome
	 * Browser für Form Verläufe)  um folgende Daten zu ermitteln:
	 * Vornanme,Nachname,Wohnort(Straße,Ort),Telefonummer,IBAN,Emailadresse
	 */
	private void analyseChromeForms(){

		if(myDbs.size()>0){
			if(myDbResults==null){
				myDbResults=new LinkedList<SqlSelectSaver>();
			}

			for (Path curr : myDbs) {
				String path=curr.toString().toLowerCase();
				if(path.contains("google") && path.contains("web data")) {
					for (int i = 0; i < 7; i++) {
						String title = "";
						String select = "";
						switch (i) {
							case 0:
								title = "Vorname";
								select = SELECT_FIRST_NAME_CHROME;
								break;
							case 1:
								title = "Nachname";
								select = SELECT_LAST_NAME_CHROME;
								break;
							case 2:
								title = "Straße";
								select = SELECT_STREET_CHROME;
								break;
							case 3:
								title = "Ort";
								select = SELECT_PLACE_CHROME;
								break;
							case 4:
								title = "Telefon";
								select = SELECT_PHONE_NUMBER_CHROME;
								break;
							case 5:
								title = "Email";
								select = SELECT_EMAIL_CHROME;
								break;
							case 6:
								title = "IBAN";
								select = SELECT_BANK_ACCOUNT_CHROME;

						}
						myDbResults.addAll(executeSqlAndReturn(curr,
								title, select));
					}
				}
			}



		}

	}

	/**
	 *Diese MEthode sendet SQL Statements and die sogenannte FireFox FormHistory um folgende
	 * Daten zu ermitteln:
	 * Vornanme,Nachname,Wohnort(Straße,Ort),Telefonummer,IBAN,Emailadresse
	 */
	private void analyseFfForms(){



		if(myDbs.size()>0){

			//herausfinden welche DBs der gefunden zu FF gehören
			List<Path> ffDbs=new LinkedList<Path>();
			for(Path curr: myDbs){
				String path=curr.toString().toLowerCase();
				if(path.toLowerCase().contains("firefox") && path.toLowerCase().contains
						("formhistory")){
					ffDbs.add(curr);
				}
			}
			if(ffDbs.size()>0) {
				if(myDbResults==null) {
					myDbResults = new LinkedList<>();
				}
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
						myDbResults.addAll(executeSqlAndReturn(curr,
								title, select));
					}
				}
			}

		}
	}

	/**
	 *
	 * @param formHistoryPath Pfad zu einer Datenbak auf der das SQL Statement asugeführt werden
	 *                           soll
	 * @param title Der Titel
	 * @param sqlStatement Das SQL Statement welches die Daten auf der Datenbank erfasst,
	 *                        es sollte mit "SELECT (XXXXX) AS value, (XXXXX) AS hcnt" beginnen
	 * @return gibt eine List von SqlSelectSaver Instanzen zurück die die Werte von value und
	 * hcnt zusammen mit dem übergebenen Titel enthalten, falls die Anfrage fehlerhaft war oder
	 * keine Werte selektiert wurden wird eine leere Liste zurückgegeben
	 */
		private List<SqlSelectSaver> executeSqlAndReturn(Path formHistoryPath,
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

				} catch (Exception e) {
					//kann eigentlich nur crashen wenn die Datenbank Probleme macht
					//oder das Statement falsch ist
					error = true;
					resultSet=null;
				}
				if (!error && resultSet!=null) {
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
					}



				}
			}
			return sqlResults;
		}



	/**
	 * Filtert aus allen Daten die für dieses Modul die SQlite DBs heraus da diese anders
	 * behandelt werden als "normale" Dateien.
	 */
	private void dbExtraction(){
		//sqlite daten rausspeichern
		myDbs = new ArrayList<Path>();
		int foundDbs = 0;

		try {
			for (Path curr : myPersonalDataPath) {
				if (curr != null) {
					String path;
					try {
						path = curr.toString().toLowerCase();
					} catch (Exception e) {
						e.printStackTrace();
						path = "";
					}

					if (path.contains(".sqlite")) {
						myDbs.add( curr);
						foundDbs++;
					} else if (path.contains("web data")) {
						myDbs.add(curr);
						foundDbs++;
					}

				}
			}
		}catch(Exception e){e.printStackTrace();}

		//Db-Files aus Liste mit anderen Files löschen
		for(int i=0; i<foundDbs; i++) {
			try {

				myPersonalDataPath.remove(myDbs.get(i));
			}catch(Exception e){

			}
		}
	}

	/**
	 * übermittelt die gesammelten Resultate der FireFox und Chrome analyse bezüglich der
	 * FormFields gesammelt an die Global Data Klasse
	 */
	private void transmitBrowserForensik(){
		if(myDbResults!=null){
			if(myDbResults.size()>0){
				GlobalData gd=GlobalData.getInstance();
				for(SqlSelectSaver curr: myDbResults){
					gd.proposeData(curr.title,curr.value,curr.hitCount);
				}
			}
		}

	}

	}

