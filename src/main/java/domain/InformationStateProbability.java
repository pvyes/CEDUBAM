package domain;

import com.bayesserver.State;
import com.bayesserver.inference.InconsistentEvidenceException;

public class InformationStateProbability implements InformationFunction {
	
	private InformationType informationtype;
	
	public InformationStateProbability() {
		informationtype = InformationType.STATE_PROBABILITY;
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
		EvidenceMap probeevidencemap = diagnoser.getEvidencemanager().setEvidence(probestate, true);
		informationresult.setProbeEvidence(probeevidencemap);
		informationresult.setDiagnosis(diagnosis);
		informationresult.setInformation(computeInformation(diagnoser, diagnosis));
		diagnoser.getEvidencemanager().removeEvidence(probeevidencemap);
		return informationresult;
	}
	
	/**
	 * The information for one probe is the probability of the diagnosis given the probe's result. 
	 * @return
	 * @throws Exception
	 */
	protected double computeInformation(Diagnoser diagnoser, Diagnosis diagnosis) {
		double result;
		try {
			result = diagnoser.getQuerymanager().getProbability(diagnosis.getStates());
			return result;
		} catch (Exception e) {
			System.out.println("Belief could not be retrieved in InformationStateProbability.computeInformation. " + e.getMessage());
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
		informationresult.setDiagnosisProbability(getDgProb(diagnoser, diagnosis));
		informationresult.setInformation(computeExpectedInformation(diagnoser, probe, diagnosis));
		return informationresult;
	}
	
	private Double computeExpectedInformation(Diagnoser diagnoser, Probe probe, Diagnosis diagnosis)	throws Exception {
		double result = 0;
		for (State state: probe.getTarget().getStates()) {
			double stateprob = diagnoser.getQuerymanager().getProbability(diagnoser.getEvidencemanager().getActiveEvidence(), state);
			EvidenceMap probeevidencemap = diagnoser.getEvidencemanager().setEvidence(state, true);
			double info = this.computeInformation(diagnoser, diagnosis);
			diagnoser.getEvidencemanager().removeEvidence(probeevidencemap);
			//double exp = stateprob * info;
			result += stateprob * info;
		}			
		return result;
	}
	
	private double getDgProb(Diagnoser diagnoser, Diagnosis diagnosis) throws InconsistentEvidenceException {
		return diagnoser.getQuerymanager().getProbability(diagnoser.getEvidencemanager().getActiveEvidence(), diagnosis.getStates());
	}
}
