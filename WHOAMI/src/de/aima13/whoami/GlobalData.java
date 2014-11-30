package de.aima13.whoami;

import de.aima13.whoami.support.Utilities;
import org.stringtemplate.v4.ST;

import java.util.*;

/**
 * Diese Klasse bietet allen Modulen an, globale Scores und persönliche Daten zu sammeln. Dies
 * ist nur zur Laufzeit der Module und nicht während der Auswertung der HTML- und CSV-Dateien
 * möglich
 *
 * @author Marco Dörfler
 */
public class GlobalData implements Representable {

	private final String REPORT_TITLE = "Persönliche Daten und Scores";
	private final String CSV_PREFIX = "GlobalData";
	private final String CSV_PREFIX_SCORE = "Score ";
	private final String CSV_PREFIX_DATA = "PersData  ";
	private final String TEMPLATE_LOCATION = "/data/GlobalData_Output.html";
	public final int MAX_SCORE_VALUE = 100;
	private boolean dataProposalsAllowed = true;
	private Config config;

	// Zuordnung: Key - Value
	private Map<String, Integer> globalScores = new HashMap<>();

	/*
	 * Jedem Key können verschiedenen Vorschläge zugeordnet werden, wobei jedem Vorschlag
	 * die Häufigkeit zugeordnet werden kann.
	 */
	private Map<String, Map<String, Integer>> globalDataProposals = new HashMap<>();
	private Map<String, String> globalDataResults;

	/*
	 * Instanz der Singleton-Klasse
	 */
	private static GlobalData instance;

	/**
	 * DataMapping stellt das Mapping der Templatevariablen auf die vorgeschlagenen Daten der
	 * GlobalData Klasse dar und wird jeweils durch zwei Strings dargestellt
	 *
	 * @author Marco Dörfler
	 */
	private class DataMapping {
		public String templateName;
		public String dataName;
	}

	/**
	 * Private Klasse zum Laden der Konfigurationen
	 *
	 * @author Marco Dörfler
	 */
	private class Config {
		public String[] allowedScores;
		public String[] allowedData;
		public DataMapping[] dataMapping;
	}

	@Override
	public String getHtml() {
		// Wenn Daten noch nicht berechnet wurden, erledige dies jetzt
		if (this.globalDataResults == null) {
			this.calculateDataResults();
		}


		// Template laden
		ST template = new ST(Utilities.getResourceAsString(TEMPLATE_LOCATION), '$', '$');

		/*
		Das Template benötigt im Header einige Variablen aus den Scores. Diese müssen vorher in
		der JSON Datei auf die entsprechenden Attribute der Daten gemapt werden
		Beispiel: Der Datensatz Straße wird auf die Templatevariable street gemapt
		 */
		// Durchgehen der einzelnen Mappings
		for (DataMapping dataMapping : this.config.dataMapping) {
			// Wenn ein Datensatz vorhanden ist, wird dieser gesetzt. Ansonsten wird false gesetzt
			if (this.globalDataResults.containsKey(dataMapping.dataName)) {
				template.add(dataMapping.templateName, this.globalDataResults.get(dataMapping
						.dataName));
			} else {
				template.add(dataMapping.templateName, false);
			}
		}

		template.add("maxScore", MAX_SCORE_VALUE);

		// Globale Informationen hinzufügen, wenn vorhanden
		if (!this.globalDataResults.isEmpty()) {
			template.add("hasInformation", true);

			// Daten vorhanden? -> füllen
			for (Map.Entry<String, String> dataResult : this.globalDataResults.entrySet()) {
				template.addAggr("information.{name, value}", dataResult.getKey(),
						dataResult.getValue());
			}
		} else {
			template.add("hasInformation", false);
		}

		// Scores hinzufügen, wenn vorhanden
		if (!this.globalScores.isEmpty()) {
			template.add("hasScores", true);

			// Scores vorhanden? -> füllen
			for (Map.Entry<String, Integer> score : this.globalScores.entrySet()) {
				template.addAggr("scores.{name, value}", score.getKey(), score.getValue());
			}
		} else {
			template.add("hasScores", false);
		}

		// Template rendern und zurückgeben
		return template.render();
	}

	@Override
	public String getReportTitle() {
		return this.REPORT_TITLE;
	}

	@Override
	public String getCsvPrefix() {
		return this.CSV_PREFIX;
	}

	@Override
	public String[] getCsvHeaders() {
		List<String> headerCols = new ArrayList<>();

		for (String dataName : this.config.allowedData) {
			headerCols.add(CSV_PREFIX_DATA + dataName);
		}

		for (String scoreName : this.config.allowedScores) {
			headerCols.add(CSV_PREFIX_SCORE + scoreName);
		}

		return headerCols.toArray(new String[headerCols.size()]);
	}

	@Override
	public SortedMap<String, String> getCsvContent() {
		// Wenn Werte noch nicht kalkuliert wurden, berechne diese jetzt
		if (this.globalDataResults == null) {
			this.calculateDataResults();
		}

		SortedMap<String, String> csvContent = new TreeMap<>();
		for (Map.Entry<String, Integer> globalScore : this.globalScores.entrySet()) {
			csvContent.put(this.CSV_PREFIX_SCORE + globalScore.getKey(),
					globalScore.getValue().toString());
		}
		for (Map.Entry<String, String> globalData : this.globalDataResults.entrySet()) {
			csvContent.put(this.CSV_PREFIX_DATA + globalData.getKey(),
					globalData.getValue());
		}

		return csvContent;
	}


	/**
	 * Privater Konstruktor, da Singleton
	 */
	private GlobalData() {
		// Lesen der JSON-Konfiguration
		this.config = Utilities.loadDataFromJson("/data/GlobalData_Config.json", Config.class);
	}


	private static class InstanceHolder {
		public static GlobalData instance = new GlobalData();
	}


	/**
	 * Erlangen der Singletoninstanz der Klasse
	 *
	 * @return Instanz der Singleton Klasse
	 */
	public static GlobalData getInstance() {
		return InstanceHolder.instance;
	}


	/**
	 * Vorschlagen von persönlichen Daten. Es können nur Daten vorgeschlagen werden,
	 * die in der <tt>GlobalData_Config.json</tt> als erlaubte Werte angemeldet wurden
	 *
	 * @param key   Key des Datensatzes (z.B. "Name")
	 * @param value Wert des Datensatzes (z.B. der Name des Nutzers)
	 * @throws java.lang.RuntimeException Es wurde ein Datensatz übergeben,
	 *                                    welcher nicht in der <tt>GlobalData_Config.json</tt> eingetragen ist oder die Laufzeit der
	 *                                    Module ist beendet und damit die Zeit zum Vorschlagen von Datensätzen vorbei.
	 */
	public synchronized void proposeData(String key, String value) {
		this.proposeData(key, value, 1);
	}


	/**
	 * Vorschlagen von persönlichen Daten. Es können nur Daten vorgeschlagen werden,
	 * die in der <tt>GlobalData_Config.json</tt> als erlaubte Werte angemeldet wurden
	 *
	 * @param key   Key des Datensatzes (z.B. "Name")
	 * @param value Wert des Datensatzes (z.B. der Name des Nutzers)
	 * @param count Wie oft wurde der Wert gefunden?
	 * @throws java.lang.RuntimeException Es wurde ein Datensatz übergeben,
	 *                                    welcher nicht in der <tt>GlobalData_Config.json</tt> eingetragen ist oder die Laufzeit der
	 *                                    Module ist beendet und damit die Zeit zum Vorschlagen von Datensätzen vorbei.
	 */
	public synchronized void proposeData(String key, String value, int count) {
		// Prüfen, ob Datenvorschläge aktuell erlaubt sind
		if (!this.dataProposalsAllowed) {
			throw new RuntimeException("No data proposals allowed after calculating the results!");
		}

		// Prüfe, ob dieser Datensatz vorgeschlagen werden darf
		if (!Arrays.asList(this.config.allowedData).contains(key)) {
			throw new RuntimeException("Proposal of not registered data.");
		}

		// Prüfen, ob für diesen Key Daten vorgeschlagen werden dürfen

		// Prüfen, ob der Datensatz für diesen Key schon existiert, ansonsten anlegen und mit
		// diesem Vorschlag initialisieren
		Map<String, Integer> valueProposals;
		if (this.globalDataProposals.containsKey(key)) {
			valueProposals = this.globalDataProposals.get(key);
		} else {
			valueProposals = new HashMap<>();
			this.globalDataProposals.put(key, valueProposals);
		}

		// Entscheide, ob dieser Wert für diesen Keyschonmal vorgeschlagen wurde
		if (valueProposals.containsKey(value)) {
			valueProposals.put(value, valueProposals.get(value) + count);
		} else {
			valueProposals.put(value, count);
		}
	}

	/**
	 * Verändern eines globalen Scores. Diese Methode erlaubt das Ändern eines globalen Scores,
	 * sofern dieser in der <tt>GlobalData_Config.json</tt> als erlaubt eingetragen wurde.
	 *
	 * @param key   Key des Scores
	 * @param value Wert, um welchen erhöht oder erniedrigt werden soll
	 * @throws java.lang.RuntimeException Ein Score wurde zu einer nicht erlaubten Zeit geändert
	 *                                    oder er ist nicht in der <tt>GlobalData_Config.json</tt> eingetragen
	 */
	public synchronized void changeScore(String key, int value) {
		if (!this.dataProposalsAllowed) {
			throw new RuntimeException("No score changes allowed after calculating the results!");
		}

		// Nur erlaubte Scores dürfen gesetzt werden 
		if (!Arrays.asList(this.config.allowedScores).contains(key)) {
			throw new RuntimeException("Changing of not registered score.");
		}

		// Alle Scores werden mit der Hälfte des Maximums initialisiert
		if (!this.globalScores.containsKey(key)) {
			this.globalScores.put(key, MAX_SCORE_VALUE / 2);
		}

		// Score darf zur Runtime auch über 100 oder unter 0 gehen,
		// sodass alle Vorschläge berücksichtigt werden
		int newScore = this.globalScores.get(key) + value;
		this.globalScores.put(key, newScore);
	}


	/**
	 * Kalkuliere den wahrscheinlich richtigen Wert aus allen Vorschlägen für alle
	 * vorgeschlagenen Werte und speichere die Ergebnisse
	 */
	private void calculateDataResults() {
		// Sobald diese Methode aufgerufen wurde, sind keine dataProposals mehr erlaubt
		this.dataProposalsAllowed = false;

		Map<String, String> dataResults = new HashMap<>();

		// Iteriere über alle Keys
		for (
				Map.Entry<String, Map<String, Integer>> dataSet
				: this.globalDataProposals.entrySet()
				) {
			String key = dataSet.getKey();
			Map<String, Integer> proposals = dataSet.getValue();

			// Sollte es mehrere Vorschläge mit der selben Anzahl geben,
			// wird einfach der erste genommen
			Map.Entry<String, Integer> bestProposal = null;
			for (Map.Entry<String, Integer> proposal : proposals.entrySet()) {
				// Ist der Wert besser als der bisher beste?
				if (bestProposal == null || bestProposal.getValue() < proposal.getValue()) {
					bestProposal = proposal;
				}
			}

			// Gab es ein Ergebnis? Dann Wert speichern
			if (bestProposal != null) {
				dataResults.put(key, bestProposal.getKey());
			}
		}

		this.globalDataResults = dataResults;


		// Sicherstellen, dass kein Score über MAX oder unter 0 liegt
		for (Map.Entry<String, Integer> score : this.globalScores.entrySet()) {
			if (score.getValue() < 0) {
				score.setValue(0);
			} else if (score.getValue() > this.MAX_SCORE_VALUE) {
				score.setValue(this.MAX_SCORE_VALUE);
			}
		}
	}
}
