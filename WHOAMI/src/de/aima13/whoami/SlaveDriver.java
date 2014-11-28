package de.aima13.whoami;

import de.aima13.whoami.support.DataSourceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Diese Klasse ist für die Ausführung der Module zuständig
 *
 * @author Marco Dörfler
 */
public class SlaveDriver {

	private static final long MAX_TIME_OVERHEAD = 5000;


	/**
	 * Module parallel als Threads starten
	 *
	 * @param analyzables Liste der zu startenden Module
	 */
	public static void startModules(List<Analyzable> analyzables) {
		// Liste der erstellten Threads
		List<Thread> moduleThreads = new ArrayList<>();

		// Über alle Module iterieren und diese als Thread starten
		for (Analyzable module : analyzables) {
			Thread moduleThread = new Thread(module);
			moduleThread.start();
			moduleThreads.add(moduleThread);
		}

		// Warten auf alle Module
		for (Thread moduleThread : moduleThreads) {
			// Das Modul hat noch die geplante Analysezeit plus eine oben festgelegte Toleranz
			long remainingMillis = Whoami.getRemainingMillis() + MAX_TIME_OVERHEAD;

			// Gebe dem Modul noch die Zeit, die es verdient
			if (remainingMillis > 0) {
				try {
					moduleThread.join(remainingMillis);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// Spätestens jetzt muss es abgeschossen werden
			if (moduleThread.isAlive()) {
				moduleThread.stop();
			}
		}
	}
}