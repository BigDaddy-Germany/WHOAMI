package de.aima13.whoami;

import org.apache.commons.lang3.text.WordUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by Marco Dörfler on 16.10.14.
 */
public class GlobalData implements Representable {

	private final String REPORT_TITLE = "Persönliche Daten und Scores";
	private final String CSV_PREFIX = "global";
	private final String CSV_PREFIX_SCORE = "score";
	private final String CSV_PREFIX_DATA = "data";
	private final int MAX_SCORE_VALUE = 100;

	// Zuordnung: Key - Value
	private Map<String, Integer> globalScores = new HashMap<>();

	/**
	 * Jedem Key können verschiedenen Vorschläge zugeordnet werden, wobei jedem Vorschlag
	 * die Häufigkeit zugeordnet werden kann.
	 * Sämtliche vorschläge werden in Großbuchstaben gespeichert und später Capitalized (erster
	 * Buchstabe groß) ausgegeben, sodass es grundsätzlich besser aussieht und es keine Probleme
	 * mit eventuell unterschiedlicher Groß-/Kleinschreibung gibt
	 */
	private Map<String, Map<String, Integer>> globalDataProposals = new HashMap<>();
	private Map<String, String> globalDataResults;

	/**
	 * @todo Global Data muss Thread-safe sein
	 */

	/**
	 * Instanz der Singleton-Klasse
	 */
	private static GlobalData instance;

	@Override
	public String getHtml() {
		// Wenn Daten noch nicht berechnet wurden, erledige dies jetzt
		if (this.globalDataResults == null) {
			this.calculateDataResults();
		}

		String html = "<b>Es wurden folgende Daten gefunden:</b><br />";
		html += "<table border=\"1\"><tr><th>Name</th><th>Wert</th></tr>";

		for (Map.Entry<String, Integer> score : this.globalScores.entrySet()) {
			html += "<tr><td>Score " + score.getKey() + "</td><td>" + score.getValue().toString()
					+ "</td></tr>";
		}
		for (Map.Entry<String, String> data : this.globalDataResults.entrySet()) {
			html += "<tr><td>" + data.getKey() + "</td><td>" + data.getValue() + "</td></tr>";
		}

		html += "</table>";

		return html;
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
	 *
	 * @author Marco Dörfler
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
	 *
	 * @author Marco Dörfler
	 */
	private static synchronized void createInstance() {
		if (instance == null) {
			instance = new GlobalData();
		}
	}


	/**
	 * Vorschlagen von persönlichen Daten
	 * @param key Key des Datensatzes (z.B. "Name")
	 * @param value Wert des Datensatzes (z.B. der Name des Nutzers)
	 *
	 * @author Marco Dörfler
	 */
	public synchronized void proposeData(String key, String value) {
		// Siehe Beschreibung oben - value to upper
		value = value.toUpperCase();

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
			valueProposals.put(value, valueProposals.get(value) + 1);
		} else {
			valueProposals.put(value, 1);
		}
	}

	/**
	 * Verändern eines globalen Scores
	 * @param key Key des Scores
	 * @param value Wert, um welchen erhöht oder erniedrigt werden soll
	 *
	 * @author Marco Dörfler
	 */
	public synchronized void changeScore(String key, int value) {
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
	 *
	 * @author Marco Dörfler
	 */
	private void calculateDataResults() {
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
				dataResults.put(key, WordUtils.capitalizeFully(bestProposal.getKey()));
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
