package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.GlobalData;
import de.aima13.whoami.GuiManager;
import de.aima13.whoami.Whoami;
import de.aima13.whoami.support.DataSourceManager;
import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.ID3v1;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

/**
 * favourite Music, created 16.10.14.
 *
 * @author Inga Miadowicz
 * @version 1.1
 */

public class Music implements Analyzable {

	List<Path> musicDatabases = new ArrayList<>(); //Liste aller Dateien fürs Musikmodul
	List<Path> localFiles = new ArrayList<>();     //Liste von MP3-Dateien
	List<Path> browserFiles = new ArrayList<>();   //Liste der Browser-DB
	List<Path> exeFiles = new ArrayList<>();       //Liste der Musikprogramme
	ArrayList<String> urls = new ArrayList<>();
	Map<String, Integer> mapMaxApp = new HashMap<>();//Map: Artist - Häufigkeit
	Map<String, Integer> mapMaxGen = new HashMap<>();//Map Genre - Häufigkeit
	ResultSet mostVisited = null; //Liste der gefundenen URLs

	public String html = "";        //Output als HTML in String
	public String favArtist = "";
	public String favGenre = "";
	public String onlService = ""; //Genutzte Onlinedienste (siehe: MY_SEARCH_DELIVERY_URLS)
	public String cltProgram = ""; //Installierte Programme
	String stmtGenre = "";         //Kommentar zum Genre nach Kategorie
	String Qualität = "";          //Kommentar zu flac-Dateien
	long nrAudio = 0;              //Anzahl der Audiodateien

	private static final String[] MY_SEARCH_DELIEVERY_URLS = {"youtube.com", "myvideo.de", "dailymotion.com",
			"soundcloud.com", "deezer.com", "spotify.com", "play.google.com/store/music"};
	private static final String[] MY_SEARCH_DELIVERY_EXES = {"Deezer.exe", "spotify.exe",
			"Amazon Music.exe", "SWYH.exe", "iTunes.exe", "napster.exe", "simfy.exe"};
	private static final String[] MY_SEARCH_DELIVERY_NAMES = {"Deezer", "Spotify", "Amazon Music",
			"Stream What You Hear", "iTunes", "napster", "simfy"};
	private static final String[] MY_CSV_PREFIX = {"Lieblingskünstler", "Lieblingsgenre", "Onlineservices",
			"Musikprogramme", "Anzahl_Musikdateien"};

	private static final String TITLE = "Musikgeschmack";

	String[] arrayGenre = {    // Position im Array ist die ID des id3Tag:
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
	/**
	 * Implementierung der Methode run() von Runnable. Hier wird die Reihenfolge der Analyse
	 * festgelegt. Zusätzlich wird ein Time-boxing implementiert,
	 * um ungewöhnlich lange Laufzweiten zu vermeiden.
	 * @return void
	 * @param
	 */
	public void run() {
		getFilter();
		if (Whoami.getTimeProgress() < 100) {
			this.checkNativeClients(MY_SEARCH_DELIVERY_EXES, MY_SEARCH_DELIVERY_NAMES);
		}
		if (Whoami.getTimeProgress() < 300) {
			readBrowser(MY_SEARCH_DELIEVERY_URLS);
		}
		if (Whoami.getTimeProgress() < 1000) {
			GuiManager.updateProgress("Lese ID3-Tags deiner Musikdateien...");
			this.readId3Tag();
		}
		if(favGenre.equals("Emo")){
			GlobalData.getInstance().changeScore("Selbstmordgefährdung", 80);
			stmtGenre += " Aufgrund deines Lieblingsgenres sehen wir " +
					"eine deutliche erhöhte Selbstmordgefahr!";
		}
		if(favGenre.equals("Games")){
			GlobalData.getInstance().changeScore("Nerdfaktor", 70);
			stmtGenre += " Aufgrund deines Lieblingsgenres haben wir den Nerdfaktor" +
					" um 70 erhöht.";
		}
	}


	/////////////////////////////////////////////////////////////
	//// Übergebe Angaben für Informationen vom FileSearcher////
	///////////////////////////////////////////////////////////

	@Override
	/** Legt den Filter für den FileSearcher fest
	 *  @param
	 *  @return filterMusic
	 */
	public List<String> getFilter() {
		List<String> filterMusic = new ArrayList<>();

		// Audiodateien
		filterMusic.add("**.mp3");
		filterMusic.add("**.wav");
		filterMusic.add("**.wma");
		filterMusic.add("**.aac");
		filterMusic.add("**.ogg");
		filterMusic.add("**.flac");
		filterMusic.add("**.rm");
		filterMusic.add("**.M4a");
		filterMusic.add("**.vox");
		filterMusic.add("**.m4b");

		// Browser-history
		filterMusic.add("**Google/Chrome**History");
		filterMusic.add("**Firefox**places.sqlite");

		// installierte Programme
		filterMusic.add("**spotify.exe");
		filterMusic.add("**iTunes.exe");
		filterMusic.add("**SWYH.exe");
		filterMusic.add("**simfy.exe");
		filterMusic.add("**napster.exe");
		filterMusic.add("**Amazon*Music.exe");
		filterMusic.add("**Deezer.exe");

		return filterMusic;
	}

	@Override
	/**
	 * Ordnet musicDatabases für die Analyse des Musikgeschmacks
	 * @param List<File> files
	 * @return void
	 */
	public void setFileInputs(List<Path> files) throws Exception {
		long count = 0;

		//Überprüfe ob Dateien gefunden wurden
		if (!(files == null)) {
			musicDatabases = files;
		} else {
			throw new IllegalArgumentException("Auf dem Dateisystem konnten keine " +
					"Informationen zu Musik gefunden werden.");
		}

		//Benutzername wird an Globaldata übergeben
		String username = System.getProperty("user.name");
		GlobalData.getInstance().proposeData("Windows-Benutzername", username);

		//Spalte die Liste in drei Unterlisten:
		for (Path element : musicDatabases) {
			String path = element.toString();

			if (element.toString().contains(".mp3") || element.toString().contains(".MP3")) {
				localFiles.add(element);
				count++;

				//Kommentar zu ".flac-Dateien" abgeben
				if(element.toString().endsWith(".flac")) {
					Qualität = "Du bist ein richtiger Audiofan und legst wert auf die maximale " +
							"Qualität deiner Musiksammlung! ";
				}

				//Entferne Musik zu PC-Spielen (aus Steam), Beispielmusik und
				// Audiodateien die nicht auf Metadaten untersucht werden
				if(element.toString().contains("Steam") || element.toString().contains("Kalimba" +
						".mp3")	|| element.toString().contains("Sleep Away.mp3") || element.toString().contains("Maid with the Flaxen " +
						"Hair.mp3") || element.toString().contains("$RJLQJ56.mp3") || element
						.toString().contains("$IJLQJ56.mp3") || element.toString().endsWith("" +
						".wav") || element.toString().endsWith(".wma") || element.toString()
						.endsWith(".aac") || element.toString().endsWith(".ogg") || element.toString
						().endsWith(".flac") || element.toString().endsWith(".rm") || element
						.toString().endsWith(".M4a") || element.toString().endsWith(".vox") ||
						element.toString().endsWith("m4b")) {
					localFiles.remove(element);
				}
			} else if (element.toString().contains(".exe")) {
				exeFiles.add(element);
			} else if (path.contains(".sqlite") || (path.endsWith("\\History") && path.contains
					(username))) {
				browserFiles.add(element);
			}
		}

		musicDatabases.clear();
		nrAudio = count;
	}

	///////////////////////////////////////////////////////
	///// Output-Dateien vorbereiten /////////////////////
	/////////////////////////////////////////////////////

	@Override
	/**
	 * Das Ergebnis der Analyse wird in html als String in diesem Modul zusammengefügt
	 * @return String html
	 * @param
	 */
	public String getHtml() {
		StringBuilder buffer = new StringBuilder();

		// Ergebnistabelle
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
		buffer.append("</table>");

		// Abschlussfazit des Musikmoduls
		if (favGenre.equals("") && onlService.equals("") && cltProgram.equals("") && favArtist
				.equals("")) {
			buffer.append("Es wurden keine Informationen gefunden um den scheinbar " +
					"sehr geheimen Musikgeschmack des Users zu analysieren.");
		} else if (!(onlService.equals("")) && !(favArtist.equals("")) && !(favGenre.equals(""))
				&& !(cltProgram.equals(""))) {
			buffer.append("<br /><b>Fazit:</b> Dein Computer enthält Informationen zu allem " +
					"was wir gesucht haben. <br />Musik scheint ein wichtiger Teil deines Lebens " +
					"zu sein. <br />" + "Insgesamt haben wir " + nrAudio + " Musikdateien " +
					"gefunden." + Qualität +  stmtGenre);
		} else if (onlService.equals("") && cltProgram.equals("") && !(favGenre.equals(""))) {
			buffer.append("<br /><b>Fazit:</b> Deinen Geschmack konnten wir " +
					"erfolgreich analysieren:<br /> Insgesamt haben wir " + nrAudio +  " Musikdateien " +
					"gefunden." + Qualität + stmtGenre + ". Worrüber du deine Musik konsumierst " +
					"bleibt eine offene Frage.");
		} else if (favGenre.equals("") && favArtist.equals("")) {
			buffer.append("<br /><b>Fazit:</b> Es konnten keine Informationen dazu gefunden " +
					"werden was du hörst. Deine Lieblingsgenre und Lieblingkünstler bleiben eine " +
					"offene Frage...");
			if (!(onlService.equals("")) || !(cltProgram.equals(""))) {
				buffer.append(" Aber Musik hörst du über " + onlService + ", " + cltProgram + ".");
				if(nrAudio != 0){
					buffer.append("Insgesamt haben wir zusätzlich " + nrAudio + " Musikdateien " +
							"gefunden." + Qualität);
				}
			}
		} else {
			buffer.append("<br /><b>Fazit:</b> Zwar konnten einige Informationen über " +
					"dich nicht herausgefunden werden, <br />aber einiges wissen wir.");
			if(!(localFiles.isEmpty())){
				buffer.append("<br />Insgesamt haben wir " + nrAudio + " Musikdateien gefunden." +
						Qualität);
			}
			if (!(onlService.equals(""))) {
				buffer.append("<br />Du hörst über " + onlService + " online Musik.");
			}
			if (!(cltProgram.equals(""))) {
				buffer.append("<br />Auf deinem PC benutzt du zum Musik hören " + cltProgram + ".");
			}
			if (!(favArtist.equals(""))) {
				buffer.append( "<br />Deine Lieblingsband ist " + favArtist + ".");
			}
			if (!(favGenre.equals(""))) {
				buffer.append("<br />" + stmtGenre);
			}
		}

		html = buffer.toString();

		return html;
	}

	@Override
	/**
	 * Übergibt den Prefix ("Musikgeschmack") für den Output der PDF-Datei
	 * @param
	 * @return static final String TITLE
	 */
	public String getReportTitle() {
		return TITLE;
	}

	@Override
	/**
	 * Übergibt den Prefix ("Musikgeschmack") für den Output der CSV-Datei
	 * @param
	 * @return static final String TITLE
	 */
	public String getCsvPrefix() {
		return TITLE;
	}

	@Override
	public String[] getCsvHeaders() {
		return MY_CSV_PREFIX;
	}

	@Override
	/**
	 * Füllt die CSV-Datei mit den Analyseergebnissen
	 * @return SortedMap<String, String> csvData
	 * @param
	 */
	public SortedMap<String, String> getCsvContent() {
		SortedMap<String, String> csvData = new TreeMap<>();

		if (!(favArtist.equals(""))) {
			csvData.put("Lieblingskünstler", favArtist);
		} else csvData.put("Lieblingskünstler", "-");
		if (!(favGenre.equals(""))) {
			csvData.put("Lieblingsgenre", favGenre);
		} else csvData.put("Lieblingsgenre", "-");
		if (!(onlService.equals(""))) {
			csvData.put("Onlineservices", onlService);
		} else csvData.put("Onlineservices", "-");
		if (!(cltProgram.equals(""))) {
			csvData.put("Musikprogramme", cltProgram);
		} else csvData.put("Musikprogramme", "-");
		if (nrAudio != 0){
			String s = (new Long(nrAudio)).toString();
			csvData.put("Anzahl_Musikdateien", s);
		}
		return csvData;
	}


	///////////////////////////////////////////
	///// Analysiere Audidateien /////////////
	/////////////////////////////////////////

	/**
	 * Ordnet dem Genre eine Art Kategorie zu, wie im Architekturdokument angekündigt. Dazu
	 * wird zu jeder Kategory ein Kommentar, "hardcoded" als String hinzugefügt,
	 * um einen Fließtext im PDF-Dokument zu erhalten.
	 * @return String stmtGenre
	 * @param
	 */
	public String getCategory() {
		StringBuilder statementToGenre = new StringBuilder();

		if (favGenre.equals("Top 40") || favGenre.equals("House") || favGenre.equals("Drum & " +
				"Bass") || favGenre.equals("Euro-House")) {
			statementToGenre.append("<br />Dein Musikgeschmack ist nicht gerade " +
					"aussagekräftig.<br />Du scheinst nicht wirklich auszuwählen was " +
					"dir gefällt,<br />sondern orientierst dich an Listen und Freunden.<br />" +
					"Was dich charaktierisitert ist wahrscheinlich das Mainstream-Opfer");
		} else if (favGenre.equals("Dance") || favGenre.equals("Disco") || favGenre.equals("Dancehall")
				|| favGenre.equals("Samba") || favGenre.equals("Tango") || favGenre.equals("Club") ||
				favGenre.equals("Swing") || favGenre.equals("Latin") || favGenre.equals("Salsa")
				|| favGenre.equals("Eurodance") || favGenre.equals("Pop")) {
			statementToGenre.append("<br />Deinem Musikstil, " + favGenre + ", " +
					"nach zu urteilen,<br />schwingst du zumindest gerne dein Tanzbein oder bist " +
					"sogar eine richtige Dancing Queen! <3");
		} else if (favGenre.equals("Techno") || favGenre.equals("Industrial") || favGenre.equals
				("Acid Jazz") || favGenre.equals("Rave") || favGenre.equals("Psychedelic") ||
				favGenre.equals("Dream") || favGenre.equals("Elecronic") || favGenre
				.equals("Techno-Industrial") || favGenre.equals("Space") || favGenre.equals("Acid")
				|| favGenre.equals("Trance") || favGenre.equals("Fusion") ||
				favGenre.equals("Euro-Techno") || favGenre.equals("Hardcore Techno") || favGenre
				.equals("Goa") || favGenre.equals("Fast Fusion") || favGenre.equals("Synthpop") ||
				favGenre.equals("Dub") || favGenre.equals("Psytrance") || favGenre.equals
				("Dubstep") || favGenre.equals("Psybient")) {
			statementToGenre.append("<br />Dein Musikstil lässt darauf schließen, " +
					"<br />dass wenn man dich grob einer Richtung zuordnet du am ehesten einem Raver " +
					"entsprichst.");
		} else if (favGenre.equals("Retro") || favGenre.equals("Polka") || favGenre.equals
				("Country") || favGenre.equals("Oldies") || favGenre.equals("Native US") ||
				favGenre.equals("Southern Rock") || favGenre.equals("Instrumental") || favGenre
				.equals("Classical") || favGenre.equals("Gospel") || favGenre.equals("Folklore") ||
				favGenre.equals("A capella") || favGenre.equals("Symphony") ||
				favGenre.equals("Sonata") || favGenre.equals("Opera") || favGenre.equals
				("National Folk") || favGenre.equals("Avantgarde") || favGenre.equals("Baroque") ||
				favGenre.equals("World Music") || favGenre.equals("Neoclassical")) {
			statementToGenre.append("<br />Dein Musikstil" + favGenre + " ist eher von " +
					"traditioneller Natur und verrät uns, dass du in der Zeit stehen geblieben bist.");
		} else if (favGenre.equals("Christian Rap") || favGenre.equals("Pop-Folk") || favGenre
				.equals("Christian Rock") || favGenre.equals("Contemporary Christian") ||
				favGenre.equals("Christian Gangsta Rap") || favGenre.equals("Terror") || favGenre
				.equals("Jpop") || favGenre.equals("Math Rock") || favGenre.equals("Emo") ||
				favGenre.equals("New Romantic")) {
			statementToGenre.append("<br />Über Geschmack lässt sich ja bekanntlich streiten. " +
					"Aber " + favGenre + " - Dein Ernst?!");
		} else if (favGenre.equals("Post-Rock") || favGenre.equals("Classic Rock") || favGenre
				.equals("Metal") || favGenre.equals("Rock") || favGenre.equals("Death Metal") ||
				favGenre.equals("Hard Rock") || favGenre.equals("Alternative Rock") || favGenre
				.equals("Instrumental Rock") || favGenre.equals("Darkwave") || favGenre.equals
				("Gothic") || favGenre.equals("Folk Rock") ||
				favGenre.equals("Symphonic Rock") || favGenre.equals("Gothic Rock") || favGenre
				.equals("Progressive Rock") || favGenre.equals("Black Metal") || favGenre.equals
				("Heavy Metal") || favGenre.equals("Punk Rock") || favGenre.equals("Rythmic " +
				"Soul") || favGenre.equals("Thrash Metal") || favGenre.equals("Garage Rock") ||
				favGenre.equals("Space Rock") || favGenre.equals("Industro-Goth") || favGenre
				.equals("Garage") || favGenre.equals("Art Rock")) {
			statementToGenre.append(" " + favGenre + "? In dir steckt bestimmt ein Headbanger! " +
					"Yeah" + "\\m/ !!!");
		} else if (favGenre.equals("Chillout") || favGenre.equals("Reggea") || favGenre.equals
				("Trip-Hop") || favGenre.equals("Hip-Hop")) {
			statementToGenre.append("<br />Deine Szene ist wahrscheinlich die Hip Hop Szene.<br />Du bist ein " +
					"sehr relaxter Mensch <br />und vermutlich gehören die Baggy Pants " +
					"zu deinen Lieblingskleidungstücken?");
		} else if (favGenre.equals("Blues") || favGenre.equals("Jazz") || favGenre.equals("Vocal")
				|| favGenre.equals("Jazz & Funk") || favGenre.equals("Soul") || favGenre.equals
				("Ambient") || favGenre.equals("Illbient") || favGenre.equals("Lounge")) {
			statementToGenre.append("<br />Deinem Lieblingsgenre zu urteilen beschreibt sich dieses " +
					"Modul als wahren Kenner.<br />Vermutlich spielst du selber mindestens ein " +
					"Instrument <br />und verbringt dein Leben am liebsten entspannt mit einem " +
					"Glas Rotwein.");
		} else if (favGenre.equals("Gangsta") || favGenre.equals("Rap")) {
			statementToGenre.append("<br />Du hörst Rap. Vielleicht bis du sogar ein übler " +
					"Gangstarapper");
		} else if (favGenre.equals("Ska") || favGenre.equals("Acid Punk") || favGenre.equals("Punk")
				|| favGenre.equals("Polsk Punk") || favGenre.equals("Negerpunk") || favGenre
				.equals("Post-Punk")) {
			statementToGenre.append("<br />Deine Musiklieblingsrichtung ist Punk oder zumindest eine" +
					"Strömung des Punks. ");
		} else if (favGenre.equals("Funk") || favGenre.equals("New Age") || favGenre.equals
				("Grunge") || favGenre.equals("New Wave") || favGenre.equals("Rock & Roll") ||
				favGenre.equals("BritPop") || favGenre.equals("Indie") || favGenre.equals("Porn " +
				"Groove") || favGenre.equals("Chanson") || favGenre.equals("Folk") || favGenre
				.equals("Experimental") || favGenre.equals("Neue Deutsche Welle") || favGenre
				.equals("Indie Rock") || favGenre.equals("Alternative")) {
			statementToGenre.append("<br />Dein Musikgeschmack, " + favGenre + ", " +
					"zeugt auf jeden Fall von Geschmack und Stil.");
		} else if (favGenre.equals("Podcast") || favGenre.equals("Audio Theatre") || favGenre.equals
				("Audiobook") || favGenre.equals("Speech") || favGenre.equals("Satire") ||
				favGenre.equals("Soundtrack") || favGenre.equals("Sound Clip") || favGenre.equals
				("Comedy") || favGenre.equals("Cabaret") || favGenre.equals("Showtunes") ||
				favGenre.equals("Trailer") || favGenre.equals("Musical")) {
			statementToGenre.append("<br />Die Audiodatei lässt sich einer Art Literatur zuordnen. " +
					"<br />Du bist entweder sehr Literaturbegeistert und liebst Soundtracks und Co" +
					"<br />oder eine sehr faule Leseratte, die sich lieber alles vorlesen lässt. <br />" +
					"Wie auch immer du bist, " +
					"wahrscheinlich ein ziemlich belesener Mensch. ");
		} else {
			statementToGenre.append("<br />Dein Musikgeschmack " + favGenre + " <br />ist auf jeden " +
					"Fall ziemlich " +
					"extravagant.");
		}
		stmtGenre = statementToGenre.toString();

		return stmtGenre;
	}

	/**
	 * Liest den ID3 Tag von gefundenen MP3- und FLAC-Dateien aus
	 *
	 * @param
	 * @return void
	 * @remark benutzt Bibliothek "jid3lib-0.5.4.jar"
	 * @exception org.farng.mp3.TagException, FileNotFoundException,
	 * UnsupportedOperationException, IOException, Exception
	 */
	public void readId3Tag() {
		String genre = ""; //Name of Genre
		int numberAmax = 0;
		int numberGmax = 0;
		GuiManager.updateProgress("Wer ist wohl dein Lieblingskünstler und was hörst du für " +
				"Musik?");
		if (!(localFiles.isEmpty())){
			for (Path file : localFiles) {
				int numberA = 0;
				int numberG = 0;
				try {
					String fileLocation = file.toAbsolutePath().toString(); //Get path to file
					MP3File mp3file = new MP3File(fileLocation); //create new object from ID3tag-package

					if (mp3file.hasID3v2Tag()) {
						AbstractID3v2 tagv2 = mp3file.getID3v2Tag();

						//Analyse Lieblingskünstler
						String artist = tagv2.getLeadArtist().replaceAll("^[a-zA-Z0-9ß?!.,äüö]",
								""); //Ersetze Sonderzeichen
						if(!artist.equals("") && !(artist.contains("\\"))){ //Fehlerhafte
						// ID3-tags abfangen
							if(!(mapMaxApp.containsKey(artist))){ //Erstelle Eintrag
								numberA++;
								mapMaxApp.put(artist,numberA);
								if(numberA > numberAmax){ //Überprüfe Favorit
									favArtist = artist;
									numberAmax = numberA;
								}
							} else {
								numberA = mapMaxApp.get(tagv2.getLeadArtist()); //Erhöhe Häufigkeit
								mapMaxApp.remove(tagv2.getLeadArtist());
								numberA++;
								mapMaxApp.put(tagv2.getLeadArtist(), numberA);
								if(numberA > numberAmax){ //Überprüfe Favorit
									favArtist = tagv2.getLeadArtist();
									numberAmax = numberA;
								}
							}
						}

						//Analyse Lieblingsgenre
						genre = tagv2.getSongGenre();
						if(!genre.equals("") && !(genre.contains("\\"))) {
							//Einige ID3-Tags sind fehlerhaft und die ID wird in der Form "(XX)"als String
							// gespeichert. Hier wird nochmal geguckt ob das Genre zugeordnet werden kann.
							if (genre.startsWith("(")) {
								String str;
								str = genre.replaceAll("\\D+", "");
								short gId = Short.parseShort(str);
								try {
									genre = arrayGenre[gId];
								} catch (ArrayIndexOutOfBoundsException e) {
									// Das ID existiert nicht
								}
							}
						}
						//Da die Genres bei dieser Version als String gespeichert sind,
						// kann das Feld auch unsinnige Genres enthalten. Gleicht das Genre nicht
						// einem der bekannten aus der Liste wird es nicht aufgenommen
						for(int i = 0; i<arrayGenre.length; i++){
							if(genre.equals(arrayGenre[i])){
								if(!(mapMaxGen.containsKey(genre))){ //Erstelle neuen Eintrag
									numberG++;
									mapMaxGen.put(genre, numberG);
									if(numberG > numberGmax || !genre.equals("Other")){ //Überprüfe
									// auf Favorit und ignoriere 'Other',
									// da dieses Genre unaussagekräftig ist.
										favGenre = genre;
										numberGmax = numberG;
									}
								} else {
									numberG = mapMaxGen.get(genre); //Erhöhe Häufigkeit
									mapMaxGen.remove(genre);
									numberG++;
									mapMaxGen.put(genre, numberG);
									if(numberG > numberGmax || !genre.equals("Other")){ //Überprüfe auf Favorit
										favGenre = genre;
										numberGmax = numberG;
									}
								}
							}
						}
					} else if (mp3file.hasID3v1Tag()) {
						ID3v1 tagv1 = mp3file.getID3v1Tag();

						//Analyse Lieblingskünstler
						String artist = tagv1.getArtist().replaceAll("^[a-zA-Z0-9ß?!.,äüö]",
								""); //Ersetze dabei Sonderzeichen
						if(!(artist.equals(""))&& !(artist.contains("\\"))) {
							//Fehlerhafte ID3-tags abfangen (\\ leitet Fehlercode ein)
								if (!(mapMaxApp.containsKey(tagv1.getLeadArtist()))) { //Erstelle neuen Eintrag
									numberA++;
									mapMaxApp.put(tagv1.getLeadArtist(), numberA);
									if (numberA > numberAmax) { //Überprüfe auf Favorit
										favArtist = tagv1.getLeadArtist();
										numberAmax = numberA;
									}
								} else {
									numberA = mapMaxApp.get(tagv1.getLeadArtist()); //Erhöhe
									// Häufigkeit
									mapMaxApp.remove(tagv1.getLeadArtist());
									numberA++;
									mapMaxApp.put(tagv1.getLeadArtist(), numberA);
									if (numberA > numberAmax) { //Überprüfe auf Favorit
										favArtist = tagv1.getLeadArtist();
										numberAmax = numberA;
									}
								}
							}

						//Analyse Lieblingsgenre
						short gId = tagv1.getGenre(); //Get Genre ID
						if(gId != 0) {
							try {
								genre = arrayGenre[gId]; // Map Genre-ID zu Genre-Name
							} catch (ArrayIndexOutOfBoundsException e) {
								// Die Genre-ID existiert offiziell nicht
							}
							for (int i = 0; i < arrayGenre.length; i++) {
								if (genre.equals(arrayGenre[i])) {
									if (!(mapMaxGen.containsKey(genre))) {
										numberG++;
										mapMaxGen.put(genre, numberG);
										if(numberG > numberGmax || !genre.equals("Other")){
											favGenre = genre;
											numberGmax = numberG;
										}
									} else {
										numberG = mapMaxGen.get(genre);
										mapMaxGen.remove(genre);
										numberG++;
										mapMaxGen.put(genre, numberG);
										if(numberG > numberGmax || !genre.equals("Other")){
											favGenre = genre;
											numberGmax = numberG;
										}
									}
								}
							}

						}
					}

				} catch (TagException e) {
					//
				} catch (FileNotFoundException e) {
					//Dateipfad existiert nicht oder der Zugriff wurde verweigert
				} catch (UnsupportedOperationException e) {
					//MP3-File Objekt kann nicht gebildet werden
				} catch (IOException e) {
					//
				} catch (Exception e) {
					// ungültige Dateinamen, die nicht verarbeitet werden können
				}
				getCategory();
			}
		}
	}
	///////////////////////////////////////////
	///// Analysiere Musikprogramme //////////
	/////////////////////////////////////////

	/**
	 * Überprüft welche Musikprogramme gefunden wurden und speichert diese global als Liste in der
	 * Variable cltProgram
	 * @return void
	 * @param
	 */
	public void checkNativeClients(String exes[], String names[]) {
		GuiManager.updateProgress("Wie du Musik hörst prüfen wir auch...");
		for (Path currentExe : exeFiles) {
			for(int i = 0; i < exes.length; i++) {
				if (currentExe.toString().endsWith(exes[i])) {
					if(cltProgram.equals("")){
						cltProgram = names[i];
					}
					else if(!(cltProgram.contains(names[i]))){
						cltProgram += ", " + names[i];
					}
				}
			}
		}
	}

	///////////////////////////////////////////
	///// Analysiere Browserverlauf //////////
	/////////////////////////////////////////

	/**
	 * Durchsucht den Browser-Verlauf auf bekannte Musikportale (MY_SEARCH_DELIEVERY_URLS)
	 * @param searchUrl "final static String[] MY_SEARCH_DELIEVERY_URLS" wird übergeben
	 * @return void
	 * @exception java.sql.SQLException
	 */
	public void readBrowser(String searchUrl[]) {
		GuiManager.updateProgress("Was nutzt du wohl online zum Musikhören?");
		for (Path db : browserFiles) {
			try {
				mostVisited = dbExtraction(db, MY_SEARCH_DELIEVERY_URLS);
				if(mostVisited != null) {
					while (mostVisited.next()) {
						String urlName = "";
						urlName = mostVisited.getString("host");
						if (urlName != null && !urlName.equals("")) {
							if (!(urls.contains(urlName))) {
								urls.add(urlName);
							}
						}
					}
				}
			} catch (SQLException e) {
				//Ergebnis ist leer
			} finally {
				//Schließe ResultSet imd Statement
				if (mostVisited != null) {
					try {
						mostVisited.close();
						mostVisited.getStatement().close();
					} catch (SQLException e) {
						//Keine DB
					}
				}
			}
		}

		// Füge den String onlServices als Aufzählung zusammen
		for (int i = 0; i < urls.size(); i++) {
			for(int j = 0; j < searchUrl.length; j++){
				if(urls.get(i).contains(searchUrl[j]) && !(onlService.contains(searchUrl[j]))){
					if (onlService.isEmpty()) {
						onlService += searchUrl[j]; // erster Dienst
					} else {
						onlService += ", " + searchUrl[j]; // weitere Dienste werden mit Komma
						// angehangen
					}
				}
			}
		}
	}

	/**
	 * Durchsucht den Browser-Verlauf auf bekannte Musikportale (MY_SEARCH_DELIEVERY_URLS)
	 * @param sqliteDb
	 * @param searchUrl "final static String[] MY_SEARCH_DELIEVERY_URLS" wird übergeben
	 * @return mostVisited Ergebnisliste aller gefundener URLs/Hosts
	 */
	private ResultSet dbExtraction(Path sqliteDb, String searchUrl[]) {
		DataSourceManager dbManager;
		try {
			dbManager = new DataSourceManager(sqliteDb);

			//Kontruktion des SQL-Statements für Firefox
			if (sqliteDb.toString().contains("Firefox")) {
				String sqlStatement = "SELECT host " +
						"FROM moz_hosts " +
						"WHERE host LIKE '" + searchUrl[0] + "'";
				for (int i = 1; i < searchUrl.length; i++) {
					sqlStatement += " OR host LIKE '" + searchUrl[i] + "'";
				}
				mostVisited = dbManager.querySqlStatement(sqlStatement);
			}

			//Kontruktion des SQL-Statements für Chrome
			else if (sqliteDb.toString().contains("Chrome")) {
				String sqlStatement = "SELECT url AS host " +
						"FROM urls " +
						"WHERE host LIKE '%" + searchUrl[0] + "%'";
				for (int i = 1; i < searchUrl.length; i++) {
					sqlStatement += " OR host LIKE '%" + searchUrl[i] + "%'";
				}
				mostVisited = dbManager.querySqlStatement(sqlStatement);
			}
		} catch (ClassNotFoundException | SQLException | NullPointerException e) {
			// Deadlock auf DB | es kommt null auf die DB-Abfrage zurück
		}
		return mostVisited;
	}

}