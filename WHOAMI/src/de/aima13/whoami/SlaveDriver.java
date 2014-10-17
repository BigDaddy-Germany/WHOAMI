package de.aima13.whoami;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by D060469 on 16.10.14.
 */
public class SlaveDriver {


	/**
	 * @param analyzables Liste der zu startenden Module
	 * @throws InterruptedException Ein oder mehrere Module konnten nicht komplett ausgeführt
	 * werden
	 */
	public static void startModules(List<Analyzable> analyzables) throws InterruptedException {
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
			moduleThread.join();
		}
	}
}