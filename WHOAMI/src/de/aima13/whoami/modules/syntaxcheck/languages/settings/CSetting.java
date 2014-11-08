package de.aima13.whoami.modules.syntaxcheck.languages.settings;

import de.aima13.whoami.modules.syntaxcheck.languages.LanguageSetting;
import de.aima13.whoami.modules.syntaxcheck.languages.antlrgen.CLexer;
import de.aima13.whoami.modules.syntaxcheck.languages.antlrgen.CParser;

/**
 * Einstellungen für die Sprache C
 *
 * @author Marco Dörfler
 */
public class CSetting extends LanguageSetting {
	/**
	 * Einstellungen der Sprache werden gesetzt
	 */
	public CSetting() {
		super(
				"C",
				"c",
				CParser.class,
				CLexer.class,
				"compilationUnit"
		);
	}
}
