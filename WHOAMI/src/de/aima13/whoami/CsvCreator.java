package de.aima13.whoami;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import de.aima13.whoami.support.Utilities;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Erstellt eine CSV und speichert diese
 *
 * @author Marco Dörfler
 */
public class CsvCreator {
	private static enum CSV_STATUS {
		IS_PERFECT, WRONG_FORMAT, DOESNT_EXIST
	}

	private static final String FILE_NAME = "WHOAMI_Analyze_Results.csv"; // Name der CSV Datei
	private static final File csvFile = new File(Whoami.OUTPUT_DIRECTORY
			+ FILE_NAME); // File Datei der CSV Datei
	private static final String PREFIX_SEPERATOR = " "; // Seperator zw. Modulname und Header

	private static final char CSV_SEPERATOR = ';'; // Separator der CSV Datei
	private static final char CSV_QUOTECHAR = '"'; // Feldbegrenzer der CSV Datei

	/**
	 * Starten der Speicherung
	 *
	 * @param representables Liste alle zu präsentierenden CSV Werte
	 */
	public static boolean saveCsv(Map<Representable, String[]> representables, String scanId) {
		SortedMap<String, String> completeCsvContent = new TreeMap<>();

		// Als erster Eintrag jeder Spalte sollte die ID des Scans stehen
		completeCsvContent.put("00_ScanId", scanId);

		// CSV Werte aus allen Representables ziehen
		for (Map.Entry<Representable, String[]> representableCsvEntry : representables.entrySet()) {
			Representable representable = representableCsvEntry.getKey();

			SortedMap<String, String> moduleCsvContent = representable.getCsvContent();

			if (moduleCsvContent != null) {
				// Header werden mit Prefix versehen -> keine Namensgleichheit
				String prefix;
				// Wenn das Modul keinen Prefix zurückgibt, wird der Klassenname genutzt
				if (representable.getCsvPrefix() == null) {
					prefix = representable.getClass().getSimpleName();
				} else {
					prefix = representable.getCsvPrefix() + PREFIX_SEPERATOR;
				}

				// Es sollen genau die vorgesehenen Spalten verwendet werden
				for (String csvColName : representableCsvEntry.getValue()) {
					String csvColValue;
					// Sollte das Modul hierzu keinen Eintrag haben, wird ein Platzhalter eingefügt
					if (moduleCsvContent.containsKey(csvColName)) {
						csvColValue = moduleCsvContent.get(csvColName);
					} else {
						csvColValue = "-";
					}
					completeCsvContent.put(
							prefix + csvColName,
							csvColValue
					);
				}
			}
		}

		// Header und Values als Stringarray exportieren
		String[] csvHeader = completeCsvContent
				.keySet()
				.toArray(
						new String[
								completeCsvContent
										.keySet()
										.size()
								]
				);

		String[] csvData = completeCsvContent
				.values()
				.toArray(
						new String[
								completeCsvContent
										.values()
										.size()]
				);


		// Status der CSV-Datei herausfinden
		CSV_STATUS csvStatus = getCsvStatus(csvHeader);

		Writer fileWriter;

		switch (csvStatus) {
			case DOESNT_EXIST:
				try {
					// Versuche neue Datei zu erstellen und darauf zu schreiben
					if (!csvFile.createNewFile()) {
						return false;
					}
					fileWriter = new OutputStreamWriter(new
							FileOutputStream(csvFile), StandardCharsets.UTF_8);
				} catch (IOException e) {
					return false;
				}
				break;
			case WRONG_FORMAT:
				// Nach neuem, nicht vergebenem Namen suchen
				String newFileName;
				if ((newFileName = Utilities.getNewFileName(Whoami.OUTPUT_DIRECTORY + FILE_NAME)) == null) {
					// Kein neuer nutzbarer Name gefunden
					return false;
				}

				// Vorhandene Datei umbenennen
				if (!csvFile.renameTo(new File(newFileName))) {
					// Wenn nicht erfolgreich, kann die Datei nicht geschrieben werden
					return false;
				}

				try {
					File newFile = new File(Whoami.OUTPUT_DIRECTORY + FILE_NAME);
					fileWriter = new OutputStreamWriter(new
							FileOutputStream(newFile), StandardCharsets.UTF_8);
				} catch (IOException e) {
					return false;
				}
				break;
			case IS_PERFECT:
				try {
					if (!csvFile.canWrite() && !csvFile.setWritable(true)) {
						// Datei kann nicht benutzt werden. Suche nach anderem Namen
						if ((newFileName = Utilities.getNewFileName(Whoami.OUTPUT_DIRECTORY + FILE_NAME)) == null) {
							// Kein neuer nutzbarer Name gefunden
							return false;
						}

						File newFile = new File(newFileName);
						fileWriter = new OutputStreamWriter(new
								FileOutputStream(newFile), StandardCharsets.UTF_8);

					} else {
						// Datei ist schreibbar
						fileWriter = new OutputStreamWriter(new
								FileOutputStream(csvFile, true), StandardCharsets.UTF_8);
					}
				} catch (IOException e) {
					return false;
				}
				break;

			default:
				return false;
		}

		// Neuen Writer für CSV Datei erstellen (sollte klappen)
		CSVWriter writer = new CSVWriter(fileWriter, CSV_SEPERATOR, CSV_QUOTECHAR);

		if (csvStatus != CSV_STATUS.IS_PERFECT) {
			// Header muss neu geschrieben werden
			writer.writeNext(csvHeader);
		}
		writer.writeNext(csvData);

		try {
			writer.close();
		} catch (IOException e) {
			return false;
		}


		// wenn wir hier gelandet sind, sollte es geklappt haben.
		return true;
	}


	/**
	 * Untersuchen auf eventuell bereits vorhandener CSV-Dateien
	 *
	 * @param moduleHeader String-Array des aktuellen Headers
	 * @return enum zur Statusunterscheidung
	 */
	private static CSV_STATUS getCsvStatus(String[] moduleHeader) {
		try {
			CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(csvFile),
					StandardCharsets.UTF_8), CSV_SEPERATOR, CSV_QUOTECHAR);

			// Wir gehen davon aus, dass die erste Zeile der Header ist
			String[] header = reader.readNext();
			reader.close();

			if (header == null) {
				// Leere Datei
				return CSV_STATUS.WRONG_FORMAT;
			}

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
