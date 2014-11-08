package de.aima13.whoami;

import de.aima13.whoami.support.Utilities;
import org.stringtemplate.v4.ST;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Diese Klasse bietet allen Modulen an, globale Scores und persönliche Daten zu sammeln. Dies
 * ist nur zur Laufzeit der Module und nicht während der Auswertung der HTML- und CSV-Dateien
 * möglich
 *
 * @author Marco Dörfler
 */
public class GlobalData implements Representable {

	private final String REPORT_TITLE = "Persönliche Daten und Scores";
	private final String CSV_PREFIX = "global";
	private final String CSV_PREFIX_SCORE = "score";
	private final String CSV_PREFIX_DATA = "data";
	private final String TEMPLATE_LOCATION = "/data/GlobalData_Output.html";
	public final int MAX_SCORE_VALUE = 100;
	private boolean dataProposalsAllowed = true;

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

	@Override
	public String getHtml() {
		// Wenn Daten noch nicht berechnet wurden, erledige dies jetzt
		if (this.globalDataResults == null) {
			this.calculateDataResults();
		}


		// Template laden
		ST template = new ST(Utilities.getResourceAsString(TEMPLATE_LOCATION), '$', '$');

		// Liste der benötigten Headdaten
		String[] headData = new String[] {
				"firstName", "location", "street", "zipCode", "iban"
		};

		// Eventuell benötigte Headdaten hinzufügen
		for (String dataKey : headData) {
			if (this.globalDataResults.containsKey(dataKey)) {
				template.add(dataKey, this.globalDataResults.get(dataKey));
			} else {
				template.add(dataKey, false);
			}
		}

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
		return new String[0];
	}

	@Override
	public SortedMap<String, String> getCsvContent() {
		// Wenn Werte noch nicht kalkuliert wurden, berechne diese jetzt
		if (this.globalDataResults == null) {
			this.calculateDataResults();
		}

		SortedMap<String, String> csvContent = new TreeMap<>();
		for (Map.Entry<String, Integer> globalScore : this.globalScores.entrySet()) {
			csvContent.put(this.CSV_PREFIX_SCORE + "_" + globalScore.getKey(),
					globalScore.getValue().toString());
		}
		for (Map.Entry<String, String> globalData : this.globalDataResults.entrySet()) {
			csvContent.put(this.CSV_PREFIX_DATA + "_" + globalData.getKey(),
					globalData.getValue());
		}

		return csvContent;
	}


	/**
	 * Privater Konstruktor, da Singleton
	 */
	private GlobalData() {

	}

	/**
	 * Erlangen der Singletoninstanz der Klasse
	 * @return Instanz der Singleton Klasse
	 */
	public static GlobalData getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}



	/**
	 * Synchronized Methode zum Erstellen der Instanz
	 * So wird verhindert, dass es am Ende mehrere unterschiedliche Instanzen gibt
	 * Ausgelagert, da getInstance sonst langsamer wird.
	 */
	private static synchronized void createInstance() {
		if (instance == null) {
			instance = new GlobalData();
		}
	}



	/**
	 * Überladung der Methode proposeDate - Normalerweise wird ein einzelner Fund gemeldet
	 * @param key Key des Datensatzes (z.B. "Name")
	 * @param value Wert des Datensatzes (z.B. der Name des Nutzers)
	 */
	public synchronized void proposeData(String key, String value) {
		this.proposeData(key, value, 1);
	}


	/**
	 * Vorschlagen von persönlichen Daten
	 * @param key Key des Datensatzes (z.B. "Name")
	 * @param value Wert des Datensatzes (z.B. der Name des Nutzers)
	 * @param count Wie oft wurde der Wert gefunden?
	 */
	public synchronized void proposeData(String key, String value, int count) {
		if (!this.dataProposalsAllowed) {
			throw new RuntimeException("No data proposals allowed after calculating the results!");
		}

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
	 * Verändern eines globalen Scores
	 * @param key Key des Scores
	 * @param value Wert, um welchen erhöht oder erniedrigt werden soll
	 */
	public synchronized void changeScore(String key, int value) {
		if (!this.dataProposalsAllowed) {
			throw new RuntimeException("No score changes allowed after calculating the results!");
		}
		// Alle Scores werden mit der Hälfte des Maximums initialisiert
		if (!this.globalScores.containsKey(key)) {
			this.globalScores.put(key, MAX_SCORE_VALUE/2);
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
