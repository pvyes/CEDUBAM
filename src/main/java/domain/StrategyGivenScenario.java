package domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.bayesserver.State;
import com.bayesserver.Variable;

public class StrategyGivenScenario implements Strategy {
	Diagnoser diagnoser;
	StrategyName name;
	ProbeSequence ps;
	List<List<State>> scenario;
	
	public StrategyGivenScenario(Diagnoser diagnoser) {
		this.diagnoser = diagnoser;
		this.name = StrategyName.GIVEN_SCENARIO;
		this.scenario = new ArrayList<List<State>>();
	}	

	public StrategyName getName() {
		return name;
	}

	public void setName(StrategyName name) {
		this.name = name;
	}
	
	public ProbeSequence getPs() {
		return ps;
	}

	public void setProbeSequence(ProbeSequence probesequence) {
		this.ps = probesequence;
	}
	
	public void setSequenceScenario(List<List<State>> scenario) {
		this.scenario = scenario;
	}
	
	public List<List<State>> getSequenceScenario() {
		return scenario;
	}

	@Override
	public Probe getSuggestedProbe() {
		return getNext();
	}
	
	private Probe getNext() {
		removeUnreachableScenarios();
		Collection<State> usedstatessubsets = ps.getUsedStates();
		State nextstate = null;
		if (usedstatessubsets.size() == 0) {
			nextstate = scenario.get(0).get(0);
		} else {
			List<State> se = findScenarioElement(usedstatessubsets);
			nextstate = se.get(usedstatessubsets.size());
		}
		Probe probe = null;
		if (nextstate != null) {
			probe = findProbeByState(nextstate);
		}
		return probe;
	}

	//Finds the probe where the action's targetvariable contains the variable of the state.
	//This works only good if targetvariables are exclusively divided between the actions 
	private Probe findProbeByState(State state) {
		Variable var = state.getVariable();
		List<Probe> enabledProbes = new ArrayList<Probe>(diagnoser.getProbes());
		enabledProbes.removeAll(ps.getUsedProbes());
		for (Probe p: enabledProbes) {
			if (p.getTarget() == var) {
				return p;
			}
		}
		return null;
	}

	//Returns the first element containing the usedstates
	//(There are the number of elements with these usedstates) 
	private List<State> findScenarioElement(Collection<State> usedstatessubsets) {
		for (List<State> el: scenario) {
			if (el.containsAll(usedstatessubsets)) {
				return el;
			}
		}
		return null;
	}
	
	//Returns the first element containing the usedstates
	//(There are the number of elements with these usedstates) 
	private List<List<State>> findScenarioElements(Collection<State> usedstatessubsets) {
		List<List<State>> els = new ArrayList<List<State>>();
		for (List<State> el: scenario) {
			if (el.containsAll(usedstatessubsets)) {
				els.add(el);
			}
		}
		return els;
	}
	
	/*
	 * Tries to rationalize the sequencescenarios by removing all the sequences that are not reached because a diagnosis has yet been found.
	 * 
	 */
	public List<List<State>> removeUnreachableScenarios() {
		List<List<State>> rats = new ArrayList<List<State>>();
		if (ps.getStatus() == ProbeSequence.Status.DIAGNOSIS_FOUND) {
			List<List<State>> els = findScenarioElements(ps.getUsedStates());
			System.out.println("stop");
		}
		return rats;
	}
}
