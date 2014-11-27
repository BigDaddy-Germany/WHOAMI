package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.GlobalData;
import de.aima13.whoami.Whoami;
import de.aima13.whoami.support.DataSourceManager;
import de.aima13.whoami.support.Utilities;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Marvin on 28.10.2014.
 */
public class Studienrichtung implements Analyzable {
	private final float DROPBOX_WEIGHTING_FACTOR = 0.18f;
	private final float SAMENESS_WEIGHTING_FACTOR = 8f;
	private final int   INITITIAL_VALUE = 100;
	private List<Path> dropboxFiles = new ArrayList<Path>();
	private List<Path> databaseFiles = new ArrayList<Path>();
	private Studiengang[] courseList;
	private Kurskalendermap[] calenderCourseList;
	private ArrayList<CourseVisitedEntry> courseResult = new ArrayList<CourseVisitedEntry>();
	private boolean foundOnlineCalender = true;

	/**
	 * Das Modul interessiert sich für die Chrome und Firefox History zum Abgleich,
	 * mit dem Kurskalenderaufrufen und .dropbox die einen Shared-Folder identifizieren des
	 * Studiengangs.
	 *
	 * @return Liste die den Filter für diese Modul darstellt.
	 */
	@Override
	public List<String> getFilter() {
		List<String> myFilters = new ArrayList<String>();

		//		SQLite Datenbanken der Browser
		myFilters.add("**Firefox**places.sqlite");
		myFilters.add("**Google/Chrome**History");

		// Datei in Shared Foldern
		myFilters.add("**/*.dropbox");

		return myFilters;
	}

	/**
	 * Setzen der für das Modul gefundenen Dateien und einsortieren, damit es später schneller geht.
	 * Und nicht in der Logik nochmal getrennt werden muss oder über alle Paths iteriert wird.
	 *
	 * @param files Liste der gefundenen Dateien
	 * @throws Exception Ein Fehler ist aufgetreten
	 * @author Marco Dörfler
	 */
	@Override
	public void setFileInputs(List<Path> files) throws Exception {
		for (Path p : files) {
			if (p.toString().endsWith(".dropbox")) {
				dropboxFiles.add(p);
			} else if (p.toString().endsWith(".sqlite") || p.toString().endsWith("History")) {
				databaseFiles.add(p);
			} else {
				// irgendwas unbrauchbares
			}
		}

	}

	/**
	 * Aus den gefunden Ergebenissen wird ein netter Bericht erstellt.
	 *
	 * @return HTML als String der in Bericht einfließt.
	 */
	@Override
	public String getHtml() {
		StringBuilder html = new StringBuilder();
		String courseToken = "";
		String courseName = "";
		String lastCommentOnClass = "";
		String graduation = "";
		if (!foundOnlineCalender){
			html.append("Sag mal kommst du überhaupt pünktlich zu deinen Vorlesungen? Oder löscht" +
					" du einfach nur deinen Verlauf?");
		}
		if (courseResult.size() > 0 && courseResult.get(0).visitCount > INITITIAL_VALUE) {
			courseToken = courseResult.get(0).kurzbez;
			courseName = courseResult.get(0).name;
			lastCommentOnClass = courseResult.get(0).kommentar;
			try {
				int graduationYear = 2000 + Integer.parseInt(courseToken.replaceAll("[A-Z]*",
						"").substring(0, 2)) + 3;
				graduation = String.valueOf(graduationYear);
			} catch (NumberFormatException e) {

			}

			html.append("Du studierst also auch an der DHBW Mannheim. Gefällt dir die Rahmsoße genau " +
					"so gut wie uns? Immerhin siegt durch Autoload endlich die Faulheit! ");

			if (courseToken.startsWith("T")) {
				html.append("Immerhin bist du auch an der Fakultät Technik! ");
				if (courseToken.startsWith("TMT") || courseToken.startsWith("TWIW")) {
					html.append("Aber wir haben Mitleid, dass ihr in Eppelheim sitzt");
				}
			} else if (courseToken.startsWith("W")) {
				html.append("Wir als Techniker sind froh über jede Studentin, " +
						"die bei uns in Neuostheim ist!");
			}
			html.append(" Um so besser, dass nächste Jahr hoffentlich alle in Neuostheim vereint " +
					"sind");
			html.append("<br \\> Nichts desto trotz, du studierst also <b>" + courseName + "</b>");
			if (graduation == null) {
				graduation = "irgendwann";
			}
			html.append(" simmt's? Wir hoffen, dass du dir " + graduation + " auch dein " +
					"Bachelorzeugnis abholen darfst.<br \\><br \\> ");


			html.append("Am Ende möchten wir noch etwas los werden:<br \\>");
			if (lastCommentOnClass != null) {
				html.append(lastCommentOnClass);
			} else {
				html.append("Viel Erfolg weiterhin im Studium!");
			}
		} else {
			html.append("Das ist wohl ein Laptop oder Rechner, den du nicht hauptsächlich für das" +
					" Studium an der DHBW Mannheim nutzt. Also können wir an dieser leider keine " +
					"fundierte Aussage treffen");
		}
		return html.toString();
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
	public String[] getCsvHeaders() {
		return new String[]{"Kurs", "Kursbezeichung"};
	}

	/**
	 * Für den CSV Part der den vermuteten Namen entspricht und Kursbezeichnung.
	 *
	 * @return TreeMap Was letztendlich in die CSV einfließt
	 */
	@Override
	public SortedMap<String, String> getCsvContent() {
		TreeMap<String, String> csvOutput = new TreeMap<String, String>();
		if (!courseResult.isEmpty() && courseResult.get(0).visitCount>INITITIAL_VALUE) {
			csvOutput.put("Kurs", courseResult.get(0).name);
			csvOutput.put("Kursbezeichnung", courseResult.get(0).kurzbez);
		} else {
			csvOutput.put("Kurs", "Unkown");
			csvOutput.put("Kursbezeichung", "Unknown");
		}
		return csvOutput;
	}

	/**
	 * Logik
	 * Hole die meist aufgerufnen Vorlesungskalender, ergänze Informationen,
	 * lass Dropbox-Wertung einfließen.
	 */
	@Override
	public void run() {
		courseList = Utilities.loadDataFromJson("/data/studiengaenge.json", Studiengang[].class);
		calenderCourseList = Utilities.loadDataFromJson("/data/studienbezeichner.json",
				Kurskalendermap[].class);
		courseResult = getViewedCalenders();
		if (courseResult.isEmpty()) {
			foundOnlineCalender = false;
			for (Kurskalendermap entry : calenderCourseList) {
				courseResult.add(new CourseVisitedEntry(entry.id, INITITIAL_VALUE));
			}
		}
		for (CourseVisitedEntry entry : courseResult) {
			// Ergänze restlichen Informationen
			entry.kurzbez = this.getKursById(entry.courseID);
			addNameAndComment(entry);
		}

		analyzePathNames(courseResult);
		Collections.sort(courseResult, new EntryComparator());
		if (!courseResult.isEmpty() && courseResult.get(0).visitCount> INITITIAL_VALUE) {
			String course = getMostSuitableCourse();
			GlobalData.getInstance().proposeData("Kurskürzel", course);
		}
	}

	/**
	 * Reine Dropbox Analyse kann unter Umständen Mehrdeutigkeiten nicht final entscheiden.
	 * Sollte 2 Vorschläge auf eine gleich Gewichtung kommen, dann wird der Rest der nicht mehr
	 * gleich ist mit X auf gefüllt.
	 * @return Studiengangskürzel mit X aufgefüllt am Ende falls merhdeutig war
	 */
	private String getMostSuitableCourse() {
		if (courseResult.size() >= 2) {
			if (courseResult.get(0).visitCount == courseResult.get(1).visitCount) {
				char[] first = courseResult.get(0).kurzbez.toCharArray();
				char[] second = courseResult.get(1).kurzbez.toCharArray();
				char[] ausgabe = new char[Math.max(first.length, second.length)];
				int i = 0;
				while (first[i] == second[i] && i < second.length && i < first.length) {
					ausgabe[i] = first[i];
					i++;
				}
				for (int j = i; j < ausgabe.length; j++) {
					ausgabe[j] = 'X';
				}
				return String.valueOf(ausgabe);
			}
		}
		return courseResult.get(0).kurzbez;
	}

	/**
	 * Vergleich der Kurse mit der Dropbox. Anhand des bisheringen Maximums kann die Dropbox
	 * nochmal die Ergebnisse umwerfen oder deutlich bestätigen.
	 *
	 * @param courseResult Ergebnis nach der Datenbankabfrage
	 */
	private void analyzePathNames(ArrayList<CourseVisitedEntry> courseResult) {
		float influence = (float)INITITIAL_VALUE;
		if (courseResult != null && !courseResult.isEmpty() && this.foundOnlineCalender) {
			influence = courseResult.get(0).visitCount * DROPBOX_WEIGHTING_FACTOR;
		}
		for (CourseVisitedEntry entry : courseResult) {
			if (Whoami.getTimeProgress() > 95) {
				break;
			}
			for (Path p : dropboxFiles) {
				String[] pathParts = p.getParent().toString().split("\\\\|\\s|-");
				for (String partOfPath : pathParts) {
					float samenessLevel = 0.60f;
					while (Utilities.isRoughlyEqual(partOfPath, entry.kurzbez, samenessLevel)) {
						samenessLevel += 0.05f;
					}
					if (samenessLevel >= 0.75f) {
						entry.visitCount += influence * SAMENESS_WEIGHTING_FACTOR * samenessLevel;
					}
				}
			}
		}
	}

	/**
	 * Es wird Prefix und Suffix extrahiert. Davon abhängig wird der volle Name des Kurses bzw.
	 * Studiengang bestimmt. TINF13AIBC --> Angewandte Informatik
	 *
	 * @param entry Den wir um Name und Kommentar ergänzen wollen.
	 */
	private void addNameAndComment(CourseVisitedEntry entry) {
		String prefix;
		String suffix;
		Pattern p = Pattern.compile("[A-Z]*");
		Matcher m = p.matcher(entry.kurzbez);
		m.find();
		prefix = m.group(); //TINF
		m.find(); // 1 wird nicht gematched weil Zahlen nicht in  [A-Z]
		m.find(); // 3 wie oben
		m.find();
		suffix = m.group(); //AIBC

		for (Studiengang course : courseList) {
			if (course.prefix.equals(prefix)) {
				if (course.suffix != null && Arrays.asList(course.suffix).contains(suffix)) {
					entry.name = course.name;
					entry.kommentar = course.comment;
					break; // perfekt Match mit Suffix wir können aufhören
				} else if (!suffixNecessary(suffix)) {
					entry.name = course.name;
					entry.kommentar = course.comment;
					break; // es gibt keinen Suffix also können wir auch hier aufhören
				}

			}
		}
	}

	private boolean suffixNecessary(String searchSuffix) {
		for (Studiengang course : courseList) {
			if (course.suffix != null && Arrays.asList(course.suffix).contains(searchSuffix)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Nur die Kalender ID soll extrahiert werden von Firefox oder Chrome. Die Abfrage ist die
	 * selbe, mit String Operationen wird die UID schon auf sqlite Seite bestimmt.
	 * Chrome und Firefox unterscheiden sich nur in der Tabelle von der gelesen werden soll.
	 * Die Iteration erfolgt über alle Pfade es sei denn es ist schon mehr als die Hälfte der
	 * Zeit rum.
	 *
	 * @return 10 Vorlesungsplan IDs die am häufigsten aufgerufen werden.
	 */
	private ArrayList<CourseVisitedEntry> getViewedCalenders() {
		ArrayList<CourseVisitedEntry> result = new ArrayList<CourseVisitedEntry>();
		for (Path dbPath : databaseFiles) {
			if (Whoami.getTimeProgress() > 85) {
				break;
			}
			String fromTable = "";
			if (dbPath.toString().contains("Firefox")) {
				fromTable = "moz_places";
			} else if (dbPath.toString().contains("Chrome")) {
				fromTable = "urls";
			} else {
				continue;
			}
			ResultSet rs = null;
			try {
				DataSourceManager dSm = new DataSourceManager(dbPath);
				rs = dSm.querySqlStatement("SELECT substr(url,instr(url,'uid=')+4,7) " +
						"kurs, sum(visit_count) aufrufe " +
						"FROM " + fromTable + " " +
						"WHERE url LIKE '%vorlesungsplan.dhbw-mannheim.de/%uid=%' " +
						"GROUP BY kurs " +
						"ORDER BY aufrufe DESC " +
						"LIMIT 10;");
				if (rs != null) {
					while (rs.next()) {
						if (!resultContainsID(result, rs.getString(1))) {
							String a = rs.getString(1);
							int b = rs.getInt(2);
							result.add(new CourseVisitedEntry(rs.getString(1), rs.getInt(2)));
						} else {
							summarizeVisitCount(result, rs.getString(1), rs.getInt(2));
						}
					}
				}
			} catch (ClassNotFoundException | SQLException e) {
			} finally {
				try {
					if (rs != null) {
						rs.close();
						rs.getStatement().close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		Collections.sort(result, new EntryComparator());
		return result;
	}

	/* --------------------------------------------
	* Ab hier folgen nur noch private Hilfsmethoden
	* die zur Lesbarkeit beitragen.
	* (Teils triviale Implementierungen)
	* ---------------------------------------------
	 */

	private void summarizeVisitCount(ArrayList<CourseVisitedEntry> result, String id,
	                                 int increment) {
		for (CourseVisitedEntry entry : result) {
			if (entry.courseID.equals(id)) {
				entry.visitCount += increment;
			}
		}
	}

	private boolean resultContainsID(ArrayList<CourseVisitedEntry> input, String id) {
		for (CourseVisitedEntry entry : input) {
			if (entry.courseID.equals(id)) {
				return true;
			}
		}
		return false;
	}

	private String getKursById(String lookUpID) {
		for (Kurskalendermap mapping : this.calenderCourseList) {
			if (mapping.id.equals(lookUpID)) {
				return mapping.kursbez;
			}
		}
		return "Unbekannt";
	}

	/* 
	* ------------------------------------------------
	* Ab hier folgen nur noch Speicherklassen für JSON
	* ------------------------------------------------
	*/

	private class Studiengang {
		String name;
		String prefix;
		String[] suffix;
		String comment;
	}

	private class Kurskalendermap {
		String kursbez;
		String id;
	}

	private class CourseVisitedEntry {
		String courseID;
		int visitCount;
		String kurzbez;
		String name;
		String kommentar;

		public CourseVisitedEntry(String id, int count) {
			courseID = id;
			visitCount = count;
		}

	}

	/**
	 * Absteiger Comparator, der vergleichbar mit DESC in SQL ist.
	 * Damit der wahrscheinlichste Eitrag oben steht in der ArrayList.
	 */
	private class EntryComparator implements Comparator<CourseVisitedEntry> {
		@Override
		public int compare(CourseVisitedEntry first, CourseVisitedEntry second) {
			if (first.visitCount == second.visitCount) {
				return 0;
			} else if (first.visitCount < second.visitCount) {
				return 1;
			} else {
				return -1;
			}
		}
	}
}
