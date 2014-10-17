package de.aima13.whoami;

import java.io.File;
import java.util.List;

/**
 * Created by D060469 on 16.10.14.
 */
public interface Analyzable extends Runnable, Representable {

	/**
	 * Liste der Dateien, für welche sich das Modul interessiert
	 * @return Alle Dateien, welche das Modul benötigt
	 */
	public List<String> getFilter();

	/**
	 * Eingabe der für das Modul gefundenen Dateien
	 * @param files Liste der Dateien
	 * @throws Exception Ein Fehler ist aufgetreten
	 */
	public void setFileInputs(List<File> files) throws Exception;
}
