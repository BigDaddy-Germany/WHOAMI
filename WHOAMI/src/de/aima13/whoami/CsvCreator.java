package de.aima13.whoami;

import au.com.bytecode.opencsv.CSVReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

	private static final char CSV_SEPERATOR = ';';
	private static final char CSV_QUOTECHAR = '"';

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

		// Header als String Array exportieren
		CSV_STATUS csvStatus = getCsvStatus(
				completeCsvContent
						.keySet()
						.toArray(
								new String[
										completeCsvContent
												.keySet()
												.size()
										]
						)
		);

		System.out.println(csvStatus);

	}

	private static CSV_STATUS getCsvStatus(String[] moduleHeader) {
		try {
			CSVReader reader = new CSVReader(new FileReader(csvDatei), CSV_SEPERATOR, CSV_QUOTECHAR);

			// Wir gehen davon aus, dass die erste Zeile der Header ist
			String[] header = reader.readNext();
			reader.close();

			// zu Anfang gehen wir von einem korrekten Header aus, wenn die header die selbe
			// Größe haben
			boolean headerCorrect = (header.length == moduleHeader.length);

			// Jetzt werden alle Spalten geprüft, sofern die Anzahl die selbe ist
			if (headerCorrect) {
				for (int i = 0; i < header.length; i++) {
					if (!header[i].equals(moduleHeader[i])) {
						headerCorrect = false;
						break;
					}
				}
			}

			// entsprechende Rückgabe
			if (headerCorrect) {
				return CSV_STATUS.IS_PERFECT;
			} else {
				return CSV_STATUS.WRONG_FORMAT;
			}


		} catch (IOException e) {
			// Hier landen wir, wenn die Datei nicht existiert
			return CSV_STATUS.DOESNT_EXIST;
		}
	}
}
