package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.GlobalData;
import de.aima13.whoami.Whoami;
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
 * @version 1.0
 */

public class Music implements Analyzable {

	List<Path> musicDatabases = new ArrayList<>(); //Liste aller fürs Musikmodul wichtige Dateien
	List<Path> localFiles = new ArrayList<>(); //Liste von MP3- und FLAC-Dateien
	List<Path> browserFiles = new ArrayList<>(); //Liste der Browser-DB
	List<Path> exeFiles = new ArrayList<>(); //Liste der Musikprogramme
	ArrayList<String> FileArtist = new ArrayList<>();
	ArrayList<String> FileGenre = new ArrayList<>();
	ArrayList<String> urls = new ArrayList<>();
	Map<String, Integer> mapMaxApp = new HashMap<>();//Map: Artist - Häufigkeit
	Map<String, Integer> mapMaxGen = new HashMap<>();//Map Genre - Häufigkeit

	public String html = ""; //Output der HTML
	public String favArtist = "";
	public String favGenre = "";
	public String onlService = ""; //Genutzte Onlinedienste (siehe: MY_SEARCH_DELIVERY_URLS)
	public String cltProgram = ""; //Installierte Programme
	String stmtGenre = ""; //Kommentar zum Genre nach Kategorie
	private boolean cancelledByTimeLimit = false;

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

		if (Whoami.getTimeProgress() >= 99) {
			//Ausstieg wegen Timeboxing
			cancelledByTimeLimit = true;
			return;
		}
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
		//Alle Dateien die mit ID3Tag kompatibel sind
		filterMusic.add("**.mp3");
		filterMusic.add("**.MP3");
		filterMusic.add("**.mP3");
		filterMusic.add("**.Mp3");

		//filterMusic.add("**.m4b");//Format für Hörbücher
		//filterMusic.add("**.aax"); //Format für Hörbücher (Audible)

		//b) Browser-history
		filterMusic.add("**Google/Chrome**History");
		filterMusic.add("**Firefox**places.sqlite");

		//c) installierte Programme
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

			if (Whoami.getTimeProgress() >= 99) {
				//Ausstieg wegen Timeboxing
				cancelledByTimeLimit = true;
				break;
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

		buffer.append("</table>");

		// Abschlussfazit des Musikmoduls
		if (musicDatabases.isEmpty()) {
			buffer.append("Es wurden keine Informationen gefunden um den scheinbar " +
					"sehr geheimen Musikgeschmack des Users zu analysieren.");
		} else if (!(onlService.equals("")) && !(favArtist.equals("")) && !(favGenre.equals(""))
				&& !(cltProgram.equals(""))) {
			buffer.append("<br /><b>Fazit:</b> Dein Computer enthält Informationen zu allem " +
					"was wir gesucht haben.<br /> Musik scheint ein wichtiger Teil deines Lebens " +
					"zu sein. <br />" + stmtGenre);
		} else if (onlService.equals("") && cltProgram.equals("") && !(favGenre.equals(""))) {
			buffer.append("<br /><b>Fazit:</b> Das Modul konnte weder online noch nativ " +
					"herausfinden wie du Musik hörst. Du scheinst dies über einen nicht sehr " +
					"verbreiteten Weg zu machen. Nichts desto trotz konnten wir deinen Geschmack " +
					"analysieren:<br /> " + stmtGenre);
		} else if (favGenre.equals("") && favArtist.equals("")) {
			buffer.append("<br /><b>Fazit:</b> Es konnten keine Informationen zu deinem " +
					"Musikgeschmack gefunden werden.");
			if (!(onlService.equals("")) || !(cltProgram.equals(""))) {
				buffer.append("Aber Musik hörst du über " + onlService + ", " + cltProgram + "" +
						". Nur was bleibt eine offene Frage.");
			}
		} else {
			buffer.append("<br /><b>Fazit:</b> Zwar konnten einige Informationen über " +
					"dich nicht herausgefunden werden, <br />aber einiges wissen wir.");
			if (!(onlService.equals(""))) {
				buffer.append("<br />Du hörst über " + onlService + " online Musik.");
			}
			if (!(cltProgram.equals(""))) {
				buffer.append("<br />Auf deinem PC benutzt du zum Musik hören " + cltProgram + ".");
			}
			if (!(favArtist.equals(""))) {
				buffer.append("<br />Deine Lieblingsband ist " + favArtist + ".");
			}
			if (!(favGenre.equals(""))) {
				buffer.append("<br />" + stmtGenre);
			}

		}

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

		if (!(favArtist.equals(""))) {
			csvData.put("Lieblingskünstler", favArtist);
		}
		if (!(favArtist.equals(""))) {
			csvData.put("Lieblingsgenre", favGenre);
		}
		if (!(favArtist.equals(""))) {
			csvData.put("Onlineservices", onlService);
		}
		if (!(favArtist.equals(""))) {
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

			//Einige ID3-Tags sind fehlerhaft und das Byte wird in der Form "(XX)"als String
			// gespeichert. Hier wird nochmal geguckt ob das Genre zugeordnet werden kann.
			if (each.startsWith("(")) {
				String str;
				str = each.replaceAll("\\D+", "");
				byte gId = Byte.parseByte(str);
				each = arrayGenre[gId];
			}

			count = 0;
			if (mapMaxGen.containsKey(each)) {
				count = mapMaxGen.get(each);
				mapMaxGen.remove(each);
			}
			count++;
			mapMaxGen.put(each, count);

			if (Whoami.getTimeProgress() >= 99) {
				//Ausstieg wegen Timeboxing
				cancelledByTimeLimit = true;
				return;
			}
		}

		//System.out.println("Hörbuch: " + mapMaxGen.get("Hörbuch"));

		//Finde Genre mit der höchsten Häufigkeit
		Iterator it = mapMaxGen.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			if ((int) (pairs.getValue()) > max || !(pairs.equals("Other"))) {
				favGenre = (String) pairs.getKey();
				max = (int) (pairs.getValue());
			}
			it.remove();
		}

		getCategory();

		////Ist das Lieblingsgenre "Emo" wird die Selbstmordgefährdung erhöht
		if (favGenre.equals("Emo")) {
			GlobalData.getInstance().changeScore("Selbstmordgefährung", 50);
		}
		//Ist das Lieblingsgenre "Dance" oder "Disco" wird die Selbstmordgefährdung verringert
		else if (favGenre.equals("Dance") || favGenre.equals("Disco")) {
			GlobalData.getInstance().changeScore("Selbstmordgefährung", -20);
		}
		//Ist das Lieblingsgenre "Chillout" wird der Faulenzerfaktor erhöht
		else if (favGenre.equals("Chillout")) {
			GlobalData.getInstance().changeScore("Faulenzerfaktor", 40);
		}

	}

	public String getCategory() {
		/**
		 * Ordnet dem Genre eine Art Kategorie zu und füllt die Variable statementToGenre,
		 * damit im html-Output ein Kommentar zum Genre abgegeben werden kann.
		 *
		 * @return void
		 * @param byte genreByte
		 */

		StringBuilder statementToGenre = new StringBuilder();

		if (favGenre.equals("Top 40") || favGenre.equals("House") || favGenre.equals("Drum & " +
				"Bass") || favGenre.equals("Euro-House")) {
			statementToGenre.append("Dein Musikgeschmack ist nicht gerade " +
					"aussagekräftig.<br />Du scheinst nicht wirklich auszuwählen was " +
					"dir gefällt,<br />sondern orientierst dich an Listen und Freunden.<br />" +
					"Was dich charaktierisitert ist wahrscheinlich das Mainstream-Opfer");
		} else if (favGenre.equals("Dance") || favGenre.equals("Disco") || favGenre.equals("Dancehall")
				|| favGenre.equals("Samba") || favGenre.equals("Tango") || favGenre.equals("Club") ||
				favGenre.equals("Swing") || favGenre.equals("Latin") || favGenre.equals("Salsa")
				|| favGenre.equals("Eurodance")) {
			statementToGenre.append("Deinem Musikstil, " + favGenre + ", " +
					"nach zu urteilen,<br />schwingst " +
					"du gerne dein Tanzbein.");
		} else if (favGenre.equals("Techno") || favGenre.equals("Industrial") || favGenre.equals
				("Acid Jazz") || favGenre.equals("Rave") || favGenre.equals("Psychedelic") ||
				favGenre.equals("Dream") || favGenre.equals("Elecronic") || favGenre
				.equals("Techno-Industrial") || favGenre.equals("Space") || favGenre.equals("Acid")
				|| favGenre.equals("Trance") || favGenre.equals("Fusion") ||
				favGenre.equals("Euro-Techno") || favGenre.equals("Hardcore Techno") || favGenre
				.equals("Goa") || favGenre.equals("Fast Fusion") || favGenre.equals("Synthpop") ||
				favGenre.equals("Dub") || favGenre.equals("Psytrance") || favGenre.equals
				("Dubstep") || favGenre.equals("Psybient")) {
			statementToGenre.append("Dein Musikstil lässt darauf schließen, " +
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
			statementToGenre.append("Dein Musikstil" + favGenre + "ist eher von traditioneller" +
					" Natur.");
		} else if (favGenre.equals("Christian Rap") || favGenre.equals("Pop-Folk") || favGenre
				.equals("Christian Rock") || favGenre.equals("Contemporary Christian") ||
				favGenre.equals("Christian Gangsta Rap") || favGenre.equals("Terror") || favGenre
				.equals("Jpop") || favGenre.equals("Math Rock") || favGenre.equals("Emo") ||
				favGenre.equals("New Romantic")) {
			statementToGenre.append("Über Geschmack lässt sich ja bekanntlich streiten. " +
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
			statementToGenre.append(favGenre + "? In dir steckt bestimmt ein Headbanger!");
		} else if (favGenre.equals("Chillout") || favGenre.equals("Reggea") || favGenre.equals
				("Trip-Hop") || favGenre.equals("Hip-Hop")) {
			statementToGenre.append("Deine Szene ist wahrscheinlich die Hip Hop Szene.<br />Du bist ein " +
					"sehr relaxter Mensch <br />und vermutlich gehören die Baggy Pants " +
					"zu deinen Lieblingskleidungstücken?");
		} else if (favGenre.equals("Blues") || favGenre.equals("Jazz") || favGenre.equals("Vocal")
				|| favGenre.equals("Jazz & Funk") || favGenre.equals("Soul") || favGenre.equals
				("Ambient") || favGenre.equals("Illbient") || favGenre.equals("Lounge")) {
			statementToGenre.append("Deinem Lieblingsgenre zu urteilen beschreibt sich dieses " +
					"Modul als wahren Kenner.<br />Vermutlich spielst du selber mindestens ein " +
					"Instrument <br />und verbringt dein Leben am liebsten entspannt mit einem " +
					"Glas Rotwein.");
		} else if (favGenre.equals("Gangsta") || favGenre.equals("Rap")) {
			statementToGenre.append("Du hörst Rap. Vielleicht bis du sogar ein übler " +
					"Gangstarapper");
		} else if (favGenre.equals("Ska") || favGenre.equals("Acid Punk") || favGenre.equals("Punk")
				|| favGenre.equals("Polsk Punk") || favGenre.equals("Negerpunk") || favGenre
				.equals("Post-Punk")) {
			statementToGenre.append("Deine Musiklieblingsrichtung ist Punk oder zumindest eine" +
					"Strömung des Punks.");
		} else if (favGenre.equals("Funk") || favGenre.equals("New Age") || favGenre.equals
				("Grunge") || favGenre.equals("New Wave") || favGenre.equals("Rock & Roll") ||
				favGenre.equals("BritPop") || favGenre.equals("Indie") || favGenre.equals("Porn " +
				"Groove") || favGenre.equals("Chanson") || favGenre.equals("Folk") || favGenre
				.equals("Experimental") || favGenre.equals("Neue Deutsche Welle") || favGenre
				.equals("Indie Rock") || favGenre.equals("Alternative")) {
			statementToGenre.append("Dein Musikgeschmack, " + favGenre + ", " +
					"zeugt auf jeden Fall von Geschmack und Stil.");
		} else if (favGenre.equals("Podcast") || favGenre.equals("Audio Theatre") || favGenre.equals
				("Audiobook") || favGenre.equals("Speech") || favGenre.equals("Satire") ||
				favGenre.equals("Soundtrack") || favGenre.equals("Sound Clip") || favGenre.equals
				("Comedy") || favGenre.equals("Cabaret") || favGenre.equals("Showtunes") ||
				favGenre.equals("Trailer") || favGenre.equals("Musical")) {
			statementToGenre.append("Die Audiodatei lässt sich einer Art Literatur zuordnen. " +
					"<br />Du bist entweder sehr Literaturbegeistert und liebst Soundtracks und Co" +
					"<br />oder eine sehr faule Leseratte, die sich lieber alles vorlesen lässt. <br />" +
					"Wie auch immer du bist, " +
					"wahrscheinlich ein ziemlich belesener Mensch. ");
		} else if (favGenre.equals("Other") || favGenre.equals("Andere")) {
			statementToGenre.append("Du hast anscheinend mehr MP3-Files in deiner " +
					"Spielebibliothek <br/ >als sonst auf dem PC. Das Genre dieser Dateien wird " +
					"als <br />'" +
					favGenre + "betitelt.");
		} else {
			statementToGenre.append("Dein Musikgeschmack " + favGenre + " <br />ist auf jeden " +
					"Fall ziemlich " +
					"extravagant.");
		}

		stmtGenre = statementToGenre.toString();

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

			if (Whoami.getTimeProgress() >= 99) {
				//Ausstieg wegen Timeboxing
				cancelledByTimeLimit = true;
				return;
			}
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
		int count = 0;

		for (Path file : localFiles) {
			/*//Zähle Anzahl der Hörbücher und lösche sie
			if(!(file.toString().endsWith(".mp3") || file.toString().endsWith(".MP3"))){
				if (mapMaxGen.containsKey("Hörbuch")) {
					count = mapMaxGen.get("Hörbuch");
					mapMaxGen.remove("Hörbuch");
				}
				count++;
				mapMaxGen.put("Hörbuch", count);
				localFiles.remove(file);
				break;
			}*/

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
					} catch (ArrayIndexOutOfBoundsException e) {
						//System.out.println("This Genre doesn't exist");
					}

					FileGenre.add(genre); //Fill List of Type String with genre

					//System.out.println("Artists: " + FileArtist);
					//System.out.println("Genre: " + FileGenre);

				}
			} catch (TagException e) {
			} //bewusst ignoriert
			catch (FileNotFoundException e) {
			} catch (IOException e) {
			} catch (UnsupportedOperationException e) {
			} catch (Exception e) {
			}

			if (Whoami.getTimeProgress() >= 99) {
				//Ausstieg wegen Timeboxing
				cancelledByTimeLimit = true;
				return;
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
			if (currentExe.toString().endsWith("Amazon Music.exe")) {
				clients[count] = "Amazon Music";
				count++;
			}
			if (currentExe.toString().endsWith("napster.exe")) {
				clients[count] = "napster";
				count++;
			}
			if (currentExe.toString().endsWith("Deezer.exe")) {
				clients[count] = "deezer";
				count++;
			}

			if (Whoami.getTimeProgress() >= 99) {
				//Ausstieg wegen Timeboxing
				cancelledByTimeLimit = true;
				return;
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
		} else if (count == 5) {
			cltProgram = clients[0] + ", " + clients[1] + ", " + clients[2] + ", " +
					"" + clients[3] + ", " + clients[4];
		} else if (count == 6) {
			cltProgram = clients[0] + ", " + clients[1] + ", " + clients[2] + ", " +
					"" + clients[3] + ", " + clients[4] + ", " + clients[5];
		} else if (count == 7) {
			cltProgram = clients[0] + ", " + clients[1] + ", " + clients[2] + ", " +
					"" + clients[3] + ", " + clients[4] + ", " + clients[5] + ", " + clients[6];
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
				//System.out.println("database file ist busy. Have to Close Browser to get acces.");
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
						} else {
							onlService += ", youtube.com";
						}
					}
					if (curr.contains("myvideo.de") && !(onlService.contains("myvideo.de"))) {
						if (onlService.isEmpty()) {
							onlService += "myvideo.de";
						} else {
							onlService += ", myvideo.de";
						}
					}
					if (curr.contains("soundcloud.com") && !(onlService.contains("soundcloud.com"))) {
						if (onlService.isEmpty()) {
							onlService += "soundcloud.com";
						} else {
							onlService += ", soundcloud.com";
						}
					}
					if (curr.contains("dailymotion.com") && !(onlService.contains("dailymotion.com"))) {
						if (onlService.isEmpty()) {
							onlService += "dailymotion.com";
						} else {
							onlService += ", dailymotion.com";
						}
					}
					if (curr.contains("deezer.com") && !(onlService.contains("deezer.com"))) {
						if (onlService.isEmpty()) {
							onlService += "deezer.com";
						} else {
							onlService += ", deezer.com";
						}
					}

					if (Whoami.getTimeProgress() >= 99) {
						//Ausstieg wegen Timeboxing
						cancelledByTimeLimit = true;
						return;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					resultSet.close();
					statement.close();
					connection.close();
				} catch (NullPointerException e) {
				} catch (Exception e) {
				}
			}
		} catch (ClassNotFoundException e) {
		} catch (SQLException e) {
		} catch (IndexOutOfBoundsException e) {
		}
	}


	private void dbExtraction() {
		/**
		 *
		 *
		 * @param
		 * @retrun void
		 */

		//Benutzername wird an Globaldata übergeben
		String username = System.getProperty("user.name");
		GlobalData.getInstance().proposeData("Benutzername", username);

		//Richtige Datenbank hinzufügen
		int foundDbs = 0;
		try {
			for (Path curr : browserFiles) {
				if (curr != null) {
					String path = "";
					try {
						path = curr.toString();
					} catch (Exception e) {
					}

					//Unterscheidung zwischen Firefox und Chrome Datenbank
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

				if (Whoami.getTimeProgress() >= 99) {
					//Ausstieg wegen Timeboxing
					cancelledByTimeLimit = true;
					return;
				}
			}
		} catch (Exception e) {
		}
	}
}