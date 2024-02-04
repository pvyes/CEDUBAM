package domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.bayesserver.State;
import com.bayesserver.Variable;
import com.bayesserver.inference.DefaultEvidence;
import com.bayesserver.inference.Evidence;

public class EvidenceManager {
	private Diagnoser diagnoser;
	private List<EvidenceMap> evidences;
	private List<NamedEvidence> namedevidences;
		
	public EvidenceManager(Diagnoser diagnoser) {
		super();
		this.diagnoser = diagnoser;
		this.evidences = new ArrayList<EvidenceMap>();
		this.namedevidences = new ArrayList<NamedEvidence>();
	}

	public Collection<NamedEvidence> getNamedEvidences() {
		return namedevidences;
	}
	
	public Collection<NamedEvidence> getNamedEvidences(Collection<String> names) {
		Collection<NamedEvidence> result = new HashSet<NamedEvidence>();
		for (String name: names) {
			NamedEvidence ne = getNamedEvidence(name);
			result.add(ne);
		}
		return result;
	}
	
	public NamedEvidence getNamedEvidence(String name) {
		for (NamedEvidence ne: namedevidences) {
			if (ne.getName().equals(name)) {
				return ne;
			}
		}
		return null;
	}
	
	public NamedEvidence addNamedEvidence(String name) {
		NamedEvidence ne = getNamedEvidence(name);
		if (ne == null) {
			ne = new NamedEvidence(diagnoser, name);
			namedevidences.add(ne);
		}
		return ne;
	}
	
	/**
	 * Only removes the named evuidence, not the evidence itself.
	 * @param name
	 */
	public void removeNamedEvidence(String name) {
		namedevidences.remove(getNamedEvidence(name));
	}
	
	public Collection<EvidenceMap> getEvidences() {
		return evidences;
	}
	
	public EvidenceMap setEvidence(Variable variable, Map<State, Double> statevalues, boolean activate) throws Exception {
		EvidenceMap ev = setEvidence(variable, statevalues);
		ev.activateEvidence(activate);
		return ev;
	}
	
	public EvidenceMap setEvidence(Variable variable, Map<State, Double> statevalues) throws Exception {
		EvidenceMap ev = getEvidence(variable);
		boolean newev = false;
		if (ev == null) {
			ev = new EvidenceMap(diagnoser, variable);
			newev = true;
		}
		for (Entry<State, Double> entry: statevalues.entrySet()) {
			ev.setEvidence(entry.getKey(), entry.getValue());
		}
		if (newev) {
			evidences.add(ev);
		}
		return ev;
	}
	
	public EvidenceMap setEvidence(State state, boolean activate) throws Exception {
		Variable variable = state.getVariable();
		EvidenceMap ev = getEvidence(variable);
		boolean newev = false;
		if (ev == null) {
			ev = new EvidenceMap(diagnoser, variable);
			newev = true;
		}
		for (State s: state.getVariable().getStates()) {
			int value = 0;
			if (s == state) {
				value = 1;
			}
			ev.setEvidence(s, value);
		}
		ev.activateEvidence(activate);
		if (newev) {
			evidences.add(ev);
		}
		return ev;
	}
	
	public EvidenceMap setEvidence(Variable variable, List<State> states, List<Double> values, boolean activate) throws Exception {
		EvidenceMap ev = getEvidence(variable);
		boolean newev = false;
		if (ev == null) {
			ev = new EvidenceMap(diagnoser, variable);
			newev = true;
		}
		ev.setEvidence(states, values, activate);
		if (newev) {
			evidences.add(ev);
		}
		return ev;
	}
	
	public EvidenceMap getEvidence(Variable variable) {
		for (EvidenceMap em: evidences) {
			if (em.getVariable() == variable) {
				return em;
			}
		}
		return null;
	}
	
	public EvidenceMap getEvidence(int variableindex) {
		for (EvidenceMap em: evidences) {
			if (em.getVariable().getIndex() == variableindex) {
				return em;
			}
		}
		return null;
	}
	
	public List<EvidenceMap> getEvidences(boolean active) {
		List<EvidenceMap> result = new ArrayList<EvidenceMap>();
		for (int i = 0; i < evidences.size(); i++) {
			EvidenceMap em = evidences.get(i);
			if (em.isActivated() == active) {
				result.add(em);
			}
		}
		return result;
	}

	public EvidenceMap getEvidenceById(int evidenceindex) {
		return evidences.get(evidenceindex);
	}
	
	public void activateEvidence(Variable variable, boolean activate) throws Exception {
		getEvidence(variable).activateEvidence(activate);
	}
	
	public void activateAllEvidences(boolean activate) throws Exception {
		for (EvidenceMap ev: evidences) {
			ev.activateEvidence(activate);
		}
	}
	
	public Evidence getActiveEvidence() {
		Evidence evidence = new DefaultEvidence(diagnoser.getSystem());
		for (EvidenceMap varEvidence: getEvidences(true)) {
			Variable var = varEvidence.getVariable();
			double[] stateValues = varEvidence.getStateValues(varEvidence.getEvidenceMap());
			evidence.setStates(var, stateValues);
		}	
		return evidence;
	}

	public void removeEvidence(EvidenceMap evidence) {
		if (evidence != null) {
			evidence.setActivated(false);
			evidences.remove(evidence);
			for (NamedEvidence ne: namedevidences) {
				for (EvidenceMap em: ne.getEvidences()) {
					if (em == evidence) {
						ne.getEvidences().remove(evidence);
					}
				}
			}
		}
	}
	
	public void clearEvidence() {
		namedevidences.clear();
		evidences.clear();
	}
	
	public void clearPdvsEvidence() {
		for (Variable v: diagnoser.getPdvs()) {
			removeEvidence(getEvidence(v));
		}
	}
}
