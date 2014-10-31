package de.aima13.whoami.support;

/**
 * Created by Michi on 29.10.14.
 * Klasse dient lediglich zum speichern von Daten ähnlich eines Struct in C
 */
public class SqlSelectSaver {
	public String title;
	public String value;
	public int hitCount;

	public SqlSelectSaver(String mTitle){
		title =mTitle;
	}
}
