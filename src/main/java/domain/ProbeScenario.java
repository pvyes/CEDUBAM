package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.bayesserver.State;

public class ProbeScenario {
	private Diagnoser diagnoser;
	private List<ProbeSequence> branches;
	private STimer timer;
	
	public ProbeScenario(Diagnoser diagnoser) {
		this.diagnoser = diagnoser;
		this.branches = new ArrayList<ProbeSequence>();
		timer = new STimer();
	}

	public STimer getTimer() {
		return timer;
	}

	public List<ProbeSequence> getBranches() {
		return branches;
	}

	public void addBranch(ProbeSequence branch) {
		this.branches.add(branch);
	}
	
	/*
	 * Returns a clone of each unfinished sequence (status == next-round).
	 */
	public List<ProbeSequence> cloneUnfinishedBranches() throws Exception {
		List<ProbeSequence> result = new ArrayList<ProbeSequence>();
		for (ProbeSequence branch: getUnfinishedBranches()) {
			result.add(branch.getClone());
			branch.setStatus(ProbeSequence.Status.CONTINUED);
		}
		return result;
	}
	
	public List<ProbeSequence> getUnfinishedBranches() {
		List<ProbeSequence> result = new ArrayList<ProbeSequence>();
		for (ProbeSequence branch: branches) {
			if (branch.getStatus() == ProbeSequence.Status.NEXT_ROUND) {
				result.add(branch);
			}
		}
		return result;
	}
	
	/*
	 * Returns all finished sequence (status == found or finished).
	 */
	public List<ProbeSequence> getFinishedBranches() {
		List<ProbeSequence> result = new ArrayList<ProbeSequence>();
		for (ProbeSequence branch: branches ) {
			if (branch.getStatus() == ProbeSequence.Status.DIAGNOSIS_FOUND || branch.getStatus() == ProbeSequence.Status.FINISHED) {
				result.add(branch);
			}
		}
		return result;
	}
	
	public double getExpectedCost() {
		double cost = 0;
		for (ProbeSequence branch: branches) {
			double probecost = 0;
			for (Probe probe: branch.getUsedProbes()) {
				probecost += probe.getCost();
			}
			cost += probecost * branch.getStateprobability();
		}
		return cost;
	}
	
	public String makeReport(int detaillevel) throws Exception {
		String str = "";
		if (detaillevel <= 0) {
			str += makeMinimalReport();
		}
		if (detaillevel == 1) {
			str += makeReport();
		}
		if (detaillevel == 2) {
			str += makeDetailedReport(detaillevel);
		}
		if (detaillevel >= 3) {
			str += makeDetailedReport(detaillevel);
		}
		
		str += "Computing time: " + timer.getElapsedMsTime() + "ms" + "\n";
		
		return str;
	}

	private String makeReport() {
		String str = "";
		str += makeSettingsReport();
		str += makeFunctionSettingsReport();	
		str += makeSummaryReport();
		return str;
	}

	private String makeSummaryReport() {
		String str = "";
		str += "Summary:\n";
		str += "*******\n";		
		str += "Expected cost: " + getExpectedCost() + "\n\n";
		if (getFinishedBranches().size() > 0) {
			str += "Finished Branches (" + getFinishedBranches().size() + "):\n";
			str += "**********************\n";			
			for (ProbeSequence branch: getFinishedBranches()) {
				str += "[" + branches.indexOf(branch) + "] " +  "status: " + branch.getStatus() + "; ";
				str += "diagnoses found:" + branch.getFoundDiagnoses() + "\n";
				str += "probe states: ";
				str += "[";
				for (State s: branch.getUsedStates()) {
					str += s.getVariable().getName() + ":" + s.getName() + ", ";
				}
				str = str.substring(0, str.length() - 2);
				str += "]\n";
				str += "probability: " + branch.getStateprobability() + "\n\n";
			}
			str += "\n";
		}
		if (getUnfinishedBranches().size() > 0) {
			str += "Unfinished Branches (" + getUnfinishedBranches().size() + "): \n";
			str += "************************\n";			
			for (ProbeSequence branch: getUnfinishedBranches()) {
				str += "[" + branches.indexOf(branch) + "] " +  "status: " + branch.getStatus() + "; ";
				str += "diagnoses found:" + branch.getFoundDiagnoses() + "\n";
				str += "probe states:" + branch.getFoundDiagnoses() + "\n";
				str += "[";
				for (State s: branch.getUsedStates()) {
					str += s.getVariable().getName() + ":" + s.getName() + ",";
				}
				str = str.substring(0, str.length() - 1);
				str += "]\n";
				str += "probability = " + branch.getStateprobability() + "\n\n";
			}
		}
		return str;
	}

	private String makeFunctionSettingsReport() {
		String str = "";
		str += "Possible diagnoses:\n";
		str += "*******************\n";
		for (Diagnosis dg: diagnoser.getPossibleDiagnoses()) {
			str += dg.toString() + "\n";
		}
		str +="\n";
		str += "Probes:\n";
		str += "*******\n";
		for (Probe p: diagnoser.getProbes()) {
			str += p.toString() + "\n";
		}
		str +="\n";
		return str;
	}
	public String makeMinimalReport() throws Exception {
		String str = makeSettingsReport();		
		str += makeMinimalSummary();
		return str;
	}

	private String makeMinimalSummary() {
		String str = "";
		str += "Summary:\n";
		str += "*******\n";		
		str += "Expected cost: " + getExpectedCost() + "\n\n";
		if (getFinishedBranches().size() > 0) {
			str += "Finished Branches (" + getFinishedBranches().size() + "):\n";
			str += "**********************\n";			
			for (ProbeSequence branch: getFinishedBranches()) {
				str += "[" + branches.indexOf(branch) + "] ";
				str += "probe states: ";
				str += "[";
				for (State s: branch.getUsedStates()) {
					str += s.getVariable().getName() + ":" + s.getName() + ", ";
				}
				str = str.substring(0, str.length() - 2);
				str += "]\n";
			}
		}
		str += "\n";
		return str;
	}

	private String makeSettingsReport() {
		String str = "";
		str += "*********************\n";
		str += "Probe Scenario Report\n";
		str += "*********************\n\n";
		str += "Settings:\n";
		str += "*********\n";
		str += "System: " + diagnoser.getSystem().getName() + "\n";
		str += "Problem defining variables: " + diagnoser.getPdvs() + "\n";
		str += "Problem defining states: " + diagnoser.getPdvsEvidence() + "\n";
		if (diagnoser.getInformationfunction() != null ) {
			str += "Informationfunction: " + diagnoser.getInformationfunction().getInformationtype() + "\n";
		}
		if (diagnoser.getUtilityfunction() != null ) {
			str += "Utility function: " + diagnoser.getUtilityfunction().getName() + "\n";
		}
		if (diagnoser.getStrategy() != null ) {			
			str += "Strategy: " + diagnoser.getStrategy().getName() + "\n";
		}
		str +="\n";
		return str;
	}
	
	public String makeDetailedReport(int detaillevel) throws Exception {
		String str = makeReport();
		str += makeComputingDetailsReport(detaillevel);
		
		return str;
	}

	private String makeComputingDetailsReport(int detaillevel) {
		String str = "";
		str += "\n";
		str += "***************************\n";
		str += "Details of the calculations\n";
		str += "***************************\n";
		str += "\n\n";
		if (diagnoser.getStrategy().getName().equals(StrategyName.MEU)) {
			for (ProbeSequence branch: branches) {
				str += "Branch " + branches.indexOf(branch) + "\n";
				str += "***********\n";
				str += "[" + branches.indexOf(branch) + "] " +  "status: " + branch.getStatus() + "; ";
				str += "diagnoses found:" + branch.getFoundDiagnoses() + "\n";
				str += "probe states: ";
				str += "[";
				for (State s: branch.getUsedStates()) {
					str += s.getVariable().getName() + ":" + s.getName() + ", ";
				}
				str = str.substring(0, str.length() - 2);
				str += "]\n";
				str += "probability: " + branch.getStateprobability() + "\n\n";
				
				str += getMEUReport(branch.getMeuresult());
				str += "\n\n";
				
				if (detaillevel > 2) {
					str += "History of the MEUs\n";
					str += "-------------------\n\n";
					for (MEUResult res: branch.getPreviousResults()) {
						str += "[" + branch.getPreviousResults().indexOf(res) + "] MEU History\n";
						str += getMEUReport(res);
						str += "\n\n";
					}
				}
			}
		} else {
			str += "Details are not provided for strategy " + diagnoser.getStrategy();
		}
		return str;
	}

	private String getMEUReport(MEUResult smeuResult) {
		String str = "";
		str += "Maximum expected utility\n";
		str += smeuResult.toString() + "\n\n";
		
		str += "Weighted sum expected utility per probe\n";
		for (Entry<Probe, Double> entry: smeuResult.getSumPerProbe().entrySet()) {
			str += "\t" + entry.getKey().getName() + ": " + entry.getValue() + "\n";
		}
		str += "\n";
						
		if (smeuResult.getMeuresults() != null) {
			str += "Utilityresults for the MEU probe(s):\n";
			for (UtilityResult res: smeuResult.getMeuresults()) {
				str += "\tprobe: " + res.getProbe().getTarget() + "; probestate; " + res.getInformationresult().getProbeEvidence() + "; diagnosis: " + res.getInformationresult().getDiagnosis() + "; expected information: " + res.getInformationresult().getInformation() + "\n";
				str +=  "\tdiagnosis probability: " + res.getInformationresult().getDiagnosisProbability() + ", expected utility: " + res.getUtility() + "\n\n";
			}
		}
		
		if (smeuResult.getMeuresults() != null) {
			str += "Informationresults:\n";
			for (UtilityResult res: smeuResult.getUtilityresults()) {
				InformationResult ir = res.getInformationresult();
				str += "\tprobe: " + ir.getProbe()+ "; probestate; " + ir.getProbeEvidence() + "; diagnosis: " + ir.getDiagnosis() + "; information: " + ir.getInformation() + "\n";
			}
		}
		return str;
	}
}
