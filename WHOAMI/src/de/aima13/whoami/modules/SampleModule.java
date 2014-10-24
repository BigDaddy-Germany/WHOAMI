package de.aima13.whoami.modules;

import com.google.gson.Gson;
import de.aima13.whoami.Analyzable;
import de.aima13.whoami.Whoami;

import java.io.IOException;
import java.io.InputStream;
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
		return "<b>Hallo!</b> Ich bin das SampleModule.<br>Dies ist mein <i>HTML-Text</i>.";
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

		InputStream stream = Whoami.class.getResourceAsStream("/data/SampleModule_TestData.json");
		try {
			String input = org.apache.commons.io.IOUtils.toString(stream);

			Gson deserializer = new Gson();
			SampleData data = deserializer.fromJson(input, SampleData.class);

			System.out.println("Sampled: " + data.myText);
			System.out.println("Sampled: " + data.myNumber);
			System.out.println("Sampled: " + Arrays.toString(data.myArray));
			for (SamplePerson whoami : data.myList) {
				System.out.println("Sampled: Liste -> " + whoami.lastname + ", " + whoami.surname);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
