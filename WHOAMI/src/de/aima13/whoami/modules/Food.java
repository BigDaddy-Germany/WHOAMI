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

	private List<File> myFiles;
	//besonderheiten
	private String myHtml="<h1>Essen</h1>";
	private TreeMap<String, String> myCsvData = new TreeMap<String,String>();


	@Override
	public List<String> getFilter() {
		List<String> searchList = new ArrayList<String>();
		searchList.add("Rezepte");
		searchList.add("Rezept");
		searchList.add("Rezept");
		searchList.add("backen");
		searchList.add("Kuchen");
		searchList.add("Pizza");

		return searchList;
	}

	@Override
	public void setFileInputs(List<File> files) throws Exception {
		if(files==null){
			throw new IllegalArgumentException("I need input to do stuff");
		}else{
			myFiles=files;
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

	@Override
	public void run() {
		if(myFiles.size()!=0) {

		myHtml+="<p>"+myFiles.size() + " Rezepte wurden auf diesem PC gefunden.";
		myCsvData.put("Anzahl Rezepte",""+myFiles.size());

		//herausfinden welche Datei zuletzt erzeugt wurde

			File lastCreated = myFiles.get(0);

			for (int i = 1; i < myFiles.size(); i++) {
				File curr;
				curr = myFiles.get(i);
				if (lastCreated.lastModified() < curr.lastModified()) {
					lastCreated = curr;
				}
			}
			myHtml += "<p>Zuletzt hast du das Rezept:\"" + lastCreated.getName() + "\" bearbeitet.</p>";
			myCsvData.put("Zuletzt geändertes Rezept", lastCreated.getName());
		}else{
			myHtml += "<p>Keine Rezepte gefunden. Mami kocht wohl immer noch am besten, was?</p>";
			//GlobalData.changeScore("Faulenzerfaktor",5);
		}

		//ToDo
		boolean pizzaFound =false;
		int countDeliveryServices=0;
		//Suche nach Pizzerien/Lieferservicen


		if(pizzaFound){
			//GlobalData.changeScore("Nerdfaktor",5);
			//GlobalData.changeScore("Faulenzerfaktor",5);
		}
		//GlobalData.changeScore("Faulenzerfaktor",countDeliveryServices*3);


	}
}
