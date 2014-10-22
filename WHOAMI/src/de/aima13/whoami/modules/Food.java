package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.GlobalData;
import de.aima13.whoami.support.DataSourceManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.nio.file.Path;

/**
 * Created by momoXD007 on 17.10.14.
 */
public class Food implements Analyzable {

	private List<Path> myFoodFiles;
	private List<Path> myDbs;
	//besonderheiten
	private String myHtml = "<h1>Essen</h1>\n";
	private TreeMap<String, String> myCsvData = new TreeMap<String, String>();


	//URLs nach dennen gesucht werden soll
	private static  final String[] MY_SEARCH_DELIEVERY_URLS={"lieferheld","pizza.de"};
	private static  final String[] MY_SEARCH_COOKING_URLS={"chefkoch.de","thestonerscookbook.com"};


	@Override
	public List<String> getFilter() {
		List<String> searchList = new ArrayList<String>();
		searchList.add("**" + File.separator + "Rezepte" + File.separator + "**");
		searchList.add("**" + File.separator + "Rezept" + File.separator + "**");
		searchList.add("**" + File.separator + "rezept" + File.separator + "**");
		searchList.add("**" + File.separator + "backen" + File.separator + "**");
		searchList.add("**" + File.separator + "Kuchen" + File.separator + "**");
		searchList.add("**" + File.separator + "Pizza" + File.separator + "**");


		//places.sql gehört zu Firefox
		searchList.add("**Firefox**places.sqlite");

		//* hier weil History Datein gibt es zu viele und Chrome kann mehrere Benutzer verwalten
		searchList.add("**Google"+File.separator+"Chrome**History");


		return searchList;
	}

	@Override
	public void setFileInputs(List<Path> files) throws Exception {
		if (files == null) {
			throw new IllegalArgumentException("I need input to do stuff");
		}
		else {
			myFoodFiles = files;
		}
	}

	@Override
	public String getHtml() {

		return myHtml;
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
		url = url.substring(0, url.length() - 5);
		return url;
	}

	@Override
	public void run() {
		//*********************debugging*******

		//		Path f = new Path("/Volumes/internal/debugg/Firefox/witzig/places.sqlite");
		//	myFoodFiles = new ArrayList<Path>();
		//	myFoodFiles.add(f);
		//String x = this.parseChefkochUrl("http://www.chefkoch" +
				//".de/rezepte/1108101216891426/Apfelkuchen-mit-Streuseln-vom-Blech.html");

		//*************************************************************


		//sqlite daten rausspeichern
		myDbs = new ArrayList<Path>();
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

					myFoodFiles.remove(myDbs.get(i));
				}catch(Exception e){
					e.printStackTrace();
				}
			}



		if (myFoodFiles != null && myFoodFiles.size() != 0) {

			myHtml += "<p>" + myFoodFiles.size() + " Rezepte wurden auf diesem PC gefunden.\n";
			myCsvData.put("Anzahl Rezepte", "" + myFoodFiles.size());

			//herausfinden welche Datei zuletzt erzeugt wurde


			//TODO: Run Methode in sinnvolle Untermethoden aufschlüsseln
			//TODO: IN FOR EACH SCHLEIFE UMWANDELN DA PERFORMANTER
			Path latestReciept = myFoodFiles.get(0);

			for (int i = 1; i < myFoodFiles.size(); i++) {
				Path curr;
				curr = myFoodFiles.get(i);
				try {
						if (Files.getLastModifiedTime(latestReciept).toMillis() < Files
								.getLastModifiedTime(curr).toMillis()){

							latestReciept = curr;
						}
					}catch(IOException e){

			}
			}
			//Dateiendung wird hier mit ausgegeben
			myHtml += "<p>Zuletzt hast du das Rezept:\"" + latestReciept.getName(latestReciept
					.getNameCount()-1).toString()+ "\" bearbeitet.</p>\n";
			myCsvData.put("Zuletzt geändertes Rezept",  latestReciept.getName(latestReciept
			.getNameCount()-1).toString());
		}
		else {
			myHtml += "<p>Keine Rezepte gefunden. Mami kocht wohl immer noch am besten, was?</p>\n";
			GlobalData.getInstance().changeScore("Faulenzerfaktor", 5);
		}
		this.analyzeDelieveryServices();
		this.analyzeOnlineCookBooks();


	}

	/**
	 * Diese Methode liefert einen Beitrag zur Analyse indem sie die Browserverläufe nach
	 * bestimmten Lieferservice anlaysiert.
	 * Alle Lieferservices werden nach Anazahl der visits bewertet.
	 * Es wird nicht ausgewertet was für Gerichte sich der Nutzer angesehen hat.
	 */
	private void analyzeDelieveryServices() {
		boolean pizzaFound = false;
		int countDeliveryServices = 0;

		if (myDbs.size() > 0) {


			ResultSet[] dbResult = this.getViewCountAndUrl(MY_SEARCH_DELIEVERY_URLS);
			for (ResultSet resultSet : dbResult) {

				try {
					if(resultSet!=null) {
						//resultSet.beforeFirst();
						while (resultSet.next()) {
							String currUrl = resultSet.getString("url");
							if (currUrl.contains("pizza")) {
								pizzaFound = true;
							}

							countDeliveryServices += resultSet.getInt("visit_count");

						}
						resultSet.getStatement().close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			myCsvData.put("Webzugriffe auf Lieferservices:",""+countDeliveryServices);
			myHtml+="<p>Du hast: "+ countDeliveryServices  +" mal auf die Website eines " +
					"bekannten Lieferservices zugegriffen.";

			if(countDeliveryServices<100){
				myHtml+=" Das ist aber nicht oft. Biste pleite oder was?";
			}else{
				myHtml+="Uhh das ist aber ordentlich, du scheinst aber <b>mächtig Abetitt</b> zu " +
						"haben.";
			}

					myHtml+="</p>\n";

			if(pizzaFound){
				GlobalData.getInstance().changeScore("Nerdfaktor", 5);
				GlobalData.getInstance().changeScore("Faulenzerfaktor", 5);
				myCsvData.put("Pizzaliebhaber","ja");
				myHtml+="<p>Du scheinst auch Pizza zu mögen ;)</p>\n";
			}else{
				myCsvData.put("Pizzaliebhaber","nein");
				myHtml+="<p>Du ist keine Pizza, was ist denn mit dir falsch?</p>\n";
			}

			GlobalData.getInstance().changeScore("Faulenzerfaktor", countDeliveryServices * 3);
		}


	}

	/**
	 * Diese Methode liefert eienen Beitrag zur Analyze indem sie die gefundenen Browserverläufe
	 * nach Einträgen gängiger Online -Kochseiten durchsucht und zumindest bei Chefkoch auch
	 * parst welche Gerichte am häufigsten aufgerufen wurden
	 */
	private void analyzeOnlineCookBooks() {

		ResultSet[] dbResults = this.getViewCountAndUrl(MY_SEARCH_COOKING_URLS);
		TreeMap<Integer,String> chefKochReciepts = new TreeMap<>();
		int countCookingSiteAccess=0;
		boolean clientIsStoner=false;
		for(ResultSet rs:dbResults){
			try {
				if(rs!=null) {
					//resultSet.beforeFirst();
					while (rs.next()) {
						String currUrl = rs.getString("url");
						if (currUrl.contains("chefkoch")&& currUrl.length()>23 && !currUrl
								.contains("suche")) {
							chefKochReciepts.put(rs.getInt("visit_count"),
									this.parseChefkochUrl(currUrl));
						}else if(currUrl.toLowerCase().contains("thestonerscookbook")){
							clientIsStoner=true;
						}

						countCookingSiteAccess += rs.getInt("visit_count");

					}
					rs.getStatement().close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(clientIsStoner){
			myHtml+="<p><font color=#00C000>Tja du hast wohl den grünen Gaumen oder " +
					"bist öfters in den Niederlanden. ;)</font></p>\n";
			myCsvData.put("Niederländer","ja");
		}else{
			myCsvData.put("Niederländer","nein");
		}
		myHtml+="<p>"+ countCookingSiteAccess +" Zugriffe auf Online-Kochbücher detektiert";
		myCsvData.put("Zugriffe auf Online-Kochseiten",""+countCookingSiteAccess);
		if(countCookingSiteAccess<100){
			myHtml+="Das ist aber nicht oft...Dein Essen verbrennt wohl ab und an mal :D";
			GlobalData.getInstance().changeScore("Faulenzerfaktor", 5);

		}else{
			myHtml+="Schon ganz ordentlich, du scheinst kulinarisch was drauf zu haben.";
		}
		myHtml+="</p>\n";

		//Chefkoch Rezepte auswerten

		if(chefKochReciepts.size()>5){
			myHtml+="<p>";
			//ersten drei Top-Hits ausgeben(sortiert nach visit_count):
			for(int i=1; i<5; i++) {
				int topHit = chefKochReciepts.firstKey();
				String recipeName=chefKochReciepts.get(topHit);
				myHtml+="Dein Nummer " + i +" Rezept auf Chefkoch ist:\"" + recipeName +"\".";
				myCsvData.put("Chefkoch Top-"+i,recipeName);
			}
			myHtml+="</p>\n";
		}


	}

	/**
	 *Diese Methode durchsucht die gefundenen Browserverläufe nach den mitgegebenen URLs
	 * @param searchUrl Ein String-Array welches URLs enthält nach dennen gesucht werden soll
	 * @return ein Array von ResultSets welches die Ergebnisse der SQL-Abfragen enthält(kann auch
	 * leer sein)
	 */
private ResultSet[] getViewCountAndUrl(String[] searchUrl) {
	ResultSet[] results=new ResultSet[2];
	String sqlStatement="SELECT url,visit_count ";
	DataSourceManager dbManager = null;
	int x=0;
	for (Path db: myDbs){

		if(db!=null){
			String path="";
			try {
				path = db.toString();
			}catch(Exception e){
				path="";
			}
			path=path.toLowerCase();
			if(path.contains("firefox")){
				sqlStatement+="FROM moz_places ";
			}else if(path.contains("google")){
				sqlStatement+="FROM urls ";
			}
			//Suchbegriffe in Statement einbauen
			sqlStatement+="WHERE url LIKE '%"+searchUrl[0]+"%' ";
			for (int i = 1; i <searchUrl.length ; i++) {
				sqlStatement+= "OR url LIKE '%"+searchUrl[i]+"%' ";
			}
			try {
				dbManager = new DataSourceManager(db);
			}catch(Exception e){
				dbManager=null;
			}
			if(dbManager!=null){
				try {
					results[x] = dbManager.querySqlStatement(sqlStatement);
				}catch(Exception e){
					results[x]=null;
				}

			}
			x++;
		}
	}


	return  results;
}



}
