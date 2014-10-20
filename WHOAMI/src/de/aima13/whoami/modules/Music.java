package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;

import java.io.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.farng.mp3.id3.AbstractID3;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.farng.mp3.id3.AbstractID3.*;


/**
 *
 */
public class Music implements Analyzable { //Analysable enthält runnable und representable


	List<File> localFiles = new ArrayList<File>();
	List<File> musicDatabases = new ArrayList<File>();
	List<File> browserFiles = new ArrayList<File>();

	@Override
	//Rückgabe einer Liste der vom Modul akzeptierten bzw. geforderten Dateitypen.
	//mit diesen Dateitypen kann das Modul arbeiten.
	public List<String> getFilter() {
		//a) Sammelt bisher nur MP3-Dateien (.FLAC, .RM, .acc, .ogg, .wav?)
		List<String> filterMusic = new ArrayList<String>();
		filterMusic.add("**.mp3");
		filterMusic.add("**.MP3");

		//b) Browser-history
		filterMusic.add("**Google/Profile/*/history");
		filterMusic.add("**Firefox**places.sqlite");
		return filterMusic;

		//c) windows (registry)?: spotify, wimp, naster... (?)
		//-> Keine Daten oder RegDaten?

	}


	@Override
	//Die Eingabe der für das Modul gefundenen Dateien durch den Scanner erfolgt über diese Methode
	public void setFileInputs(List<File> files) throws Exception {

		//Überprüfe ob überhaupt Daten gefunden wurden
		if (files != null && !files.isEmpty()) {
			musicDatabases = files;
		}
		else {
			throw new IllegalArgumentException("Keine Musikspuren gefunden");
		}

		//Spalte Dateien in BrowserFiles und lokale Dateien auf
		for(File element : musicDatabases){
			if(element.getName().contains(".mp3")){
				localFiles.add(element);
			}
			else {
				browserFiles.add(element);
			}
			}
		}



	}

	//Rückgabe des erzegten HTML-Codes zur Einbindung in den später erstellten Bericht
	@Override
	public String getHtml() {
		return null;
	}


	//Rückgabe der Key-Value Paare zur Einbindung in die spätere CSV-Datei
	@Override
	public SortedMap<String, String> getCsvContent() {
		return null;
	}

	@Override
	//Steuerung der Analyse der Musik
	public void run() {


	}

	public void scoreUser(){

	}

	//http://stackoverflow.com/questions/1645803/how-to-read-mp3-file-tags
	public void readId3Tag() {
		/**
		 * @param
		 */
			//String fileLocation = "G:/asas/album/song.mp3"
			/*for(File file : localFiles) {
					try {
						String fileLocation = file.getCanonicalPath();
						InputStream input = new FileInputStream(new File(fileLocation));
						ContentHandler handler = new DefaultHandler();
						AbstractID3 iD3 = new AbstractID3() {
						}
					} catch (IOException e) {
						e.printStackTrace();
					}


					file = (AbstractID3) file;
					file.getSongGenre();
				} catch(){ } */

					/*Metadata metadata = new Metadata();
					Mp3Parser parser = new Mp3Parser();
					ParseContext parseCtx = new ParseContext();
					parser.parse(input, handler, metadata, parseCtx);
					input.close();

					// List all metadata
					String[] metadataNames = metadata.names();

					for (String name : metadataNames) {
						System.out.println(name + ": " + metadata.get(name));
					}

					// Retrieve the necessary info from metadata
					// Names - title, xmpDM:artist etc. - mentioned below may differ based
					System.out.println("----------------------------------------------");
					System.out.println("Title: " + metadata.get("title"));
					System.out.println("Artists: " + metadata.get("xmpDM:artist"));
					System.out.println("Composer : " + metadata.get("xmpDM:composer"));
					System.out.println("Genre : " + metadata.get("xmpDM:genre"));
					System.out.println("Album : " + metadata.get("xmpDM:album"));

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (TikaException e) {
					e.printStackTrace();
				} */
			}

		}
	}


	public void checkNativeClients() {
		try
		{
			Process p=Runtime.getRuntime().exec("wmic product get name");
			p.waitFor();
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

	}

}
