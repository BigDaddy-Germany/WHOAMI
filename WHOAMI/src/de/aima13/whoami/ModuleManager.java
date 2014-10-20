package de.aima13.whoami;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by D060469 on 16.10.14.
 */
public class ModuleManager {

	static final String MODULE_SUBPACKAGE = "modules";
	static final ClassLoader CLASS_LOADER = Thread.currentThread().getContextClassLoader();


	/**
	 * Gibt eine Liste der Instanzen aller Module zurück
	 * @return Die Liste der Module
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IOException
	 */
	public static List<Analyzable> getModuleList() throws ClassNotFoundException,
			IllegalAccessException, InstantiationException, IOException {

		List<Analyzable> moduleList = new ArrayList<>();

		final String MODULE_PACKAGE = ModuleManager.class.getPackage().getName() + "." +
				MODULE_SUBPACKAGE;

		// Liste aller Klassen abfragen und iterieren
		final Class[] classes = getClasses(MODULE_PACKAGE);
		for (Class cl : classes) {
			Analyzable module = (Analyzable) cl.newInstance();
			moduleList.add(module);
		}

		return moduleList;
	}


	/**
	 * Scannen aller Klassen innerhalb eines Paketes
	 * @param packageName Der Name des Paketes
	 * @return Ein Array aller gefundenen Klassen
	 * @throws ClassNotFoundException
	 * @throws java.io.IOException
	 */
	private static Class[] getClasses(String packageName) throws ClassNotFoundException,
			IOException {
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = CLASS_LOADER.getResources(path);

		List<File> dirs = new ArrayList<>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile().replace("%20", " ")));
		}
		ArrayList<Class> classes = new ArrayList<>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes.toArray(new Class[classes.size()]);
	}

	/**
	 * Rekursiv alle Klassen aus dem Ordner und seinen Unterordner suchen
	 * @param directory   Der Ordner, in dem gesucht werden soll
	 * @param packageName Der Name des Paketes, dem die Klassen innerhalb des Ordners angehören
	 * @return Liste der Klassen
	 * @throws ClassNotFoundException
	 */
	private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class> classes = new ArrayList<Class>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			}
			else if (file.getName().endsWith(".class")) {
				classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}

}