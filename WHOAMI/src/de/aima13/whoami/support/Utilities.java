package de.aima13.whoami.support;

import org.apache.commons.lang3.StringUtils;
import org.w3c.tidy.Configuration;
import org.w3c.tidy.Tidy;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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

		try {

			InputStream stream = de.aima13.whoami.Whoami.class.getResourceAsStream(jsonPath);
			java.util.Scanner scanner = new java.util.Scanner(stream).useDelimiter("\\A");
			jsonText = scanner.next();

		} catch (Exception e) {
			//catch-all, weil alle Fehler auf eine nicht vorhandene oder leere Datei
			// zurückzuführen sind, was es zur Development-Zeit zu beheben gilt
			e.printStackTrace();
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
	 *
	 * @return Der neue Dateiname oder im Misserfolg null
	 *
	 * @author Marco Dörfler
	 */
	public static String getNewFileName(String favoredName) {
		return getNewFileName(favoredName, true);
	}

	/**
	 * Dateiendung einer Datei berechnen
	 *
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
			extension = fileName.substring(indexDot + 1);
		}

		return extension;
	}

	/**
	 * Berechnet den Basename einer Datei
	 * (Keine Ordnerstruktur mehr, keine Endung mehr
	 *
	 * @param fileName Der Name der Datei
	 * @return Der Basename der Datei
	 *
	 * @author Marco Dörfler
	 */
	public static String getFileBaseName(String fileName) {
		String baseName;

		int indexDot = fileName.lastIndexOf('.');
		int indexSlash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

		if (indexDot > indexSlash) {
			baseName = fileName.substring(indexSlash + 1, indexDot);
		} else {
			baseName = fileName.substring(indexSlash + 1);
		}

		return baseName;
	}

	/**
	 * Vergleicht zwei Texte auf In-Etwa-Gleichheit auf Basis ihrer Levenshtein-Distanz
	 *
	 * @param compareText1         Erster Vergleichstext
	 * @param compareText2         Zweiter Vergleichstext
	 * @param ratioMinimumSameness Geforderte Ähnlichkeit zwischen 0 und 1
	 *                             Beispiel: 0.8 für 80%-ige Ähnlichkeit ist für die Strings
	 *                             "Hallo Welt!" <-> "Hallo Wald!" gerade noch erfüllt
	 *                             (20% von Maximaldistanz 11 sind 2.2 ~> 2 ~> passt!)
	 * @param optimizeInput        Vor dem Vergleich Strings mit optimizeForComparison() vorbehandeln
	 * @return Sind die Texte in etwa gleich?
	 *
	 * @author Niko Berkmann
	 */
	public static boolean isRoughlyEqual(String compareText1, String compareText2,
	                                     float ratioMinimumSameness, boolean optimizeInput) {
		int distanceLimit;
		int distance;

		if (optimizeInput) {
			compareText1 = optimizeForComparison(compareText1);
			compareText2 = optimizeForComparison(compareText2);
		}

		//Maximaler Abstand ist das längere Wort
		distanceLimit = Math.max(compareText1.length(), compareText2.length());

		//Setze Abbruchlimit auf Maximaldistanz*Fehlertoleranz (gerundet)
		distanceLimit = Math.round(distanceLimit * (1 - ratioMinimumSameness));

		//Distanz berechnen
		distance = org.apache.commons.lang3.StringUtils.getLevenshteinDistance(
				compareText1, compareText2, distanceLimit);

		//Texte sind in etwa gleich, wenn Limit nicht überschritten (getLevenstheinDistance() == -1)
		return (distance != -1);
	}

	/**
	 * Vergleicht zwei Texte auf In-Etwa-Gleichheit auf Basis ihrer Levenshtein-Distanz
	 * Beinhaltet Voraboptimierung durch optimizeForComparison()
	 *
	 * @param compareText1         Erster Vergleichstext
	 * @param compareText2         Zweiter Vergleichstext
	 * @param ratioMinimumSameness Geforderte Ähnlichkeit zwischen 0 und 1
	 *                             Beispiel: 0.8 für 80%-ige Ähnlichkeit ist für die Strings
	 *                             "Hallo Welt!" <-> "Hallo Wald!" gerade noch erfüllt
	 *                             (20% von Maximaldistanz 11 sind 2.2 ~> 2 ~> passt!)
	 * @return Sind die Texte in etwa gleich?
	 *
	 * @author Niko Berkmann
	 */
	public static boolean isRoughlyEqual(String compareText1, String compareText2,
	                                     float ratioMinimumSameness) {
		return isRoughlyEqual(compareText1, compareText2, ratioMinimumSameness, true);
	}

	/**
	 * Text für Vergleich optimieren: Reduktion auf Wörter, alles in Kleinschreibung,
	 * Sonderzeichen löschen, Zahlen gewichten, Umschreiben von Umlauten
	 *
	 * @param text Zu optimierender Text
	 * @return Für In-Etwa-Vergleich optimierter Text
	 */
	public static String optimizeForComparison(String text) {
		StringBuilder optimizedText = new StringBuilder();
		String wordSeparators = " _-+()[]{}&/`´'\"";
		Map<String, String> umlautConverter = new HashMap<String, String>();
		umlautConverter.put("ä", "ae");
		umlautConverter.put("ö", "oe");
		umlautConverter.put("ü", "ue");
		umlautConverter.put("ß", "ss");

		//Vereinheitlichen und Whitespace am Rand löschen
		text = text.trim().toLowerCase();

		String s;
		for (char c : text.toCharArray()) {
			s = Character.toString(c);

			//Worttrennzeichen auf Leerzeichen vereinheitlichen
			if (wordSeparators.contains(s)) {
				optimizedText.append(' ');

				//Zahlen mehr gewichten, weil sie bedeutender sind
			} else if (Character.isDigit(c)) {
				optimizedText.append(StringUtils.repeat(c, 5)); //Ziffern verfünffachen

				//Umlaute umschreiben
			} else if (umlautConverter.containsKey(s)) {
				optimizedText.append(umlautConverter.get(s));

				//Alle Buchstaben übernehmen...
			} else if (Character.isAlphabetic(c)) {
				optimizedText.append(c);
			} //...und alle anderen Zeichen wegwerfen
		}

		return optimizedText.toString();
	}


	/**
	 * Diese Methode nutzt den TidyParser, um HTML zu korrektem XHTML zu wandeln
	 * @param html Der HTML code
	 * @return Der generierte XHTML Code
	 *
	 * @author Marco Dörfler
	 */
	public static String convertHtmlToXhtml(String html) {
		// Fehlerausgaben unterdrücken
		PrintStream errStream = System.err;
		System.setErr(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				// Nichts geschieht....
			}
		}));

		Tidy tidy = new Tidy();
		tidy.setXHTML(true);
		tidy.setCharEncoding(Configuration.UTF8);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(html.getBytes());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		tidy.parseDOM(inputStream, outputStream);

		// Fehlerausgaben wieder zulassen
		System.setErr(errStream);

		return outputStream.toString();
	}
}
