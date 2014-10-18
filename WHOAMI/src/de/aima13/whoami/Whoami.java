package de.aima13.whoami;

/**
 * Created by D060469 on 16.10.14.
 */
public class Whoami {
	private static final int ANALYZE_TIME = 10; // Analysezeit in Sekunden
	private static long startTime;

	public static void main(String[] args) {
		startTime = System.nanoTime();
		
	}

	public static int getTimeProgress() {
		float elapsedTime = (float) ((System.nanoTime() - startTime) / 1000000000);
		int timeProgress = (int) (elapsedTime / ANALYZE_TIME * 100);

		if (timeProgress < 100) {
			return timeProgress;
		} else {
			return 100;
		}
	}
}
