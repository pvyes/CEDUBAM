package domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.bayesserver.State;
import com.bayesserver.Variable;

public class ProbeCombinations extends SetCombinations {
	
	/*
	 * Makes a set collections of the states of the given variables, containing nrOfElements states per collection. 
	 * Of each variable at most one state is included in the collection.
	 */
	public static Collection<Collection<State>> getStateProducts(List<Variable> vars, int nrOfElements, List<State> excludedStates) {
		Collection<Collection<State>> result = new HashSet<Collection<State>>();
		List<List<State>> product = new ArrayList<List<State>>(); 
		List<List<State>> lists = new ArrayList<List<State>>();
		int i = 0;
		for (Variable var: vars) {
			List<State> coll = new ArrayList<State>();
			for (State s: var.getStates()) {
				if (excludedStates == null || !excludedStates.contains(s)) {
					coll.add(s);
				}
			}
			lists.add(i, coll);
			i++;
		}
		System.out.println("Lists: ");
		lists.forEach(l -> System.out.println(l));
		//make combinations				
		product = SetCombinations.choose(lists, nrOfElements);
		product.forEach(p -> result.add(p));
		return result;
	}
	
	/*
	 * Makes a set collections of the states of the given variables, containing nrOfElements states per collection. 
	 * Of each variable at most one state is included in the collection.
	 */
	private static Collection<Collection<State>> getStateProducts(Collection<Variable> vars, int nrOfElements) {
		Collection<Collection<State>> result = new HashSet<Collection<State>>();
		List<List<State>> product = new ArrayList<List<State>>(); 
		List<List<State>> lists = new ArrayList<List<State>>();
		int i = 0;
		for (Variable var: vars) {
			List<State> coll = new ArrayList<State>();
			for (State s: var.getStates()) {
				coll.add(s);
			}
			lists.add(i, coll);
			i++;
		}
		//System.out.println("Lists: ");
		//lists.forEach(l -> System.out.println(l));
		//make combinations				
		product = SetCombinations.choose(lists, nrOfElements);
		product.forEach(p -> result.add(p));
		return result;
	}
	

	//a scenario holds one possible flow of probes. The elements are particular lists of states 
	public static List<List<List<State>>> makeAllScenarios(List<Probe> probes, List<List<List<State>>> scenarios, boolean onlyfirstprobe, Diagnoser diagnoser) throws Exception {
		List<List<List<State>>> allscenarios = new ArrayList<List<List<State>>>();
		int max = probes.size();
		if (onlyfirstprobe) {
			max = 1;
		}		
		for (int i = 0; i < max; i++) {
			Probe p = probes.get(i);
			System.out.println("Computing with first probe = " + p + "\n");
			//set start of scenario
			List<List<State>> scenario = new ArrayList<List<State>>();
			for (State s: p.getTarget().getStates()) {
				List<State> lst = new ArrayList<State>();
				lst.add(s);
				scenario.add(lst);
			}
			scenarios.add(scenario);
			List<Probe> tmpprobes = new ArrayList<Probe>(probes);
			tmpprobes.remove(p);
			for (int j= 1; j < probes.size()-1; j++) {
				scenarios = makeScenarios(p, scenario, probes, scenarios);
			}
			for (List<List<State>> sc: scenarios) {
				List<List<List<State>>> newScenario = makeNewScenario(sc, probes, new ArrayList<List<List<State>>>());
				StrategyGivenScenario sf = (StrategyGivenScenario) diagnoser.getStrategy();
				sf.setSequenceScenario(newScenario.get(0));
				//System.out.println(newScenario.get(0));
				List<ProbeScenario> result = diagnoser.runProbeSequencer();
				for (ProbeScenario r: result) {
					MinimalCostCounter.setMincost(r);
				}
			}
			System.out.println("Result after first probe = " + p + ": minimal cost = " + MinimalCostCounter.getMin() + "for " + MinimalCostCounter.getNrOfMinProbescenarios() + " out of " + MinimalCostCounter.getNrOfscenarios() +  " scenarios.");
			System.out.println("Probescenario: \n" + MinimalCostCounter.getProbeScenario().makeMinimalReport());
		}
		//System.out.println("#scenarios = " + allscenarios.size());
		return allscenarios;
	}
	
	private static List<List<List<State>>> makeScenarios(Probe probe, List<List<State>> scenario, List<Probe> probes, List<List<List<State>>> scenarios) {
		List<List<List<State>>> allscenarios = new ArrayList<List<List<State>>>();
		List<List<List<State>>> tmpscenarios = new ArrayList<List<List<State>>>();
		tmpscenarios.addAll(scenarios);
		for (List<List<State>> scene: tmpscenarios) {
			allscenarios.addAll(makeNewScenario(scene, probes, scenarios));
		}
		return allscenarios;
	}
	
	private static List<List<List<State>>> makeNewScenario(List<List<State>> scenario, List<Probe> probes, List<List<List<State>>> scenarios) {
		List<List<List<State>>> allscenarios = new ArrayList<List<List<State>>>();
		//set start of scenario
		List<List<State>> result = null;
		scenarios.clear();
		boolean lastlist = false;
		for (int i = 0; i < scenario.size(); i++) {
			if (i == scenario.size() - 1) {
				lastlist = true;
			}
			List<State> list1 = scenario.get(i);
			List<List<List<State>>> tmpscenarios = new ArrayList<List<List<State>>>();
			List<Probe> usedProbes = getUsedProbes(probes, list1);
			List<Probe> restprobes = new ArrayList<Probe>(probes);
			restprobes.removeAll(usedProbes);
			int round = 0;
			for (Probe p: restprobes) {
				List<List<State>> newscenario = new ArrayList<List<State>>();
				List<State> list2 = p.getTarget().getStates();
				result = SetCombinations.addElementsOfsecondToFirst(list1, list2);
				newscenario = makeList(result);
				//first time (= first state) make a scenario for the first state
				if (i == 0) {
					tmpscenarios.add(round, new ArrayList<List<State>>());
					tmpscenarios.get(round).addAll(newscenario);
				} else {
					//add this scenarioelement to all existing ones and make newscenarios
					for (List<List<State>> scene: scenarios) {
						List<List<State>> newscene = new ArrayList<List<State>>();
						newscene.addAll(scene);
						newscene.addAll(newscenario);
						tmpscenarios.add(newscene);						
					}
				}
				round++;
			}			
			scenarios.clear();
			scenarios.addAll(tmpscenarios);
			if (lastlist) {
				allscenarios.addAll(scenarios);
			}
		}
		System.out.print(".");
		return allscenarios;
	}
	
	private static List<Probe> getUsedProbes(List<Probe> probes, List<State> scenarioElement) {
		List<Probe> usedprobes = new ArrayList<Probe>();
		for (Probe p: probes) {
			List<State> states = p.getTarget().getStates();
			for (State s: scenarioElement) {
				if (states.contains(s)) {
					usedprobes.add(p);
				}
			}
		}
		return usedprobes;
	}
	
	private static <T> List<List<T>> makeList(Collection<List<T>> collection) {
		List<List<T>> tmp = new ArrayList<List<T>>();
		for (Collection<T> el: collection) {
			List<T> list = new ArrayList<T>(el);
			tmp.add(list);
		}
		return tmp;
	}
}