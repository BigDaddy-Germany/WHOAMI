package de.aima13.whoami.modules.gamesmod;

import de.aima13.whoami.support.Utilities;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Spieleliste hält einzigartige Einträge von Spielen, ggf. nach Installationszeitpunkt sortiert
 *
 * @author Niko Berkmann
 */
class GameList extends ArrayList<GameEntry> {
	/**
	 * Spiel nach In-Etwa-Duplikatscheck hinzufügen
	 *
	 * @param game Neues Spiel
	 */
	public void addUnique(GameEntry game) {
		//Falls Spiel schon enthalten, abbrechen und nichts hinzufügen
		boolean found = false;
		for(GameEntry compare: this) {
			if (Utilities.isRoughlyEqual(game.name, compare.name, 0.8f)) {
				return;
			}
		}

		//Vor dem Hinzufügen aufhüschen:
		// Falls Spieleordner in Kleinschreibung sind, Wortanfänge groß machen
		if (game.name.toLowerCase().equals(game.name)) {
			game.name = WordUtils.capitalize(game.name);
		}
		this.add(game);
	}

	/**
	 * Sortiert Spieleliste nach Installationsdatum
	 */
	public void sortByLatestCreated() {
		Collections.sort(this, new Comparator<GameEntry>() {
			@Override
			public int compare(GameEntry o1, GameEntry o2) {
				return o2.created.compareTo(o1.created);
			}
		});
	}

	/**
	 * Sortiert Spieleliste nach Veränderungsdatum
	 */
	public void sortByLatestModified() {
		Collections.sort(this, new Comparator<GameEntry>() {
			@Override
			public int compare(GameEntry o1, GameEntry o2) {
				return o2.modified.compareTo(o1.modified);
			}
		});
	}
}

