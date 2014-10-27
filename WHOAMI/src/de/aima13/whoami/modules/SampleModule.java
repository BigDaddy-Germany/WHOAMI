package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.support.Utilities;

import java.nio.file.Path;
import java.util.*;

/**
 * Demo-Modul als Kopiervorlage und zum Testen
 */
public class SampleModule implements Analyzable {
	public SampleModule() {
		System.out.println("SampleModule wurde konstruiert.");
	}

	@Override
	public List<String> getFilter() {
		System.out.println("SampleModule verrät uns seinen Filter.");

		List<String> filter = new ArrayList<>();
		filter.add("**.txt");

		return filter;
	}

	@Override
	public void setFileInputs(List<Path> files) throws Exception {
		System.out.println("SampleModule bekommt " + files.size() + " Dateien.");
		if (files.size() > 0) {
			System.out.println("Eine davon ist: " + files.get(0).getFileName());
		}
	}

	@Override
	public String getHtml() {
		return "<b>Hallo!</b> Ich bin das SampleModule.<br />Dies ist mein <i>HTML-Text</i>.";
	}

	@Override
	public String getReportTitle() {
		return null;
	}

	@Override
	public String getCsvPrefix() {
		return null;
	}

	@Override
	public SortedMap<String, String> getCsvContent() {
		SortedMap<String, String> csvContent = new TreeMap<>();
		csvContent.put("SampleModuleHeader", "SampleModule test value");

		return csvContent;
	}

	public class SamplePerson {
		public String surname;
		public String lastname;
	}

	public class SampleData {
		public String myText;
		public Float myNumber;
		public int[] myArray;
		public List<SamplePerson> myList;
	}

	@Override
	public void run() {
		System.out.println("SampleModule wurde gestartet!");

		SampleData data = Utilities.loadDataFromJson("/data/SampleModule_TestData.json",
				SampleData.class);

		System.out.println("Sampled: " + data.myText);
		System.out.println("Sampled: " + data.myNumber);
		System.out.println("Sampled: " + Arrays.toString(data.myArray));
		for (SamplePerson whoami : data.myList) {
			System.out.println("Sampled: Liste -> " + whoami.lastname + ", " + whoami.surname);
		}
	}
}
