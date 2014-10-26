package de.aima13.whoami;

import java.nio.file.Path;
import java.util.List;

/**
 * Created by Marco Dörfler on 16.10.14.
 * Alle Module des Projektes implementieren dieses Interface
 */
public interface Analyzable extends Runnable, Representable {

	/**
	 * Filter für Dateien, welche das Modul benötigt
	 * @return Filtereinstellungen mit * als Platzhalter
	 *
	 * @author Marco Dörfler
	 */
	public List<String> getFilter();

	/**
	 * Setzen der für das Modul gefundenen Dateien
	 * @param files Liste der gefundenen Dateien
	 * @throws Exception Ein Fehler ist aufgetreten
	 *
	 * @author Marco Dörfler
	 */
	public void setFileInputs(List<Path> files) throws Exception;
}
