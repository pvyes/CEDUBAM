package domain;

import java.util.ArrayList;
import java.util.List;

import com.bayesserver.State;
import com.bayesserver.Variable;

public class Diagnosis {
	List<State> states;
	int index;
	
	
	public Diagnosis(List<State> statelist) {
		this.states = statelist;
	}

	//getEvidenceMap
	public List<State> getStates() {
		return states;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public List<Variable> getVariables() {
		List<Variable> vars = new ArrayList<Variable>();
		for (State s:states) {
			vars.add(s.getVariable());
		}
		return vars;
	}

	@Override
	public String toString() {
		return "[" + index + "] " + "[" + getStatesString() + "]";
	}

	private String getStatesString() {
		String str = "";
		for (State s: states) {
			str += s.getVariable().getName() + ":" + s.getName() + ", ";
		}
		return str.substring(0, str.length() - 2);
	}
}
