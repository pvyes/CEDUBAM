package domain;

import java.util.ArrayList;
import java.util.List;

import com.bayesserver.State;
import com.bayesserver.Variable;
import com.bayesserver.inference.Evidence;
import com.bayesserver.inference.InconsistentEvidenceException;
import com.bayesserver.statistics.LogarithmBase;

public class InformationEntropyDifference implements InformationFunction {
	
	private final LogarithmBase LOG_BASE = LogarithmBase.TWO;
	//private SProbeResult proberesult;
	private InformationType informationtype;

	public InformationEntropyDifference() {
		informationtype = InformationType.ENTROPY_DIFFERENCE;
	}
/*
	public SProbeResult getProberesult() {
		return proberesult;
	}

	public void setProberesult(SProbeResult proberesult) {
		this.proberesult = proberesult;
	}
*/		
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
		informationresult.setInformation(computeInformation(diagnoser, diagnosis, probestate));
		return informationresult;
	}
	
	/**
	 * The information for one probe is the probability of the diagnosis given the probe's result. 
	 * @return
	 * @throws Exception
	 */
	protected double computeInformation(Diagnoser diagnoser, Diagnosis diagnosis, State probestate) throws Exception {
		double result;
		List<Variable> probevars = new ArrayList<Variable>();
		probevars.add(probestate.getVariable());
		List<Variable> dgvars = new ArrayList<Variable>();
		dgvars.addAll(diagnosis.getVariables());
		//get activeevidence
		Evidence reference = diagnoser.getEvidencemanager().getActiveEvidence();
		//activate probe evidence
		EvidenceMap probeveidencemap = diagnoser.getEvidencemanager().setEvidence(probestate, true);
		Evidence second = diagnoser.getEvidencemanager().getActiveEvidence();
		diagnoser.getEvidencemanager().removeEvidence(probeveidencemap);
		try {
			result = diagnoser.getQuerymanager().getKLDifference(dgvars, reference, second, LOG_BASE);
			return result;
		} catch (Exception e) {
			System.out.println("Belief could not be retrieved in InformationEntropyDifference.computeInformation. " + e.getMessage());
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public InformationResult getExpectedInformation(Diagnoser diagnoser, Probe probe, Diagnosis diagnosis)	throws Exception {
		InformationResult informationresult = new InformationResult(informationtype);
		informationresult.setProbe(probe);
		informationresult.setInformationType(informationtype);
		informationresult.setBaseEvidence(diagnoser.getEvidencemanager().getEvidences(true));
		informationresult.setDiagnosis(diagnosis);
		informationresult.setDiagnosisProbability(getDgProb(diagnoser, diagnosis, probe));
		informationresult.setInformation(computeExpectedInformation(diagnoser, probe, diagnosis));
		return informationresult;
	}
	
	private Double computeExpectedInformation (Diagnoser diagnoser, Probe probe, Diagnosis diagnosis) {
		double result;
		List<Variable> probevars = new ArrayList<Variable>();
		probevars.add(probe.getTarget());
		List<Variable> dgvars = new ArrayList<Variable>();
		dgvars.addAll(diagnosis.getVariables());
		//do not compute H over the variables known by the probe
		dgvars.removeAll(probevars);
		if (dgvars.size() == 0) {
//			return 0.0;
		}	
		//get activeevidence
		Evidence reference = diagnoser.getEvidencemanager().getActiveEvidence();
		try {
			//result = diagnoser.getQuerymanager().getMutualInformation(reference, probevars, diagnosis, LOG_BASE);
			result = diagnoser.getQuerymanager().getMutualInformation(reference, probevars, diagnosis, dgvars, LOG_BASE);
			return result;
		} catch (Exception e) {
			System.out.println("Belief could not be retrieved in InformationEntropyDifference.computeInformation. " + e.getMessage());
			e.printStackTrace();
		}
		return -1.0;
	}
		
	private double getDgProb(Diagnoser diagnoser, Diagnosis diagnosis, Probe probe) throws InconsistentEvidenceException {
		List<State> states = new ArrayList<State>(diagnosis.getStates());
		states.removeAll(probe.getTarget().getStates());
		return diagnoser.getQuerymanager().getProbability(diagnoser.getEvidencemanager().getActiveEvidence(), states);
	}
}
