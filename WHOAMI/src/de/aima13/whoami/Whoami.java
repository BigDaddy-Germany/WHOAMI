package de.aima13.whoami;

/**
 * Created by D060469 on 16.10.14.
 */
public class Whoami {
	private static final int ANALYZE_TIME = 10; // Analysezeit in Sekunden
	private static long startTime;

	public static void main(String[] args) {
		startTime = System.currentTimeMillis();

	}

	/**
	 * Information über die bisherige und restliche Laufzeit des Programms
	 * @return Ganzzahliger Prozentwert zwischen 0 und 100 (100: Zeit ist um)
	 */
	public static int getTimeProgress() {
		float elapsedTime = (float) ((System.currentTimeMillis() - startTime) / 1000);
		int timeProgress = (int) (elapsedTime / ANALYZE_TIME * 100);

		if (timeProgress < 100) {
			return timeProgress;
		} else {
			return 100;
		}
	}
}
