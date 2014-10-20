package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;
import sun.java2d.cmm.Profile;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 *
 */
public class Music implements Analyzable { //Analysable enthält runnable und representable
	@Override
	//Rückgabe einer Liste der vom Modul akzeptierten bzw. geforderten Dateitypen.
	//mit diesen Dateitypen kann das Modul arbeiten.
	public List<String> getFilter() {

		return null;
	}

	@Override
	//Die Eingabe der für das Modul gefundenen Dateien durch den Scanner erfolgt über diese Methode
	public void setFileInputs(List<File> files) throws Exception {
		//files.add(** -> über Ordnergrenzen, * -> ?)
		//FilenameFilter?
		//FileNameExtensionFilter(.MP3);
		//files.add('**.MP3');
		//files.add('**Google/Profile/*/history');
		//setFileInputs(files);

		//Suche Dateitypen:

		/*
		a) lokal: .MP3, .FLAC, .RM, .acc, .ogg, .wav -> ID3 Tag ist doch nur bei MP3 oder?
		b) browser: youtube.com, spotify.com, myvideo.com...
		c) windows (registry)?: spotify, wimp, naster... (?)
		 */

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
	//Start der eigentlichen Analyse der Musik
	public void run() {

	}


	public void scoreUser(){

	}

	//http://stackoverflow.com/questions/1645803/how-to-read-mp3-file-tags
	public void readId3Tag() {
		/**
		 * @param args
		 */
		public static void main (String[]args){
			//String fileLocation = "G:/asas/album/song.mp3"
			foreach(file : files) {

				try {

					InputStream input = new FileInputStream(new File(fileLocation));
					ContentHandler handler = new DefaultHandler();
					Metadata metadata = new Metadata();
					Parser parser = new Mp3Parser();
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
				}
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
