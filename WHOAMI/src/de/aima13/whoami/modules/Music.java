package de.aima13.whoami.modules;

import com.sun.deploy.util.StringUtils;
import com.sun.org.apache.xalan.internal.xsltc.runtime.*;
import de.aima13.whoami.Analyzable;

import java.sql.*;
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
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
	String stmtGenre = ""; //Kommentar zum Genre nach Kategorie
	byte gId;

	private static final String[] MY_SEARCH_DELIEVERY_URLS = {"youtube.com", "myvideo.de", "dailymotion.com",
			"soundcloud.com", "deezer.com"};
	private static final String TITLE = "Musikgeschmack";

	String[] arrayGenre = {    // Position im Array ist Byte des id3Tag:
			// z.B. GenreID ist 3: Genre zu 3 ist "Dance"
			// Quelle: http://id3.org/id3v2.3.0

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
		readId3Tag();
		checkNativeClients();
		readBrowser(MY_SEARCH_DELIEVERY_URLS);
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
		//Vorschlag: Amazon Prime instant

		return filterMusic;
	} //ANDERE DATEN

	@Override
	public void setFileInputs(List<Path> files) throws Exception {
		/**
		 * Ordnet meine Suchergebnisse für die Analyse im Modul
		 * @param List<File> files
		 * @return void
		 */

		//Überprüfe ob Dateien gefunden wurden
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
		StringBuilder buffer = new StringBuilder();
		buffer.append("<table>");


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

		// Abschlussfazit des Musikmoduls
		if (musicDatabases.isEmpty()) {
			buffer.append("<tr><td><br />Es wurden keine Informationen gefunden um den scheinbar " +
					"sehr geheimen Musikgeschmack des Users zu analysieren.</td></tr>");
		} else if (!(onlService.equals("")) && !(favArtist.equals("")) && !(favGenre.equals(""))
				&& !(cltProgram.equals(""))) {
			buffer.append("<tr>" +
					"<td colspan='2'><br /><b>Fazit:</b> Dein Computer enthält Informationen zu allem " +
					"was wir gesucht haben.<br /> Musik schein ein wichtiger Teil deines Lebens " +
					"zu sein. <br />" + stmtGenre + "</td>" +
					"</tr>");
		} else if(onlService.equals("") && cltProgram.equals("") && !(favGenre.equals(""))){
			buffer.append("<tr>" +
					"<td colspan='2'><br /><b>Fazit:</b> Das Modul konnte weder online noch nativ " +
					"herausfinden wie du Musik hörst. Du scheinst dies über einen nicht sehr " +
					"verbreiteten Weg zu machen. Nichts desto trotz konnten wir deinen Geschmack " +
					"analysieren:<br /> " + stmtGenre + "</td>" +
					"</tr>");
		} else if (favGenre.equals("") && favArtist.equals("")) {
			buffer.append("<td colspan='2'><br /><b>Fazit:</b> Es können keine Informationen zu deinem " +
					"Musikgeschmack " +	"gefunden werden. ");
			if(!(onlService.equals("")) || !(cltProgram.equals(""))){
				buffer.append("Aber Musik hörst du über " + onlService + cltProgram + "" +
						". Nur was bleibt eine offene Frage.</td></tr>");
			}
		} else {
			buffer.append("<tr>" +
					"<td colspan='2'><br /><b>Fazit:</b>Zwar konnten einige Informationen über " +
					"dich nicht herausgefunden werden, <br />aber einiges wissen wir.<br />");
			if(!(onlService.equals(""))){
				buffer.append("<br />Du hörst über " + onlService + " online Musik.<br />");
			}if(!(cltProgram.equals(""))){
				buffer.append("<br />Auf deinem PC benutzt du zum Musik hören " + cltProgram + ".<br />");
			} if(!(favArtist.equals(""))){
				buffer.append("<br />Deine Lieblingsband ist" + favArtist + "<br />" );
			} if(!(favGenre.equals(""))){
				buffer.append(stmtGenre);
			}

			buffer.append("</td></tr>");

		}

		buffer.append("</table>");
		html = buffer.toString();

		return html;
	}

	@Override
	public String getReportTitle() {
		return TITLE;
	}

	@Override
	public String getCsvPrefix() {
		return TITLE;
	}

	@Override
	public SortedMap<String, String> getCsvContent() {
		/**
		 *
		 * @return SortedMap<String, String> csvData
		 * @param
		 */

		SortedMap<String, String> csvData = new TreeMap<>();

		if(!(favArtist.equals(""))) {
			csvData.put("Lieblingskünstler", favArtist);
		}
		if(!(favArtist.equals(""))) {
			csvData.put("Lieblingsgenre", favGenre);
		}
		if(!(favArtist.equals(""))) {
			csvData.put("Onlineservices", onlService);
		}
		if(!(favArtist.equals(""))) {
			csvData.put("Musikprogramme", cltProgram);
		}
		return csvData;
	}


	///////////////////////////////////////////
	///// Analysiere Audidateien /////////////
	/////////////////////////////////////////

	public void scoreFavGenre() {
		/**
		 * Sucht aus der Liste aller Genres das Lieblingsgenre heraus
		 * @return String favGenre
		 * @param ArrayList<String> fileArtist
		 */

		int max = 0; // Häufigkeit des am meisten existierenden Genre
		int count; // Häufigkeit des aktuellen Genre

		FileGenre.removeAll(Arrays.asList("", null)); //Lösche leere Einträge

		//Ordne einem Genre seine Häufigkeit zu
		for (String each : FileGenre) {
			count = 0;
			if (mapMaxGen.containsKey(each)) {
				count = mapMaxGen.get(each);
				mapMaxGen.remove(each);
			}
			count++;
			mapMaxGen.put(each, count);
		}

		//Finde Genre mit der höchsten Häufigkeit
		Iterator it = mapMaxGen.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			if ((int) (pairs.getValue()) > max) {
				favGenre = (String) pairs.getKey();
				max = (int) (pairs.getValue());
			}
			it.remove();
		}

		//Einige ID3-Tags sind fehlerhaft und das Byte wird in der Form "(XX)"als String
		// gespeichert. Hier wird nochmal geguckt ob das Genre zugeordnet werden kann.
		if (favGenre.startsWith("(")) {
			String str;
			str = favGenre.replaceAll("\\D+", "");
			gId = Byte.parseByte(str);
			favGenre = arrayGenre[gId];
		}

		getCategory();

	}

	public String getCategory(){
		/**
		 * Ordnet dem Genre eine Art Kategorie zu und füllt die Variable statementToGenre,
		 * damit im html-Output ein Kommentar zum Genre abgegeben werden kann.
		 *
		 * @return void
		 * @param byte genreByte
		 */

		StringBuilder statementToGenre = new StringBuilder();

		if(favGenre.equals("Top 40") || favGenre.equals("House") || favGenre.equals("Drum & " +
				"Bass") || favGenre.equals("Euro-House")){
			statementToGenre.append("Dein Musikgeschmack ist nicht gerade " +
					"aussagekräftig.<br />Du scheinst nicht wirklich auszuwählen was " +
					"dir gefällt,<br />sondern orientierst dich an Listen und Freunden.<br />" +
					"Was dich charaktierisitert ist wahrscheinlich das Mainstream-Opfer");
		}
		else if(favGenre.equals("Dance") || favGenre.equals("Disco") || favGenre.equals("Dancehall")
		|| favGenre.equals("Samba")	|| favGenre.equals("Tango") || favGenre.equals("Club")||
				favGenre.equals("Swing") || favGenre.equals("Latin") || favGenre.equals("Salsa")
				||favGenre.equals("Eurodance")){
			statementToGenre.append("Deinem Musikstil, " + favGenre + ", " +
					"nach zu urteilen,<br />schwingst " +
					"du gerne dein Tanzbein.");
		}
		else if( favGenre.equals("Techno") || favGenre.equals("Industrial") || favGenre.equals
				("Acid Jazz")|| favGenre.equals("Rave") || favGenre.equals("Psychedelic") ||
				favGenre.equals("Dream") || favGenre.equals("Elecronic") || favGenre
				.equals("Techno-Industrial") || favGenre.equals("Space") || favGenre.equals("Acid")
				|| favGenre.equals("Trance") || favGenre.equals("Fusion") ||
				favGenre.equals("Euro-Techno") || favGenre.equals("Hardcore Techno") || favGenre
				.equals("Goa") || favGenre.equals("Fast Fusion") || favGenre.equals("Synthpop") ||
				favGenre.equals("Dub") || favGenre.equals("Psytrance") || favGenre.equals
				("Dubstep") || favGenre.equals("Psybient")){
			statementToGenre.append("Dein Musikstil lässt darauf schließen, " +
					"<br />dass wenn man dich grob einer Richtung zuordnet du am ehesten einem Raver " +
					"entsprichst.");
		} else if( favGenre.equals("Retro") || favGenre.equals("Polka") || favGenre.equals
				("Country") || favGenre.equals("Oldies") || favGenre.equals("Native US") ||
				favGenre.equals("Southern Rock") || favGenre.equals("Instrumental") || favGenre
				.equals("Classical") || favGenre.equals("Gospel") || favGenre.equals("Folklore") ||
				favGenre.equals("A capella") || favGenre.equals("Symphony") ||
				favGenre.equals("Sonata") || favGenre.equals("Opera") || favGenre.equals
				("National Folk") || favGenre.equals("Avantgarde") || favGenre.equals("Baroque") ||
				favGenre.equals("World Music") || favGenre.equals("Neoclassical")){
			statementToGenre.append("Dein Musikstil" + favGenre + "ist eher von traditioneller" +
					" Natur.");
		} else if( favGenre.equals("Christian Rap") || favGenre.equals("Pop-Folk") || favGenre
				.equals("Christian Rock") || favGenre.equals("Contemporary Christian") ||
				favGenre.equals("Christian Gangsta Rap") || favGenre.equals("Terror") || favGenre
				.equals("Jpop") || favGenre.equals("Math Rock") || favGenre.equals("Emo") ||
				favGenre.equals("New Romantic")){
			statementToGenre.append("Über Geschmack lässt sich ja bekanntlich streiten. " +
					"Aber " + favGenre + " - Dein Ernst?!");
		}
		else if(favGenre.equals("Post-Rock") || favGenre.equals("Classic Rock") || favGenre
				.equals("Metal") || favGenre.equals("Rock") || favGenre.equals("Death Metal") ||
				favGenre.equals("Hard Rock") || favGenre.equals("Alternative Rock") || favGenre
				.equals("Instrumental Rock") || favGenre.equals("Darkwave") || favGenre.equals
				("Gothic") || favGenre.equals("Alternative") || favGenre.equals("Folk Rock") ||
				favGenre.equals("Symphonic Rock") || favGenre.equals("Gothic Rock") || favGenre
				.equals("Progressive Rock") || favGenre.equals("Black Metal") || favGenre.equals
				("Heavy Metal") || favGenre.equals("Punk Rock") || favGenre.equals("Rythmic " +
				"Soul") || favGenre.equals("Thrash Metal") || favGenre.equals("Garage Rock") ||
				favGenre.equals("Space Rock") || favGenre.equals("Industro-Goth") || favGenre
				.equals("Garage") || favGenre.equals("Art Rock")){
			statementToGenre.append(favGenre + "? <br />In dir steckt bestimmt ein Headbanger!");
		}
		else if(favGenre.equals("Chillout") || favGenre.equals("Reggea") || favGenre.equals
				("Trip-Hop") || favGenre.equals("Hip-Hop")){
			statementToGenre.append("Deine Szene ist wahrscheinlich die Hip Hop Szene.<br />Du bist ein " +
					"sehr relaxter Mensch <br />und vermutlich gehören die Baggy Pants " +
					"zu deinen Lieblingskleidungstücken?");
		}
		else if(favGenre.equals("Blues") || favGenre.equals("Jazz") || favGenre.equals("Vocal")
				|| favGenre.equals("Jazz & Funk") || favGenre.equals("Soul")|| favGenre.equals
				("Ambient") || favGenre.equals("Illbient") || favGenre.equals("Lounge")){
			statementToGenre.append("Deinem Lieblingsgenre zu urteilen beschreibt sich dieses " +
					"Modul als wahren Kenner.<br />Vermutlich spielst du selber mindestens ein " +
					"Instrument <br />und verbringt dein Leben am liebsten entspannt mit einem " +
					"Glas Rotwein.");
		}
		else if(favGenre.equals("Gangsta") || favGenre.equals("Rap")){
			statementToGenre.append("Du hörst Rap. Vielleicht bis du sogar ein übler " +
					"Gangstarapper");
		}
		else if(favGenre.equals("Ska") || favGenre.equals("Acid Punk") || favGenre.equals("Punk")
				|| favGenre.equals("Polsk Punk") || favGenre.equals("Negerpunk") || favGenre
				.equals("Post-Punk")){
			statementToGenre.append("Deine Musiklieblingsrichtung ist Punk oder zumindest eine" +
					"Strömung des Punks.");
		}
		else if(favGenre.equals("Funk") || favGenre.equals("New Age") || favGenre.equals
				("Grunge")|| favGenre.equals("New Wave") || favGenre.equals("Rock & Roll") ||
				favGenre.equals("BritPop")|| favGenre.equals("Indie") || favGenre.equals("Porn " +
				"Groove") || favGenre.equals("Chanson") || favGenre.equals("Folk") || favGenre
				.equals("Experimental") || favGenre.equals("Neue Deutsche Welle") || favGenre
				.equals("Indie Rock")){
			statementToGenre.append("Dein Musikgeschmack, " + favGenre + ", " +
					"zeugt von Geschmack und Stil.");
		}
		else if(favGenre.equals("Podcast") || favGenre.equals("Audio Theatre") || favGenre.equals
				("Audiobook")|| favGenre.equals("Speech") || favGenre.equals("Satire") ||
				favGenre.equals("Soundtrack") || favGenre.equals("Sound Clip") || favGenre.equals
				("Comedy") || favGenre.equals("Cabaret") || favGenre.equals("Showtunes") ||
				favGenre.equals("Trailer") || favGenre.equals("Musical")){
			statementToGenre.append("Die Audiodatei lässt sich einer Art Literatur zuordnen. " +
					"<br />Du bist entweder sehr Literaturbegeistert und liebst Soundtracks und Co" +
					"<br />oder eine sehr faule Leseratte, die sich lieber alles vorlesen lässt. <br />" +
					"Wie auch immer du bist, " +
					"wahrscheinlich ein ziemlich belesener Mensch. ");
		} else if (favGenre.equals("Other")) {
			statementToGenre.append("Wer auch immer sich die ID3v1-Genres ausgedacht hat, " +
					"<br />diese Richtung als 'Other' zu " +
					"betiteln ist mehr als unaussagekräftig.<br />Du hast wahrscheinlich einen guten " +
					"Musikgeschmack, wenn man sich anschaut <br />was da so unter diese Bezeichnung " +
					"fällt :-)");
		} else {
			statementToGenre.append("Dein Musikgeschmack " + favGenre + " ist ziemlich " +
					"extravagant.");
		}

		stmtGenre = statementToGenre.toString();
		System.out.println(stmtGenre);

		return stmtGenre;
	}


	public void scoreFavArtist() {
		/**
		 * Sucht aus einer Liste aller Artisten des Lieblingsartisten heraus
		 *
		 * @param ArrayList<String> FileArtist
		 * @return void
		 */

		int count; //counts frequency of artist
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

	public void readId3Tag() {
		/**
		 * Liest den ID3 Tag von gefundenen MP3- und FLAC-Dateien aus
		 *
		 * @param ArrayList<File> localFiles
		 * @return void
		 * @remark benutzt Bibliothek "jid3lib-0.5.4.jar"
		 */

		String genre = ""; //Name of Genre

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
					gId = tagv1.getGenre(); //Get Genre ID

					try {
						genre = arrayGenre[gId]; //look up String to ID
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
			} catch (Exception e) {
				System.out.println("general Exception in readId3Tag");
				e.printStackTrace();
			}

		}

		scoreFavArtist(); //Call functions to find favArtist
		scoreFavGenre();   //Call functions to find favGenre

	}

	///////////////////////////////////////////
	///// Analysiere Musikprogramme //////////
	/////////////////////////////////////////

	public void checkNativeClients() {
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
			cltProgram = "";
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


	public void readBrowser(String searchUrl[]) {
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
			String sqlStatement = "SELECT * FROM urls WHERE url LIKE '%" + searchUrl[0] + "%'";
			for (int i = 1; i < searchUrl.length; i++) {
				sqlStatement += "OR url LIKE '%" + searchUrl[i] + "%' ";
			}

			//Datenbankabfrage Chrome
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + urls.get(0));
			statement = connection.createStatement();

			try {
				resultSet = statement.executeQuery(sqlStatement);
			} catch (SQLException e) {
				//e.printStackTrace();
				System.out.println("database file ist busy. Have to Close Browser to get acces.");
			}
			//Die Liste urls wird jetzt für alle passenden URLs der history genutzt
			urls.clear();

			try {
				while (resultSet.next()) {
					if (!(resultSet.getString("url").contains("google"))) {
						urls.add(resultSet.getString("url"));
					}
				}

				// Füge den String onlServices als Aufzählung zusammen
				for (int i = 1; i < urls.size(); i++) {
					String curr = urls.get(i);
					if (curr.contains("youtube.com") && !(onlService.contains("youtube.com"))) {
						if (onlService.isEmpty()) {
							onlService += "youtube.com";
						} else
							onlService += ", youtube.com";
					}
					if (curr.contains("myvideo.de") && !(onlService.contains("myvideo.de"))) {
						if (onlService.isEmpty()) {
							onlService += "myvideo.de";
						} else
							onlService += ", myvideo.de";
					}
					if (curr.contains("soundcloud.com") && !(onlService.contains("soundcloud.com"))) {
						if (onlService.isEmpty()) {
							onlService += "soundcloud.com";
						} else
							onlService += ", soundcloud.com";
					}
					if (curr.contains("dailymotion.com") && !(onlService.contains("dailymotion.com"))) {
						if (onlService.isEmpty()) {
							onlService += "dailymotion.com";
						} else
							onlService += ", dailymotion.com";
					}
					if (curr.contains("deezer.com") && !(onlService.contains("deezer.com"))) {
						if (onlService.isEmpty()) {
							onlService += "deezer.com";
						} else
							onlService += ", deezer.com";
					}
				}

				System.out.println("onlService:" + onlService);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					resultSet.close();
					statement.close();
					connection.close();
				} catch (NullPointerException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
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