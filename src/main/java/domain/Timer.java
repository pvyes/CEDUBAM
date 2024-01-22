package domain;

public class Timer {
	private long starttime = 0;
	private long stoptime = 0;
		
	public double getStarttime() {
		return starttime;
	}

	public void start() {
		starttime = System.nanoTime();
	}

	public double getStoptime() {
		return stoptime;
	}

	public void stop() {
		stoptime = System.nanoTime();
	}

	public double getElapsedNanoTime() {
		return stoptime - starttime;
	}
	
	public long getElapsedMsTime() {
		return (stoptime - starttime) / 1000000;
	}
	
	public void reset() {
		starttime = 0;
		stoptime = 0;
	}
}