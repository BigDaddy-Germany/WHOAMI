package de.aima13.whoami.modules.syntaxcheck.languages.settings;

import de.aima13.whoami.modules.syntaxcheck.languages.LanguageSetting;
import de.aima13.whoami.modules.syntaxcheck.languages.antlrgen.Java8Lexer;
import de.aima13.whoami.modules.syntaxcheck.languages.antlrgen.Java8Parser;

/**
 * Einstellungen für die Sprache Java (Version Java 8)
 *
 * @author Marco Dörfler
 */
public class Java8Setting extends LanguageSetting {
	/**
	 * Einstellungen der Sprache werden gesetzt
	 */
	public Java8Setting() {
		super(
				"Java",
				"java",
				Java8Parser.class,
				Java8Lexer.class,
				"compilationUnit"
		);
	}
}