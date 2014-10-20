package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;

import java.io.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;

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
		//FilenameFilter?

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
