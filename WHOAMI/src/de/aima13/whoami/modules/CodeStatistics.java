package de.aima13.whoami.modules;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.support.Utilities;

import java.nio.file.Path;
import java.util.List;
import java.util.SortedMap;

/**
 * Created by D060469 on 03.11.14.
 */
public class CodeStatistics implements Analyzable {

	private final FileExtension[] fileExtensions;
	private class FileExtension {
		public String ext;
		public String lang;
	}

	public CodeStatistics() {
		fileExtensions = Utilities.loadDataFromJson("/data/CodeStatistics_FileExtensions.json",
				FileExtension[].class);

		for (FileExtension fileExtension : fileExtensions) {
			System.out.println(fileExtension.ext + "->" + fileExtension.lang);
		}
		System.exit(0);
	}

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
