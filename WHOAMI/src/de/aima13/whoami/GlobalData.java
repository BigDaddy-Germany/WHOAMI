package de.aima13.whoami;

import java.util.SortedMap;

/**
 * Created by D060469 on 16.10.14.
 */
public class GlobalData implements Representable {

	/**
	 * @todo Global Data muss Thrad-safe sein
	 */

	@Override
	public String getHtml() {

		return null;
	}

	@Override
	public SortedMap<String, String> getCsvContent() {

		return null;
	}

	public void proposeData(String key, String value) {

	}

	public void changeScore(String key, int value) {

	}
}
