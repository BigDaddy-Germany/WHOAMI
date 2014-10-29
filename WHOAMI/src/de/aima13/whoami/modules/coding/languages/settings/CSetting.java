package de.aima13.whoami.modules.coding.languages.settings;

import de.aima13.whoami.modules.coding.languages.LanguageSetting;
import de.aima13.whoami.modules.coding.languages.antlrgen.CLexer;
import de.aima13.whoami.modules.coding.languages.antlrgen.CParser;

/**
 * Einstellungen für die Sprache C
 *
 * Created by Marco dörfler on 29.10.14.
 */
public class CSetting extends LanguageSetting {
	public CSetting() {
		super(
				"C",
				"c",
				CParser.class,
				CLexer.class
		);
	}
}
