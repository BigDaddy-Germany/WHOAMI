package de.aima13.whoami.modules.music;

import de.aima13.whoami.GuiManager;
import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.ID3v1;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

///////////////////////////////////////////
///// Analysiere Audidateien /////////////
/////////////////////////////////////////

/**
 * Created by Inga on 24.11.2014.
 * Diese Klasse enthält Methoden zur Analyse der lokalen Audiodateien.
 */
public class LocalMusicFiles {

	Map<String, Integer> mapMaxApp = new HashMap<>();//Map: Artist - Häufigkeit
	Map<String, Integer> mapMaxGen = new HashMap<>();//Map Genre - Häufigkeit
	String favArtist = "";
	String favGenre = "";

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

			//Erfunden von Autoren von Winamp und 'backported' in ID3 spec.
			"Folk", "Folk-Rock", "National Folk", "Swing", "Fast Fusion",
			"Bebob", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde",
			"Gothic Rock", "Progressive Rock", "Psychedelic Rock", "Symphonic Rock",
			"Slow Rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour",
			"Speech", "Chanson", "Opera", "Chamber Music", "Sonata", "Symphony",
			"Booty Bass", "Primus", "Porn Groove", "Satire", "Slow Jam", "Club",
			"Tango", "Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul",
			"Freestyle", "Duet", "Punk Rock", "Drum Solo", "A capella", "Euro-House",
			"Dance Hall",

			//Erfunden von Winamp Leuten, aber ignoriert von den ID3 Autoren.
			"Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror", "Indie",
			"BritPop", "Negerpunk", "Polsk Punk", "Beat", "Christian Gangsta Rap",
			"Heavy Metal", "Black Metal", "Crossover", "Contemporary Christian",
			"Christian Rock", "Merengue", "Salsa", "Thrash Metal", "Anime", "Jpop",
			"Synthpop",

			//Von mir hinzugefügt
			"Pop-Rock"//, "Podcast"
	};

	public void setFavArtist(String setFavArtist){
		favArtist = setFavArtist;
	}

	public String getFavArtist(){
		return favArtist;
	}

	public void setFavGenre(String setFavGenre){
		favGenre = setFavGenre;
	}

	public String getFavGenre(){
		return favGenre;
	}

	public void FavArtist(String artist){
	int numberA = 0;
	int numberAmax = 0;
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
				numberA = mapMaxApp.get(artist); //Erhöhe Häufigkeit
				mapMaxApp.remove(artist);
				numberA++;
				mapMaxApp.put(artist, numberA);
				if(numberA > numberAmax){ //Überprüfe Favorit
					favArtist = artist;
					numberAmax = numberA;
				}
			}
		}

		/*//Da es bei der Ausgabe von UTF-8 in UTF-16 zu ungewollten Zeichen kommt, wird der
		//String hier konvertiert
			Charset utf8charset = Charset.forName("UTF-8");
			Charset utf16charset = Charset.forName("ISO-8859-1");

			ByteBuffer inputBuffer = ByteBuffer.wrap(new byte[]{(byte)0xC3, (byte)0xA2});

			// decode UTF-8
			CharBuffer data = utf8charset.decode(inputBuffer);

			// encode UTF-16
			ByteBuffer outputBuffer = utf16charset.encode(data);
			byte[] outputData = outputBuffer.array();

			favArtist = outputData.toString();*/

	setFavArtist(favArtist); //Setze den Lieblingskünstler für die Übergabe in Klasse Musik
	}

	public void FavGenre(String genre){
		int numberG = 0;
		int numberGmax = 0;

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
		setFavGenre(favGenre); //Setze den Lieblingsgenre für die Übergabe in Klasse Musik
	}

	/**
	 * Liest den ID3 Tag von gefundenen MP3- und FLAC-Dateien aus
	 * @param localFiles Liste der gefundenen MP3-Dateien
	 * @return void
	 * @remark benutzt Bibliothek "jid3lib-0.5.4.jar"
	 * @exception org.farng.mp3.TagException, FileNotFoundException,
	 * UnsupportedOperationException, IOException, Exception
	 */
	public void readId3Tag(List<Path> localFiles) {
		String genre = "";
		String artist = "";
		GuiManager.updateProgress("Wer ist wohl dein Lieblingskünstler und was hörst du für " +
				"Musik?");
		if (!(localFiles.isEmpty())){
			for (Path file : localFiles) {
				try {
					String fileLocation = file.toAbsolutePath().toString(); //Get path to file
					MP3File mp3file = new MP3File(fileLocation); //create new object from ID3tag-package

					if (mp3file.hasID3v2Tag()) {
						AbstractID3v2 tagv2 = mp3file.getID3v2Tag();

						//Analyse Lieblingskünstler
						artist = tagv2.getLeadArtist();
						if(!artist.equals("")) {
							FavArtist(artist);
						}
						//Analyse Lieblingsgenre
						genre = tagv2.getSongGenre();
						if(!genre.equals("")) {
							FavGenre(genre);
						}
					} else if (mp3file.hasID3v1Tag()) {
						ID3v1 tagv1 = mp3file.getID3v1Tag();

						//Analyse Lieblingskünstler
						artist = tagv1.getArtist();
						if(!artist.equals("")) {
							FavArtist(artist);
						}
						//Analyse Lieblingsgenre
						short gId = tagv1.getGenre(); //Get Genre ID
							try {
								genre = arrayGenre[gId]; // Map Genre-ID zu Genre-Name
							} catch (ArrayIndexOutOfBoundsException e) {
								// Die Genre-ID existiert nicht
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
			}
		}
	}
}
