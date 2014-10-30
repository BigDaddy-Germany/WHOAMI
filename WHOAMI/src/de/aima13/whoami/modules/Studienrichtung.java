package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.support.DataSourceManager;
import de.aima13.whoami.support.Utilities;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Marvin on 28.10.2014.
 */
public class Studienrichtung implements Analyzable{
	private List<Path> myFiles = new ArrayList<Path>();

	private Studiengang[] courseList;
	private Kurskalendermap[] calenderCourseList;
	private ArrayList<CourseVisitedEntry> viewedSchedules;

	@Override
	public List<String> getFilter() {
		List<String> myFilters = new ArrayList<String>();

//		places.sql gehört zu Firefox
		myFilters.add("**Firefox**places.sqlite");

		//* hier weil History Datein gibt es zu viele und Chrome kann mehrere Benutzer verwalten
		myFilters.add("**Google/Chrome**History");

		myFilters.add("**/*.dropbox");
		return myFilters;
	}

	/**
	 * Setzen der für das Modul gefundenen Dateien
	 *
	 * @param files Liste der gefundenen Dateien
	 * @throws Exception Ein Fehler ist aufgetreten
	 * @author Marco Dörfler
	 */
	@Override
	public void setFileInputs(List<Path> files) throws Exception {
		myFiles = files;
	}


	@Override
	public String getHtml() {
		return null;
	}


	@Override
	public String getReportTitle() {
		return "Deine Studienrichtung";
	}


	@Override
	public String getCsvPrefix() {
		return "dhbw";
	}

	@Override
	public SortedMap<String, String> getCsvContent() {
		return null;
	}


	@Override
	public void run() {
		courseList = Utilities.loadDataFromJson("/data/studiengaenge.json", Studiengang[].class);
		calenderCourseList = Utilities.loadDataFromJson("/data/studienbezeichner.json",
				Kurskalendermap[].class);
		ArrayList<CourseVisitedEntry> viewedSchedules = getViewedCalenders();
		for (CourseVisitedEntry entry : viewedSchedules){
			//joine restlichen Informationen dran
			entry.kurzbez= this.getKursById(entry.courseID);
			entry.name = getNameFromPreAndSuffix(entry.kurzbez);
		}
		analyzePathNames(viewedSchedules);
	}

	private void analyzePathNames(ArrayList<CourseVisitedEntry> viewedSchedules) {
		int max = viewedSchedules.get(0).visitCount;
		for (CourseVisitedEntry entry : viewedSchedules){
			for (Path p : myFiles){
				String [] pathParts = p.getParent().toString().split("\\");
				for (String partOfPath : pathParts){
					float samenessLevel = 0.50f;
					while (Utilities.isRoughlyEqual(partOfPath,entry.kurzbez,samenessLevel)){
						samenessLevel += 0.05f;
					}
					if (samenessLevel >= 0.65f){
						entry.visitCount += 10*samenessLevel;
					}
				}
			}
		}
	}

	private String getNameFromPreAndSuffix(String kursbez) {
		String prefix;
		String suffix;
		Pattern p = Pattern.compile("[A-Z]*");
		Matcher m = p.matcher( kursbez );
		m.find(); prefix = m.group(); //TINF
		m.find(); // 1 wird nicht gematched weil Zahlen nicht in  [A-Z]
		m.find(); // 3 wie oben
		m.find(); suffix = m.group(); //AIBC

		for (Studiengang course : courseList){
			if(course.prefix.equals(prefix)){
				if (course.suffix!=null && Arrays.asList(course.suffix).contains(suffix)){
					return course.name;
				}
				return course.name;
			}
		}
		return "";
	}

	private class Studiengang{
		String name;
		String prefix;
		String [] suffix;
	}
	private class Kurskalendermap{
		String kursbez;
		String id;
	}

	private class CourseVisitedEntry{
		public CourseVisitedEntry(String id, int count){
			courseID = id;
			visitCount = count;
		}
		String courseID;
		int visitCount;
		String kurzbez;
		String name;
	}
	private class EntryComparator implements Comparator<CourseVisitedEntry> {
		@Override
		public int compare(CourseVisitedEntry first, CourseVisitedEntry second) {
			if(first.visitCount == second.visitCount){
				return 0;
			}else if(first.visitCount < second.visitCount){
				return 1;
			}else {
				return -1;
			}
		}
	}
	private ArrayList<CourseVisitedEntry> getViewedCalenders(){
		ArrayList<CourseVisitedEntry> result = new ArrayList<CourseVisitedEntry>();
		for (Path dbPath : this.myFiles){
			String fromTable="";

			if (dbPath.toString().contains("Firefox")){
				fromTable= "moz_places";
			}else if(dbPath.toString().contains("Chrome")){
				fromTable = "urls";
			}else{
				break;
			}
			try {
				DataSourceManager dSm = new DataSourceManager(dbPath);
				ResultSet rs = dSm.querySqlStatement("SELECT substr(url,instr(url,'uid=')+4,7) " +
						"kurs, sum(visit_count) aufrufe " +
						"FROM "+ fromTable +" "+
						"WHERE url LIKE '%vorlesungsplan.dhbw-mannheim.de/%uid=%' " +
						"GROUP BY kurs " +
						"ORDER BY aufrufe DESC "+
						"LIMIT 10;");
				while (rs.next()){
					if(!resultContainsID(result,rs.getString(1))){
						result.add(new CourseVisitedEntry(rs.getString(1),rs.getInt(2)));
					}else {
						summarizeVisitCount(result,rs.getString(1),rs.getInt(2));
					}
				}
				rs.getStatement().close();
				rs.close();
			} catch (ClassNotFoundException | SQLException e) {
			}
		}
		Collections.sort(result, new EntryComparator());
		return result;
	}

	private void summarizeVisitCount(ArrayList<CourseVisitedEntry> result, String id,
	                                 int inkrrement) {
		for(CourseVisitedEntry entry : result){
			if(entry.courseID.equals(id)){
				entry.visitCount += inkrrement;
			}
		}
	}

	private boolean resultContainsID(ArrayList<CourseVisitedEntry> input,String id){
		for (CourseVisitedEntry entry : input){
			if (entry.courseID.equals(id)){
				return true;
			}
		}
		return false;
	}
	private String getKursById(String lookUpID){
		for(Kurskalendermap mapping : this.calenderCourseList){
			if (mapping.id.equals(lookUpID)){
				return mapping.kursbez;
			}
		}
		return "Unbekannt";
	}
}
