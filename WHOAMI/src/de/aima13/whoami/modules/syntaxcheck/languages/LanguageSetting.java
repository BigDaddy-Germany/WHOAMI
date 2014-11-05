package de.aima13.whoami.modules.syntaxcheck.languages;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;

/**
 * Einstellung einer vom Whoami-Scanner unterstützten zu parsenden Programmiersprache bestehend
 * aus deren Name, Dateiendung, Parser und Lexer
 *
 * @author Marco Dörfler
 */
public abstract class LanguageSetting {
	public final String LANGUAGE_NAME;
	public final String FILE_EXTENSION;
	public final Class<? extends Parser> PARSER;
	public final Class<? extends Lexer> LEXER;
	public final String START_SYMBOL;

	/**
	 * Konstruktion nur durch Subklassen
	 * @param language_name Name der Sprache zur Repräsentation auf dem Bericht
	 * @param file_extension Dateiendung, welche diese Sprache repräsentiert
	 * @param parser Der zugehörige ANTLR Parser
	 * @param lexer Der zugehörige ANTLR Lexer
	 * @param start_symbol Der Name des Startsymbols der Sprache
	 */
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
