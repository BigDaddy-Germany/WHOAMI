package de.aima13.whoami;

import java.util.Map;

/**
 * Created by D060469 on 16.10.14.
 */
public interface Representable {
	/**
	 * Teil des Reports in Form eines HTML-Schnipsels
	 * @return Der HTML Code
	 */
	public String getHtml();

	/**
	 * Teil der späteren CSV-Datei
	 * @return Key-Value Paare der Werte für die CSV-Datei
	 */
	public Map<String, String> getCsvContent();
}
