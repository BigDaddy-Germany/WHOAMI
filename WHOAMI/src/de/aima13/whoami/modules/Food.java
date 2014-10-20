package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.GlobalData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by momoXD007 on 17.10.14.
 */
public class Food implements Analyzable {

	private List<File> myFoodFiles;
	//besonderheiten
	private String myHtml="<h1>Essen</h1>\n";
	private TreeMap<String, String> myCsvData = new TreeMap<String,String>();


	@Override
	public List<String> getFilter() {
		List<String> searchList = new ArrayList<String>();
		searchList.add("**"+File.separator+"Rezepte"+File.separator+"**");
		searchList.add("**"+File.separator+"Rezept"+File.separator+"**");
		searchList.add("**"+File.separator+"rezept"+File.separator+"**");
		searchList.add("**"+File.separator+"backen"+File.separator+"**");
		searchList.add("**"+File.separator+"Kuchen"+File.separator+"**");
		searchList.add("**"+File.separator+"Pizza"+File.separator+"**");

		return searchList;
	}

	@Override
	public void setFileInputs(List<File> files) throws Exception {
		if(files==null){
			throw new IllegalArgumentException("I need input to do stuff");
		}else{
			myFoodFiles =files;
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
	 * @param url eine URL von Chefkoch
	 * @return DEr Name des Gerichtes wird zurückgegeben
	 */
	private String parseChefkochUrl(String url){
		String[] urlParts = url.split("/");
		//immer den letzten PArt nehmen, da der anfangsteil immer varieren kann
		url=urlParts[urlParts.length-1];
		//jetzt noch Bindestriche und .html entfernen
		url=url.replace('-',' ');
		url=url.substring(0,url.length()-5);
		return url;
	}

	@Override
	public void run() {
		//*********************debugging for Unix Systems only*******

		//File f = new File("/Volumes/internal/debugg/Rezepte/Kuchen.txt");
		//myFoodFiles = new ArrayList<File>();
		//myFoodFiles.add(f);
		String x=this.parseChefkochUrl("http://www.chefkoch" +
				".de/rezepte/1108101216891426/Apfelkuchen-mit-Streuseln-vom-Blech.html");

		//*************************************************************




		if(myFoodFiles!=null && myFoodFiles.size()!=0  ) {

		myHtml+="<p>"+ myFoodFiles.size() + " Rezepte wurden auf diesem PC gefunden.\n";
		myCsvData.put("Anzahl Rezepte",""+ myFoodFiles.size());

		//herausfinden welche Datei zuletzt erzeugt wurde


			//TODO: Run Methode in sinnvolle Untermethoden aufschlüsseln
			//TODO: IN FOR EACH SCHLEIFE UMWANDELN DA PERFORMANTER
			File latestReciept = myFoodFiles.get(0);

			for (int i = 1; i < myFoodFiles.size(); i++) {
				File curr;
				curr = myFoodFiles.get(i);
				if (latestReciept.lastModified() < curr.lastModified()) {
					latestReciept = curr;
				}
			}
			//Dateiendung wird hier mit ausgegeben
			myHtml += "<p>Zuletzt hast du das Rezept:\"" + latestReciept.getName()
					+ "\" bearbeitet.</p>\n";
			myCsvData.put("Zuletzt geändertes Rezept", latestReciept.getName());
		}else{
			myHtml += "<p>Keine Rezepte gefunden. Mami kocht wohl immer noch am besten, was?</p>\n";
			GlobalData.getInstance().changeScore("Faulenzerfaktor",5);
		}
		this.analyzeDelieveryServices();
		this.analyzeOnlineCookBooks();


	}
	private void analyzeDelieveryServices(){
		boolean pizzaFound =false;
		int countDeliveryServices=0;
		//Suche nach Pizzerien/Lieferservicen


		if(pizzaFound){
			GlobalData.getInstance().changeScore("Nerdfaktor",5);
			GlobalData.getInstance().changeScore("Faulenzerfaktor", 5);
		}
		GlobalData.getInstance().changeScore("Faulenzerfaktor",countDeliveryServices*3);


	}
	private void analyzeOnlineCookBooks(){

	}
}
