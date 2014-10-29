package de.aima13.whoami.modules;

import com.sun.deploy.util.StringUtils;
import com.sun.org.apache.xalan.internal.xsltc.runtime.*;
import de.aima13.whoami.Analyzable;

import java.sql.Statement;
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.*;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentMap;

import de.aima13.whoami.GlobalData;
import de.aima13.whoami.support.DataSourceManager;
import javafx.beans.property.MapProperty;
import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.AbstractID3;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.ID3v1;
import org.omg.CORBA.Environment;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * favourite Music, created 16.10.14.
 *
 * @author Inga Miadowicz
 * @version 1.0
 */

public class Music implements Analyzable {

	List<Path> musicDatabases = new ArrayList<>(); //List I get from FileSearcher
	List<Path> localFiles = new ArrayList<>(); //List of MP3-files from musicDatabase
	List<Path> browserFiles = new ArrayList<>(); //List of browser-entries from musicDatabase
	List<Path> exeFiles = new ArrayList<>(); //List of browser-entries from musicDatabase
	ArrayList<String> FileArtist = new ArrayList<>(); //List of Artists
	ArrayList<String> FileGenre = new ArrayList<>(); //List of Genres
	ArrayList<String> urls = new ArrayList<>(); //List of URLs
	Map<String, Integer> mapMaxApp = new HashMap<>();//Map Artist - frequency of this artist
	Map<String, Integer> mapMaxGen = new HashMap<>();//Map Genre - frequency of this genre


	public String html = ""; //Output der HTML
	public String favArtist = ""; //Ergebnis von ScoreUser
	public String favGenre = ""; //Ergebnis von ScoreGenre
	public String onlService = ""; //Genutzte Onlinedienste (siehe MY_SEARCH_DELIVERY_URLS)
	public String cltProgram = ""; //Installierte Programme
	String statementToGenre = "";

	private static final String[] MY_SEARCH_DELIEVERY_URLS = {"youtube.com", "myvideo.de", "dailymotion.com",
			"soundcloud.com", "deezer.com"};

	String[] arrayGenre = {    // Position im Array ist Byte des id3Tag:
			// z.B. GenreID ist 3: Genre zu 3 ist "Dance"
			// Quelle: http://id3.org/id3v2.3.0

			//NEUNZIG

			/*StatmentToGenre:
			Mainstreamopfer: 60, 35, 12, 127, 124
			Tänzer und Partygirls: 3, 4, 125, 114, 113, 112, 83, 86, 143
			Faule Leseratte:
			Raver: 18, 19, 74, 68, 67, 55, 54, 52, 51, 45, 44, 41, 34, 31, 30, 25, 129, 126, 84,
			147, 156,158, 176, 177, 189, 191
			Chiller und Baggy Pants: 16, 27, 7, 26, 154
			Headbanger und Zecken: 1, 9, 17, 22, 79, 50, 49, 47, 40, 20, 138, 137, 121, 118, 94,
			93,92,91, 81, 144, 149, 163, 167, 179,189,, 190
			Kenner & Rotweintrinker: 0, 8, 28, 29, 42, 26, 166,171
			Gangstarapper: 15, 59
			Zecke: 21, 73, 43, 134, 133, 175, 176
			Originell / Geschmack und Stil: 5, 10, 66, 78, 6, 132, 131, 109, 102, 80,162,185, 187
			Literatur-Student: 77, 24, 71, 70, 69, 65, 57,37, 24, 110, 101,183, 184, 186
			Dein Ernst? 44, 61, 53, 141, 140, 136, 130, 146, 172, 161, 173
			Traditionell und oldy: 76, 75, 58 2, 11, 64, 56, 33, 32, 38, 123, 115, 106, 105, 103,
			 82, 90, 150, 181, 182
			Extravagant: 72, 71, 14, 13, 12, 48, 39, 34, 63, 46, 23, 62, 139, 135, 122, 120, 119,
			 117, 116, 111, 108, 107, 104, 100, 99, 98, 97, 96, 95, 90, 85, 87, 88, 89, 142, 145,
			  148, 151, 152, 153, 155, 157, 160, 164, 165,168, 169, 170, 174, 178, 188
			*/

			//Dies sind die offiziellen ID3v1 Genres.
			"Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge",
			"Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B", "Rap",
			"Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska",
			"Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient",
			"Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical",
			"Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel", "Noise",
			"Alternative Rock", "Bass", "Soul", "Punk", "Space", "Meditative",
			"Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic", "Darkwave",
			"Techno-Industrial", "Electronic", "Pop-Folk", "Eurodance", "Dream",
			"Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40", "Christian Rap",
			"Pop/Funk", "Jungle", "Native American", "Cabaret", "New Wave",
			"Psychadelic", "Rave", "Showtunes", "Trailer", "Lo-Fi", "Tribal",
			"Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical", "Rock & Roll",
			"Hard Rock",

			//These were made up by the authors of Winamp but backported into the ID3 spec.
			"Folk", "Folk-Rock", "National Folk", "Swing", "Fast Fusion",
			"Bebob", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde",
			"Gothic Rock", "Progressive Rock", "Psychedelic Rock", "Symphonic Rock",
			"Slow Rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour",
			"Speech", "Chanson", "Opera", "Chamber Music", "Sonata", "Symphony",
			"Booty Bass", "Primus", "Porn Groove", "Satire", "Slow Jam", "Club",
			"Tango", "Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul",
			"Freestyle", "Duet", "Punk Rock", "Drum Solo", "A capella", "Euro-House",
			"Dance Hall",

			//These were also invented by the Winamp folks but ignored by the ID3 authors.
			"Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror", "Indie",
			"BritPop", "Negerpunk", "Polsk Punk", "Beat", "Christian Gangsta Rap",
			"Heavy Metal", "Black Metal", "Crossover", "Contemporary Christian",
			"Christian Rock", "Merengue", "Salsa", "Thrash Metal", "Anime", "Jpop",
			"Synthpop"
	};

	@Override
	public void run() {
		/**
		 * Implementierung der Methode run() von Runnable
		 * @return void
		 * @param
		 */

		getFilter();
		readId3Tag(localFiles);
		checkNativeClients(exeFiles);
		readBrowser(browserFiles, MY_SEARCH_DELIEVERY_URLS);
	}


	/////////////////////////////////////////////////////////////
	//// Übergebe Angaben für Informationen vom FileSearcher////
	///////////////////////////////////////////////////////////

	@Override
	public List<String> getFilter() {
		/** Legt den Filter für den FileSearcher fest
		 *
		 *  @param
		 *  @return filterMusic
		 */

		//a) local MP3-files. LATER ADD(.FLAC, .RM, .acc, .ogg, .wav?)
		List<String> filterMusic = new ArrayList<>();
		filterMusic.add("**.mp3");
		filterMusic.add("**.MP3");
		filterMusic.add("**.mP3");
		filterMusic.add("**.Mp3");
		filterMusic.add("**.FLAC");
		filterMusic.add("**.flac");


		//b) Browser-history
		filterMusic.add("**Google/Chrome**History");
		filterMusic.add("**Firefox**places.sqlite");

		//c) installed programs
		filterMusic.add("**spotify.exe"); //AppData/Roaming... -> versteckter Ordner by default
		filterMusic.add("**iTunes.exe");
		filterMusic.add("**SWYH.exe");
		filterMusic.add("**simfy.exe");

		//jukebox, napster kostenpflichtig?

		return filterMusic;
	}

	@Override
	public void setFileInputs(List<Path> files) throws Exception {
		/**
		 * Ordnet meine Suchergebnisse für die Analyse im Modul
		 * @param List<File> files
		 * @return void
		 */

		//Überprüfe ob relevante Dateien gefunden wurden musicDatabases
		if (!(files == null)) {
			musicDatabases = files;
		} else {
			throw new IllegalArgumentException("Auf dem Dateisystem konnten keine " +
					"Informationen zu Musik gefunden werden.");
		}

		//Spalte die Liste in drei Unterlisten:
		for (Path element : musicDatabases) {
			if (element.toString().contains(".mp3") || element.toString().contains(".flac") ||
					element.toString().contains(".FLAC") || element.toString().contains(".MP3")) {
				localFiles.add(element); // Liste der lokalen Audiodateien von denen der ID3Tag
				// ausgelesen wird

			} else if (element.toString().contains(".exe")) {
				exeFiles.add(element); //Liste aller ausführbarer Musikprogramme

			} else {
				browserFiles.add(element);
			}
		}
	}

	///////////////////////////////////////////////////////
	///// Output-Dateien vorbereiten /////////////////////
	/////////////////////////////////////////////////////

	@Override
	public String getHtml() {
		/**
		 * Das Ergebnis der Analyse wird in HTML-lesbaren Format umgesetzt
		 *
		 * @return String html
		 * @param
		 */
		StringBuffer buffer = new StringBuffer();
		buffer.append("<table>");

		if (musicDatabases.isEmpty()) {
			buffer.append("<tr><td>Es wurden keine Informationen gefunden um den scheinbar " +
					"sehr geheimen Musikgeschmack des Users zu analysieren.</td></tr>");
		}

		if (!(favArtist.equals(""))) {
			buffer.append("<tr><td>Lieblingskünstler:</td>" +
					"<td>" + favArtist + "</td></tr>");
		}
		if (!(favGenre.equals(""))) {
			buffer.append("<tr>" +
					"<td>Lieblingsgenre:</td>" +
					"<td>" + favGenre + "</td>" +
					"</tr>");
		}
		if (!(cltProgram.equals(""))) {
			buffer.append("<tr>" +
					"<td>Musikprogramme:</td>" +
					"<td>" + cltProgram + "</td>" +
					"</tr>");
		}
		if (!(onlService.equals(""))) {
			buffer.append("<tr>" +
					"<td>Onlinestreams:</td>" +
					"<td>" + onlService + "</td>" +
					"</tr>");
		}
		if (!(onlService.equals("")) && !(favArtist.equals("")) && !(favGenre.equals("")) && !
				(cltProgram.equals(""))) {
			buffer.append("<tr>" +
					"<td colspan='2'><b>Fazit:</b> Dein Computer enthält Informationen zu allem " +
					"was wir " +
					"gesucht haben. " +
					" " +
					"</td>" +
					"</tr>");
		}


		buffer.append("</table>");
		html = buffer.toString();

		return html;
	}

	@Override
	public String getReportTitle() {
		return null;
	}

	@Override
	public String getCsvPrefix() {
		return null;
	}

	@Override
	public SortedMap<String, String> getCsvContent() {
		/**
		 *
		 * @return csvContent
		 * @param
		 */
		SortedMap<String, String> csvContent = new TreeMap<>();
		csvContent.put("TestHead", "Test Value");
		return csvContent;
	}


	///////////////////////////////////////////
	///// Analysiere Audidateien /////////////
	/////////////////////////////////////////

	public void scoreFavGenre(ArrayList<String> FileGenre) {
		/**
		 * Sucht aus der Liste aller Genres das Lieblingsgenre heraus
		 * @return String favGenre
		 * @param ArrayList<String> fileArtist
		 */

		int max = 0; //highest frequency of a genre
		int count; //count frequency of actual genre
		//String favGenre = "";

		FileGenre.removeAll(Arrays.asList("", null)); //delete empty entries
		Collections.sort(FileGenre);//sort list alphabetically


		//hashes number of existance to genre
		for (String each : FileGenre) {
			count = 0;
			if (mapMaxGen.containsKey(each)) {
				count = mapMaxGen.get(each);
				mapMaxGen.remove(each);
			}
			count++;
			mapMaxGen.put(each, count);
		}

		//find Genre which has the highest key
		Iterator it = mapMaxGen.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			if ((int) (pairs.getValue()) > max) {
				favGenre = (String) pairs.getKey();
				max = (int) (pairs.getValue());
			}
			it.remove();
		}

		if (favGenre.startsWith("(")) {
			String str = "";
			str = favGenre.replaceAll("\\D+", "");
			byte a = Byte.parseByte(str);
			favGenre = (String) arrayGenre[a];
			getCategory(a);
		}

		//Genre einteilung Text
	}

	public void getCategory(byte genreByte){
		if(genreByte == 60 || genreByte == 35 || genreByte == 12 || genreByte == 127 || genreByte
				== 124){
			statementToGenre = "Main-Streamofer";
		}
		else if(genreByte == 3 || genreByte == 4 || genreByte == 125 || genreByte == 114 ||
				genreByte
				== 113 || genreByte == 112 || genreByte == 83 || genreByte == 86 || genreByte ==
				143){
			statementToGenre = "Deinem Musikstil, " + favGenre + ", nach zu urteilen schwingst du" +
					" gerne dein Tanzbein.";
		}
		else if(genreByte == 18 || genreByte == 19 || genreByte == 74 || genreByte == 68 ||
				genreByte
				== 67 || genreByte == 55 || genreByte == 54 || genreByte == 52 || genreByte == 651
				|| genreByte == 45 || genreByte == 44 || genreByte == 41 || genreByte == 34 ||
				genreByte == 31 || genreByte == 30 || genreByte == 25 || genreByte == 129 ||
				genreByte == 126 || genreByte == 84 || genreByte == 147 || genreByte == 156 ||
				genreByte == 158 || genreByte == 177 || genreByte == 189 ||
				genreByte == 191){
			statementToGenre = "Raver";
		}
		else if(genreByte == 76 || genreByte == 75 || genreByte == 2 || genreByte == 11
				|| genreByte == 64 || genreByte == 56 || genreByte == 33 || genreByte ==
				32 || genreByte == 38 || genreByte == 115 || genreByte == 123 || genreByte == 106
				|| genreByte == 105 || genreByte == 103 || genreByte == 82 || genreByte == 90 ||
				genreByte == 150 || genreByte == 181 || genreByte == 182){
			statementToGenre = "Dein Musikstil" + favGenre + "ist eher von traditioneller Natur.";
		}
		else if( genreByte == 61 || genreByte == 53 || genreByte == 141 ||
				genreByte
				== 140 || genreByte == 136 || genreByte == 130 || genreByte == 146 || genreByte
				== 172 || genreByte == 161 || genreByte == 173){
			statementToGenre = "Über Geschmack lässt sich ja bekanntlich streiten. " +
					"Aber " + favGenre + " - Dein Ernst?!";
		}
		else if(genreByte == 1 || genreByte == 9 || genreByte == 17 || genreByte == 22 || genreByte
				== 79 || genreByte == 50 || genreByte == 49 || genreByte == 47 || genreByte
				== 40 || genreByte == 20 || genreByte == 138 || genreByte == 137 || genreByte ==
				121 || genreByte == 93 || genreByte == 81 || genreByte == 149 || genreByte ==
				118 || genreByte == 92 || genreByte == 144 || genreByte == 163 || genreByte ==
				94 || genreByte == 91 || genreByte == 167 || genreByte == 179 || genreByte == 190){
			statementToGenre = "In dir steckt ein Headbanger!";
		}
		else if(genreByte == 16 || genreByte == 27 || genreByte == 7 || genreByte
				== 154){
			statementToGenre = "Deine Szene ist wahrscheinlich die Hip Hop Szene. Du bist ein " +
					"sehr chilliger Mensch und vermutlich finden sich die Baggy Pants mindestens " +
					"in deinem Kleiderschrank?";
		}
		else if(genreByte == 0 || genreByte == 8 || genreByte == 28 || genreByte == 29 || genreByte
				== 42 || genreByte == 26 || genreByte == 166 || genreByte == 171){
			statementToGenre = "Deinem Lieblingsgenre zu urteilen beschreibt sich dieses " +
					"Modul als wahren Kenner. Vermutlich spielst du selber mindestens ein " +
					"Instrument und verbringt dein Leben am liebsten entspannt mit einem " +
					"Glas Rotwein.";
		}
		else if(genreByte == 15 || genreByte == 59){
			statementToGenre = "Du hörst Rap. Vielleicht bis du sogar ein übler Gangstarapper";
		}
		else if(genreByte == 21 || genreByte == 73 || genreByte == 43 || genreByte == 134 ||
				genreByte == 133 || genreByte == 175 || genreByte == 176){
			statementToGenre = "Deine Musiklieblingsrichtung ist Punk oder zumindest eine " +
					"Strömung des Punks.";
		}
		else if(genreByte == 5 || genreByte == 10 || genreByte == 66 || genreByte == 78 ||
				genreByte == 6 || genreByte == 132 || genreByte == 131 || genreByte == 109 ||
				genreByte == 102 || genreByte == 80 || genreByte == 162 || genreByte == 185 ||
				genreByte == 187){
			statementToGenre = "Dein Musikgeschmack, " + favGenre + ", " +
					"zeugt von Geschmack und Stil.";
		}
		else if(genreByte == 77 || genreByte == 71 || genreByte == 70 ||
				genreByte == 69 || genreByte == 65 || genreByte == 57 || genreByte == 37 ||
				genreByte == 24 || genreByte == 110 || genreByte == 101 || genreByte == 183 ||
				genreByte == 184 || genreByte == 186){
			statementToGenre = "Die Audiodatei lässt sich einer Art Literatur zuordnen. Du bist " +
					"entweder sehr Literaturbegeistert und liebst lesen oder eine sehr faule " +
					"Leseratte, die sich lieber alles vorlesen lässt. Wie auch immer du bist " +
					"wahrscheinlich ein ziemlich belesener Mensch. ";
		} else {
			statementToGenre = "Dein Musikgeschmack " + favGenre + " ist ziemlich extravagant.";
		}
	}


	public void scoreFavArtist(ArrayList<String> FileArtist) {
		/**
		 * Sucht aus einer Liste aller Artisten des Lieblingsartisten heraus
		 * @param ArrayList<String> FileArtist
		 * @return void
		 */

		int count = 0; //counts frequency of artist
		//String favArtist = ""; //saves artist with highest frequency
		int max = 0; //highest frequency

		FileArtist.removeAll(Arrays.asList("", null)); //delete empty entries
		Collections.sort(FileArtist); //sort list alphabetically

		//hashes frequency to artist
		for (String each : FileArtist) {
			count = 0;
			if (mapMaxApp.containsKey(each)) {
				count = mapMaxApp.get(each);
				mapMaxApp.remove(each);
			}
			count++;
			mapMaxApp.put(each, count);
		}

		//Find artist with highest frequency
		Iterator it = mapMaxApp.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			if ((int) (pairs.getValue()) > max) {
				favArtist = (String) pairs.getKey();
				max = (int) (pairs.getValue());
			}
			it.remove();
		}

	}

	public void readId3Tag(List<Path> localFiles) {
		/**
		 * Liest den ID3 Tag von gefundenen MP3- und FLAC-Dateien aus
		 *
		 * @param ArrayList<File> localFiles
		 * @return void
		 * @remark benutzt Bibliothek "jid3lib-0.5.4.jar"
		 */

		String genre = ""; //String of Genre

		for (Path file : localFiles) {
			try {
				String fileLocation = file.toAbsolutePath().toString(); //Get path to file
				MP3File mp3file = new MP3File(fileLocation); //create new object from ID3tag-package

				if (mp3file.hasID3v2Tag()) {
					AbstractID3v2 tagv2 = mp3file.getID3v2Tag();

					//Fill ArrayList<String> with Artists and Genres
					FileArtist.add(tagv2.getLeadArtist());
					FileGenre.add(tagv2.getSongGenre());

				} else if (mp3file.hasID3v1Tag()) {
					ID3v1 tagv1 = mp3file.getID3v1Tag();
					FileArtist.add(tagv1.getArtist()); //Fill List of Type String with artist

					//Have to map genreID to name of genre
					byte gId = tagv1.getGenre(); //Get Genre ID

					try {
						genre = arrayGenre[gId]; //look up String to ID
						getCategory(gId);
					} catch (ArrayIndexOutOfBoundsException e) {
						System.out.println("This Genre doesn't exist");
					}

					FileGenre.add(genre); //Fill List of Type String with genre

					System.out.println("Artists: " + FileArtist);
					System.out.println("Genre: " + FileGenre);

				} else {
					//System.out.println("Found audiofile without ID3-Tag");
				}
			} catch (TagException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
				e.printStackTrace();
			} catch (UnsupportedOperationException e) {
				//e.printStackTrace();
				//System.out.println("Unsupported Operation Exception");
			} catch (Exception e) {
				System.out.println("general Exception in readId3Tag");
				e.printStackTrace();
			}

		}

		scoreFavArtist(FileArtist); //Call functions to find favArtist
		scoreFavGenre(FileGenre);   //Call functions to find favGenre
	}

	///////////////////////////////////////////
	///// Analysiere Musikprogramme //////////
	/////////////////////////////////////////

	public void checkNativeClients(List<Path> exeFiles) {
		String clients[] = new String[4];
		int count = 0;

		for (Path currentExe : exeFiles) {
			if (currentExe.toString().endsWith("spotify.exe")) {
				clients[count] = "Spotify";
				count++;
			}
			if (currentExe.toString().endsWith("iTunes.exe")) {
				clients[count] = "iTunes";
				count++;
			}
			if (currentExe.toString().endsWith("SWYH.exe")) {
				clients[count] = "Stream What You Hear";
				count++;
			}
			if (currentExe.toString().endsWith("simfy.exe")) {
				clients[count] = "simfy";
				count++;
			}
		}

		if (count == 0) {

		} else if (count == 1) {
			cltProgram = clients[0];
		} else if (count == 2) {
			cltProgram = clients[0] + ", " + clients[1];
		} else if (count == 3) {
			cltProgram = clients[0] + ", " + clients[1] + ", " + clients[2];
		} else if (count == 4) {
			cltProgram = clients[0] + ", " + clients[1] + ", " + clients[2] + ", " + clients[3];
		}
	}

	///////////////////////////////////////////
	///// Analysiere Browserverlauf //////////
	/////////////////////////////////////////


	public void readBrowser(List<Path> BrowserFiles, String searchUrl[]) {
		/**
		 * Durchsucht den Browser-Verlauf auf bekannte Musikportale
		 *
		 * @param browserFiles
		 * @return void
		 */

		dbExtraction();
		Connection connection = null;
		ResultSet resultSet = null;
		Statement statement = null;

		try {
			//Zusammenfügen des SQL-Statements aus der WhiteListe der Onlineseiten
			String sqlStatement = "SELECT * FROM urls WHERE url LIKE '%" +searchUrl[0]+ "%'";
			for (int i = 1; i <searchUrl.length ; i++) {
				sqlStatement+= "OR url LIKE '%"+searchUrl[i]+"%' ";
			}

			//Datenbankabfrage Chrome
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + urls.get(0));
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlStatement);

			//Die Liste urls wird jetzt für alle passenden URLs der history genutzt
			urls.clear();
			while (resultSet.next()) {
				if(!(resultSet.getString("url").contains("google"))) {
					urls.add(resultSet.getString("url"));
				}
			}

			System.out.println("urls:" + urls.toString());
			System.out.println("length: " + urls.size());

			for(int i = 1; i < urls.size(); i++){
				String curr = urls.get(i);
				if(curr.contains("youtube.com") && !(onlService.contains("youtube.com"))){
					if(onlService.isEmpty()){
						onlService += "youtube.com";
					} else onlService += ", youtube.com";
				}
				if(curr.contains("myvideo.de") && !(onlService.contains("myvideo.de"))){
					if(onlService.isEmpty()){
						onlService += "myvideo.de";
					} else onlService += ", myvideo.de";
				}
				if(curr.contains("soundcloud.com") && !(onlService.contains("soundcloud.com"))){
					if(onlService.isEmpty()){
						onlService += "soundcloud.com";
					} else onlService += ", soundcloud.com";
				}
				if(curr.contains("dailymotion.com") && !(onlService.contains("dailymotion.com"))){
					if(onlService.isEmpty()){
						onlService += "dailymotion.com";
					} else onlService += ", dailymotion.com";
				}
				if(curr.contains("deezer.com") && !(onlService.contains("deezer.com"))){
					if(onlService.isEmpty()){
						onlService += "deezer.com";
					} else onlService += ", deezer.com";
				}
			}

			System.out.println("onlService:" + onlService);
		}

		catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				resultSet.close();
				statement.close();
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}


	private void dbExtraction() {
		/**
		 *
		 *
		 * @param
		 * @retrun void
		 */

		//get Username -> Globaldata
		String username = System.getProperty("user.name");
		GlobalData.getInstance().changeScore("username: " + username, 1);

		//sqlite daten rausspeichern
		int foundDbs = 0;
		System.out.println(browserFiles);
		try {
			for (Path curr : browserFiles) {
				if (curr != null) {
					String path;
					try {
						path = curr.toString(); //getCanonicalPath();
					} catch (Exception e) {
						e.printStackTrace();
						path = "";
					}

					if (path.contains(".sqlite")) {
						urls.add(path);
						foundDbs++;
					} else if (path.endsWith("\\History") && path.contains(username)) {
						urls.add(path);
						foundDbs++;
					}
					if (foundDbs > 1) {
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}