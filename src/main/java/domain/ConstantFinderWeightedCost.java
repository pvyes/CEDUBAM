package domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ConstantFinderWeightedCost implements ConstantFinder {
	private Diagnoser diagnoser;
	
	public ConstantFinderWeightedCost(Diagnoser diagnoser) {
		this.diagnoser= diagnoser;
	}
	
	@Override
	public Collection<ProbeScenario> getOptimalScenarios(int iterationlimit, int timelimit) throws Exception {
		UtilityFunction utilityfunction = diagnoser.getUtilityfunction();
		return getOptimalWeightedCostScenarios(iterationlimit, timelimit, utilityfunction);
	}

	/*
	 * Returns a map of optimal alpha values with keys the index of the iteration (as a string) and value the alpha to use in this iteration.
	 */
	private Collection<ProbeScenario> getOptimalWeightedCostScenarios(int iterationlimit, int timelimit, UtilityFunction utilityfunction) throws Exception {
		List<List<ProbeScenario>> result = new ArrayList<List<ProbeScenario>>();
		UtilityWeightedCost wcu = (UtilityWeightedCost) diagnoser.getUtilityfunction();
		Timer timer = new Timer();
		long elapsedTime = 0;
		//make lists for infoprevalences
		List<Boolean> booleans = new ArrayList<Boolean>();
		booleans.add(true);
		booleans.add(false);
		Collection<List<Boolean>> infoprevalences = new ArrayList<List<Boolean>>();
		infoprevalences.add(new ArrayList<Boolean>());
		//for (int i = 1; i <= iterationlimit; i++) {
		infoprevalences.addAll(SetCombinations.makeCombinationsWithRepetion(booleans, iterationlimit));
		//}
		//compute probescenarios
		for (List<Boolean> ip: infoprevalences) {
			timer.start();
			//compute expected cost for each infoprevalence and assemble them. Track the values of alpha.
			if  (timelimit < 0 || elapsedTime / 1000 < timelimit) {
				wcu.setInfoprevalences(ip);
				result.add(diagnoser.runProbeSequencer());
			}
			timer.stop();
			elapsedTime += timer.getElapsedMsTime();
			timer.reset();
		}
		//choose the minimal expected cost
		List<ProbeScenario> mins = getMinExpectedCost(result);
		printOptimalScenarios(mins);
		return mins;
	}

	private List<ProbeScenario> getMinExpectedCost(List<List<ProbeScenario>> result) {
		List<ProbeScenario> pss = new ArrayList<ProbeScenario>();
		double lowest = Double.NaN;
		for (List<ProbeScenario> list: result) {
			for (ProbeScenario ps: list) {
				if (Double.isNaN(lowest)) {
					lowest = ps.getExpectedCost();
				}
				if (ps.getExpectedCost() == lowest) {
					pss.add(ps);
				}
				if (ps.getExpectedCost() <  lowest) {
					lowest = ps.getExpectedCost();
					pss.clear();
					pss.add(ps);
				}
			}
		}
		return pss;
	}
	
	public void printOptimalScenarios(List<ProbeScenario> optimal) throws Exception {
		diagnoser.getReportManager().exportOptimal(optimal.get(0));
	}
}
