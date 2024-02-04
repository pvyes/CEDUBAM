package domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.bayesserver.State;
import com.bayesserver.StateCollection;
import com.bayesserver.Variable;

public class ProbeSequence {
	
	static enum Status {
		DIAGNOSIS_FOUND,
		FINISHED,
		NEXT_ROUND,
		CONTINUED
	}

	private Diagnoser diagnoser;
	private State currentstate;
	private List<State> usedStates;
	private List<EvidenceMap> baseEvidence;
	private EvidenceMap stateEvidence;
	private Map<Diagnosis, Double> diagnosesProbabilities;
	private Collection<Probe> remainingProbes;
	private Collection<Diagnosis> remainingDiagnoses;
	private List<Diagnosis> foundDiagnoses;
	private Status status;
	private double stateprobability;
	private MEUResult meuresult;
	private List<MEUResult> previousResults;
	//private Collection<Variable> pdvs;
	
	public ProbeSequence(Diagnoser diagnoser, List<EvidenceMap> baseevidences) {
		this.diagnoser = diagnoser;
		init(baseevidences);
	}
	
	public ProbeSequence(Diagnoser diagnoser, List<EvidenceMap> baseevidences, Collection<Probe> remainingprobes, Collection<State> usedstates, Collection<Diagnosis> remainingdiagnoses, Status status, double stateprobability, MEUResult meuresult, List<MEUResult> previousResults) {
		this.diagnoser = diagnoser;
		init(baseevidences);
		init(remainingprobes, usedstates, remainingdiagnoses, status, stateprobability, meuresult, previousResults);
	}

	private void init(List<EvidenceMap> baseevidences) {
		this.baseEvidence = new ArrayList<EvidenceMap>();
		this.baseEvidence.addAll(baseevidences);
		this.remainingProbes = new ArrayList<Probe>();
		this.usedStates = new ArrayList<State>();
		this.remainingDiagnoses = new ArrayList<Diagnosis>();
		this.foundDiagnoses = new ArrayList<Diagnosis>();
		this.diagnosesProbabilities = new HashMap<Diagnosis, Double>();
		this.status = Status.NEXT_ROUND;
		this.stateprobability = 1;
		this.previousResults = new ArrayList<MEUResult>();
	}
	
	private void init(Collection<Probe> remainingprobes, Collection<State> usedstates, Collection<Diagnosis> remainingdiagnoses, Status status, double stateprobability, MEUResult meuresult, List<MEUResult> previousResults) {
		this.remainingProbes = new ArrayList<Probe>(remainingprobes);
		this.usedStates = new ArrayList<State>(usedstates);
		this.remainingDiagnoses = new ArrayList<Diagnosis>(remainingdiagnoses);
		this.status = status;
		this.stateprobability = stateprobability;
		this.meuresult = meuresult;
		this.previousResults = new ArrayList<MEUResult>(previousResults);
		this.previousResults.add(meuresult);
	}
	
	public void setRemainingProbes(Collection<Probe> probes) {
		this.remainingProbes = probes;
	}
	
	public Collection<Diagnosis> getRemainingDiagnoses() {
		return remainingDiagnoses;
	}

	public void setRemainingDiagnoses(Collection<Diagnosis> remainingDiagnoses) {
		this.remainingDiagnoses = remainingDiagnoses;
	}

	public Collection<Probe> getRemainingProbes() {
		return remainingProbes;
	}
	
	public void setStatus(Status status) {
		this.status = status ;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public List<State> getUsedStates() {
		return usedStates;
	}

	public void setUsedStates(List<State> usedStates) {
		this.usedStates = usedStates;
	}
	
	public State getCurrentstate() {
		return currentstate;
	}

	public void setCurrentstate(State currentstate) {
		this.currentstate = currentstate;
	}

	public EvidenceMap getStateEvidence() {
		return stateEvidence;
	}

	public void setStateEvidence(EvidenceMap stateEvidence) {
		this.stateEvidence = stateEvidence;
	}
	
	public List<Diagnosis> getFoundDiagnoses() {
		return foundDiagnoses;
	}

	public double getStateprobability() {
		return stateprobability;
	}

	public List<Probe> getUsedProbes() {
		List<Probe> result = new ArrayList<Probe>();
		List<Variable> variables = getUsedVariables();
		for (Variable var: variables) {
			result.add(diagnoser.getProbe(var));
		}
		return result;
	}

	private List<Variable> getUsedVariables() {
		List<Variable> result = new ArrayList<Variable>();
		for (State s: usedStates) {
			result.add(s.getVariable());
		}
		return result;
	}
	
	public ProbeSequence run(State state) throws Exception {
		currentstate = state;
		//set evidence on the used states (before the probe)
		setUsedStatesEvidence();
		//prepare evidence for the new probe
		stateEvidence = diagnoser.getEvidencemanager().setEvidence(state, false);
		setStateProbability();
		if (this.stateprobability == 0) {
			status = Status.FINISHED;
		} else {
			//make evidence for the new probe
			stateEvidence = diagnoser.getEvidencemanager().setEvidence(state, false);
			//activate state evidence
			stateEvidence.activateEvidence(true);
			//get probabilities of the diagnoses
			for (Diagnosis dg: remainingDiagnoses) {
				double prob = diagnoser.getQuerymanager().getProbability(dg.getStates());
				diagnosesProbabilities.put(dg, prob);
			}
			
			//set founddiagnois (P=1) or remaining diagnoses (P!=0)
			setFoundAndRemainingDiagnoses();
			if (foundDiagnoses.size() > 0) {
				status = Status.DIAGNOSIS_FOUND;
			} else {
				status = Status.NEXT_ROUND;
			}
		}
		//update used states and probes
		remainingProbes.remove(diagnoser.getProbe(state.getVariable()));
		usedStates.add(state);
		removeSuperfluousProbes();
		//reset evidence
		removeUsedStatesEvidence();
		return this;
	}

	private void removeUsedStatesEvidence() {
		for (State s: usedStates) {
			EvidenceMap ev = diagnoser.getEvidencemanager().getEvidence(s.getVariable());
			diagnoser.getEvidencemanager().removeEvidence(ev);
		}
	}

	private void setUsedStatesEvidence() throws Exception {
		for (State s: usedStates) {
			diagnoser.getEvidencemanager().setEvidence(s, true);
		}	
	}

	private void removeSuperfluousProbes() throws Exception {
		Collection<Probe> toremove = new ArrayList<Probe>();
		for (Probe probe: remainingProbes) {
			StateCollection states = probe.getTarget().getStates();
			for (int i = 0; i < states.size(); i++) {
				double prob = diagnoser.getQuerymanager().getProbability(states.get(i));
				if (prob == 1.0) {
					toremove.add(probe);
					i = states.size();
				}
			}	
		}
		remainingProbes.removeAll(toremove);
	}

	/**
	 * Activates the baseEvidence of the probesequence, no matter the value of 'active'. If a state is given, also the hard evidence on this state is set in the status of 'active'.
	 * @param state
	 * @param active
	 * @throws Exception
	 */
	public void activateStateEvidence(State state, boolean active) throws Exception {
		if (state != null) {
			stateEvidence.setActivated(active);
		}
	}
	
	private void setFoundAndRemainingDiagnoses() {
		for (Entry<Diagnosis, Double> entry: diagnosesProbabilities.entrySet()) {
			if (entry.getValue() == 1.0) {
				foundDiagnoses.add(entry.getKey());
			}
			if (entry.getValue() == 0.0) {
				remainingDiagnoses.remove(entry.getKey());
			}
		}
	}

	private void setStateProbability() throws Exception {
		if (currentstate != null) {
			//compute probability
			double prob = diagnoser.getQuerymanager().getProbability(currentstate);
			this.stateprobability = this.stateprobability * prob;
		}
	}
	
	public ProbeSequence getClone() {
		ProbeSequence clone = new ProbeSequence(this.diagnoser, this.baseEvidence, this.remainingProbes, this.usedStates, this.remainingDiagnoses, this.status, this.stateprobability, this.meuresult, this.previousResults);
		return clone;
	}

	public void setMEUResult(MEUResult meuresult) {
		this.meuresult = meuresult;
	}

	public MEUResult getMeuresult() {
		return meuresult;
	}

	public List<MEUResult> getPreviousResults() {
		return previousResults;
	}
}
