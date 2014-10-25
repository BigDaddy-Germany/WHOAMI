/* STILL TO DO
a) lok. Dateien:
-> ArrayList und HashMap zusammen fassen
-> Unsere Kategorien?


 */



package de.aima13.whoami.modules;

import com.sun.deploy.util.StringUtils;
import com.sun.org.apache.xalan.internal.xsltc.runtime.*;
import de.aima13.whoami.Analyzable;

import java.io.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentMap;

import javafx.beans.property.MapProperty;
import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.AbstractID3;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.ID3v1;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.farng.mp3.id3.AbstractID3.*;

/**
 * favourite Music
 *
 *
 */

public class Music implements Analyzable {

	List<Path> musicDatabases = new ArrayList<>(); //List I get from FileSearcher
	List<Path> localFiles = new ArrayList<>(); //List of MP3-files from musicDatabase
	List<Path> browserFiles = new ArrayList<>(); //List of browser-entries from musicDatabase
	List<Path> exeFiles = new ArrayList<>(); //List of browser-entries from musicDatabase
	ArrayList<String> FileArtist = new ArrayList<>(); //List of Artists
	ArrayList<String> FileGenre = new ArrayList<>(); //List of Genres
	Map<String, Integer> mapMaxApp = new HashMap<>();//Map Artist - frequency of this artist
	Map<String, Integer> mapMaxGen = new HashMap<>();//Map Genre - frequency of this genre

	String favArtist = ""; //favourite Artist
	String favGenre="";    //favourite Genre

	String[] arrayGenre =  {	// Position in array is Byte of id3Tag for Genre:
								// GenreID is 3: Genre to ID is "Dance"

			//These are the official ID3v1 genres.
			"Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge",
			"Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B", "Rap",
			"Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska",
			"Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient",
			"Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical",
			"Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel", "Noise",
			"AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative",
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
	/* Filter for FileSearcher
	*  Set the type of file the module needs
	*  @param
	*  @return filterMusic
	*/
	public List<String> getFilter() {
		//a) local MP3-files. LATER ADD(.FLAC, .RM, .acc, .ogg, .wav?)
		List<String> filterMusic = new ArrayList<>();
		filterMusic.add("**.mp3");
		filterMusic.add("**.MP3");
		filterMusic.add("**.mP3");
		filterMusic.add("**.Mp3");

		//b) Browser-history
		filterMusic.add("**Google/Profile/*/history");
		filterMusic.add("**Firefox**places.sqlite");

		//c) windows (registry)?: spotify, wimp, naster... (?)
		filterMusic.add("**spotify.exe");
		filterMusic.add("**Itunes.exe");

		//simfy
		//napster
		//rdio
		//Deezer
		//juke

		return filterMusic;
	}

	@Override
	/*
	* Sort list of file by
	  * a) look up if it is empty
	  * b) split files to local and browser files
	* @param List<File> files
	* @return void
	 */
	public void setFileInputs(List<Path> files) throws Exception {

		//if data is found add it to musicDatabases
		if (files != null && !files.isEmpty()) {
			musicDatabases = files;
		} else {
			throw new IllegalArgumentException("Keine Musikspuren gefunden");
		}

		//Split List into local and BrowserStuff
		for (Path element : musicDatabases) {
			if (element.toAbsolutePath().endsWith("" + ".mp3")) {
				localFiles.add(element);
			} 	else if (element.toAbsolutePath().endsWith("" + ".exe")) {
				exeFiles.add(element);
			} else {
				browserFiles.add(element);
			}
		}
	}

	//Rückgabe des erzeugten HTML-Codes zur Einbindung in den später erstellten Bericht???
	@Override
	/*
	* @return String
	* @param
	 */
	public String getHtml() {
		return "Hallo. Ich bin irgendein HTML-Text.";
	}


	//Rückgabe der Key-Value Paare zur Einbindung in die spätere CSV-Datei
	@Override
	/*
	* @return csvContent
	* @param
	 */
	public SortedMap<String, String> getCsvContent() {
		SortedMap<String, String> csvContent = new TreeMap<>();
		csvContent.put("TestHead", "Test Value");
		return csvContent;
	}

	@Override
	/* abstract method of Runnable
	* @return void
	* @param
	 */
	public void run() {
		getFilter();
		readId3Tag(localFiles);
		//checkNativeClients(exeFiles);
	}

	/*
	Guess favourite Genre
	@return String favGenre
	@param ArrayList<String> fileArtist
	 */
	public void scoreFavGenre(ArrayList<String> FileGenre){
		int max=0; //highest frequency of a genre
		int count; //count frequency of actual genre
		String favGenre = "";

		FileGenre.removeAll(Arrays.asList("", null)); //delete empty entries
		Collections.sort(FileGenre);//sort list alphabetically


		//hashes number of existance to genre
		for (String each : FileGenre) {
			count = 0;
			if(mapMaxGen.containsKey(each)) {
				count = mapMaxGen.get(each);
				mapMaxGen.remove(each);
			}
			count++;
			mapMaxGen.put(each, count);
		}

		//find Genre which has the highest key
		Iterator it = mapMaxGen.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			if((int)(pairs.getValue()) > max){
				favGenre = (String) pairs.getKey();
				max = (int)(pairs.getValue());
			}
			it.remove();
		}

		String id = "";
		boolean containsDigit = false;

		String str = "";
		str = favGenre.replaceAll("\\D+","");
		byte a = Byte.parseByte(str);
		favGenre = (String) arrayGenre[a];
		System.out.println("Your favourite genre is: " + favGenre);
	}


	public void scoreFavArtist(ArrayList<String> FileArtist) {

		int count = 0; //counts frequency of artist
		String favArtist = ""; //saves artist with highest frequency
		int max=0; //highest frequency

		FileArtist.removeAll(Arrays.asList("", null)); //delete empty entries
		Collections.sort(FileArtist); //sort list alphabetically

		//hashes frequency to artist
		for (String each : FileArtist) {
			count = 0;
			if(mapMaxApp.containsKey(each)) {
				count = mapMaxApp.get(each);
				mapMaxApp.remove(each);
			}
			count++;
			mapMaxApp.put(each, count);
		}

		//Find artist with highest frequency
		Iterator it = mapMaxApp.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			if((int)(pairs.getValue()) > max){
				favArtist = (String) pairs.getKey();
				max = (int)(pairs.getValue());
			}
			it.remove();
		}
		System.out.println("Your favourite artist is: " + favArtist);
	}

	//uses library "jid3lib-0.5.5.jar" to read the ID3-Tag of a MP3-File
	public void readId3Tag(List<Path> localFiles) {
		/**
		 * @param ArrayList<File> localFiles
		 * @return void
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
					} catch (ArrayIndexOutOfBoundsException e) {
						System.out.println("This Genre doesn't exist");
					}

					FileGenre.add(genre); //Fill List of Type String with genre

				} else {
					System.out.println("Found audiofile without ID3-Tag");
				}
			} catch (TagException e) {
				e.printStackTrace();
			}catch(FileNotFoundException e){
			} catch (IOException e) {
				e.printStackTrace();
			}catch(UnsupportedOperationException e){
				//e.printStackTrace();
				//System.out.println("Unsupported Operation Exception");
			}  catch (Exception e){
				System.out.println("general Exception in readId3Tag");
				e.printStackTrace();
			}

		}

		scoreFavArtist(FileArtist); //Call functions to find favArtist
		scoreFavGenre(FileGenre);   //Call functions to find favGenre
	}

	public void checkNativeClients(List exeFile) {

		String clients[] = new String[10];

		//Sammle .exe-Dateien über cmd
		/*try
		{
			//Process p=Runtime.getRuntime().exec("wmic product get name");
			//p.waitFor();
			BufferedReader reader=new BufferedReader(
					new InputStreamReader(p.getInputStream())
			);
			String line;
			while((line = reader.readLine()) != null)
			{
				System.out.println(line);
			}

		}
		catch(IOException e1) {}
		catch(InterruptedException e2) {}

		System.out.println("Done");
	}


	public void readBrowser(){
*/
	/*Vorrausgesetzt ich kriege die .exe-Dateien
		for(File currentExe : exeFiles){
			int count = 0;
			if(exeFiles.contains("spotify.exe")){
				System.out.println("Spotify is installed.");
				clients[count] = "Spotify";
			}
		}




		System.out.println("Module found " + clients);

	}*/

	}
}