package domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class MEUResult {
	private List<UtilityResult> utilityresults;
	private Map<Probe, Collection<UtilityResult>> weightedresults;
	private Map<Probe, Double> sumperprobe;
	private List<UtilityResult> meuresults;
	private double utility;
	
	public MEUResult() {
		meuresults = new ArrayList<UtilityResult>();	
	}
	
	public MEUResult(List<UtilityResult> utilityresults) {
		this.utilityresults = utilityresults;
		meuresults = new ArrayList<UtilityResult>();
	}

	public void setUtilityresults(List<UtilityResult> utilityresults) {
		this.utilityresults = utilityresults;
	}

	public  List<UtilityResult> getUtilityresults() {
		return utilityresults;
	}

	public double getUtility() {
		return utility;
	}

	public void setUtility(double utility) {
		this.utility = utility;
	}
	
	public Collection<UtilityResult> getMeuresults() {
		return meuresults;
	}
	
	public Map<Probe, Collection<UtilityResult>> getWeightedresults() {
		return weightedresults;
	}

	public void setWeightedresults(Map<Probe, Collection<UtilityResult>> weightedresults) {
		this.weightedresults = weightedresults;
	}
	
	public Collection<Probe> getMeuProbes() {
		Collection<Probe> probes = new HashSet<Probe>();
		for (UtilityResult meu: meuresults) {
			probes.add(meu.getProbe());
		}
		return probes;
	}
	
	public void setSumPerProbe(Map<Probe, Double> sum) {
		this.sumperprobe = sum;		
	}
	
	public Map<Probe, Double> getSumPerProbe() {
		return this.sumperprobe;		
	}

	@Override
	public String toString() {
		return "MEU = " + utility + " for probe(s) " +  getMeuProbes();
	}
	
}
