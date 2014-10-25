package de.aima13.whoami;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by D060469 on 16.10.14.
 */
public class ReportCreator {
	private List<Representable> representableList;

	public ReportCreator(List<Representable> representables) {
		this.representableList = representables;
	}

	public void savePdf() throws Exception {

	}

	public String getHtml() {

		return null;
	}

	private List<String> getHtmlSnippets() {
		List<String> htmlSnippets = new ArrayList<>();

		for (Representable representable : this.representableList) {
			htmlSnippets.add(representable.getHtml());
		}

		return htmlSnippets;
	}
}
