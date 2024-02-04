package domain;

public class MinimalCostCounter {
	
	private static long nrOfscenarios;
	private static double min = Double.NaN;
	private static ProbeScenario probeScenario;
	private static long nrOfMinProbescenarios;
	
	public static void setMincost(ProbeScenario ps) {
		double ec = ps.getExpectedCost();
		nrOfscenarios++;
		if (Double.isNaN(min) || ec < min) {
			min = ec;
			probeScenario = ps;
			nrOfMinProbescenarios = 1;
		} else {
			if (ec == min) {
				nrOfMinProbescenarios++;
			}
		}
	}
	
	public static void clear() {
		nrOfscenarios = 0;
		min = Double.NaN;
		probeScenario = null;
		nrOfMinProbescenarios = 0;
	}
	
	public static long getNrOfscenarios() {
		return nrOfscenarios;
	}

	public static double getMin() {
		return min;
	}
	
	public static ProbeScenario getProbeScenario() {
		return probeScenario;
	}
	
	public static long getNrOfMinProbescenarios() {
		return nrOfMinProbescenarios;
	}
}
