package de.aima13.whoami;

import java.nio.file.Path;
import java.util.List;

/**
 * Alle Module des Projektes implementieren dieses Interface
 *
 * @author Marco Dörfler
 */
public interface Analyzable extends Runnable, Representable {

	/**
	 * Filter für Dateien, welche das Modul benötigt
	 *
	 * @return Filtereinstellungen mit * als Platzhalter
	 */
	public List<String> getFilter();

	/**
	 * Setzen der für das Modul gefundenen Dateien
	 *
	 * @param files Liste der gefundenen Dateien
	 * @throws Exception Ein Fehler ist aufgetreten
	 */
	public void setFileInputs(List<Path> files) throws Exception;
}
