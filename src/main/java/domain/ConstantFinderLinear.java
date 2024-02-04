package domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class ConstantFinderLinear implements ConstantFinder {
	private Diagnoser diagnoser;
	List<ProbeScenario> optimal = new ArrayList<ProbeScenario>();
	
	public ConstantFinderLinear(Diagnoser diagnoser) {
		this.diagnoser= diagnoser;
	}
	
	@Override
	public Collection<ProbeScenario> getOptimalScenarios(int iterationlimit, int timelimit) throws Exception {
		UtilityFunction utilityfunction = diagnoser.getUtilityfunction();
		return getOptimalWeightedCostScenarios(iterationlimit, timelimit, utilityfunction);
	}

	/*
	 * Returns a collection of the most optimal scenarios
	 */
	private Collection<ProbeScenario> getOptimalWeightedCostScenarios(int iterationlimit, int timelimit, UtilityFunction utilityfunction) throws Exception {
		UtilityLinear wcl = (UtilityLinear) diagnoser.getUtilityfunction();
		List<ProbeScenario> scenarios = new ArrayList<ProbeScenario>();
		Timer timer = new Timer();
		long elapsedTime = 0;
		//set constant s
		double highestcost = diagnoser.getHighestProbeCost();
		wcl.setS(highestcost);
		//set constant a = 0 and a = 1
		double firstA = 0;
		wcl.setA(firstA);
		ProbeScenario firstps = diagnoser.runProbeSequencer().get(0);
		scenarios.add(firstps);;
		double secondA = 1;
		wcl.setA(secondA);
		ProbeScenario secondps = diagnoser.runProbeSequencer().get(0);
		scenarios.add(secondps);
		double currentA = 0.5;
		wcl.setA(currentA);
		ProbeScenario currentps = diagnoser.runProbeSequencer().get(0);
		scenarios.add(currentps);
		Comparator<ProbeScenario> comp = new SortA_C();
		scenarios.sort(comp);
		//iterate
		for (int i = 0; i < iterationlimit; i++) {
			timer.start();
			if  (timelimit < 0 || elapsedTime / 1000 < timelimit) {
				double newA1 = (getA(scenarios.get(0)) + getA(scenarios.get(1))) / 2;
				double newA2 = (getA(scenarios.get(1)) + getA(scenarios.get(2))) / 2;
				wcl.setA(newA1);
				scenarios.add(diagnoser.runProbeSequencer().get(0));
				wcl.setA(newA2);
				scenarios.add(diagnoser.runProbeSequencer().get(0));
				scenarios.sort(comp);
				System.out.println("[");
				scenarios.forEach(sc -> System.out.println("a = " + getA(sc) + "; EC = "  + sc.getExpectedCost()));
				System.out.println("]");
				//Stop if first three habe equal expected costs
				if (scenarios.get(0).getExpectedCost() == scenarios.get(1).getExpectedCost() && scenarios.get(0).getExpectedCost() == scenarios.get(2).getExpectedCost()) {
					i = iterationlimit;
				}
			}
			timer.stop();
			elapsedTime += timer.getElapsedMsTime();
			timer.reset();
		}
		//choose the minimal expected cost
		List<ProbeScenario> optimal = new ArrayList<ProbeScenario>();
		optimal.add(scenarios.get(0));
		printOptimalScenarios(optimal);
		return optimal;
	}
	
	protected static Double getA(ProbeScenario ps) {
		return ps.getBranches().get(0).getMeuresult().getUtilityresults().get(0).getConstants().get(UtilityLinear.A);
	}
	
	public void printOptimalScenarios(List<ProbeScenario> optimal) throws Exception {
		for (ProbeScenario ps: optimal) {
			diagnoser.getReportManager().exportOptimal(ps);
		}
	}
}

class SortA_C implements Comparator<ProbeScenario> {
	@Override
	public int compare(ProbeScenario o1, ProbeScenario o2) {
		if (o1.getExpectedCost() == o2.getExpectedCost()) {
			return Double.compare(ConstantFinderLinear.getA(o1), ConstantFinderLinear.getA(o2)); 
		} else {
			return Double.compare(o1.getExpectedCost(), o2.getExpectedCost());
		}
	}
}
