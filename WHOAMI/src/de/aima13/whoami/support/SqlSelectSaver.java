package de.aima13.whoami.support;

/**
 * Created by Michi on 29.10.14.
 * Klasse dient lediglich zum speichern von Daten ähnlich eines Struct in C
 * Dabei geht es in diesem speziellen Fall um Daten die aus der FormHistory bzw. Web Data
 * Datenbank der Browser FireFox bzw. Chrome stammen. Diese beinhalten ein Verlauf übder die
 * jenigen Daten die der Nutzer in sogenante Formularfelder eingibt.
 */
public class SqlSelectSaver {
	public String title;
	public String value;
	public int hitCount;

	public SqlSelectSaver(String mTitle){
		title =mTitle;
	}
	public SqlSelectSaver(String mTitle , String mValue,int mHitCount){
		title=mTitle;
		value=mValue;
		hitCount=mHitCount;
	}

}
