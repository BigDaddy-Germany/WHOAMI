package de.aima13.whoami;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marco Dörfler on 16.10.14.
 */
public class SlaveDriver {


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
				moduleThread.join();
			} catch (InterruptedException e) {
				// sollte eigentlich nicht passieren, da keine Zeit bei Join angegeben wurde
				e.printStackTrace();
			}
		}
	}
}