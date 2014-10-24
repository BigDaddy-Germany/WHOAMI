package de.aima13.whoami.support;

import java.io.IOException;
import java.io.InputStream;

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
}
