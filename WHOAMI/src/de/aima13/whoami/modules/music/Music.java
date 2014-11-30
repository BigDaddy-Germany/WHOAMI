package de.aima13.whoami.modules.music;
import de.aima13.whoami.Analyzable;
import de.aima13.whoami.GlobalData;
import de.aima13.whoami.GuiManager;
import de.aima13.whoami.Whoami;
import java.nio.file.Path;
import java.util.*;
/**
 * Music, created 16.10.14.
 * Die Klasse Musik ist eine Art Hauptklasse des Musikmoduls.
 * Es implementiert die Methoden der Oberklassen wie run(), gethtml() etc. die für die Arbeit
 * Programms essentiell sind.
 *
 * @author Inga Miadowicz
 * @version 1.2
 */
public class Music implements Analyzable {
	//////////////////////////////////////////
	//// Deklarierung & Initialisierungen ///
	////////////////////////////////////////
	List<Path> musicDatabases = new ArrayList<>(); //Liste aller Dateien fürs Musikmodul
	List<Path> localFiles = new ArrayList<>(); //Liste von MP3-Dateien
	List<Path> browserFiles = new ArrayList<>(); //Liste der Browser-DB
	List<Path> exeFiles = new ArrayList<>(); //Liste der Musikprogramme
	public String favArtist = ""; //Ermittelter Lieblingskünstler aus LocalMusicFiles
	public String favGenre = ""; //Ermitteltes Lieblingsgenre aus LocalMusicFiles
	public String onlService = ""; //Genutzte Onlinedienste (siehe: MY_SEARCH_DELIVERY_URLS)
	public String cltProgram = ""; //Installierte Programme (siehe: MY_SEARCH_EXES)
	String stmtGenre = ""; //Kommentar zum Genre nach Kategorie
	String Qualität = ""; //Kommentar zur Existenz von FLAC-Dateien
	long nrAudio = 0; //Anzahl der Audiodateien
	private static final String[] MY_CSV_PREFIX = {"Lieblingskünstler", "Lieblingsgenre", "Onlineservices",
			"Musikprogramme", "Anzahl_Musikdateien"};
	private static final String TITLE = "Musikgeschmack";
	//////////////////////////////////////////
	//// überschriebene und Methoden ////////
	////////////////////////////////////////
	@Override
	/**
	 * Implementierung der Methode run() von Runnable. Hier wird die Reihenfolge der Analyse
	 * festgelegt. Zusätzlich wird ein Time-boxing implementiert,
	 * um ungewöhnlich lange Laufzweiten zu vermeiden.
	 * @return void
	 */
	public void run() {
		getFilter();
		if (Whoami.getTimeProgress() < 100) {
			GuiManager.updateProgress("Überprüfe lokale Musikdienste...");
			NativeMusicClients nativeClients = new NativeMusicClients();
			cltProgram = nativeClients.checkNativeClients(exeFiles);
		}
		if (Whoami.getTimeProgress() < 300) {
			GuiManager.updateProgress("Jetzt schaue ich mal auf welchen Musikwebsites du dich so " +
					"rumtreibst...");
			BrowserMusicFiles onlineServices = new BrowserMusicFiles();
			onlService = onlineServices.readBrowser(browserFiles);
		}
		if (Whoami.getTimeProgress() < 1000) {
			GuiManager.updateProgress("Lese ID3-Tags deiner lokalen Musikdateien...");
			LocalMusicFiles locals = new LocalMusicFiles();
			locals.readId3Tag(localFiles);
			favArtist = locals.getFavArtist();
			favGenre = locals.getFavGenre();
		}
	}
	@Override
	/** Legt den Filter für den FileSearcher fest
	 * @param
	 * @return filterMusic
	 */
	public List<String> getFilter() {
		List<String> filterMusic = new ArrayList<>();
		// lokale Audiodateien
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
	 * Ordnet musicDatabases für die Analyse des Musikgeschmacks, indem sie die vom FileSearcher
	 * übergebene Liste in 3 Unterlisten mit Daten für die Klassen LocalMusicFiles,
	 * NativeMusicClients und BrowserMusicFiles spaltet.
	 * @param files
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
		//Benutzername wird an Globaldata übergeben. Benötigt für Pfadangabe der Browserhistory
		String username = System.getProperty("user.name");
		GlobalData.getInstance().proposeData("Windows-Benutzername", username);
		//Spalte die Liste in drei Unterlisten:
		for (Path element : musicDatabases) {
			String path = element.toString();
			//Lokale Audiodateien, die aufgrund ihrer ID3-Tags analysiert werden können
			if (element.toString().contains(".mp3") || element.toString().contains(".MP3") ||
					element.toString().contains(".flac")) {
				localFiles.add(element);
				count++;
				//Kommentar zu ".flac-Dateien" abgeben
				if (element.toString().endsWith(".flac")) {
					Qualität = "Da du '.flac'- Dateien auf deinem PC hast, " +
							"Kompliment von unserer Seite: Du scheinst ein richtiger Audiofan " +
							"zu sein und legst wert auf die maximale Qualität deiner " +
							"Musiksammlung! ";
				}
				// Entferne Beispielmusik und Musik aus der Steambibliothek
				if (element.toString().contains("Steam") || element.toString().contains("Kalimba" +
						".mp3") || element.toString().contains("Sleep Away.mp3") || element.toString().contains("Maid with the Flaxen " +
						"Hair.mp3") || element.toString().contains("$RJLQJ56.mp3") || element
						.toString().contains("$IJLQJ56.mp3")) {
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
	@Override
	/**
	 * Das Ergebnis der Analyse wird in html als String in diesem Modul zusammengefügt
	 * @return String html
	 * @param
	 */
	public String getHtml() {
		MusicTextOutput musicText = new MusicTextOutput();
		if(!favGenre.equals("")){
			stmtGenre = musicText.getCategory(favGenre);
		}
		String html = musicText.html(cltProgram, onlService, favArtist, favGenre, nrAudio,
				Qualität);
		return html;
	}
	@Override
	/**
	 * Übergibt den Prefix ("Musikgeschmack") für den Output der PDF-Datei
	 * @return static final String TITLE
	 */
	public String getReportTitle() {
		return TITLE;
	}
	@Override
	/**
	 * Übergibt den Prefix ("Musikgeschmack") für den Output der CSV-Datei
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
		} else
			csvData.put("Lieblingskünstler", "-");
		if (!(favGenre.equals(""))) {
			csvData.put("Lieblingsgenre", favGenre);
		} else
			csvData.put("Lieblingsgenre", "-");
		if (!(onlService.equals(""))) {
			csvData.put("Onlineservices", onlService);
		} else
			csvData.put("Onlineservices", "-");
		if (!(cltProgram.equals(""))) {
			csvData.put("Musikprogramme", cltProgram);
		} else
			csvData.put("Musikprogramme", "-");
		if (nrAudio != 0) {
			String s = (new Long(nrAudio)).toString();
			csvData.put("Anzahl_Musikdateien", s);
		}
		return csvData;
	}
}