package de.aima13.whoami.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Lose Sammlung statischer Hilfsfunktionen, die zu allgemein sind um in den Kontext anderer
 * Klassen zu passen und zu klein um eine eigene Klasse zu bilden
 */
public class Utilities {

	/**
	 * Lädt serialisiertes Klassenobjekt aus integrierter JSON-Ressourcendatei
	 *
	 * @param jsonPath    Pfad zur JSON Datei (mit führendem Slash relativ zum JAR-Root,
	 *                    also z.B. "/file.json" für eine Datei direkt im "res"-Ordner)
	 * @param classOfData Klasse der Daten
	 * @param <T>         Klasse der Daten
	 * @return Datenobjekt der angegebenen Klasse
	 *
	 * @author Niko Berkmann
	 */
	public static <T> T loadDataFromJson(String jsonPath, Class<T> classOfData) {
		String jsonText = null;

		try (InputStream stream = de.aima13.whoami.Whoami.class.getResourceAsStream(jsonPath)) {

			if (stream != null) {
				jsonText = org.apache.commons.io.IOUtils.toString(stream);
			} else {
				throw new IOException();
			}

		} catch (IOException e) {
			throw new RuntimeException("Ressourcenzugriff gescheitert: " + jsonPath);
		}

		try {

			com.google.gson.Gson deserializer = new com.google.gson.Gson();
			return deserializer.fromJson(jsonText, classOfData);

		} catch (com.google.gson.JsonSyntaxException e) {
			throw new RuntimeException("JSON fehlerhaft: " + jsonPath);
		}
	}

	/**
	 * Neuen Dateinamen suchen, der noch nicht vergeben ist
	 *
	 * @param backup soll backup als suffix genutzt werden?
	 * @return Der neue Dateiname oder im Misserfolg null
	 *
	 * @author Marco Dörfler
	 */
	public static String getNewFileName(String favoredName, boolean backup) {
		String currentName;

		String baseName = getFileBaseName(favoredName);
		String extension = getFileExtenstion(favoredName);

		if (backup) {
			baseName += ".backup";
		}

		currentName = baseName + "." + extension;

		int i = 0;
		Path newFile = Paths.get(currentName);
		while (Files.exists(newFile)) {
			i++;
			if (i == 1000) {
				// Harte Grenze bei 1000
				return null;
			}
			currentName = baseName + "." + i + "." + extension;
			newFile = Paths.get(currentName);
		}
		return currentName;
	}

	/**
	 * Neuen Dateinamen suchen, der noch nicht vergeben ist (Suffix backup)
	 * @return Der neue Dateiname oder im Misserfolg null
	 *
	 * @author Marco Dörfler
	 */
	public static String getNewFileName(String favoredName) {
		return getNewFileName(favoredName, true);
	}

	/**
	 * Dateiendung einer Datei berechnen
	 * @param fileName Name der Datei
	 * @return Die Endung der Datei (ohne Punkt)
	 *
	 * @author Marco Dörfler
	 */
	public static String getFileExtenstion(String fileName) {
		String extension = "";

		int indexDot = fileName.lastIndexOf('.');
		int indexSlash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

		if (indexDot > indexSlash) {
			extension = fileName.substring(indexDot+1);
		}

		return extension;
	}

	/**
	 * Berechnet den Basename einer Datei
	 * (Keine Ordnerstruktur mehr, keine Endung mehr
	 * @param fileName Der Name der Datei
	 * @return Der Basename der Datei
	 *
	 * @author Marco Dörfler
	 */
	public static String getFileBaseName(String fileName) {
		String baseName = "";

		int indexDot = fileName.lastIndexOf('.');
		int indexSlash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

		if (indexDot > indexSlash) {
			baseName = fileName.substring(indexSlash+1, indexDot);
		} else {
			baseName = fileName.substring(indexSlash+1);
		}

		return baseName;
	}


}
