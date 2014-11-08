package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.support.DataSourceManager;
import de.aima13.whoami.support.Utilities;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Marvin on 03.11.2014.
 */
public class Sports implements Analyzable{
	private List<Path> inputFiles = new ArrayList<Path>();
	private TreeMap<String,Integer> sportPopularity = new TreeMap<String,Integer>();
	private final String SPORTS_TITLE = "Sport";
	private Sportart[] sportsList;

	/**
	 * Modul arbeitet mit SQLite von Firefox oder Chrome
	 * @return List der Strings die den Filter spezifizieren
	 */
	@Override
	public List<String> getFilter() {
		ArrayList<String> myFilters = new ArrayList<String>();
		//		SQLite Datenbanken der Browser
		myFilters.add("**Firefox**places.sqlite");
		myFilters.add("**Google/Chrome**History");
		return myFilters;
	}

	/**
	 * Setzen der Pfad Objekte die behandelt werden sollen
	 * @param files Liste der gefundenen Dateien
	 * @throws Exception
	 */
	@Override
	public void setFileInputs(List<Path> files) throws Exception {
		inputFiles = files;
	}

	/**
	 * Setzt sich zusammen aus dem HTML String und der populärsten Sportart.
	 * @return HTML für den Bericht.
	 */
	@Override
	public String getHtml() {
		Map.Entry mostPopularSport = Utilities.getHighestEntry(sportPopularity);
		if((Integer)mostPopularSport.getValue()<25){
			return "Du scheinst dich nicht viel für Sport zu interessieren!";
		}
		return "Warum auch immer interessiert du dich am meisten für "+ mostPopularSport.getKey()
				+"!";
	}

	@Override
	public String getReportTitle() {
		return SPORTS_TITLE;
	}

	@Override
	public String getCsvPrefix() {
		return SPORTS_TITLE;
	}

	/**
	 * @return TreeMap die den Inhalt für die CSV-Datei bereitstellt.
	 */
	@Override
	public SortedMap<String, String> getCsvContent() {
		TreeMap<String,String> csvResult = new TreeMap<String,String>();
		csvResult.put("1",Utilities.getHighestEntry(sportPopularity).getKey().toString());
		return csvResult;
	}

	/**
	 * Run lädt die Resourcen und iteriert dann über die Browser Chroniken.
	 * Dabei liegt der Fokus primär entweder auf Suche nach der Sportart selbst oder
	 * nach bestimmten Keywords die zusätzlich angegeben werden können.
	 */
	@Override
	public void run() {
		sportsList = Utilities.loadDataFromJson("/data/sport.json",Sportart[].class);
		for(Sportart s : sportsList){
			sportPopularity.put(s.sportart,0);
		}
		for (Path p : this.inputFiles){
			if (p.toString().contains("Firefox")){
				handleBrowserHistory(p,"moz_places");
			}else if(p.toString().contains("Google/Chrome")){
				handleBrowserHistory(p,"urls");
			}
		}
	}

	/**
	 * Select prüft ob die Sportart in der URL oder im TITLE steht. Dabei interessiert nur die
	 * Anzahl der Aufrufe. Zusätzlich kann nach weiteren beliebigen String in den 2 Spalten
	 * gesucht werden.
	 * @param sqliteDB  Pfad zu SQLite Datenbank
	 * @param fromTable Tabelle von der gelesen werden soll. Also entweder moz_places oder urls je
	 *                  nachdem ob es sich ob Firefox oder Chrome handelt
	 */
	private void handleBrowserHistory(Path sqliteDB, String fromTable) {
		try {
			DataSourceManager dSm = new DataSourceManager(sqliteDB);
			for (Sportart s : sportsList){
				String sqlStatement  = "SELECT count(*) FROM " + fromTable+ " where title LIKE '%"+
						s.sportart+"%' or url LIKE '%"+s.sportart+"%'";
				if(s.zusatz !=null) {
					for (String addition : s.zusatz) {
						sqlStatement += "OR url LIKE '%" + addition + "%' ";
						sqlStatement += "OR title LIKE '%" + addition + "%' ";
					}
				}
				sqlStatement += ";";
				ResultSet rs = dSm.querySqlStatement(sqlStatement);
				if(rs != null){
					while (rs.next()){
						sportPopularity.put(s.sportart, sportPopularity.get(s.sportart)+ rs.getInt(1));
					}
				}

			}
		} catch (ClassNotFoundException e) {

		} catch (SQLException e) {

		}
	}
	// Klasse zum Laden der JSON Resource
	private class Sportart{
		String sportart;
		String [] zusatz;
		@Override
		public String toString(){
			return sportart + " Extras:"+Arrays.toString(zusatz);
		}
	}
}
