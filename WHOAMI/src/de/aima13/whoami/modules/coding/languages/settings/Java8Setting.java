package de.aima13.whoami.modules.coding.languages.settings;

import de.aima13.whoami.modules.coding.languages.LanguageSetting;
import de.aima13.whoami.modules.coding.languages.antlrgen.Java8Lexer;
import de.aima13.whoami.modules.coding.languages.antlrgen.Java8Parser;

/**
 * Einstellungen für die Sprache Java (Version Java 8)
 *
 * Created by Marco Dörfler on 29.10.14.
 */
public class Java8Setting extends LanguageSetting {
	public Java8Setting() {
		super(
				"Java",
				"java",
				Java8Parser.class,
				Java8Lexer.class
		);
	}
}