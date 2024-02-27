package domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Experiment {
	public static int DEFAULT_COUNT = 1000;
	
	private Diagnoser diagnoser;
	private List<Boolean> infoPrevalences; //true if the information must prevail
	private int counter;
	private boolean allCombinations;

	public Experiment(Diagnoser diagnoser) {
		super();
		this.diagnoser = diagnoser;
		this.counter = DEFAULT_COUNT;
		this.allCombinations = true;
		this.infoPrevalences = new ArrayList<Boolean>();
	}

	public List<Boolean> getInfoPrevalences() {
		return infoPrevalences;
	}

	public void setInfoPrevalences(List<Boolean> infoPrevalences) {
		this.infoPrevalences = infoPrevalences;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}
	
	public boolean isAllCombinations() {
		return allCombinations;
	}

	public void setAllCombinations(boolean allCombinations) {
		this.allCombinations = allCombinations;
	}

	public void run() throws Exception {
		if (diagnoser.getStrategy().getName() == StrategyName.RANDOM) {
			int rounds = counter;
			if (counter == 0) {
				rounds = DEFAULT_COUNT;
			}
			for (int i = 0; i < rounds; i++) {
				diagnoser.runProbeSequencer();
			}
		}
		if (diagnoser.getStrategy().getName() == StrategyName.CHEAPEST) {
			runExperiment();
		}
		if (diagnoser.getStrategy().getName() == StrategyName.MEU) {
			runExperiment();
		}
	}

	private void runExperiment() throws Exception {
		if (diagnoser.getCostvariance().getType() == CostVariance.CostVarianceType.EQUAL) {
			for (int i = 0; i < counter; i++) {
				diagnoser.runProbeSequencer();
			}
		}
		if (diagnoser.getCostvariance().getType() == CostVariance.CostVarianceType.SCATTERED && allCombinations) {
			Collection<List<Probe>> combis = SetCombinations.getPermutationsWithoutRepeat(diagnoser.getProbes());
			for (List<Probe> combi: combis) {
				diagnoser.getCostvariance().setScatteredCosts(combi);
				diagnoser.runProbeSequencer();
			}
		}
		//if counter is given 
		if (diagnoser.getCostvariance().getType() == CostVariance.CostVarianceType.SCATTERED && !allCombinations) {
			for (int i = 0; i < counter; i++) {
				diagnoser.getCostvariance().setRandomScatteredCosts();
				diagnoser.runProbeSequencer();
			}
		}
		if (diagnoser.getCostvariance().getType() == CostVariance.CostVarianceType.POLAR  && allCombinations) {
			Collection<Pair<List<Probe>, List<Probe>>> combis = SetCombinations.divideInTwoSets(diagnoser.getProbes());
			for (Pair<List<Probe>, List<Probe>> combi: combis) {
				diagnoser.getCostvariance().setPolarCosts(combi.getFirst(), combi.getSecond());
				diagnoser.runProbeSequencer();
			}
		}
		if (diagnoser.getCostvariance().getType() == CostVariance.CostVarianceType.POLAR  && !allCombinations) {
			for (int i = 0; i < counter; i++) {
				diagnoser.getCostvariance().setRandomPolarCosts();
				diagnoser.runProbeSequencer();
			}
		}
	}
	
	public String getSettings() {
		String str = "";
		if (diagnoser.getUtilityfunction() != null && diagnoser.getUtilityfunction().getType() == UtilityNames.WEIGHTED_COST) {
			str += "InfoProvalences: ";
			for (int i = 0; i < infoPrevalences.size(); i++) {
				str += "[" + i + "] " + infoPrevalences.get(i) + ", ";
			}
			str = str.substring(0, str.length() - 2);
		}
		str += "\nallCombinations = " + allCombinations + "; ";
		str += "counter = " + counter;
		return str;
	}
}
