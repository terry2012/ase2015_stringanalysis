package edu.buffalo.cse.blueseal.networkflow.interprocedural;

import java.util.concurrent.TimeUnit;

public class SootTimer {

	private static long startTime;
	private static int ONE_SECOND = 1;
	private static int THIRTY_SECONDS = ONE_SECOND * 30;
	private static int ONE_MINUTE = THIRTY_SECONDS * 2;
	private static int TEN_MINUTES = ONE_MINUTE * 10;
	private static int ONE_HOUR = ONE_MINUTE * 60;
	
	public static void startTimer(){
		startTime = System.nanoTime();
	}

	public static boolean isSafeToProcess(){
		boolean safe = true;
		long currentTime = System.nanoTime();
		long elapsedTime = TimeUnit.NANOSECONDS.toSeconds(currentTime - startTime);
		if( elapsedTime> ONE_HOUR){
			safe = false;
		}
		return safe;
	}
	
	
	
	
}
