package domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class UtilityResult {
	private InformationResult informationresult;
	private double utility;
	private Map<String, Double> constants;
	private Map<String, Pair<Boolean, Double>> infoprevalences;
	
	public UtilityResult() {
		
	}
	
	public UtilityResult(InformationResult informationresult) {
		this.informationresult = informationresult;
	}
	
	public UtilityResult(InformationResult informationresult, double utility) {
		this.informationresult = informationresult;
		this.utility = utility;
	}

	public InformationResult getInformationresult() {
		return informationresult;
	}

	public void setInformationresult(InformationResult informationresult) {
		this.informationresult = informationresult;
	}

	public double getUtility() {
		return utility;
	}

	public void setUtility(double utility) {
		this.utility = utility;
	}

	public Probe getProbe() {
		return informationresult.getProbe();
	}
	
	public Map<String, Double> getConstants() {
		return constants;
	}

	public void setConstants(Map<String, Double> constants) {
		Map<String, Double> newmap = new HashMap<String, Double>();
		for (Entry<String, Double> entry: constants.entrySet()) {
			newmap.put(entry.getKey(), entry.getValue());
		}
		this.constants = newmap;
	}
	
	public void setInfoprevalences(Map<String, Pair<Boolean, Double>> usedAlphas) {
		this.infoprevalences = usedAlphas;	
	}

	public Map<String, Pair<Boolean, Double>> getInfoprevalences() {
		return infoprevalences;
	}

	@Override
	public String toString() {
		return "[probe: " + informationresult.getProbe().getTarget() + " " +  informationresult.toString() + ", utility=" + utility + "]";
	}

		
}
