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

	@Override
	public List<String> getFilter() {
		ArrayList<String> myFilters = new ArrayList<String>();
		//		SQLite Datenbanken der Browser
		myFilters.add("**Firefox**places.sqlite");
		myFilters.add("**Google/Chrome**History");
		return myFilters;
	}

	@Override
	public void setFileInputs(List<Path> files) throws Exception {
		inputFiles = files;
	}

	@Override
	public String getHtml() {
		Map.Entry mostPopularSport = Utilities.getHighestEntry(sportPopularity);
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

	@Override
	public SortedMap<String, String> getCsvContent() {
		TreeMap<String,String> csvResult = new TreeMap<String,String>();
		csvResult.put("1",Utilities.getHighestEntry(sportPopularity).getKey().toString());
		return csvResult;
	}

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

	private void handleBrowserHistory(Path sqliteDB, String fromTable) {
		try {
			DataSourceManager dSm = new DataSourceManager(sqliteDB);
			for (Sportart s : sportsList){
				String sqlStatement  = "SELECT count(*) FROM " + fromTable+ " where title LIKE '%"+
						s.sportart+"%' or url LIKE '%"+s.sportart+"%'";
				if(s.zusatz !=null) {
					for (String addition : s.zusatz) {
						sqlStatement += "OR url LIKE '%" + addition + "%' ";
						sqlStatement += "OR title LIKE '%" + addition + "%'";
					}
				}
				sqlStatement += " ;";
				ResultSet rs = dSm.querySqlStatement(sqlStatement);
				while (rs.next()){
					sportPopularity.put(s.sportart, sportPopularity.get(s.sportart)+ rs.getInt(1));
				}
			}
		} catch (ClassNotFoundException e) {

		} catch (SQLException e) {

		}
	}

	private class Sportart{
		String sportart;
		String [] zusatz;
	}
}
