package de.aima13.whoami.modules.coding;

import de.aima13.whoami.Analyzable;
import de.aima13.whoami.modules.coding.languages.LanguageSetting;
import org.reflections.Reflections;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

/**
 * Modul zum Analysieren des auf dem System gefundenen Codes
 *
 * Created by Marco Dörfler on 28.10.14.
 */
public class CodeAnalyzer implements Analyzable {
	private List<LanguageSetting> languageSettings;
	private List<String> moduleFilter;

	/**
	 * Im Konstruktor werden alle Settings geladen und instanziiert
	 *
	 * @author Marco Dörfler
	 */
	public CodeAnalyzer() {
		this.languageSettings = new ArrayList<>();

		Reflections reflections = new Reflections("de.aima13.whoami.modules.coding.languages" +
				".settings");

		Set<Class<? extends LanguageSetting>> settingClasses = reflections.getSubTypesOf
				(LanguageSetting.class);

		for (Class<? extends LanguageSetting> settingClass : settingClasses) {
			try {
				this.languageSettings.add(settingClass.newInstance());
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
			for (LanguageSetting setting : this.languageSettings) {
				this.moduleFilter.add("**." + setting.FILE_EXTENSION);
			}
		}

		return this.moduleFilter;
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
