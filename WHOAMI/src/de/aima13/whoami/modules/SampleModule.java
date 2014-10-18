package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by D060469 on 16.10.14.
 */
public class SampleModule implements Analyzable {
	public SampleModule() {
		System.out.println("Ich wurde konstruiert.");
	}

	@Override
	public List<String> getFilter() {
		System.out.println("Nun gebe ich meine Filtereinstellungen aus.");
		return null;
	}

	@Override
	public void setFileInputs(List<File> files) throws Exception {
		System.out.println("Setze Dateien im Testmodul. Folgende Dateien gesetzt:");
		for (File file : files) {
			System.out.println(" - " + file.getAbsolutePath());
		}
	}

	@Override
	public String getHtml() {
		return "Hallo. Ich bin das TestModul. Dies ist mein HTML-Text.";
	}

	@Override
	public Map<String, String> getCsvContent() {
		Map<String, String> csvContent = new HashMap<>();
		csvContent.put("TestHead", "Test Value");

		return csvContent;
	}

	@Override
	public void run() {
		System.out.println("Ich laufe!");
	}
}
