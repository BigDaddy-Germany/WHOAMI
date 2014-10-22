package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by D060469 on 16.10.14.
 */
public class SampleModule2 implements Analyzable {
	public SampleModule2() {
		System.out.println("Ich wurde konstruiert.");
	}

	@Override
	public List<String> getFilter() {
		System.out.println("Nun gebe ich meine Filtereinstellungen aus.");

		List<String> filter = new ArrayList<>();
		filter.add("**.css");

		return filter;
	}

	@Override
	public void setFileInputs(List<Path> files) throws Exception {
		System.out.println("Setze Dateien im Testmodul. Folgende Dateien gesetzt:");
		for (Path file : files) {
			System.out.println(" - " + file.getFileName());
		}
	}

	@Override
	public String getHtml() {
		return "Hallo. Ich bin das TestModul. Dies ist mein HTML-Text.";
	}

	@Override
	public SortedMap<String,String> getCsvContent() {
		SortedMap<String, String> csvContent = new TreeMap<>();
		csvContent.put("TestHead", "Test Value");

		return csvContent;
	}

	@Override
	public void run() {
		System.out.println("Ich laufe!");
	}
}
