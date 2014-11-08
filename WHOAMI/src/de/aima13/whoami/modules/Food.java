package de.aima13.whoami.modules;


import de.aima13.whoami.Analyzable;
import de.aima13.whoami.GlobalData;
import de.aima13.whoami.Whoami;
import de.aima13.whoami.support.DataSourceManager;
import de.aima13.whoami.support.Utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by momoXD007 on 17.10.14.
 * Edited by marvinklose on 1.11.14
 */
public class Food implements Analyzable {

	//URLs nach dennen gesucht werden soll
	private static final String[] MY_SEARCH_DELIEVERY_URLS = {"lieferheld", "pizza.de"};
	private static final String[] MY_SEARCH_COOKING_URLS = {"chefkoch.de", "thestonerscookbook.com"};
	//Größen ab dennen Rezepte gewertet werden in Byte
	private static final int MINIMUM_DOCX_SIZE = 20000;
	private static final int MINIMUM_TXT_SIZE = 0;
	//Ab so vielen Bytes über dem Limit gibt es Punkte
	private static final int NEXT_RECIPE_POINT = 500;
	//gibt die maximale Größe an bis zu der es Punkte gibt (in Byte)
	private static final long MAXIMUM_FILE_SIZE = 100000000;
	private static final String MY_NAME = "Essgewohnheiten";
	private List<Path> myFoodFiles;
	private List<Path> myDbs;
	//besonderheiten
	private String myRecipeHtml="";
	private String myDelServieHtml="";
	private String myOnCookHtml="";
	private TreeMap<String, String> myCsvData = new TreeMap<String, String>();

	@Override
	public List<String> getFilter() {
		List<String> searchList = new ArrayList<String>();
		searchList.add("**Rezepte**.txt");
		searchList.add("**Rezepte**.docx");

		searchList.add("**Rezept**.txt");
		searchList.add("**Rezept**.docx");

		searchList.add("**rezept**.txt");
		searchList.add("**rezept**.docx");

		searchList.add("**backen**.txt");
		searchList.add("**backen**.docx");

		searchList.add("**Kuchen**.txt");
		searchList.add("**Kuchen**.docx");

		searchList.add("**Kochen**.txt");
		searchList.add("**Kochen**.docx");

		searchList.add("**Pizza**.txt");
		searchList.add("**Pizza**.docx");


		//places.sql gehört zu Firefox
		searchList.add("**Firefox**places.sqlite");

		//* hier weil History Datein gibt es zu viele und Chrome kann mehrere Benutzer verwalten
		searchList.add("**Google/Chrome**History");


		return searchList;
	}

	@Override
	public void setFileInputs(List<Path> files) throws Exception {
		if (files == null) {
			throw new IllegalArgumentException("I need input to do stuff");
		} else {
			myFoodFiles = files;
		}
	}

	@Override
	public String getHtml() {
		String myHtml="";
		//Sicherheitsabfrage ob HTML auch wirklich valide ist
		if(myRecipeHtml.length()>1 && myRecipeHtml.endsWith("</p>\n")){
			myHtml+=myRecipeHtml;
		}
		if(myDelServieHtml.length()>1 && myDelServieHtml.endsWith("</p>\n")){
			myHtml+=myDelServieHtml;
		}
		if(myOnCookHtml.length()>1 && myOnCookHtml.endsWith("</p>\n")){
			myHtml+=myOnCookHtml;
		}
		if(myHtml.length()>1) {
			return myHtml;
		}else{
			return null;
		}
	}

	@Override
	public String getReportTitle() {
		return MY_NAME;
	}

	@Override
	public String getCsvPrefix() {
		return MY_NAME;
	}

	@Override
	public String[] getCsvHeaders() {
		return new String[0];
	}

	@Override
	public SortedMap<String, String> getCsvContent() {

		return myCsvData;
	}

	/**
	 * Diese Methode versucht anhand der URL herauszufinden um welches Gericht es sich bei einer
	 * gegeben url von chefkoch.de handelt
	 *
	 * @param url eine URL von Chefkoch
	 * @return DEr Name des Gerichtes wird zurückgegeben
	 */
	private String parseChefkochUrl(String url) {
		String[] urlParts = url.split("/");
		//immer den letzten PArt nehmen, da der anfangsteil immer varieren kann
		url = urlParts[urlParts.length - 1];
		//jetzt noch Bindestriche und .html entfernen
		url = url.replace('-', ' ');
		if(url.length()>5) {
			url = url.substring(0, url.length() - 5);
		}
		return url;
	}

	@Override
	public void run() {
		if (Whoami.getTimeProgress() < 100) {
			this.dbExtraction();
		}
		if (Whoami.getTimeProgress() < 100) {
			this.recipeAnalysis();
		}

		if (Whoami.getTimeProgress() < 100) {
			this.analyzeDelieveryServices();
		}
		if (Whoami.getTimeProgress() < 100) {
			this.analyzeOnlineCookBooks();
		}

	}

	/**
	 * Filtert aus allen Daten die für dieses Modul die SQlite DBs heraus da diese anders
	 * behandelt werden als "normale" Dateien.
	 */
	private void dbExtraction() {
		//sqlite daten rausspeichern
		myDbs = new ArrayList<Path>();
		ArrayList<Path> myTrash = new ArrayList<>();
		int foundDbs = 0;
		try {
			for (Path curr : myFoodFiles) {
				if (curr != null) {
					String path;
					try {
						path = curr.toString();//getCanonicalPath();
					} catch (Exception e) {
						e.printStackTrace();
						path = "";
					}

					if (path.contains("places.sqlite")) {
						foundDbs++;
						myDbs.add(curr);
					} else if (path.contains("History")) {
						foundDbs++;
						myDbs.add(curr);
					} else if (path.contains(".sqlite")) {
						myTrash.add(curr);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		//Db-Files aus myFoodFiles Liste löschen
		for (int i = 0; i < foundDbs; i++) {
			try {

				myFoodFiles.remove(myDbs.get(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//remove other found sqlite files
		for (Path curr : myTrash) {
			myFoodFiles.remove(curr);
		}

	}

	/**
	 * Diese Methode analaysiert Rezepte und bewertet sie anhand der Dateigröße und dem
	 * lastModified Date.Es leifert den Hauptteil zur Analyse.
	 */
	private void recipeAnalysis() {
		String localRecipeHtml="";
		//eigentliche Rezeptanalyse
		if (myFoodFiles != null && myFoodFiles.size() != 0) {

			localRecipeHtml += "<p>" + myFoodFiles.size() + " Rezepte wurden auf diesem PC gefunden" +
					".";
			myCsvData.put("Anzahl Rezepte", "" + myFoodFiles.size());

			if(myFoodFiles.size()>9 && myFoodFiles.size()<30){
				localRecipeHtml+=" Das ist schon ganz ok, aber Luft nach oben bleibt noch.";
			}else if(myFoodFiles.size()>30){
				localRecipeHtml+=" Das ist ziemlich viel. Du solltest mal überlegen ein Buch " +
						"zu verfassen";
			}else{
				localRecipeHtml+="Das ist nun wircklich noch Ausbaufähig.";
			}
			localRecipeHtml+="</p>\n";
			//herausfinden welche Datei zuletzt erzeugt wurde
			Path latestReciept = myFoodFiles.get(0);
			int lengthScore = 0;
			for (int i = 0; i < myFoodFiles.size(); i++) {
				Path curr;
				curr = myFoodFiles.get(i);


				try {
					if (Files.getLastModifiedTime(latestReciept).toMillis() < Files
							.getLastModifiedTime(curr).toMillis()) {

						latestReciept = curr;
					}

				} catch (IOException e) {
					//tue nichts-->vor Allem nicht abstürzen
				}

				try {
					lengthScore += this.analyzeRecipeSize(curr);
				} catch (IllegalArgumentException e) {

					//tue nichts-->vor Allem nicht abstürzen
				}
			}

			if(lengthScore>99){
				localRecipeHtml+="<p> Die Gesamtlänge deiner Rezepte ergab einen Längenscore " +
						"von: "+lengthScore +". Damit bist du an der Spitze!</p>";
			}else{
			}
			localRecipeHtml+="\n";
			myCsvData.put("lokaler Rezeptscore",lengthScore+"");




			//Dateiendung wird hier mit ausgegeben
			String latestRecieptPath = latestReciept.toString().toLowerCase();
			if(latestRecieptPath.endsWith(".docx") && latestRecieptPath.endsWith(".txt")) {
				localRecipeHtml += "<p>Zuletzt hast du das Rezept:\"" + latestReciept.getName(latestReciept
						.getNameCount() - 1).toString() + "\" bearbeitet.</p>\n";
				myCsvData.put("Zuletzt geändertes Rezept", latestReciept.getName(latestReciept
						.getNameCount() - 1).toString());
			}


		} else {
			localRecipeHtml += "<p>Keine Rezepte gefunden. Mami kocht wohl immer noch am besten, " +
					"was?</p>\n";
			GlobalData.getInstance().changeScore("Faulenzerfaktor", 5);

		}
		//so wir ähnlich einem Buffer alles HTML auf einmal übergeben
		myRecipeHtml=localRecipeHtml;
	}

	/**
	 * Diese Methode liefert einen Beitrag zur Analyse indem sie die Browserverläufe nach
	 * bestimmten Lieferservice anlaysiert.
	 * Alle Lieferservices werden nach Anazahl der visits bewertet.
	 * Es wird nicht ausgewertet was für Gerichte sich der Nutzer angesehen hat.
	 */
	private void analyzeDelieveryServices() {
		String localDelServiceHtml="";
		boolean pizzaFound = false;
		int countDeliveryServices = 0;

		if (myDbs.size() > 0) {


			ResultSet[] dbResult = this.getViewCountAndUrl(MY_SEARCH_DELIEVERY_URLS);
			for (ResultSet resultSet : dbResult) {

				try {
					if (resultSet != null) {
						//resultSet.beforeFirst();
						while (resultSet.next()) {
							String currUrl = resultSet.getString("url");
							if (currUrl.contains("pizza")) {
								pizzaFound = true;
							}

							countDeliveryServices += resultSet.getInt("visit_count");

						}
						resultSet.getStatement().close();
						resultSet.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			myCsvData.put("Webzugriffe auf Lieferservices:",""+countDeliveryServices);
			localDelServiceHtml+="<p>Du hast: "+ countDeliveryServices  +" mal auf die Website eines " +
					"bekannten Lieferservices zugegriffen.";

			if(countDeliveryServices<100){
				localDelServiceHtml+=" Das ist aber nicht oft. Biste pleite oder was?";
			}else{
				localDelServiceHtml+="Uhh das ist aber ordentlich, du scheinst aber <b>mächtig Abetitt</b> zu " +
						"haben.";
			}

			localDelServiceHtml+="</p>\n";

			if (pizzaFound) {
				GlobalData.getInstance().changeScore("Nerdfaktor", 5);
				GlobalData.getInstance().changeScore("Faulenzerfaktor", 5);
				myCsvData.put("Pizzaliebhaber","ja");
				localDelServiceHtml+="<p>Du scheinst auch Pizza zu mögen ;)</p>\n";
			}else{
				myCsvData.put("Pizzaliebhaber","nein");
				localDelServiceHtml+="<p>Du ist keine Pizza, was ist denn mit dir falsch?</p>\n";
			}

			GlobalData.getInstance().changeScore("Faulenzerfaktor", countDeliveryServices * 3);
		}
		myDelServieHtml=localDelServiceHtml;


	}

	/**
	 * Diese Methode liefert eienen Beitrag zur Analyze indem sie die gefundenen Browserverläufe
	 * nach Einträgen gängiger Online -Kochseiten durchsucht und zumindest bei Chefkoch auch
	 * parst welche Gerichte am häufigsten aufgerufen wurden
	 */
	private void analyzeOnlineCookBooks() {
		String localOnCookHtml="";
		ResultSet[] dbResults = this.getViewCountAndUrl(MY_SEARCH_COOKING_URLS);
		TreeMap<String, Integer> chefKochReciepts = new TreeMap<String,Integer>();
		int countCookingSiteAccess = 0;
		boolean clientIsStoner = false;
		for (ResultSet rs : dbResults) {
			try {
				if (rs != null) {
					while (rs.next()) {
						String currUrl = rs.getString("url");
						if (currUrl.contains("chefkoch") && currUrl.length() > 23 && !currUrl
								.contains("suche")) {
							String cookingRecipe = this.parseChefkochUrl(currUrl);
							int clicksOnRecipe = rs.getInt("visit_count");
							countCookingSiteAccess += clicksOnRecipe;

							if (chefKochReciepts.containsKey(cookingRecipe)) {
								clicksOnRecipe += chefKochReciepts.get(cookingRecipe);
							}else {
								chefKochReciepts.put(cookingRecipe, clicksOnRecipe);
							}

						} else if (currUrl.toLowerCase().contains("thestonerscookbook")) {
							clientIsStoner = true;
						}
					}
					rs.getStatement().close();
					rs.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(clientIsStoner){
			localOnCookHtml+="<p><font color=#00C000>Tja du hast wohl den grünen Gaumen oder " +
					"bist öfters in den Niederlanden. ;)</font></p>\n";

			//myCsvData.put("Niederländer","ja");
		}else{
			//myCsvData.put("Niederländer","nein");
		}
		localOnCookHtml+="<p>"+ countCookingSiteAccess +" Zugriffe auf Online-Kochbücher detektiert. ";
		myCsvData.put("Zugriffe auf Online-Kochseiten",""+countCookingSiteAccess);
		if(countCookingSiteAccess<100){
			localOnCookHtml+="Das ist aber nicht oft...Dein Essen verbrennt wohl ab und an mal :D";
			GlobalData.getInstance().changeScore("Faulenzerfaktor", 5);

		}else{
			localOnCookHtml+="Schon ganz ordentlich, du scheinst kulinarisch was drauf zu haben.";
		}
		localOnCookHtml+="</p>\n";

		//Chefkoch Rezepte auswerten

		if (chefKochReciepts.size() > 2) {
			localOnCookHtml += "<p>";
			//ersten drei Top-Hits ausgeben(sortiert nach visit_count):
			for (int i = 1; i < 4; i++) {
				try {
					Map.Entry<String, Integer> highestEntry = Utilities.getHighestEntry(chefKochReciepts);
					localOnCookHtml += "Dein Nummer " + i + " Rezept auf Chefkoch ist:\"" + highestEntry.getKey
							() + "\".";
					myCsvData.put("Chefkoch Top-" + i, String.valueOf(highestEntry.getValue()));
					chefKochReciepts.remove(highestEntry.getKey());
				}catch (NoSuchElementException e){
					// hier erzeuge auch irgendetwas
				}

			}
			localOnCookHtml += "</p>\n";
		}
		myOnCookHtml=localOnCookHtml;

	}

	/**
	 * Diese Methode durchsucht die gefundenen Browserverläufe nach den mitgegebenen URLs
	 *
	 * @param searchUrl Ein String-Array welches URLs enthält nach dennen gesucht werden soll
	 * @return ein Array von ResultSets welches die Ergebnisse der SQL-Abfragen enthält(kann auch
	 * leer sein)
	 */
	private ResultSet[] getViewCountAndUrl(String[] searchUrl) {
		ResultSet[] results;
		List<ResultSet> resultList= new LinkedList<ResultSet>();
		DataSourceManager dbManager = null;
		for (Path db : myDbs) {
			String sqlStatement = "SELECT url,visit_count ";
			if (db != null) {
				String path = "";
				try {
					path = db.toString();
				} catch (Exception e) {
					path = "";
				}
				path = path.toLowerCase();
				if (path.contains("firefox")) {
					sqlStatement += "FROM moz_places ";
				} else if (path.contains("google")) {
					sqlStatement += "FROM urls ";
				}
				//Suchbegriffe in Statement einbauen
				sqlStatement += "WHERE url LIKE '%" + searchUrl[0] + "%' ";
				for (int i = 1; i < searchUrl.length; i++) {
					sqlStatement += "OR url LIKE '%" + searchUrl[i] + "%' ";
				}
				try {
					dbManager = new DataSourceManager(db);
				} catch (ClassNotFoundException | SQLException e) {
					dbManager = null;
				}
				if (dbManager != null) {
					try {
						ResultSet rs = dbManager.querySqlStatement(sqlStatement);
						if(rs !=null){
							resultList.add(rs);
						}
					} catch (SQLException e) {
						//anscheinend ist das SQL-Statement fehlerhaft oder die Datenbank hat in
						// irgend einer Weise Fehler --> kann man nichts gegen machen
						e=e;
					}

				}
			}
		}
		//ab hier wird keine dynamische Datenstrucktur mehr benötigt
		results=new ResultSet[resultList.size()];
		results=resultList.toArray(results);
		return results;
	}

	/**
	 * Diese Methode bewertet Dateien anhand ihrer Größe. Dies geht nur für .txt und .docx Dateien
	 *
	 * @param recipe muss ein Pfad zu einer docx oder txt Datei sein
	 * @return ein Wert der Schrittweise angibt
	 *
	 * @throws IllegalArgumentException falls die Datei nicht im txt oder docx Format ist
	 */
	private int analyzeRecipeSize(Path recipe) throws IllegalArgumentException {
		int vote = 0;
		String ending = recipe.toString();
		//hole nur die letzten paar Zeichen die auf jeden Fall auch die Dateiendung enthalten
		ending = ending.substring(ending.length() - 7, ending.length());

		long size;
		try {
			size = Files.size(recipe);
		} catch (IOException e) {
			size = 0;
		}
		if (ending.contains("docx")) {
			for (long i = MINIMUM_DOCX_SIZE; i < size && i < MAXIMUM_FILE_SIZE;
			     i += NEXT_RECIPE_POINT) {
				vote++;
			}


		} else if (ending.contains("txt")) {
			for (long i = MINIMUM_TXT_SIZE; i < size && i < MAXIMUM_FILE_SIZE;
			     i += NEXT_RECIPE_POINT) {
				vote++;
			}

		} else {
			throw new IllegalArgumentException("Can't analyze this type of file");
		}
		return vote;
	}

}
