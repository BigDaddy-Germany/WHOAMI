package de.aima13.whoami.modules.coding;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.modules.coding.languages.LanguageSetting;
import de.aima13.whoami.support.Utilities;
import org.reflections.Reflections;

import java.nio.file.Path;
import java.util.*;

/**
 * Modul zum Analysieren des auf dem System gefundenen Codes
 *
 * Created by Marco Dörfler on 28.10.14.
 */
public class CodeAnalyzer implements Analyzable {
	private Map<LanguageSetting, List<Path>> languageFilesMap;
	private List<String> moduleFilter;

	/**
	 * Im Konstruktor werden alle Settings geladen und instanziiert
	 *
	 * @author Marco Dörfler
	 */
	public CodeAnalyzer() {
		this.languageFilesMap = new HashMap<>();

		Reflections reflections = new Reflections("de.aima13.whoami.modules.coding.languages" +
				".settings");

		Set<Class<? extends LanguageSetting>> settingClasses = reflections.getSubTypesOf
				(LanguageSetting.class);

		for (Class<? extends LanguageSetting> settingClass : settingClasses) {
			try {
				// Sprache wird mit leerer Dateiliste instanziiert
				this.languageFilesMap.put(settingClass.newInstance(), new ArrayList<Path>());
			} catch (InstantiationException | IllegalAccessException e) {
				// Code kann nicht analysiert werden
			}
		}

	}

	/**
	 * Filter bei Bedarf durch Iteration über die unterstützen Sprachen generieren
	 * und danach zurückgeben
	 *
	 * @return Liste der Filter
	 *
	 * @author Marco Dörfler
	 */
	@Override
	public List<String> getFilter() {
		// Filtereinstellungen werden nur bei Bedarf generiert, danach sollten sich diese
		// allterdings nicht mehr ändern, können also gespeichert werden
		if (this.moduleFilter == null) {
			this.moduleFilter = new ArrayList<>();

			// Da nur eine Endung pro Sprache unterstützt wird, reicht eine einfache Iteration
			// und Erstellen eines Patterns pro Endung
			for (LanguageSetting setting : this.languageFilesMap.keySet()) {
				this.moduleFilter.add("**." + setting.FILE_EXTENSION);
			}
		}

		return this.moduleFilter;
	}

	/**
	 * Erhaltene Dateien müssen den Programmiersprachen zugeordnet werden
	 *
	 * @param files Liste der gefundenen Dateien
	 *
	 * @author Marco Dörfler
	 */
	@Override
	public void setFileInputs(List<Path> files) {
		// Erstelle (nur für Zuordnung) Map: Extension -> List of Files
		Map<String, List<Path>> extensionFilesMap = new HashMap<>();
		for (Map.Entry<LanguageSetting, List<Path>> languageFilesEntry : this.languageFilesMap
				.entrySet()) {
			extensionFilesMap.put(languageFilesEntry.getKey().FILE_EXTENSION,
					languageFilesEntry.getValue());
		}

		// Iteriere über Dateien
		for (Path file : files) {
			// Versuche Liste der Dateien zu erreichen und füge Datei ein
			List<Path> fileList = extensionFilesMap.get(Utilities.getFileExtenstion(file
					.toString()));

			// Wenn entsprechende Liste gefunden wurde, sortiere die Datei ein.
			if (fileList != null) {
				fileList.add(file);
			}
		}
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
