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

			/*StatmentToGenre:
			Mainstreamopfer: 60, 35, 12, 127, 124
			Tänzer und Partygirls: 3, 4, 125, 114, 113, 112,
			Faule Leseratte:
			Raver: 18, 19, 74, 68, 67, 55, 54, 52, 51, 45, 44, 41, 34, 31, 30, 25, 129, 126
			Chiller und Baggy Pants: 16, 27, 7, 26
			Headbanger und Zecken: 1, 9, 17, 22, 79, 50, 49, 47, 40, 20, 138, 137, 121, 118, 94,
			93,92,91
			Kenner & Rotweintrinker: 0, 8, 28, 29, 42, 26
			Gangstarapper: 15, 59
			Zecke: 21, 73, 43, 134, 133
			Originell / Geschmack und Stil: 5, 10, 66, 78, 6, 132, 131, 109, 102
			Literatur-Student: 77, 24, 71, 70, 69, 65, 57,37, 24, 110, 101
			Dein Ernst? 44, 61, 53, 141, 140, 136, 130
			Traditionell und oldy: 76, 75, 58 2, 11, 64, 56, 33, 32, 38, 123, 115, 106, 105, 103
			Extravagant: 72, 71, 14, 13, 12, 48, 39, 34, 63, 46, 23, 62, 139, 135, 122, 120, 119,
			 117, 116, 111, 108, 107, 104, 100, 99, 98, 97, 96, 95, 90
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
		System.out.println("bf1" + browserFiles);
		readBrowser(browserFiles);
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
			if (element.toString().contains(".mp3") || element.toString().contains(".flac")) {
				localFiles.add(element); // Liste der lokalen Audiodateien von denen der ID3Tag
				// ausgelesen wird

			} else if (element.toString().contains(".exe")) {
				exeFiles.add(element); //Liste aller ausführbarer Musikprogramme

			} else if (element.toString().contains(".sqlite") || element.toString().contains
					("History")) {
				browserFiles.add(element);
			}
		}

		System.out.println("bf2: " + browserFiles);
		System.out.println("lf: " + localFiles);
		System.out.println("ef: " + exeFiles);
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
					"<td><br /></td>" +
					"<td><b>Fazit:</b> Dein Computer enthält Informationen zu allem was wir " +
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
		}

		//Genre einteilung Text
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
						/*if(gId <= 79){
							statementToGenre = "Dein Lieblingsgenre '" + genre + "', " +
									"' zeugt von Geschmack und Stil.";
						}
						if(gId > 79 && gId < ){
							statementToGenre = "Dein Lieblingsgenre '" + genre + "', " +
									"' zeugt von Geschmack und Stil.";
						}*/
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


	public void readBrowser(List<Path> BrowserFiles) {
		/**
		 * Durchsucht den Browser-Verlauf auf bekannte Musikportale
		 *
		 * @param browserFiles
		 * @return void
		 */
		/*System.out.println("URLs: " + browserFiles);

		System.out.println("URLs after DBExtraction: " + browserFiles);
		ResultSet[] dbResult = this.getViewCountAndUrl(MY_SEARCH_DELIEVERY_URLS);*/
		System.out.println("bf: " + browserFiles);
		dbExtraction();
		Connection connection = null;
		ResultSet resultSet = null;
		Statement statement = null;

		try {
			//for (int i = 0; i < urls.size(); i++) {
			Class.forName("org.sqlite.JDBC");
			System.out.println("URLS:" + urls);
			connection = DriverManager.getConnection
					("jdbc:sqlite:" + urls.get(0));
					//C:\Users\Inga\AppData\Local\Google\Chrome\User " +
			//"Data\Default\History" lübbt
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM urls WHERE url LIKE '%youtube.com%'");

			while (resultSet.next()) {
				System.out.println("URL [" + resultSet.getString("url") + "]" +
						", visit count [" + resultSet.getString("visit_count") + "]");
			}
		}
		//}

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