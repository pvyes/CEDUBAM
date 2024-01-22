package domain;

public class STimer {
	private double starttime = 0;
	private double stoptime = 0;
		
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
	
	public int getElapsedMsTime() {
		return (int) (stoptime - starttime) / 1000000;
	}
	
	public void reset() {
		starttime = 0;
		stoptime = 0;
	}
}