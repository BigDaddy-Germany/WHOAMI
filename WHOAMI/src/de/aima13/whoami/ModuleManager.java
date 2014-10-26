package de.aima13.whoami;

import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

/**
 * Created by Marco Dörfler on 16.10.14.
 */
public class ModuleManager {

	static final String MODULE_PACKAGE = "de.aima13.whoami.modules";


	/**
	 * Gibt eine Liste der Instanzen aller Module zurück
	 * @return Die Liste der Module
	 *
	 * @author Marco Dörfler
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