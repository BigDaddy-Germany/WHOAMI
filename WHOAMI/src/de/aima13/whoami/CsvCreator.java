package de.aima13.whoami;

import java.util.List;
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
	private static final String FILE_NAME = "WHOAMI_Analyze_Results.csv";

	/**
	 * Starten der Speicherung
	 * @param representables Liste alle zu präsentierenden CSV Werte
	 * @throws Exception Ein Fehler ist aufgetreten
	 */
	public static void saveCsv(List<Representable> representables) throws Exception {
		SortedMap<String, String> csvValues = new TreeMap<>();

		// CSV Werte aus allen Representables ziehen
		for (Representable representable : representables) {
			csvValues.putAll(representable.getCsvContent());
		}

	}

	private static CSV_STATUS getCsvStatus(SortedMap<String, String> representables) {
		
		return CSV_STATUS.IS_PERFECT;
	}
}
