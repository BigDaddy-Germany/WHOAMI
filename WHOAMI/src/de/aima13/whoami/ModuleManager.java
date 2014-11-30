package de.aima13.whoami;

import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Zuständig für das Laden und instanziieren der Module
 *
 * @author Marco Dörfler
 */
public class ModuleManager {

	static final String MODULE_PACKAGE = "de.aima13.whoami.modules";


	/**
	 * Gibt eine Liste der Instanzen aller Module zurück
	 *
	 * @return Die Liste der Module
	 */
	public static List<Analyzable> getModuleList() {

		List<Analyzable> moduleList = new ArrayList<>();

		Reflections reflections = new Reflections("de.aima13.whoami.modules");

		Set<Class<? extends Analyzable>> moduleClasses = reflections.getSubTypesOf(Analyzable
				.class);

		for (Class<? extends Analyzable> moduleClass : moduleClasses) {
			try {
				moduleList.add(moduleClass.newInstance());
			} catch (IllegalAccessException e) {
				System.out.println("Could not load Module " + moduleClass.getSimpleName());
			} catch (InstantiationException e) {
				// don't use module
			}
		}

		return moduleList;
	}

}