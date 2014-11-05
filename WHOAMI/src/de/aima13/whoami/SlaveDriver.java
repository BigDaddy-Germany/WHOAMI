package de.aima13.whoami;

import de.aima13.whoami.support.DataSourceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marco Dörfler on 16.10.14.
 */
public class SlaveDriver {

	private static final int MAX_TIME_OVERHEAD = 5;


	/**
	 * Module parallel als Threads starten
	 *
	 * @param analyzables Liste der zu startenden Module
	 *
	 * @author Marco Dörfler
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
			try {
				// Das Modul hat Zeit, bis die vorgesehene Programmlaufzeit abgelaufen ist
				long remainingMillis = Whoami.getRemainingMillis();
				System.out.println("Remaining: " + remainingMillis);
				if (remainingMillis > 0) {
					moduleThread.join(Whoami.getRemainingMillis());
				}

				// Wenn das Modul noch lebt, wird es abgeschossen, sobald die Laufzeit um eine
				// gewissen Zeit überschritten wurde
				if (remainingMillis > MAX_TIME_OVERHEAD * 1000 * -1) {
					Thread.sleep(MAX_TIME_OVERHEAD * 1000 - remainingMillis * -1);
				}
				if (moduleThread.isAlive()) {
					moduleThread.interrupt();
				}
			} catch (InterruptedException e) {
				// Niemand sollte diesen Thread interrupten
			}
		}
		DataSourceManager.closeRemainingOpenConnections();
	}
}