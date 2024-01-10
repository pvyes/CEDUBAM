package domain;

import java.util.Collection;
import java.util.HashSet;

/**
 * Class to hold the information results for a probe and a particular subset. 
 * Be sure to set all variables in order to facilitate further processing of the results, done by interface <code>InformationFunctionResultProcessor</code>.
 * A possible workflow used by the strategy is:
 * 1. Make the proberesults for each diagnosis and each proberesult before and after the test. 
 * 2. Reduce the proberesults to the ones needed for calculating the information using <code>InformationFunctionResultProcessor.getResultsToUse</code>.
 * 3. Make inforesults for each result using <code>InformationFunction.setprobeResult</code> and <code>InformationFunction.getInformation</code>.
 * 4. Make an informationresult for each probe apart, using the inforesults, possibly weighted by the probability of the result, gathering the used informationresults, using <code>InformationFunctionResultProcessor.getInformationResultPerProbe</code>  
 * 
 * @author Peter
 */

public class InformationResult {
	private InformationType informationtype;
	private double information;
	private Probe probe;
	private EvidenceMap probeevidence;
	private Collection<EvidenceMap> baseevidence;
	private Diagnosis diagnosis;
	private double diagnosisProbability;
	private Collection<InformationResult> usedInformationResults; //if the information is obtained using other inforesults
	
	public InformationResult(InformationType informationtype) {
		this.usedInformationResults = new HashSet<InformationResult>();
		this.informationtype = informationtype;
	}

	public InformationType getInformationType() {
		return informationtype;
	}

	public Diagnosis getDiagnosis() {
		return diagnosis;
	}
	
	public void setDiagnosis(Diagnosis diagnosis) {
		this.diagnosis = diagnosis;
	}
	
	public void setInformationType(InformationType informationtype) {
		this.informationtype = informationtype;
	}

	public double getInformation() {
		return information;
	}

	public void setInformation(double information) {
		this.information = information;
	}

	public Probe getProbe() {
		return probe;
	}

	public void setProbe(Probe probe) {
		this.probe = probe;
	}
		
	public EvidenceMap getProbeEvidence() {
		return probeevidence;
	}
	
	public void setProbeEvidence(EvidenceMap probeevidence) {
		this.probeevidence = probeevidence;
	}
	
	public Collection<EvidenceMap> getBaseevidence() {
		return baseevidence;
	}
	
	public void setBaseEvidence(Collection<EvidenceMap> baseevidence) {
		this.baseevidence = baseevidence;
	}

	public Collection<InformationResult> getUsedInformationResults() {
		return usedInformationResults;
	}

	public void setUsedInformationResults(Collection<InformationResult> usedInformationResults) {
		this.usedInformationResults = usedInformationResults;
	}

	@Override
	public String toString() {
		if (probeevidence == null) {
			return "diagnosis:" + diagnosis + ", information=" + information + "]";
		} else {
			return "diagnosis:" + diagnosis + " probevalues=" + probeevidence + ", information=" + information + "]";
		}
	}

	public void setDiagnosisProbability(double dgprob) {
		this.diagnosisProbability = dgprob;		
	}

	public double getDiagnosisProbability() {
		return diagnosisProbability;
	}	
}
