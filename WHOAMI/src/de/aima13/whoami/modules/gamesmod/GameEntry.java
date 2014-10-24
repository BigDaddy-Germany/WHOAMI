package de.aima13.whoami.modules.gamesmod;

import java.util.Date;

/**
 * Datenstruktur "Spiel"
 * Quasi ein struct.
 *
 * @author Niko Berkmann
 */
class GameEntry {
	public String name;
	public Date created;
	public Date modified;

	public GameEntry(String name, Date installed, Date modified) {
		this.name = name;
		this.created = installed;
		this.modified = modified;
	}
}
