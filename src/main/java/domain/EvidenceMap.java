package domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.bayesserver.State;
import com.bayesserver.Variable;
import com.bayesserver.inference.DefaultEvidence;
import com.bayesserver.inference.Evidence;

/**
 * Represents the evidence on a variable.
 * @author Peter
 *
 */

public class EvidenceMap {
	private Diagnoser diagnoser;
	private Evidence evidence;
	private Variable variable;
	private Map<State, Double> evidencemap;
	private boolean activated;
	
	public EvidenceMap(Diagnoser diagnoser, Variable variable) {
		this.diagnoser = diagnoser;
		this.variable = variable;
		evidencemap = new HashMap<State, Double>();
		for (State s: variable.getStates()) {
			evidencemap.put(s, null);
		}
	}
	
	public void setEvidence(State s, double value) throws Exception {
		if (this.getVariable() == s.getVariable() || this.getVariable() == null) {
			evidencemap.put(s, value);
		} else {
			throw new Exception("This evidencemap is meant for another variable (" + getVariable() + ").");
		}
	}
/*	
	public void setEvidence(State s, double value, boolean activated) throws Exception {
		if (this.getVariable() == s.getVariable() || this.getVariable() == null) {
			evidencemap.put(s, value);
			if (activated) {
				activateEvidence();
			}
		} else {
			throw new Exception("This evidencemap is meant for another variable (" + getVariable() + ").");
		}
	}
*/	
	public void setEvidence(List<State> states, List<Double> values, boolean activate) throws Exception {
		for (int i = 0; i < states.size(); i++) {
			setEvidence(states.get(i), values.get(i));
		}
		activateEvidence(activate);
	}


	public Evidence getEvidence() {
		if (activated) {
			return evidence;
		}
		return null;
	}
	
	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	public void activateEvidence(boolean activate) throws Exception {
		this.evidence = new DefaultEvidence(diagnoser.getSystem());
		if (activate && allstatesPresent()) {
			double[] stateValues = getStateValues(evidencemap);
			evidence.setStates(variable, stateValues);
			activated = true;
		} else {
			evidence.clear();
			activated = false;
		}
	}

	private boolean allstatesPresent() throws Exception {
		boolean allstates = true;
		double sum = 0;
		if (evidencemap.size() != variable.getStates().size()) {
			throw new Exception("Not all states of variable " + variable.getName() + " have a value.");
		}
		for (Entry<State, Double> entry: evidencemap.entrySet()) {
			sum += entry.getValue();
		}
		if (sum != 1) {
			throw new Exception("The values of the states must sum to 1. (and was " + sum + ")");
		}
		return allstates;
	}

	public Map<State, Double> getEvidenceMap() {
		return evidencemap;
	}
	
	public Variable getVariable() {
		return variable;
	}
	
	public double[] getStateValues(Map<State, Double> evidenceMap) {
		double[] result = new double[evidenceMap.size()];
		for (Entry<State, Double> entry: evidenceMap.entrySet()) {
			int i = entry.getKey().getIndex();
			result[i] = entry.getValue();
		}
		return result;
	}
	
	@Override
	public String toString() {
		String s = "";
		s += variable + ":[";
		for (Entry<State, Double> entry: evidencemap.entrySet()) {
			s += entry.getKey().getName() + "=" + entry.getValue();
			s += ";";
		}
		s = s.substring(0, s.length() - 1);
		s += "]";
		return s;
	}
}
