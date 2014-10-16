package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;

import java.io.File;
import java.util.List;
import java.util.SortedMap;

/**
 * Created by D060469 on 16.10.14.
 */
public class SampleModule implements Analyzable {
	@Override
	public List<String> getFilter() {

		return null;
	}

	@Override
	public void setFileInputs(List<File> files) throws Exception {

	}

	@Override
	public String getHtml() {

		return null;
	}

	@Override
	public SortedMap<String, String> getCsvContent() {

		return null;
	}

	@Override
	public void run() {

	}
}
