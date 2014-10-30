package de.aima13.whoami.modules.coding.languages;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;

/**
 * Created by Marco Dörfler on 29.10.14.
 */
public abstract class LanguageSetting {
	public final String LANGUAGE_NAME;
	public final String FILE_EXTENSION;
	public final Class<? extends Parser> PARSER;
	public final Class<? extends Lexer> LEXER;
	public final String START_SYMBOL;

	protected LanguageSetting(String language_name, String file_extension,
	                          Class<? extends Parser> parser, Class<? extends Lexer> lexer,
	                          String start_symbol) {
		LANGUAGE_NAME = language_name;
		FILE_EXTENSION = file_extension;
		PARSER = parser;
		LEXER = lexer;
		START_SYMBOL = start_symbol;
	}
}
