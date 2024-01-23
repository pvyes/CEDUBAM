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
	private static Collection<Collection<State>> getStateProducts(Collection<Variable> vars, int nrOfElements, Collection<Collection<State>> pds) {
		Collection<Collection<State>> result = new HashSet<Collection<State>>();
		Collection<Collection<State>> products = getStateProducts(vars, nrOfElements);
		for (Collection<State> resultlist: products) {
			result.add(resultlist);
			for (Collection<State> excList: pds) {
				if (resultlist.containsAll(excList)) {
					result.remove(resultlist);
				}
			}
		}
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
	private static List<List<List<State>>> makeAllScenarios(List<Probe> probes, List<List<List<State>>> scenarios, boolean onlyfirstprobe) {
		List<List<List<State>>> allscenarios = new ArrayList<List<List<State>>>();
		int max = probes.size();
		if (onlyfirstprobe) {
			max = 1;
		}		
		for (var i = 0; i < max; i++) {
			Probe p = probes.get(i);
			System.out.println("Probe = " + p);
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
			for (var j= 1; j < probes.size(); j++) {
				//scenarios = makeScenarios(p, scenario, probes, scenarios);
				System.out.println("i=" + i + "; #scenarios = " + scenarios.size());
			}
			allscenarios.addAll(scenarios);
		}
		return allscenarios;
	}
	
	//TODO When choosing the probe?
/*	private static void makeScenario(Network system, Collection<Variable> probes, Collection<Collection<State>> pds, Map<Variable, Map<State, Double>> baseEvidenceMap, ProbeScenarioSimplified allScenarios, ProbeSequenceSimplified oldseq) {
		ProbeScenario result = new ProbeScenario();
		if (oldseq == null) {
			oldseq = new ProbeSequenceSimplified(system, probes, pds, baseEvidenceMap);
		}
		result.getBranches().addAll(makeNewSequences(oldseq).getBranches());
		
		List<ProbeSequenceSimplified> newSequences = result.getUnfinishedSequences();
		if (newSequences.size() > 0) {
			for (ProbeSequenceSimplified seq: newSequences) {
				makeScenario(system, seq.getRemainingProbes(), seq.getRemainingDiagnoses(), seq.getEvidenceMap(), allScenarios, seq);
			}
		}
		allScenarios.getBranches().addAll(result.getBranches());
	}
/*	
	//a scenario holds one possible flow of probes. The elements are particular lists of states 
		private static List<List<List<State>>> makeAllScenarios(List<Variable> probes, List<List<List<State>>> scenarios, boolean onlyfirstprobe) {
			List<List<List<State>>> allscenarios = new ArrayList<List<List<State>>>();
			int max = probes.size();
			if (onlyfirstprobe) {
				max = 1;
			}		
			for (var i = 0; i < max; i++) {
				Variable p = probes.get(i);
				System.out.println("Probe = " + p);
				//set start of scenario
				List<List<State>> scenario = new ArrayList<List<State>>();
				for (State s: p.getStates()) {
					List<State> lst = new ArrayList<State>();
					lst.add(s);
					scenario.add(lst);
				}
				scenarios.add(scenario);
				List<Variable> tmpprobes = new ArrayList<Variable>(probes);
				tmpprobes.remove(p);
				for (var j= 1; j < probes.size(); j++) {
					scenarios = makeScenarios(p, scenario, probes, scenarios);
					System.out.println("i=" + i + "; #scenarios = " + scenarios.size());
				}
				allscenarios.addAll(scenarios);
			}
			return allscenarios;
		}
		*/
}
