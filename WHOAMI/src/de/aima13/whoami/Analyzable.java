package de.aima13.whoami;

import java.io.File;
import java.util.List;

/**
 * Created by D060469 on 16.10.14.
 * Alle Module des Projektes implementieren dieses Interface
 */
public interface Analyzable extends Runnable, Representable {

	/**
<<<<<<< HEAD
	 * Liste der Dateien, für welche sich das Modul interessiert
	 * @return Alle Dateien, welche das Modul benötigt
=======
	 * Rückgabe der Filter für Dateien, welche das Modul benötigt
	 * @return Filtereinstellungen mit * als Platzhalter
>>>>>>> master
	 */
	public List<String> getFilter();

	/**
<<<<<<< HEAD
	 * Eingabe der für das Modul gefundenen Dateien
	 * @param files Liste der Dateien
=======
	 * Setzen der für das Modul gefundenen Dateien
	 * @param files Liste der gefundenen Dateien
>>>>>>> master
	 * @throws Exception Ein Fehler ist aufgetreten
	 */
	public void setFileInputs(List<File> files) throws Exception;
}
