package de.aima13.whoami;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by D060469 on 16.10.14.
 * Erstellt eine CSV und speichert diese
 */
public class CsvCreator {
	private static enum CSV_STATUS {
		IS_PERFECT, WRONG_FORMAT, DOESNT_EXIST
	}
	private static final String FILE_NAME = "WHOAMI_Analyze_Results.csv"; // Name der CSV Datei
	private static final File csvDatei = new File(FILE_NAME); // File Datei der CSV Datei
	private static final String PREFIX_SEPERATOR = "_"; // Seperator zw. Modulname und Header

	/**
	 * Starten der Speicherung
	 * @param representables Liste alle zu präsentierenden CSV Werte
	 * @throws Exception Ein Fehler ist aufgetreten
	 */
	public static void saveCsv(List<Representable> representables) throws Exception {
		SortedMap<String, String> completeCsvContent = new TreeMap<>();

		System.out.println("\n\nStarting CsvCreator\n----------------\n");

		// CSV Werte aus allen Representables ziehen
		for (Representable representable : representables) {
			SortedMap<String, String> moduleCsvContent = representable.getCsvContent();

			if (moduleCsvContent != null) {
				// Header werden mit Prefix versehen -> keine Namensgleichheit
				String prefix = representable.getClass().getSimpleName();

				for (Map.Entry<String, String> moduleCsvCol : moduleCsvContent.entrySet()) {
					// Titel mit Prefix versehen und Spalte hinzufügen
					completeCsvContent.put(prefix + PREFIX_SEPERATOR + moduleCsvCol.getKey(),
							moduleCsvCol.getValue());

					System.out.println("Found Pair: (" + prefix + PREFIX_SEPERATOR +
							moduleCsvCol.getKey() + "|" +
							moduleCsvCol.getValue() + ")");
				}
			}
		}

		System.out.println((new File(".")).getAbsolutePath());

		getCsvStatus(completeCsvContent);

	}

	private static CSV_STATUS getCsvStatus(SortedMap<String, String> moduleCsvContent) {
		
		return CSV_STATUS.IS_PERFECT;
	}
}
