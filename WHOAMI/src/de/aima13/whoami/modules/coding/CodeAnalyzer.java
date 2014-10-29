package de.aima13.whoami.modules.coding;

import de.aima13.whoami.Analyzable;

import java.nio.file.Path;
import java.util.List;
import java.util.SortedMap;

/**
 * Created by Marco Dörfler on 28.10.14.
 */
public class CodeAnalyzer implements Analyzable {
	@Override
	public List<String> getFilter() {
		return null;
	}

	@Override
	public void setFileInputs(List<Path> files) throws Exception {

	}

	@Override
	public String getHtml() {
		return null;
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
		return null;
	}

	@Override
	public void run() {

	}
}
