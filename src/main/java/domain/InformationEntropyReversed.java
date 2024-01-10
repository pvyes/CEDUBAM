package domain;

import java.util.ArrayList;
import java.util.List;

import com.bayesserver.State;
import com.bayesserver.Variable;
import com.bayesserver.inference.InconsistentEvidenceException;
import com.bayesserver.statistics.LogarithmBase;

public class InformationEntropyReversed implements InformationFunction {
	
	private final LogarithmBase LOG_BASE = LogarithmBase.TWO;
	private InformationType informationtype;
	
	public InformationEntropyReversed() {
		informationtype = InformationType.ENTROPY_REVERSED;
	}

	public InformationType getInformationtype() {
		return informationtype;
	}

	@Override
	public String toString() {
		return informationtype.toString();
	}

	@Override
	public InformationResult getInformation(Diagnoser diagnoser, Probe probe, State probestate, Diagnosis diagnosis) throws Exception {
		InformationResult informationresult = new InformationResult(informationtype);
		informationresult.setProbe(probe);
		informationresult.setInformationType(informationtype);
		informationresult.setBaseEvidence(diagnoser.getEvidencemanager().getEvidences(true));
		informationresult.setDiagnosis(diagnosis);
		informationresult.setDiagnosisProbability(getDgProb(diagnoser, diagnosis));
		double entropy = computeInformation(diagnoser, diagnosis, probestate);
		double info = reverseEntropy(entropy, diagnosis);
		informationresult.setInformation(info);
		return informationresult;
	}
	
	
	/**
	 * The information for one probe is the probability of the diagnosis given the probe's result. 
	 * @return
	 * @throws Exception
	 */
	protected double computeInformation(Diagnoser diagnoser, Diagnosis diagnosis, State probestate) {
		double result;
		List<Variable> probevars = new ArrayList<Variable>();
		List<Variable> dgvars = new ArrayList<Variable>();
		dgvars.addAll(diagnosis.getVariables());
		//do not compute H over the variables known by the probe
		dgvars.removeAll(probevars);
		if (dgvars.size() == 0) {
			return 0;
		}	
		try {
			EvidenceMap probeev = diagnoser.getEvidencemanager().setEvidence(probestate, true);
			result = diagnoser.getQuerymanager().getEntropy(diagnoser.getEvidencemanager().getActiveEvidence(), dgvars, LOG_BASE);
			diagnoser.getEvidencemanager().removeEvidence(probeev);
			return result;
		} catch (Exception e) {
			System.out.println("Belief could not be retrieved in InformationEntropy.computeInformation. " + e.getMessage());
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * This method 'reverses' the entropy such that the lower the entropy the higher the informational value
	 * @param entropy
	 * @param diagnosis
	 * @return
	 */
	private double reverseEntropy(double entropy, Diagnosis diagnosis) {
		int nrOfStates = 0;
		double maxEntropy = 0;
		for (Variable var: diagnosis.getVariables()) {
			double size = (double) var.getStates().size();
			nrOfStates += size;
			double log = getLogFraction(1, size, LOG_BASE);
			maxEntropy += log;
		}
		double info = (-1 * maxEntropy - entropy) / nrOfStates;
		return info;
	}
	
	private double getLogFraction(double num, double denom, LogarithmBase base) {
		if (denom == 0 || num == 0) {
			return 0;
		}
		if (base == LogarithmBase.NATURAL) {
			return Math.log(num / denom);
		}
		if (base == LogarithmBase.TWO) {
			return (Math.log(num / denom)) / Math.log(2);
		}
		return 0;
	}

	@Override
	public InformationResult getExpectedInformation(Diagnoser diagnoser, Probe probe, Diagnosis diagnosis)	throws Exception {
		InformationResult informationresult = new InformationResult(informationtype);
		informationresult.setProbe(probe);
		informationresult.setInformationType(informationtype);
		informationresult.setBaseEvidence(diagnoser.getEvidencemanager().getEvidences(true));
		informationresult.setDiagnosis(diagnosis);
		informationresult.setDiagnosisProbability(getDgProb(diagnoser, diagnosis));
		double entropy = computeExpectedInformation(diagnoser, diagnosis, probe);
		double info = reverseEntropy(entropy, diagnosis);
		informationresult.setInformation(info);
		return informationresult;
	}
	
	/**
	 * The information for one probe is the probability of the diagnosis given the probe's result. 
	 * @return
	 * @throws Exception
	 */
	protected double computeExpectedInformation(Diagnoser diagnoser, Diagnosis diagnosis, Probe probe) {
		double result;
		List<Variable> probevars = new ArrayList<Variable>();
		probevars.add(probe.getTarget());
		List<Variable> dgvars = new ArrayList<Variable>();
		dgvars.addAll(diagnosis.getVariables());
		//do not compute H over the variables known by the probe
		dgvars.removeAll(probevars);
		if (dgvars.size() == 0) {
			return 0;
		}	
		try {
			result = diagnoser.getQuerymanager().getEntropy(diagnoser.getEvidencemanager().getActiveEvidence(), dgvars, probevars, LOG_BASE);
			return result;
		} catch (Exception e) {
			System.out.println("Belief could not be retrieved in InformationEntropy.computeInformation. " + e.getMessage());
			e.printStackTrace();
		}
		return -1;
	}

	private double getDgProb(Diagnoser diagnoser, Diagnosis diagnosis) throws InconsistentEvidenceException {
		return diagnoser.getQuerymanager().getProbability(diagnoser.getEvidencemanager().getActiveEvidence(), diagnosis.getStates());
	}
}
